package org.delightofcomposition.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

/**
 * Vertical VU-style level meter for per-stratum volume display.
 * Green for low levels, amber for mid, red for high.
 */
public class LevelMeter extends JComponent {
    private float level; // 0.0 to 1.0

    public void setLevel(float level) {
        this.level = Math.max(0f, Math.min(1f, level));
        repaint();
    }

    @Override
    public Dimension getPreferredSize() { return new Dimension(14, 140); }

    @Override
    public Dimension getMinimumSize() { return new Dimension(14, 40); }

    @Override
    public Dimension getMaximumSize() { return new Dimension(14, Integer.MAX_VALUE); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int r = Theme.RADIUS_SM;

        // Background track
        if (Theme.isSynthwave()) {
            SynthwavePainter.fillPanel(g2, 0, 0, w, h, Theme.BG_MUTED, Theme.SW_PURPLE);
        } else {
            g2.setColor(Theme.BG_MUTED);
            g2.fillRoundRect(0, 0, w, h, r, r);
            g2.setColor(Theme.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, r, r);
        }

        // Level fill from bottom
        int fillH = (int)(level * (h - 4));
        if (fillH > 0) {
            int fillY = h - 2 - fillH;
            int inset = 2;
            int fillW = w - inset * 2;

            // Draw segments with color gradient: green -> amber -> red
            for (int y = fillY; y < h - 2; y++) {
                float frac = 1.0f - (float)(y - 2) / (h - 4); // 0 at top, 1 at bottom
                Color c;
                if (frac < 0.7f) {
                    c = Theme.SUCCESS;
                } else if (frac < 0.9f) {
                    c = Theme.AMBER;
                } else {
                    c = Theme.DESTRUCTIVE;
                }
                g2.setColor(c);
                g2.fillRect(inset, y, fillW, 1);
            }

            if (Theme.isSynthwave()) {
                SynthwavePainter.paintGlow(g2, inset, fillY, fillW, fillH, Theme.SUCCESS, 2);
            }
        }

        g2.dispose();
    }
}
