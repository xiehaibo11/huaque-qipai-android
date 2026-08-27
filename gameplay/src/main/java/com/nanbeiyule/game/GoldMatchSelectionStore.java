package com.nanbeiyule.game;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 记住上一次进入的金币场档位，供结算后「重新匹配队友」原地重排。
 *
 * <p>原版 {@code GameManager:onReqPlayerPlace} 点确定后走
 * {@code CenterBtns:onStartGameEvent()}，用房间自带的 50 模式参数重新入队；进程被杀之后
 * 这些参数只能从本地取回，所以 join 成功时落一份。
 */
final class GoldMatchSelectionStore {
    private static final String PREFERENCES_NAME = "gold_match_selection";
    private static final String LOBBY_ID = "lobbyId";
    private static final String GAME_ID = "gameId";
    private static final String ROOM_NAME_FLAG = "roomNameFlag";

    record Selection(long lobbyId, long gameId, int roomNameFlag) {
        boolean valid() {
            return lobbyId > 0 && gameId > 0 && roomNameFlag > 0;
        }
    }

    private final SharedPreferences preferences;

    GoldMatchSelectionStore(Context context) {
        preferences =
                context.getApplicationContext()
                        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    void save(long lobbyId, long gameId, int roomNameFlag) {
        preferences
                .edit()
                .putLong(LOBBY_ID, lobbyId)
                .putLong(GAME_ID, gameId)
                .putInt(ROOM_NAME_FLAG, roomNameFlag)
                .apply();
    }

    Selection load() {
        return new Selection(
                preferences.getLong(LOBBY_ID, 0L),
                preferences.getLong(GAME_ID, 0L),
                preferences.getInt(ROOM_NAME_FLAG, 0));
    }
}
