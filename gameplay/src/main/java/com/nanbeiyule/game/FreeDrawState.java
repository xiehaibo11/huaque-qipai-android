package com.nanbeiyule.game;

import java.time.Instant;
import java.util.List;

record FreeDrawState(
        String activityCode,
        String adPlacementId,
        int dailyLimit,
        int completedDraws,
        int remainingDraws,
        List<Prize> prizes,
        Instant serverTime) {
    FreeDrawState {
        prizes = List.copyOf(prizes);
    }

    FreeDrawState withRemaining(int remaining) {
        return new FreeDrawState(
                activityCode,
                adPlacementId,
                dailyLimit,
                dailyLimit - remaining,
                remaining,
                prizes,
                serverTime);
    }

    record Prize(String prizeId, String type, long amount, String displayName, String iconKey) {}
}
