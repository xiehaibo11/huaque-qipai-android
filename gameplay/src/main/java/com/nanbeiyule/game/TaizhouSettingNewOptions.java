package com.nanbeiyule.game;

import com.nanbeiyule.game.TaizhouSettingNewDetailLayout.Option;
import com.nanbeiyule.game.TaizhouSettingNewLayout.Page;
import com.nanbeiyule.game.TaizhouSettingStyle.Choice;

/**
 * 每个详情页管辖的选项组，取自 {@code View.lua:initCurrLayer} 的 {@code controlNames}，
 * 以及每一组第 index 个按钮的几何。
 */
final class TaizhouSettingNewOptions {
    private static final Choice[] NONE = {};

    static Choice[] choices(Page page) {
        return switch (page) {
            case MAH -> new Choice[] {
                Choice.FACE_TYPE, Choice.BODY_TYPE, Choice.BACK_TYPE, Choice.WORD_TYPE
            };
            case TABLE -> new Choice[] {Choice.TABLE_STYLE};
            case ANIMATION -> new Choice[] {
                Choice.OUT_MOVE_STYLE, Choice.INSERT_STYLE, Choice.OUT_STYLE
            };
            case EFFECTS -> new Choice[] {Choice.OUT_EFFECTS};
            case HAND -> new Choice[] {
                Choice.HAND_STYLE, Choice.HAND_SORT_STYLE, Choice.OUT_TABLE_CARD_STYLE
            };
            case ADVANCED -> NONE;
        };
    }

    static Option option(Choice choice, int index) {
        return switch (choice) {
            case WORD_TYPE -> TaizhouSettingNewDetailLayout.wordOption(index);
            case BACK_TYPE -> TaizhouSettingNewDetailLayout.backOption(index);
            case BODY_TYPE -> TaizhouSettingNewDetailLayout.tileOption(
                    TaizhouSettingNewDetailLayout.MAH_BODY, index, 102.0f, 144.0f);
            case FACE_TYPE -> TaizhouSettingNewDetailLayout.tileOption(
                    TaizhouSettingNewDetailLayout.MAH_FACE, index, 95.0f, 127.0f);
            case TABLE_STYLE -> TaizhouSettingNewDetailLayout.tableOption(index);
            case OUT_MOVE_STYLE -> TaizhouSettingNewDetailLayout.animationOption(
                    TaizhouSettingNewDetailLayout.MOVE_GROUP_Y, index,
                    index == 0 ? "弧线" : "直线", 87.0f);
            case INSERT_STYLE -> TaizhouSettingNewDetailLayout.animationOption(
                    TaizhouSettingNewDetailLayout.INSERT_GROUP_Y, index,
                    index == 0 ? "有插牌动画" : "无插牌动画", 217.0f);
            case OUT_STYLE -> TaizhouSettingNewDetailLayout.outStyleOption(
                    index, index == 0 ? "不显示大牌" : "显示大牌", index == 0 ? 217.0f : 174.0f);
            case OUT_EFFECTS -> TaizhouSettingNewDetailLayout.effectsOption(
                    index, index == 0 ? "有光效" : "无光效");
            case HAND_STYLE -> TaizhouSettingNewDetailLayout.handStyleOption(
                    index, index == 0 ? 329.0f : 358.0f);
            case HAND_SORT_STYLE -> TaizhouSettingNewDetailLayout.handSortOption(
                    index, index == 0 ? "左" : "右");
            case OUT_TABLE_CARD_STYLE -> index == 0
                    ? TaizhouSettingNewDetailLayout.outTableOption(0, 460.0f, 260.0f)
                    : TaizhouSettingNewDetailLayout.outTableOption(1, 461.0f, 259.0f);
        };
    }

    /** 选中角标的 capInsets：大图选项用另一组。 */
    static float[] selectCaps(Choice choice) {
        return choice == Choice.TABLE_STYLE || choice == Choice.OUT_EFFECTS
                ? TaizhouSettingNewDetailLayout.WIDE_SELECT_CAPS
                : TaizhouSettingNewDetailLayout.SELECT_CAPS;
    }

    /** 选项说明文字的字号：CSB 里动画/摆牌 43、特效 40、倒牌方向 60×0.75。 */
    static float textSize(Choice choice) {
        return switch (choice) {
            case OUT_EFFECTS -> 40.0f;
            case HAND_SORT_STYLE -> 45.0f;
            default -> 43.0f;
        };
    }

    /**
     * 详情页整体的 Y 偏移：桌布（列表无 VerticalEdge）与特效（{@code _KW_SET_DETAIL_4}
     * 无 VerticalEdge）跟随面板底边，其余页的子节点都是 TopEdge。
     */
    static float anchorOffset(Page page, TaizhouSettingNewViewport viewport) {
        return page == Page.TABLE || page == Page.EFFECTS
                ? viewport.bottomOffset()
                : viewport.topOffset();
    }

    /** 页面可滚动的高度（内容器高 − 视口高）。 */
    static float scrollRange(Page page) {
        return switch (page) {
            case TABLE -> TaizhouSettingNewDetailLayout.TABLE_CONTENT_HEIGHT
                    - TaizhouSettingNewDetailLayout.TABLE_VIEWPORT.height();
            case ANIMATION -> TaizhouSettingNewDetailLayout.ANIMATION_CONTENT_HEIGHT
                    - TaizhouSettingNewDetailLayout.ANIMATION_VIEWPORT.height();
            default -> 0.0f;
        };
    }

    private TaizhouSettingNewOptions() {}
}
