package com.nanbeiyule.game;

/** {@code Common/TipLayer/CSB/share_tips.csb}（{@code TIP_LAYER_TYPE.OK}）的节点几何。 */
final class TaizhouTipLayout {
    /** {@code game_exit_bg} 788×509，屏幕居中。 */
    static final float PANEL_LEFT = 566.0f;
    static final float PANEL_TOP = 285.5f;
    static final float PANEL_RIGHT = 1354.0f;
    static final float PANEL_BOTTOM = 794.5f;

    /** {@code common_title_bg} 与 {@code img_title_tip} 同心，压在面板顶栏上。 */
    static final float TITLE_CENTER_X = 960.0f;
    static final float TITLE_CENTER_Y = 326.0f;
    static final float TITLE_PLATE_WIDTH = 394.0f;
    static final float TITLE_PLATE_HEIGHT = 81.0f;
    static final float TITLE_WIDTH = 125.0f;
    static final float TITLE_HEIGHT = 61.0f;

    /** {@code game_prompt_txt} 650×150，字号 40，CColor(205,133,81)。 */
    static final float MESSAGE_CENTER_X = 960.0f;
    static final float MESSAGE_CENTER_Y = 494.5f;
    static final float MESSAGE_WIDTH = 650.0f;
    static final float MESSAGE_SIZE = 40.0f;
    static final int MESSAGE_COLOR = 0xFFCD8551;

    /** {@code KW_BUTTON_HINT_OK} 301×131，节点 scale 0.9。 */
    static final float OK_CENTER_X = 960.0f;
    static final float OK_CENTER_Y = 704.5f;
    static final float OK_WIDTH = 301.0f * 0.9f;
    static final float OK_HEIGHT = 131.0f * 0.9f;

    /** {@code KW_BUTTON_HINT_CLOSE} 99×102。 */
    static final float CLOSE_CENTER_X = 1334.5f;
    static final float CLOSE_CENTER_Y = 316.5f;
    static final float CLOSE_SIZE_WIDTH = 99.0f;
    static final float CLOSE_SIZE_HEIGHT = 102.0f;

    static boolean okContains(float x, float y) {
        return contains(x, y, OK_CENTER_X, OK_CENTER_Y, OK_WIDTH, OK_HEIGHT);
    }

    static boolean closeContains(float x, float y) {
        return contains(x, y, CLOSE_CENTER_X, CLOSE_CENTER_Y, CLOSE_SIZE_WIDTH, CLOSE_SIZE_HEIGHT);
    }

    private static boolean contains(
            float x, float y, float centerX, float centerY, float width, float height) {
        return Math.abs(x - centerX) <= width / 2.0f && Math.abs(y - centerY) <= height / 2.0f;
    }

    private TaizhouTipLayout() {}
}
