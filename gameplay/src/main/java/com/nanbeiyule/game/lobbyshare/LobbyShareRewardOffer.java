package com.nanbeiyule.game.lobbyshare;

/** Reward copy supplied by a successful server share-info response. */
public final class LobbyShareRewardOffer {
    private static final LobbyShareRewardOffer NONE = new LobbyShareRewardOffer(0);

    private final long diamondCount;

    private LobbyShareRewardOffer(long diamondCount) {
        this.diamondCount = diamondCount;
    }

    public static LobbyShareRewardOffer none() {
        return NONE;
    }

    public static LobbyShareRewardOffer diamondFromServer(long count) {
        if (count <= 0) {
            throw new IllegalArgumentException("server reward count must be positive");
        }
        return new LobbyShareRewardOffer(count);
    }

    public boolean hasReward() {
        return diamondCount > 0;
    }

    public long diamondCount() {
        return diamondCount;
    }

    public String rewardLabel() {
        return hasReward() ? "x" + diamondCount : "";
    }
}
