package org.delightofcomposition.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import org.delightofcomposition.markov.MarkovParameters;
import org.delightofcomposition.markov.TransitionMatrix;

/**
 * Interactive 8x8 transition probability matrix editor.
 * Cells colored by probability value. Click+drag to edit; rows auto-normalize.
 */
public class MatrixEditorPanel extends JComponent {
    private static final int LABEL_SIZE = 40;
    private static final int PAD = 8;

    private final MarkovParameters params;
    private int hoveredRow = -1, hoveredCol = -1;
    private int dragRow = -1, dragCol = -1;
    private int dragStartY;
    private double dragStartValue;

    public MatrixEditorPanel(MarkovParameters params) {
        this.params = params;
        setMinimumSize(new Dimension(200, 200));
        setPreferredSize(new Dimension(280, 280));

        params.getMatrix().addChangeListener(e -> repaint());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                int[] cell = cellAt(e.getX(), e.getY());
                if (cell != null) {
                    dragRow = cell[0];
                    dragCol = cell[1];
                    dragStartY = e.getY();
                    dragStartValue = params.getMatrix().getCell(dragRow, dragCol);
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                dragRow = -1;
                dragCol = -1;
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                hoveredCol = -1;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragRow >= 0) {
                    int gridH = getHeight() - LABEL_SIZE - PAD * 2;
                    if (gridH <= 0) return;
                    double delta = (dragStartY - e.getY()) / (double) gridH;
                    double newVal = Math.max(0, Math.min(1, dragStartValue + delta));

                    TransitionMatrix m = params.getMatrix();
                    double[] row = m.getRow(dragRow);
                    row[dragCol] = newVal;
                    m.setRow(dragRow, row);
                }
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                int[] cell = cellAt(e.getX(), e.getY());
                int oldRow = hoveredRow, oldCol = hoveredCol;
                if (cell != null) {
                    hoveredRow = cell[0];
                    hoveredCol = cell[1];
                } else {
                    hoveredRow = -1;
                    hoveredCol = -1;
                }
                if (oldRow != hoveredRow || oldCol != hoveredCol) repaint();
            }
        });

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (hoveredRow < 0 || hoveredCol < 0) return;
                TransitionMatrix m = params.getMatrix();
                double[] row = m.getRow(hoveredRow);
                double step = 0.05;
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_EQUALS) {
                    row[hoveredCol] = Math.min(1, row[hoveredCol] + step);
                    m.setRow(hoveredRow, row);
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_MINUS) {
                    row[hoveredCol] = Math.max(0, row[hoveredCol] - step);
                    m.setRow(hoveredRow, row);
                }
            }
        });
    }

    private int[] cellAt(int px, int py) {
        int gridX = LABEL_SIZE + PAD;
        int gridY = PAD;
        int gridW = getWidth() - LABEL_SIZE - PAD * 2;
        int gridH = getHeight() - LABEL_SIZE - PAD * 2;
        if (gridW <= 0 || gridH <= 0) return null;

        // Use floating point to avoid rounding gaps
        double cellW = gridW / 8.0;
        double cellH = gridH / 8.0;

        int col = (int)((px - gridX) / cellW);
        int row = (int)((py - gridY) / cellH);

        if (col >= 0 && col < 8 && row >= 0 && row < 8
            && px >= gridX && py >= gridY
            && px < gridX + gridW && py < gridY + gridH) {
            return new int[]{row, col};
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // Card background
        if (Theme.isSynthwave()) {
            SynthwavePainter.fillPanel(g2, 0, 0, w, h, Theme.BG_CARD, Theme.BORDER);
            SynthwavePainter.paintBevel(g2, 0, 0, w, h, true);
        } else {
            g2.setColor(Theme.BG_CARD);
            g2.fillRoundRect(0, 0, w, h, Theme.RADIUS_LG, Theme.RADIUS_LG);
            g2.setColor(Theme.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, Theme.RADIUS_LG, Theme.RADIUS_LG);
        }

        int gridX = LABEL_SIZE + PAD;
        int gridY = PAD;
        int gridW = w - LABEL_SIZE - PAD * 2;
        int gridH = h - LABEL_SIZE - PAD * 2;
        if (gridW <= 0 || gridH <= 0) { g2.dispose(); return; }

        double cellW = gridW / 8.0;
        double cellH = gridH / 8.0;

        TransitionMatrix matrix = params.getMatrix();

        // Draw cells
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int cx = gridX + (int)(col * cellW);
                int cy = gridY + (int)(row * cellH);
                int cw = (int)((col + 1) * cellW) - (int)(col * cellW);
                int ch = (int)((row + 1) * cellH) - (int)(row * cellH);

                double val = matrix.getCell(row, col);

                // Color intensity based on probability
                Color accent = Theme.ACCENT;
                int alpha = (int)(val * 235) + 20;
                alpha = Math.min(255, Math.max(20, alpha));
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), alpha));
                g2.fillRect(cx + 1, cy + 1, cw - 2, ch - 2);

                // Active drag highlight
                if (row == dragRow && col == dragCol) {
                    g2.setColor(new Color(Theme.FG.getRed(), Theme.FG.getGreen(), Theme.FG.getBlue(), 40));
                    g2.fillRect(cx + 1, cy + 1, cw - 2, ch - 2);
                }

                // Hover highlight + value display
                if (row == hoveredRow && col == hoveredCol) {
                    g2.setColor(new Color(Theme.FG.getRed(), Theme.FG.getGreen(), Theme.FG.getBlue(), 30));
                    g2.fillRect(cx + 1, cy + 1, cw - 2, ch - 2);

                    g2.setColor(Theme.FG);
                    g2.setFont(Theme.FONT_SMALL);
                    String valStr = String.format("%.2f", val);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(valStr, cx + (cw - fm.stringWidth(valStr)) / 2,
                                  cy + (ch + fm.getAscent() - fm.getDescent()) / 2);
                }
            }
        }

        // Grid lines
        g2.setColor(Theme.BORDER);
        for (int i = 0; i <= 8; i++) {
            int gx = gridX + (int)(i * cellW);
            int gy = gridY + (int)(i * cellH);
            g2.drawLine(gx, gridY, gx, gridY + gridH);
            g2.drawLine(gridX, gy, gridX + gridW, gy);
        }

        // Note labels
        g2.setFont(Theme.FONT_SMALL);
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < 8; i++) {
            String name = params.getScale().noteName(i);
            int cw = (int)((i + 1) * cellW) - (int)(i * cellW);
            int ch = (int)((i + 1) * cellH) - (int)(i * cellH);

            // Column labels (bottom) - "To" notes
            g2.setColor(i == hoveredCol ? Theme.FG : Theme.FG_MUTED);
            int lx = gridX + (int)(i * cellW) + (cw - fm.stringWidth(name)) / 2;
            int ly = gridY + gridH + fm.getAscent() + 4;
            g2.drawString(name, lx, ly);

            // Row labels (left) - "From" notes
            g2.setColor(i == hoveredRow ? Theme.FG : Theme.FG_MUTED);
            int rx = LABEL_SIZE - fm.stringWidth(name) - 4;
            int ry = gridY + (int)(i * cellH) + (ch + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(name, rx, ry);
        }

        // Axis labels
        g2.setFont(Theme.FONT_SECTION);
        g2.setColor(Theme.FG_DIM);
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString("FROM", 4, gridY + gridH / 2 + fm2.getAscent() / 2);
        g2.drawString("TO", gridX + gridW / 2 - fm2.stringWidth("TO") / 2,
                      gridY + gridH + fm.getAscent() + 4 + fm2.getHeight());

        // Focus ring
        if (isFocusOwner()) {
            g2.setColor(Theme.RING);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, Theme.RADIUS_LG, Theme.RADIUS_LG);
        }

        g2.dispose();
    }
}
