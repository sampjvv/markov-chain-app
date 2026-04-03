package org.delightofcomposition.gui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import org.delightofcomposition.audio.SampleBank;
import org.delightofcomposition.markov.MarkovParameters;
import org.delightofcomposition.markov.Scale;

/**
 * Left sidebar with global parameter controls:
 * sample, scale, sine controller, reverb, master volume, tempo, note range.
 */
public class GlobalParameterPanel extends JPanel implements Scrollable {
    private final MarkovParameters params;
    private final SampleBank sampleBank;
    private final JComboBox<Scale> scaleCombo;
    private final LabeledSlider reverbMixSlider;
    private final LabeledSlider reverbRoomSlider;
    private final LabeledSlider reverbDampSlider;
    private final LabeledSlider masterSlider;
    private final LabeledSlider tempoSlider;
    private final LabeledSlider highestSlider;
    private final LabeledSlider lowestSlider;
    private final ToggleSwitch sineToggle;
    private final LabeledSlider sineCycleSlider;
    private final StepperControl origFreqStepper;

    public GlobalParameterPanel(MarkovParameters params, SampleBank sampleBank) {
        this.params = params;
        this.sampleBank = sampleBank;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // === Sample Section ===
        add(Theme.sectionLabel("Sample"));
        add(Box.createVerticalStrut(Theme.LABEL_GAP));

        SampleDropPanel sampleDrop = new SampleDropPanel("Source Sample",
            new File("resources/11-p.wav"), file -> {
                sampleBank.loadSample(file.getAbsolutePath());
            });
        sampleDrop.setAlignmentX(Component.LEFT_ALIGNMENT);
        sampleDrop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        add(sampleDrop);
        add(Box.createVerticalStrut(Theme.CONTROL_GAP));

        // Original frequency
        JLabel freqLabel = Theme.paramLabel("Original Freq (Hz)");
        freqLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(freqLabel);
        add(Box.createVerticalStrut(Theme.LABEL_GAP));
        origFreqStepper = new StepperControl(880, 20, 8000, 10);
        origFreqStepper.setAlignmentX(Component.LEFT_ALIGNMENT);
        origFreqStepper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        origFreqStepper.addChangeListener(e -> sampleBank.setOrigFreq(origFreqStepper.getIntValue()));
        add(origFreqStepper);
        add(Box.createVerticalStrut(Theme.SECTION_GAP));

        // === Scale Section ===
        add(Theme.sectionLabel("Scale"));
        add(Box.createVerticalStrut(Theme.LABEL_GAP));
        scaleCombo = new JComboBox<>(Scale.ALL_SCALES);
        scaleCombo.setSelectedItem(params.getScale());
        scaleCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        scaleCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        scaleCombo.addActionListener(e -> {
            Scale selected = (Scale) scaleCombo.getSelectedItem();
            if (selected != null) params.setScale(selected);
        });
        add(scaleCombo);
        add(Box.createVerticalStrut(Theme.SECTION_GAP));

        // === Master Section ===
        add(Theme.sectionLabel("Master"));
        add(Box.createVerticalStrut(Theme.LABEL_GAP));
        masterSlider = new LabeledSlider("Volume", 0, 100, (int)(params.getMasterVolume() * 100), v -> v + "%");
        masterSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        masterSlider.addChangeListener(e -> params.setMasterVolume(masterSlider.getValue() / 100.0));
        add(masterSlider);
        add(Box.createVerticalStrut(Theme.CONTROL_GAP));

        tempoSlider = new LabeledSlider("Tempo", 20, 240, (int)(params.getTempo() * 60), v -> v + " BPM");
        tempoSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        tempoSlider.addChangeListener(e -> params.setTempo(tempoSlider.getValue() / 60.0));
        add(tempoSlider);
        add(Box.createVerticalStrut(Theme.SECTION_GAP));

        // === Reverb Section ===
        add(Theme.sectionLabel("Reverb"));
        add(Box.createVerticalStrut(Theme.LABEL_GAP));
        reverbMixSlider = new LabeledSlider("Mix", 0, 100, (int)(params.getReverbMix() * 100), v -> v + "%");
        reverbMixSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        reverbMixSlider.addChangeListener(e -> params.setReverbMix(reverbMixSlider.getValue() / 100.0));
        add(reverbMixSlider);
        add(Box.createVerticalStrut(Theme.CONTROL_GAP));

        reverbRoomSlider = new LabeledSlider("Room", 0, 100, (int)(params.getReverbRoom() * 100), v -> v + "%");
        reverbRoomSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        reverbRoomSlider.addChangeListener(e -> params.setReverbRoom(reverbRoomSlider.getValue() / 100.0));
        add(reverbRoomSlider);
        add(Box.createVerticalStrut(Theme.CONTROL_GAP));

        reverbDampSlider = new LabeledSlider("Damp", 0, 100, (int)(params.getReverbDamp() * 100), v -> v + "%");
        reverbDampSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        reverbDampSlider.addChangeListener(e -> params.setReverbDamp(reverbDampSlider.getValue() / 100.0));
        add(reverbDampSlider);
        add(Box.createVerticalStrut(Theme.SECTION_GAP));

        // === Note Range Section ===
        add(Theme.sectionLabel("Note Range"));
        add(Box.createVerticalStrut(Theme.LABEL_GAP));
        highestSlider = new LabeledSlider("Highest", 24, 96, (int)params.getTargetHighest(), v -> "MIDI " + v);
        highestSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        highestSlider.addChangeListener(e -> params.setTargetHighest(highestSlider.getValue()));
        add(highestSlider);
        add(Box.createVerticalStrut(Theme.CONTROL_GAP));

        lowestSlider = new LabeledSlider("Lowest", 24, 96, (int)params.getTargetLowest(), v -> "MIDI " + v);
        lowestSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        lowestSlider.addChangeListener(e -> params.setTargetLowest(lowestSlider.getValue()));
        add(lowestSlider);
        add(Box.createVerticalStrut(Theme.SECTION_GAP));

        // === Sine Controller Section ===
        add(Theme.sectionLabel("Sine Controller"));
        add(Box.createVerticalStrut(Theme.LABEL_GAP));

        JPanel sineRow = new JPanel();
        sineRow.setLayout(new BoxLayout(sineRow, BoxLayout.X_AXIS));
        sineRow.setOpaque(false);
        sineRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        sineRow.add(Theme.paramLabel("Highest Note"));
        sineRow.add(Box.createHorizontalGlue());
        sineToggle = new ToggleSwitch(params.isSineControlsHighest());
        sineToggle.addChangeListener(e -> {
            if (sineToggle.isSelected()) {
                params.startSineController();
            } else {
                params.stopSineController();
            }
        });
        sineRow.add(sineToggle);
        sineRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.TOUCH_TARGET));
        add(sineRow);
        add(Box.createVerticalStrut(Theme.CONTROL_GAP));

        sineCycleSlider = new LabeledSlider("Cycle", 1, 120, 60, v -> v + "s");
        sineCycleSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        sineCycleSlider.addChangeListener(e -> params.setSineFrequency(1.0 / sineCycleSlider.getValue()));
        add(sineCycleSlider);

        add(Box.createVerticalGlue());
    }

    /** Sync all UI controls from model (e.g., after loading a cue). */
    public void syncFromModel() {
        scaleCombo.setSelectedItem(params.getScale());
        masterSlider.setValue((int)(params.getMasterVolume() * 100));
        tempoSlider.setValue((int)(params.getTempo() * 60));
        reverbMixSlider.setValue((int)(params.getReverbMix() * 100));
        reverbRoomSlider.setValue((int)(params.getReverbRoom() * 100));
        reverbDampSlider.setValue((int)(params.getReverbDamp() * 100));
        highestSlider.setValue((int)params.getTargetHighest());
        lowestSlider.setValue((int)params.getTargetLowest());
        revalidate();
        repaint();
    }

    // Scrollable implementation — tracks viewport width
    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(java.awt.Rectangle vis, int orient, int dir) { return 16; }
    @Override public int getScrollableBlockIncrement(java.awt.Rectangle vis, int orient, int dir) { return 64; }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}
