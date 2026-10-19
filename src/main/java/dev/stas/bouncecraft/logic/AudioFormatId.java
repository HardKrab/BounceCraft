package dev.stas.bouncecraft.logic;

import java.util.Locale;
import java.util.Optional;

public enum AudioFormatId {
    OGG("ogg"),
    MP3("mp3"),
    WAV("wav");

    private final String id;

    AudioFormatId(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static Optional<AudioFormatId> fromFileName(String fileName) {
        if (fileName == null) {
            return Optional.empty();
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return Optional.empty();
        }
        return parse(fileName.substring(dot + 1));
    }

    public static Optional<AudioFormatId> parse(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String lower = raw.toLowerCase(Locale.ROOT).trim();
        for (AudioFormatId value : values()) {
            if (value.id.equals(lower)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
