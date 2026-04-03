package org.delightofcomposition.gui;

import javax.swing.*;
import java.awt.*;

import org.delightofcomposition.audio.SpectrumAnalyzer;

/**
 * Live frequency bar spectrum display. Logarithmic frequency grouping,
 * smooth decay with peak hold.
 */
public class BarSpectrumView extends JComponent {
    private static final int NUM_BARS = 64;
    private static final int MAX_FREQ_HZ = 10000;
    private static final int LABEL_HEIGHT = 16;
    private static final double DECAY = 0.85;
    private static final double PEAK_DECAY = 0.97;

    private final SpectrumAnalyzer analyzer;
    private final double[] smoothed = new double[NUM_BARS];
    private final double[] peaks = new double[NUM_BARS];
    private final Timer repaintTimer;

    public BarSpectrumView(SpectrumAnalyzer analyzer) {
        this.analyzer = analyzer;
        setOpaque(false);

        repaintTimer = new Timer(30, e -> updateAndRepaint());
    }

    public void start() { repaintTimer.start(); }
    public void stop() { repaintTimer.stop(); }

    @Override
    public void removeNotify() {
        super.removeNotify();
        repaintTimer.stop();
    }

    private void updateAndRepaint() {
        double[] spectrum = analyzer.getSpectrum();
        if (spectrum == null) return;

        int maxBin = SpectrumAnalyzer.freqToBin(MAX_FREQ_HZ);
        if (maxBin > spectrum.length) maxBin = spectrum.length;

        // Logarithmic frequency grouping into NUM_BARS bars
        double logMin = Math.log(20); // 20 Hz
        double logMax = Math.log(MAX_FREQ_HZ);

        for (int bar = 0; bar < NUM_BARS; bar++) {
            double logLo = logMin + (logMax - logMin) * bar / NUM_BARS;
            double logHi = logMin + (logMax - logMin) * (bar + 1) / NUM_BARS;
            int binLo = SpectrumAnalyzer.freqToBin(Math.exp(logLo));
            int binHi = SpectrumAnalyzer.freqToBin(Math.exp(logHi));
            if (binLo < 0) binLo = 0;
            if (binHi >= maxBin) binHi = maxBin - 1;
            if (binHi < binLo) binHi = binLo;

            // Average magnitude in this frequency band
            double sum = 0;
            int count = 0;
            for (int b = binLo; b <= binHi; b++) {
                if (b < spectrum.length) {
                    sum += spectrum[b];
                    count++;
                }
            }
            double val = count > 0 ? sum / count : 0;

            // Smooth decay
            if (val > smoothed[bar]) {
                smoothed[bar] = val;
            } else {
                smoothed[bar] = smoothed[bar] * DECAY + val * (1 - DECAY);
            }

            // Peak hold with slow decay
            if (smoothed[bar] > peaks[bar]) {
                peaks[bar] = smoothed[bar];
            } else {
                peaks[bar] *= PEAK_DECAY;
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // Background
        g2.setColor(Theme.BG_INPUT);
        g2.fillRect(0, 0, w, h);

        int barAreaH = h - LABEL_HEIGHT;
        if (barAreaH <= 0) { g2.dispose(); return; }

        double barW = (double) w / NUM_BARS;
        int gap = Math.max(1, (int)(barW * 0.15));

        for (int i = 0; i < NUM_BARS; i++) {
            int x = (int)(i * barW) + gap;
            int bw = (int)((i + 1) * barW) - (int)(i * barW) - gap * 2;
            if (bw < 1) bw = 1;

            // Bar height
            int barH = (int)(smoothed[i] * barAreaH);
            if (barH > 0) {
                // Gradient from accent to accent_hover
                Color bottom = Theme.ACCENT;
                Color top = Theme.ACCENT_HOVER;
                GradientPaint gp = new GradientPaint(x, barAreaH, bottom, x, barAreaH - barH, top);
                g2.setPaint(gp);
                g2.fillRect(x, barAreaH - barH, bw, barH);
            }

            // Peak marker
            int peakY = barAreaH - (int)(peaks[i] * barAreaH);
            if (peaks[i] > 0.01) {
                g2.setColor(Theme.FG);
                g2.fillRect(x, peakY, bw, 2);
            }
        }

        // Frequency labels
        g2.setFont(Theme.FONT_SMALL);
        g2.setColor(Theme.FG_DIM);
        int[] freqLabels = {100, 500, 1000, 2000, 5000, 10000};
        String[] freqNames = {"100", "500", "1k", "2k", "5k", "10k"};
        double logMin = Math.log(20);
        double logMax = Math.log(MAX_FREQ_HZ);
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < freqLabels.length; i++) {
            double logF = Math.log(freqLabels[i]);
            double frac = (logF - logMin) / (logMax - logMin);
            int x = (int)(frac * w);
            g2.drawString(freqNames[i], x - fm.stringWidth(freqNames[i]) / 2, h - 2);
        }

        g2.dispose();
    }
}
