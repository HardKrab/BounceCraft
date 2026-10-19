package dev.stas.bouncecraft.network;

import dev.stas.bouncecraft.BounceCraftConstants;
import dev.stas.bouncecraft.logic.BeatDeltas;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class NetworkCodecs {
    public static final StreamCodec<ByteBuf, UUID> UUID_CODEC = StreamCodec.of(
            (buf, id) -> {
                buf.writeLong(id.getMostSignificantBits());
                buf.writeLong(id.getLeastSignificantBits());
            },
            buf -> new UUID(buf.readLong(), buf.readLong())
    );

    public static final StreamCodec<ByteBuf, byte[]> SHA256 = StreamCodec.of(
            (buf, sha) -> {
                if (sha.length != BounceCraftConstants.SHA256_BYTES) {
                    throw new IllegalArgumentException("sha256 length");
                }
                buf.writeBytes(sha);
            },
            buf -> {
                byte[] sha = new byte[BounceCraftConstants.SHA256_BYTES];
                buf.readBytes(sha);
                return sha;
            }
    );

    public static final StreamCodec<ByteBuf, byte[]> CHUNK = StreamCodec.of(
            (buf, data) -> {
                if (data.length > BounceCraftConstants.CHUNK_SIZE) {
                    throw new IllegalArgumentException("chunk too large");
                }
                ByteBufCodecs.VAR_INT.encode(buf, data.length);
                buf.writeBytes(data);
            },
            buf -> {
                int len = ByteBufCodecs.VAR_INT.decode(buf);
                if (len < 0 || len > BounceCraftConstants.CHUNK_SIZE) {
                    throw new IllegalArgumentException("chunk length");
                }
                byte[] data = new byte[len];
                buf.readBytes(data);
                return data;
            }
    );

    public static final StreamCodec<ByteBuf, int[]> BEATS = StreamCodec.of(
            (buf, beats) -> {
                if (beats.length > BounceCraftConstants.BEATS_MAX) {
                    throw new IllegalArgumentException("too many beats");
                }
                int[] deltas = BeatDeltas.toDeltas(beats);
                ByteBufCodecs.VAR_INT.encode(buf, deltas.length);
                for (int delta : deltas) {
                    ByteBufCodecs.VAR_INT.encode(buf, delta);
                }
            },
            buf -> {
                int n = ByteBufCodecs.VAR_INT.decode(buf);
                if (n < 0 || n > BounceCraftConstants.BEATS_MAX) {
                    throw new IllegalArgumentException("beats length");
                }
                int[] deltas = new int[n];
                for (int i = 0; i < n; i++) {
                    deltas[i] = ByteBufCodecs.VAR_INT.decode(buf);
                }
                return BeatDeltas.fromDeltas(deltas);
            }
    );

    private NetworkCodecs() {
    }
}
