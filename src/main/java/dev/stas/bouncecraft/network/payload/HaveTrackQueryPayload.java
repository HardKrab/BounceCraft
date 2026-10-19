package dev.stas.bouncecraft.network.payload;

import dev.stas.bouncecraft.BounceCraftConstants;
import dev.stas.bouncecraft.network.NetworkCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HaveTrackQueryPayload(byte[] sha256) implements CustomPacketPayload {
    public static final Type<HaveTrackQueryPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BounceCraftConstants.MOD_ID, "have_track_query"));

    public static final StreamCodec<ByteBuf, HaveTrackQueryPayload> STREAM_CODEC =
            NetworkCodecs.SHA256.map(HaveTrackQueryPayload::new, HaveTrackQueryPayload::sha256);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
