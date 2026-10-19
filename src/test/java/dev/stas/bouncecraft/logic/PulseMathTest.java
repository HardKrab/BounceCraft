package dev.stas.bouncecraft.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PulseMathTest {
    @Test
    void envelopePeaksOnBeatAndDecays() {
        assertEquals(1f, PulseMath.envelope(0f, 0.12f), 1e-5);
        assertTrue(PulseMath.envelope(0.12f, 0.12f) < 0.4f);
        assertTrue(PulseMath.envelope(1f, 0.12f) < 0.01f);
    }

    @Test
    void squashPreservesApproximateVolume() {
        float[] s = PulseMath.squashScale(1f, 0.35f);
        float volume = s[0] * s[1] * s[2];
        assertEquals(1f, volume, 0.02f);
        assertTrue(s[1] < 1f);
        assertTrue(s[0] > 1f);
    }

    @Test
    void playheadPicksLastBeat() {
        int[] beats = {0, 500, 1000};
        assertEquals(0f, PulseMath.timeSinceLastBeatSec(beats, 500), 1e-4);
        assertEquals(0.1f, PulseMath.timeSinceLastBeatSec(beats, 600), 1e-4);
    }
}
