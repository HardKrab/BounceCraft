package dev.stas.bouncecraft.client;

import dev.stas.bouncecraft.BounceCraftConstants;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public final class BounceSoundInstance extends AbstractTickableSoundInstance {
    private final int entityId;
    private final MemoryAudioStream stream;

    public BounceSoundInstance(int entityId, MemoryAudioStream stream) {
        super(
                SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(BounceCraftConstants.MOD_ID, "stream")),
                SoundSource.RECORDS,
                SoundInstance.createUnseededRandom()
        );
        this.entityId = entityId;
        this.stream = stream;
        this.looping = false;
        this.delay = 0;
        this.volume = 1.0f;
        this.pitch = 1.0f;
        this.attenuation = SoundInstance.Attenuation.LINEAR;
        this.relative = false;
        tick();
    }

    public MemoryAudioStream stream() {
        return stream;
    }

    public int entityId() {
        return entityId;
    }

    public void requestStop() {
        stop();
    }

    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            stop();
            return;
        }
        Entity entity = mc.level.getEntity(entityId);
        if (entity == null || stream.ended()) {
            stop();
            return;
        }
        this.x = entity.getX();
        this.y = entity.getEyeY();
        this.z = entity.getZ();
    }

    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary library, net.minecraft.client.resources.sounds.Sound sound, boolean looping) {
        return CompletableFuture.completedFuture(stream);
    }
}
