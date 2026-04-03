package org.delightofcomposition.markov;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * 8x8 transition probability matrix with auto-normalization.
 * Each row represents transition probabilities from a given note to all possible next notes.
 */
public class TransitionMatrix {
    public static final int SIZE = 8;
    private final double[][] data;
    private final List<ChangeListener> listeners = new ArrayList<>();

    public TransitionMatrix() {
        data = new double[SIZE][SIZE];
        // Initialize to uniform
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                data[r][c] = 1.0 / SIZE;
            }
        }
    }

    public TransitionMatrix(double[][] values) {
        data = new double[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                data[r][c] = values[r][c];
            }
            normalizeRow(r);
        }
    }

    public double getCell(int row, int col) {
        return data[row][col];
    }

    public void setCell(int row, int col, double value) {
        data[row][col] = Math.max(0, value);
        normalizeRow(row);
        fireChanged();
    }

    public double[] getRow(int row) {
        return data[row].clone();
    }

    public void setRow(int row, double[] values) {
        for (int c = 0; c < SIZE; c++) {
            data[row][c] = Math.max(0, values[c]);
        }
        normalizeRow(row);
        fireChanged();
    }

    /** Load all values from a 2D array. Each row is normalized. */
    public void loadFrom(double[][] values) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                data[r][c] = Math.max(0, values[r][c]);
            }
            normalizeRow(r);
        }
        fireChanged();
    }

    /** Normalize a single row so probabilities sum to 1.0. */
    private void normalizeRow(int row) {
        double sum = 0;
        for (int c = 0; c < SIZE; c++) sum += data[row][c];
        if (sum < 1e-10) {
            // All zeros: uniform distribution
            for (int c = 0; c < SIZE; c++) data[row][c] = 1.0 / SIZE;
        } else {
            for (int c = 0; c < SIZE; c++) data[row][c] /= sum;
        }
    }

    /** Deep copy. */
    public TransitionMatrix copy() {
        double[][] copy = new double[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(data[r], 0, copy[r], 0, SIZE);
        }
        return new TransitionMatrix(copy);
    }

    public double[][] toArray() {
        double[][] result = new double[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(data[r], 0, result[r], 0, SIZE);
        }
        return result;
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChanged() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(e);
        }
    }
}
