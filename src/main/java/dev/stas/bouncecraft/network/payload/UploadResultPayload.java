package dev.stas.bouncecraft.network.payload;

import dev.stas.bouncecraft.BounceCraftConstants;
import dev.stas.bouncecraft.network.NetworkCodecs;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UploadResultPayload(UUID trackId, boolean ok, String reason) implements CustomPacketPayload {
    public static final Type<UploadResultPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BounceCraftConstants.MOD_ID, "upload_result"));

    public static final StreamCodec<ByteBuf, UploadResultPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                NetworkCodecs.UUID_CODEC.encode(buf, p.trackId);
                buf.writeBoolean(p.ok);
                ByteBufCodecs.stringUtf8(64).encode(buf, p.reason == null ? "" : p.reason);
            },
            buf -> new UploadResultPayload(
                    NetworkCodecs.UUID_CODEC.decode(buf),
                    buf.readBoolean(),
                    ByteBufCodecs.stringUtf8(64).decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
