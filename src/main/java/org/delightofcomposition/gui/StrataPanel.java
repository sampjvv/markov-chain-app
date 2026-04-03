package org.delightofcomposition.gui;

import javax.swing.*;
import java.awt.*;

import org.delightofcomposition.audio.MarkovAudioEngine;
import org.delightofcomposition.markov.MarkovParameters;

/**
 * Panel containing 6 stratum strips in a 3-row x 2-column grid.
 */
public class StrataPanel extends JPanel {
    private final StratumStripPanel[] strips;
    private MarkovAudioEngine engine;
    private Timer meterTimer;

    public StrataPanel(MarkovParameters params) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // Header
        JLabel header = Theme.sectionLabel("Strata");
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setBorder(BorderFactory.createEmptyBorder(2, 6, 4, 0));
        add(header);

        // 3 rows x 2 columns grid
        JPanel grid = new JPanel(new GridLayout(3, 2, 6, 4));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        strips = new StratumStripPanel[6];
        for (int i = 0; i < 6; i++) {
            strips[i] = new StratumStripPanel(params.getStratum(i));

            final int idx = i;
            JPanel cellWrapper = new JPanel(new BorderLayout()) {
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
            cellWrapper.setOpaque(false);
            cellWrapper.add(strips[i], BorderLayout.CENTER);
            grid.add(cellWrapper);
        }

        add(grid);
        add(Box.createVerticalGlue());
    }

    public void setEngine(MarkovAudioEngine engine) {
        this.engine = engine;
    }

    public void startMetering() {
        if (meterTimer != null) meterTimer.stop();
        meterTimer = new Timer(30, e -> {
            if (engine != null) {
                for (int i = 0; i < 6; i++) {
                    strips[i].updateLevel(engine.getStratumLevel(i));
                }
            }
        });
        meterTimer.start();
    }

    public void stopMetering() {
        if (meterTimer != null) {
            meterTimer.stop();
            meterTimer = null;
        }
        for (StratumStripPanel strip : strips) {
            strip.updateLevel(0);
        }
    }

    public void syncFromModel() {
        for (StratumStripPanel strip : strips) {
            strip.syncFromModel();
        }
    }
}
