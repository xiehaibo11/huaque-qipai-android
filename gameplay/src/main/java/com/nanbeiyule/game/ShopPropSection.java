package com.nanbeiyule.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Original second-level tabs shown by the Zhejiang lobby's prop shop. */
public enum ShopPropSection {
    ENTRY_CARD("entry_card", "入场卡"),
    RECORDER("recorder", "记牌器"),
    WASH_CARD("wash_card", "洗牌券"),
    LUCK_PROP("luck_prop", "转运道具");

    private static final List<ShopPropSection> ORDERED =
            Collections.unmodifiableList(Arrays.asList(values()));

    private final String id;
    private final String title;

    ShopPropSection(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public static List<ShopPropSection> ordered() {
        return ORDERED;
    }

    boolean contains(ShopProduct product) {
        String code = product.productCode();
        return switch (this) {
            case ENTRY_CARD ->
                    code.startsWith("PROP_GOLD_CARD_")
                            || code.startsWith("PROP_BLACK_CARD_");
            case RECORDER -> code.startsWith("PROP_RECORDER_");
            case WASH_CARD -> code.startsWith("PROP_WASH_CARD_");
            case LUCK_PROP -> code.startsWith("PROP_LUCK_");
        };
    }
}
