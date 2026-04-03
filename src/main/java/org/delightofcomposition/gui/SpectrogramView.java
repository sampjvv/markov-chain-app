package org.delightofcomposition.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import org.delightofcomposition.audio.SpectrumAnalyzer;

/**
 * Scrolling spectrogram display. Time flows left-to-right,
 * frequency on Y axis (low=bottom, high=top), color intensity = amplitude.
 */
public class SpectrogramView extends JComponent {
    private static final int MAX_FREQ_HZ = 10000;
    private static final int LABEL_WIDTH = 45;

    private final SpectrumAnalyzer analyzer;
    private BufferedImage image;
    private int imageWidth;
    private int imageHeight;
    private int drawPos = 0;
    private int lastHistoryHead = -1;
    private final Timer repaintTimer;

    public SpectrogramView(SpectrumAnalyzer analyzer) {
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
        int head = analyzer.getHistoryHead();
        if (head == lastHistoryHead) return;

        int w = getWidth() - LABEL_WIDTH;
        int maxBin = SpectrumAnalyzer.freqToBin(MAX_FREQ_HZ);
        if (maxBin > SpectrumAnalyzer.SPECTRUM_SIZE) maxBin = SpectrumAnalyzer.SPECTRUM_SIZE;

        if (w <= 0 || maxBin <= 0) return;

        // Recreate image if size changed
        if (image == null || imageWidth != w || imageHeight != maxBin) {
            imageWidth = w;
            imageHeight = maxBin;
            image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
            drawPos = 0;
            // Fill with background
            Graphics2D g = image.createGraphics();
            g.setColor(Theme.BG_INPUT);
            g.fillRect(0, 0, imageWidth, imageHeight);
            g.dispose();
        }

        // Draw new columns from history
        double[][] history = analyzer.getHistory();
        int histSize = analyzer.getHistorySize();
        int count = (head - lastHistoryHead + histSize) % histSize;
        if (count > imageWidth) count = imageWidth; // cap

        int readPos = (head - count + histSize) % histSize;
        for (int c = 0; c < count; c++) {
            double[] spectrum = history[(readPos + c) % histSize];
            for (int bin = 0; bin < maxBin && bin < spectrum.length; bin++) {
                int y = maxBin - 1 - bin; // flip: low freq at bottom
                int rgb = magnitudeToColor(spectrum[bin]);
                image.setRGB(drawPos % imageWidth, y, rgb);
            }
            drawPos++;
        }

        lastHistoryHead = head;
        repaint();
    }

    private int magnitudeToColor(double mag) {
        // Three-point gradient: BG_INPUT → ACCENT → ZINC_50
        Color lo = Theme.BG_INPUT;
        Color mid = Theme.ACCENT;
        Color hi = Theme.ZINC_50;

        int r, g, b;
        if (mag < 0.5) {
            float t = (float)(mag * 2.0);
            r = blend(lo.getRed(), mid.getRed(), t);
            g = blend(lo.getGreen(), mid.getGreen(), t);
            b = blend(lo.getBlue(), mid.getBlue(), t);
        } else {
            float t = (float)((mag - 0.5) * 2.0);
            r = blend(mid.getRed(), hi.getRed(), t);
            g = blend(mid.getGreen(), hi.getGreen(), t);
            b = blend(mid.getBlue(), hi.getBlue(), t);
        }
        return (r << 16) | (g << 8) | b;
    }

    private static int blend(int a, int b, float t) {
        return Math.min(255, Math.max(0, (int)(a + (b - a) * t)));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth(), h = getHeight();

        // Background
        g2.setColor(Theme.BG_INPUT);
        g2.fillRect(0, 0, w, h);

        if (image != null && imageWidth > 0) {
            int displayW = w - LABEL_WIDTH;
            int displayH = h;

            // Draw the scrolling image — newest data on the right
            int imgX = drawPos % imageWidth;

            // Right portion (older data): from imgX to end of image → draw on left of display
            int rightChunkW = imageWidth - imgX;
            if (rightChunkW > 0 && rightChunkW <= imageWidth) {
                g2.drawImage(image,
                    LABEL_WIDTH, 0, LABEL_WIDTH + rightChunkW * displayW / imageWidth, displayH,
                    imgX, 0, imageWidth, imageHeight, null);
            }
            // Left portion (newer data): from 0 to imgX → draw on right of display
            if (imgX > 0) {
                int dstX = LABEL_WIDTH + rightChunkW * displayW / imageWidth;
                g2.drawImage(image,
                    dstX, 0, LABEL_WIDTH + displayW, displayH,
                    0, 0, imgX, imageHeight, null);
            }
        }

        // Frequency labels
        g2.setFont(Theme.FONT_SMALL);
        g2.setColor(Theme.FG_DIM);
        int maxBin = SpectrumAnalyzer.freqToBin(MAX_FREQ_HZ);
        int[] freqLabels = {100, 500, 1000, 2000, 5000, 10000};
        String[] freqNames = {"100", "500", "1k", "2k", "5k", "10k"};
        for (int i = 0; i < freqLabels.length; i++) {
            int bin = SpectrumAnalyzer.freqToBin(freqLabels[i]);
            int y = h - (int)((double)bin / maxBin * h);
            if (y > 10 && y < h - 5) {
                g2.drawString(freqNames[i], 4, y + 4);
                g2.setColor(new Color(Theme.FG_DIM.getRed(), Theme.FG_DIM.getGreen(), Theme.FG_DIM.getBlue(), 40));
                g2.drawLine(LABEL_WIDTH, y, w, y);
                g2.setColor(Theme.FG_DIM);
            }
        }

        g2.dispose();
    }
}
