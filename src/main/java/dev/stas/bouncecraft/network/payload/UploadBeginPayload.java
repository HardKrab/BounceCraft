package dev.stas.bouncecraft.network.payload;

import dev.stas.bouncecraft.BounceCraftConstants;
import dev.stas.bouncecraft.network.NetworkCodecs;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UploadBeginPayload(
        UUID trackId,
        byte[] sha256,
        int totalBytes,
        String format,
        String title,
        int durationMs,
        int[] beatsMs,
        float bpm
) implements CustomPacketPayload {
    public static final Type<UploadBeginPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BounceCraftConstants.MOD_ID, "upload_begin"));

    public static final StreamCodec<ByteBuf, UploadBeginPayload> STREAM_CODEC = StreamCodec.of(
            UploadBeginPayload::encode,
            UploadBeginPayload::decode
    );

    private static void encode(ByteBuf buf, UploadBeginPayload p) {
        NetworkCodecs.UUID_CODEC.encode(buf, p.trackId);
        NetworkCodecs.SHA256.encode(buf, p.sha256);
        buf.writeInt(p.totalBytes);
        ByteBufCodecs.stringUtf8(16).encode(buf, p.format);
        ByteBufCodecs.stringUtf8(BounceCraftConstants.TITLE_MAX).encode(buf, p.title);
        buf.writeInt(p.durationMs);
        NetworkCodecs.BEATS.encode(buf, p.beatsMs);
        buf.writeFloat(p.bpm);
    }

    private static UploadBeginPayload decode(ByteBuf buf) {
        return new UploadBeginPayload(
                NetworkCodecs.UUID_CODEC.decode(buf),
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
