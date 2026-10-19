package dev.stas.bouncecraft.logic;

/**
 * Squash/stretch and beat phase, ported from ViceBounce (MIT)
 * https://github.com/tek-95/ViceBounce — BounceMath.h / BeatMap.h
 */
public final class PulseMath {
    public static final float DEFAULT_AMPLITUDE = 0.35f;
    public static final int DEFAULT_EASING = 1;
    public static final double MIN_BPM = 20.0;
    public static final double MAX_BPM = 300.0;

    private PulseMath() {
    }

    public static double ease(double x, int type) {
        x = clamp01(x);
        if (type == 1) {
            return (1.0 - Math.cos(Math.PI * x)) * 0.5;
        }
        if (type == 2) {
            return x < 0.5 ? 2.0 * x * x : 1.0 - Math.pow(-2.0 * x + 2.0, 2.0) * 0.5;
        }
        if (type == 3) {
            return x < 0.5 ? (1.0 - bounceEaseOut(1.0 - 2.0 * x)) * 0.5 : (1.0 + bounceEaseOut(2.0 * x - 1.0)) * 0.5;
        }
        return x;
    }

    /**
     * Phase 0.5 is on every beat (maximum squash). Phase 0 is the midpoint stretch.
     */
    public static double musicPhase(double positionMs, double firstBeatMs, double bpm) {
        if (!Double.isFinite(positionMs) || !Double.isFinite(firstBeatMs) || !Double.isFinite(bpm)) {
            return 0.5;
        }
        bpm = Math.clamp(bpm, MIN_BPM, MAX_BPM);
        return wrap01((positionMs - firstBeatMs) * bpm / 60_000.0 + 0.5);
    }

    /**
     * Interpolates between consecutive detected beats so each beat is a squash peak
     * even when tempo varies. Needs at least two timestamps.
     */
    public static double beatMapPhase(int[] beatsMs, double positionMs) {
        if (beatsMs == null || beatsMs.length < 2 || !Double.isFinite(positionMs)) {
            return 0.5;
        }
        int next = 0;
        while (next < beatsMs.length && beatsMs[next] <= positionMs) {
            next++;
        }
        int i = next == 0 ? 0 : next == beatsMs.length ? beatsMs.length - 2 : next - 1;
        double span = beatsMs[i + 1] - (double) beatsMs[i];
        if (span <= 0.0) {
            return 0.5;
        }
        return wrap01((positionMs - beatsMs[i]) / span + 0.5);
    }

    public static double phase(int[] beatsMs, double positionMs, double bpm) {
        if (beatsMs != null && beatsMs.length >= 2) {
            return beatMapPhase(beatsMs, positionMs);
        }
        double first = beatsMs != null && beatsMs.length == 1 ? beatsMs[0] : 0.0;
        return musicPhase(positionMs, first, bpm);
    }

    /**
     * Opposing squash/stretch. On-beat (phase 0.5): wide and short.
     * Mid-beat (phase 0): narrow and tall. Identity at phase 0.25 and 0.75.
     */
    public static float[] getScale(double phase, double amplitude, int easing) {
        if (!Double.isFinite(phase)) {
            phase = 0.0;
        }
        phase -= Math.floor(phase);
        double t = phase < 0.5 ? phase * 2.0 : (1.0 - phase) * 2.0;
        double centered = 2.0 * ease(t, easing) - 1.0;
        if (!Double.isFinite(amplitude)) {
            amplitude = DEFAULT_AMPLITUDE;
        }
        amplitude = Math.clamp(amplitude, 0.0, 0.75);
        float horizontal = (float) (1.0 + amplitude * centered);
        float vertical = (float) (1.0 - amplitude * centered);
        return new float[] {horizontal, vertical, horizontal};
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

    private static double bounceEaseOut(double t) {
        final double n = 7.5625;
        final double d = 2.75;
        if (t < 1.0 / d) {
            return n * t * t;
        }
        if (t < 2.0 / d) {
            t -= 1.5 / d;
            return n * t * t + 0.75;
        }
        if (t < 2.5 / d) {
            t -= 2.25 / d;
            return n * t * t + 0.9375;
        }
        t -= 2.625 / d;
        return n * t * t + 0.984375;
    }

    private static double wrap01(double value) {
        double result = value % 1.0;
        return result < 0.0 ? result + 1.0 : result;
    }

    private static double clamp01(double x) {
        if (x < 0.0) {
            return 0.0;
        }
        if (x > 1.0) {
            return 1.0;
        }
        return x;
    }
}
