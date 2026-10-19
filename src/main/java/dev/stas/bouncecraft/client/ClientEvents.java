package dev.stas.bouncecraft.client;

import dev.stas.bouncecraft.BounceCraftConstants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = BounceCraftConstants.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        ClientKeys.consumeClicks();
        UploadClient.tick();
        ClientPlayback.clientTick();
    }
}
