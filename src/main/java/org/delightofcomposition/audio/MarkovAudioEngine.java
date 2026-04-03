package org.delightofcomposition.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.delightofcomposition.markov.MarkovParameters;
import org.delightofcomposition.markov.Stratum;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Real-time audio engine that schedules Markov-generated notes
 * across 6 strata and mixes them to stereo output.
 */
public class MarkovAudioEngine {
    public static final int SAMPLE_RATE = 48000;
    private static final int BUFFER_SIZE = 1024;
    private static final int MAX_VOICES = 256;

    private final MarkovParameters params;
    private final SampleBank sampleBank;
    private final SimpleReverb reverb;

    private final AtomicBoolean shouldRun = new AtomicBoolean(false);
    private volatile boolean running;
    private volatile Thread audioThread;

    // Voice pool (pre-allocated)
    private final SampleVoice[] voices;

    // Per-stratum scheduling
    private final double[] nextNoteTime;
    private final double[] elapsed;

    // Pre-allocated audio buffers
    private final double[] mixBufferL;
    private final double[] mixBufferR;
    private final byte[] byteBuffer;

    // Spectrum analyzer for visualization
    private final SpectrumAnalyzer spectrumAnalyzer;

    // Callback for UI note indicator updates
    private volatile NoteListener noteListener;

    public interface NoteListener {
        void onNote(int stratumIndex, int noteIndex, String noteName);
    }

    public MarkovAudioEngine(MarkovParameters params, SampleBank sampleBank) {
        this.params = params;
        this.sampleBank = sampleBank;
        this.reverb = new SimpleReverb();
        this.voices = new SampleVoice[MAX_VOICES];
        for (int i = 0; i < MAX_VOICES; i++) {
            voices[i] = new SampleVoice();
        }

        this.nextNoteTime = new double[6];
        this.elapsed = new double[6];
        this.mixBufferL = new double[BUFFER_SIZE];
        this.mixBufferR = new double[BUFFER_SIZE];
        this.byteBuffer = new byte[BUFFER_SIZE * 4];
        this.spectrumAnalyzer = new SpectrumAnalyzer(800); // ~800 columns of history
    }

    public void setNoteListener(NoteListener listener) {
        this.noteListener = listener;
    }

    /** Start the audio engine on a new thread. Safe to call multiple times. */
    public synchronized void start() {
        if (running) return;
        shouldRun.set(true);

        // Reset scheduling state
        for (int s = 0; s < 6; s++) {
            nextNoteTime[s] = 0;
            elapsed[s] = 0;
        }

        audioThread = new Thread(this::run, "MarkovAudio");
        audioThread.setDaemon(true);
        audioThread.start();
    }

    /** Stop the audio engine and wait for the thread to finish. */
    public synchronized void stop() {
        shouldRun.set(false);

        // Kill all voices immediately
        for (SampleVoice voice : voices) {
            voice.kill();
        }

        // Wait for audio thread to finish
        Thread t = audioThread;
        if (t != null) {
            try { t.join(2000); } catch (InterruptedException ignored) {}
            audioThread = null;
        }
        running = false;
    }

