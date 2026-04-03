package org.delightofcomposition.markov;

/**
 * State for one musical stratum (layer).
 * Each stratum occupies a different octave range and has independent
 * amplitude, rhythm rate, and active state.
 */
public class Stratum {
    /** Octave base MIDI notes for the 6 strata: C3, C4, C5, C6, C7, C8 */
    public static final int[] OCTAVE_BASES = {24, 36, 48, 60, 72, 84};
    public static final String[] RANGE_LABELS = {"C3-C4", "C4-C5", "C5-C6", "C6-C7", "C7-C8", "C8-C9"};

    private int currentNoteIndex;
    private boolean active;
    private double amplitude;
    private double currentAmplitude; // smoothed amplitude (lag)
    private int rateLevel; // 0=fast, 1=medium, 2=slow
    private final int octaveBase;
    private final int index;

    public Stratum(int index) {
        this.index = index;
        this.octaveBase = OCTAVE_BASES[index];
        this.currentNoteIndex = (index % 3 == 0) ? 0 : (index % 3 == 1) ? 4 : 2; // from SC: [0,4,2,0,4,2]
        this.active = false;
        this.amplitude = 0.5;
        this.currentAmplitude = 0.5;
        this.rateLevel = 0;
    }

    /**
     * Smooth amplitude chase from SC (lines 248-252).
     * Multiplicative approach: if above target, multiply by 0.8; if below, by 1.3.
     */
    public double smoothAmplitude() {
        if (currentAmplitude > amplitude) {
            currentAmplitude *= 0.8;
        } else {
            currentAmplitude *= 1.3;
        }
        // Clamp
        if (currentAmplitude > 1.0) currentAmplitude = 1.0;
        if (currentAmplitude < 0.001) currentAmplitude = 0.001;
        return currentAmplitude;
    }

    // Getters and setters
    public int getIndex() { return index; }
    public int getOctaveBase() { return octaveBase; }
    public String getRangeLabel() { return RANGE_LABELS[index]; }
    public int getCurrentNoteIndex() { return currentNoteIndex; }
    public void setCurrentNoteIndex(int idx) { this.currentNoteIndex = idx; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public double getAmplitude() { return amplitude; }
    public void setAmplitude(double amplitude) { this.amplitude = amplitude; }
    public double getCurrentAmplitude() { return currentAmplitude; }
    public int getRateLevel() { return rateLevel; }
    public void setRateLevel(int rateLevel) { this.rateLevel = Math.max(0, Math.min(2, rateLevel)); }
}
