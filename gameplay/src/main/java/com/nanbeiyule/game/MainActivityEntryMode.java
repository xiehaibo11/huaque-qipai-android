package com.nanbeiyule.game;

enum MainActivityEntryMode {
    STANDARD,
    TAIZHOU_GOLD,
    ZHEJIANG_CREATE_ROOM,
    ZHEJIANG_JOIN_ROOM,
    ZHEJIANG_SHOP,
    ZHEJIANG_DECORATION_SHOP,
    ZHEJIANG_PERSONAL_CENTER,
    ZHEJIANG_ACTIVITIES,
    ZHEJIANG_SHARE,
    ZHEJIANG_BAG,
    ZHEJIANG_SCORING_ASSISTANT,
    ZHEJIANG_WECHAT_PUBLIC,
    ZHEJIANG_NEWS,
    ZHEJIANG_PHONE_BINDING,
    ZHEJIANG_RULES,
    ZHEJIANG_HEALTH_NOTICE,
    ZHEJIANG_ANNOUNCEMENTS;

    static MainActivityEntryMode fromAction(String action) {
        if (MainActivity.ACTION_OPEN_TAIZHOU_GOLD.equals(action)) {
            return TAIZHOU_GOLD;
        }
        if (MainActivity.ACTION_OPEN_ZHEJIANG_CREATE_ROOM.equals(action)) {
            return ZHEJIANG_CREATE_ROOM;
        }
        if (MainActivity.ACTION_OPEN_ZHEJIANG_JOIN_ROOM.equals(action)) {
            return ZHEJIANG_JOIN_ROOM;
        }
        if (MainActivity.ACTION_OPEN_ZHEJIANG_SHOP.equals(action)) {
            return ZHEJIANG_SHOP;
        }
        if (MainActivity.ACTION_OPEN_ZHEJIANG_DECORATION_SHOP.equals(action)) {
            return ZHEJIANG_DECORATION_SHOP;
        }
        if (MainActivity.ACTION_OPEN_ZHEJIANG_PERSONAL_CENTER.equals(action)) {
            return ZHEJIANG_PERSONAL_CENTER;
        }
        if (MainActivity.ACTION_OPEN_ZHEJIANG_ACTIVITIES.equals(action)) {
            return ZHEJIANG_ACTIVITIES;
        }
        if (MainActivity.ACTION_OPEN_ZHEJIANG_SHARE.equals(action)) {
            return ZHEJIANG_SHARE;
        }
        if (MainActivity.ACTION_OPEN_ZHEJIANG_BAG.equals(action)) {
            return ZHEJIANG_BAG;
        }
        if (MainActivity.ACTION_OPEN_ZHEJIANG_SCORING_ASSISTANT.equals(action)) {
            return ZHEJIANG_SCORING_ASSISTANT;
        }
        if (MainActivity.ACTION_OPEN_ZHEJIANG_WECHAT_PUBLIC.equals(action)) {
            return ZHEJIANG_WECHAT_PUBLIC;
        }
        if (MainActivity.ACTION_OPEN_ZHEJIANG_NEWS.equals(action)) {
            return ZHEJIANG_NEWS;
        }
        if (MainActivity.ACTION_OPEN_ZHEJIANG_PHONE_BINDING.equals(action)) {
            return ZHEJIANG_PHONE_BINDING;
        }
        if (MainActivity.ACTION_OPEN_ZHEJIANG_RULES.equals(action)) {
            return ZHEJIANG_RULES;
        }
        if (MainActivity.ACTION_OPEN_ZHEJIANG_HEALTH_NOTICE.equals(action)) {
            return ZHEJIANG_HEALTH_NOTICE;
        }
        if (MainActivity.ACTION_OPEN_ZHEJIANG_ANNOUNCEMENTS.equals(action)) {
            return ZHEJIANG_ANNOUNCEMENTS;
        }
        return STANDARD;
    }

    boolean isDirect() {
        return this != STANDARD;
    }

    boolean rendersGameHomeBeforeDestination() {
        return this == STANDARD;
    }

    boolean loadsGameHomeBeforeDestination() {
        return !opensShop();
    }

    boolean opensGoldRoom() {
        return this == TAIZHOU_GOLD;
    }

    boolean opensShop() {
        return this == ZHEJIANG_SHOP || this == ZHEJIANG_DECORATION_SHOP;
    }

    ShopCategory initialShopCategory() {
        return this == ZHEJIANG_DECORATION_SHOP ? ShopCategory.DECORATION : null;
    }

    boolean opensPersonalCenter() {
        return this == ZHEJIANG_PERSONAL_CENTER;
    }

    MainActivityDestination destination() {
        return switch (this) {
            case STANDARD -> MainActivityDestination.GAME_HOME;
            case ZHEJIANG_CREATE_ROOM -> MainActivityDestination.CREATE_ROOM;
            case ZHEJIANG_JOIN_ROOM -> MainActivityDestination.JOIN_ROOM;
            case TAIZHOU_GOLD -> MainActivityDestination.GOLD_ROOM;
            case ZHEJIANG_SHOP,
                    ZHEJIANG_DECORATION_SHOP -> MainActivityDestination.SHOP;
            case ZHEJIANG_PERSONAL_CENTER -> MainActivityDestination.PERSONAL_CENTER;
            case ZHEJIANG_ACTIVITIES -> MainActivityDestination.ACTIVITY_CENTER;
            case ZHEJIANG_SHARE -> MainActivityDestination.SHARE;
            case ZHEJIANG_BAG -> MainActivityDestination.BAG;
            case ZHEJIANG_SCORING_ASSISTANT -> MainActivityDestination.SCORING_ASSISTANT;
            case ZHEJIANG_WECHAT_PUBLIC -> MainActivityDestination.WECHAT_PUBLIC;
            case ZHEJIANG_NEWS -> MainActivityDestination.ZHEJIANG_NEWS;
            case ZHEJIANG_PHONE_BINDING -> MainActivityDestination.PHONE_BINDING;
            case ZHEJIANG_RULES -> MainActivityDestination.RULES;
            case ZHEJIANG_HEALTH_NOTICE -> MainActivityDestination.HEALTH_NOTICE;
            case ZHEJIANG_ANNOUNCEMENTS -> MainActivityDestination.ANNOUNCEMENTS;
        };
    }

    boolean returnsAccountSwitchToLauncher() {
        return this == ZHEJIANG_PERSONAL_CENTER || this == ZHEJIANG_PHONE_BINDING;
    }
}
