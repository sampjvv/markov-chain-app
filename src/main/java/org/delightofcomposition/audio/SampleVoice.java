package org.delightofcomposition.audio;

/**
 * A single note event that plays the bell sample at a given pitch.
 * Pitch is controlled via playback rate (resampling).
 * Uses a percussion envelope: instant attack, exponential decay.
 */
public class SampleVoice {
    private static final double ATTACK_TIME = 0.01;   // 10ms attack (SC default)
    private static final double RELEASE_TIME = 5.0;   // 5s release
    private static final double SAMPLE_RATE = 48000.0;
    private static final double ENV_THRESHOLD = 0.001;
    private static final double ENV_CURVE = -4.0;      // SC Env.perc default curve

    private double position;       // fractional sample index
    private double playbackRate;   // targetFreq / origFreq
    private double amplitude;
    private long sampleCount;      // for envelope
    private boolean alive;
    private double pan;            // -1 to 1
    private int stratumIndex = -1;
    private double currentEnvelope;

    public SampleVoice() {
        this.alive = false;
    }

    /** Initialize/reuse this voice for a new note. */
    public void trigger(double frequency, double amplitude, double pan, double origFreq, int stratumIndex) {
        this.stratumIndex = stratumIndex;
        this.position = 0;
        // SC multiplies freq by 4.0 to shift all strata up 2 octaves into audible range.
        // Without this, stratum 0 (C3) produces 32 Hz — inaudible on most speakers.
        // With it: stratum 0 sounds at ~131 Hz (C3), stratum 5 at ~8kHz (C9).
        this.playbackRate = (frequency * 4.0) / origFreq;
        // Scale amplitude to prevent clipping with many concurrent voices
        this.amplitude = amplitude * 0.15;
        this.sampleCount = 0;
        this.pan = pan;
        this.alive = true;
    }

    /**
     * Render this voice into stereo mix buffers (additive).
     * Returns false if voice has died.
     */
    public boolean render(float[] sampleData, int sampleLength,
                          double[] bufL, double[] bufR, int frames) {
        if (!alive) return false;

        double attackSamples = ATTACK_TIME * SAMPLE_RATE;
        double releaseSamples = RELEASE_TIME * SAMPLE_RATE;

        for (int i = 0; i < frames; i++) {
            // SC-style Env.perc: short attack, curved release
            double env;
            if (sampleCount < attackSamples) {
                // Attack phase: linear ramp up (don't threshold-kill during attack)
                env = (sampleCount + 1) / attackSamples;
            } else {
                // Release phase: SC curve -4 = fast initial drop, long tail
                double t = (sampleCount - attackSamples) / releaseSamples;
                if (t >= 1.0) { alive = false; return false; }
                env = Math.pow(1.0 - t, Math.abs(ENV_CURVE));
                if (env < ENV_THRESHOLD) {
                    alive = false;
                    return false;
                }
            }

            // Linear interpolation for sample reading
            int idx = (int) position;
            if (idx >= sampleLength - 1) {
                alive = false;
                return false;
            }

            double frac = position - idx;
            double sample = sampleData[idx] * (1.0 - frac) + sampleData[idx + 1] * frac;

            currentEnvelope = env;
            double output = sample * env * amplitude;

            // Stereo panning (constant power)
            double panAngle = (pan + 1.0) * Math.PI / 4.0;
            bufL[i] += output * Math.cos(panAngle);
            bufR[i] += output * Math.sin(panAngle);

            position += playbackRate;
            sampleCount++;
        }

        return alive;
    }

    public boolean isAlive() { return alive; }
    public void kill() { alive = false; stratumIndex = -1; }
    public int getStratumIndex() { return stratumIndex; }
    public double getAmplitude() { return amplitude; }
    public double getEnvelope() { return currentEnvelope; }
}
