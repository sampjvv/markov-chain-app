package org.delightofcomposition.gui;

import javax.swing.*;
import java.awt.*;

import org.delightofcomposition.audio.SpectrumAnalyzer;

/**
 * Live waveform oscilloscope display. Shows the most recent audio samples
 * as a scrolling waveform with a center line.
 */
public class WaveformView extends JComponent {
    private final SpectrumAnalyzer analyzer;
    private final Timer repaintTimer;

    public WaveformView(SpectrumAnalyzer analyzer) {
        this.analyzer = analyzer;
        setOpaque(false);
        repaintTimer = new Timer(30, e -> repaint());
    }

    public void start() { repaintTimer.start(); }
    public void stop() { repaintTimer.stop(); }

    @Override
    public void removeNotify() {
        super.removeNotify();
        repaintTimer.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // Background
        g2.setColor(Theme.BG_INPUT);
        g2.fillRect(0, 0, w, h);

        int centerY = h / 2;

        // Center line
        g2.setColor(Theme.BORDER_SUBTLE);
        g2.drawLine(0, centerY, w, centerY);

        // Amplitude grid lines at +/- 0.5
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 4}, 0));
        int halfLine = h / 4;
        g2.drawLine(0, centerY - halfLine, w, centerY - halfLine);
        g2.drawLine(0, centerY + halfLine, w, centerY + halfLine);
        g2.setStroke(new BasicStroke(1f));

        // Draw waveform
        double[] waveform = analyzer.getWaveformBuffer();
        int head = analyzer.getWaveformHead();
        int size = analyzer.getWaveformSize();

        if (waveform == null || size == 0) { g2.dispose(); return; }

        // Map waveform samples to display pixels
        // Show the most recent 'w' samples (or fewer if display is wider than buffer)
        int samplesToShow = Math.min(size, w * 2); // 2 samples per pixel for detail
        double samplesPerPixel = (double) samplesToShow / w;

        g2.setColor(Theme.ACCENT);
        g2.setStroke(new BasicStroke(1.5f));

        int prevX = 0, prevY = centerY;
        for (int px = 0; px < w; px++) {
            // Find min/max in this pixel's sample range for proper rendering
            int sampleStart = (int)(px * samplesPerPixel);
            int sampleEnd = (int)((px + 1) * samplesPerPixel);
            if (sampleEnd <= sampleStart) sampleEnd = sampleStart + 1;

            double minVal = 1, maxVal = -1;
            for (int s = sampleStart; s < sampleEnd && s < samplesToShow; s++) {
                int idx = (head - samplesToShow + s + size) % size;
                double val = waveform[idx];
                if (val < minVal) minVal = val;
                if (val > maxVal) maxVal = val;
            }

            int yMin = centerY - (int)(maxVal * (h / 2 - 4));
            int yMax = centerY - (int)(minVal * (h / 2 - 4));

            // Draw vertical line for this pixel (min to max)
            if (yMin == yMax) {
                g2.drawLine(px, yMin, px, yMin);
            } else {
                g2.drawLine(px, yMin, px, yMax);
            }

            // Connect to previous pixel
            if (px > 0) {
                int midY = (yMin + yMax) / 2;
                g2.drawLine(prevX, prevY, px, midY);
                prevY = midY;
            } else {
                prevY = (yMin + yMax) / 2;
            }
            prevX = px;
        }

        // Labels
        g2.setFont(Theme.FONT_SMALL);
        g2.setColor(Theme.FG_DIM);
        g2.drawString("+1", 4, 12);
        g2.drawString("0", 4, centerY - 2);
        g2.drawString("-1", 4, h - 4);

        g2.dispose();
    }
}
