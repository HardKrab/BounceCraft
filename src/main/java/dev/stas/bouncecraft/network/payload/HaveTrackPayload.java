package dev.stas.bouncecraft.network.payload;

import dev.stas.bouncecraft.BounceCraftConstants;
import dev.stas.bouncecraft.network.NetworkCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HaveTrackPayload(byte[] sha256, boolean present) implements CustomPacketPayload {
    public static final Type<HaveTrackPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BounceCraftConstants.MOD_ID, "have_track"));

    public static final StreamCodec<ByteBuf, HaveTrackPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                NetworkCodecs.SHA256.encode(buf, p.sha256);
                buf.writeBoolean(p.present);
            },
            buf -> new HaveTrackPayload(NetworkCodecs.SHA256.decode(buf), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
