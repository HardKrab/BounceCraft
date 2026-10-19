package dev.stas.bouncecraft.server;

import dev.stas.bouncecraft.BounceCraft;
import dev.stas.bouncecraft.BounceCraftConstants;
import dev.stas.bouncecraft.config.BounceCraftServerConfig;
import dev.stas.bouncecraft.logic.BeatGrid;
import dev.stas.bouncecraft.logic.SessionRules;
import dev.stas.bouncecraft.logic.Sha256;
import dev.stas.bouncecraft.logic.TrackChunker;
import dev.stas.bouncecraft.network.payload.HaveTrackQueryPayload;
import dev.stas.bouncecraft.network.payload.PlayPayload;
import dev.stas.bouncecraft.network.payload.StopPayload;
import dev.stas.bouncecraft.network.payload.TrackBeginPayload;
import dev.stas.bouncecraft.network.payload.TrackChunkPayload;
import dev.stas.bouncecraft.network.payload.UploadBeginPayload;
import dev.stas.bouncecraft.network.payload.UploadChunkPayload;
import dev.stas.bouncecraft.network.payload.UploadResultPayload;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SessionManager {
    private static SessionManager instance;

    private final MinecraftServer server;
    private final Map<UUID, Upload> uploads = new HashMap<>();
    private final Map<UUID, Session> sessionsByDj = new HashMap<>();
    private final Map<UUID, Long> lastUploadMs = new HashMap<>();
    private final Map<String, StoredTrack> tracks = new HashMap<>();
    private final Map<UUID, ArrayDeque<CustomPacketPayload>> outbound = new HashMap<>();
    private final Map<UUID, PendingHave> pendingHave = new HashMap<>();

    private SessionManager(MinecraftServer server) {
        this.server = server;
    }

    public static SessionManager get(MinecraftServer server) {
        if (instance == null || instance.server != server) {
            instance = new SessionManager(server);
        }
        return instance;
    }

    public static void clear(MinecraftServer server) {
        if (instance != null && instance.server == server) {
            instance = null;
        }
    }

    public void handleBegin(ServerPlayer player, UploadBeginPayload payload) {
        if (!player.hasPermissions(BounceCraftServerConfig.REQUIRE_PERMISSION_LEVEL.getAsInt())) {
            reply(player, payload.trackId(), false, "no_permission");
            return;
        }
        long now = System.currentTimeMillis();
        Long last = lastUploadMs.get(player.getUUID());
        if (!SessionRules.cooldownElapsed(last == null ? 0L : last, now, BounceCraftServerConfig.UPLOAD_COOLDOWN_SEC.getAsInt())) {
            reply(player, payload.trackId(), false, "cooldown");
            return;
        }
        if (!SessionRules.canStartUpload(uploads.size(), BounceCraftServerConfig.MAX_UPLOADS_IN_FLIGHT.getAsInt())) {
            reply(player, payload.trackId(), false, "busy");
            return;
        }
        if (!SessionRules.canStartDj(sessionsByDj.containsKey(player.getUUID()), sessionsByDj.size(), BounceCraftServerConfig.MAX_CONCURRENT_DJS.getAsInt())) {
            reply(player, payload.trackId(), false, "too_many_djs");
            return;
        }
        String error = SessionRules.validateBegin(
                payload.totalBytes(),
                BounceCraftServerConfig.MAX_TRACK_BYTES.getAsInt(),
                payload.durationMs(),
                BounceCraftServerConfig.MAX_DURATION_SEC.getAsInt(),
                payload.format(),
                payload.title(),
                payload.beatsMs().length,
                BounceCraftConstants.BEATS_MAX,
                BounceCraftServerConfig.allowedFormats()
        );
        if (error != null) {
            reply(player, payload.trackId(), false, error);
            return;
        }
        if (uploads.containsKey(player.getUUID())) {
            reply(player, payload.trackId(), false, "already_uploading");
            return;
        }
        uploads.put(player.getUUID(), new Upload(
                player.getUUID(),
                payload.trackId(),
                payload.sha256(),
                payload.totalBytes(),
                payload.format(),
                SessionRules.sanitizeTitle(payload.title()),
                payload.durationMs(),
                payload.beatsMs(),
                payload.bpm(),
                new byte[payload.totalBytes()],
                0,
                0
        ));
    }

    public void handleChunk(ServerPlayer player, UploadChunkPayload payload) {
        Upload upload = uploads.get(player.getUUID());
        if (upload == null || !upload.trackId.equals(payload.trackId())) {
            reply(player, payload.trackId(), false, "no_upload");
            return;
        }
        if (payload.index() != upload.nextIndex) {
            uploads.remove(player.getUUID());
            reply(player, payload.trackId(), false, "bad_index");
            return;
        }
        int remaining = upload.totalBytes - upload.received;
        if (payload.data().length > remaining) {
            uploads.remove(player.getUUID());
            reply(player, payload.trackId(), false, "overflow");
            return;
        }
        System.arraycopy(payload.data(), 0, upload.buffer, upload.received, payload.data().length);
        upload.received += payload.data().length;
        upload.nextIndex++;
        if (upload.received < upload.totalBytes) {
            return;
        }
        uploads.remove(player.getUUID());
        if (!Sha256.equal(Sha256.hash(upload.buffer), upload.sha256)) {
            reply(player, upload.trackId, false, "sha_mismatch");
            return;
        }
        StoredTrack track = new StoredTrack(
                upload.sha256,
                upload.buffer,
                upload.format,
                upload.title,
                upload.durationMs,
                upload.beatsMs,
                upload.bpm
        );
        tracks.put(Sha256.hex(upload.sha256), track);
        lastUploadMs.put(player.getUUID(), System.currentTimeMillis());
        startSession(player, track);
        reply(player, upload.trackId, true, "ok");
    }

    public void handleHaveTrack(ServerPlayer player, byte[] sha, boolean present) {
        PendingHave pending = pendingHave.get(player.getUUID());
        if (pending == null || !Sha256.equal(pending.sha256, sha)) {
            return;
        }
        pendingHave.remove(player.getUUID());
        Session session = sessionsByDj.get(pending.djId);
        if (session == null) {
            return;
        }
        queueTrack(player, session, !present);
    }

    public void handleStopRequest(ServerPlayer player) {
        stopByPlayer(player, "stopped");
    }

    public boolean setBpm(ServerPlayer player, float bpm) {
        Session session = sessionsByDj.get(player.getUUID());
        if (session == null) {
            return false;
        }
        int[] beats = BeatGrid.retimed(session.track.beatsMs(), bpm, session.track.durationMs());
        StoredTrack updated = new StoredTrack(
                session.track.sha256(),
                session.track.bytes(),
                session.track.format(),
                session.track.title(),
                session.track.durationMs(),
                beats,
                bpm
        );
        tracks.put(Sha256.hex(updated.sha256()), updated);
        session.track = updated;
        for (UUID listener : session.listeners) {
            ServerPlayer target = server.getPlayerList().getPlayer(listener);
            if (target == null) {
                continue;
            }
            send(target, new TrackBeginPayload(
                    updated.sha256(),
                    updated.bytes().length,
                    updated.format(),
                    updated.title(),
                    updated.durationMs(),
                    updated.beatsMs(),
                    updated.bpm()
            ));
        }
        return true;
    }

    public void stopByPlayer(ServerPlayer player, String reason) {
        Session session = sessionsByDj.remove(player.getUUID());
        uploads.remove(player.getUUID());
        if (session == null) {
            return;
        }
        BounceCraft.LOGGER.info("Stopping BounceCraft session for {} ({})", player.getGameProfile().getName(), reason);
        StopPayload stop = new StopPayload(session.sourceEntityId);
        for (UUID listener : session.listeners) {
            ServerPlayer target = server.getPlayerList().getPlayer(listener);
            if (target != null) {
                send(target, stop);
            }
        }
    }

    public void tick() {
        tickHaveTimeouts();
        flushOutbound();
        if (server.getTickCount() % BounceCraftConstants.RANGE_TICK_INTERVAL != 0) {
            return;
        }
        for (Session session : List.copyOf(sessionsByDj.values())) {
            ServerPlayer dj = server.getPlayerList().getPlayer(session.djId);
            if (dj == null) {
                sessionsByDj.remove(session.djId);
                continue;
            }
            double radius = BounceCraftServerConfig.RADIUS.getAsInt();
            Set<UUID> inRange = new HashSet<>();
            for (ServerPlayer listener : server.getPlayerList().getPlayers()) {
                if (listener.level() != dj.level()) {
                    continue;
                }
                if (!hasMod(listener)) {
                    continue;
                }
                double dx = listener.getX() - dj.getX();
                double dy = listener.getY() - dj.getY();
                double dz = listener.getZ() - dj.getZ();
                if (SessionRules.inRange(dx, dy, dz, radius)) {
                    inRange.add(listener.getUUID());
                }
            }
            for (UUID id : List.copyOf(session.listeners)) {
                if (!inRange.contains(id)) {
                    session.listeners.remove(id);
                    ServerPlayer left = server.getPlayerList().getPlayer(id);
                    if (left != null) {
                        send(left, new StopPayload(session.sourceEntityId));
                    }
                }
            }
            for (UUID id : inRange) {
                if (session.listeners.contains(id) || pendingHave.containsKey(id)) {
                    continue;
                }
                ServerPlayer listener = server.getPlayerList().getPlayer(id);
                if (listener == null) {
                    continue;
                }
                pendingHave.put(id, new PendingHave(session.djId, session.track.sha256(), BounceCraftConstants.HAVE_TRACK_TIMEOUT_TICKS));
                send(listener, new HaveTrackQueryPayload(session.track.sha256()));
            }
        }
    }

    private void startSession(ServerPlayer dj, StoredTrack track) {
        Session previous = sessionsByDj.remove(dj.getUUID());
        if (previous != null) {
            StopPayload stop = new StopPayload(previous.sourceEntityId);
            for (UUID listener : previous.listeners) {
                ServerPlayer target = server.getPlayerList().getPlayer(listener);
                if (target != null) {
                    send(target, stop);
                }
            }
        }
        long start = dj.level().getGameTime() + BounceCraftConstants.START_DELAY_TICKS;
        Session session = new Session(dj.getUUID(), dj.getId(), track, start);
        sessionsByDj.put(dj.getUUID(), session);
        BounceCraft.LOGGER.info("BounceCraft session started by {} ({}, {} ms)", dj.getGameProfile().getName(), track.title(), track.durationMs());
    }

    private void queueTrack(ServerPlayer listener, Session session, boolean sendBytes) {
        StoredTrack track = session.track;
        if (sendBytes) {
            enqueue(listener, new TrackBeginPayload(
                    track.sha256(),
                    track.bytes().length,
                    track.format(),
                    track.title(),
                    track.durationMs(),
                    track.beatsMs(),
                    track.bpm()
            ));
            List<byte[]> chunks = TrackChunker.split(track.bytes(), BounceCraftConstants.CHUNK_SIZE);
            for (int i = 0; i < chunks.size(); i++) {
                enqueue(listener, new TrackChunkPayload(track.sha256(), i, chunks.get(i)));
            }
        } else {
            enqueue(listener, new TrackBeginPayload(
                    track.sha256(),
                    track.bytes().length,
                    track.format(),
                    track.title(),
                    track.durationMs(),
                    track.beatsMs(),
                    track.bpm()
            ));
        }
        enqueue(listener, new PlayPayload(
                session.sourceEntityId,
                track.sha256(),
                session.startGameTime,
                BounceCraftServerConfig.RADIUS.getAsInt()
        ));
        session.listeners.add(listener.getUUID());
    }

    private void tickHaveTimeouts() {
        Iterator<Map.Entry<UUID, PendingHave>> it = pendingHave.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, PendingHave> entry = it.next();
            PendingHave pending = entry.getValue();
            pending.ticksLeft--;
            if (pending.ticksLeft > 0) {
                continue;
            }
            it.remove();
            Session session = sessionsByDj.get(pending.djId);
            ServerPlayer listener = server.getPlayerList().getPlayer(entry.getKey());
            if (session == null || listener == null) {
                continue;
            }
            queueTrack(listener, session, true);
        }
    }

    private void flushOutbound() {
        int cap = BounceCraftServerConfig.CHUNKS_PER_TICK_PER_PLAYER.getAsInt();
        for (Map.Entry<UUID, ArrayDeque<CustomPacketPayload>> entry : List.copyOf(outbound.entrySet())) {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            ArrayDeque<CustomPacketPayload> queue = entry.getValue();
            if (player == null) {
                outbound.remove(entry.getKey());
                continue;
            }
            int sent = 0;
            while (sent < cap && !queue.isEmpty()) {
                send(player, queue.pollFirst());
                sent++;
            }
            if (queue.isEmpty()) {
                outbound.remove(entry.getKey());
            }
        }
    }

    private void enqueue(ServerPlayer player, CustomPacketPayload payload) {
        outbound.computeIfAbsent(player.getUUID(), id -> new ArrayDeque<>()).addLast(payload);
    }

    private static void reply(ServerPlayer player, UUID trackId, boolean ok, String reason) {
        send(player, new UploadResultPayload(trackId, ok, reason == null ? "" : reason));
    }

    private static void send(ServerPlayer player, CustomPacketPayload payload) {
        if (!hasMod(player)) {
            return;
        }
        PacketDistributor.sendToPlayer(player, payload);
    }

    private static boolean hasMod(ServerPlayer player) {
        return player.connection.hasChannel(PlayPayload.TYPE);
    }

    private static final class Upload {
        private final UUID playerId;
        private final UUID trackId;
        private final byte[] sha256;
        private final int totalBytes;
        private final String format;
        private final String title;
        private final int durationMs;
        private final int[] beatsMs;
        private final float bpm;
        private final byte[] buffer;
        private int nextIndex;
        private int received;

        private Upload(
                UUID playerId,
                UUID trackId,
                byte[] sha256,
                int totalBytes,
                String format,
                String title,
                int durationMs,
                int[] beatsMs,
                float bpm,
                byte[] buffer,
                int nextIndex,
                int received
        ) {
            this.playerId = playerId;
            this.trackId = trackId;
            this.sha256 = sha256;
            this.totalBytes = totalBytes;
            this.format = format;
            this.title = title;
            this.durationMs = durationMs;
            this.beatsMs = beatsMs;
            this.bpm = bpm;
            this.buffer = buffer;
            this.nextIndex = nextIndex;
            this.received = received;
        }
    }

    private static final class Session {
        private final UUID djId;
        private final int sourceEntityId;
        private StoredTrack track;
        private final long startGameTime;
        private final Set<UUID> listeners = new HashSet<>();

        private Session(UUID djId, int sourceEntityId, StoredTrack track, long startGameTime) {
            this.djId = djId;
            this.sourceEntityId = sourceEntityId;
            this.track = track;
            this.startGameTime = startGameTime;
        }
    }

    private static final class PendingHave {
        private final UUID djId;
        private final byte[] sha256;
        private int ticksLeft;

        private PendingHave(UUID djId, byte[] sha256, int ticksLeft) {
            this.djId = djId;
            this.sha256 = sha256;
            this.ticksLeft = ticksLeft;
        }
    }
}
