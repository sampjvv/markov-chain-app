package org.delightofcomposition.gui;

import javax.swing.*;
import java.awt.*;

import org.delightofcomposition.audio.SpectrumAnalyzer;

/**
 * Tabbed output visualizer with SegmentedControl to switch
 * between spectrogram, bar spectrum, and live waveform.
 */
public class SpectrumPanel extends JPanel {
    private final SpectrogramView spectrogramView;
    private final BarSpectrumView barSpectrumView;
    private final WaveformView waveformView;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    private static final String[] TAB_NAMES = {"Spectrogram", "Bars", "Waveform"};
    private static final String[] CARD_KEYS = {"spectrogram", "bars", "waveform"};

    public SpectrumPanel(SpectrumAnalyzer analyzer) {
        setLayout(new BorderLayout(0, 4));
        setOpaque(false);

        // Header with tab control
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 0, 8));

        JLabel title = new JLabel("Output");
        Theme.tagFont(title, "title");
        Theme.tagFg(title, "fgMuted");
        headerPanel.add(title, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // Card layout for views
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        SegmentedControl tabs = new SegmentedControl(TAB_NAMES, 0);
        tabs.setPreferredSize(new Dimension(260, 28));
        tabs.setMaximumSize(new Dimension(260, 28));
        tabs.addChangeListener(e -> {
            cardLayout.show(cardPanel, CARD_KEYS[tabs.getSelectedIndex()]);
        });
        headerPanel.add(tabs, BorderLayout.EAST);

        spectrogramView = new SpectrogramView(analyzer);
        barSpectrumView = new BarSpectrumView(analyzer);
        waveformView = new WaveformView(analyzer);

        cardPanel.add(spectrogramView, "spectrogram");
        cardPanel.add(barSpectrumView, "bars");
        cardPanel.add(waveformView, "waveform");

        add(cardPanel, BorderLayout.CENTER);
    }

    public void start() {
        spectrogramView.start();
        barSpectrumView.start();
        waveformView.start();
    }

    public void stop() {
        spectrogramView.stop();
        barSpectrumView.stop();
        waveformView.stop();
    }
}
