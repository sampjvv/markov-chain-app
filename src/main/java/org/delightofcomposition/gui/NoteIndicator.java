package org.delightofcomposition.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Small component that displays the current note name and flashes on trigger.
 */
public class NoteIndicator extends JComponent {
    private String noteName = "-";
    private float flashAlpha = 0f;
    private Timer fadeTimer;

    public NoteIndicator() {
        setPreferredSize(new Dimension(40, 28));
        setMinimumSize(new Dimension(30, 28));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        fadeTimer = new Timer(30, e -> {
            flashAlpha -= 0.08f;
            if (flashAlpha <= 0) {
                flashAlpha = 0;
                fadeTimer.stop();
            }
            repaint();
        });
    }

    /** Trigger a flash with a new note name. */
    public void triggerNote(String name) {
        this.noteName = name;
        this.flashAlpha = 1.0f;
        fadeTimer.restart();
        repaint();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        fadeTimer.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // Background with flash
        Color bg = Theme.BG_MUTED;
        if (flashAlpha > 0) {
            Color accent = Theme.ACCENT;
            int r = blend(bg.getRed(), accent.getRed(), flashAlpha);
            int gr = blend(bg.getGreen(), accent.getGreen(), flashAlpha);
            int b = blend(bg.getBlue(), accent.getBlue(), flashAlpha);
            bg = new Color(r, gr, b);
        }

        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h, Theme.RADIUS_SM, Theme.RADIUS_SM);

        g2.setColor(Theme.BORDER);
        g2.drawRoundRect(0, 0, w - 1, h - 1, Theme.RADIUS_SM, Theme.RADIUS_SM);

        // Note name
        g2.setColor(flashAlpha > 0.3f ? Theme.BG : Theme.FG);
        g2.setFont(Theme.FONT_HEADING);
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(noteName);
        g2.drawString(noteName, (w - textW) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);

        g2.dispose();
    }

    private static int blend(int a, int b, float t) {
        return Math.min(255, Math.max(0, (int)(a + (b - a) * t)));
    }
}
