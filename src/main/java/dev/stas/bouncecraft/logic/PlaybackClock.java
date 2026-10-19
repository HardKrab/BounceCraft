package dev.stas.bouncecraft.logic;

/**
 * Visual playhead from wall-clock elapsed time, not the OpenAL write cursor.
 * Minecraft prebuffers ~4 seconds of {@code AudioStream} on play, so a byte
 * position jumps then stalls; ViceBounce uses the actual playback position.
 */
public final class PlaybackClock {
    private long startWallMs;
    private final int startOffsetMs;
    private boolean paused;
    private long frozenElapsedMs;

    public PlaybackClock(long startWallMs, int startOffsetMs) {
        this.startWallMs = startWallMs;
        this.startOffsetMs = Math.max(0, startOffsetMs);
    }

    public int playheadMs(long nowWallMs, int audioDelayMs) {
        long elapsed = paused ? frozenElapsedMs : nowWallMs - startWallMs;
        return startOffsetMs + (int) elapsed - audioDelayMs;
    }

    public void pause(long nowWallMs) {
        if (paused) {
            return;
        }
        frozenElapsedMs = nowWallMs - startWallMs;
        paused = true;
    }

    public void resume(long nowWallMs) {
        if (!paused) {
            return;
        }
        startWallMs = nowWallMs - frozenElapsedMs;
        paused = false;
    }

    public boolean paused() {
        return paused;
    }
}
