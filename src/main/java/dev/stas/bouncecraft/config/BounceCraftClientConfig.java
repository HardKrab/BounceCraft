package dev.stas.bouncecraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class BounceCraftClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue EFFECT_STRENGTH = BUILDER
            .comment("Squash/stretch amplitude. 0.35 keeps volume while pulsing.")
            .defineInRange("effectStrength", 0.35, 0.0, 0.9);

    public static final ModConfigSpec.BooleanValue EFFECT_ENABLED = BUILDER
            .comment("Render the beat squash-and-stretch effect.")
            .define("effectEnabled", true);

    public static final ModConfigSpec.IntValue CACHE_MAX_MB = BUILDER
            .comment("Maximum decoded track cache on the client, in megabytes.")
            .defineInRange("cacheMaxMB", 200, 8, 2048);

    public static final ModConfigSpec.IntValue AUDIO_DELAY_MS = BUILDER
            .comment("Assumed OpenAL/stream buffer delay used to align the render pulse with heard audio.")
            .defineInRange("audioDelayMs", 250, 0, 2000);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private BounceCraftClientConfig() {
    }
}
