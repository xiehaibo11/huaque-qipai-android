package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LobbyActivityCenterLayoutTest {
    @Test
    public void originalRowsRemainInsideTheRecoveredLeftRail() {
        AdaptiveViewport.Rect first = LobbyActivityCenterLayout.row(0, 0f);
        AdaptiveViewport.Rect second = LobbyActivityCenterLayout.row(1, 0f);

        assertTrue(first.left() >= 107f);
        assertTrue(second.right() <= 458f);
        assertEquals(125f, second.top() - first.top(), 0.01f);
        assertEquals(first.width(), second.width(), 0.01f);
    }

    @Test
    public void resolvesTouchesToTheMatchingOriginalRow() {
        assertEquals(0, LobbyActivityCenterLayout.rowAt(280f, 245f, 0f, 6));
        assertEquals(1, LobbyActivityCenterLayout.rowAt(280f, 370f, 0f, 6));
        assertEquals(5, LobbyActivityCenterLayout.rowAt(280f, 870f, 0f, 6));
        assertEquals(-1, LobbyActivityCenterLayout.rowAt(960f, 245f, 0f, 6));
    }

    @Test
    public void freeDrawArtworkAndButtonMatchTheReferenceContentArea() {
        AdaptiveViewport.Rect content = LobbyActivityCenterLayout.FREE_DRAW_CONTENT;

        assertEquals(1282f, content.width(), 0.01f);
        assertEquals(738f, content.height(), 0.01f);
        assertTrue(LobbyActivityCenterLayout.freeDrawButtonContains(1120f, 915f));
        assertTrue(!LobbyActivityCenterLayout.freeDrawButtonContains(800f, 915f));
    }

    @Test
    public void loginGiftBadgeSitsInsideTheThirdOriginalRailRow() {
        AdaptiveViewport.Rect badge = LobbyActivityCenterLayout.LOGIN_GIFT_BADGE;
        AdaptiveViewport.Rect row = LobbyActivityCenterLayout.row(2, 0f);

        assertEquals(28f, badge.width(), 0.01f);
        assertEquals(28f, badge.height(), 0.01f);
        assertTrue(badge.centerX() >= row.left() && badge.centerX() <= row.right());
        assertTrue(badge.centerY() >= row.top() && badge.centerY() <= row.bottom());
    }

    @Test
    public void freeDrawPanelRemainsVisibleAcrossRequiredLandscapeScreens() {
        float[][] screens = {
            {1920f, 1080f}, {2160f, 1080f}, {2340f, 1080f}, {2400f, 1080f},
            {2560f, 1600f}, {2208f, 1768f}, {2268f, 832f}
        };

        for (float[] screen : screens) {
            AdaptiveViewport viewport =
                    AdaptiveViewport.create(
                            screen[0],
                            screen[1],
                            LobbyActivityCenterLayout.DESIGN_WIDTH,
                            LobbyActivityCenterLayout.DESIGN_HEIGHT,
                            AdaptiveViewport.Insets.NONE);
            AdaptiveViewport.Rect rendered =
                    viewport.designTransform().map(LobbyActivityCenterLayout.FREE_DRAW_CONTENT);
            assertTrue(rendered.left() >= 0f && rendered.right() <= screen[0]);
            assertTrue(rendered.top() >= 0f && rendered.bottom() <= screen[1]);
        }
    }
}
