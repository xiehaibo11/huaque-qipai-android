package com.nanbeiyule.game;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class OriginalLobbyTapEffectFrameTest {
    private static final float[] SPINE_QUAD = {
            0f, 0f,
            0f, 10f,
            20f, 10f,
            20f, 0f
    };

    @Test
    public void mapsSpineQuadToAndroidCanvasWithoutStretching() {
        assertArrayEquals(
                new float[] {100f, 95f, 110f, 95f, 100f, 100f, 110f, 100f},
                OriginalLobbyTapEffectFrame.toCanvasMesh(
                        SPINE_QUAD, 100f, 100f, 0.5f, false),
                0.001f);
    }

    @Test
    public void preservesRotatedAtlasRegionOrientation() {
        assertArrayEquals(
                new float[] {110f, 95f, 110f, 100f, 100f, 95f, 100f, 100f},
                OriginalLobbyTapEffectFrame.toCanvasMesh(
                        SPINE_QUAD, 100f, 100f, 0.5f, true),
                0.001f);
    }

    @Test
    public void finishesOnlyAfterTheOneShotAnimationDuration() {
        assertFalse(OriginalLobbyTapEffectFrame.isFinished(1.19f, 1.2f));
        assertTrue(OriginalLobbyTapEffectFrame.isFinished(1.2f, 1.2f));
    }
}
