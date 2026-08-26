package com.nanbeiyule.game;

import android.content.Context;
import com.nanbeiyule.game.gameplay.GameplayMeld;
import com.nanbeiyule.game.gameplay.GameplaySeat;
import com.nanbeiyule.game.gameplay.GameplaySnapshot;
import com.nanbeiyule.game.mahjong.TaizhouMahjongVisibleRound;
import java.util.UUID;
import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxHelper;
import org.cocos2dx.lib.Cocos2dxLuaJavaBridge;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Narrow Java boundary for the recovered Cocos/Lua table runtime. */
public final class CocosLuaBridge {
    private static final Object LOCK = new Object();
    private static volatile String roomNumber = "";
    private static AuthSessionCoordinator auth;
    private static GameplayApiClient gameplay;

    private CocosLuaBridge() {}

    public static void setRoomNumber(String value) {
        roomNumber = normalizeRoomNumber(value);
    }

    public static String getRoomNumber() {
        return roomNumber;
    }

    /** Called by Lua with a retained callback function id. */
    public static void requestSnapshot(String requestedRoom, int callbackId) {
        String targetRoom = normalizeRoomNumber(requestedRoom);
        if (!isValidRoomNumber(targetRoom)) {
            finish(callbackId, errorPayload("房间号格式不正确"));
            return;
        }
        Context context = Cocos2dxActivity.getContext();
        if (context == null) {
            finish(callbackId, errorPayload("Cocos 宿主尚未初始化"));
            return;
        }
        ensureServices(context.getApplicationContext());
        auth.<GameplaySnapshot>execute(
                (token, callback) ->
                        gameplay.loadSnapshot(token, targetRoom, transportCallback(callback)),
                new AuthSessionCoordinator.Callback<GameplaySnapshot>() {
                    @Override
                    public void onSuccess(GameplaySnapshot result) {
                        finish(callbackId, snapshotPayload(result));
                    }

                    @Override
                    public void onLoginRequired() {
                        finish(callbackId, errorPayload("登录状态已失效"));
                    }

                    @Override
                    public void onError(String message) {
                        finish(callbackId, errorPayload(message));
                    }
                });
    }