    private void run() {
        if (!shouldRun.get()) return;
        running = true;

        AudioFormat format = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            SAMPLE_RATE, 16, 2, 4,
            SAMPLE_RATE, false);

        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format, BUFFER_SIZE * 8);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format, BUFFER_SIZE * 8);
            line.start();

            System.out.println("Markov audio engine started (48kHz, 16-bit, stereo)");

            while (shouldRun.get()) {
                scheduleNotes();

                // Read sample data fresh each block (supports hot-swap)
                float[] sampleData = sampleBank.getSampleData();
                int sampleLength = sampleBank.getLength();

                // Clear mix buffers
                for (int i = 0; i < BUFFER_SIZE; i++) {
                    mixBufferL[i] = 0;
                    mixBufferR[i] = 0;
                }

                // Render all active voices
                for (SampleVoice voice : voices) {
                    if (voice.isAlive()) {
                        voice.render(sampleData, sampleLength, mixBufferL, mixBufferR, BUFFER_SIZE);
                    }
                }

                double master = params.getMasterVolume();

                // Apply reverb
                reverb.setRoom(params.getReverbRoom());
                reverb.setMix(params.getReverbMix());
                reverb.setDamp(params.getReverbDamp());
                reverb.process(mixBufferL, mixBufferR, BUFFER_SIZE);

                // Feed spectrum analyzer
                spectrumAnalyzer.feed(mixBufferL, mixBufferR, BUFFER_SIZE);

                // Soft clip and convert to PCM
                for (int i = 0; i < BUFFER_SIZE; i++) {
                    double sampleL = mixBufferL[i] * master;
                    double sampleR = mixBufferR[i] * master;
                    if (sampleL > 1.0) sampleL = 1.0;
                    else if (sampleL < -1.0) sampleL = -1.0;
                    if (sampleR > 1.0) sampleR = 1.0;
                    else if (sampleR < -1.0) sampleR = -1.0;

                    int pcmL = (int)(sampleL * Short.MAX_VALUE);
                    int pcmR = (int)(sampleR * Short.MAX_VALUE);
                    byteBuffer[i * 4]     = (byte)(pcmL & 0xFF);
                    byteBuffer[i * 4 + 1] = (byte)((pcmL >> 8) & 0xFF);
                    byteBuffer[i * 4 + 2] = (byte)(pcmR & 0xFF);
                    byteBuffer[i * 4 + 3] = (byte)((pcmR >> 8) & 0xFF);
                }

                line.write(byteBuffer, 0, byteBuffer.length);

                for (int s = 0; s < 6; s++) {
                    elapsed[s] += BUFFER_SIZE;
                }
            }

            line.drain();
            line.stop();
            line.close();
            System.out.println("Markov audio engine stopped.");
        } catch (Exception e) {
            System.err.println("Audio engine error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            running = false;
        }
    }

    private void scheduleNotes() {
        for (int s = 0; s < 6; s++) {
            Stratum stratum = params.getStratum(s);
            if (!stratum.isActive()) continue;

            if (elapsed[s] >= nextNoteTime[s]) {
                int noteIdx = params.getEngine().getNextNoteIndex(stratum.getCurrentNoteIndex());
                stratum.setCurrentNoteIndex(noteIdx);

                params.updateNoteLimits();

                int midi = params.getScale().midiNote(noteIdx, stratum.getOctaveBase());
                midi = params.clampMidi(midi);

                double freq = 440.0 * Math.pow(2, (midi - 69) / 12.0);
                double amp = stratum.smoothAmplitude();
                double pan = (Math.random() - 0.5) * 0.6;

                spawnVoice(freq, amp, pan);

                NoteListener listener = noteListener;
                if (listener != null) {
                    String noteName = params.getScale().noteName(noteIdx);
                    listener.onNote(s, noteIdx, noteName);
                }

                double duration = params.getRandomDuration(stratum.getRateLevel());
                nextNoteTime[s] = duration * SAMPLE_RATE;
                elapsed[s] = 0;
            }
        }
    }

    private void spawnVoice(double freq, double amplitude, double pan) {
        double origFreq = sampleBank.getOrigFreq();
        for (SampleVoice voice : voices) {
            if (!voice.isAlive()) {
                voice.trigger(freq, amplitude, pan, origFreq);
                return;
            }
        }
        voices[0].trigger(freq, amplitude, pan, origFreq);
    }

    public boolean isRunning() {
        return running;
    }

    public SpectrumAnalyzer getSpectrumAnalyzer() {
        return spectrumAnalyzer;
    }

    public int getActiveVoiceCount() {
        int count = 0;
        for (SampleVoice v : voices) {
            if (v.isAlive()) count++;
        }
        return count;
    }
}
