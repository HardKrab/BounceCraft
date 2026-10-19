package dev.stas.bouncecraft.logic;

import java.util.Locale;
import java.util.Set;

public final class SessionRules {
    private SessionRules() {
    }

    public static boolean inRange(double dx, double dy, double dz, double radius) {
        if (radius < 0) {
            return false;
        }
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    public static boolean cooldownElapsed(long lastUploadEpochMs, long nowEpochMs, int cooldownSec) {
        if (lastUploadEpochMs <= 0) {
            return true;
        }
        return nowEpochMs - lastUploadEpochMs >= cooldownSec * 1000L;
    }

    public static long cooldownRemainingMs(long lastUploadEpochMs, long nowEpochMs, int cooldownSec) {
        if (cooldownElapsed(lastUploadEpochMs, nowEpochMs, cooldownSec)) {
            return 0L;
        }
        return lastUploadEpochMs + cooldownSec * 1000L - nowEpochMs;
    }

    public static boolean canStartDj(boolean alreadyDj, int activeCount, int maxConcurrent) {
        return alreadyDj || activeCount < maxConcurrent;
    }

    public static boolean canStartUpload(int uploadsInFlight, int maxUploadsInFlight) {
        return uploadsInFlight < maxUploadsInFlight;
    }

    public static String validateBegin(
            int totalBytes,
            int maxTrackBytes,
            int durationMs,
            int maxDurationSec,
            String format,
            String title,
            int beatCount,
            int maxBeats,
            Set<String> allowedFormats
    ) {
        if (totalBytes <= 0 || totalBytes > maxTrackBytes) {
            return "invalid_size";
        }
        if (durationMs <= 0 || durationMs > maxDurationSec * 1000L) {
            return "invalid_duration";
        }
        if (format == null || !allowedFormats.contains(format.toLowerCase(Locale.ROOT))) {
            return "invalid_format";
        }
        if (title == null || title.isEmpty() || title.length() > 64) {
            return "invalid_title";
        }
        if (beatCount < 0 || beatCount > maxBeats) {
            return "invalid_beats";
        }
        return null;
    }

    public static String sanitizeTitle(String raw) {
        if (raw == null) {
            return "track";
        }
        StringBuilder out = new StringBuilder(64);
        for (int i = 0; i < raw.length() && out.length() < 64; i++) {
            char c = raw.charAt(i);
            if (c >= 32 && c != 127) {
                out.append(c);
            }
        }
        String trimmed = out.toString().trim();
        return trimmed.isEmpty() ? "track" : trimmed;
    }
}
