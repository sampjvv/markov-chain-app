package org.delightofcomposition.audio;

import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * Loads and holds the sample used for pitch-shifted playback.
 * Supports 16-bit and 24-bit WAV files, mono or stereo.
 * Supports hot-swapping samples at runtime.
 */
public class SampleBank {
    public static final double DEFAULT_ORIG_FREQ = 880.0;

    private volatile float[] sampleData;
    private volatile float sampleRate;
    private volatile double origFreq = DEFAULT_ORIG_FREQ;
    private volatile String currentFile;

    public SampleBank(String filePath) {
        loadSample(filePath);
    }

    /** Load a new sample. Thread-safe — can be called while audio engine is running. */
    public void loadSample(String filePath) {
        float[] data = null;
        float rate = 48000;

        try {
            File file = resolveFile(filePath);
            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            AudioFormat fmt = ais.getFormat();
            rate = fmt.getSampleRate();
            int channels = fmt.getChannels();
            int bytesPerSample = fmt.getSampleSizeInBits() / 8;
            int bytesPerFrame = fmt.getFrameSize();
            int frameCount = (int) ais.getFrameLength();

            byte[] rawBytes = new byte[frameCount * bytesPerFrame];
            int totalRead = 0;
            while (totalRead < rawBytes.length) {
                int read = ais.read(rawBytes, totalRead, rawBytes.length - totalRead);
                if (read < 0) break;
                totalRead += read;
            }
            ais.close();

            data = new float[frameCount];
            float max = 0;

            for (int frame = 0; frame < frameCount; frame++) {
                float mixed = 0;
                for (int ch = 0; ch < channels; ch++) {
                    int offset = frame * bytesPerFrame + ch * bytesPerSample;
                    int sample;

                    if (bytesPerSample == 3) {
                        // 24-bit little-endian signed
                        int b0 = rawBytes[offset] & 0xFF;
                        int b1 = rawBytes[offset + 1] & 0xFF;
                        int b2 = rawBytes[offset + 2]; // signed for sign extension
                        sample = (b2 << 16) | (b1 << 8) | b0;
                    } else if (bytesPerSample == 2) {
                        // 16-bit
                        if (fmt.isBigEndian()) {
                            sample = (rawBytes[offset] << 8) | (rawBytes[offset + 1] & 0xFF);
                        } else {
                            sample = (rawBytes[offset + 1] << 8) | (rawBytes[offset] & 0xFF);
                        }
                    } else {
                        // 8-bit unsigned
                        sample = (rawBytes[offset] & 0xFF) - 128;
                    }

                    mixed += sample;
                }
                mixed /= channels;
                max = Math.max(max, Math.abs(mixed));
                data[frame] = mixed;
            }

            // Normalize to [-1, 1]
            if (max > 0) {
                for (int i = 0; i < data.length; i++) {
                    data[i] /= max;
                }
            }

            currentFile = file.getName();
            System.out.println("Loaded sample: " + currentFile +
                " (" + frameCount + " frames, " + rate + " Hz, " +
                (bytesPerSample * 8) + "-bit, " + channels + "ch)");

        } catch (Exception e) {
            System.err.println("Failed to load sample: " + e.getMessage());
            e.printStackTrace();
            data = new float[48000];
            for (int i = 0; i < data.length; i++) {
                data[i] = (float) Math.sin(2 * Math.PI * DEFAULT_ORIG_FREQ * i / 48000.0);
            }
            currentFile = "(sine fallback)";
            System.out.println("Using synthesized sine wave fallback");
        }

        this.sampleData = data;
        this.sampleRate = rate;
    }

    private File resolveFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) return file;
        file = new File("resources/" + filePath);
        if (file.exists()) return file;
        throw new RuntimeException("Sample not found: " + filePath);
    }

    public float[] getSampleData() { return sampleData; }
    public float getSampleRate() { return sampleRate; }
    public int getLength() { return sampleData.length; }
    public double getOrigFreq() { return origFreq; }
    public void setOrigFreq(double freq) { this.origFreq = freq; }
    public String getCurrentFile() { return currentFile; }
}
