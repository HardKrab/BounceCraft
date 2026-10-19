package dev.stas.bouncecraft.client;

import dev.stas.bouncecraft.audio.PcmUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.sounds.JOrbisAudioStream;

public final class OggDecoder {
    private OggDecoder() {
    }

    public record Pcm(float[] mono, int sampleRate, int durationMs) {
    }

    public static Pcm decode(byte[] oggBytes, int maxDurationSec) throws IOException {
        try (JOrbisAudioStream stream = new JOrbisAudioStream(new ByteArrayInputStream(oggBytes))) {
            AudioFormat format = stream.getFormat();
            int channels = Math.max(1, format.getChannels());
            int sampleRate = Math.round(format.getSampleRate());
            ByteArrayOutputStream raw = new ByteArrayOutputStream();
            int maxBytes = maxDurationSec > 0 ? maxDurationSec * sampleRate * channels * 2 : Integer.MAX_VALUE;
            while (raw.size() < maxBytes) {
                ByteBuffer buffer = stream.read(4096);
                if (buffer == null || !buffer.hasRemaining()) {
                    break;
                }
                byte[] chunk = new byte[buffer.remaining()];
                buffer.get(chunk);
                int canWrite = Math.min(chunk.length, maxBytes - raw.size());
                raw.write(chunk, 0, canWrite);
                if (canWrite < chunk.length) {
                    break;
                }
            }
            float[] mono = PcmUtil.pcm16leToMonoFloat(raw.toByteArray(), channels);
            return new Pcm(mono, sampleRate, PcmUtil.durationMs(mono.length, sampleRate));
        }
    }
}
