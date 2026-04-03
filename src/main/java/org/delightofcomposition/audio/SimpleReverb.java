package org.delightofcomposition.audio;

/**
 * Schroeder/Moorer algorithmic reverb with controllable room, mix, and damping.
 * 4 parallel comb filters + 2 series allpass filters.
 */
public class SimpleReverb {
    // Comb filter delay lengths (in samples at 48kHz) - mutually prime
    private static final int[] COMB_LENGTHS = {1557, 1617, 1491, 1422};
    // Allpass filter delay lengths
    private static final int[] AP_LENGTHS = {225, 556};

    private final float[][] combBuffersL, combBuffersR;
    private final int[] combIndices;
    private final float[] combFeedback;
    private final float[] combDamp;    // per-comb lowpass state
    private final float[] combDampL, combDampR;

    private final float[][] apBuffersL, apBuffersR;
    private final int[] apIndices;
    private static final float AP_GAIN = 0.5f;

    private double room = 0.7;
    private double mix = 0.5;
    private double damp = 0.5;

    public SimpleReverb() {
        combBuffersL = new float[4][];
        combBuffersR = new float[4][];
        combIndices = new int[4];
        combFeedback = new float[4];
        combDampL = new float[4];
        combDampR = new float[4];
        combDamp = new float[4];

        for (int i = 0; i < 4; i++) {
            combBuffersL[i] = new float[COMB_LENGTHS[i]];
            combBuffersR[i] = new float[COMB_LENGTHS[i]];
            combIndices[i] = 0;
        }

        apBuffersL = new float[2][];
        apBuffersR = new float[2][];
        apIndices = new int[2];
        for (int i = 0; i < 2; i++) {
            apBuffersL[i] = new float[AP_LENGTHS[i]];
            apBuffersR[i] = new float[AP_LENGTHS[i]];
            apIndices[i] = 0;
        }

        updateFeedback();
    }

    private void updateFeedback() {
        float fb = (float)(0.7 + room * 0.28); // range 0.7-0.98
        for (int i = 0; i < 4; i++) {
            combFeedback[i] = fb;
        }
    }

    public void setRoom(double room) { this.room = room; updateFeedback(); }
    public void setMix(double mix) { this.mix = mix; }
    public void setDamp(double damp) { this.damp = damp; }

    /**
     * Process audio in-place. Mixes wet signal with dry based on mix parameter.
     */
    public void process(double[] bufL, double[] bufR, int frames) {
        float dampCoeff = (float) damp;
        float wetGain = (float) mix;
        float dryGain = 1.0f - wetGain;

        for (int i = 0; i < frames; i++) {
            float inputL = (float) bufL[i];
            float inputR = (float) bufR[i];
            float wetL = 0, wetR = 0;

            // Parallel comb filters
            for (int c = 0; c < 4; c++) {
                int idx = combIndices[c];
                float outL = combBuffersL[c][idx];
                float outR = combBuffersR[c][idx];

                // Lowpass damping
                combDampL[c] = outL * (1 - dampCoeff) + combDampL[c] * dampCoeff;
                combDampR[c] = outR * (1 - dampCoeff) + combDampR[c] * dampCoeff;

                combBuffersL[c][idx] = inputL + combDampL[c] * combFeedback[c];
                combBuffersR[c][idx] = inputR + combDampR[c] * combFeedback[c];

                combIndices[c] = (idx + 1) % COMB_LENGTHS[c];
                wetL += outL;
                wetR += outR;
            }

            wetL *= 0.25f;
            wetR *= 0.25f;

            // Series allpass filters
            for (int a = 0; a < 2; a++) {
                int idx = apIndices[a];
                float bufOutL = apBuffersL[a][idx];
                float bufOutR = apBuffersR[a][idx];

                apBuffersL[a][idx] = wetL + bufOutL * AP_GAIN;
                apBuffersR[a][idx] = wetR + bufOutR * AP_GAIN;

                wetL = bufOutL - wetL * AP_GAIN;
                wetR = bufOutR - wetR * AP_GAIN;

                apIndices[a] = (idx + 1) % AP_LENGTHS[a];
            }

            bufL[i] = inputL * dryGain + wetL * wetGain;
            bufR[i] = inputR * dryGain + wetR * wetGain;
        }
    }
}
