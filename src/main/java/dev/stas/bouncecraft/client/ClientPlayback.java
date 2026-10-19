package dev.stas.bouncecraft.client;

import dev.stas.bouncecraft.BounceCraft;
import dev.stas.bouncecraft.audio.Mp3Decoder;
import dev.stas.bouncecraft.audio.PcmUtil;
import dev.stas.bouncecraft.audio.WavDecoder;
import dev.stas.bouncecraft.config.BounceCraftClientConfig;
import dev.stas.bouncecraft.logic.AudioFormatId;
import dev.stas.bouncecraft.logic.PlaybackClock;
import dev.stas.bouncecraft.network.payload.PlayPayload;
import dev.stas.bouncecraft.network.payload.StopPayload;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ClientPlayback {
    private static BounceSoundInstance current;
    private static CachedTrack currentTrack;
    private static PlaybackClock clock;
    private static PlayPayload pending;
    private static final Set<String> decoding = ConcurrentHashMap.newKeySet();
    public static volatile float uploadProgress = -1f;
    public static volatile String status = "";

    private ClientPlayback() {
    }

    public static boolean isPlaying() {
        return current != null && !current.isStopped();
    }

    public static void handlePlay(PlayPayload payload) {
        pending = payload;
        tryStart();
    }

    public static void handleStop(StopPayload payload) {
        if (current != null && current.entityId() == payload.sourceEntityId()) {
            stopLocal();
        }
        if (pending != null && pending.sourceEntityId() == payload.sourceEntityId()) {
            pending = null;
        }
    }

    public static void stopLocal() {
        if (current != null) {
            current.requestStop();
            current = null;
        }
        currentTrack = null;
        clock = null;
        pending = null;
        status = "";
    }

    public static void clientTick() {
        if (current != null && current.isStopped()) {
            current = null;
            currentTrack = null;
            clock = null;
            status = "";
        }
        syncClockPause();
        tryStart();
    }

    public static int playheadMs() {
        if (clock == null) {
            return 0;
        }
        syncClockPause();
        return clock.playheadMs(Util.getMillis(), BounceCraftClientConfig.AUDIO_DELAY_MS.getAsInt());
    }

    public static float radius() {
        return pending != null ? pending.radius() : current == null ? 0f : 24f;
    }

    public static int sourceEntityId() {
        return current == null ? -1 : current.entityId();
    }

    public static CachedTrack track() {
        return currentTrack;
    }

    public static BounceSoundInstance sound() {
        return current;
    }

    private static void syncClockPause() {
        if (clock == null) {
            return;
        }
        long now = Util.getMillis();
        if (Minecraft.getInstance().isPaused()) {
            clock.pause(now);
        } else {
            clock.resume(now);
        }
    }

    private static void tryStart() {
        PlayPayload play = pending;
        Minecraft mc = Minecraft.getInstance();
        if (play == null || mc.level == null) {
            return;
        }
        CachedTrack cached = TrackCache.get(play.sha256());
        if (cached == null || !cached.complete) {
            status = Component.translatable("hud.bouncecraft.receiving").getString();
            return;
        }
        long now = mc.level.getGameTime();
        if (now < play.startGameTime()) {
            status = Component.translatable("hud.bouncecraft.waiting").getString();
            return;
        }
        if (cached.pcm16leMono == null) {
            decodeAsync(cached);
            status = Component.translatable("hud.bouncecraft.decoding").getString();
            return;
        }
        int offsetMs = (int) ((now - play.startGameTime()) * 50L);
        if (offsetMs < 0) {
            offsetMs = 0;
        }
        stopLocal();
        pending = null;
        MemoryAudioStream stream = new MemoryAudioStream(cached.pcm16leMono, cached.sampleRate, offsetMs);
        BounceSoundInstance instance = new BounceSoundInstance(play.sourceEntityId(), stream);
        current = instance;
        currentTrack = cached;
        clock = new PlaybackClock(Util.getMillis(), offsetMs);
        mc.getSoundManager().play(instance);
        status = Component.translatable("hud.bouncecraft.playing", cached.title, String.format("%.0f", cached.bpm)).getString();
        BounceCraft.LOGGER.info("Playing {} from entity {}", cached.title, play.sourceEntityId());
    }

    private static void decodeAsync(CachedTrack cached) {
        String key = java.util.HexFormat.of().formatHex(cached.sha256);
        if (!decoding.add(key)) {
            return;
        }
        Util.ioPool().execute(() -> {
            try {
                AudioFormatId format = AudioFormatId.parse(cached.format).orElseThrow();
                float[] mono;
                int sampleRate;
                switch (format) {
                    case WAV -> {
                        WavDecoder.Pcm pcm = WavDecoder.decode(cached.bytes, cached.durationMs / 1000 + 1);
                        mono = pcm.mono();
                        sampleRate = pcm.sampleRate();
                    }
                    case MP3 -> {
                        Mp3Decoder.Pcm pcm = Mp3Decoder.decode(cached.bytes, cached.durationMs / 1000 + 1);
                        mono = pcm.mono();
                        sampleRate = pcm.sampleRate();
                    }
                    case OGG -> {
                        OggDecoder.Pcm pcm = OggDecoder.decode(cached.bytes, cached.durationMs / 1000 + 1);
                        mono = pcm.mono();
                        sampleRate = pcm.sampleRate();
                    }
                    default -> throw new IllegalStateException("format");
                }
                cached.pcm16leMono = PcmUtil.monoFloatToPcm16le(mono);
                cached.sampleRate = sampleRate;
            } catch (Exception e) {
                BounceCraft.LOGGER.warn("Failed to decode track {}", cached.title, e);
            } finally {
                decoding.remove(key);
            }
        });
    }
}
