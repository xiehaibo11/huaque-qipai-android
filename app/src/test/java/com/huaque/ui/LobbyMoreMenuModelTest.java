package com.huaque.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class LobbyMoreMenuModelTest {
    @Test
    public void startsHidden() {
        assertFalse(LobbyMoreMenuModel.DEFAULT_VISIBLE);
    }

    @Test
    public void togglesVisibilityWhenMoreIsTapped() {
        assertTrue(LobbyMoreMenuModel.toggle(false));
        assertFalse(LobbyMoreMenuModel.toggle(true));
    }

    @Test
    public void exposesEveryRequestedMoreFeatureInOriginalOrder() {
        List<LobbyMoreMenuModel.Item> items = LobbyMoreMenuModel.items();

        assertEquals(8, items.size());
        assertEquals(LobbyMoreMenuModel.Destination.SCORING_ASSISTANT, items.get(0).destination());
        assertEquals(LobbyMoreMenuModel.Destination.WECHAT_PUBLIC, items.get(1).destination());
        assertEquals(LobbyMoreMenuModel.Destination.ZHEJIANG_NEWS, items.get(2).destination());
        assertEquals(LobbyMoreMenuModel.Destination.PHONE_BINDING, items.get(3).destination());
        assertEquals(LobbyMoreMenuModel.Destination.SETTINGS, items.get(4).destination());
        assertEquals(LobbyMoreMenuModel.Destination.RULES, items.get(5).destination());
        assertEquals(LobbyMoreMenuModel.Destination.HEALTH_NOTICE, items.get(6).destination());
        assertEquals(LobbyMoreMenuModel.Destination.ANNOUNCEMENTS, items.get(7).destination());
    }
}
