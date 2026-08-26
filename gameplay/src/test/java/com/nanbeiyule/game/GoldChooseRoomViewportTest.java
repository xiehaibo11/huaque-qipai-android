package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.goldroom.GoldChooseRoomLayout;
import org.junit.Test;

public class GoldChooseRoomViewportTest {
    private static final float EPSILON = 0.01f;

    @Test
    public void interactiveContentUsesOneContainedScaleOnEveryTargetShape() {
        int[][] screens = {
            {1600, 900},   // 16:9
            {1800, 900},   // 18:9
            {1950, 900},   // 19.5:9
            {2000, 900},   // 20:9
            {1920, 1200},  // tablet
            {2200, 1800}   // unfolded foldable
        };

        for (int[] screen : screens) {
            GoldChooseRoomViewport.Transform transform =
                    GoldChooseRoomViewport.content(screen[0], screen[1]);

            assertTrue(
                    GoldChooseRoomLayout.DESIGN_WIDTH * transform.scale()
                            <= screen[0] + EPSILON);
            assertTrue(
                    GoldChooseRoomLayout.DESIGN_HEIGHT * transform.scale()
                            <= screen[1] + EPSILON);
            assertEquals(
                    GoldChooseRoomLayout.DESIGN_WIDTH / 2.0f,
                    transform.designX(screen[0] / 2.0f),
                    EPSILON);
            assertEquals(
                    GoldChooseRoomLayout.DESIGN_HEIGHT / 2.0f,
                    transform.designY(screen[1] / 2.0f),
                    EPSILON);
        }
    }

    @Test
    public void widerScreensKeepContentCenteredWithoutStretching() {
        GoldChooseRoomViewport.Transform transform =
                GoldChooseRoomViewport.content(2000, 900);

        assertEquals(5.0f / 6.0f, transform.scale(), EPSILON);
        assertEquals(200.0f, transform.offsetX(), EPSILON);
        assertEquals(0.0f, transform.offsetY(), EPSILON);
    }

    @Test
    public void recoveredBackdropCoversEveryTargetShapeWithoutTransparentGutters() {
        int[][] screens = {
            {1600, 900}, {1800, 900}, {1950, 900}, {2000, 900}, {1920, 1200}, {2200, 1800}
        };
        float overhang =
                (GoldChooseRoomViewport.BACKDROP_WIDTH
                                - GoldChooseRoomLayout.DESIGN_WIDTH)
                        / 2.0f;

        for (int[] screen : screens) {
            GoldChooseRoomViewport.Transform transform =
                    GoldChooseRoomViewport.backdrop(screen[0], screen[1]);

            assertTrue(transform.screenX(-overhang) <= EPSILON);
            assertTrue(
                    transform.screenX(GoldChooseRoomLayout.DESIGN_WIDTH + overhang)
                            >= screen[0] - EPSILON);
            assertTrue(transform.screenY(0.0f) <= EPSILON);
            assertTrue(
                    transform.screenY(GoldChooseRoomLayout.DESIGN_HEIGHT)
                            >= screen[1] - EPSILON);
        }
    }
}
