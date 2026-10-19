package dev.stas.bouncecraft.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BeatDetectorTest {
    @Test
    void detectsHundredTwentyBpmClickTrack() {
        int rate = BeatDetector.TARGET_RATE;
        int bpm = 120;
        int durationSec = 8;
        float[] samples = clickTrack(rate, bpm, durationSec, 0);

        BeatDetector.Result result = BeatDetector.detect(samples, rate);

        assertEquals(120.0, result.bpm(), 1.0, "BPM should be 120 ± 1, was " + result.bpm());
        assertTrue(result.beatsMs().length >= 12, "expected many beats, got " + result.beatsMs().length);

        int periodMs = 500;
        int within = 0;
        for (int beat : result.beatsMs()) {
            int phase = ((beat % periodMs) + periodMs) % periodMs;
            int error = Math.min(phase, periodMs - phase);
            if (error <= 15) {
                within++;
            }
        }
        double ratio = within / (double) result.beatsMs().length;
        assertTrue(ratio >= 0.7, "at least 70% of beats should be within 15ms of the 120 BPM grid, was "
                + ratio + " (" + within + "/" + result.beatsMs().length + ")");
    }

    @Test
    void retimesGridFromFirstBeat() {
        int[] original = {40, 540, 1040, 1540};
        int[] retimed = BeatGrid.retimed(original, 60f, 4000);
        assertEquals(40, retimed[0]);
        assertEquals(1040, retimed[1]);
        assertEquals(2040, retimed[2]);
    }

    private static float[] clickTrack(int rate, int bpm, int durationSec, int offsetMs) {
        int n = rate * durationSec;
        float[] samples = new float[n];
        double period = 60.0 / bpm;
        int click = Math.max(32, rate / 400);
        for (double t = offsetMs / 1000.0; t < durationSec; t += period) {
            int start = (int) Math.round(t * rate);
            for (int i = 0; i < click && start + i < n; i++) {
                // Short noisy burst so spectral flux sees a sharp onset.
                samples[start + i] = (i % 2 == 0 ? 1f : -1f) * (1f - i / (float) click);
            }
        }
        return samples;
    }
}
