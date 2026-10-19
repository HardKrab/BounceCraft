package dev.stas.bouncecraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.stas.bouncecraft.config.BounceCraftClientConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

public final class ClientKeys {
    public static final String CATEGORY = "key.categories.bouncecraft";

    public static final Lazy<KeyMapping> TOGGLE_PLAY = Lazy.of(() -> new KeyMapping(
            "key.bouncecraft.toggle_play",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            CATEGORY
    ));

    public static final Lazy<KeyMapping> PICK = Lazy.of(() -> new KeyMapping(
            "key.bouncecraft.pick",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            CATEGORY
    ));

    public static final Lazy<KeyMapping> TOGGLE_EFFECT = Lazy.of(() -> new KeyMapping(
            "key.bouncecraft.toggle_effect",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY
    ));

    private ClientKeys() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_PLAY.get());
        event.register(PICK.get());
        event.register(TOGGLE_EFFECT.get());
    }

    public static void consumeClicks() {
        Minecraft mc = Minecraft.getInstance();
        while (TOGGLE_PLAY.get().consumeClick()) {
            if (ClientPlayback.isPlaying() || UploadClient.isBusy()) {
                UploadClient.requestStop();
            } else {
                FilePicker.open();
            }
        }
        while (PICK.get().consumeClick()) {
            FilePicker.open();
        }
        while (TOGGLE_EFFECT.get().consumeClick()) {
            boolean next = !BounceCraftClientConfig.EFFECT_ENABLED.get();
            BounceCraftClientConfig.EFFECT_ENABLED.set(next);
            BounceCraftClientConfig.EFFECT_ENABLED.save();
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.translatable(
                        next ? "message.bouncecraft.effect_on" : "message.bouncecraft.effect_off"
                ), true);
            }
        }
    }
}
