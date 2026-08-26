package com.nanbeiyule.game.gameplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class GameplayApiProtocol {
    private GameplayApiProtocol() {}

    public static GameplaySnapshot parseSnapshot(String response) throws JSONException {
        JSONObject body = new JSONObject(response);
        JSONArray sourceSeats = body.getJSONArray("seats");
        List<GameplaySeat> seats = new ArrayList<>(sourceSeats.length());
        for (int index = 0; index < sourceSeats.length(); index++) {
            JSONObject seat = sourceSeats.getJSONObject(index);
            seats.add(
                    new GameplaySeat(
                            seat.getInt("seatNumber"),
                            seat.getString("userId"),
                            seat.getLong("publicPlayerId"),
                            seat.getString("displayName"),
                            seat.getString("avatarKey"),
                            seat.getLong("score"),
                            seat.getBoolean("host"),
                            seat.getBoolean("ready"),
                            seat.getBoolean("connected")));
        }
        return new GameplaySnapshot(
                body.getString("sessionId"),
                body.getString("roomNumber"),
                body.getLong("gameId"),
                body.optInt("roomMode", 0),
                body.optString("roomVenue", ""),
                GameplayPhase.valueOf(body.getString("phase")),
                body.getInt("roundNumber"),
                body.getLong("revision"),
                body.getInt("chairCount"),
                body.getInt("maxPlayCount"),
                body.getString("gameRuleDisplay"),
                body.getBoolean("autoReady"),
                body.getInt("mySeat"),
                seats,
                GameplayRoundProtocol.parseOptionalVisibleRound(body.optJSONObject("visibleRound")),
                GameplayRoundProtocol.parseOptionalPlayPermission(body.optJSONObject("playPermission")),
                GameplayRoundProtocol.parseOptionalSettlement(body.optJSONObject("settlement")),
                GameplayRoundProtocol.parseOptionalMultipleChoice(body.optJSONObject("multipleChoice")),
                GameplayRoundProtocol.optionalActiveSeat(body),
                GameplayRoundProtocol.optionalNonNegativeCount(body, "clockRemainingSeconds"),
                body.optInt("remainingWallCount", -1),
                body.getString("updatedAt"),
                GameplayRoundProtocol.parseOptionalActionOffer(body.optJSONObject("actionOffer")),
                GameplayRoundProtocol.parseOptionalMelds(body.optJSONArray("melds")),
                GameplayRoundProtocol.parseOptionalFlowers(body.optJSONArray("flowers")),
                GameplayRoundProtocol.parseOptionalTingInfo(body.optJSONObject("tingInfo")),
                GameplayRoundProtocol.optionalNonNegativeCount(body, "shengPaiCount"),
                GameplayRoundProtocol.optionalNonNegativeCount(body, "leftBankerCount"),
                GameplayRoundProtocol.parseOptionalDiceRoll(body.optJSONObject("diceRoll")),
                GameplayRoundProtocol.parseOptionalTotalResult(body.optJSONObject("totalResult")),
                body.has("wuLongRound") && !body.isNull("wuLongRound")
                        ? java.util.Optional.of(WuLongRound.parse(body.getJSONObject("wuLongRound")))
                        : java.util.Optional.empty(),
                parseChengBaoFlags(body));
    }

    private static Map<Integer, Boolean> parseChengBaoFlags(JSONObject body)
            throws JSONException {
        if (!body.has("chengBaoFlagsBySeat") || body.isNull("chengBaoFlagsBySeat")) {
            return Map.of();
        }
        JSONObject source = body.getJSONObject("chengBaoFlagsBySeat");
        int chairCount = body.getInt("chairCount");
        Map<Integer, Boolean> flags = new HashMap<>();
        for (Iterator<String> keys = source.keys(); keys.hasNext(); ) {
            String key = keys.next();
            int seat;
            try {
                seat = Integer.parseInt(key);
            } catch (NumberFormatException exception) {
                throw new JSONException("invalid chengBao seat: " + key);
            }
            if (seat <= 0 || seat > chairCount) {
                throw new JSONException("chengBao seat is outside chairCount: " + key);
            }
            Object value = source.get(key);
            if (value instanceof Boolean flag) {
                flags.put(seat, flag);
            } else if (value instanceof Number number
                    && (number.doubleValue() == 0d || number.doubleValue() == 1d)) {
                flags.put(seat, number.doubleValue() == 1d);
            } else {
                throw new JSONException("invalid chengBao flag for seat " + key);
            }
        }
        return Map.copyOf(flags);
    }

    public static List<GameplayEvent> parseEvents(String response) throws JSONException {
        return parseEventArray(new JSONArray(response));
    }

    private static List<GameplayEvent> parseEventArray(JSONArray bodies) throws JSONException {
        List<GameplayEvent> events = new ArrayList<>(bodies.length());
        for (int index = 0; index < bodies.length(); index++) {
            JSONObject body = bodies.getJSONObject(index);
            events.add(
                    new GameplayEvent(
                            body.getString("sessionId"),
                            body.getLong("revision"),
                            body.getInt("eventOrder"),
                            body.getString("type"),
                            body.getJSONObject("payload")));
        }
        return List.copyOf(events);
    }

    public static JSONObject commandBody(String type, long expectedRevision)
            throws JSONException {
        return commandBody(type, expectedRevision, null);
    }

    public static JSONObject commandBody(String type, long expectedRevision, JSONObject payload)
            throws JSONException {
        if (type == null || type.isBlank() || expectedRevision < 0) {
            throw new IllegalArgumentException("invalid gameplay command");
        }
        JSONObject body =
                new JSONObject()
                        .put("type", type)
                        .put("expectedRevision", expectedRevision);
        if (payload != null) {
            body.put("payload", payload);
        }
        return body;
    }

    /**
     * 解析命令应答。{@code events} 是服务端随应答下发的、对本座位可见的权威事件；
     * 旧版本后端没有该字段时按空列表处理，客户端退回 {@code GET /events} 恢复。
     */
    public static GameplayCommandResult parseCommandResult(String response)
            throws JSONException {
        JSONObject body = new JSONObject(response);
        JSONArray events = body.optJSONArray("events");
        return new GameplayCommandResult(
                body.getLong("revision"),
                body.getString("eventType"),
                body.getInt("seatNumber"),
                body.getBoolean("ready"),
                body.getBoolean("replayed"),
                events == null ? List.of() : parseEventArray(events));
    }


}
