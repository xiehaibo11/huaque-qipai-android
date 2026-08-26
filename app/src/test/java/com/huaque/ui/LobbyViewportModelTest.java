package com.huaque.ui;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LobbyViewportModelTest {
    @Test
    public void phoneRatiosContinueToFillTheWholeDisplay() {
        assertArrayEquals(
                new int[]{0, 0, 1600, 900},
                LobbyViewportModel.map(1600, 900, 0, 0, 1920, 1080));
        assertArrayEquals(
                new int[]{0, 0, 2000, 900},
                LobbyViewportModel.map(2000, 900, 0, 0, 1920, 1080));
    }

    @Test
    public void tabletsAndUnfoldedScreensKeepForegroundProportions() {
        assertArrayEquals(
                new int[]{0, 60, 1920, 1080},
                LobbyViewportModel.map(1920, 1200, 0, 0, 1920, 1080));
        assertArrayEquals(
                new int[]{0, 281, 2200, 1238},
                LobbyViewportModel.map(2200, 1800, 0, 0, 1920, 1080));
        assertArrayEquals(
                new int[]{1134, 1192, 344, 132},
                LobbyViewportModel.map(2200, 1800, 990, 795, 300, 115));
    }

    @Test
    public void touchCoordinatesUseTheSameCenteredTransform() {
        assertEquals(960f, LobbyViewportModel.unmapX(2200, 1800, 1100f), 0.01f);
        assertEquals(540f, LobbyViewportModel.unmapY(2200, 1800, 900f), 0.01f);
        assertEquals(0f, LobbyViewportModel.unmapY(2200, 1800, 281.25f), 0.01f);
    }
}
