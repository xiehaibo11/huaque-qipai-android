package com.nanbeiyule.game;

import java.util.List;

/** Original Zhejiang activity rail order shown by TabsActivity. */
final class LobbyActivityCenterModel {
    enum Destination {
        FREE_DRAW,
        MEMBERSHIP_GIFT,
        LOGIN_GIFT,
        TIME_LOGIN,
        BATTLE_SAND_CITY,
        DAILY_FORTUNE,
        EXPERIENCE_GIFT
    }

    record Item(String title, Destination destination) {}

    private static final List<Item> ITEMS =
            List.of(
                    new Item("免费抽奖", Destination.FREE_DRAW),
                    new Item("会员好礼", Destination.MEMBERSHIP_GIFT),
                    new Item("登录有礼", Destination.LOGIN_GIFT),
                    new Item("百战沙城", Destination.BATTLE_SAND_CITY),
                    new Item("每日运势", Destination.DAILY_FORTUNE),
                    new Item("体验有礼", Destination.EXPERIENCE_GIFT));

    private LobbyActivityCenterModel() {}

    static List<Item> items() {
        return ITEMS;
    }
}
