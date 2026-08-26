package com.nanbeiyule.game.gameplay;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Seat-private/public 30588 payload. Opponent hands are deliberately not a client schema field. */
public record WuLongRound(
        List<Integer> hand,
        Map<Integer, Integer> cardCounts,
        Integer activeSeat,
        Integer clockRemainingSeconds,
        List<Integer> lastPlay,
        Integer lastPlaySeat,
        int deskScore,
        List<Integer> finishOrder,
        JSONObject result,
        String turnTimeoutPolicy,
        String turnTimeoutStatus) {
    static WuLongRound parse(JSONObject value) throws JSONException {
        Map<Integer, Integer> counts = new LinkedHashMap<>();
        JSONObject sourceCounts = value.optJSONObject("cardCounts");
        if (sourceCounts != null) {
            for (int seat = 1; seat <= 4; seat++) counts.put(seat, sourceCounts.optInt(Integer.toString(seat)));
        }
        return new WuLongRound(
                numbers(value.optJSONArray("hand")),
                Map.copyOf(counts),
                value.has("activeSeat") ? value.getInt("activeSeat") : null,
                value.has("clockRemainingSeconds") ? value.getInt("clockRemainingSeconds") : null,
                numbers(value.optJSONArray("lastPlay")),
                value.has("lastPlaySeat") ? value.getInt("lastPlaySeat") : null,
                value.optInt("deskScore", 0),
                numbers(value.optJSONArray("finishOrder")),
                value.optJSONObject("result"),
                value.optString("turnTimeoutPolicy", ""),
                value.optString("turnTimeoutStatus", ""));
    }

    private static List<Integer> numbers(JSONArray values) {
        List<Integer> result = new ArrayList<>();
        if (values != null) for (int index = 0; index < values.length(); index++) result.add(values.optInt(index));
        return List.copyOf(result);
    }
}
