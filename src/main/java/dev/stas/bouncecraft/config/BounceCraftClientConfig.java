package dev.stas.bouncecraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class BounceCraftClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue EFFECT_STRENGTH = BUILDER
            .comment("Squash/stretch amplitude (ViceBounce-style opposing axes). Clamped to 0.75 at apply time.")
            .defineInRange("effectStrength", 0.35, 0.0, 0.9);

    public static final ModConfigSpec.IntValue EFFECT_EASING = BUILDER
            .comment("Pulse curve: 0 linear, 1 sine (ViceBounce default), 2 quadratic, 3 bounce.")
            .defineInRange("effectEasing", 1, 0, 3);

    public static final ModConfigSpec.BooleanValue EFFECT_ENABLED = BUILDER
            .comment("Render the beat squash-and-stretch effect.")
            .define("effectEnabled", true);

    public static final ModConfigSpec.IntValue CACHE_MAX_MB = BUILDER
            .comment("Maximum decoded track cache on the client, in megabytes.")
            .defineInRange("cacheMaxMB", 200, 8, 2048);

    public static final ModConfigSpec.IntValue AUDIO_DELAY_MS = BUILDER
            .comment("Shift the visual playhead later so the squash peak matches heard audio. 0 if the speakers are in sync.")
            .defineInRange("audioDelayMs", 0, 0, 2000);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private BounceCraftClientConfig() {
    }
}
