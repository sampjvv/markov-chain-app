package org.delightofcomposition.markov;

import java.util.ArrayList;
import java.util.List;

/**
 * Library of predefined cues from the SuperCollider exercise,
 * plus user-created cues.
 */
public class CueLibrary {
    private final List<Cue> cues = new ArrayList<>();

    public CueLibrary() {
        buildPredefinedCues();
    }

    private void buildPredefinedCues() {
        // Cue 7: Trills - Acoustic
        cues.add(new Cue("Cue 7: Trills - Acoustic",
            MatrixPresets.trills().toArray(),
            Scale.ACOUSTIC,
            new boolean[]{false, true, true, true, true, true},
            new double[]{0.0, 0.1, 0.1, 0.1, 0.1, 0.1},
            new double[]{2, 2, 2, 2, 1, 1},
            0.7, 0.9, 0.5));

        // Cue 8: Quartal Focus
        cues.add(new Cue("Cue 8: Quartal Focus",
            MatrixPresets.quartalFocus().toArray(),
            Scale.NATURAL_MINOR,
            new boolean[]{true, true, true, false, true, true},
            new double[]{0.3, 0.4, 0.5, 0.3, 0.8, 1.0},
            new double[]{2, 2, 2, 2, 1, 1},
            0.7, 0.9, 0.5));

        // Cue 9: Phrygian
        cues.add(new Cue("Cue 9: Phrygian",
            MatrixPresets.phrygianEmphasis().toArray(),
            Scale.A_PHRYGIAN,
            new boolean[]{true, false, true, true, false, true},
            new double[]{0.3, 0.3, 0.6, 0.8, 0.3, 0.4},
            new double[]{0, 2, 1, 0, 2, 2},
            0.8, 0.7, 0.3));

        // Cue 10: Bb Modal
        cues.add(new Cue("Cue 10: Bb Modal",
            MatrixPresets.bbModal().toArray(),
            Scale.BB_CUSTOM,
            new boolean[]{true, true, false, true, false, true},
            new double[]{0.3, 0.6, 0.3, 0.4, 0.3, 0.8},
            new double[]{0, 0, 2, 1, 2, 2},
            0.8, 0.7, 0.3));

        // Cue 11: A Natural Minor
        cues.add(new Cue("Cue 11: A Natural Minor",
            MatrixPresets.aNaturalMinor().toArray(),
            Scale.NATURAL_MINOR,
            new boolean[]{true, true, true, true, true, false},
            new double[]{0.9, 0.8, 0.4, 0.3, 0.5, 0.3},
            new double[]{0, 1, 1, 0, 0, 2},
            0.8, 0.7, 0.3));

        // Cue 11.1: A Minor - Building (final state of the gradual transition)
        cues.add(new Cue("Cue 11.1: A Minor - Building",
            MatrixPresets.aNaturalMinor().toArray(),
            Scale.NATURAL_MINOR,
            new boolean[]{true, true, true, true, true, false},
            new double[]{0.8, 0.9, 0.7, 0.6, 0.9, 0.3},
            new double[]{1, 2, 1, 0, 0, 2},
            0.5, 0.1, 0.3));

        // Cue 12: Ab-Bb Modal
        cues.add(new Cue("Cue 12: Ab-Bb Modal",
            MatrixPresets.abBbModal().toArray(),
            Scale.AB_BB_MODAL,
            new boolean[]{true, true, true, true, true, false},
            new double[]{0.8, 0.9, 0.7, 0.6, 0.4, 0.3},
            new double[]{1, 2, 1, 0, 0, 2},
            0.5, 0.1, 0.3));

        // Cue 13: Sharp Keys - Dense
        cues.add(new Cue("Cue 13: Sharp Keys - Dense",
            MatrixPresets.fsharpCentric().toArray(),
            Scale.FSHARP_CENTRIC,
            new boolean[]{true, true, true, true, true, false},
            new double[]{1.0, 0.9, 0.7, 0.4, 0.1, 0.3},
            new double[]{2, 1, 1, 0, 0, 2},
            0.9, 0.7, 0.5));

        // Cue 11.2: Ending (final state)
        cues.add(new Cue("Cue 11.2: Ending",
            MatrixPresets.aNaturalMinor().toArray(),
            Scale.NATURAL_MINOR,
            new boolean[]{true, true, true, false, true, false},
            new double[]{0.8, 0.9, 0.7, 0.3, 0.9, 0.3},
            new double[]{1, 1, 2, 2, 1, 2},
            0.7, 0.6, 0.5));
    }

    public List<Cue> getCues() {
        return new ArrayList<>(cues);
    }

    public Cue getCue(int index) {
        return cues.get(index);
    }

    public int size() {
        return cues.size();
    }

    public void addCue(Cue cue) {
        cues.add(cue);
    }

    public void removeCue(int index) {
        // Don't allow removing predefined cues (first 9)
        if (index >= 9) {
            cues.remove(index);
        }
    }
}
