package com.nanbeiyule.game;

import com.nanbeiyule.game.TaizhouSettingStyle.Choice;
import com.nanbeiyule.game.TaizhouSettingStyle.Slider;
import java.util.EnumMap;
import java.util.Map;

/** {@code BasicMahjong/Config/MahSettingConfig.lua} 的 {@code tab[7109]} 逐值副本。 */
record TaizhouSettingArea7109Catalog(
        Map<Choice, int[]> options,
        Map<Choice, Integer> defaults,
        Map<Slider, Float> sliderDefaults,
        String[] planLabels,
        int defaultPlayerType) {

    static TaizhouSettingArea7109Catalog original() {
        Map<Choice, int[]> options = new EnumMap<>(Choice.class);
        Map<Choice, Integer> defaults = new EnumMap<>(Choice.class);
        put(options, defaults, Choice.WORD_TYPE, new int[] {1, 2}, 2);
        // BACK_TYPE 的第四项是 XGSJ=6（霞光胜境），不是 BLUE=4：见 GameDefine
        // MAH_BACK_COLOR_TYPE 与 tab[7109] 的 {ORANGE, YELLOW, GREEN, XGSJ}。
        put(options, defaults, Choice.BACK_TYPE, new int[] {1, 2, 3, 6}, 3);
        put(options, defaults, Choice.BODY_TYPE, new int[] {1, 2}, 1);
        put(options, defaults, Choice.FACE_TYPE, new int[] {1, 2}, 1);
        put(options, defaults, Choice.TABLE_STYLE, new int[] {7, 1, 2, 3, 4, 5}, 2);
        put(options, defaults, Choice.OUT_MOVE_STYLE, new int[] {1, 2}, 2);
        put(options, defaults, Choice.INSERT_STYLE, new int[] {1, 2}, 2);
        put(options, defaults, Choice.OUT_STYLE, new int[] {1, 2}, 2);
        put(options, defaults, Choice.OUT_EFFECTS, new int[] {1, 2}, 1);
        put(options, defaults, Choice.HAND_STYLE, new int[] {1, 2}, 2);
        put(options, defaults, Choice.HAND_SORT_STYLE, new int[] {1, 2}, 1);
        put(options, defaults, Choice.OUT_TABLE_CARD_STYLE, new int[] {1, 2}, 1);

        Map<Slider, Float> sliderDefaults = new EnumMap<>(Slider.class);
        sliderDefaults.put(Slider.CARD_HEIGHT, 0.0f);
        sliderDefaults.put(Slider.CARD_WIDTH, 0.2f);
        sliderDefaults.put(Slider.CARD_WORD_SIZE, 1.0f);

        return new TaizhouSettingArea7109Catalog(
                options, defaults, sliderDefaults, new String[] {"经典方案"}, 1);
    }

    private static void put(
            Map<Choice, int[]> options,
            Map<Choice, Integer> defaults,
            Choice choice,
            int[] values,
            int normal) {
        options.put(choice, values);
        defaults.put(choice, normal);
    }

    int[] options(Choice choice) {
        return options.get(choice);
    }

    int defaultValue(Choice choice) {
        return defaults.get(choice);
    }

    float defaultValue(Slider slider) {
        return sliderDefaults.get(slider);
    }

    /** 按钮下标（0 起）→ 真实枚举值。 */
    int realValue(Choice choice, int index) {
        int[] values = options(choice);
        return index >= 0 && index < values.length ? values[index] : values[0];
    }

    /** 真实枚举值 → 按钮下标（0 起），找不到时回落到第一项。 */
    int localIndex(Choice choice, int realValue) {
        int[] values = options(choice);
        for (int index = 0; index < values.length; index++) {
            if (values[index] == realValue) {
                return index;
            }
        }
        return 0;
    }

    /** 自定义方案占用运营方案之后的那一格（{@code View.lua:1101-1102}）。 */
    int customPlanIndex() {
        return planLabels.length;
    }
}
