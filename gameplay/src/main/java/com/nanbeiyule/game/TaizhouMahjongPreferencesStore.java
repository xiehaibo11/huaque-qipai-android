package com.nanbeiyule.game;

import android.content.Context;
import android.content.SharedPreferences;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayGesture;

/** Persists the original area-specific Mahjong settings without session data. */
final class TaizhouMahjongPreferencesStore {
    private static final String PREFERENCES_NAME = "taizhou_mahjong_preferences";
    private static final String DIALECT_ENABLED = "dialect_enabled";
    private static final String PLAY_MODE = "play_mode";
    // 原版存档键 HAVE_TING（SettingData SaveKeyTab.ting）。
    private static final String TING_HINT_ENABLED = "ting_hint_enabled";
    // 原版存档键 MAH_IS_HAVE_TRACE（SettingData SaveKeyTab.MahIsHaveTrace）。
    private static final String TRACE_ENABLED = "trace_enabled";
    // 原版纯净模式 ClearModel（SettingData configTab.ClearModel，EVENT_CLEAR_MODEL）。
    private static final String PURE_MODE_ENABLED = "pure_mode_enabled";

    private final SharedPreferences preferences;

    TaizhouMahjongPreferencesStore(Context context) {
        preferences =
                context.getApplicationContext()
                        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    TaizhouMahjongPreferences load() {
        TaizhouMahjongPreferences defaults = TaizhouMahjongPreferences.defaults();
        TaizhouMahjongPlayGesture.Mode mode;
        try {
            mode = TaizhouMahjongPlayGesture.Mode.valueOf(
                    preferences.getString(PLAY_MODE, defaults.playMode().name()));
        } catch (IllegalArgumentException | NullPointerException ignored) {
            mode = defaults.playMode();
        }
        return new TaizhouMahjongPreferences(
                preferences.getBoolean(DIALECT_ENABLED, defaults.dialectEnabled()),
                mode,
                preferences.getBoolean(TING_HINT_ENABLED, defaults.tingHintEnabled()),
                preferences.getBoolean(TRACE_ENABLED, defaults.traceEnabled()),
                preferences.getBoolean(PURE_MODE_ENABLED, defaults.pureModeEnabled()));
    }

    void save(TaizhouMahjongPreferences value) {
        preferences.edit()
                .putBoolean(DIALECT_ENABLED, value.dialectEnabled())
                .putString(PLAY_MODE, value.playMode().name())
                .putBoolean(TING_HINT_ENABLED, value.tingHintEnabled())
                .putBoolean(TRACE_ENABLED, value.traceEnabled())
                .putBoolean(PURE_MODE_ENABLED, value.pureModeEnabled())
                .apply();
    }
}
