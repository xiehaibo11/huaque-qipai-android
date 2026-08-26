package com.nanbeiyule.game;

import java.util.Objects;

public final class ShopProduct {
    public enum Currency {
        CNY,
        DIAMOND,
        ROOM_CARD,
        COUPON,
        FREE
    }

    public enum Reward {
        MEMBERSHIP_DAY,
        DIAMOND,
        ROOM_CARD,
        COIN,
        GOLD_MEMBERSHIP_DAY,
        INVENTORY_PROP,
        INTERACTION_PROP,
        DECORATION_PROP,
        COUPON
    }

    private final String productCode;
    private final ShopCategory category;
    private final String section;
    private final String displayName;
    private final String iconKey;
    private final Currency currency;
    private final long priceMinor;
    private final Reward reward;
    private final long rewardQuantity;
    private final Integer dailyLimit;
    private final Integer lifetimeLimit;
    private final long purchasedToday;
    private final long purchasedLifetime;
    private final Integer remainingPurchases;
    private final boolean enabled;

    public ShopProduct(
            String productCode,
            ShopCategory category,
            String displayName,
            String iconKey,
            Currency currency,
            long priceMinor,
            Reward reward,
            long rewardQuantity,
            boolean enabled) {
        this(
                productCode,
                category,
                category == ShopCategory.HOT_RECOMMENDATION
                        ? ShopHotSection.VALUE_RECOMMENDATION.id()
                        : "default",
                displayName,
                iconKey,
                currency,
                priceMinor,
                reward,
                rewardQuantity,
                null,
                null,
                0,
                0,
                null,
                enabled);
    }

    public ShopProduct(
            String productCode,
            ShopCategory category,
            String section,
            String displayName,
            String iconKey,
            Currency currency,
            long priceMinor,
            Reward reward,
            long rewardQuantity,
            Integer dailyLimit,
            Integer lifetimeLimit,
            long purchasedToday,
            long purchasedLifetime,
            Integer remainingPurchases,
            boolean enabled) {
        this.productCode = requireText(productCode, "productCode");
        this.category = Objects.requireNonNull(category, "category");
        this.section = requireText(section, "section");
        this.displayName = requireText(displayName, "displayName");
        this.iconKey = requireText(iconKey, "iconKey");
        this.currency = Objects.requireNonNull(currency, "currency");
        this.reward = Objects.requireNonNull(reward, "reward");
        if (currency == Currency.FREE ? priceMinor != 0 : priceMinor <= 0) {
            throw new IllegalArgumentException("priceMinor is invalid for " + currency);
        }
        if (rewardQuantity <= 0) {
            throw new IllegalArgumentException("rewardQuantity must be positive");
        }
        if (purchasedToday < 0
                || purchasedLifetime < 0
                || (remainingPurchases != null && remainingPurchases < 0)) {
            throw new IllegalArgumentException("purchase counts must not be negative");
        }
        this.priceMinor = priceMinor;
        this.rewardQuantity = rewardQuantity;
        this.dailyLimit = dailyLimit;
        this.lifetimeLimit = lifetimeLimit;
        this.purchasedToday = purchasedToday;
        this.purchasedLifetime = purchasedLifetime;
        this.remainingPurchases = remainingPurchases;
        this.enabled = enabled;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    public String productCode() {
        return productCode;
    }

    public ShopCategory category() {
        return category;
    }

    public String section() {
        return section;
    }

    public String displayName() {
        return displayName;
    }

    public String iconKey() {
        return iconKey;
    }

    public Currency currency() {
        return currency;
    }

    public long priceMinor() {
        return priceMinor;
    }

    public Reward reward() {
        return reward;
    }

    public long rewardQuantity() {
        return rewardQuantity;
    }

    public Integer dailyLimit() {
        return dailyLimit;
    }

    public Integer lifetimeLimit() {
        return lifetimeLimit;
    }

    public long purchasedToday() {
        return purchasedToday;
    }

    public long purchasedLifetime() {
        return purchasedLifetime;
    }

    public Integer remainingPurchases() {
        return remainingPurchases;
    }

    public boolean available() {
        return enabled && (remainingPurchases == null || remainingPurchases > 0);
    }

    public boolean enabled() {
        return enabled;
    }
}
