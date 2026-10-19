package dev.stas.bouncecraft.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class SessionRulesTest {
    @Test
    void rangeUsesInclusiveSphere() {
        assertTrue(SessionRules.inRange(0, 0, 0, 24));
        assertTrue(SessionRules.inRange(24, 0, 0, 24));
        assertFalse(SessionRules.inRange(24.1, 0, 0, 24));
        assertTrue(SessionRules.inRange(10, 10, 10, 18));
        assertFalse(SessionRules.inRange(20, 20, 20, 24));
    }

    @Test
    void cooldownElapsedAfterConfiguredDelay() {
        long start = 1_000_000L;
        assertTrue(SessionRules.cooldownElapsed(0, start, 30));
        assertFalse(SessionRules.cooldownElapsed(start, start + 29_000, 30));
        assertTrue(SessionRules.cooldownElapsed(start, start + 30_000, 30));
        assertEquals(1000L, SessionRules.cooldownRemainingMs(start, start + 29_000, 30));
    }

    @Test
    void concurrentDjAndUploadCaps() {
        assertTrue(SessionRules.canStartDj(true, 8, 8));
        assertTrue(SessionRules.canStartDj(false, 7, 8));
        assertFalse(SessionRules.canStartDj(false, 8, 8));
        assertTrue(SessionRules.canStartUpload(1, 2));
        assertFalse(SessionRules.canStartUpload(2, 2));
    }

    @Test
    void validateBeginRejectsOversizeAndBadFormat() {
        Set<String> allowed = Set.of("ogg", "mp3", "wav");
        assertNull(SessionRules.validateBegin(100, 10_000_000, 1000, 600, "ogg", "song", 16, 4000, allowed));
        assertEquals("invalid_size", SessionRules.validateBegin(0, 10, 1000, 600, "ogg", "song", 1, 4000, allowed));
        assertEquals("invalid_size", SessionRules.validateBegin(11, 10, 1000, 600, "ogg", "song", 1, 4000, allowed));
        assertEquals("invalid_duration", SessionRules.validateBegin(10, 10, 700_000, 600, "ogg", "song", 1, 4000, allowed));
        assertEquals("invalid_format", SessionRules.validateBegin(10, 10, 1000, 600, "flac", "song", 1, 4000, allowed));
        assertEquals("invalid_title", SessionRules.validateBegin(10, 10, 1000, 600, "ogg", "", 1, 4000, allowed));
        assertEquals("invalid_beats", SessionRules.validateBegin(10, 10, 1000, 600, "ogg", "song", 4001, 4000, allowed));
    }

    @Test
    void sanitizeTitleStripsControlsAndCapsLength() {
        assertEquals("track", SessionRules.sanitizeTitle("   "));
        assertEquals("hello", SessionRules.sanitizeTitle("he\nllo"));
        assertEquals(64, SessionRules.sanitizeTitle("x".repeat(80)).length());
    }
}
