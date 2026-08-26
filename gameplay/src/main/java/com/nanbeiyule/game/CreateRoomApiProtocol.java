package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class CreateRoomApiProtocol {
    private CreateRoomApiProtocol() {}

    static List<CreateRoomGame> parseGames(String text) throws JSONException {
        String normalized = text == null ? "" : text.trim();
        JSONArray bodies;
        if (normalized.startsWith("[")) {
            bodies = new JSONArray(normalized);
        } else {
            JSONObject wrapper = new JSONObject(normalized);
            bodies = wrapper.optJSONArray("games");
            if (bodies == null) {
                bodies = wrapper.getJSONArray("items");
            }
        }
        List<CreateRoomGame> games = new ArrayList<>();
        for (int index = 0; index < bodies.length(); index++) {
            JSONObject body = bodies.getJSONObject(index);
            long gameId = body.getLong("gameId");
            String displayName = body.getString("displayName").trim();
            if (gameId <= 0 || displayName.isBlank()) {
                throw new JSONException("Invalid room game");
            }
            games.add(
                    new CreateRoomGame(
                            gameId,
                            displayName,
                            body.optString("badge").trim(),
                            body.optInt("sortOrder", index)));
        }
        games.sort(Comparator.comparingInt(CreateRoomGame::sortOrder));
        return List.copyOf(games);
    }

    static CreateRoomRuleConfig parseRuleConfig(String text) throws JSONException {
        return CreateRoomRuleConfig.fromJson(new JSONObject(text));
    }

    static JSONObject createBody(
            long lobbyId,
            long gameId,
            int categoryIndex,
            List<String> selectedNodeNames) throws JSONException {
        return new JSONObject()
                .put("lobbyId", lobbyId)
                .put("gameId", gameId)
                .put("categoryIndex", categoryIndex)
                .put("selectedNodeNames", new JSONArray(selectedNodeNames));
    }

    /** 解析 {@code GET /api/v1/rooms/current}，对标原版 {@code RespPlayerPosition}。 */
    static RoomPlacement parsePlacement(String text) throws JSONException {
        return RoomPlacement.fromJson(new JSONObject(text == null ? "" : text));
    }

    /**
     * 从 Problem Details 错误体里取出返场坐标。
     *
     * <p>原版建房失败响应本身就带房间定位字段，客户端据此返场而不是停在建房页；无法解析时返回
     * {@link RoomPlacement#none()}，调用方退回普通错误提示。
     */
    static RoomPlacement parseErrorPlacement(String text) {
        try {
            JSONObject error = new JSONObject(text == null ? "" : text);
            return RoomPlacement.fromJson(error.optJSONObject("placement"));
        } catch (JSONException ignored) {
            return RoomPlacement.none();
        }
    }

    static CreateRoomResult parseCreateResult(String text) throws JSONException {
        JSONObject body = new JSONObject(text);
        CreateRoomResult result =
                new CreateRoomResult(
                        body.get("roomNumber").toString(),
                        body.getString("status"),
                        body.getLong("gameId"),
                        body.optString("gameRule"),
                        body.optString("roomRule"),
                        body.optInt("roomMode", 0),
                        body.getInt("playerCount"),
                        body.getInt("playCount"),
                        body.getString("payType"),
                        body.getLong("roomFeeCenti"));
        if (!result.hasSixDigitRoomNumber()) {
            throw new JSONException("roomNumber must contain exactly six digits");
        }
        return result;
    }
}
