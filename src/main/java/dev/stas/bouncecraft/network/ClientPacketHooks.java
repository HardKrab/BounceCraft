package dev.stas.bouncecraft.network;

import dev.stas.bouncecraft.network.payload.HaveTrackQueryPayload;
import dev.stas.bouncecraft.network.payload.PlayPayload;
import dev.stas.bouncecraft.network.payload.StopPayload;
import dev.stas.bouncecraft.network.payload.TrackBeginPayload;
import dev.stas.bouncecraft.network.payload.TrackChunkPayload;
import dev.stas.bouncecraft.network.payload.UploadResultPayload;
import java.util.function.BiConsumer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientPacketHooks {
    public static BiConsumer<UploadResultPayload, IPayloadContext> uploadResult = (p, c) -> {};
    public static BiConsumer<HaveTrackQueryPayload, IPayloadContext> haveTrackQuery = (p, c) -> {};
    public static BiConsumer<TrackBeginPayload, IPayloadContext> trackBegin = (p, c) -> {};
    public static BiConsumer<TrackChunkPayload, IPayloadContext> trackChunk = (p, c) -> {};
    public static BiConsumer<PlayPayload, IPayloadContext> play = (p, c) -> {};
    public static BiConsumer<StopPayload, IPayloadContext> stop = (p, c) -> {};

    private ClientPacketHooks() {
    }
}
