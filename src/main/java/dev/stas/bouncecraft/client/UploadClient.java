package dev.stas.bouncecraft.client;

import dev.stas.bouncecraft.BounceCraft;
import dev.stas.bouncecraft.BounceCraftConstants;
import dev.stas.bouncecraft.config.BounceCraftServerConfig;
import dev.stas.bouncecraft.logic.TrackChunker;
import dev.stas.bouncecraft.network.payload.StopRequestPayload;
import dev.stas.bouncecraft.network.payload.UploadBeginPayload;
import dev.stas.bouncecraft.network.payload.UploadChunkPayload;
import dev.stas.bouncecraft.network.payload.UploadResultPayload;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.List;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public final class UploadClient {
    private static UUID currentTrackId;
    private static final ArrayDeque<UploadChunkPayload> pendingChunks = new ArrayDeque<>();
    private static int totalChunks;
    private static int sentChunks;
    private static boolean preparing;

    private UploadClient() {
    }

    public static boolean isBusy() {
        return preparing || currentTrackId != null;
    }

    public static void requestStop() {
        PacketDistributor.sendToServer(new StopRequestPayload());
        ClientPlayback.stopLocal();
    }

    public static void startFile(Path file) {
        if (preparing) {
            return;
        }
        preparing = true;
        ClientPlayback.status = Component.translatable("hud.bouncecraft.preparing").getString();
        Util.ioPool().execute(() -> {
            try {
                int maxDuration = 600;
                try {
                    maxDuration = BounceCraftServerConfig.MAX_DURATION_SEC.getAsInt();
                } catch (Exception ignored) {
                    // Server config is world-scoped; fall back to the spec default while not in a world.
                }
                ClientAudio.PreparedTrack prepared = ClientAudio.prepare(file, maxDuration);
                Minecraft.getInstance().execute(() -> enqueueUpload(prepared));
            } catch (Exception e) {
                BounceCraft.LOGGER.warn("Failed to prepare {}", file, e);
                Minecraft.getInstance().execute(() -> {
                    preparing = false;
                    ClientPlayback.status = Component.translatable("hud.bouncecraft.prepare_fail").getString();
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null) {
                        mc.player.displayClientMessage(Component.translatable("hud.bouncecraft.prepare_fail"), true);
                    }
                });
            }
        });
    }

    private static void enqueueUpload(ClientAudio.PreparedTrack prepared) {
        preparing = false;
        CachedTrack cached = new CachedTrack(
                prepared.sha256(),
                prepared.format().id(),
                prepared.title(),
                prepared.durationMs(),
                prepared.beatsMs(),
                prepared.bpm(),
                prepared.rawBytes().length
        );
        System.arraycopy(prepared.rawBytes(), 0, cached.bytes, 0, prepared.rawBytes().length);
        cached.pcm16leMono = prepared.pcm16leMono();
        cached.sampleRate = prepared.sampleRate();
        TrackCache.putComplete(cached);

        UUID trackId = UUID.randomUUID();
        currentTrackId = trackId;
        pendingChunks.clear();
        List<byte[]> chunks = TrackChunker.split(prepared.rawBytes(), BounceCraftConstants.CHUNK_SIZE);
        totalChunks = chunks.size();
        sentChunks = 0;
        for (int i = 0; i < chunks.size(); i++) {
            pendingChunks.addLast(new UploadChunkPayload(trackId, i, chunks.get(i)));
        }
        PacketDistributor.sendToServer(new UploadBeginPayload(
                trackId,
                prepared.sha256(),
                prepared.rawBytes().length,
                prepared.format().id(),
                prepared.title(),
                prepared.durationMs(),
                prepared.beatsMs(),
                prepared.bpm()
        ));
        ClientPlayback.uploadProgress = 0f;
        ClientPlayback.status = Component.translatable("hud.bouncecraft.uploading", 0).getString();
    }

    public static void tick() {
        if (currentTrackId == null) {
            return;
        }
        int n = 0;
        while (n < 4 && !pendingChunks.isEmpty()) {
            PacketDistributor.sendToServer(pendingChunks.pollFirst());
            sentChunks++;
            n++;
        }
        if (totalChunks > 0) {
            ClientPlayback.uploadProgress = sentChunks / (float) totalChunks;
            ClientPlayback.status = Component.translatable(
                    "hud.bouncecraft.uploading",
                    (int) (ClientPlayback.uploadProgress * 100)
            ).getString();
        }
        if (pendingChunks.isEmpty() && sentChunks >= totalChunks) {
            ClientPlayback.uploadProgress = 1f;
        }
    }

    public static void onResult(UploadResultPayload payload) {
        if (currentTrackId != null && currentTrackId.equals(payload.trackId())) {
            currentTrackId = null;
            pendingChunks.clear();
        }
    }
}
