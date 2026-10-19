package dev.stas.bouncecraft.config;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class BounceCraftServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue RADIUS = BUILDER
            .comment("Hearing and effect radius in blocks.")
            .defineInRange("radius", 24, 1, 128);

    public static final ModConfigSpec.IntValue MAX_TRACK_BYTES = BUILDER
            .comment("Maximum uploaded track size in bytes.")
            .defineInRange("maxTrackBytes", 10 * 1024 * 1024, 16 * 1024, 50 * 1024 * 1024);

    public static final ModConfigSpec.IntValue MAX_DURATION_SEC = BUILDER
            .comment("Maximum track duration in seconds.")
            .defineInRange("maxDurationSec", 600, 5, 3600);

    public static final ModConfigSpec.IntValue UPLOAD_COOLDOWN_SEC = BUILDER
            .comment("Cooldown between uploads for one player, in seconds.")
            .defineInRange("uploadCooldownSec", 30, 0, 3600);

    public static final ModConfigSpec.IntValue MAX_CONCURRENT_DJS = BUILDER
            .comment("Maximum number of players playing a track at once.")
            .defineInRange("maxConcurrentDJs", 8, 1, 64);

    public static final ModConfigSpec.IntValue MAX_UPLOADS_IN_FLIGHT = BUILDER
            .comment("Maximum concurrent uploads on the whole server.")
            .defineInRange("maxUploadsInFlight", 2, 1, 16);

    public static final ModConfigSpec.IntValue CHUNKS_PER_TICK_PER_PLAYER = BUILDER
            .comment("Maximum track chunks sent to one listener per server tick.")
            .defineInRange("chunksPerTickPerPlayer", 4, 1, 32);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ALLOWED_FORMATS = BUILDER
            .comment("Allowed upload formats.")
            .defineListAllowEmpty("allowedFormats", List.of("ogg", "mp3", "wav"), () -> "ogg", BounceCraftServerConfig::isFormat);

    public static final ModConfigSpec.IntValue REQUIRE_PERMISSION_LEVEL = BUILDER
            .comment("Vanilla permission level required to start a track (0 = everyone).")
            .defineInRange("requirePermissionLevel", 0, 0, 4);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private BounceCraftServerConfig() {
    }

    public static Set<String> allowedFormats() {
        return ALLOWED_FORMATS.get().stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private static boolean isFormat(Object value) {
        return value instanceof String s && (s.equalsIgnoreCase("ogg") || s.equalsIgnoreCase("mp3") || s.equalsIgnoreCase("wav"));
    }
}
