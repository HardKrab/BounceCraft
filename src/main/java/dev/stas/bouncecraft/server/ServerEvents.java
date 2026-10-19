package dev.stas.bouncecraft.server;

import dev.stas.bouncecraft.BounceCraftConstants;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = BounceCraftConstants.MOD_ID)
public final class ServerEvents {
    private ServerEvents() {
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        BounceCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        SessionManager.get(event.getServer()).tick();
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SessionManager.get(player.server).stopByPlayer(player, "logout");
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SessionManager.get(player.server).stopByPlayer(player, "dimension");
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SessionManager.get(player.server).stopByPlayer(player, "death");
        }
    }

    @SubscribeEvent
    public static void onStopping(ServerStoppingEvent event) {
        SessionManager.clear(event.getServer());
    }
}
