package dev.stas.bouncecraft.client;

import dev.stas.bouncecraft.BounceCraftConstants;
import dev.stas.bouncecraft.network.ClientPacketHooks;
import dev.stas.bouncecraft.network.payload.HaveTrackPayload;
import dev.stas.bouncecraft.network.payload.HaveTrackQueryPayload;
import dev.stas.bouncecraft.network.payload.PlayPayload;
import dev.stas.bouncecraft.network.payload.StopPayload;
import dev.stas.bouncecraft.network.payload.TrackBeginPayload;
import dev.stas.bouncecraft.network.payload.TrackChunkPayload;
import dev.stas.bouncecraft.network.payload.UploadResultPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClientNetwork {
    private ClientNetwork() {
    }

    public static void install() {
        ClientPacketHooks.uploadResult = (payload, ctx) -> handleUploadResult(payload);
        ClientPacketHooks.haveTrackQuery = (payload, ctx) -> handleHaveQuery(payload);
        ClientPacketHooks.trackBegin = (payload, ctx) -> handleTrackBegin(payload);
        ClientPacketHooks.trackChunk = (payload, ctx) -> handleTrackChunk(payload);
        ClientPacketHooks.play = (payload, ctx) -> ClientPlayback.handlePlay(payload);
        ClientPacketHooks.stop = (payload, ctx) -> ClientPlayback.handleStop(payload);
    }

    private static void handleUploadResult(UploadResultPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (payload.ok()) {
            ClientPlayback.uploadProgress = -1f;
            ClientPlayback.status = Component.translatable("hud.bouncecraft.upload_ok").getString();
        } else {
            ClientPlayback.uploadProgress = -1f;
            ClientPlayback.status = Component.translatable("hud.bouncecraft.upload_fail", payload.reason()).getString();
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.translatable("hud.bouncecraft.upload_fail", payload.reason()), true);
            }
        }
        UploadClient.onResult(payload);
    }

    private static void handleHaveQuery(HaveTrackQueryPayload payload) {
        PacketDistributor.sendToServer(new HaveTrackPayload(payload.sha256(), TrackCache.hasComplete(payload.sha256())));
    }

    private static void handleTrackBegin(TrackBeginPayload payload) {
        CachedTrack existing = TrackCache.get(payload.sha256());
        if (existing != null) {
            existing.title = payload.title();
            existing.durationMs = payload.durationMs();
            existing.beatsMs = payload.beatsMs();
            existing.bpm = payload.bpm();
            if (existing.complete) {
                return;
            }
        }
        TrackCache.putIncoming(new CachedTrack(
                payload.sha256(),
                payload.format(),
                payload.title(),
                payload.durationMs(),
                payload.beatsMs(),
                payload.bpm(),
                payload.totalBytes()
        ));
    }

    private static void handleTrackChunk(TrackChunkPayload payload) {
        CachedTrack track = TrackCache.get(payload.sha256());
        if (track == null || track.complete) {
            return;
        }
        track.putChunk(payload.index(), payload.data(), BounceCraftConstants.CHUNK_SIZE);
    }
}
