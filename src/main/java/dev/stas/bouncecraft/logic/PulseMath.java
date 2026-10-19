package dev.stas.bouncecraft.logic;

public final class PulseMath {
    public static final float DEFAULT_TAU_SEC = 0.12f;
    public static final float DEFAULT_AMPLITUDE = 0.35f;

    private PulseMath() {
    }

    public static float envelope(float secondsAfterBeat, float tauSec) {
        if (secondsAfterBeat < 0) {
            return 0f;
        }
        return (float) Math.exp(-secondsAfterBeat / tauSec);
    }

    public static float timeSinceLastBeatSec(int[] beatsMs, int playheadMs) {
        int last = -1;
        for (int beat : beatsMs) {
            if (beat <= playheadMs) {
                last = beat;
            } else {
                break;
            }
        }
        if (last < 0) {
            return 10f;
        }
        return (playheadMs - last) / 1000f;
    }

    public static float[] squashScale(float envelope, float amplitude) {
        float sy = 1f - amplitude * envelope;
        if (sy < 0.05f) {
            sy = 0.05f;
        }
        float sx = (float) (1.0 / Math.sqrt(sy));
        return new float[] {sx, sy, sx};
    }

    public static float distanceVolume(double distance, double radius) {
        if (radius <= 0) {
            return 0f;
        }
        if (distance >= radius) {
            return 0f;
        }
        return (float) Math.max(0.0, 1.0 - distance / radius);
    }
}
