package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class MatchArenaProtocol {
    private MatchArenaProtocol() {}

    static MatchArenaSummary parseSummary(JSONObject json) {
        return new MatchArenaSummary(
                json.optString("id"),
                json.optString("arenaNumber"),
                json.optLong("lobbyId"),
                json.optString("areaName"),
                json.optString("remark"),
                json.optString("level"),
                json.optString("mode"),
                json.optString("costType"),
                json.optString("role"),
                json.optLong("ownerPublicPlayerId"),
                json.optString("ownerNickname"),
                json.optString("ownerAvatarKey"),
                json.optLong("roomCards"),
                json.optLong("dailyRoomCardLimit"),
                json.optString("status"),
                json.optInt("memberCount"),
                json.optInt("onlineCount"),
                json.optBoolean("duplicate"));
    }

    static List<MatchArenaSummary> parseList(String body) throws JSONException {
        JSONArray items = new JSONObject(body).getJSONArray("items");
        List<MatchArenaSummary> result = new ArrayList<>(items.length());
        for (int index = 0; index < items.length(); index++) {
            result.add(parseSummary(items.getJSONObject(index)));
        }
        return List.copyOf(result);
    }

    static String createBody(long lobbyId, MatchArenaCreateState state) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("lobbyId", lobbyId);
        body.put("remark", state.remark().trim());
        body.put("level", "LEGACY");
        body.put("mode", state.mode().name());
        body.put("costType", state.costType().name());
        body.put("initialRoomCards", state.initialRoomCardsValue());
        body.put("dailyRoomCardLimit", state.dailyRoomCardLimitValue());
        body.put("visibleToStrangers", state.visibleToStrangers());
        body.put("autoTransferEnabled", state.autoTransferEnabled());
        body.put("autoTransferThreshold", state.autoTransferThreshold());
        body.put("autoTransferAmount", state.autoTransferAmount());
        body.put(
                "lowCardReminderThreshold",
                state.lowCardReminderThreshold() == null
                        ? JSONObject.NULL
                        : state.lowCardReminderThreshold());
        return body.toString();
    }
}
