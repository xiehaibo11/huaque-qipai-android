package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/** Original Sxvip daily-gift reward artwork mapped by backend reward code. */
final class MembershipDailyGiftRewardIconSet {
    private final Bitmap coin;
    private final Bitmap recorder;
    private final Bitmap shuffleTicket;
    private final Bitmap treasureBowl;
    private final Bitmap supremeKingFrame;
    private final Bitmap bullTable;
    private final Bitmap fortuneFrame;
    private final Bitmap goldToad;

    private MembershipDailyGiftRewardIconSet(Resources resources) {
        coin = load(resources, R.drawable.membership_reward_coin);
        recorder = load(resources, R.drawable.membership_reward_game_card);
        shuffleTicket = load(resources, R.drawable.membership_reward_shuffle_ticket);
        treasureBowl = load(resources, R.drawable.membership_reward_treasure_bowl);
        supremeKingFrame = load(resources, R.drawable.membership_reward_supreme_king_frame);
        bullTable = load(resources, R.drawable.membership_reward_bull_table);
        fortuneFrame = load(resources, R.drawable.membership_reward_fortune_frame);
        goldToad = load(resources, R.drawable.membership_reward_gold_toad);
    }

    static MembershipDailyGiftRewardIconSet load(Resources resources) {
        return new MembershipDailyGiftRewardIconSet(resources);
    }

    Bitmap iconFor(MembershipDailyGiftState.Reward reward) {
        return switch (reward.code()) {
            case "COIN" -> coin;
            case "RECORDER" -> recorder;
            case "SHUFFLE_TICKET" -> shuffleTicket;
            case "TREASURE_BOWL" -> treasureBowl;
            case "SUPREME_KING_FRAME" -> supremeKingFrame;
            case "BULL_TABLE" -> bullTable;
            case "FORTUNE_FRAME" -> fortuneFrame;
            case "GOLD_TOAD" -> goldToad;
            default -> iconForKey(reward.iconKey());
        };
    }

    private Bitmap iconForKey(String iconKey) {
        return switch (iconKey) {
            case "membership_reward_game_card" -> recorder;
            case "membership_reward_shuffle_ticket" -> shuffleTicket;
            case "membership_reward_entry_ticket" -> shuffleTicket;
            case "membership_reward_treasure_bowl" -> treasureBowl;
            case "membership_reward_supreme_king_frame" -> supremeKingFrame;
            case "membership_reward_bull_table" -> bullTable;
            case "membership_reward_fortune_frame" -> fortuneFrame;
            case "membership_reward_gold_toad" -> goldToad;
            default -> coin;
        };
    }

    private static Bitmap load(Resources resources, int resourceId) {
        return BitmapFactory.decodeResource(resources, resourceId);
    }
}
