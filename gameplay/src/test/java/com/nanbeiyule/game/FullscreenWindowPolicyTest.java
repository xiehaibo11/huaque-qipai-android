package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.WindowManager;
import org.junit.Test;

public class FullscreenWindowPolicyTest {
    @Test
    public void api30AndLaterUsesAlwaysCutoutMode() {
        assertEquals(
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS,
                FullscreenWindowPolicy.cutoutModeForApi(30));
        assertEquals(
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS,
                FullscreenWindowPolicy.cutoutModeForApi(35));
        assertTrue(FullscreenWindowPolicy.disablesDecorFitting(30));
    }

    @Test
    public void api28And29UseShortEdgesMode() {
        assertEquals(
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES,
                FullscreenWindowPolicy.cutoutModeForApi(28));
        assertEquals(
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES,
                FullscreenWindowPolicy.cutoutModeForApi(29));
        assertFalse(FullscreenWindowPolicy.disablesDecorFitting(29));
    }

    @Test
    public void preCutoutAndroidKeepsDefaultMode() {
        assertEquals(
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT,
                FullscreenWindowPolicy.cutoutModeForApi(27));
    }
}
