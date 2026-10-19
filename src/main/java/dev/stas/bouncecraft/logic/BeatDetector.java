package dev.stas.bouncecraft.logic;

import dev.stas.bouncecraft.audio.PcmUtil;

public final class BeatDetector {
    public static final int TARGET_RATE = 22_050;
    public static final int WINDOW = 1024;
    public static final int HOP = 512;
    public static final float MIN_BPM = 70f;
    public static final float MAX_BPM = 180f;
    public static final float PREFERRED_BPM = 120f;

    private BeatDetector() {
    }

    public record Result(float bpm, int[] beatsMs) {
    }

    public static Result detect(float[] mono, int sampleRate) {
        if (mono.length == 0 || sampleRate <= 0) {
            return new Result(PREFERRED_BPM, new int[0]);
        }
        float[] samples = PcmUtil.downsample(mono, sampleRate, TARGET_RATE);
        float[] envelope = onsetEnvelope(samples);
        if (envelope.length < 8) {
            return new Result(PREFERRED_BPM, new int[0]);
        }
        float bpm = estimateBpm(envelope);
        int[] beats = trackBeats(envelope, bpm, samples.length);
        return new Result(bpm, beats);
    }

    static float[] onsetEnvelope(float[] samples) {
        int frames = 1 + Math.max(0, (samples.length - WINDOW) / HOP);
        if (frames < 2) {
            return new float[0];
        }
        float[] window = hann(WINDOW);
        float[] prevMag = new float[WINDOW / 2];
        float[] flux = new float[frames];
        float[] re = new float[WINDOW];
        float[] im = new float[WINDOW];
        for (int f = 0; f < frames; f++) {
            int start = f * HOP;
            for (int i = 0; i < WINDOW; i++) {
                float s = start + i < samples.length ? samples[start + i] : 0f;
                re[i] = s * window[i];
                im[i] = 0f;
            }
            fft(re, im);
            float sum = 0f;
            for (int k = 0; k < WINDOW / 2; k++) {
                float mag = (float) Math.log1p(Math.hypot(re[k], im[k]));
                float diff = mag - prevMag[k];
                if (diff > 0) {
                    sum += diff;
                }
                prevMag[k] = mag;
            }
            flux[f] = sum;
        }
        int avgWindow = Math.max(1, Math.round(0.5f * TARGET_RATE / (float) HOP));
        float[] out = new float[frames];
        float running = 0f;
        for (int i = 0; i < frames; i++) {
            running += flux[i];
            if (i >= avgWindow) {
                running -= flux[i - avgWindow];
            }
            int n = Math.min(i + 1, avgWindow);
            float mean = running / n;
            out[i] = Math.max(0f, flux[i] - mean);
        }
        return out;
    }

    static float estimateBpm(float[] envelope) {
        int minLag = bpmToLag(MAX_BPM);
        int maxLag = bpmToLag(MIN_BPM);
        maxLag = Math.min(maxLag, envelope.length - 1);
        minLag = Math.max(1, minLag);
        float bestScore = Float.NEGATIVE_INFINITY;
        int bestLag = bpmToLag(PREFERRED_BPM);
        for (int lag = minLag; lag <= maxLag; lag++) {
            float corr = 0f;
            int n = envelope.length - lag;
            for (int i = 0; i < n; i++) {
                corr += envelope[i] * envelope[i + lag];
            }
            corr /= Math.max(1, n);
            float bpm = lagToBpm(lag);
            float weight = logGaussian(bpm, PREFERRED_BPM, 0.35f);
            float score = corr * weight;
            if (score > bestScore) {
                bestScore = score;
                bestLag = lag;
            }
        }
        float refinedLag = interpolateLag(envelope, bestLag, minLag, maxLag);
        float bpm = lagToBpm(refinedLag);
        bpm = preferOctave(envelope, bpm, bestScore);
        return clamp(bpm, MIN_BPM, MAX_BPM);
    }

    private static float interpolateLag(float[] envelope, int bestLag, int minLag, int maxLag) {
        if (bestLag <= minLag || bestLag >= maxLag) {
            return bestLag;
        }
        float y0 = correlation(envelope, bestLag - 1);
        float y1 = correlation(envelope, bestLag);
        float y2 = correlation(envelope, bestLag + 1);
        float denom = y0 - 2f * y1 + y2;
        if (Math.abs(denom) < 1e-9f) {
            return bestLag;
        }
        float delta = 0.5f * (y0 - y2) / denom;
        if (delta > 1f) {
            delta = 1f;
        } else if (delta < -1f) {
            delta = -1f;
        }
        return bestLag + delta;
    }

    private static float correlation(float[] envelope, int lag) {
        float corr = 0f;
        int n = envelope.length - lag;
        for (int i = 0; i < n; i++) {
            corr += envelope[i] * envelope[i + lag];
        }
        return corr / Math.max(1, n);
    }

