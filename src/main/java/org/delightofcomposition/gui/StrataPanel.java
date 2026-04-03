package org.delightofcomposition.gui;

import javax.swing.*;
import java.awt.*;

import org.delightofcomposition.markov.MarkovParameters;

/**
 * Panel containing 6 stratum horizontal rows stacked vertically.
 * Sits to the right of the matrix editor.
 */
public class StrataPanel extends JPanel {
    private final StratumStripPanel[] strips;

    public StrataPanel(MarkovParameters params) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // Header
        JLabel header = Theme.sectionLabel("Strata");
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setBorder(BorderFactory.createEmptyBorder(2, 6, 4, 0));
        add(header);

        strips = new StratumStripPanel[6];
        for (int i = 0; i < 6; i++) {
            strips[i] = new StratumStripPanel(params.getStratum(i));
            strips[i].setAlignmentX(Component.LEFT_ALIGNMENT);

            // Alternate row backgrounds for readability
            final int idx = i;
            JPanel rowWrapper = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    if (idx % 2 == 0) {
                        g.setColor(Theme.BG_CARD);
                    } else {
                        g.setColor(Theme.BG_MUTED);
                    }
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            rowWrapper.setOpaque(false);
            rowWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
            rowWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            rowWrapper.add(strips[i], BorderLayout.CENTER);
            add(rowWrapper);
        }

        add(Box.createVerticalGlue());
    }

    public void syncFromModel() {
        for (StratumStripPanel strip : strips) {
            strip.syncFromModel();
        }
    }

    public void triggerNote(int stratumIndex, String noteName) {
        if (stratumIndex >= 0 && stratumIndex < 6) {
            strips[stratumIndex].triggerNote(noteName);
        }
    }
}
