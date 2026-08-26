package com.huaque.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FullscreenWindowPolicyTest {
    @Test
    public void androidElevenAndNewerUseEveryCutoutEdge() {
        assertEquals(3, FullscreenWindowPolicy.cutoutModeForApi(30));
        assertEquals(3, FullscreenWindowPolicy.cutoutModeForApi(35));
        assertTrue(FullscreenWindowPolicy.disablesDecorFitting(30));
    }

    @Test
    public void androidNineAndTenUseShortEdges() {
        assertEquals(1, FullscreenWindowPolicy.cutoutModeForApi(28));
        assertEquals(1, FullscreenWindowPolicy.cutoutModeForApi(29));
        assertFalse(FullscreenWindowPolicy.disablesDecorFitting(29));
    }

    @Test
    public void olderAndroidKeepsDefaultCutoutMode() {
        assertEquals(0, FullscreenWindowPolicy.cutoutModeForApi(27));
        assertFalse(FullscreenWindowPolicy.disablesDecorFitting(27));
    }
}
