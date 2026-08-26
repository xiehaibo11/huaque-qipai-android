package com.nanbeiyule.game;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class GameRuleViewportTest {
    @Test public void fits16By9WithThreeButtonNavigation() {
        assertSafe(1920, 1080, new AdaptiveViewport.Insets(0, 0, 0, 96));
    }

    @Test public void fits18By9WithGestureNavigation() {
        assertSafe(2160, 1080, new AdaptiveViewport.Insets(0, 0, 64, 36));
    }

    @Test public void fits19Point5By9CutoutScreen() {
        assertSafe(2340, 1080, new AdaptiveViewport.Insets(96, 32, 48, 36));
    }

    @Test public void fits20By9CutoutScreen() {
        assertSafe(2400, 1080, new AdaptiveViewport.Insets(110, 36, 54, 36));
    }

    @Test public void fitsTablet() {
        assertSafe(2560, 1600, new AdaptiveViewport.Insets(0, 36, 0, 48));
    }

    @Test public void fitsFoldableUnfolded() {
        assertSafe(2208, 1768, new AdaptiveViewport.Insets(0, 52, 0, 84));
    }

    @Test public void fitsFoldableNonUnfolded() {
        assertSafe(2268, 832, new AdaptiveViewport.Insets(92, 0, 0, 48));
    }

    private static void assertSafe(float width, float height, AdaptiveViewport.Insets insets) {
        GameRuleViewport viewport = GameRuleViewport.fit(width, height, insets);

        assertTrue(viewport.left() >= insets.left() - 0.01f);
        assertTrue(viewport.top() >= insets.top() - 0.01f);
        assertTrue(viewport.right() <= width - insets.right() + 0.01f);
        assertTrue(viewport.bottom() <= height - insets.bottom() + 0.01f);
        assertTrue(viewport.mapX(GameRuleLayout.CLOSE_LEFT) >= insets.left());
        assertTrue(viewport.mapY(GameRuleLayout.CLOSE_TOP) >= insets.top());
        assertTrue(viewport.mapX(GameRuleLayout.IMAGE_TUTORIAL_RIGHT)
                <= width - insets.right());
    }
}
