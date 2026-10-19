package dev.stas.bouncecraft.client;

public final class CachedTrack {
    public final byte[] sha256;
    public final String format;
    public String title;
    public int durationMs;
    public int[] beatsMs;
    public float bpm;
    public final byte[] bytes;
    public int received;
    public boolean complete;
    public byte[] pcm16leMono;
    public int sampleRate;

    public CachedTrack(
            byte[] sha256,
            String format,
            String title,
            int durationMs,
            int[] beatsMs,
            float bpm,
            int totalBytes
    ) {
        this.sha256 = sha256;
        this.format = format;
        this.title = title;
        this.durationMs = durationMs;
        this.beatsMs = beatsMs;
        this.bpm = bpm;
        this.bytes = new byte[totalBytes];
        this.received = 0;
        this.complete = totalBytes == 0;
    }

    public void putChunk(int index, byte[] data, int chunkSize) {
        int offset = index * chunkSize;
        if (offset < 0 || offset + data.length > bytes.length) {
            throw new IllegalArgumentException("chunk range");
        }
        System.arraycopy(data, 0, bytes, offset, data.length);
        received = Math.min(bytes.length, Math.max(received, offset + data.length));
        if (received >= bytes.length) {
            complete = true;
        }
    }
}
