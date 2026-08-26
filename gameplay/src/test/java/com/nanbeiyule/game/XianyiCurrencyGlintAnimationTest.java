package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class XianyiCurrencyGlintAnimationTest {
    private final XianyiCurrencyGlintAnimation.Transform transform =
            new XianyiCurrencyGlintAnimation.Transform();

    @Test
    public void keepsTheThreeOriginalCocosStudioLoopLengths() {
        assertEquals(260, XianyiCurrencyGlintAnimation.DOU_CYCLE_FRAMES);
        assertEquals(265, XianyiCurrencyGlintAnimation.GOLD_CYCLE_FRAMES);
        assertEquals(260, XianyiCurrencyGlintAnimation.CARD_CYCLE_FRAMES);
        assertEquals(60.0f, XianyiCurrencyGlintAnimation.frameAt(1_000L, 260), 0.001f);
    }

    @Test
    public void samplesTheOriginalDouMainStarKeyframe() {
        XianyiCurrencyGlintAnimation.sampleDouStar(0, 96.0f, transform);

        assertEquals(-11.0867f, transform.x, 0.0001f);
        assertEquals(-18.8717f, transform.y, 0.0001f);
        assertEquals(0.1774f, transform.scale, 0.0001f);
        assertEquals(-22.1406f, transform.rotation, 0.0001f);
        assertEquals(255, transform.alpha);
    }

    @Test
    public void preservesTheDifferentGoldPhase() {
        XianyiCurrencyGlintAnimation.sampleGoldStar(0, 50.0f, transform);

        assertEquals(-11.0867f, transform.x, 0.0001f);
        assertEquals(-17.8717f, transform.y, 0.0001f);
        assertEquals(0.1774f, transform.scale, 0.0001f);
        assertEquals(-22.1406f, transform.rotation, 0.0001f);
        assertEquals(255, transform.alpha);
    }

    @Test
    public void playsTheFiveCardSweepFramesBeforeTheCornerStar() {
        assertEquals(153, XianyiCurrencyGlintAnimation.cardSweepAlpha(0, 2.0f));
        assertEquals(0, XianyiCurrencyGlintAnimation.cardSweepAlpha(0, 6.0f));
        assertEquals(156, XianyiCurrencyGlintAnimation.cardSweepAlpha(1, 7.0f));

        XianyiCurrencyGlintAnimation.sampleCardStar(45.0f, transform);
        assertEquals(24.5507f, transform.x, 0.0001f);
        assertEquals(18.2328f, transform.y, 0.0001f);
        assertEquals(0.7f, transform.scale, 0.0001f);
        assertEquals(90.0f, transform.rotation, 0.0001f);
        assertEquals(255, transform.alpha);
    }

    @Test
    public void samplesEveryShippedFrameWithoutLeavingTheAlphaRange() {
        for (int frame = 0; frame < XianyiCurrencyGlintAnimation.GOLD_CYCLE_FRAMES; frame++) {
            for (int index = 0; index < 4; index++) {
                XianyiCurrencyGlintAnimation.sampleDouStar(index, frame, transform);
                assertTrue(transform.alpha >= 0 && transform.alpha <= 255);
                XianyiCurrencyGlintAnimation.sampleGoldStar(index, frame, transform);
                assertTrue(transform.alpha >= 0 && transform.alpha <= 255);
            }
            for (int index = 0; index < 5; index++) {
                int alpha = XianyiCurrencyGlintAnimation.cardSweepAlpha(index, frame);
                assertTrue(alpha >= 0 && alpha <= 255);
            }
            XianyiCurrencyGlintAnimation.sampleCardStar(frame, transform);
            assertTrue(transform.alpha >= 0 && transform.alpha <= 255);
        }
    }
}
