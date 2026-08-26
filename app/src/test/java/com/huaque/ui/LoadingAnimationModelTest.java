package com.huaque.ui;

import org.junit.Test;

public final class LoadingAnimationModelTest {
    public static void main(String[] args) {
        keepsRingInsideTheView();
        centersTextAroundTheRequestedVisualCenter();
        showsBrandCharactersOneAtATime();
        keepsLoadingVisibleForNineSeconds();
    }

    private static void keepsRingInsideTheView() {
        float radius = LoadingAnimationModel.ringRadius(340f, 340f, 28f);
        assertNear(142f, radius, "ring radius");
        assertNear(28f, 170f - radius, "top and left inset");
        assertNear(312f, 170f + radius, "bottom and right edge");
    }

    private static void centersTextAroundTheRequestedVisualCenter() {
        float baseline = LoadingAnimationModel.centeredTextBaseline(154f, -72f, 18f);
        assertNear(181f, baseline, "text baseline");
    }

    private static void showsBrandCharactersOneAtATime() {
        assertEquals("南", LoadingAnimationModel.centerGlyph(0f), "first character");
        assertEquals("南", LoadingAnimationModel.centerGlyph(24.9f), "first quarter");
        assertEquals("北", LoadingAnimationModel.centerGlyph(25f), "second quarter");
        assertEquals("娱", LoadingAnimationModel.centerGlyph(50f), "third quarter");
        assertEquals("乐", LoadingAnimationModel.centerGlyph(75f), "fourth quarter");
        assertEquals("乐", LoadingAnimationModel.centerGlyph(100f), "completed state");
    }

    private static void keepsLoadingVisibleForNineSeconds() {
        int progress = 0;
        int ticks = 0;
        while (progress < 100) {
            progress = LoadingAnimationModel.nextProgress(progress);
            ticks++;
        }
        assertEquals(100, ticks, "loading ticks");
        assertEquals(9000L, ticks * LoadingAnimationModel.TICK_MILLIS, "loading duration");
    }

    @Test
    public void keepsLoadingLabelShadowInsideItsBoxAtThreeHundredDpi() {
        int virtualHeight = LoadingAnimationModel.requiredVirtualTextBoxHeight(
                -45,
                11,
                7.5f,
                3.75f,
                1600,
                900,
                56);

        org.junit.Assert.assertEquals(90, virtualHeight);
        org.junit.Assert.assertEquals(
                691,
                LoadingAnimationModel.centeredBoxTop(736, virtualHeight));
    }

    private static void assertNear(float expected, float actual, String label) {
        if (Math.abs(expected - actual) > 0.001f) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }
}
