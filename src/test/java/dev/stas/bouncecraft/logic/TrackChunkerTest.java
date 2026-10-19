package dev.stas.bouncecraft.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TrackChunkerTest {
    @Test
    void splitAndAssembleRoundTrip() {
        byte[] data = new byte[75_000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i * 31);
        }
        List<byte[]> chunks = TrackChunker.split(data, 30_000);
        assertEquals(3, chunks.size());
        assertEquals(30_000, chunks.get(0).length);
        assertEquals(30_000, chunks.get(1).length);
        assertEquals(15_000, chunks.get(2).length);
        for (byte[] chunk : chunks) {
            assertTrue(chunk.length <= 30_000);
            assertTrue(chunk.length < 32_767, "chunk must fit in vanilla serverbound payload");
        }
        byte[] restored = TrackChunker.assemble(chunks, data.length);
        assertArrayEquals(data, restored);
    }

    @Test
    void emptySplitIsEmpty() {
        assertTrue(TrackChunker.split(new byte[0], 30_000).isEmpty());
        assertEquals(0, TrackChunker.chunkCount(0, 30_000));
    }

    @Test
    void assembleRejectsSizeMismatch() {
        List<byte[]> chunks = TrackChunker.split(new byte[] {1, 2, 3}, 2);
        assertThrows(IllegalArgumentException.class, () -> TrackChunker.assemble(chunks, 2));
    }

    @Test
    void beatDeltasRoundTrip() {
        int[] beats = {12, 512, 1012, 1510};
        assertArrayEquals(beats, BeatDeltas.fromDeltas(BeatDeltas.toDeltas(beats)));
    }
}
