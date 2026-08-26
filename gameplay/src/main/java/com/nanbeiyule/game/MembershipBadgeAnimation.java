package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;

/** Pure eight-second timing model for the generated membership badge. */
final class MembershipBadgeAnimation {
    static final long PERIOD_MS = 8000L;

    record Sparkle(float x, float y, float alpha, float size) {}

    record Frame(
            float scale,
            float sweepProgress,
            float sweepAlpha,
            List<Sparkle> sparkles) {}

    private static final float[][] SPARKLE_ANCHORS = {
        {0.31f, 0.15f},
        {0.32f, 0.48f},
        {0.43f, 0.42f},
        {0.78f, 0.24f}
    };

    private MembershipBadgeAnimation() {}

    static Frame sample(long timeMs) {
        long wrapped = Math.floorMod(timeMs, PERIOD_MS);
        float phase = wrapped / (float) PERIOD_MS;
        float scale =
                1.0075f
                        + 0.0075f
                                * (float)
                                        Math.sin(
                                                phase * Math.PI * 2.0
                                                        - Math.PI / 2.0);
        float sweepProgress = (wrapped % 4000L) / 4000.0f;
        float sweepAlpha =
                (float) Math.sin(sweepProgress * Math.PI);

        ArrayList<Sparkle> sparkles =
                new ArrayList<>(SPARKLE_ANCHORS.length);
        for (int index = 0; index < SPARKLE_ANCHORS.length; index++) {
            float sparklePhase = (phase + index * 0.23f) % 1.0f;
            float raw =
                    Math.max(
                            0.0f,
                            (float)
                                    Math.sin(
                                            sparklePhase
                                                    * Math.PI
                                                    * 2.0));
            float alpha = raw * raw * raw * raw;
            sparkles.add(
                    new Sparkle(
                            SPARKLE_ANCHORS[index][0],
                            SPARKLE_ANCHORS[index][1],
                            alpha,
                            0.018f + index * 0.002f));
        }
        return new Frame(
                scale,
                sweepProgress,
                sweepAlpha,
                List.copyOf(sparkles));
    }
}
