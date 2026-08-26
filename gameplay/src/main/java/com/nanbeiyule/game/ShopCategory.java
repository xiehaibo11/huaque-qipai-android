package com.nanbeiyule.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ShopCategory {
    TIME_MEMBERSHIP("time_membership", "时效会员"),
    HOT_RECOMMENDATION("hot_recommendation", "超值推荐"),
    DIAMOND_RECHARGE("diamond_recharge", "钻石充值"),
    ROOM_CARD("room_card", "房卡"),
    COIN("coin", "金币"),
    GOLD_MEMBERSHIP("gold_membership", "金币会员"),
    PROP("prop", "道具"),
    INTERACTION("interaction", "互动"),
    DECORATION("decoration", "装扮"),
    COUPON_STORE("coupon_store", "礼券商城");

    private static final List<ShopCategory> ORDERED =
            Collections.unmodifiableList(Arrays.asList(values()));

    private final String id;
    private final String title;

    ShopCategory(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public static List<ShopCategory> ordered() {
        return ORDERED;
    }

    public static ShopCategory fromId(String id) {
        if (id != null) {
            for (ShopCategory category : values()) {
                if (category.id.equals(id)) {
                    return category;
                }
            }
        }
        throw new IllegalArgumentException("Unknown shop category: " + id);
    }
}
