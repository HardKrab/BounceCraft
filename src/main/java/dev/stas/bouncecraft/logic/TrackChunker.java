package dev.stas.bouncecraft.logic;

import java.util.ArrayList;
import java.util.List;

public final class TrackChunker {
    private TrackChunker() {
    }

    public static List<byte[]> split(byte[] data, int chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be positive");
        }
        if (data.length == 0) {
            return List.of();
        }
        List<byte[]> chunks = new ArrayList<>((data.length + chunkSize - 1) / chunkSize);
        int offset = 0;
        while (offset < data.length) {
            int len = Math.min(chunkSize, data.length - offset);
            byte[] chunk = new byte[len];
            System.arraycopy(data, offset, chunk, 0, len);
            chunks.add(chunk);
            offset += len;
        }
        return chunks;
    }

    public static byte[] assemble(List<byte[]> chunks, int totalBytes) {
        if (totalBytes < 0) {
            throw new IllegalArgumentException("totalBytes must be non-negative");
        }
        byte[] out = new byte[totalBytes];
        int offset = 0;
        for (byte[] chunk : chunks) {
            if (offset + chunk.length > totalBytes) {
                throw new IllegalArgumentException("chunks exceed totalBytes");
            }
            System.arraycopy(chunk, 0, out, offset, chunk.length);
            offset += chunk.length;
        }
        if (offset != totalBytes) {
            throw new IllegalArgumentException("chunks do not fill totalBytes: " + offset + " != " + totalBytes);
        }
        return out;
    }

    public static int chunkCount(int totalBytes, int chunkSize) {
        if (totalBytes == 0) {
            return 0;
        }
        return (totalBytes + chunkSize - 1) / chunkSize;
    }
}
