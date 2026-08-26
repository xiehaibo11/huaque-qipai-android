package com.nanbeiyule.game;

import java.util.List;

/** Immutable original SxvipShopItem card data shared by every membership purchase surface. */
record MembershipPurchasePlan(
        String productCode,
        String productName,
        int days,
        String giftValue,
        String price,
        String dayCost,
        CardTint tint,
        Tag tag,
        List<Reward> rewards) {
    enum CardTint {
        RED,
        GREEN,
        PURPLE
    }

    enum Tag {
        NONE,
        HOT,
        VALUE
    }

    record Reward(String name, String count, int iconResId) {}

    MembershipPurchasePlan {
        rewards = List.copyOf(rewards);
    }

    String giftValueText() {
        return "送价值" + giftValue + "元礼包";
    }

    static List<MembershipPurchasePlan> originalPlans() {
        return OriginalPlans.PLANS;
    }

    static MembershipPurchasePlan fromProduct(MembershipProductsState.Product product) {
        List<Reward> convertedRewards =
                product.rewards().stream()
                        .map(
                                reward ->
                                        new Reward(
                                                reward.displayName(),
                                                reward.countText(),
                                                rewardIcon(reward.code(), reward.iconKey())))
                        .toList();
        return new MembershipPurchasePlan(
                product.productCode(),
                product.name(),
                product.durationDays(),
                String.valueOf(product.giftValueYuan()),
                product.priceText(),
                product.dayCostText(),
                cardTint(product.cardStyle()),
                tag(product.cornerTag()),
                convertedRewards);
    }

    MembershipPurchasePlan withProduct(String name, String currentPrice) {
        return new MembershipPurchasePlan(
                productCode,
                name == null || name.isBlank() ? productName : name,
                days,
                giftValue,
                currentPrice == null || currentPrice.isBlank() ? price : currentPrice,
                dayCost,
                tint,
                tag,
                rewards);
    }

    private static MembershipPurchasePlan plan(
            String productCode,
            int days,
            String giftValue,
            String price,
            String dayCost,
            CardTint tint,
            Tag tag,
            Reward... rewards) {
        return new MembershipPurchasePlan(
                productCode,
                days + "天会员",
                days,
                giftValue,
                price,
                dayCost,
                tint,
                tag,
                List.of(rewards));
    }

    private static Reward reward(String name, String count, int iconResId) {
        return new Reward(name, count, iconResId);
    }

    private static int rewardIcon(String code, String iconKey) {
        String key = code == null || code.isBlank() ? iconKey : code;
        return switch (key) {
            case "COIN", "membership_reward_coin" -> R.drawable.membership_reward_coin;
            case "SHUFFLE_TICKET", "membership_reward_shuffle_ticket" ->
                    R.drawable.membership_reward_shuffle_ticket;
            case "LUCK_BEAD", "membership_reward_luck_bead" ->
                    R.drawable.membership_reward_luck_bead;
            case "ENTRY_EFFECT", "membership_reward_entry_ticket" ->
                    R.drawable.membership_reward_entry_ticket;
            case "TABLECLOTH", "membership_reward_tablecloth" ->
                    R.drawable.membership_reward_tablecloth;
            case "CARD_BACK", "membership_reward_card_back" ->
                    R.drawable.membership_reward_card_back;
            default -> R.drawable.membership_reward_game_card;
        };
    }

    private static CardTint cardTint(String cardStyle) {
        return "PURPLE".equals(cardStyle)
                ? CardTint.PURPLE
                : "RED".equals(cardStyle) ? CardTint.RED : CardTint.GREEN;
    }

    private static Tag tag(String cornerTag) {
        return "HOT".equals(cornerTag)
                ? Tag.HOT
                : "VALUE".equals(cornerTag) ? Tag.VALUE : Tag.NONE;
    }

    private static final class OriginalPlans {
        private static final List<MembershipPurchasePlan> PLANS =
                List.of(
                        plan(
                                "SXVIP_CONTINUOUS_MONTH",
                                30,
                                "42",
                                "连续包月:28元",
                                "每天仅0.9元",
                                CardTint.RED,
                                Tag.HOT,
                                reward("金币", "x20000", R.drawable.membership_reward_coin),
                                reward("洗牌券", "x2", R.drawable.membership_reward_shuffle_ticket),
                                reward("转运珠", "x2", R.drawable.membership_reward_luck_bead),
                                reward("入场3天", "x3", R.drawable.membership_reward_entry_ticket),
                                reward("桌布3天", "x3", R.drawable.membership_reward_tablecloth),
                                reward("牌背3天", "x3", R.drawable.membership_reward_card_back)),
                        plan(
                                "SXVIP_30_DAYS",
                                30,
                                "42",
                                "35元",
                                "每天仅1.1元",
                                CardTint.GREEN,
                                Tag.NONE,
                                reward("金币", "x20000", R.drawable.membership_reward_coin),
                                reward("洗牌券", "x2", R.drawable.membership_reward_shuffle_ticket),
                                reward("转运珠", "x2", R.drawable.membership_reward_luck_bead),
                                reward("入场3天", "x3", R.drawable.membership_reward_entry_ticket),
                                reward("桌布3天", "x3", R.drawable.membership_reward_tablecloth),
                                reward("牌背3天", "x3", R.drawable.membership_reward_card_back)),
                        plan(
                                "SXVIP_90_DAYS",
                                90,
                                "136",
                                "78元",
                                "每天仅0.8元",
                                CardTint.GREEN,
                                Tag.NONE,
                                reward("金币", "x60000", R.drawable.membership_reward_coin),
                                reward("洗牌券", "x6", R.drawable.membership_reward_shuffle_ticket),
                                reward("转运珠", "x6", R.drawable.membership_reward_luck_bead),
                                reward("入场10天", "x10", R.drawable.membership_reward_entry_ticket),
                                reward("桌布10天", "x10", R.drawable.membership_reward_tablecloth),
                                reward("牌背10天", "x10", R.drawable.membership_reward_card_back)),
                        plan(
                                "SXVIP_365_DAYS",
                                365,
                                "588",
                                "268元",
                                "每天仅0.7元",
                                CardTint.PURPLE,
                                Tag.VALUE,
                                reward("金币", "x300000", R.drawable.membership_reward_coin),
                                reward("洗牌券", "x30", R.drawable.membership_reward_shuffle_ticket),
                                reward("转运珠", "x30", R.drawable.membership_reward_luck_bead),
                                reward("入场50天", "x50", R.drawable.membership_reward_entry_ticket),
                                reward("桌布50天", "x50", R.drawable.membership_reward_tablecloth),
                                reward("牌背50天", "x50", R.drawable.membership_reward_card_back)),
                        plan(
                                "SXVIP_7_DAYS",
                                7,
                                "12",
                                "25元",
                                "每天仅3.5元",
                                CardTint.GREEN,
                                Tag.NONE,
                                reward("金币", "x10000", R.drawable.membership_reward_coin),
                                reward("洗牌券", "x1", R.drawable.membership_reward_shuffle_ticket),
                                reward("桌布1天", "x1", R.drawable.membership_reward_tablecloth),
                                reward("牌背1天", "x1", R.drawable.membership_reward_card_back)));

        private OriginalPlans() {}
    }
}
