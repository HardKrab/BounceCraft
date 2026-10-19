package dev.stas.bouncecraft.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlaybackClockTest {
    @Test
    void followsElapsedMillisNotBufferCursor() {
        PlaybackClock clock = new PlaybackClock(0L, 0);
        assertEquals(0, clock.playheadMs(0L, 0));
        assertEquals(250, clock.playheadMs(250L, 0));
        assertEquals(4000, clock.playheadMs(4000L, 0));
        assertEquals(4100, clock.playheadMs(4100L, 0));
    }

    @Test
    void appliesStartOffsetAndDelay() {
        PlaybackClock clock = new PlaybackClock(1000L, 2000);
        assertEquals(2000, clock.playheadMs(1000L, 0));
        assertEquals(2500, clock.playheadMs(1500L, 0));
        assertEquals(2420, clock.playheadMs(1500L, 80));
    }

    @Test
    void freezeOnPauseResumesFromSamePlayhead() {
        PlaybackClock clock = new PlaybackClock(1000L, 100);
        clock.pause(1500L);
        assertTrue(clock.paused());
        assertEquals(600, clock.playheadMs(9000L, 0));
        clock.pause(9200L);
        assertEquals(600, clock.playheadMs(9400L, 0));
        clock.resume(2000L);
        assertFalse(clock.paused());
        assertEquals(600, clock.playheadMs(2000L, 0));
        assertEquals(700, clock.playheadMs(2100L, 0));
    }
}