    /** Sends an authoritative table command; Lua never mutates server state locally. */
    public static void submitCommand(
            String requestedRoom,
            String type,
            String payloadJson,
            String expectedRevisionValue,
            int callbackId) {
        String targetRoom = normalizeRoomNumber(requestedRoom);
        if (!isValidRoomNumber(targetRoom) || type == null || type.isBlank()) {
            finish(callbackId, errorPayload("牌局操作参数不正确"));
            return;
        }
        Context context = Cocos2dxActivity.getContext();
        if (context == null) {
            finish(callbackId, errorPayload("Cocos 宿主尚未初始化"));
            return;
        }
        ensureServices(context.getApplicationContext());
        JSONObject payload = null;
        try {
            if (payloadJson != null && !payloadJson.isBlank()) {
                payload = new JSONObject(payloadJson);
            }
        } catch (JSONException exception) {
            finish(callbackId, errorPayload("牌局操作参数不正确"));
            return;
        }
        long expectedRevision;
        try {
            expectedRevision = Long.parseLong(expectedRevisionValue == null ? "" : expectedRevisionValue);
        } catch (NumberFormatException exception) {
            finish(callbackId, errorPayload("牌局版本号不正确"));
            return;
        }
        JSONObject commandPayload = payload;
        auth.<com.nanbeiyule.game.gameplay.GameplayCommandResult>execute(
                (token, callback) ->
                        gameplay.submitCommand(
                                token,
                                targetRoom,
                                UUID.randomUUID().toString(),
                                type,
                                expectedRevision,
                                commandPayload,
                                transportCallback(callback)),
                new AuthSessionCoordinator.Callback<com.nanbeiyule.game.gameplay.GameplayCommandResult>() {
                    @Override
                    public void onSuccess(com.nanbeiyule.game.gameplay.GameplayCommandResult result) {
                        try {
                            finish(callbackId, new JSONObject()
                                    .put("ok", true)
                                    .put("revision", result.revision())
                                    .put("eventType", result.eventType())
                                    .put("replayed", result.replayed())
                                    .toString());
                        } catch (JSONException exception) {
                            finish(callbackId, errorPayload("牌局响应格式不正确"));
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        finish(callbackId, errorPayload("登录状态已失效"));
                    }

                    @Override
                    public void onError(String message) {
                        finish(callbackId, errorPayload(message));
                    }
                });
    }

    static boolean isValidRoomNumber(String value) {
        return value != null && value.matches("\\d{6}");
    }

    private static String normalizeRoomNumber(String value) {
        return value == null ? "" : value.trim();
    }

    private static void ensureServices(Context context) {
        synchronized (LOCK) {
            if (auth == null || gameplay == null) {
                auth = new AuthSessionCoordinator(new LoginSessionStore(context), new AuthApiClient());
                gameplay = new GameplayApiClient();
            }
        }
    }

    private static <T> GameplayTransport.Callback<T> transportCallback(
            AuthSessionCoordinator.CallCallback<T> callback) {
        return new GameplayTransport.Callback<>() {
            @Override
            public void onSuccess(T result) {
                callback.onSuccess(result);
            }

            @Override
            public void onUnauthorized() {
                callback.onUnauthorized();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        };
    }

    private static String snapshotPayload(GameplaySnapshot snapshot) {
        try {
            JSONObject body = new JSONObject()
                    .put("ok", true)
                    .put("sessionId", snapshot.sessionId())
                    .put("roomNumber", snapshot.roomNumber())
                    .put("gameId", snapshot.gameId())
                    .put("phase", snapshot.phase().name())
                    .put("roundNumber", snapshot.roundNumber())
                    .put("revision", snapshot.revision())
                    .put("chairCount", snapshot.chairCount())
                    .put("mySeat", snapshot.mySeat())
                    .put("gameRuleDisplay", snapshot.gameRuleDisplay())
                    .put("remainingWallCount", snapshot.remainingWallCount())
                    .put("activeSeat", snapshot.activeSeat() == null
                            ? JSONObject.NULL : snapshot.activeSeat());
            JSONArray seats = new JSONArray();
            for (GameplaySeat seat : snapshot.seats()) {
                seats.put(new JSONObject()
                        .put("seatNumber", seat.seatNumber())
                        .put("displayName", seat.displayName())
                        .put("avatarKey", seat.avatarKey())
                        .put("score", seat.score())
                        .put("host", seat.host())
                        .put("ready", seat.ready())
                        .put("connected", seat.connected()));
            }
            body.put("seats", seats);
            JSONArray melds = new JSONArray();
            for (GameplayMeld meld : snapshot.melds()) {
                melds.put(new JSONObject()
                        .put("seat", meld.seat())
                        .put("combType", meld.combType().name())
                        .put("tiles", integers(meld.tiles()))
                        .put("fromSeat", meld.fromSeat()));
            }
            body.put("melds", melds);
            snapshot.visibleRound().ifPresent(round -> putVisibleRound(body, round));
            return body.toString();
        } catch (JSONException exception) {
            return errorPayload("牌局数据格式不正确");
        }
    }

    private static void putVisibleRound(JSONObject body, TaizhouMahjongVisibleRound round) {
        try {
            JSONObject visible = new JSONObject()
                    .put("chairCount", round.chairCount())
                    .put("mySeat", round.mySeat())
                    .put("jokerTiles", integers(round.jokerTiles()))
                    .put("insteadTiles", integers(round.insteadTiles()));
            JSONArray hands = new JSONArray();
            for (TaizhouMahjongVisibleRound.SeatHand hand : round.hands()) {
                hands.put(new JSONObject()
                        .put("seatNumber", hand.seatNumber())
                        .put("concealedTiles", integers(hand.concealedTiles()))
                        .put("drawnTile", hand.drawnTile() == null
                                ? JSONObject.NULL : hand.drawnTile())
                        .put("meldCount", hand.meldCount()));
            }
            visible.put("hands", hands);
            JSONArray rivers = new JSONArray();
            for (TaizhouMahjongVisibleRound.SeatRiver river : round.rivers()) {
                rivers.put(new JSONObject()
                        .put("seatNumber", river.seatNumber())
                        .put("tiles", integers(river.tiles()))
                        .put("maxLineCount", river.maxLineCount()));
            }
            visible.put("rivers", rivers);
            if (round.lastDiscard() != null) {
                visible.put("lastDiscard", new JSONObject()
                        .put("seatNumber", round.lastDiscard().seatNumber())
                        .put("tileIndex", round.lastDiscard().tileIndex()));
            }
            body.put("visibleRound", visible);
        } catch (JSONException exception) {
            throw new IllegalStateException("visible round cannot be serialized", exception);
        }
    }

    private static JSONArray integers(Iterable<Integer> values) {
        JSONArray result = new JSONArray();
        for (Integer value : values) {
            result.put(value);
        }
        return result;
    }

    private static String errorPayload(String message) {
        try {
            return new JSONObject().put("ok", false)
                    .put("error", message == null ? "" : message).toString();
        } catch (JSONException impossible) {
            return "{\"ok\":false,\"error\":\"bridge failure\"}";
        }
    }

    private static void finish(int callbackId, String payload) {
        Cocos2dxHelper.runOnGLThread(() -> {
            Cocos2dxLuaJavaBridge.callLuaFunctionWithString(callbackId, payload);
            Cocos2dxLuaJavaBridge.releaseLuaFunction(callbackId);
        });
    }
}
