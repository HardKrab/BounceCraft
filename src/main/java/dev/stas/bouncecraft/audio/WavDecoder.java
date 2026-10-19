package dev.stas.bouncecraft.audio;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class WavDecoder {
    private WavDecoder() {
    }

    public record Pcm(float[] mono, int sampleRate, int durationMs) {
    }

    public static Pcm decode(byte[] wavBytes, int maxDurationSec) throws IOException, UnsupportedAudioFileException {
        try (AudioInputStream in = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wavBytes))) {
            AudioFormat src = in.getFormat();
            AudioFormat pcm = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    src.getSampleRate(),
                    16,
                    src.getChannels(),
                    src.getChannels() * 2,
                    src.getSampleRate(),
                    false
            );
            try (AudioInputStream converted = AudioSystem.isConversionSupported(pcm, src)
                    ? AudioSystem.getAudioInputStream(pcm, in)
                    : in) {
                byte[] raw = converted.readAllBytes();
                AudioFormat used = converted.getFormat();
                int channels = Math.max(1, used.getChannels());
                int sampleRate = Math.round(used.getSampleRate());
                float[] mono = PcmUtil.pcm16leToMonoFloat(raw, channels);
                int maxSamples = maxDurationSec * sampleRate;
                if (maxSamples > 0 && mono.length > maxSamples) {
                    float[] trimmed = new float[maxSamples];
                    System.arraycopy(mono, 0, trimmed, 0, maxSamples);
                    mono = trimmed;
                }
                return new Pcm(mono, sampleRate, PcmUtil.durationMs(mono.length, sampleRate));
            }
        }
    }
}
