package com.nanbeiyule.game;

import com.nanbeiyule.game.mahjong.MahjongTileAppearance;
import java.util.Arrays;

/**
 * 原版 {@code SettingData} 的自定义外观配置（{@code MahSettingConfig.lua} 的 CUSTOM_STYLE）。
 *
 * <p>枚举值都是「真实值」，不是按钮下标；下标与真实值的换算沿用
 * {@code View.lua:realIndexSwitchToLocalIndex / localIndexSwitchToRealIndex}，
 * 由 {@link TaizhouSettingArea7109Catalog} 提供每一项的可选列表。
 */
record TaizhouSettingStyle(int[] choices, float[] sliders, int playerType) {

    /** {@code KW_TEXTUTRE_LIST} 里按真实值取图的那几组。 */
    enum Choice {
        WORD_TYPE,
        BACK_TYPE,
        BODY_TYPE,
        FACE_TYPE,
        TABLE_STYLE,
        OUT_MOVE_STYLE,
        INSERT_STYLE,
        OUT_STYLE,
        OUT_EFFECTS,
        HAND_STYLE,
        HAND_SORT_STYLE,
        OUT_TABLE_CARD_STYLE
    }

    /** 0~1 的滑条项。 */
    enum Slider {
        CARD_HEIGHT,
        CARD_WIDTH,
        CARD_WORD_SIZE
    }

    static TaizhouSettingStyle defaults() {
        TaizhouSettingArea7109Catalog catalog = TaizhouSettingArea7109Catalog.original();
        int[] choices = new int[Choice.values().length];
        for (Choice choice : Choice.values()) {
            choices[choice.ordinal()] = catalog.defaultValue(choice);
        }
        float[] sliders = new float[Slider.values().length];
        for (Slider slider : Slider.values()) {
            sliders[slider.ordinal()] = catalog.defaultValue(slider);
        }
        return new TaizhouSettingStyle(choices, sliders, catalog.defaultPlayerType());
    }

    /** {@code View.lua:switchToNormalKey}：设置项键名到 UIMah 配置键的逐项映射。 */
    MahjongTileAppearance appearance() {
        return new MahjongTileAppearance(
                value(Choice.BODY_TYPE),
                value(Choice.FACE_TYPE),
                value(Choice.BACK_TYPE),
                value(Choice.WORD_TYPE),
                value(Slider.CARD_HEIGHT),
                value(Slider.CARD_WIDTH),
                value(Slider.CARD_WORD_SIZE));
    }

    int value(Choice choice) {
        return choices[choice.ordinal()];
    }

    float value(Slider slider) {
        return sliders[slider.ordinal()];
    }

    TaizhouSettingStyle with(Choice choice, int realValue) {
        int[] next = choices.clone();
        next[choice.ordinal()] = realValue;
        // 原版 setCustomStyle 之后立即跳到「自定义」方案（jumpToCustomBtn）。
        return new TaizhouSettingStyle(next, sliders.clone(), 0);
    }

    TaizhouSettingStyle with(Slider slider, float percent) {
        float[] next = sliders.clone();
        next[slider.ordinal()] = Math.max(0.0f, Math.min(1.0f, percent));
        return new TaizhouSettingStyle(choices.clone(), next, 0);
    }

    /** 切换到某个运营方案（{@code setPlayerType}）。 */
    static TaizhouSettingStyle ofPlan(int playerType) {
        TaizhouSettingStyle defaults = defaults();
        return new TaizhouSettingStyle(defaults.choices(), defaults.sliders(), playerType);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TaizhouSettingStyle style
                && playerType == style.playerType
                && Arrays.equals(choices, style.choices)
                && Arrays.equals(sliders, style.sliders);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(choices) * 31 + Arrays.hashCode(sliders) + playerType;
    }
}
