package org.delightofcomposition;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.delightofcomposition.audio.MarkovAudioEngine;
import org.delightofcomposition.audio.SampleBank;
import org.delightofcomposition.gui.MarkovMainWindow;
import org.delightofcomposition.gui.Theme;
import org.delightofcomposition.gui.ThemePreferences;
import org.delightofcomposition.markov.MarkovParameters;

/**
 * Entry point for the Markov Chain Generative Music App.
 */
public class MarkovMain {
    public static void main(String[] args) {
        // Load sample
        SampleBank sampleBank = new SampleBank("resources/11-p.wav");

        // Create parameter model
        MarkovParameters params = new MarkovParameters();

        // Create audio engine
        MarkovAudioEngine engine = new MarkovAudioEngine(params, sampleBank);

        // Launch GUI
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // fall back to default
            }

            Theme.applyTheme(ThemePreferences.load());

            MarkovMainWindow window = new MarkovMainWindow(params, engine, sampleBank);
            window.setVisible(true);
        });
    }
}
