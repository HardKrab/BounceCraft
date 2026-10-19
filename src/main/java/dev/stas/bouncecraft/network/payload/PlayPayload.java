package dev.stas.bouncecraft.network.payload;

import dev.stas.bouncecraft.BounceCraftConstants;
import dev.stas.bouncecraft.network.NetworkCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PlayPayload(int sourceEntityId, byte[] sha256, long startGameTime, float radius) implements CustomPacketPayload {
    public static final Type<PlayPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BounceCraftConstants.MOD_ID, "play"));

    public static final StreamCodec<ByteBuf, PlayPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeInt(p.sourceEntityId);
                NetworkCodecs.SHA256.encode(buf, p.sha256);
                buf.writeLong(p.startGameTime);
                buf.writeFloat(p.radius);
            },
            buf -> new PlayPayload(buf.readInt(), NetworkCodecs.SHA256.decode(buf), buf.readLong(), buf.readFloat())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
