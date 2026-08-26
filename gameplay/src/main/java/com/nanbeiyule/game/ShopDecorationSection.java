package com.nanbeiyule.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Original second-level tabs shown by the Zhejiang lobby's decoration shop. */
public enum ShopDecorationSection {
    VEHICLE("enterani", "专属座驾"),
    TABLE("tablebg", "牌桌"),
    CARD_BACK("pb", "牌背"),
    AVATAR_FRAME("txk", "头像框"),
    CARD_PRESS("ypq", "压牌器");

    private static final List<ShopDecorationSection> ORDERED =
            Collections.unmodifiableList(Arrays.asList(values()));

    private final String id;
    private final String title;

    ShopDecorationSection(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public static List<ShopDecorationSection> ordered() {
        return ORDERED;
    }

    boolean contains(ShopProduct product) {
        return id.equals(product.section());
    }
}
