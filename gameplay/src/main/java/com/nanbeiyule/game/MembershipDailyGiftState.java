package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

record MembershipDailyGiftState(
        boolean membershipActive,
        boolean claimedToday,
        int claimedGiftId,
        String serverDate,
        String claimedAt,
        List<Option> options,
        Wallet wallet) {
    record Option(int giftId, String title, String buttonStyle, List<Reward> rewards) {}

    record Reward(
            String code,
            String displayName,
            long quantity,
            String subtitle,
            String iconKey,
            int durationDays) {}

    record Wallet(long roomCards, long boundRoomCards, long coins, long diamonds) {}

    MembershipDailyGiftState {
        options = List.copyOf(options);
    }

    static MembershipDailyGiftState fromJson(JSONObject body) throws JSONException {
        JSONArray optionBodies = body.getJSONArray("options");
        List<Option> options = new ArrayList<>(optionBodies.length());
        for (int optionIndex = 0; optionIndex < optionBodies.length(); optionIndex++) {
            JSONObject optionBody = optionBodies.getJSONObject(optionIndex);
            JSONArray rewardBodies = optionBody.getJSONArray("rewards");
            List<Reward> rewards = new ArrayList<>(rewardBodies.length());
            for (int rewardIndex = 0; rewardIndex < rewardBodies.length(); rewardIndex++) {
                JSONObject rewardBody = rewardBodies.getJSONObject(rewardIndex);
                rewards.add(
                        new Reward(
                                requiredString(rewardBody, "code"),
                                requiredString(rewardBody, "displayName"),
                                rewardBody.getLong("quantity"),
                                rewardBody.optString("subtitle", ""),
                                rewardBody.optString("iconKey", ""),
                                rewardBody.optInt("durationDays", 0)));
            }
            options.add(
                    new Option(
                            optionBody.getInt("giftId"),
                            optionBody.optString("title", ""),
                            optionBody.optString("buttonStyle", ""),
                            rewards));
        }
        JSONObject walletBody = body.optJSONObject("wallet");
        Wallet wallet =
                walletBody == null
                        ? null
                        : new Wallet(
                                walletBody.optLong("roomCards", 0L),
                                walletBody.optLong("boundRoomCards", 0L),
                                walletBody.optLong("coins", 0L),
                                walletBody.optLong("diamonds", 0L));
        return new MembershipDailyGiftState(
                body.getBoolean("membershipActive"),
                body.getBoolean("claimedToday"),
                body.optInt("claimedGiftId", 0),
                body.optString("serverDate", ""),
                body.optString("claimedAt", ""),
                options,
                wallet);
    }

    Option option(int giftId) {
        for (Option option : options) {
            if (option.giftId() == giftId) {
                return option;
            }
        }
        return null;
    }

    static Option defaultOption(int giftId) {
        if (giftId == 1) {
            return new Option(1, "", "red", List.of(
                    new Reward("COIN", "金币", 10000, "", "", 0),
                    new Reward("RECORDER", "记牌器", 5, "", "", 1),
                    new Reward("SUPREME_KING_FRAME", "至尊帝王1天", 1, "", "", 1),
                    new Reward("BULL_TABLE", "牛气冲天1天", 1, "", "", 1)));
        }
        return new Option(2, "", "green", List.of(
                new Reward("SHUFFLE_TICKET", "洗牌券", 1, "", "", 1),
                new Reward("TREASURE_BOWL", "聚宝盆", 1, "", "", 1),
                new Reward("FORTUNE_FRAME", "招财进宝1天", 1, "", "", 1),
                new Reward("GOLD_TOAD", "金蟾吞宝1天", 1, "", "", 1)));
    }

    private static String requiredString(JSONObject body, String field) throws JSONException {
        String value = body.getString(field).trim();
        if (value.isEmpty()) {
            throw new JSONException(field + " must not be blank");
        }
        return value;
    }
}
