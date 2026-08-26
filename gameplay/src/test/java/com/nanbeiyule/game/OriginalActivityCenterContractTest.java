package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

/** Contracts recovered from the original ActivityLayer.csd and TabsActivity/View.lua. */
public class OriginalActivityCenterContractTest {
    private static final float EPSILON = 0.01f;

    @Test
    public void activityUsesTheOriginalFullScreenChromeGeometry() throws Exception {
        assertEquals(1920f, LobbyActivityCenterLayout.DESIGN_WIDTH, EPSILON);
        assertEquals(1080f, LobbyActivityCenterLayout.DESIGN_HEIGHT, EPSILON);
        assertRect("BACKGROUND", 0f, 0f, 1920f, 1080f);
        assertRect("ACTIVITY_TAB", 306.912f, 13.484f, 554.912f, 94.484f);
        assertRect("ANNOUNCEMENT_TAB", 544.912f, 13.484f, 792.912f, 94.484f);
        assertRect("ACTIVITY_LIST", 110f, 183.96f, 455f, 983.96f);
        assertRect("ACTIVITY_CONTENT", 488f, 183.956f, 1770f, 983.956f);
        assertRect("AWARD_CENTER", 1328.76f, 43.012f, 1514.76f, 149.012f);
        assertRect("CLOSE", 1776.64f, 128.68f, 1856.64f, 213.68f);
        assertRect("TITLE_LEFT_OUTER", 220.9123f, 6.984f, 314.9123f, 96.984f);
        assertRect("TITLE_LEFT_INNER", 305.9123f, 48.984f, 346.9123f, 96.984f);
        assertRect("TITLE_RIGHT_OUTER", 774.912f, 6.984f, 868.912f, 96.984f);
        assertRect("TITLE_RIGHT_INNER", 742.912f, 48.984f, 783.912f, 96.984f);
    }

    @Test
    public void chromeTouchRegionsMatchTheVisibleOriginalControls() throws Exception {
        assertEquals(0, LobbyActivityCenterLayout.rowAt(280f, 245f, 0f, 2));
        assertEquals(1, LobbyActivityCenterLayout.rowAt(280f, 370f, 0f, 2));
        assertEquals(-1, LobbyActivityCenterLayout.rowAt(900f, 245f, 0f, 2));
        assertEquals(
                LobbyActivityCenterLayout.Section.ACTIVITY,
                LobbyActivityCenterLayout.sectionAt(430f, 50f));
        assertEquals(
                LobbyActivityCenterLayout.Section.ANNOUNCEMENT,
                LobbyActivityCenterLayout.sectionAt(670f, 50f));
        assertEquals(
                LobbyActivityCenterLayout.Section.ANNOUNCEMENT,
                LobbyActivityCenterLayout.sectionAt(550f, 50f));
        assertTrue(LobbyActivityCenterLayout.awardCenterContains(1420f, 95f));
        assertTrue(LobbyActivityCenterLayout.closeContains(1816f, 170f));
    }

    @Test
    public void announcementUsesTheSameOriginalChromeAndBlankEmptySurface() {
        assertEquals(1920f, AnnouncementCenterLayout.PANEL_WIDTH, EPSILON);
        assertEquals(1080f, AnnouncementCenterLayout.PANEL_HEIGHT, EPSILON);
        assertBox(AnnouncementCenterLayout.CLOSE, 1776.64f, 128.68f, 1856.64f, 213.68f);
        assertBox(AnnouncementCenterLayout.LIST, 110f, 183.96f, 455f, 983.96f);
        assertBox(AnnouncementCenterLayout.DETAIL, 488f, 183.956f, 1770f, 983.956f);
        assertTrue(AnnouncementCenterLayout.emptyStateIsBlank());
    }

    @Test
    public void originalLayeredArtworkIsPackagedInsteadOfAFlattenedScreenshot() {
        assertTrue(resourceExists("original_activity_shop_background.png"));
        assertTrue(resourceExists("original_activity_title_atlas.png"));
    }

    private static void assertRect(
            String fieldName, float left, float top, float right, float bottom) throws Exception {
        Field field = LobbyActivityCenterLayout.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        AdaptiveViewport.Rect rect = (AdaptiveViewport.Rect) field.get(null);
        assertEquals(left, rect.left(), EPSILON);
        assertEquals(top, rect.top(), EPSILON);
        assertEquals(right, rect.right(), EPSILON);
        assertEquals(bottom, rect.bottom(), EPSILON);
    }

    private static void assertBox(
            AnnouncementCenterLayout.Box box,
            float left,
            float top,
            float right,
            float bottom) {
        assertEquals(left, box.left(), EPSILON);
        assertEquals(top, box.top(), EPSILON);
        assertEquals(right, box.right(), EPSILON);
        assertEquals(bottom, box.bottom(), EPSILON);
    }

    private static boolean resourceExists(String name) {
        Path fromRoot = Path.of("gameplay", "src", "main", "res", "drawable-nodpi", name);
        Path fromModule = Path.of("src", "main", "res", "drawable-nodpi", name);
        return Files.isRegularFile(fromRoot) || Files.isRegularFile(fromModule);
    }
}