    private static float preferOctave(float[] envelope, float bpm, float currentScore) {
        float[] candidates = {bpm / 2f, bpm, bpm * 2f};
        float bestBpm = bpm;
        float best = currentScore;
        for (float candidate : candidates) {
            if (candidate < MIN_BPM || candidate > MAX_BPM) {
                continue;
            }
            int lag = bpmToLag(candidate);
            if (lag < 1 || lag >= envelope.length) {
                continue;
            }
            float corr = 0f;
            int n = envelope.length - lag;
            for (int i = 0; i < n; i++) {
                corr += envelope[i] * envelope[i + lag];
            }
            corr /= Math.max(1, n);
            float score = corr * logGaussian(candidate, PREFERRED_BPM, 0.35f);
            if (score > best) {
                best = score;
                bestBpm = candidate;
            }
        }
        return bestBpm;
    }

    static int[] trackBeats(float[] envelope, float bpm, int sampleCount) {
        double period = Math.max(1.0, 60.0 * TARGET_RATE / (bpm * (double) HOP));
        int search = Math.max(1, (int) Math.ceil(period));
        int bestOffset = 0;
        float bestSum = Float.NEGATIVE_INFINITY;
        for (int offset = 0; offset < search; offset++) {
            float sum = 0f;
            for (double t = offset; t < envelope.length; t += period) {
                int i = (int) Math.round(t);
                if (i >= 0 && i < envelope.length) {
                    sum += envelope[i];
                }
            }
            if (sum > bestSum) {
                bestSum = sum;
                bestOffset = offset;
            }
        }
        int snap = Math.max(1, (int) Math.round(period * 0.10));
        int[] frames = new int[(int) (envelope.length / period) + 4];
        int count = 0;
        for (double t = bestOffset; t < envelope.length; t += period) {
            int i = (int) Math.round(t);
            int from = Math.max(0, i - snap);
            int to = Math.min(envelope.length - 1, i + snap);
            int peak = Math.min(Math.max(i, 0), envelope.length - 1);
            float peakVal = envelope[peak];
            for (int j = from; j <= to; j++) {
                if (envelope[j] > peakVal) {
                    peakVal = envelope[j];
                    peak = j;
                }
            }
            frames[count++] = peak;
        }
        int durationMs = (int) Math.round(sampleCount * 1000.0 / TARGET_RATE);
        int[] beats = new int[count];
        int n = 0;
        int last = -1_000_000;
        for (int i = 0; i < count; i++) {
            int ms = (int) Math.round((frames[i] * (double) HOP + WINDOW / 2.0) * 1000.0 / TARGET_RATE);
            if (ms < 0 || ms > durationMs) {
                continue;
            }
            if (ms - last < 50) {
                continue;
            }
            beats[n++] = ms;
            last = ms;
        }
        int[] trimmed = new int[n];
        System.arraycopy(beats, 0, trimmed, 0, n);
        return trimmed;
    }

    static int bpmToLag(float bpm) {
        return Math.max(1, Math.round(60f * TARGET_RATE / (bpm * HOP)));
    }

    static float lagToBpm(float lag) {
        return 60f * TARGET_RATE / (lag * (float) HOP);
    }

    private static float logGaussian(float value, float center, float sigma) {
        double x = Math.log(value / center) / sigma;
        return (float) Math.exp(-0.5 * x * x);
    }

    private static float[] hann(int n) {
        float[] w = new float[n];
        for (int i = 0; i < n; i++) {
            w[i] = (float) (0.5 - 0.5 * Math.cos(2 * Math.PI * i / (n - 1)));
        }
        return w;
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    static void fft(float[] re, float[] im) {
        int n = re.length;
        int bits = Integer.numberOfTrailingZeros(n);
        for (int i = 0; i < n; i++) {
            int j = Integer.reverse(i) >>> (32 - bits);
            if (j > i) {
                float tr = re[i];
                re[i] = re[j];
                re[j] = tr;
                float ti = im[i];
                im[i] = im[j];
                im[j] = ti;
            }
        }
        for (int size = 2; size <= n; size <<= 1) {
            int half = size >> 1;
            double ang = -2 * Math.PI / size;
            float wrStep = (float) Math.cos(ang);
            float wiStep = (float) Math.sin(ang);
            for (int i = 0; i < n; i += size) {
                float wr = 1f;
                float wi = 0f;
                for (int j = 0; j < half; j++) {
                    int even = i + j;
                    int odd = even + half;
                    float tr = wr * re[odd] - wi * im[odd];
                    float ti = wr * im[odd] + wi * re[odd];
                    re[odd] = re[even] - tr;
                    im[odd] = im[even] - ti;
                    re[even] += tr;
                    im[even] += ti;
                    float nwr = wr * wrStep - wi * wiStep;
                    wi = wr * wiStep + wi * wrStep;
                    wr = nwr;
                }
            }
        }
    }
}
