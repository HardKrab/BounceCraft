package dev.stas.bouncecraft.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PulseMathTest {
    private static final int[] BEATS_120 = {0, 500, 1000, 1500};

    @Test
    void onBeatIsMaximumSquash() {
        assertEquals(0.5, PulseMath.beatMapPhase(BEATS_120, 500), 1e-6);
        float[] scale = PulseMath.getScale(0.5, 0.20, 1);
        assertEquals(1.2f, scale[0], 1e-5);
        assertEquals(0.8f, scale[1], 1e-5);
        assertEquals(1.2f, scale[2], 1e-5);
    }

    @Test
    void halfBeatIsMaximumStretch() {
        assertEquals(0.0, PulseMath.beatMapPhase(BEATS_120, 250), 1e-6);
        float[] scale = PulseMath.getScale(0.0, 0.20, 1);
        assertEquals(0.8f, scale[0], 1e-5);
        assertEquals(1.2f, scale[1], 1e-5);
    }

    @Test
    void identityQuarterPeriodAfterBeatAt120Bpm() {
        double phase = PulseMath.beatMapPhase(BEATS_120, 500 + 125);
        assertEquals(0.75, phase, 1e-6);
        float[] scale = PulseMath.getScale(phase, 0.35, 1);
        assertEquals(1f, scale[0], 1e-4);
        assertEquals(1f, scale[1], 1e-4);
    }

    @Test
    void musicPhaseLandsOnBeat() {
        double first = 173.0;
        double bpm = 135.0;
        for (int beat = 0; beat < 20; beat++) {
            double position = first + beat * 60_000.0 / bpm;
            double phase = PulseMath.musicPhase(position, first, bpm);
            assertEquals(0.5, phase, 1e-6, "beat " + beat);
            float[] onBeat = PulseMath.getScale(phase, 0.20, 1);
            assertEquals(1.2f, onBeat[0], 1e-4);
            assertEquals(0.8f, onBeat[1], 1e-4);
            float[] half = PulseMath.getScale(PulseMath.musicPhase(position + 30_000.0 / bpm, first, bpm), 0.20, 1);
            assertEquals(0.8f, half[0], 1e-4);
            assertEquals(1.2f, half[1], 1e-4);
        }
    }

    @Test
    void variableTempoMapPeaksOnEachBeat() {
        int[] beats = {370, 870, 1320, 1720};
        for (int beat : beats) {
            assertEquals(0.5, PulseMath.beatMapPhase(beats, beat), 1e-6);
        }
        assertEquals(0.0, PulseMath.beatMapPhase(beats, 620), 1e-6);
        assertEquals(0.0, PulseMath.beatMapPhase(beats, 1095), 1e-6);
        assertEquals(0.5, PulseMath.beatMapPhase(beats, -130), 1e-6);
    }

    @Test
    void peakAtBeatPlusAudioDelay() {
        PlaybackClock clock = new PlaybackClock(10_000L, 0);
        int delay = 80;
        int beatMs = 500;
        int playhead = clock.playheadMs(10_000L + beatMs + delay, delay);
        assertEquals(beatMs, playhead);
        float[] scale = PulseMath.getScale(PulseMath.beatMapPhase(BEATS_120, playhead), 0.20, 1);
        assertEquals(1.2f, scale[0], 1e-4);
        assertEquals(0.8f, scale[1], 1e-4);
    }

    @Test
    void phasePrefersBeatMapThenBpmGrid() {
        assertEquals(0.5, PulseMath.phase(BEATS_120, 0, 90), 1e-6);
        assertEquals(0.5, PulseMath.phase(new int[] {200}, 200, 120), 1e-6);
        assertEquals(0.5, PulseMath.phase(new int[0], 0, 120), 1e-6);
    }

    @Test
    void opposingAxesAndPeriodicity() {
        for (int easing = 0; easing < 4; easing++) {
            for (int i = 0; i <= 100; i++) {
                double p = i / 100.0;
                float[] scale = PulseMath.getScale(p, 0.20, easing);
                assertEquals(2.0, scale[0] + scale[1], 1e-4);
                assertTrue(scale[0] >= 0.8f - 1e-5f && scale[0] <= 1.2f + 1e-5f);
                float[] next = PulseMath.getScale(p + 1.0, 0.20, easing);
                assertEquals(scale[0], next[0], 1e-5);
                assertEquals(scale[1], next[1], 1e-5);
            }
        }
    }

    @Test
    void distanceVolumeFallsOff() {
        assertEquals(1f, PulseMath.distanceVolume(0, 24), 1e-5);
        assertEquals(0.5f, PulseMath.distanceVolume(12, 24), 1e-5);
        assertEquals(0f, PulseMath.distanceVolume(24, 24), 1e-5);
    }
}
