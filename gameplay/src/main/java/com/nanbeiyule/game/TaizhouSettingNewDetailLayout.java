package com.nanbeiyule.game;

import com.nanbeiyule.game.TaizhouSettingNewLayout.Box;
import com.nanbeiyule.game.TaizhouSettingNewLayout.Frame;

/**
 * {@code SettingNew.csb} 六个详情页（{@code _KW_SET_DETAIL_1..6}）的节点几何。
 *
 * <p>坐标是详情面板局部值，绘制时整体平移 {@link TaizhouSettingNewLayout#DETAIL_LOCAL_X}。
 * 两个 ScrollView 的内容器按 Cocos 规则顶对齐：{@code innerBottom = viewBottom + viewH - innerH}。
 */
final class TaizhouSettingNewDetailLayout {
    static final Frame DETAIL = new Frame(0.0f, 0.0f);

    /** {@code set_mah_set_bg2.png} 100×100 的详情底。 */
    static final float[] BACKGROUND_CAPS = {33.0f, 33.0f, 34.0f, 34.0f};

    /** {@code set_mah_set_bg_2.png} 112×140 的选项底。 */
    static final float[] ITEM_CAPS = {37.0f, 46.0f, 38.0f, 48.0f};

    /** {@code set_mah_style_select.png} 108×132 的选中角标。 */
    static final float[] SELECT_CAPS = {48.0f, 60.0f, 4.0f, 12.0f};

    /** 大图选项用的另一组角标 capInsets（桌布/动画/特效）。 */
    static final float[] WIDE_SELECT_CAPS = {48.0f, 20.0f, 4.0f, 52.0f};

    /** {@code set_mah_btn_bg1.png} 121×102 的底部条。 */
    static final float[] BOTTOM_CAPS = {39.0f, 33.0f, 43.0f, 36.0f};

    /** {@code set_mah_btn_off_3.png} 332×84 的双段开关。 */
    static final float[] SWITCH_CAPS = {20.0f, 26.0f, 296.0f, 32.0f};

    /** {@code set_mah_btn_on_2.png} 171×84 的语音选中底。 */
    static final float[] VOICE_CAPS = {38.0f, 12.0f, 85.0f, 4.0f};

    /** 一个可选项：底图、内容图、选中角标、说明文字。 */
    record Option(Box background, Box image, Box selected, Box label, String text) {
        boolean contains(float x, float y) {
            return background.contains(x, y);
        }
    }

    /** 底部方案条 {@code _KW_BOTTOM_NODE}。 */
    static final Box BOTTOM_BAR = DETAIL.boxCorner(0.0f, 0.0f, 2000.0f, 102.0f);

    static Box plan(int index) {
        float[] centers = {150.0f, 385.0f, 625.0f, 865.0f};
        return DETAIL.box(centers[index], 55.0f, 171.0f, 64.0f);
    }

    // ---------------------------------------------------------------- 麻将
    static final Box MAH_PREVIEW = DETAIL.box(188.0f, 928.0f, 275.0f, 244.0f);
    static final Frame MAH_HEIGHT = DETAIL.at(335.0f, 985.0f);
    static final Frame MAH_WIDTH = DETAIL.at(335.0f, 870.0f);
    static final Box MAH_LINE = DETAIL.box(625.0f, 768.0f, 1200.0f, 2.0f);
    static final Frame MAH_WORD = DETAIL.at(37.0184f, 699.464f);
    static final Frame MAH_WORD_SIZE = DETAIL.at(35.0f, 555.0f);
    static final Frame MAH_BACK = DETAIL.at(35.0f, 440.0f);
    static final Frame MAH_BODY = DETAIL.at(35.0f, 266.7f);
    static final Frame MAH_FACE = DETAIL.at(680.0f, 266.7f);

    /** 牌花：{@code _KW_WORD_TYPE_n} 112×136，横向步距 114。 */
    static Option wordOption(int index) {
        Frame item = MAH_WORD.at(118.0f + 114.0f * index, -86.0f);
        return new Option(
                item.box(55.0f, 66.0f, 114.0f, 142.0f),
                item.box(55.0f, 70.0f, 81.0f * 0.95f, 108.0f * 0.95f),
                item.box(55.0f, 68.0f, 120.0f, 150.0f),
                null,
                null);
    }

