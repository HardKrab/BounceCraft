package dev.stas.bouncecraft;

public final class BounceCraftConstants {
    public static final String MOD_ID = "bouncecraft";
    public static final int CHUNK_SIZE = 30_000;
    public static final int SERVERBOUND_PAYLOAD_MAX = 32_767;
    public static final int SHA256_BYTES = 32;
    public static final int TITLE_MAX = 64;
    public static final int BEATS_MAX = 4000;
    public static final int START_DELAY_TICKS = 40;
    public static final int RANGE_TICK_INTERVAL = 10;
    public static final int DEFAULT_RADIUS = 24;
    public static final int DEFAULT_MAX_TRACK_BYTES = 10 * 1024 * 1024;
    public static final int DEFAULT_MAX_DURATION_SEC = 600;
    public static final int DEFAULT_UPLOAD_COOLDOWN_SEC = 30;
    public static final int DEFAULT_MAX_CONCURRENT_DJS = 8;
    public static final int DEFAULT_MAX_UPLOADS_IN_FLIGHT = 2;
    public static final int DEFAULT_CHUNKS_PER_TICK = 4;
    public static final int BEAT_SAMPLE_RATE = 22_050;
    public static final int HAVE_TRACK_TIMEOUT_TICKS = 20;

    private BounceCraftConstants() {
    }
}
