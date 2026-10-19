package dev.stas.bouncecraft.audio;

public final class PcmUtil {
    private PcmUtil() {
    }

    public static float[] pcm16leToMonoFloat(byte[] pcm, int channels) {
        if (channels < 1) {
            throw new IllegalArgumentException("channels");
        }
        int frameCount = pcm.length / (2 * channels);
        float[] mono = new float[frameCount];
        int idx = 0;
        for (int i = 0; i < frameCount; i++) {
            float sum = 0f;
            for (int ch = 0; ch < channels; ch++) {
                int lo = pcm[idx] & 0xFF;
                int hi = pcm[idx + 1];
                idx += 2;
                short sample = (short) (lo | (hi << 8));
                sum += sample / 32768f;
            }
            mono[i] = sum / channels;
        }
        return mono;
    }

    public static byte[] monoFloatToPcm16le(float[] mono) {
        byte[] out = new byte[mono.length * 2];
        int idx = 0;
        for (float v : mono) {
            float clamped = Math.max(-1f, Math.min(1f, v));
            int sample = Math.round(clamped * 32767f);
            if (sample > 32767) {
                sample = 32767;
            } else if (sample < -32768) {
                sample = -32768;
            }
            out[idx] = (byte) (sample & 0xFF);
            out[idx + 1] = (byte) ((sample >> 8) & 0xFF);
            idx += 2;
        }
        return out;
    }

    public static float[] downsample(float[] samples, int fromRate, int toRate) {
        if (fromRate == toRate) {
            return samples.clone();
        }
        if (fromRate <= 0 || toRate <= 0) {
            throw new IllegalArgumentException("sample rates");
        }
        if (fromRate < toRate) {
            return linearResample(samples, fromRate, toRate);
        }
        double ratio = (double) fromRate / toRate;
        int outLen = (int) (samples.length / ratio);
        float[] out = new float[outLen];
        for (int i = 0; i < outLen; i++) {
            int start = (int) Math.floor(i * ratio);
            int end = (int) Math.floor((i + 1) * ratio);
            if (end <= start) {
                end = start + 1;
            }
            if (end > samples.length) {
                end = samples.length;
            }
            float sum = 0f;
            int n = 0;
            for (int j = start; j < end; j++) {
                sum += samples[j];
                n++;
            }
            out[i] = n == 0 ? 0f : sum / n;
        }
        return out;
    }

    private static float[] linearResample(float[] samples, int fromRate, int toRate) {
        if (samples.length == 0) {
            return new float[0];
        }
        double ratio = (double) fromRate / toRate;
        int outLen = (int) Math.ceil(samples.length / ratio);
        float[] out = new float[outLen];
        for (int i = 0; i < outLen; i++) {
            double src = i * ratio;
            int i0 = (int) Math.floor(src);
            int i1 = Math.min(i0 + 1, samples.length - 1);
            float t = (float) (src - i0);
            float s0 = samples[Math.min(i0, samples.length - 1)];
            out[i] = s0 + t * (samples[i1] - s0);
        }
        return out;
    }

    public static int durationMs(int sampleCount, int sampleRate) {
        if (sampleRate <= 0) {
            return 0;
        }
        return (int) Math.round(sampleCount * 1000.0 / sampleRate);
    }
}
