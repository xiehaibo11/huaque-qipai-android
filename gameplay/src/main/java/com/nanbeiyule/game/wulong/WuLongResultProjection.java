package com.nanbeiyule.game.wulong;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/** Server-persisted 30588 result values mapped to the recovered local-seat arrangement. */
final class WuLongResultProjection {
    record SeatResult(int serverSeat, int localSeat, int finishIndex, long finalScore) {}

    private WuLongResultProjection() {}

    static List<SeatResult> from(JSONObject result, List<Integer> finishOrder, int mySeat) {
        JSONObject scores = result == null ? null : result.optJSONObject("finalScores");
        List<SeatResult> values = new ArrayList<>();
        for (int index = 0; index < finishOrder.size(); index++) {
            int serverSeat = finishOrder.get(index);
            long score = scores == null ? 0L : scores.optLong(Integer.toString(serverSeat), 0L);
            values.add(new SeatResult(serverSeat, WuLongTableLayout.localSeatFor(serverSeat, mySeat),
                    index, score));
        }
        return List.copyOf(values);
    }
}
