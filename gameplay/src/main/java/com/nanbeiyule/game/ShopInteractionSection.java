package com.nanbeiyule.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Original second-level tabs under the Zhejiang shop's interaction category. */
public enum ShopInteractionSection {
    EMOTICON("prop_emoji", "表情包"),
    CHAT_VOICE("yuyin", "聊天语音");

    private static final List<ShopInteractionSection> ORDERED =
            Collections.unmodifiableList(Arrays.asList(values()));

    private final String id;
    private final String title;

    ShopInteractionSection(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public static List<ShopInteractionSection> ordered() {
        return ORDERED;
    }

    boolean contains(ShopProduct product) {
        return id.equals(product.section());
    }
}