    /** 牌背：{@code _KW_BACK_TYPE_n} 112×136，横向步距 130。 */
    static Option backOption(int index) {
        Frame item = MAH_BACK.at(155.0f + 130.0f * index, -104.0f);
        return new Option(
                item.box(55.0f, 66.0f, 114.0f, 142.0f),
                item.box(55.0f, 68.0f, 81.0f, 115.0f),
                item.box(55.0f, 68.0f, 120.0f, 150.0f),
                null,
                null);
    }

    /** 牌型/牌面：{@code _KW_BODY_TYPE_n} / {@code _KW_FACE_TYPE_n} 140×190。 */
    static Option tileOption(Frame group, int index, float imageWidth, float imageHeight) {
        Frame item = group.at(140.0f + 190.0f * index, -141.0f);
        return new Option(
                item.box(75.0f, 93.0f, 130.0f, 180.0f),
                item.box(75.0f, 97.0f, imageWidth, imageHeight),
                item.box(75.0f, 95.0f, 140.0f, 190.0f),
                null,
                null);
    }

    // ---------------------------------------------------------------- 桌布
    /** {@code _KW_TABLE_STYLE_LIST}：1200×960 视口，内容器 1200×1080 顶对齐。 */
    static final Box TABLE_VIEWPORT = DETAIL.boxCorner(0.0f, 110.004f, 1200.0f, 960.0f);

    private static final Frame TABLE_CONTENT = DETAIL.at(0.0f, 110.004f + 960.0f - 1080.0f);

    static final float TABLE_CONTENT_HEIGHT = 1080.0f;

    static Option tableOption(int index) {
        float x = index % 2 == 0 ? 99.9999f : 667.0f;
        float y = 755.0f - 330.0f * (index / 2);
        Frame item = TABLE_CONTENT.at(x, y);
        return new Option(
                item.box(235.0f, 150.0f, 470.0f, 300.0f),
                item.box(235.0f, 150.0f, 386.0f, 228.0f),
                item.box(235.0f, 154.0f, 250.0f * 2.0f, 180.0f * 1.8f),
                null,
                null);
    }

    // ---------------------------------------------------------------- 动画
    /** {@code _KW_SET_ANI_SCROLL_VIEW}：2000×970 视口，内容器 2000×1300 顶对齐。 */
    static final Box ANIMATION_VIEWPORT = DETAIL.boxCorner(0.0f, 105.0f, 2000.0f, 970.0f);

    private static final Frame ANIMATION_CONTENT = DETAIL.at(0.0f, 105.0f + 970.0f - 1300.0f);

    static final float ANIMATION_CONTENT_HEIGHT = 1300.0f;

    static final Box ANIMATION_LINE_1 = ANIMATION_CONTENT.boxLeft(85.0f, 869.96f, 1200.0f, 2.0f);
    static final Box ANIMATION_LINE_2 = ANIMATION_CONTENT.boxLeft(85.0f, 455.0f, 1200.0f, 2.0f);

    static Option animationOption(float groupY, int index, String text, float textWidth) {
        Frame item = ANIMATION_CONTENT.at(0.0f, groupY).at(index == 0 ? 100.0f : 667.0f, 200.0f);
        return new Option(
                item.box(235.0f, 150.0f, 470.0f, 300.0f),
                item.box(235.0f, 150.0f, 410.0f, 224.0f),
                item.box(235.0f, 154.0f, 250.0f * 2.0f, 180.0f * 1.8f),
                item.box(227.5f, -41.0f, textWidth, 51.0f),
                text);
    }

    static Option outStyleOption(int index, String text, float textWidth) {
        Frame item = ANIMATION_CONTENT.at(0.0f, 479.96f).at(index == 0 ? 100.0f : 667.0f, -360.0f);
        return new Option(
                item.box(235.0f, 150.0f, 470.0f, 300.0f),
                item.box(235.0f, 148.0f, 410.0f, 224.0f),
                item.box(235.0f, 154.0f, 250.0f * 2.0f, 180.0f * 1.8f),
                item.box(227.5f, -41.0f, textWidth, 51.0f),
                text);
    }

    static final float MOVE_GROUP_Y = 749.97f;
    static final float INSERT_GROUP_Y = 339.95f;

