package com.nanbeiyule.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Original second-level tabs under the Zhejiang shop's 超值推荐 category. */
public enum ShopHotSection {
    VALUE_RECOMMENDATION("value_recommendation", "超值推荐"),
    GOLD_GIFT("gold_gift", "金币礼包");

    private static final List<ShopHotSection> ORDERED =
            Collections.unmodifiableList(Arrays.asList(values()));

    private final String id;
    private final String title;

    ShopHotSection(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public static List<ShopHotSection> ordered() {
        return ORDERED;
    }

    boolean contains(ShopProduct product) {
        return id.equals(product.section());
    }
}
