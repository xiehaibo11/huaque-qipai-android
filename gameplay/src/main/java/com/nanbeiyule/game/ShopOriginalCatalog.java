package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Screenshot- and Lua-backed fallback catalog used before/while the API loads. */
public final class ShopOriginalCatalog {
    private ShopOriginalCatalog() {}

    public static ShopCatalogState create() {
        EnumMap<ShopCategory, List<ShopProduct>> products =
                new EnumMap<>(ShopCategory.class);
        for (ShopCategory category : ShopCategory.ordered()) {
            products.put(category, new ArrayList<>());
        }

        addMembership(products);
        addHotRecommendations(products);
        addDiamonds(products);
        addRoomCards(products);
        addCoins(products);
        addEvidenceLimitedProducts(products);
        addRecorderProducts(products);
        return ShopCatalogState.create(products);
    }

    private static void addMembership(Map<ShopCategory, List<ShopProduct>> products) {
        add(products, product("SXVIP_CONTINUOUS_MONTH", ShopCategory.TIME_MEMBERSHIP,
                "连续包月30天", "vip_gift", ShopProduct.Currency.CNY, 2800,
                ShopProduct.Reward.MEMBERSHIP_DAY, 30, true));
        add(products, product("SXVIP_30_DAYS", ShopCategory.TIME_MEMBERSHIP,
                "普通会员30天", "vip_gift", ShopProduct.Currency.CNY, 3500,
                ShopProduct.Reward.MEMBERSHIP_DAY, 30, true));
        add(products, product("SXVIP_90_DAYS", ShopCategory.TIME_MEMBERSHIP,
                "普通会员90天", "vip_gift", ShopProduct.Currency.CNY, 7800,
                ShopProduct.Reward.MEMBERSHIP_DAY, 90, true));
        add(products, product("SXVIP_365_DAYS", ShopCategory.TIME_MEMBERSHIP,
                "普通会员365天", "vip_gift", ShopProduct.Currency.CNY, 26800,
                ShopProduct.Reward.MEMBERSHIP_DAY, 365, true));
        add(products, product("SXVIP_7_DAYS", ShopCategory.TIME_MEMBERSHIP,
                "7天会员", "vip_gift", ShopProduct.Currency.CNY, 2500,
                ShopProduct.Reward.MEMBERSHIP_DAY, 7, true));
    }

    private static void addHotRecommendations(
            Map<ShopCategory, List<ShopProduct>> products) {
        add(products, limitedHotProduct("HOT_FIRST_RECHARGE", "首充礼包", "vip_gift",
                ShopProduct.Currency.CNY, 600, ShopProduct.Reward.DIAMOND, 100,
                null, 1));
        add(products, limitedHotProduct("HOT_DAILY_BENEFIT", "每日福利", "daily_gift",
                ShopProduct.Currency.FREE, 0, ShopProduct.Reward.INTERACTION_PROP, 10,
                1, null));
        add(products, limitedHotProduct("HOT_DAILY_GIFT", "每日礼包", "coin_gift",
                ShopProduct.Currency.CNY, 600, ShopProduct.Reward.COIN, 78000,
                3, null));
        add(products, product("HOT_WEEK_GIFT", ShopCategory.HOT_RECOMMENDATION,
                "每周礼包", "coin_bag", ShopProduct.Currency.CNY, 1800,
                ShopProduct.Reward.COIN, 210000, true));
        add(products, product("HOT_MONTH_GIFT", ShopCategory.HOT_RECOMMENDATION,
                "每月礼包", "coin_chest", ShopProduct.Currency.CNY, 4800,
                ShopProduct.Reward.COIN, 480000, true));
        add(products, product("HOT_VALUE_MONTH_CARD", ShopCategory.HOT_RECOMMENDATION,
                "超值月卡", "treasure_pot", ShopProduct.Currency.CNY, 2800,
                ShopProduct.Reward.COIN, 376000, true));
        addGoldGift(products, "GOLD_GIFT_6", "6元金币礼包", "coin_gift", 600, 78_000);
        addGoldGift(products, "GOLD_GIFT_18", "18元金币礼包", "coin_bag", 1800, 210_000);
        addGoldGift(products, "GOLD_GIFT_30", "30元金币礼包", "coin_chest", 3000, 300_000);
        addGoldGift(products, "GOLD_GIFT_88", "88元金币礼包", "treasure_pot", 8800, 880_000);
    }

