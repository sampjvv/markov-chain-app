package org.delightofcomposition.markov;

/**
 * Defines 8-note scales as semitone offsets from root.
 * All scales from the SuperCollider Markov exercise.
 */
public class Scale {
    private final String name;
    private final int[] degrees;

    public Scale(String name, int[] degrees) {
        this.name = name;
        this.degrees = degrees.clone();
    }

    public String getName() { return name; }
    public int[] getDegrees() { return degrees.clone(); }
    public int getDegree(int index) { return degrees[index]; }
    public int size() { return degrees.length; }

    /** Convert scale degree index + octave base (MIDI) to MIDI note number. */
    public int midiNote(int degreeIndex, int octaveBase) {
        return octaveBase + degrees[degreeIndex];
    }

    /** Get note name for a degree index (using chromatic names). */
    public String noteName(int degreeIndex) {
        String[] names = {"C","C#","D","Eb","E","F","F#","G","Ab","A","Bb","B"};
        int semitone = degrees[degreeIndex] % 12;
        if (semitone < 0) semitone += 12;
        return names[semitone];
    }

    @Override
    public String toString() { return name; }

    // --- All scale presets from the SC exercise ---

    public static final Scale MAJOR = new Scale("Major", new int[]{0,2,4,5,7,9,11,12});
    public static final Scale NATURAL_MINOR = new Scale("Natural Minor", new int[]{0,2,3,5,7,8,10,12});
    public static final Scale DORIAN = new Scale("Dorian", new int[]{0,2,3,5,7,9,10,12});
    public static final Scale PHRYGIAN = new Scale("Phrygian", new int[]{0,1,3,5,7,8,10,12});
    public static final Scale LYDIAN = new Scale("Lydian", new int[]{0,2,4,6,7,9,11,12});
    public static final Scale MIXOLYDIAN = new Scale("Mixolydian", new int[]{0,2,4,5,7,9,10,12});
    public static final Scale LOCRIAN = new Scale("Locrian", new int[]{0,1,3,5,6,8,10,12});
    public static final Scale ACOUSTIC = new Scale("Acoustic", new int[]{0,2,4,6,7,9,10,12});
    public static final Scale OCTATONIC_1 = new Scale("Octatonic 1", new int[]{0,1,3,4,6,7,9,10});
    public static final Scale OCTATONIC_2 = new Scale("Octatonic 2", new int[]{0,2,3,5,6,8,9,11});
    public static final Scale WHOLE_TONE = new Scale("Whole Tone", new int[]{0,2,4,6,8,10,12,14});
    public static final Scale HEXATONIC = new Scale("Hexatonic", new int[]{0,1,4,5,8,9,12,13});

    // Custom scales used in cues
    public static final Scale A_PHRYGIAN = new Scale("A Phrygian", new int[]{0,1,3,5,6,8,10,12});
    public static final Scale BB_CUSTOM = new Scale("Bb Custom", new int[]{1,3,4,11,6,8,10,11});
    public static final Scale AB_BB_MODAL = new Scale("Ab-Bb Modal", new int[]{0,1,3,5,6,8,10,11});
    public static final Scale FSHARP_CENTRIC = new Scale("F# Centric", new int[]{6,7,11,1,3,4,8,9});

    public static final Scale[] ALL_SCALES = {
        MAJOR, NATURAL_MINOR, DORIAN, PHRYGIAN, LYDIAN, MIXOLYDIAN,
        LOCRIAN, ACOUSTIC, OCTATONIC_1, OCTATONIC_2, WHOLE_TONE, HEXATONIC,
        A_PHRYGIAN, BB_CUSTOM, AB_BB_MODAL, FSHARP_CENTRIC
    };
}
