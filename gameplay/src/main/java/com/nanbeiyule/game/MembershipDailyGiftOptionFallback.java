package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;

/** Completes partial API options with the original four-prop daily gift layout. */
final class MembershipDailyGiftOptionFallback {
    private MembershipDailyGiftOptionFallback() {}

    static MembershipDailyGiftState.Option withOriginalRewards(
            MembershipDailyGiftState.Option option, int giftId) {
        MembershipDailyGiftState.Option fallback = MembershipDailyGiftState.defaultOption(giftId);
        if (option == null) {
            return fallback;
        }
        if (option.rewards().size() >= fallback.rewards().size()) {
            return option;
        }
        List<MembershipDailyGiftState.Reward> rewards = new ArrayList<>(option.rewards());
        rewards.addAll(fallback.rewards().subList(option.rewards().size(), fallback.rewards().size()));
        return new MembershipDailyGiftState.Option(
                option.giftId(), option.title(), option.buttonStyle(), rewards);
    }
}