    private static ShopProduct limitedHotProduct(
            String code,
            String name,
            String icon,
            ShopProduct.Currency currency,
            long price,
            ShopProduct.Reward reward,
            long quantity,
            Integer dailyLimit,
            Integer lifetimeLimit) {
        Integer remaining = dailyLimit == null ? lifetimeLimit : dailyLimit;
        return new ShopProduct(
                code,
                ShopCategory.HOT_RECOMMENDATION,
                ShopHotSection.VALUE_RECOMMENDATION.id(),
                name,
                icon,
                currency,
                price,
                reward,
                quantity,
                dailyLimit,
                lifetimeLimit,
                0,
                0,
                remaining,
                true);
    }

    private static void addGoldGift(
            Map<ShopCategory, List<ShopProduct>> products,
            String code,
            String name,
            String icon,
            long price,
            long coins) {
        add(
                products,
                new ShopProduct(
                        code,
                        ShopCategory.HOT_RECOMMENDATION,
                        ShopHotSection.GOLD_GIFT.id(),
                        name,
                        icon,
                        ShopProduct.Currency.CNY,
                        price,
                        ShopProduct.Reward.COIN,
                        coins,
                        null,
                        null,
                        0,
                        0,
                        null,
                        true));
    }

    private static void addDiamonds(Map<ShopCategory, List<ShopProduct>> products) {
        long[] amounts = {100, 300, 600, 1800, 3000, 6800, 9800, 12800};
        for (long amount : amounts) {
            add(products, product("DIAMOND_" + amount, ShopCategory.DIAMOND_RECHARGE,
                    amount + "钻石", "diamond", ShopProduct.Currency.CNY, amount,
                    ShopProduct.Reward.DIAMOND, amount, true));
        }
    }

    private static void addRoomCards(Map<ShopCategory, List<ShopProduct>> products) {
        long[] quantities = {1, 5, 17, 29, 67, 128};
        long[] costs = {400, 600, 1800, 3000, 6800, 12800};
        for (int index = 0; index < quantities.length; index++) {
            add(products, product("ROOM_CARD_" + quantities[index], ShopCategory.ROOM_CARD,
                    quantities[index] + "张房卡", "room_card",
                    ShopProduct.Currency.DIAMOND, costs[index],
                    ShopProduct.Reward.ROOM_CARD, quantities[index], true));
        }
    }

    private static void addCoins(Map<ShopCategory, List<ShopProduct>> products) {
        long[] quantities = {
            60_000, 300_000, 880_000, 1_880_000, 5_180_000, 6_480_000, 9_280_000
        };
        long[] costs = {600, 3000, 8800, 18800, 51800, 64800, 92800};
        String[] icons = {"coin_stack", "coin_bag", "coin_chest", "coin_chest",
                "treasure_pot", "treasure_pot", "treasure_pot"};
        for (int index = 0; index < quantities.length; index++) {
            add(products, product("COIN_" + quantities[index], ShopCategory.COIN,
                    formatChineseQuantity(quantities[index]) + "金币", icons[index],
                    ShopProduct.Currency.DIAMOND, costs[index], ShopProduct.Reward.COIN,
                    quantities[index], true));
        }
    }

