package org.delightofcomposition.gui;

import javax.swing.*;
import java.awt.*;

import org.delightofcomposition.markov.CueLibrary;
import org.delightofcomposition.markov.MarkovParameters;

/**
 * Bottom transport bar with play/stop and status.
 */
public class TransportPanel extends JPanel {
    private final JButton playStopButton;
    private boolean playing = false;

    private PlayStopListener playStopListener;
    private CueLoadListener cueLoadListener;

    public interface PlayStopListener {
        void onPlayStop(boolean play);
    }

    public interface CueLoadListener {
        void onCueLoaded();
    }

    public TransportPanel(MarkovParameters params, CueLibrary cueLibrary) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        setOpaque(false);

        // Play/Stop button
        playStopButton = Theme.primaryButton("Play");
        playStopButton.setPreferredSize(new Dimension(100, 36));
        playStopButton.addActionListener(e -> {
            playing = !playing;
            playStopButton.setText(playing ? "Stop" : "Play");
            if (playStopListener != null) playStopListener.onPlayStop(playing);
        });
        add(playStopButton);

        // Status label
        JLabel statusLabel = Theme.paramLabel("Select a cue or matrix preset from the right panel, then press Play");
        add(statusLabel);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Theme.BG_MUTED);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Theme.BORDER);
        g.drawLine(0, 0, getWidth(), 0);
    }

    public void setPlayStopListener(PlayStopListener l) { this.playStopListener = l; }
    public void setCueLoadListener(CueLoadListener l) { this.cueLoadListener = l; }

    public void setPlaying(boolean playing) {
        this.playing = playing;
        playStopButton.setText(playing ? "Stop" : "Play");
    }
}
