package org.delightofcomposition.markov;

/**
 * Core Markov chain algorithm.
 * Given a current note index, selects the next note based on
 * the transition probability matrix.
 */
public class MarkovEngine {

    private volatile TransitionMatrix matrix;

    public MarkovEngine(TransitionMatrix matrix) {
        this.matrix = matrix;
    }

    /**
     * Select the next note index using the Markov chain.
     * Ported from SC: ~getNextNote (lines 137-147).
     */
    public int getNextNoteIndex(int currentIndex) {
        double rnd = Math.random();
        double sum = 0;
        double[] row = matrix.getRow(currentIndex);
        for (int i = 0; i < TransitionMatrix.SIZE; i++) {
            sum += row[i];
            if (sum >= rnd) return i;
        }
        return TransitionMatrix.SIZE - 1;
    }

    public TransitionMatrix getMatrix() {
        return matrix;
    }

    public void setMatrix(TransitionMatrix matrix) {
        this.matrix = matrix;
    }
}
