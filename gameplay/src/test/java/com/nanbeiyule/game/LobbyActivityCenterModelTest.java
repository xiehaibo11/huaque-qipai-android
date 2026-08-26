package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;

public class LobbyActivityCenterModelTest {
    @Test
    public void exposesTheOriginalActivityRailWithFreeDrawSelectedFirst() {
        List<LobbyActivityCenterModel.Item> items = LobbyActivityCenterModel.items();

        assertEquals(6, items.size());
        assertEquals(
                new LobbyActivityCenterModel.Item(
                        "免费抽奖", LobbyActivityCenterModel.Destination.FREE_DRAW),
                items.get(0));
        assertEquals(
                new LobbyActivityCenterModel.Item(
                        "会员好礼", LobbyActivityCenterModel.Destination.MEMBERSHIP_GIFT),
                items.get(1));
        assertEquals("登录有礼", items.get(2).title());
        assertEquals("LOGIN_GIFT", items.get(2).destination().name());
        assertEquals("百战沙城", items.get(3).title());
        assertEquals("每日运势", items.get(4).title());
        assertEquals("体验有礼", items.get(5).title());
    }
}
