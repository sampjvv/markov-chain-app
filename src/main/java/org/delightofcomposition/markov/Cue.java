package org.delightofcomposition.markov;

/**
 * Immutable snapshot of all parameters for a cue.
 * Contains matrix, scale, per-stratum settings, and reverb.
 */
public class Cue {
    private final String name;
    private final double[][] matrix;
    private final Scale scale;
    private final boolean[] active;     // 6 strata
    private final double[] amplitudes;  // 6 strata
    private final double[] rateLevels;  // 6 strata (continuous 0.0-2.0)
    private final double reverbRoom;
    private final double reverbMix;
    private final double reverbDamp;

    public Cue(String name, double[][] matrix, Scale scale,
               boolean[] active, double[] amplitudes, double[] rateLevels,
               double reverbRoom, double reverbMix, double reverbDamp) {
        this.name = name;
        this.matrix = deepCopy(matrix);
        this.scale = scale;
        this.active = active.clone();
        this.amplitudes = amplitudes.clone();
        this.rateLevels = rateLevels.clone();
        this.reverbRoom = reverbRoom;
        this.reverbMix = reverbMix;
        this.reverbDamp = reverbDamp;
    }

    public String getName() { return name; }
    public double[][] getMatrix() { return deepCopy(matrix); }
    public Scale getScale() { return scale; }
    public boolean[] getActive() { return active.clone(); }
    public double[] getAmplitudes() { return amplitudes.clone(); }
    public double[] getRateLevels() { return rateLevels.clone(); }
    public double getReverbRoom() { return reverbRoom; }
    public double getReverbMix() { return reverbMix; }
    public double getReverbDamp() { return reverbDamp; }

    @Override
    public String toString() { return name; }

    private static double[][] deepCopy(double[][] src) {
        double[][] copy = new double[src.length][];
        for (int i = 0; i < src.length; i++) {
            copy[i] = src[i].clone();
        }
        return copy;
    }
}
