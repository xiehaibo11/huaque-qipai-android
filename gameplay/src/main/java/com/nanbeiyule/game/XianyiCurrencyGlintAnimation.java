package com.nanbeiyule.game;

/** Samples the original 60 FPS Cocos Studio keyframes without per-frame allocations. */
final class XianyiCurrencyGlintAnimation {
    static final int DOU_CYCLE_FRAMES = 260;
    static final int GOLD_CYCLE_FRAMES = 265;
    static final int CARD_CYCLE_FRAMES = 260;

    static final class Transform {
        float x;
        float y;
        float scale;
        float rotation;
        int alpha;
    }

    private XianyiCurrencyGlintAnimation() {}

    static float frameAt(long elapsedMillis, int cycleFrames) {
        long frame = elapsedMillis * 60L / 1_000L;
        return frame % cycleFrames;
    }

    static void sampleDouStar(int index, float frame, Transform result) {
        sample(XianyiCurrencyGlintTracks.DOU[index], frame, result);
    }

    static void sampleGoldStar(int index, float frame, Transform result) {
        sample(XianyiCurrencyGlintTracks.GOLD[index], frame, result);
    }

    static int cardSweepAlpha(int index, float frame) {
        return clampAlpha(sample(XianyiCurrencyGlintTracks.CARD_SWEEP_ALPHA[index], frame));
    }

    static void sampleCardStar(float frame, Transform result) {
        sample(XianyiCurrencyGlintTracks.CARD_STAR, frame, result);
    }

    static boolean goldMainStarUsesAdditiveBlend(float frame) {
        return frame >= 40.0f && frame < 45.0f;
    }

    private static void sample(
            XianyiCurrencyGlintTracks.Star star,
            float frame,
            Transform result) {
        result.x = sample(star.x(), frame);
        result.y = sample(star.y(), frame);
        result.scale = sample(star.scale(), frame);
        result.rotation = sample(star.rotation(), frame);
        result.alpha = clampAlpha(sample(star.alpha(), frame));
    }

    private static float sample(XianyiCurrencyGlintTracks.Track track, float frame) {
        float[] frames = track.frames();
        float[] values = track.values();
        if (frame <= frames[0]) {
            return values[0];
        }
        for (int index = 1; index < frames.length; index++) {
            if (frame <= frames[index]) {
                float progress = (frame - frames[index - 1]) / (frames[index] - frames[index - 1]);
                return values[index - 1] + (values[index] - values[index - 1]) * progress;
            }
        }
        return values[values.length - 1];
    }

    private static int clampAlpha(float alpha) {
        return Math.max(0, Math.min(255, (int) alpha));
    }
}