    private static void addEvidenceLimitedProducts(
            Map<ShopCategory, List<ShopProduct>> products) {
        add(products, product("GOLD_MEMBER_WEEK", ShopCategory.GOLD_MEMBERSHIP,
                "会员周卡", "coin_gift", ShopProduct.Currency.DIAMOND, 1800,
                ShopProduct.Reward.GOLD_MEMBERSHIP_DAY, 7, true));
        add(products, product("GOLD_MEMBER_MONTH", ShopCategory.GOLD_MEMBERSHIP,
                "会员月卡", "coin_bag", ShopProduct.Currency.DIAMOND, 4800,
                ShopProduct.Reward.GOLD_MEMBERSHIP_DAY, 30, true));
        add(products, product("GOLD_MEMBER_VALUE_MONTH", ShopCategory.GOLD_MEMBERSHIP,
                "超值月卡", "treasure_pot", ShopProduct.Currency.CNY, 2800,
                ShopProduct.Reward.GOLD_MEMBERSHIP_DAY, 30, true));

        inventory(products, "PROP_GOLD_CARD_1", ShopCategory.PROP,
                "黄金卡1张", "coupon_gold", 600, ShopProduct.Reward.INVENTORY_PROP, 1);
        inventory(products, "PROP_GOLD_CARD_5", ShopCategory.PROP,
                "黄金卡5张", "coupon_gold", 2500, ShopProduct.Reward.INVENTORY_PROP, 5);
        inventory(products, "PROP_GOLD_CARD_10", ShopCategory.PROP,
                "黄金卡10张", "coupon_gold", 5000, ShopProduct.Reward.INVENTORY_PROP, 10);
        inventory(products, "PROP_BLACK_CARD_1", ShopCategory.PROP,
                "黑钻卡1张", "coupon_black", 3000, ShopProduct.Reward.INVENTORY_PROP, 1);
        inventory(products, "PROP_BLACK_CARD_5", ShopCategory.PROP,
                "黑钻卡5张", "coupon_black", 12500, ShopProduct.Reward.INVENTORY_PROP, 5);
        inventory(products, "PROP_BLACK_CARD_10", ShopCategory.PROP,
                "黑钻卡10张", "coupon_black", 25000, ShopProduct.Reward.INVENTORY_PROP, 10);
        inventory(products, "PROP_WASH_CARD_1", ShopCategory.PROP,
                "洗牌券1张", "wash_card", 20, ShopProduct.Reward.INVENTORY_PROP, 1);
        inventory(products, "PROP_WASH_CARD_5", ShopCategory.PROP,
                "洗牌券5张", "wash_card", 90, ShopProduct.Reward.INVENTORY_PROP, 5);
        inventory(products, "PROP_WASH_CARD_10", ShopCategory.PROP,
                "洗牌券10张", "wash_card", 160, ShopProduct.Reward.INVENTORY_PROP, 10);
        inventory(products, "PROP_LUCK_BEAD_1", ShopCategory.PROP,
                "转运珠1颗", "luck_bead", 20, ShopProduct.Reward.INVENTORY_PROP, 1);
        inventory(products, "PROP_LUCK_BEAD_5", ShopCategory.PROP,
                "转运珠5颗", "luck_bead", 90, ShopProduct.Reward.INVENTORY_PROP, 5);
        inventory(products, "PROP_LUCK_BEAD_10", ShopCategory.PROP,
                "转运珠10颗", "luck_bead", 160, ShopProduct.Reward.INVENTORY_PROP, 10);

        interaction(products, "INTERACTION_THUMB", ShopInteractionSection.EMOTICON,
                "点赞", "thumb", 30, ShopProduct.Reward.INTERACTION_PROP, 1);
        interaction(products, "INTERACTION_TOAST", ShopInteractionSection.EMOTICON,
                "碰杯", "face", 30, ShopProduct.Reward.INTERACTION_PROP, 1);
        interaction(products, "INTERACTION_HANDSHAKE", ShopInteractionSection.EMOTICON,
                "握手", "face", 30, ShopProduct.Reward.INTERACTION_PROP, 1);
        interaction(products, "INTERACTION_ICE_BUCKET", ShopInteractionSection.EMOTICON,
                "冰桶", "wash_card", 30, ShopProduct.Reward.INTERACTION_PROP, 1);
        interaction(products, "INTERACTION_BOMB", ShopInteractionSection.EMOTICON,
                "炸弹", "face", 30, ShopProduct.Reward.INTERACTION_PROP, 1);
        interaction(products, "INTERACTION_MACHINE_GUN", ShopInteractionSection.EMOTICON,
                "机关枪", "voice", 30, ShopProduct.Reward.INTERACTION_PROP, 1);
        interaction(products, "INTERACTION_SLIPPER", ShopInteractionSection.EMOTICON,
                "拖鞋", "slipper", 50, ShopProduct.Reward.INTERACTION_PROP, 1);
        interaction(products, "INTERACTION_ROSE", ShopInteractionSection.EMOTICON,
                "玫瑰", "rose", 50, ShopProduct.Reward.INTERACTION_PROP, 1);
        interaction(products, "CHAT_VOICE_XIAOGU_1_DAY", ShopInteractionSection.CHAT_VOICE,
                "小谷专属语音包1天", "voice", 100,
                ShopProduct.Reward.INTERACTION_PROP, 1);

        decoration(products, "DECORATION_VEHICLE_150801", ShopDecorationSection.VEHICLE,
                "二八大杠7天", "vehicle_150801", 300);
        decoration(products, "DECORATION_VEHICLE_150802", ShopDecorationSection.VEHICLE,
                "北欧幽灵7天", "vehicle_150802", 1500);
        decoration(products, "DECORATION_VEHICLE_150804", ShopDecorationSection.VEHICLE,
                "暗夜精灵7天", "vehicle_150804", 1500);
        decoration(products, "DECORATION_VEHICLE_150803", ShopDecorationSection.VEHICLE,
                "冰蓝狂啸7天", "vehicle_150803", 1500);
        decoration(products, "DECORATION_VEHICLE_150808", ShopDecorationSection.VEHICLE,
                "红色疾风7天", "vehicle_150808", 1500);
        decoration(products, "DECORATION_VEHICLE_150807", ShopDecorationSection.VEHICLE,
                "极速幻影7天", "vehicle_150807", 1500);
        decoration(products, "DECORATION_VEHICLE_150806", ShopDecorationSection.VEHICLE,
                "跃马风情7天", "vehicle_150806", 1500);
        decoration(products, "DECORATION_VEHICLE_150805", ShopDecorationSection.VEHICLE,
                "英伦领航者7天", "vehicle_150805", 1500);
        decoration(products, "DECORATION_VEHICLE_150816", ShopDecorationSection.VEHICLE,
                "越野家7天", "vehicle_150816", 1500);

        decoration(products, "DECORATION_TABLE_1", ShopDecorationSection.TABLE,
                "财神桌布7天", "tablecloth", 300);
        decoration(products, "DECORATION_TABLE_2", ShopDecorationSection.TABLE,
                "招财桌布7天", "tablecloth", 1500);
        decoration(products, "DECORATION_TABLE_3", ShopDecorationSection.CARD_BACK,
                "运旺气旺牌背7天", "card_back", 1500);
        decoration(products, "DECORATION_TABLE_4", ShopDecorationSection.TABLE,
                "牛气桌布7天", "tablecloth", 1500);
        decoration(products, "DECORATION_TABLE_5", ShopDecorationSection.CARD_BACK,
                "福气牌背7天", "card_back", 1500);
        decoration(products, "DECORATION_TABLE_6", ShopDecorationSection.AVATAR_FRAME,
                "白银相框7天", "avatar_frame", 1500);
        decoration(products, "DECORATION_TABLE_7", ShopDecorationSection.TABLE,
                "鼠你最豪7天", "tablecloth", 1500);
        decoration(products, "DECORATION_TABLE_8", ShopDecorationSection.AVATAR_FRAME,
                "浪漫花语7天", "avatar_frame", 1500);
        decoration(products, "DECORATION_TABLE_9", ShopDecorationSection.CARD_PRESS,
                "麒麟祥瑞压牌器7天", "press_bull", 1500);

        add(products, product("COUPON_ROOM_CARD_1", ShopCategory.COUPON_STORE,
                "1房卡", "room_card", ShopProduct.Currency.COUPON, 120,
                ShopProduct.Reward.ROOM_CARD, 1, true));
        add(products, product("COUPON_ROOM_CARD_10", ShopCategory.COUPON_STORE,
                "10房卡", "room_card", ShopProduct.Currency.COUPON, 1100,
                ShopProduct.Reward.ROOM_CARD, 10, true));
        add(products, product("COUPON_ROOM_CARD_30", ShopCategory.COUPON_STORE,
                "30房卡", "room_card", ShopProduct.Currency.COUPON, 3000,
                ShopProduct.Reward.ROOM_CARD, 30, true));
        add(products, product("COUPON_ROOM_CARD_50", ShopCategory.COUPON_STORE,
                "50房卡", "room_card", ShopProduct.Currency.COUPON, 4800,
                ShopProduct.Reward.ROOM_CARD, 50, true));
        add(products, product("COUPON_COIN_10000", ShopCategory.COUPON_STORE,
                "1万金币", "coin_stack", ShopProduct.Currency.COUPON, 150,
                ShopProduct.Reward.COIN, 10_000, true));
    }

