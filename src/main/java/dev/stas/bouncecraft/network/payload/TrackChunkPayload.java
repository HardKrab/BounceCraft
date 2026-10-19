package dev.stas.bouncecraft.network.payload;

import dev.stas.bouncecraft.BounceCraftConstants;
import dev.stas.bouncecraft.network.NetworkCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TrackChunkPayload(byte[] sha256, int index, byte[] data) implements CustomPacketPayload {
    public static final Type<TrackChunkPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BounceCraftConstants.MOD_ID, "track_chunk"));

    public static final StreamCodec<ByteBuf, TrackChunkPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                NetworkCodecs.SHA256.encode(buf, p.sha256);
                buf.writeInt(p.index);
                NetworkCodecs.CHUNK.encode(buf, p.data);
            },
            buf -> new TrackChunkPayload(
                    NetworkCodecs.SHA256.decode(buf),
                    buf.readInt(),
                    NetworkCodecs.CHUNK.decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
