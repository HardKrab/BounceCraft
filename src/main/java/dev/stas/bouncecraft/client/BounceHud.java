package dev.stas.bouncecraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

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
        int x = 8;
        int y = 8;
        graphics.drawString(mc.font, text, x, y, 0xFFFFFF, true);
        if (ClientPlayback.uploadProgress >= 0f && ClientPlayback.uploadProgress < 1f) {
            int width = 120;
            int filled = (int) (width * ClientPlayback.uploadProgress);
            graphics.fill(x, y + 12, x + width, y + 16, 0x80000000);
            graphics.fill(x, y + 12, x + filled, y + 16, 0xFF55FF55);
        }
    }
}
