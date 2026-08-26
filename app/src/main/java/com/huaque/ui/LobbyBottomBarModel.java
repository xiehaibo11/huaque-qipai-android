package com.huaque.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class LobbyBottomBarModel {
    static final boolean DEFAULT_MORE_MENU_VISIBLE = false;

    private static final Rect BAR_BOUNDS = new Rect(80, 945, 1300, 95);
    private static final Rect QUICK_START_BOUNDS = new Rect(1436, 890, 430, 180);
    private static final String QUICK_START_SUBTITLE = "金币场-台州麻将";
    private static final List<Item> ITEMS = List.of(
            new Item(1001, "商城"),
            new Item(1027, "装扮"),
            new Item(1005, "战绩"),
            new Item(1002, "活动"),
            new Item(1017, "分享"),
            new Item(1021, "背包"),
            new Item(1018, "邮件"),
            new Item(1007, "更多"));

    private LobbyBottomBarModel() {
    }

    static List<Item> items() {
        return ITEMS;
    }

    static Rect barBounds() {
        return BAR_BOUNDS;
    }

    static Rect quickStartBounds() {
        return QUICK_START_BOUNDS;
    }

    static String quickStartSubtitle() {
        return QUICK_START_SUBTITLE;
    }

    static List<Rect> itemBounds() {
        List<Rect> bounds = new ArrayList<>(ITEMS.size());
        for (int index = 0; index < ITEMS.size(); index++) {
            bounds.add(new Rect(90 + index * 160, 945, 160, 95));
        }
        return List.copyOf(bounds);
    }

    static boolean toggleMoreMenu(boolean visible) {
        return !visible;
    }

    static Destination destination(Item item) {
        if (item.id == 1001) {
            return Destination.SHOP;
        }
        if (item.id == 1027) {
            return Destination.SHOP_DECORATION;
        }
        if (item.id == 1005) {
            return Destination.RECORDS;
        }
        if (item.id == 1002) {
            return Destination.ACTIVITIES;
        }
        if (item.id == 1017) {
            return Destination.SHARE;
        }
        if (item.id == 1021) {
            return Destination.BAG;
        }
        if (item.id == 1018) {
            return Destination.MAIL;
        }
        if (item.id == 1007) {
            return Destination.MORE;
        }
        return Destination.UNAVAILABLE;
    }

    enum Destination {
        SHOP,
        SHOP_DECORATION,
        RECORDS,
        ACTIVITIES,
        SHARE,
        BAG,
        MAIL,
        MORE,
        UNAVAILABLE
    }

    static final class Item {
        final int id;
        final String title;

        Item(int id, String title) {
            this.id = id;
            this.title = title;
        }
    }

    static final class Rect {
        final int x;
        final int y;
        final int width;
        final int height;

        Rect(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Rect)) {
                return false;
            }
            Rect rect = (Rect) other;
            return x == rect.x
                    && y == rect.y
                    && width == rect.width
                    && height == rect.height;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, width, height);
        }
    }
}
