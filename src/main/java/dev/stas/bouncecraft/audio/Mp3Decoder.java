package dev.stas.bouncecraft.audio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

public final class Mp3Decoder {
    private Mp3Decoder() {
    }

    public record Pcm(float[] mono, int sampleRate, int durationMs) {
    }

    public static Pcm decode(byte[] mp3Bytes, int maxDurationSec) throws IOException {
        Bitstream bitstream = new Bitstream(new ByteArrayInputStream(mp3Bytes));
        Decoder decoder = new Decoder();
        ByteArrayOutputStream pcmOut = new ByteArrayOutputStream();
        int sampleRate = 44100;
        int channels = 2;
        try {
            Header header;
            int maxSamples = Integer.MAX_VALUE;
            int writtenFrames = 0;
            while ((header = bitstream.readFrame()) != null) {
                SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);
                sampleRate = output.getSampleFrequency();
                channels = output.getChannelCount();
                if (maxDurationSec > 0) {
                    maxSamples = maxDurationSec * sampleRate;
                }
                short[] buffer = output.getBuffer();
                int n = output.getBufferLength();
                int frames = n / channels;
                if (writtenFrames + frames > maxSamples) {
                    frames = Math.max(0, maxSamples - writtenFrames);
                }
                for (int i = 0; i < frames; i++) {
                    int base = i * channels;
                    int sum = 0;
                    for (int ch = 0; ch < channels; ch++) {
                        sum += buffer[base + ch];
                    }
                    short mono = (short) (sum / channels);
                    pcmOut.write(mono & 0xFF);
                    pcmOut.write((mono >> 8) & 0xFF);
                }
                writtenFrames += frames;
                bitstream.closeFrame();
                if (writtenFrames >= maxSamples) {
                    break;
                }
            }
        } catch (BitstreamException | DecoderException e) {
            throw new IOException("MP3 decode failed", e);
        } finally {
            try {
                bitstream.close();
            } catch (BitstreamException ignored) {
            }
        }
        float[] mono = PcmUtil.pcm16leToMonoFloat(pcmOut.toByteArray(), 1);
        return new Pcm(mono, sampleRate, PcmUtil.durationMs(mono.length, sampleRate));
    }
}
