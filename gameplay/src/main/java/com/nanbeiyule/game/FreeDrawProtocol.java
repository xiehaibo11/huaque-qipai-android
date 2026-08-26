package com.nanbeiyule.game;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class FreeDrawProtocol {
    private FreeDrawProtocol() {}

    static FreeDrawState parseState(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        JSONArray array = root.getJSONArray("prizes");
        List<FreeDrawState.Prize> prizes = new ArrayList<>(array.length());
        for (int index = 0; index < array.length(); index++) {
            prizes.add(prize(array.getJSONObject(index)));
        }
        return new FreeDrawState(
                root.getString("activityCode"),
                root.getString("adPlacementId"),
                root.getInt("dailyLimit"),
                root.getInt("completedDraws"),
                root.getInt("remainingDraws"),
                prizes,
                Instant.parse(root.getString("serverTime")));
    }

    static FreeDrawSession parseSession(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        return new FreeDrawSession(
                root.getString("sessionId"),
                root.getString("userCustomData"),
                root.getString("adPlacementId"),
                Instant.parse(root.getString("expiresAt")));
    }

    static FreeDrawResult parseResult(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        JSONObject wallet = root.getJSONObject("wallet");
        return new FreeDrawResult(
                root.getString("sessionId"),
                root.getBoolean("replayed"),
                prize(root.getJSONObject("reward")),
                root.getInt("remainingDraws"),
                new FreeDrawResult.Wallet(
                        wallet.getLong("roomCards"),
                        wallet.getLong("boundRoomCards"),
                        wallet.getLong("coins"),
                        wallet.getLong("diamonds")));
    }

    private static FreeDrawState.Prize prize(JSONObject value) throws JSONException {
        return new FreeDrawState.Prize(
                value.getString("prizeId"),
                value.getString("type"),
                value.getLong("amount"),
                value.getString("displayName"),
                value.getString("iconKey"));
    }
}
