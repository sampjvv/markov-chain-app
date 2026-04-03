package org.delightofcomposition.audio;

/**
 * Real-time FFT spectrum analyzer. Performs in-place Cooley-Tukey radix-2 FFT
 * on the audio engine's mix buffer and provides magnitude spectrum for visualization.
 */
public class SpectrumAnalyzer {
    public static final int FFT_SIZE = 2048;
    public static final int SPECTRUM_SIZE = FFT_SIZE / 2; // 1024 positive frequency bins
    public static final int SAMPLE_RATE = 48000;

    // Hamming window (precomputed)
    private final double[] window;

    // FFT working buffers
    private final double[] real;
    private final double[] imag;

    // Accumulation buffer for when audio blocks are smaller than FFT_SIZE
    private final double[] accumBuffer;
    private int accumPos = 0;

    // Latest magnitude spectrum (log-scaled, 0-1 range)
    private volatile double[] spectrum;

    // Waveform ring buffer for live waveform display
    private static final int WAVEFORM_SIZE = 4096;
    private final double[] waveformBuffer = new double[WAVEFORM_SIZE];
    private volatile int waveformHead = 0;

    // Ring buffer for spectrogram history
    private final double[][] history;
    private volatile int historyHead = 0;
    private final int historySize;

    public SpectrumAnalyzer(int historySize) {
        this.historySize = historySize;
        this.window = new double[FFT_SIZE];
        this.real = new double[FFT_SIZE];
        this.imag = new double[FFT_SIZE];
        this.accumBuffer = new double[FFT_SIZE];
        this.spectrum = new double[SPECTRUM_SIZE];
        this.history = new double[historySize][SPECTRUM_SIZE];

        // Precompute Hamming window
        for (int i = 0; i < FFT_SIZE; i++) {
            window[i] = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (FFT_SIZE - 1));
        }
    }

    /**
     * Feed audio samples (mono mix of L+R). Called from audio thread.
     * Accumulates until FFT_SIZE samples, then runs FFT.
     */
    public void feed(double[] bufL, double[] bufR, int frames) {
        for (int i = 0; i < frames; i++) {
            double mono = (bufL[i] + bufR[i]) * 0.5;
            accumBuffer[accumPos++] = mono;

            // Write to waveform ring buffer
            waveformBuffer[waveformHead] = mono;
            waveformHead = (waveformHead + 1) % WAVEFORM_SIZE;

            if (accumPos >= FFT_SIZE) {
                processFFT();
                // Overlap: shift by half
                System.arraycopy(accumBuffer, FFT_SIZE / 2, accumBuffer, 0, FFT_SIZE / 2);
                accumPos = FFT_SIZE / 2;
            }
        }
    }

    private void processFFT() {
        // Apply window and copy to real buffer
        for (int i = 0; i < FFT_SIZE; i++) {
            real[i] = accumBuffer[i] * window[i];
            imag[i] = 0;
        }

        // In-place Cooley-Tukey FFT
        fft(real, imag, FFT_SIZE);

        // Compute magnitude spectrum with log scaling
        double[] mag = new double[SPECTRUM_SIZE];
        for (int i = 0; i < SPECTRUM_SIZE; i++) {
            double m = Math.sqrt(real[i] * real[i] + imag[i] * imag[i]);
            // Log scaling with moderate sensitivity (lower multiplier = more headroom)
            mag[i] = Math.log1p(m * 8) / Math.log1p(8);
            if (mag[i] > 1.0) mag[i] = 1.0;
        }

        // Atomic publish
        spectrum = mag;

        // Add to history ring buffer
        int head = historyHead;
        System.arraycopy(mag, 0, history[head], 0, SPECTRUM_SIZE);
        historyHead = (head + 1) % historySize;
    }

    /** Get latest magnitude spectrum (1024 bins, 0-1 range). */
    public double[] getSpectrum() {
        return spectrum;
    }

    /** Get spectrogram history. Returns the ring buffer and current head position. */
    public double[][] getHistory() {
        return history;
    }

    public int getHistoryHead() {
        return historyHead;
    }

    public int getHistorySize() {
        return historySize;
    }

    /** Get the waveform ring buffer (mono samples, -1 to 1). */
    public double[] getWaveformBuffer() { return waveformBuffer; }
    public int getWaveformHead() { return waveformHead; }
    public int getWaveformSize() { return WAVEFORM_SIZE; }

    /** Convert bin index to frequency in Hz. */
    public static double binToFreq(int bin) {
        return (double) bin * SAMPLE_RATE / FFT_SIZE;
    }

    /** Convert frequency to bin index. */
    public static int freqToBin(double freq) {
        return (int)(freq * FFT_SIZE / SAMPLE_RATE);
    }

    // ── In-place radix-2 Cooley-Tukey FFT ──

    private static void fft(double[] re, double[] im, int n) {
        // Bit-reversal permutation
        int j = 0;
        for (int i = 0; i < n; i++) {
            if (i < j) {
                double tr = re[i]; re[i] = re[j]; re[j] = tr;
                double ti = im[i]; im[i] = im[j]; im[j] = ti;
            }
            int m = n >> 1;
            while (m >= 1 && j >= m) {
                j -= m;
                m >>= 1;
            }
            j += m;
        }

        // Butterfly stages
        for (int size = 2; size <= n; size <<= 1) {
            int halfSize = size >> 1;
            double angle = -2.0 * Math.PI / size;
            double wRe = Math.cos(angle);
            double wIm = Math.sin(angle);

            for (int i = 0; i < n; i += size) {
                double curRe = 1.0, curIm = 0.0;
                for (int k = 0; k < halfSize; k++) {
                    int even = i + k;
                    int odd = i + k + halfSize;

                    double tRe = curRe * re[odd] - curIm * im[odd];
                    double tIm = curRe * im[odd] + curIm * re[odd];

                    re[odd] = re[even] - tRe;
                    im[odd] = im[even] - tIm;
                    re[even] += tRe;
                    im[even] += tIm;

                    double newCurRe = curRe * wRe - curIm * wIm;
                    curIm = curRe * wIm + curIm * wRe;
                    curRe = newCurRe;
                }
            }
        }
    }
}
