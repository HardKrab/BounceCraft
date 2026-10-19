package dev.stas.bouncecraft.logic;

public final class BeatGrid {
    private BeatGrid() {
    }

    public static int[] fromBpm(float bpm, int firstBeatMs, int durationMs) {
        if (bpm <= 0 || durationMs <= 0) {
            return new int[0];
        }
        double periodMs = 60_000.0 / bpm;
        int capacity = (int) Math.ceil((durationMs - firstBeatMs) / periodMs) + 2;
        int[] tmp = new int[Math.max(capacity, 1)];
        int count = 0;
        double t = firstBeatMs;
        while (t <= durationMs + 0.5 && count < 4000) {
            int ms = (int) Math.round(t);
            if (ms >= 0 && ms <= durationMs) {
                tmp[count++] = ms;
            }
            t += periodMs;
        }
        int[] beats = new int[count];
        System.arraycopy(tmp, 0, beats, 0, count);
        return beats;
    }

    public static int[] retimed(int[] existing, float newBpm, int durationMs) {
        int first = existing.length == 0 ? 0 : existing[0];
        return fromBpm(newBpm, first, durationMs);
    }
}
