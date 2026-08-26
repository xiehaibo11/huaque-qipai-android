package com.nanbeiyule.game;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 玩家当前所在的未结束房间，即原版的「返场坐标」。
 *
 * <p>对标原版 {@code PlayerData:setPlayerPosition} 保留的字段（{@code
 * artifacts/zhejiang_game_lobby_1.5.4/recovered/lua/hotfix-source/app/Data/PlayerData.lua:665}）。
 * 原版以 {@code position.gameID == 0} 表示「不在任何房间」，大厅的建房/返场入口、加入房间入口都读
 * 这一个判断（{@code lobby/Modules/Lobby/View.lua:725}、{@code :794}、{@code :862}）。
 *
 * <p>南北娱乐后端没有原版的 appid / srsgroupid / 桌号 / 椅号拓扑，因此只保留语义等价的房间定位
 * 字段，并沿用 {@code gameId == 0} 约定。
 */
record RoomPlacement(
        boolean inRoom,
        String roomNumber,
        long lobbyId,
        long gameId,
        String gameRuleDisplay,
        int playerCount,
        int playCount,
        boolean owner) {

    /** 原版 {@code gameID == 0} 语义：玩家不在任何未结束房间里。 */
    static RoomPlacement none() {
        return new RoomPlacement(false, "", 0L, 0L, "", 0, 0, false);
    }

    static RoomPlacement fromJson(JSONObject body) throws JSONException {
        if (body == null || !body.optBoolean("inRoom", false)) {
            return none();
        }
        RoomPlacement placement =
                new RoomPlacement(
                        true,
                        body.optString("roomNumber", "").trim(),
                        body.optLong("lobbyId", 0L),
                        body.optLong("gameId", 0L),
                        body.optString("gameRuleDisplay", "").trim(),
                        body.optInt("playerCount", 0),
                        body.optInt("playCount", 0),
                        body.optBoolean("owner", false));
        // 缺定位字段的返场坐标无法进房，按原版「不在房间」处理，避免把玩家送进空房号。
        return placement.hasRoom() ? placement : none();
    }

    /** 是否是一个可以真正返场的坐标。 */
    boolean hasRoom() {
        return inRoom && gameId != 0L && hasSixDigitRoomNumber();
    }

    private boolean hasSixDigitRoomNumber() {
        if (roomNumber == null || roomNumber.length() != 6) {
            return false;
        }
        for (int index = 0; index < 6; index++) {
            if (!Character.isDigit(roomNumber.charAt(index))) {
                return false;
            }
        }
        return true;
    }
}
