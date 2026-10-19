package dev.stas.bouncecraft.client;

import dev.stas.bouncecraft.config.BounceCraftClientConfig;
import dev.stas.bouncecraft.logic.Sha256;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TrackCache {
    private static final LinkedHashMap<String, CachedTrack> TRACKS = new LinkedHashMap<>(16, 0.75f, true);

    private TrackCache() {
    }

    public static CachedTrack get(byte[] sha) {
        return TRACKS.get(Sha256.hex(sha));
    }

    public static CachedTrack putComplete(CachedTrack track) {
        track.complete = true;
        track.received = track.bytes.length;
        TRACKS.put(Sha256.hex(track.sha256), track);
        evictIfNeeded();
        return track;
    }

    public static CachedTrack putIncoming(CachedTrack track) {
        TRACKS.put(Sha256.hex(track.sha256), track);
        evictIfNeeded();
        return track;
    }

    public static boolean hasComplete(byte[] sha) {
        CachedTrack track = get(sha);
        return track != null && track.complete;
    }

    private static void evictIfNeeded() {
        long max = BounceCraftClientConfig.CACHE_MAX_MB.getAsInt() * 1024L * 1024L;
        long used = 0;
        for (CachedTrack track : TRACKS.values()) {
            used += track.bytes.length;
        }
        Iterator<Map.Entry<String, CachedTrack>> it = TRACKS.entrySet().iterator();
        while (used > max && it.hasNext()) {
            CachedTrack evicted = it.next().getValue();
            it.remove();
            used -= evicted.bytes.length;
        }
    }
}
