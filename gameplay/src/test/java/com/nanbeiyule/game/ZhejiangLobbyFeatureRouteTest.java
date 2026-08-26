package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ZhejiangLobbyFeatureRouteTest {
    @Test
    public void originalBottomActivityAndShareOpenTheirImplementedPages() {
        assertAvailableBottom("ACTIVITIES", ZhejiangLobbyAction.Destination.ACTIVITY_CENTER);
        assertAvailableBottom("SHARE", ZhejiangLobbyAction.Destination.SHARE);
    }

    @Test
    public void originalMoreMenuOpensRulesScoreAssistantAndAnnouncements() {
        assertAvailableMore(MoreMenuItem.RULES, ZhejiangLobbyAction.Destination.RULES);
        assertAvailableMore(
                MoreMenuItem.SCORE_BOX, ZhejiangLobbyAction.Destination.SCORING_ASSISTANT);
        assertAvailableMore(
                MoreMenuItem.ANNOUNCEMENT, ZhejiangLobbyAction.Destination.ANNOUNCEMENTS);
    }

    private static void assertAvailableBottom(
            String key, ZhejiangLobbyAction.Destination destination) {
        ZhejiangLobbyAction.Route route = ZhejiangLobbyAction.bottom(key);
        assertNull(route.unavailableMessage());
        assertEquals(destination, route.destination());
    }

    private static void assertAvailableMore(
            MoreMenuItem item, ZhejiangLobbyAction.Destination destination) {
        ZhejiangLobbyAction.Route route = ZhejiangLobbyAction.more(item);
        assertNull(route.unavailableMessage());
        assertEquals(destination, route.destination());
    }
}
