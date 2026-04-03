package org.delightofcomposition.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.delightofcomposition.audio.MarkovAudioEngine;
import org.delightofcomposition.audio.SampleBank;
import org.delightofcomposition.markov.CueLibrary;
import org.delightofcomposition.markov.MarkovParameters;

/**
 * Main application window.
 *
 * Layout:
 *   WEST: params (240px)
 *   CENTER: matrix+strata (top, equal width) | spectrum (bottom)
 *   EAST: library (260px)
 *   SOUTH: transport bar (full width)
 */
public class MarkovMainWindow extends JFrame {
    private final MarkovParameters params;
    private final MarkovAudioEngine engine;
    private final SampleBank sampleBank;
    private final CueLibrary cueLibrary;

    private final GlobalParameterPanel globalPanel;
    private final MatrixEditorPanel matrixPanel;
    private final StrataPanel strataPanel;
    private final TransportPanel transportPanel;
    private final PresetLibraryPanel libraryPanel;
    private final SpectrumPanel spectrumPanel;

    public MarkovMainWindow(MarkovParameters params, MarkovAudioEngine engine, SampleBank sampleBank) {
        super("Markov Chain - Generative Music");
        this.params = params;
        this.engine = engine;
        this.sampleBank = sampleBank;
        this.cueLibrary = new CueLibrary();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Theme.BG);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setOpaque(false);
        Theme.tagBg(mainPanel, "bg");

        // === WEST: Global parameters ===
        globalPanel = new GlobalParameterPanel(params, sampleBank);
        JScrollPane leftScroll = new JScrollPane(globalPanel);
        leftScroll.setPreferredSize(new Dimension(240, 0));
        leftScroll.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER));
        leftScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        leftScroll.getViewport().setOpaque(false);
        leftScroll.setOpaque(false);
        mainPanel.add(leftScroll, BorderLayout.WEST);

        // === EAST: Preset Library ===
        libraryPanel = new PresetLibraryPanel(params, cueLibrary);
        JScrollPane rightScroll = new JScrollPane(libraryPanel);
        rightScroll.setPreferredSize(new Dimension(260, 0));
        rightScroll.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Theme.BORDER));
        rightScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rightScroll.getViewport().setOpaque(false);
        rightScroll.setOpaque(false);
        mainPanel.add(rightScroll, BorderLayout.EAST);

        // === CENTER: split vertically — top (matrix+strata) | bottom (spectrum) ===
        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        centerSplit.setOpaque(false);
        centerSplit.setBorder(BorderFactory.createEmptyBorder());
        centerSplit.setDividerSize(4);
        centerSplit.setResizeWeight(0.55); // top gets ~55%

        // -- Top: matrix (left half) + strata (right half) --
        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        topSplit.setOpaque(false);
        topSplit.setBorder(BorderFactory.createEmptyBorder());
        topSplit.setDividerSize(4);
        topSplit.setResizeWeight(0.45); // matrix gets ~45%, strata gets ~55%

        // Matrix
        JPanel matrixWrapper = new JPanel(new BorderLayout());
        matrixWrapper.setOpaque(false);
        matrixWrapper.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 2));

        JLabel matrixTitle = new JLabel("Transition Matrix");
        Theme.tagFont(matrixTitle, "heading");
        Theme.tagFg(matrixTitle, "fg");
        matrixTitle.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 0));
        matrixWrapper.add(matrixTitle, BorderLayout.NORTH);

        matrixPanel = new MatrixEditorPanel(params);
        matrixWrapper.add(matrixPanel, BorderLayout.CENTER);

        topSplit.setLeftComponent(matrixWrapper);

        // Strata
        strataPanel = new StrataPanel(params);
        JPanel strataWrapper = new JPanel(new BorderLayout());
        strataWrapper.setOpaque(false);
        strataWrapper.setBorder(BorderFactory.createEmptyBorder(6, 2, 6, 6));
        strataWrapper.add(strataPanel, BorderLayout.CENTER);

        topSplit.setRightComponent(strataWrapper);

        centerSplit.setTopComponent(topSplit);

        // -- Bottom: spectrum visualizer --
        spectrumPanel = new SpectrumPanel(engine.getSpectrumAnalyzer());
        spectrumPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));

        centerSplit.setBottomComponent(spectrumPanel);

        mainPanel.add(centerSplit, BorderLayout.CENTER);

        // === SOUTH: Transport bar (full width) ===
        transportPanel = new TransportPanel(params, cueLibrary);
        mainPanel.add(transportPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Set split pane positions after layout
        SwingUtilities.invokeLater(() -> {
            centerSplit.setDividerLocation(0.55);
            topSplit.setDividerLocation(0.45);
        });

        // === Wiring ===
        transportPanel.setPlayStopListener(this::onPlayStop);
        transportPanel.setCueLoadListener(this::onCueLoaded);
        libraryPanel.setCueLoadListener(this::onCueLoaded);

        strataPanel.setEngine(engine);

        setJMenuBar(createMenuBar());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                strataPanel.stopMetering();
                spectrumPanel.stop();
                engine.stop();
                System.exit(0);
            }
        });
    }

    private void onPlayStop(boolean play) {
        if (play) {
            engine.start();
            spectrumPanel.start();
            strataPanel.startMetering();
        } else {
            engine.stop();
            spectrumPanel.stop();
            strataPanel.stopMetering();
        }
    }

    private void onCueLoaded() {
        globalPanel.syncFromModel();
        strataPanel.syncFromModel();
        revalidate();
        repaint();
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu viewMenu = new JMenu("View");
        JMenu themeMenu = new JMenu("Theme");

        ButtonGroup themeGroup = new ButtonGroup();
        for (ThemePreset preset : ThemePreset.values()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(preset.displayName);
            item.setSelected(preset == ThemePreferences.load());
            item.addActionListener(e -> {
                Theme.applyTheme(preset);
                ThemePreferences.save(preset);
                Theme.resetExplicitProperties(this);
                Theme.refreshTaggedProperties(this);
                SwingUtilities.updateComponentTreeUI(this);
                revalidate();
                repaint();
            });
            themeGroup.add(item);
            themeMenu.add(item);
        }
        viewMenu.add(themeMenu);
        bar.add(viewMenu);

        return bar;
    }
}
