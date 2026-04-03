package org.delightofcomposition.markov;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Central parameter model for the Markov chain app.
 * Holds all state: matrix, scale, strata, reverb, transport.
 */
public class MarkovParameters {
    private final TransitionMatrix matrix;
    private final MarkovEngine engine;
    private Scale scale;
    private final Stratum[] strata;

    // Reverb
    private double reverbRoom = 0.7;
    private double reverbMix = 0.5;
    private double reverbDamp = 0.5;

    // Master
    private double masterVolume = 0.7;
    private double tempo = 1.0; // beats per second (1.0 = 60 BPM)

    // Range control
    private double highestNote = 96;
    private double lowestNote = 24;
    private double targetHighest = 96;
    private double targetLowest = 24;

    // Sine controller for highest note (from SC: ~sineController)
    private boolean sineControlsHighest = false;
    private double sineFrequency = 1.0 / 60.0;  // cycles per second (1 cycle per 60s)
    private double sinePhase = -Math.PI / 2;     // start phase
    private double sineMul = 0.5;                // amplitude
    private double sineAdd = 0.5;                // offset
    private long sineStartTime = 0;

    // Continuous rate: 0.0 = slow (~1.0s), 2.0 = fast (~0.1s)
    private static final double DURATION_SLOW = 1.0;
    private static final double DURATION_FAST = 0.1;

    private final List<ChangeListener> listeners = new ArrayList<>();

    public MarkovParameters() {
        this.matrix = MatrixPresets.defaultMatrix();
        this.engine = new MarkovEngine(matrix);
        this.scale = Scale.MAJOR;
        this.strata = new Stratum[6];
        for (int i = 0; i < 6; i++) {
            strata[i] = new Stratum(i);
        }
    }

    /** Apply a cue snapshot to all parameters. */
    public void applyCue(Cue cue) {
        matrix.loadFrom(cue.getMatrix());
        scale = cue.getScale();
        boolean[] active = cue.getActive();
        double[] amps = cue.getAmplitudes();
        double[] rates = cue.getRateLevels();
        for (int i = 0; i < 6; i++) {
            strata[i].setActive(active[i]);
            strata[i].setAmplitude(amps[i]);
            strata[i].setRateLevel(rates[i]);
        }
        reverbRoom = cue.getReverbRoom();
        reverbMix = cue.getReverbMix();
        reverbDamp = cue.getReverbDamp();
        fireChanged();
    }

    /** Create a cue from current state. */
    public Cue createCue(String name) {
        boolean[] active = new boolean[6];
        double[] amps = new double[6];
        double[] rates = new double[6];
        for (int i = 0; i < 6; i++) {
            active[i] = strata[i].isActive();
            amps[i] = strata[i].getAmplitude();
            rates[i] = strata[i].getRateLevel();
        }
        return new Cue(name, matrix.toArray(), scale, active, amps, rates,
                       reverbRoom, reverbMix, reverbDamp);
    }

    /** Continuous duration from rate level. 0.0=slow, 2.0=fast. Exponential mapping with random variation. */
    public double getRandomDuration(double rateLevel) {
        // Exponential interpolation: slow (1.0s) → fast (0.1s)
        double t = Math.max(0, Math.min(2, rateLevel)) / 2.0;
        double center = DURATION_SLOW * Math.pow(DURATION_FAST / DURATION_SLOW, t);
        // ±30% random variation for natural feel
        double variation = 0.7 + Math.random() * 0.6;
        return (center * variation) / tempo;
    }

    // Getters
    public TransitionMatrix getMatrix() { return matrix; }
    public MarkovEngine getEngine() { return engine; }
    public Scale getScale() { return scale; }
    public Stratum[] getStrata() { return strata; }
    public Stratum getStratum(int i) { return strata[i]; }
    public double getReverbRoom() { return reverbRoom; }
    public double getReverbMix() { return reverbMix; }
    public double getReverbDamp() { return reverbDamp; }
    public double getMasterVolume() { return masterVolume; }
    public double getTempo() { return tempo; }
    public double getHighestNote() { return highestNote; }
    public double getLowestNote() { return lowestNote; }
    public double getTargetHighest() { return targetHighest; }
    public double getTargetLowest() { return targetLowest; }

    // Setters
    public void setScale(Scale scale) { this.scale = scale; fireChanged(); }
    public void setReverbRoom(double v) { this.reverbRoom = v; }
    public void setReverbMix(double v) { this.reverbMix = v; }
    public void setReverbDamp(double v) { this.reverbDamp = v; }
    public void setMasterVolume(double v) { this.masterVolume = v; }
    public void setTempo(double v) { this.tempo = Math.max(0.1, v); }
    public void setTargetHighest(double v) { this.targetHighest = v; }
    public void setTargetLowest(double v) { this.targetLowest = v; }
    public boolean isSineControlsHighest() { return sineControlsHighest; }
    public double getSineFrequency() { return sineFrequency; }
    public void setSineFrequency(double v) { this.sineFrequency = v; }
    public double getSineMul() { return sineMul; }
    public void setSineMul(double v) { this.sineMul = v; }
    public double getSineAdd() { return sineAdd; }
    public void setSineAdd(double v) { this.sineAdd = v; }

    /** Gradually approach target highest/lowest (called each note event). */
    public void updateNoteLimits() {
        // Sine controller for highest note
        if (sineControlsHighest) {
            double elapsed = (System.currentTimeMillis() - sineStartTime) / 1000.0;
            double sineY = Math.sin(2 * Math.PI * sineFrequency * elapsed + sinePhase) * sineMul + sineAdd;
            sineY = Math.max(0, Math.min(1, sineY));
            targetHighest = sineY * 72 + 24;
        }

        if (highestNote > targetHighest) highestNote--;
        if (highestNote < targetHighest) highestNote++;
        if (lowestNote > targetLowest) lowestNote--;
        if (lowestNote < targetLowest) lowestNote++;
    }

    public void startSineController() {
        sineStartTime = System.currentTimeMillis();
        sineControlsHighest = true;
    }

    public void stopSineController() {
        sineControlsHighest = false;
    }

    /** Get the current sine value (0-1) for UI display. */
    public double getSineValue() {
        if (!sineControlsHighest) return 0.5;
        double elapsed = (System.currentTimeMillis() - sineStartTime) / 1000.0;
        double v = Math.sin(2 * Math.PI * sineFrequency * elapsed + sinePhase) * sineMul + sineAdd;
        return Math.max(0, Math.min(1, v));
    }

    /** Clamp MIDI note to within range using octave transposition. */
    public int clampMidi(int midi) {
        while (midi > highestNote) midi -= 12;
        while (midi < lowestNote) midi += 12;
        return midi;
    }

    public void addChangeListener(ChangeListener l) { listeners.add(l); }
    public void removeChangeListener(ChangeListener l) { listeners.remove(l); }
    public void fireChanged() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : listeners) l.stateChanged(e);
    }
}
