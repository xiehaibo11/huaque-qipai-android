package com.huaque.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class LobbyBottomBarModelTest {
    @Test
    public void exposesZhejiangLobbyItemsInOriginalOrder() {
        List<LobbyBottomBarModel.Item> items = LobbyBottomBarModel.items();

        assertEquals(8, items.size());
        assertEquals("商城", items.get(0).title);
        assertEquals("装扮", items.get(1).title);
        assertEquals("战绩", items.get(2).title);
        assertEquals("活动", items.get(3).title);
        assertEquals("分享", items.get(4).title);
        assertEquals("背包", items.get(5).title);
        assertEquals("邮件", items.get(6).title);
        assertEquals("更多", items.get(7).title);

        Set<Integer> ids = new HashSet<>();
        for (LobbyBottomBarModel.Item item : items) {
            assertTrue(ids.add(item.id));
        }
    }

    @Test
    public void matchesOriginalBottomBarGeometry() {
        assertEquals(new LobbyBottomBarModel.Rect(80, 945, 1300, 95),
                LobbyBottomBarModel.barBounds());

        List<LobbyBottomBarModel.Rect> slots = LobbyBottomBarModel.itemBounds();
        assertEquals(8, slots.size());
        for (int index = 0; index < slots.size(); index++) {
            LobbyBottomBarModel.Rect slot = slots.get(index);
            assertEquals(90 + index * 160, slot.x);
            assertEquals(945, slot.y);
            assertEquals(160, slot.width);
            assertEquals(95, slot.height);
            assertTrue(slot.x >= LobbyBottomBarModel.barBounds().x);
            assertTrue(slot.x + slot.width
                    <= LobbyBottomBarModel.barBounds().x + LobbyBottomBarModel.barBounds().width);
        }
    }

    @Test
    public void moreMenuStartsClosedAndToggles() {
        assertFalse(LobbyBottomBarModel.DEFAULT_MORE_MENU_VISIBLE);
        boolean visible = LobbyBottomBarModel.toggleMoreMenu(
                LobbyBottomBarModel.DEFAULT_MORE_MENU_VISIBLE);
        assertTrue(visible);
        assertFalse(LobbyBottomBarModel.toggleMoreMenu(visible));
    }

    @Test
    public void quickStartUsesOriginalZhejiangBounds() {
        assertEquals(new LobbyBottomBarModel.Rect(1436, 890, 430, 180),
                LobbyBottomBarModel.quickStartBounds());
    }

    @Test
    public void quickStartNamesTaizhouGoldRoom() {
        assertEquals("金币场-台州麻将", LobbyBottomBarModel.quickStartSubtitle());
    }

    @Test
    public void routesStoreRecordsAndMoreToTheirInteractiveDestinations() {
        assertEquals(
                LobbyBottomBarModel.Destination.SHOP,
                LobbyBottomBarModel.destination(LobbyBottomBarModel.items().get(0)));
        assertEquals(
                LobbyBottomBarModel.Destination.RECORDS,
                LobbyBottomBarModel.destination(LobbyBottomBarModel.items().get(2)));
        assertEquals(
                LobbyBottomBarModel.Destination.MAIL,
                LobbyBottomBarModel.destination(LobbyBottomBarModel.items().get(6)));
        assertEquals(
                LobbyBottomBarModel.Destination.MORE,
                LobbyBottomBarModel.destination(LobbyBottomBarModel.items().get(7)));
        assertEquals(
                LobbyBottomBarModel.Destination.SHOP_DECORATION,
                LobbyBottomBarModel.destination(LobbyBottomBarModel.items().get(1)));
    }

    @Test
    public void routesEveryRequestedBottomFeatureInsteadOfFallingBackToUnavailable() {
        assertEquals(
                LobbyBottomBarModel.Destination.ACTIVITIES,
                LobbyBottomBarModel.destination(LobbyBottomBarModel.items().get(3)));
        assertEquals(
                LobbyBottomBarModel.Destination.SHARE,
                LobbyBottomBarModel.destination(LobbyBottomBarModel.items().get(4)));
        assertEquals(
                LobbyBottomBarModel.Destination.BAG,
                LobbyBottomBarModel.destination(LobbyBottomBarModel.items().get(5)));
    }
}