    // ---------------------------------------------------------------- 特效
    private static final Frame EFFECTS_GROUP = DETAIL.at(0.0f, 540.0f);

    static final Box EFFECTS_LINE = EFFECTS_GROUP.boxLeft(95.0001f, 67.0f, 1200.0f, 2.0f);

    static Option effectsOption(int index, String text) {
        Frame item = EFFECTS_GROUP.at(index == 0 ? 100.0f : 667.0f, 199.999f);
        return new Option(
                item.box(235.0f, 150.0f, 470.0f, 300.0f),
                item.box(235.0f, 150.0f, 410.0f, 224.0f),
                item.box(235.0f, 154.0f, 250.0f * 2.0f, 180.0f * 1.8f),
                item.box(235.0f, -20.0f, 123.0f, 47.0f),
                text);
    }

    // ---------------------------------------------------------------- 摆牌
    private static final Frame HAND_STYLE_GROUP = DETAIL.at(32.0f, 1009.0f);
    private static final Frame HAND_SORT_GROUP = DETAIL.at(32.0f, 795.0f);
    private static final Frame OUT_TABLE_GROUP = DETAIL.at(32.0f, 395.0f);

    static final Box HAND_STYLE_LABEL = HAND_STYLE_GROUP.boxLeft(0.0f, -60.0f, 185.0f, 51.0f);
    static final Box HAND_SORT_LABEL = HAND_SORT_GROUP.boxLeft(0.0f, 0.0f, 185.0f, 51.0f);
    static final Box OUT_TABLE_LABEL = OUT_TABLE_GROUP.boxLeft(0.0f, 0.0f, 185.0f, 51.0f);

    /** {@code _KW_HAND_STYLE_n}：400×200 面板整体 0.8 缩放。 */
    static Option handStyleOption(int index, float imageWidth) {
        float scale = 0.8f;
        Frame item = HAND_STYLE_GROUP.at(index == 0 ? 211.0f : 700.0f, -140.0f);
        return new Option(
                item.box(200.0f * scale, 100.0f * scale, 400.0f * scale, 220.0f * scale),
                item.box(200.0f * scale, 100.0f * scale, imageWidth * scale, 163.0f * scale),
                item.box(200.0f * scale, 104.0f * scale, 215.0f * 2.0f * scale,
                        136.0f * 1.8f * scale),
                null,
                null);
    }

    /** 倒牌样式 1 的喂牌方向箭头，挂在 329×163 的内容图上。 */
    static Box handStyleArrow() {
        Option option = handStyleOption(0, 329.0f);
        return new Box(
                option.image().centerX() + (56.0f - 329.0f / 2.0f) * 0.8f,
                option.image().centerY() - (89.0f - 163.0f / 2.0f) * 0.8f,
                74.0f * 0.8f,
                74.0f * 0.8f);
    }

    /** {@code _KW_HAND_SORT_STYLE_n}：1100×220 面板整体 0.75 缩放。 */
    static Option handSortOption(int index, String text) {
        float scale = 0.75f;
        Frame item = HAND_SORT_GROUP.at(217.0f, index == 0 ? -144.0f : -319.0f);
        return new Option(
                item.box(550.0f * scale, 100.0f * scale, 1103.0f * scale, 225.0f * scale),
                item.box(550.0f * scale, (index == 0 ? 100.0f : 102.5f) * scale,
                        1025.0f * scale, 160.0f * scale),
                item.box(550.0f * scale, 104.0f * scale, 570.0f * 2.0f * scale,
                        145.0f * 1.8f * scale),
                item.boxLeft(1133.0f * scale, 90.0f * scale, 61.0f * scale, 71.0f * scale),
                text);
    }

    /** {@code _KW_OUT_TABLE_CARD_STYLE_n}：500×290。 */
    static Option outTableOption(int index, float imageWidth, float imageHeight) {
        Frame item = OUT_TABLE_GROUP.at(index == 0 ? 200.0f : 715.0f, -255.0f);
        return new Option(
                item.box(250.0f, 142.1f, 500.0f, 300.0f),
                item.box(250.0f, 145.0f, imageWidth, imageHeight),
                item.box(250.0f, 145.0f, 520.0f, 320.0f),
                null,
                null);
    }

    private TaizhouSettingNewDetailLayout() {}
}
