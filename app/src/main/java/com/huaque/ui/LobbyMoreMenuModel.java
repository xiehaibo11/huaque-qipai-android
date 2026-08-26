package com.huaque.ui;

import java.util.List;

final class LobbyMoreMenuModel {
    static final boolean DEFAULT_VISIBLE = false;
    private static final List<Item> ITEMS = List.of(
            new Item("计分助手", Destination.SCORING_ASSISTANT),
            new Item("公众号", Destination.WECHAT_PUBLIC),
            new Item("浙江新闻", Destination.ZHEJIANG_NEWS),
            new Item("绑定手机", Destination.PHONE_BINDING),
            new Item("设置", Destination.SETTINGS),
            new Item("规则", Destination.RULES),
            new Item("健康须知", Destination.HEALTH_NOTICE),
            new Item("公告", Destination.ANNOUNCEMENTS));

    private LobbyMoreMenuModel() {
    }

    static boolean toggle(boolean visible) {
        return !visible;
    }

    static List<Item> items() {
        return ITEMS;
    }

    enum Destination {
        SCORING_ASSISTANT,
        WECHAT_PUBLIC,
        ZHEJIANG_NEWS,
        PHONE_BINDING,
        SETTINGS,
        RULES,
        HEALTH_NOTICE,
        ANNOUNCEMENTS
    }

    record Item(String title, Destination destination) {}
}
