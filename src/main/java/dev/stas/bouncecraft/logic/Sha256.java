package dev.stas.bouncecraft.logic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class Sha256 {
    private Sha256() {
    }

    public static byte[] hash(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static boolean equal(byte[] a, byte[] b) {
        return MessageDigest.isEqual(a, b);
    }

    public static String hex(byte[] sha) {
        return HexFormat.of().formatHex(sha);
    }
}
