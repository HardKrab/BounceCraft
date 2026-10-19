package dev.stas.bouncecraft.network.payload;

import dev.stas.bouncecraft.BounceCraftConstants;
import dev.stas.bouncecraft.network.NetworkCodecs;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UploadChunkPayload(UUID trackId, int index, byte[] data) implements CustomPacketPayload {
    public static final Type<UploadChunkPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BounceCraftConstants.MOD_ID, "upload_chunk"));

    public static final StreamCodec<ByteBuf, UploadChunkPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                NetworkCodecs.UUID_CODEC.encode(buf, p.trackId);
                buf.writeInt(p.index);
                NetworkCodecs.CHUNK.encode(buf, p.data);
            },
            buf -> new UploadChunkPayload(
                    NetworkCodecs.UUID_CODEC.decode(buf),
                    buf.readInt(),
                    NetworkCodecs.CHUNK.decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
