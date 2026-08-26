package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AnnouncementCenterLayoutTest {
    private static final float EPSILON = 0.01f;

    @Test
    public void recoveredFullScreenArtworkUsesTheAdaptiveDesignTransform() {
        float[][] screens = {
            {1600f, 900f},
            {1800f, 900f},
            {1950f, 900f},
            {2000f, 900f},
            {1920f, 1200f},
            {2200f, 1800f}
        };
        AdaptiveViewport.Insets insets = new AdaptiveViewport.Insets(96f, 24f, 72f, 80f);

        for (float[] screen : screens) {
            AdaptiveViewport viewport =
                    AdaptiveViewport.create(screen[0], screen[1], 1920f, 1080f, insets);
            AdaptiveViewport.Rect rendered = AnnouncementCenterLayout.panelTransform(viewport)
                    .map(new AdaptiveViewport.Rect(0f, 0f, 1920f, 1080f));
            AdaptiveViewport.Rect expected = viewport.designTransform()
                    .map(new AdaptiveViewport.Rect(0f, 0f, 1920f, 1080f));

            assertEquals(expected.left(), rendered.left(), EPSILON);
            assertEquals(expected.top(), rendered.top(), EPSILON);
            assertEquals(expected.right(), rendered.right(), EPSILON);
            assertEquals(expected.bottom(), rendered.bottom(), EPSILON);
        }
    }

    @Test
    public void rowHitTestingAndScrollingStayInsideTheClippedList() {
        assertEquals(0, AnnouncementCenterLayout.rowAt(280f, 245f, 0f, 8));
        assertEquals(1, AnnouncementCenterLayout.rowAt(280f, 370f, 0f, 8));
        assertEquals(-1, AnnouncementCenterLayout.rowAt(700f, 245f, 0f, 8));
        assertEquals(
                AnnouncementCenterLayout.maxListScroll(8),
                AnnouncementCenterLayout.clampListScroll(100_000f, 8),
                EPSILON);
        assertEquals(0f, AnnouncementCenterLayout.clampListScroll(-1f, 8), EPSILON);
    }

    @Test
    public void longDetailBodyScrollsWithoutMovingItsTitleOrExternalPageButton() {
        float maximum = AnnouncementCenterLayout.maxDetailScroll(1280f);

        assertTrue(maximum > 0f);
        assertEquals(maximum, AnnouncementCenterLayout.clampDetailScroll(10_000f, 1280f), EPSILON);
        assertEquals(0f, AnnouncementCenterLayout.clampDetailScroll(-20f, 1280f), EPSILON);
    }
}
