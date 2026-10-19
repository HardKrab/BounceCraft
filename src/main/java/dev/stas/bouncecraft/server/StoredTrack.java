package dev.stas.bouncecraft.server;

public record StoredTrack(
        byte[] sha256,
        byte[] bytes,
        String format,
        String title,
        int durationMs,
        int[] beatsMs,
        float bpm
) {
}
