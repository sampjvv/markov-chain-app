package org.delightofcomposition.gui;

import javax.swing.*;
import java.awt.*;

import org.delightofcomposition.markov.Stratum;

/**
 * Horizontal row for one stratum.
 * Layout: [range label] [note indicator] [volume slider] [F/M/S] [toggle]
 */
public class StratumStripPanel extends JPanel {
    private final Stratum stratum;
    private final NoteIndicator noteIndicator;
    private final LabeledSlider volumeSlider;
    private final SegmentedControl rateControl;
    private final ToggleSwitch activeToggle;

    public StratumStripPanel(Stratum stratum) {
        this.stratum = stratum;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        // Range label (fixed width)
        JLabel rangeLabel = new JLabel(stratum.getRangeLabel());
        Theme.tagFont(rangeLabel, "title");
        Theme.tagFg(rangeLabel, "fg");
        rangeLabel.setPreferredSize(new Dimension(46, 28));
        rangeLabel.setMinimumSize(new Dimension(46, 28));
        rangeLabel.setMaximumSize(new Dimension(46, 28));
        add(rangeLabel);
        add(Box.createHorizontalStrut(4));

        // Note indicator
        noteIndicator = new NoteIndicator();
        noteIndicator.setPreferredSize(new Dimension(36, 28));
        noteIndicator.setMinimumSize(new Dimension(36, 28));
        noteIndicator.setMaximumSize(new Dimension(36, 28));
        add(noteIndicator);
        add(Box.createHorizontalStrut(6));

        // Volume slider (takes remaining space)
        volumeSlider = new LabeledSlider("Vol", 0, 100, (int)(stratum.getAmplitude() * 100), v -> v + "%");
        volumeSlider.addChangeListener(e -> stratum.setAmplitude(volumeSlider.getValue() / 100.0));
        add(volumeSlider);
        add(Box.createHorizontalStrut(6));

        // Rate control
        rateControl = new SegmentedControl(new String[]{"F", "M", "S"}, stratum.getRateLevel());
        rateControl.setPreferredSize(new Dimension(80, 28));
        rateControl.setMinimumSize(new Dimension(80, 28));
        rateControl.setMaximumSize(new Dimension(80, 28));
        rateControl.addChangeListener(e -> stratum.setRateLevel(rateControl.getSelectedIndex()));
        add(rateControl);
        add(Box.createHorizontalStrut(6));

        // Active toggle
        activeToggle = new ToggleSwitch(stratum.isActive());
        activeToggle.addChangeListener(e -> stratum.setActive(activeToggle.isSelected()));
        add(activeToggle);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, 36);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, 36);
    }

    public void syncFromModel() {
        volumeSlider.setValue((int)(stratum.getAmplitude() * 100));
        rateControl.setSelectedIndex(stratum.getRateLevel());
        activeToggle.setSelected(stratum.isActive());
    }

    public void triggerNote(String noteName) {
        noteIndicator.triggerNote(noteName);
    }

    public Stratum getStratum() { return stratum; }
}
