package org.delightofcomposition.gui;

import javax.swing.*;
import java.awt.*;

import org.delightofcomposition.markov.Stratum;

/**
 * Panel for one stratum.
 * Layout: [header: range + toggle] / [vol slider] / [rate slider] | [VU meter]
 */
public class StratumStripPanel extends JPanel {
    private final Stratum stratum;
    private final LabeledSlider volumeSlider;
    private final LabeledSlider rateSlider;
    private final ToggleSwitch activeToggle;
    private final LevelMeter levelMeter;

    public StratumStripPanel(Stratum stratum) {
        this.stratum = stratum;
        setLayout(new BorderLayout(4, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        // Center: header + sliders stacked vertically
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // Header row: range label + toggle
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel rangeLabel = new JLabel(stratum.getRangeLabel());
        Theme.tagFont(rangeLabel, "title");
        Theme.tagFg(rangeLabel, "fg");
        header.add(rangeLabel);
        header.add(Box.createHorizontalGlue());

        activeToggle = new ToggleSwitch(stratum.isActive());
        activeToggle.addChangeListener(e -> stratum.setActive(activeToggle.isSelected()));
        header.add(activeToggle);

        center.add(header);
        center.add(Box.createVerticalStrut(2));

        // Volume slider
        volumeSlider = new LabeledSlider("Vol", 0, 100, (int)(stratum.getAmplitude() * 100), v -> v + "%");
        volumeSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        volumeSlider.addChangeListener(e -> stratum.setAmplitude(volumeSlider.getValue() / 100.0));
        center.add(volumeSlider);

        // Rate slider
        int rateInit = (int) Math.round(stratum.getRateLevel() * 100);
        rateSlider = new LabeledSlider("Rate", 0, 200, rateInit, v -> {
            if (v <= 33) return "Slow";
            if (v <= 133) return "Med";
            return "Fast";
        });
        rateSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        rateSlider.addChangeListener(e -> stratum.setRateLevel(rateSlider.getValue() / 100.0));
        center.add(rateSlider);

        add(center, BorderLayout.CENTER);

        // VU meter on the right
        levelMeter = new LevelMeter();
        JPanel meterWrapper = new JPanel(new BorderLayout());
        meterWrapper.setOpaque(false);
        meterWrapper.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        meterWrapper.add(levelMeter, BorderLayout.CENTER);
        add(meterWrapper, BorderLayout.EAST);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, 140);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, 140);
    }

    public void syncFromModel() {
        volumeSlider.setValue((int)(stratum.getAmplitude() * 100));
        rateSlider.setValue((int) Math.round(stratum.getRateLevel() * 100));
        activeToggle.setSelected(stratum.isActive());
    }

    public void updateLevel(float level) {
        levelMeter.setLevel(level);
    }

    public Stratum getStratum() { return stratum; }
}
