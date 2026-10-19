package dev.stas.bouncecraft.client;

import dev.stas.bouncecraft.config.BounceCraftClientConfig;
import dev.stas.bouncecraft.logic.PulseMath;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

public final class RenderPulse {
    private static final Set<Integer> PUSHED = new HashSet<>();

    private RenderPulse() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        PUSHED.remove(entity.getId());
        float[] scale = pulseScale(entity);
        if (scale == null) {
            return;
        }
        event.getPoseStack().pushPose();
        event.getPoseStack().scale(scale[0], scale[1], scale[2]);
        PUSHED.add(entity.getId());
    }

    @SubscribeEvent
    public static void onPost(RenderLivingEvent.Post<?, ?> event) {
        if (PUSHED.remove(event.getEntity().getId())) {
            event.getPoseStack().popPose();
        }
    }

    private static float[] pulseScale(LivingEntity entity) {
        if (!BounceCraftClientConfig.EFFECT_ENABLED.get()) {
            return null;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return null;
        }
        if (entity == mc.player && mc.options.getCameraType().isFirstPerson()) {
            return null;
        }
        BounceSoundInstance sound = ClientPlayback.sound();
        CachedTrack track = ClientPlayback.track();
        if (sound == null || track == null || track.beatsMs == null || track.beatsMs.length == 0) {
            return null;
        }
        Entity source = mc.level.getEntity(sound.entityId());
        if (source == null) {
            return null;
        }
        float radius = ClientPlayback.radius();
        if (mc.player.distanceTo(source) > radius) {
            return null;
        }
        if (entity.distanceTo(mc.player) > radius) {
            return null;
        }
        float listenerVolume = PulseMath.distanceVolume(mc.player.distanceTo(source), radius);
        if (listenerVolume <= 0.01f) {
            return null;
        }
        float t = PulseMath.timeSinceLastBeatSec(track.beatsMs, ClientPlayback.playheadMs());
        float env = PulseMath.envelope(t, PulseMath.DEFAULT_TAU_SEC);
        float amplitude = (float) BounceCraftClientConfig.EFFECT_STRENGTH.getAsDouble() * listenerVolume;
        if (amplitude <= 0.001f || env <= 0.01f) {
            return null;
        }
        return PulseMath.squashScale(env, amplitude);
    }
}
