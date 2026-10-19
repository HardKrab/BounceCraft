package dev.stas.bouncecraft.network;

import dev.stas.bouncecraft.network.payload.HaveTrackPayload;
import dev.stas.bouncecraft.network.payload.HaveTrackQueryPayload;
import dev.stas.bouncecraft.network.payload.PlayPayload;
import dev.stas.bouncecraft.network.payload.StopPayload;
import dev.stas.bouncecraft.network.payload.StopRequestPayload;
import dev.stas.bouncecraft.network.payload.TrackBeginPayload;
import dev.stas.bouncecraft.network.payload.TrackChunkPayload;
import dev.stas.bouncecraft.network.payload.UploadBeginPayload;
import dev.stas.bouncecraft.network.payload.UploadChunkPayload;
import dev.stas.bouncecraft.network.payload.UploadResultPayload;
import dev.stas.bouncecraft.server.SessionManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class BounceCraftNetwork {
    private BounceCraftNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();

        registrar.playToServer(UploadBeginPayload.TYPE, UploadBeginPayload.STREAM_CODEC, (payload, ctx) -> {
            if (ctx.player() instanceof ServerPlayer player) {
                SessionManager.get(player.server).handleBegin(player, payload);
            }
        });
        registrar.playToServer(UploadChunkPayload.TYPE, UploadChunkPayload.STREAM_CODEC, (payload, ctx) -> {
            if (ctx.player() instanceof ServerPlayer player) {
                SessionManager.get(player.server).handleChunk(player, payload);
            }
        });
        registrar.playToServer(HaveTrackPayload.TYPE, HaveTrackPayload.STREAM_CODEC, (payload, ctx) -> {
            if (ctx.player() instanceof ServerPlayer player) {
                SessionManager.get(player.server).handleHaveTrack(player, payload.sha256(), payload.present());
            }
        });
        registrar.playToServer(StopRequestPayload.TYPE, StopRequestPayload.STREAM_CODEC, (payload, ctx) -> {
            if (ctx.player() instanceof ServerPlayer player) {
                SessionManager.get(player.server).handleStopRequest(player);
            }
        });

        registrar.playToClient(UploadResultPayload.TYPE, UploadResultPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ClientPacketHooks.uploadResult.accept(payload, ctx)));
        registrar.playToClient(HaveTrackQueryPayload.TYPE, HaveTrackQueryPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ClientPacketHooks.haveTrackQuery.accept(payload, ctx)));
        registrar.playToClient(TrackBeginPayload.TYPE, TrackBeginPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ClientPacketHooks.trackBegin.accept(payload, ctx)));
        registrar.playToClient(TrackChunkPayload.TYPE, TrackChunkPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ClientPacketHooks.trackChunk.accept(payload, ctx)));
        registrar.playToClient(PlayPayload.TYPE, PlayPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ClientPacketHooks.play.accept(payload, ctx)));
        registrar.playToClient(StopPayload.TYPE, StopPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ClientPacketHooks.stop.accept(payload, ctx)));
    }
}
