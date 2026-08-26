package com.nanbeiyule.game;

import android.content.Context;
import android.content.SharedPreferences;
import com.nanbeiyule.game.TaizhouSettingStyle.Choice;
import com.nanbeiyule.game.TaizhouSettingStyle.Slider;

/** 持久化原版 {@code SettingData} 的外观配置，键名沿用 CUSTOM_STYLE 的字段名。 */
final class TaizhouSettingStyleStore {
    private static final String PREFERENCES_NAME = "taizhou_mahjong_style";
    private static final String PLAYER_TYPE = "PLAYER_TYPE";

    private final SharedPreferences preferences;

    TaizhouSettingStyleStore(Context context) {
        preferences =
                context.getApplicationContext()
                        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    TaizhouSettingStyle load() {
        TaizhouSettingStyle defaults = TaizhouSettingStyle.defaults();
        int[] choices = defaults.choices().clone();
        for (Choice choice : Choice.values()) {
            choices[choice.ordinal()] =
                    preferences.getInt(choice.name(), choices[choice.ordinal()]);
        }
        float[] sliders = defaults.sliders().clone();
        for (Slider slider : Slider.values()) {
            sliders[slider.ordinal()] =
                    preferences.getFloat(slider.name(), sliders[slider.ordinal()]);
        }
        return new TaizhouSettingStyle(
                choices, sliders, preferences.getInt(PLAYER_TYPE, defaults.playerType()));
    }

    void save(TaizhouSettingStyle style) {
        SharedPreferences.Editor editor = preferences.edit();
        for (Choice choice : Choice.values()) {
            editor.putInt(choice.name(), style.value(choice));
        }
        for (Slider slider : Slider.values()) {
            editor.putFloat(slider.name(), style.value(slider));
        }
        editor.putInt(PLAYER_TYPE, style.playerType()).apply();
    }
}
