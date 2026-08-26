package com.nanbeiyule.game;

public final class ShopWalletState {
    public static final ShopWalletState EMPTY = new ShopWalletState(0, 0, 0, 0);

    private final long roomCards;
    private final long coins;
    private final long diamonds;
    private final long coupons;

    public ShopWalletState(long roomCards, long coins, long diamonds, long coupons) {
        if (roomCards < 0 || coins < 0 || diamonds < 0 || coupons < 0) {
            throw new IllegalArgumentException("wallet values must not be negative");
        }
        this.roomCards = roomCards;
        this.coins = coins;
        this.diamonds = diamonds;
        this.coupons = coupons;
    }

    public long roomCards() {
        return roomCards;
    }

    public long coins() {
        return coins;
    }

    public long diamonds() {
        return diamonds;
    }

    public long coupons() {
        return coupons;
    }
}
