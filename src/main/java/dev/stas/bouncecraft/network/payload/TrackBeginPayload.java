package dev.stas.bouncecraft.network.payload;

import dev.stas.bouncecraft.BounceCraftConstants;
import dev.stas.bouncecraft.network.NetworkCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TrackBeginPayload(
        byte[] sha256,
        int totalBytes,
        String format,
        String title,
        int durationMs,
        int[] beatsMs,
        float bpm
) implements CustomPacketPayload {
    public static final Type<TrackBeginPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BounceCraftConstants.MOD_ID, "track_begin"));

    public static final StreamCodec<ByteBuf, TrackBeginPayload> STREAM_CODEC = StreamCodec.of(
            TrackBeginPayload::encode,
            TrackBeginPayload::decode
    );

    private static void encode(ByteBuf buf, TrackBeginPayload p) {
        NetworkCodecs.SHA256.encode(buf, p.sha256);
        buf.writeInt(p.totalBytes);
        ByteBufCodecs.stringUtf8(16).encode(buf, p.format);
        ByteBufCodecs.stringUtf8(BounceCraftConstants.TITLE_MAX).encode(buf, p.title);
        buf.writeInt(p.durationMs);
        NetworkCodecs.BEATS.encode(buf, p.beatsMs);
        buf.writeFloat(p.bpm);
    }

    private static TrackBeginPayload decode(ByteBuf buf) {
        return new TrackBeginPayload(
                NetworkCodecs.SHA256.decode(buf),
                buf.readInt(),
                ByteBufCodecs.stringUtf8(16).decode(buf),
                ByteBufCodecs.stringUtf8(BounceCraftConstants.TITLE_MAX).decode(buf),
                buf.readInt(),
                NetworkCodecs.BEATS.decode(buf),
                buf.readFloat()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
