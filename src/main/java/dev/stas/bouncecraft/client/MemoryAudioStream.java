package dev.stas.bouncecraft.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.sounds.AudioStream;

public final class MemoryAudioStream implements AudioStream {
    private final AudioFormat format;
    private final byte[] pcm;
    private int position;

    public MemoryAudioStream(byte[] pcm16leMono, int sampleRate, int skipMs) {
        this.format = new AudioFormat(sampleRate, 16, 1, true, false);
        this.pcm = pcm16leMono;
        int skipBytes = (int) (skipMs / 1000.0 * sampleRate) * 2;
        this.position = Math.max(0, Math.min(pcm.length, skipBytes & ~1));
    }

    public synchronized int playheadMs(int delayMs) {
        int sampleRate = Math.round(format.getSampleRate());
        int samples = position / 2 - (int) (delayMs / 1000.0 * sampleRate);
        if (samples < 0) {
            samples = 0;
        }
        return (int) (samples * 1000L / sampleRate);
    }

    public synchronized boolean ended() {
        return position >= pcm.length;
    }

    @Override
    public AudioFormat getFormat() {
        return format;
    }

    @Override
    public synchronized ByteBuffer read(int size) throws IOException {
        int remaining = pcm.length - position;
        int n = Math.min(size, remaining);
        ByteBuffer buffer = ByteBuffer.allocateDirect(Math.max(n, 0)).order(ByteOrder.LITTLE_ENDIAN);
        if (n > 0) {
            buffer.put(pcm, position, n);
            position += n;
        }
        buffer.flip();
        return buffer;
    }

    @Override
    public void close() {
    }
}