    private static void addRecorderProducts(
            Map<ShopCategory, List<ShopProduct>> products) {
        recorder(products, "PROP_RECORDER_2_HOURS", "记牌器2小时",
                ShopProduct.Currency.ROOM_CARD, 3, 120);
        recorder(products, "PROP_RECORDER_1_DAY", "记牌器1天",
                ShopProduct.Currency.ROOM_CARD, 5, 1);
        recorder(products, "PROP_RECORDER_3_DAYS", "记牌器3天",
                ShopProduct.Currency.ROOM_CARD, 15, 3);
        recorder(products, "PROP_RECORDER_7_DAYS", "记牌器7天",
                ShopProduct.Currency.ROOM_CARD, 24, 7);
        recorder(products, "PROP_RECORDER_1_ROUND", "记牌器1局",
                ShopProduct.Currency.DIAMOND, 20, 1);
        recorder(products, "PROP_RECORDER_10_ROUNDS", "记牌器10局",
                ShopProduct.Currency.DIAMOND, 200, 10);
        recorder(products, "PROP_RECORDER_20_ROUNDS", "记牌器20局",
                ShopProduct.Currency.DIAMOND, 400, 20);
    }

    private static void recorder(
            Map<ShopCategory, List<ShopProduct>> products,
            String code,
            String name,
            ShopProduct.Currency currency,
            long price,
            long quantity) {
        add(products, product(code, ShopCategory.PROP, name, "recorder", currency, price,
                ShopProduct.Reward.INVENTORY_PROP, quantity, true));
    }

