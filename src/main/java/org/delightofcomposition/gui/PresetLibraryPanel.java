package org.delightofcomposition.gui;

import javax.swing.*;
import java.awt.*;

import org.delightofcomposition.markov.CueLibrary;
import org.delightofcomposition.markov.MarkovParameters;
import org.delightofcomposition.markov.MatrixPresets;

/**
 * Right-side library panel with matrix presets and cue lists.
 */
public class PresetLibraryPanel extends JPanel implements Scrollable {
    private final MarkovParameters params;
    private final CueLibrary cueLibrary;
    private final JList<String> matrixList;
    private final JList<String> cueList;
    private final DefaultListModel<String> cueListModel;

    private CueLoadListener cueLoadListener;

    public interface CueLoadListener {
        void onCueLoaded();
    }

    public PresetLibraryPanel(MarkovParameters params, CueLibrary cueLibrary) {
        this.params = params;
        this.cueLibrary = cueLibrary;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // === Matrix Presets Section ===
        add(Theme.sectionLabel("Matrix Presets"));
        add(Box.createVerticalStrut(Theme.LABEL_GAP));

        String[] presetNames = MatrixPresets.getPresetNames();
        matrixList = new JList<>(presetNames);
        matrixList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        matrixList.setVisibleRowCount(5);
        matrixList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int idx = matrixList.getSelectedIndex();
                if (idx >= 0) {
                    params.getMatrix().loadFrom(MatrixPresets.getPreset(idx).toArray());
                }
            }
        });

        JScrollPane matrixScroll = new JScrollPane(matrixList);
        matrixScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        matrixScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        matrixScroll.setPreferredSize(new Dimension(200, 140));
        matrixScroll.setBorder(new Theme.RoundedBorder(null, -1, new Insets(0, 0, 0, 0)));
        add(matrixScroll);
        add(Box.createVerticalStrut(Theme.SECTION_GAP));

        // === Cues Section ===
        add(Theme.sectionLabel("Cues"));
        add(Box.createVerticalStrut(Theme.LABEL_GAP));

        cueListModel = new DefaultListModel<>();
        for (int i = 0; i < cueLibrary.size(); i++) {
            cueListModel.addElement(cueLibrary.getCue(i).getName());
        }
        cueList = new JList<>(cueListModel);
        cueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cueList.setVisibleRowCount(8);
        cueList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int idx = cueList.getSelectedIndex();
                if (idx >= 0) {
                    params.applyCue(cueLibrary.getCue(idx));
                    if (cueLoadListener != null) cueLoadListener.onCueLoaded();
                }
            }
        });

        JScrollPane cueScroll = new JScrollPane(cueList);
        cueScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        cueScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        cueScroll.setPreferredSize(new Dimension(200, 240));
        cueScroll.setBorder(new Theme.RoundedBorder(null, -1, new Insets(0, 0, 0, 0)));
        add(cueScroll);
        add(Box.createVerticalStrut(Theme.CONTROL_GAP));

        // Save cue button
        JButton saveCueButton = Theme.secondaryButton("Save Current as Cue");
        saveCueButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveCueButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        saveCueButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Cue name:", "Save Cue", JOptionPane.PLAIN_MESSAGE);
            if (name != null && !name.trim().isEmpty()) {
                cueLibrary.addCue(params.createCue(name.trim()));
                cueListModel.addElement(name.trim());
            }
        });
        add(saveCueButton);

        add(Box.createVerticalGlue());
    }

    public void setCueLoadListener(CueLoadListener l) { this.cueLoadListener = l; }

    // Scrollable
    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(java.awt.Rectangle vis, int orient, int dir) { return 16; }
    @Override public int getScrollableBlockIncrement(java.awt.Rectangle vis, int orient, int dir) { return 64; }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}
