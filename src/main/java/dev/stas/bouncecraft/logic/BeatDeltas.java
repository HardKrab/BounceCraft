package dev.stas.bouncecraft.logic;

public final class BeatDeltas {
    private BeatDeltas() {
    }

    public static int[] toDeltas(int[] beatsMs) {
        if (beatsMs.length == 0) {
            return new int[0];
        }
        int[] deltas = new int[beatsMs.length];
        deltas[0] = beatsMs[0];
        for (int i = 1; i < beatsMs.length; i++) {
            deltas[i] = beatsMs[i] - beatsMs[i - 1];
        }
        return deltas;
    }

    public static int[] fromDeltas(int[] deltas) {
        if (deltas.length == 0) {
            return new int[0];
        }
        int[] beats = new int[deltas.length];
        int acc = 0;
        for (int i = 0; i < deltas.length; i++) {
            acc += deltas[i];
            beats[i] = acc;
        }
        return beats;
    }
}
