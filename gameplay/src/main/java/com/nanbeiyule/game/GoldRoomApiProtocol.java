package com.nanbeiyule.game;

import com.nanbeiyule.game.goldroom.GoldRoomConf;
import com.nanbeiyule.game.goldroom.GoldRoomLevel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Parses the first-party gold-room catalog responses. */
final class GoldRoomApiProtocol {
    private GoldRoomApiProtocol() {}

    static GoldRoomConf confFromJson(JSONObject payload) throws JSONException {
        JSONObject game = payload.getJSONObject("game");
        JSONArray levelArray = payload.optJSONArray("levels");
        List<GoldRoomLevel> levels = new ArrayList<>();
        if (levelArray != null) {
            for (int index = 0; index < levelArray.length(); index++) {
                levels.add(levelFromJson(levelArray.getJSONObject(index)));
            }
        }
        return new GoldRoomConf(
                game.getLong("lobbyId"),
                game.getLong("gameId"),
                game.optString("displayName", ""),
                game.optLong("boxGameId", 0L),
                game.optInt("chairCount", 0),
                Collections.unmodifiableList(levels),
                payload.optBoolean("showsPlayerCount", false));
    }

    static JSONObject joinBody(long lobbyId, int roomNameFlag) throws JSONException {
        return new JSONObject().put("lobbyId", lobbyId).put("roomNameFlag", roomNameFlag);
    }

    static JSONObject leaveBody(long lobbyId, int roomNameFlag) throws JSONException {
        return new JSONObject().put("lobbyId", lobbyId).put("roomNameFlag", roomNameFlag);
    }

    static GoldRoomJoinResponse joinFromJson(JSONObject body) throws JSONException {
        GoldRoomJoinResponse response =
                new GoldRoomJoinResponse(
                        body.getString("code"),
                        body.getString("status"),
                        body.getString("roomMode"),
                        body.getLong("lobbyId"),
                        body.getLong("gameId"),
                        body.optLong("boxGameId", 0L),
                        body.getInt("roomNameFlag"),
                        body.getInt("sessionId"),
                        body.getInt("chairCount"),
                        body.getLong("baseScore"),
                        body.optBoolean("dynamicCost", false),
                        body.getLong("minRich"),
                        body.getLong("maxRich"),
                        body.getString("matchingTicketId"),
                        body.optString("message", "正在匹配玩家..."),
                        optionalText(body, "roomNumber"),
                        body.optBoolean("autoGameplay", false),
                        body.optBoolean("replay", false));
        if (response.matchingTicketId().isBlank()) {
            throw new JSONException("matchingTicketId must not be blank");
        }
        if (response.autoGameplay()
                && (response.roomNumber() == null || !response.roomNumber().matches("\\d{6}"))) {
            throw new JSONException("QA auto gameplay requires roomNumber");
        }
        return response;
    }

    private static GoldRoomLevel levelFromJson(JSONObject level) throws JSONException {
        return new GoldRoomLevel(
                level.getInt("roomNameFlag"),
                level.getInt("uiType"),
                level.optInt("chairCount", 0),
                level.getLong("baseScore"),
                level.optBoolean("dynamicCost", false),
                level.getLong("minRich"),
                level.getLong("maxRich"),
                level.optLong("onlineCount", 0L),
                optionalText(level, "tagLeftTop"),
                optionalText(level, "tagRightTop"),
                optionalText(level, "tagRibbon1"),
                optionalText(level, "tagRibbon2"));
    }

    /** The backend omits unset tags entirely (non_null serialisation), so absence means hidden. */
    private static String optionalText(JSONObject level, String key) {
        if (!level.has(key) || level.isNull(key)) {
            return null;
        }
        String value = level.optString(key, "");
        return value.isEmpty() ? null : value;
    }
}
