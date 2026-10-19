package dev.stas.bouncecraft.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.junit.jupiter.api.Test;

class WavDecoderTest {
    @Test
    void decodesMonoPcmWav() throws IOException, UnsupportedAudioFileException {
        int rate = 22050;
        int n = rate; // 1 second
        byte[] pcm = new byte[n * 2];
        ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < n; i++) {
            short s = (short) Math.round(Math.sin(2 * Math.PI * 440 * i / (double) rate) * 16000);
            pcm[i * 2] = (byte) (s & 0xFF);
            pcm[i * 2 + 1] = (byte) ((s >> 8) & 0xFF);
        }
        byte[] wav = wrapWav(pcm, rate, 1);
        WavDecoder.Pcm decoded = WavDecoder.decode(wav, 10);
        assertEquals(rate, decoded.sampleRate());
        assertEquals(1000, decoded.durationMs(), 20);
        assertTrue(decoded.mono().length >= rate - 10);
    }

    static byte[] wrapWav(byte[] pcm, int sampleRate, int channels) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int byteRate = sampleRate * channels * 2;
        try {
            out.write(new byte[] {'R', 'I', 'F', 'F'});
            writeInt(out, 36 + pcm.length);
            out.write(new byte[] {'W', 'A', 'V', 'E', 'f', 'm', 't', ' '});
            writeInt(out, 16);
            writeShort(out, 1);
            writeShort(out, channels);
            writeInt(out, sampleRate);
            writeInt(out, byteRate);
            writeShort(out, channels * 2);
            writeShort(out, 16);
            out.write(new byte[] {'d', 'a', 't', 'a'});
            writeInt(out, pcm.length);
            out.write(pcm);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return out.toByteArray();
    }

    private static void writeInt(ByteArrayOutputStream out, int v) {
        out.write(v & 0xFF);
        out.write((v >> 8) & 0xFF);
        out.write((v >> 16) & 0xFF);
        out.write((v >> 24) & 0xFF);
    }

    private static void writeShort(ByteArrayOutputStream out, int v) {
        out.write(v & 0xFF);
        out.write((v >> 8) & 0xFF);
    }
}