    private static void inventory(
            Map<ShopCategory, List<ShopProduct>> products,
            String code,
            ShopCategory category,
            String name,
            String icon,
            long price,
            ShopProduct.Reward reward,
            long quantity) {
        add(products, product(code, category, name, icon, ShopProduct.Currency.DIAMOND,
                price, reward, quantity, true));
    }

    private static void decoration(
            Map<ShopCategory, List<ShopProduct>> products,
            String code,
            ShopDecorationSection section,
            String name,
            String icon,
            long price) {
        add(
                products,
                new ShopProduct(
                        code,
                        ShopCategory.DECORATION,
                        section.id(),
                        name,
                        icon,
                        ShopProduct.Currency.DIAMOND,
                        price,
                        ShopProduct.Reward.DECORATION_PROP,
                        7,
                        null,
                        null,
                        0,
                        0,
                        null,
                        true));
    }

    private static void interaction(
            Map<ShopCategory, List<ShopProduct>> products,
            String code,
            ShopInteractionSection section,
            String name,
            String icon,
            long price,
            ShopProduct.Reward reward,
            long quantity) {
        add(
                products,
                new ShopProduct(
                        code,
                        ShopCategory.INTERACTION,
                        section.id(),
                        name,
                        icon,
                        ShopProduct.Currency.DIAMOND,
                        price,
                        reward,
                        quantity,
                        null,
                        null,
                        0,
                        0,
                        null,
                        true));
    }

    private static ShopProduct product(
            String code,
            ShopCategory category,
            String name,
            String icon,
            ShopProduct.Currency currency,
            long price,
            ShopProduct.Reward reward,
            long quantity,
            boolean enabled) {
        return new ShopProduct(code, category, name, icon, currency, price, reward, quantity, enabled);
    }

    private static void add(
            Map<ShopCategory, List<ShopProduct>> products, ShopProduct product) {
        products.get(product.category()).add(product);
    }

    private static String formatChineseQuantity(long value) {
        if (value >= 10000 && value % 10000 == 0) {
            return (value / 10000) + "万";
        }
        if (value >= 10000) {
            return String.format(java.util.Locale.CHINA, "%.1f万", value / 10000f);
        }
        return String.valueOf(value);
    }
}
