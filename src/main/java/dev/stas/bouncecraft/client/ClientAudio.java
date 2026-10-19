package dev.stas.bouncecraft.client;

import dev.stas.bouncecraft.audio.Mp3Decoder;
import dev.stas.bouncecraft.audio.PcmUtil;
import dev.stas.bouncecraft.audio.WavDecoder;
import dev.stas.bouncecraft.logic.AudioFormatId;
import dev.stas.bouncecraft.logic.BeatDetector;
import dev.stas.bouncecraft.logic.SessionRules;
import dev.stas.bouncecraft.logic.Sha256;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class ClientAudio {
    private ClientAudio() {
    }

    public record PreparedTrack(
            byte[] rawBytes,
            byte[] sha256,
            AudioFormatId format,
            String title,
            int durationMs,
            float bpm,
            int[] beatsMs,
            byte[] pcm16leMono,
            int sampleRate
    ) {
    }

    public static PreparedTrack prepare(Path file, int maxDurationSec) throws IOException, UnsupportedAudioFileException {
        AudioFormatId format = AudioFormatId.fromFileName(file.getFileName().toString())
                .orElseThrow(() -> new IOException("unsupported format"));
        byte[] raw = Files.readAllBytes(file);
        float[] mono;
        int sampleRate;
        int durationMs;
        switch (format) {
            case WAV -> {
                WavDecoder.Pcm pcm = WavDecoder.decode(raw, maxDurationSec);
                mono = pcm.mono();
                sampleRate = pcm.sampleRate();
                durationMs = pcm.durationMs();
            }
            case MP3 -> {
                Mp3Decoder.Pcm pcm = Mp3Decoder.decode(raw, maxDurationSec);
                mono = pcm.mono();
                sampleRate = pcm.sampleRate();
                durationMs = pcm.durationMs();
            }
            case OGG -> {
                OggDecoder.Pcm pcm = OggDecoder.decode(raw, maxDurationSec);
                mono = pcm.mono();
                sampleRate = pcm.sampleRate();
                durationMs = pcm.durationMs();
            }
            default -> throw new IOException("unsupported format");
        }
        BeatDetector.Result beats = BeatDetector.detect(mono, sampleRate);
        String title = SessionRules.sanitizeTitle(stripExtension(file.getFileName().toString()));
        return new PreparedTrack(
                raw,
                Sha256.hash(raw),
                format,
                title,
                durationMs,
                beats.bpm(),
                beats.beatsMs(),
                PcmUtil.monoFloatToPcm16le(mono),
                sampleRate
        );
    }

    private static String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
