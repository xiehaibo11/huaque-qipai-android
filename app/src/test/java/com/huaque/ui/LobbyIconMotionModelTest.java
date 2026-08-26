package com.huaque.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class LobbyIconMotionModelTest {
    private static final float TOLERANCE = 0.001f;

    @Test
    public void definesOnlyTheThreeApprovedForegroundsAtTheirPsdBounds() {
        LobbyIconMotionModel.Spec[] specs = LobbyIconMotionModel.specs();

        assertEquals(3, specs.length);
        assertSpec(specs[0], R.drawable.lobby_icon_taizhou, 1188, 302, 295, 379);
        assertSpec(specs[1], R.drawable.lobby_icon_wahua, 1597, 239, 312, 214);
        assertSpec(specs[2], R.drawable.lobby_icon_shisanshui, 1555, 532, 356, 223);
    }

    @Test
    public void usesOneFiveCycleSequenceForTheThreeEmphasisMoments() {
        LobbyIconMotionModel.Spec[] specs = LobbyIconMotionModel.specs();

        assertEquals(2667L, LobbyIconMotionModel.cycleDurationMillis());
        assertEquals(13335L, LobbyIconMotionModel.sequenceDurationMillis());
        assertEquals(2, specs[0].accentCycle());
        assertEquals(3, specs[1].accentCycle());
        assertEquals(0, specs[2].accentCycle());
    }

    @Test
    public void reproducesTheReferenceIdleMidpoints() {
        LobbyIconMotionModel.Spec[] specs = LobbyIconMotionModel.specs();

        assertFrame(LobbyIconMotionModel.frameAt(specs[0], 1333L),
                0f, 0f, 1f, 0.97f);
        assertFrame(LobbyIconMotionModel.frameAt(specs[1], 1333L),
                0f, -4.16f, 1f, 1f);
        assertFrame(LobbyIconMotionModel.frameAt(specs[2], 2667L + 1333L),
                0f, -4.29f, 1f, 1.014f);
    }

    @Test
    public void reproducesEachReferenceEmphasisPeakInItsAssignedCycle() {
        LobbyIconMotionModel.Spec[] specs = LobbyIconMotionModel.specs();

        assertFrame(LobbyIconMotionModel.frameAt(specs[0], 2 * 2667L + 600L),
                0f, -15.25f, 1f, 1f);
        assertEquals(1.021f,
                LobbyIconMotionModel.frameAt(specs[1], 3 * 2667L + 567L).scaleY(),
                TOLERANCE);
        assertFrame(LobbyIconMotionModel.frameAt(specs[2], 500L),
                -11.2f, 4.55f, 1.045f, 1.05f);
    }

    @Test
    public void wrapsWithoutAFrameJumpAtTheSequenceBoundary() {
        LobbyIconMotionModel.Spec[] specs = LobbyIconMotionModel.specs();

        for (LobbyIconMotionModel.Spec spec : specs) {
            assertEquals(
                    LobbyIconMotionModel.frameAt(spec, 0L),
                    LobbyIconMotionModel.frameAt(spec, 13335L));
        }
    }

    private static void assertSpec(
            LobbyIconMotionModel.Spec spec,
            int drawableResId,
            int x,
            int y,
            int width,
            int height) {
        assertEquals(drawableResId, spec.drawableResId());
        assertEquals(x, spec.psdX());
        assertEquals(y, spec.psdY());
        assertEquals(width, spec.psdWidth());
        assertEquals(height, spec.psdHeight());
    }

    private static void assertFrame(
            LobbyIconMotionModel.Frame frame,
            float translationX,
            float translationY,
            float scaleX,
            float scaleY) {
        assertEquals(translationX, frame.translationXPsdPixels(), TOLERANCE);
        assertEquals(translationY, frame.translationYPsdPixels(), TOLERANCE);
        assertEquals(scaleX, frame.scaleX(), TOLERANCE);
        assertEquals(scaleY, frame.scaleY(), TOLERANCE);
    }
}
