package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CreateRoomViewportTest {
    private static final float EPSILON = 0.01f;

    @Test
    public void usesTheAuthorizedLobbyTypeface() {
        assertEquals("fonts/zihun_jingdian_lihei.ttf", CreateRoomLayout.FONT_ASSET);
    }

    @Test
    public void keepsTheUninsetSixteenByNineViewportUnchanged() {
        CreateRoomLayout.Viewport viewport =
                CreateRoomLayout.safeViewport(1920, 1080, 0, 0, 0, 0);

        assertEquals(1.0f, viewport.scale(), EPSILON);
        assertEquals(0.0f, viewport.offsetX(), EPSILON);
        assertEquals(0.0f, viewport.offsetY(), EPSILON);
    }

    @Test
    public void keepsTheOriginalExternalBadgeGeometryAtTheTabTopLeft() {
        assertEquals(103.0f, CreateRoomLayout.EXTERNAL_BADGE_WIDTH, EPSILON);
        assertEquals(110.0f, CreateRoomLayout.EXTERNAL_BADGE_HEIGHT, EPSILON);
        assertEquals(-2.2421f, CreateRoomLayout.EXTERNAL_BADGE_LEFT_OFFSET, EPSILON);
        assertEquals(-1.3666f, CreateRoomLayout.EXTERNAL_BADGE_TOP_OFFSET, EPSILON);
    }

    @Test
    public void keepsInteractiveControlsInsideEverySafeViewport() {
        int[][] screens = {
            {1920, 1080, 0, 0, 0, 0},        // 16:9
            {2160, 1080, 0, 0, 0, 96},       // 18:9 + three-button navigation
            {2340, 1080, 96, 0, 0, 24},      // 19.5:9 cutout + gesture navigation
            {2400, 1080, 0, 60, 120, 20},    // 20:9 cutout + gesture navigation
            {2560, 1600, 0, 0, 0, 80},       // tablet
            {2268, 832, 96, 0, 0, 24},       // folded display
            {2200, 1800, 0, 80, 0, 80}       // unfolded foldable
        };
        for (int[] screen : screens) {
            CreateRoomLayout.Viewport viewport = CreateRoomLayout.safeViewport(
                    screen[0], screen[1], screen[2], screen[3], screen[4], screen[5]);
            assertTrue(CreateRoomLayout.DESIGN_WIDTH * viewport.scale() <=
                    screen[0] - screen[2] - screen[4] + EPSILON);
            assertTrue(CreateRoomLayout.DESIGN_HEIGHT * viewport.scale() <=
                    screen[1] - screen[3] - screen[5] + EPSILON);
            assertTrue(screenX(viewport, CreateRoomLayout.BACK_LEFT) >= screen[2] - EPSILON);
            assertTrue(screenX(viewport, CreateRoomLayout.FEEDBACK_RIGHT) <= screen[0] - screen[4] + EPSILON);
            assertTrue(screenY(viewport, CreateRoomLayout.CREATE_BUTTON_CENTER_Y
                    + CreateRoomLayout.CREATE_BUTTON_HEIGHT / 2.0f) <= screen[1] - screen[5] + EPSILON);
        }
    }

    @Test
    public void scalesByPixelsWithoutDependingOnDisplayDensity() {
        CreateRoomLayout.Viewport mdpi = CreateRoomLayout.safeViewport(1280, 720, 0, 0, 0, 0);
        CreateRoomLayout.Viewport xxxhdpi =
                CreateRoomLayout.safeViewport(3840, 2160, 0, 0, 0, 0);

        assertEquals(3.0f, xxxhdpi.scale() / mdpi.scale(), EPSILON);
        assertEquals(0.0f, mdpi.offsetX(), EPSILON);
        assertEquals(0.0f, xxxhdpi.offsetX(), EPSILON);
    }

    private static float screenX(CreateRoomLayout.Viewport viewport, float designX) {
        return viewport.offsetX() + designX * viewport.scale();
    }

    private static float screenY(CreateRoomLayout.Viewport viewport, float designY) {
        return viewport.offsetY() + designY * viewport.scale();
    }
}
