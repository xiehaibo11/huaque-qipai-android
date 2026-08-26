package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FreeDrawPanelOverlayTest {
    @Test
    public void movesOnePrizeEveryOnePointFiveSecondsAndLoopsLikeTheOriginal() {
        assertFalse(FreeDrawPanelOverlay.shouldAnimate(4));
        assertTrue(FreeDrawPanelOverlay.shouldAnimate(5));
        assertEquals(0f, FreeDrawPanelOverlay.scrollOffset(0L, 6), 0.001f);
        assertEquals(87.5f, FreeDrawPanelOverlay.scrollOffset(750L, 6), 0.001f);
        assertEquals(175f, FreeDrawPanelOverlay.scrollOffset(1_500L, 6), 0.001f);
        assertEquals(0f, FreeDrawPanelOverlay.scrollOffset(9_000L, 6), 0.001f);
    }
}
