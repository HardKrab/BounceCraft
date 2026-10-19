package dev.stas.bouncecraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

public final class BounceHud {
    private BounceHud() {
    }

    @SubscribeEvent
    public static void onRender(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) {
            return;
        }
        String text = ClientPlayback.status;
        if (text == null || text.isBlank()) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        boolean progress = ClientPlayback.uploadProgress >= 0f && ClientPlayback.uploadProgress < 1f;
        int right = graphics.guiWidth() - HudLayout.MARGIN;
        int y = HudLayout.overlayY(mc.isDemo(), hasBeneficialIcons(mc), hasHarmfulIcons(mc));
        int textX = HudLayout.overlayX(graphics.guiWidth(), mc.font.width(text));
        graphics.drawString(mc.font, text, textX, y, 0xFFFFFF, true);
        if (progress) {
            int width = 120;
            int barX = right - width;
            int filled = (int) (width * ClientPlayback.uploadProgress);
            graphics.fill(barX, y + 12, barX + width, y + 16, 0x80000000);
            graphics.fill(barX, y + 12, barX + filled, y + 16, 0xFF55FF55);
        }
    }

    private static boolean hasBeneficialIcons(Minecraft mc) {
        return hasEffectIcons(mc, true);
    }

    private static boolean hasHarmfulIcons(Minecraft mc) {
        return hasEffectIcons(mc, false);
    }

    private static boolean hasEffectIcons(Minecraft mc, boolean beneficial) {
        for (MobEffectInstance effect : mc.player.getActiveEffects()) {
            if (!effect.showIcon()) {
                continue;
            }
            if (!IClientMobEffectExtensions.of(effect).isVisibleInGui(effect)) {
                continue;
            }
            if (effect.getEffect().value().isBeneficial() == beneficial) {
                return true;
            }
        }
        return false;
    }
}
