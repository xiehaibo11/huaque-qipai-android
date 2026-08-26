package com.nanbeiyule.game;

/** Parsed CreateBoxRoomDynamic.csb geometry in 1920x1080 top-left design space. */
final class CreateRoomLayout {
    static final float DESIGN_WIDTH = 1920.0f;
    static final float DESIGN_HEIGHT = 1080.0f;
    static final String FONT_ASSET = "fonts/zihun_jingdian_lihei.ttf";
    static final float EXTERNAL_BADGE_LEFT_OFFSET = -2.2421f;
    static final float EXTERNAL_BADGE_TOP_OFFSET = -1.3666f;
    static final float EXTERNAL_BADGE_WIDTH = 103.0f;
    static final float EXTERNAL_BADGE_HEIGHT = 110.0f;
    static final float TOP_HEIGHT = 100.0f;
    static final float TOP_BACKGROUND_LEFT = 0.0044f;
    static final float TOP_BACKGROUND_TOP = 1.5f;
    static final float TOP_BACKGROUND_RIGHT = 1920.0044f;
    static final float TOP_BACKGROUND_BOTTOM = 122.5f;
    static final float TITLE_BACKGROUND_LEFT = 668.0f;
    static final float TITLE_BACKGROUND_TOP = -7.0016f;
    static final float TITLE_BACKGROUND_RIGHT = 1252.0f;
    static final float TITLE_BACKGROUND_BOTTOM = 112.9984f;
    static final float TITLE_LEFT = 814.9600f;
    static final float TITLE_TOP = 3.9989f;
    static final float TITLE_RIGHT = 1106.9600f;
    static final float TITLE_BOTTOM = 87.9989f;
    static final float SPLIT_LINE_CENTER_X = 408.0f;
    static final float GAME_LIST_LEFT = 0.0f;
    static final float GAME_LIST_TOP = 142.0f;
    static final float GAME_LIST_RIGHT = 385.92f;
    static final float GAME_LIST_BOTTOM = 1062.0f;
    static final float RULE_LIST_LEFT = 421.8063f;
    static final float RULE_LIST_TOP = 110.00055f;
    static final float RULE_LIST_RIGHT = 1921.8063f;
    static final float RULE_LIST_BOTTOM = 890.0455f;
    static final float RULE_ROW_HEIGHT = 100.0f;
    static final float RULE_ROW_STRIDE = 105.0f;
    static final float CREATE_BUTTON_CENTER_X = 1171.8063f;
    static final float CREATE_BUTTON_CENTER_Y = 975.00055f;
    static final float CREATE_BUTTON_WIDTH = 430.0f;
    static final float CREATE_BUTTON_HEIGHT = 150.01f;
    static final float COST_ICON_LEFT = 1411.8064f;
    static final float COST_ICON_RIGHT = 1541.8064f;
    static final float COST_ICON_TOP = 938.0056f;
    static final float COST_ICON_BOTTOM = 1012.0056f;
    static final float COST_TEXT_LEFT = 1526.8064f;
    static final float COST_VALUE_LEFT = 1666.8064f;
    static final float COST_TEXT_CENTER_Y = 975.0056f;
    static final float BACK_LEFT = 10.0f;
    static final float BACK_TOP = 0.0f;
    static final float BACK_RIGHT = 160.0f;
    static final float BACK_BOTTOM = 110.0f;
    static final float BACK_IMAGE_LEFT = 46.0f;
    static final float BACK_IMAGE_RIGHT = 140.0f;
    static final float FEEDBACK_LEFT = 1756.5084f;
    static final float FEEDBACK_TOP = 908.1506f;
    static final float FEEDBACK_RIGHT = 1887.0084f;
    static final float FEEDBACK_BOTTOM = 1045.8506f;
    static final float GAME_TAB_WIDTH = 360.0f;
    static final float GAME_TAB_HEIGHT = 136.0f;
    /**
     * {@code _KW_LISTVIEW_CHOOSE_GAME} 的 ListView 选项：direction=Vertical、
     * gravity=Align_HorizontalCenter、itemsMargin=2，所以相邻页签间距是 136+2。
     * 与之对应，规则 ListView {@code _KW_PANEL_GAME_RULE_DYNAMIC} 的 itemsMargin 是 5（100+5=105）。
     */
    static final float GAME_TAB_MARGIN = 2.0f;
    static final float GAME_TAB_STRIDE = GAME_TAB_HEIGHT + GAME_TAB_MARGIN;
    /** ListView 水平居中：(385.92-360)/2 = 12.96，中心与 ListView 中心 192.96 重合。 */
    static final float GAME_TAB_CENTER_X = 192.96f;
    static final float GAME_TAB_LEFT = GAME_TAB_CENTER_X - GAME_TAB_WIDTH * 0.5f;
    /** {@code KW_TEXT_MODEl} pos=(170.0002,71.0012) anchor=(0.5,0.5)，换算成页签左上原点。 */
    static final float GAME_TAB_TEXT_LOCAL_X = 170.0002f;
    static final float GAME_TAB_TEXT_LOCAL_Y = GAME_TAB_HEIGHT - 71.0012f;
    /** {@code onTouchEventChooseGameDynamic} 未选中 54、选中 60。 */
    static final float GAME_TAB_TEXT_SIZE = 54.0f;
    static final float GAME_TAB_TEXT_SIZE_SELECTED = 60.0f;
    /** {@code Mark.csb}：Panel_1/_KW_IMG_MARK 150x150，挂在页签本地 (0,0)（Cocos 左下角）。 */
    static final float GAME_MARK_SIZE = 150.0f;
    /** {@code _KW_TEXT_MARK} pos=(46.8722,96.903) anchor=(0.5,0.5)，换算成角标左上原点。 */
    static final float GAME_MARK_TEXT_LOCAL_X = 46.8722f;
    static final float GAME_MARK_TEXT_LOCAL_Y = GAME_MARK_SIZE - 96.903f;
    /** {@code MarkView:initTextMark} 按 {@code string.len} 分档：9 字节（3 个汉字）用 36，否则 48。 */
    static final float GAME_MARK_TEXT_SIZE = 48.0f;
    static final float GAME_MARK_TEXT_SIZE_LONG = 36.0f;
    static final int GAME_MARK_TEXT_LONG_CODE_POINTS = 3;
    /** {@code _KW_TEXT_MARK} 的 rotationSkew=(-45,-45)，即视觉上逆时针 45 度。 */
    static final float GAME_MARK_ROTATION_DEGREES = -45.0f;
    static final float RULE_CONTENT_LEFT = 421.8063f;
    /** {@code KW_ITEM_TEXT} pos=(49.92,50) anchor=(0,0.5)，字号 46。 */
    static final float RULE_TITLE_LEFT = RULE_LIST_LEFT + 49.92f;
    static final float RULE_TITLE_CENTER_Y = 50.0f;
    static final float RULE_TITLE_TEXT_SIZE = 46.0f;
    /**
     * {@code Image_8} 是 {@code KW_ITEM_TEXT} 的子节点：pos=(680,80) anchor=(0.5,0.5)
     * size=1428x18，换算到行左上原点后横跨 15.92..1443.92、纵向 -12..6。
     */
    static final float RULE_LINE_LEFT = RULE_LIST_LEFT + 15.92f;
    static final float RULE_LINE_RIGHT = RULE_LIST_LEFT + 1443.92f;
    static final float RULE_LINE_TOP = -12.0f;
    static final float RULE_LINE_BOTTOM = 6.0f;
    /** {@code _KW_RADIO_ITEM} 82x82、{@code _KW_CHECK_BOX_ITEM} 76x80。 */
    static final float RADIO_WIDTH = 82.0f;
    static final float RADIO_HEIGHT = 82.0f;
    static final float CHECKBOX_WIDTH = 76.0f;
    static final float CHECKBOX_HEIGHT = 80.0f;
    /** {@code KW_OPTION_ITEM_TEXT} 单选 pos=(86.4538,40)、复选 pos=(85,40)，anchor=(0,0.5)。 */
    static final float RADIO_TEXT_OFFSET_X = 86.4538f - RADIO_WIDTH * 0.5f;
    static final float RADIO_TEXT_OFFSET_Y = RADIO_HEIGHT * 0.5f - 40.0f;
    static final float CHECKBOX_TEXT_OFFSET_X = 85.0f - CHECKBOX_WIDTH * 0.5f;
    static final float CHECKBOX_TEXT_OFFSET_Y = CHECKBOX_HEIGHT * 0.5f - 40.0f;
    static final float OPTION_TEXT_SIZE = 42.0f;
    /** {@code KW_PANEL_TIPS} 70x70 anchor=(0,0.5)，x = 选项文字宽度 + 10。 */
    static final float TIP_ICON_SIZE = 70.0f;
    static final float TIP_ICON_GAP = 10.0f;
    /** 提示按钮锚点在文字节点本地 (84,25)，文字节点垂直中心 24.5，故顶向下偏移 0.5。 */
    static final float TIP_ICON_OFFSET_Y = 0.5f;
    /** {@code KW_PIC_TIPS_PAOPAO} pos=(67,35) anchor=(0,0.5)，{@code KW_TEXT_TIPS} 字号 30。 */
    static final float TIP_BUBBLE_OFFSET_X = 67.0f;
    static final float TIP_TEXT_SIZE = 30.0f;
    /** {@code KW_TEXT_COST_INFO_PRE} / {@code _KW_TEXT_COST_INFO} 字号 32。 */
    static final float COST_TEXT_SIZE = 32.0f;
    /**
     * {@code _KW_TEXT_ZHU} pos=(-24,75) anchor=(1,0.5) size=496x76 字号 32，
     * 是 {@code _KW_BTN_CREATE_BOX_ROOM} 的子节点。
     */
    static final float NOTE_TEXT_SIZE = 32.0f;
    static final float NOTE_TEXT_WIDTH = 496.0f;
    static final float NOTE_TEXT_LINE_HEIGHT = 38.0f;
    static final float NOTE_TEXT_RIGHT =
            CREATE_BUTTON_CENTER_X - CREATE_BUTTON_WIDTH * 0.5f - 24.0f;
    static final float NOTE_TEXT_LEFT = NOTE_TEXT_RIGHT - NOTE_TEXT_WIDTH;
    static final float NOTE_TEXT_CENTER_Y =
            CREATE_BUTTON_CENTER_Y + CREATE_BUTTON_HEIGHT * 0.5f - 75.0f;
    static final float NOTE_TEXT_FIRST_LINE_CENTER_Y =
            NOTE_TEXT_CENTER_Y - NOTE_TEXT_LINE_HEIGHT * 0.5f;

    private CreateRoomLayout() {}

    /** {@code MarkView:initTextMark}：{@code string.len(markStr)==9}（3 个汉字）用 36，否则 48。 */
    static float markTextSize(String badge) {
        int codePoints = badge == null ? 0 : badge.codePointCount(0, badge.length());
        return codePoints >= GAME_MARK_TEXT_LONG_CODE_POINTS
                ? GAME_MARK_TEXT_SIZE_LONG
                : GAME_MARK_TEXT_SIZE;
    }

    static float[] optionXs(int count) {
        return switch (count) {
            case 1 -> new float[] {230.0f};
            case 2 -> new float[] {230.0f, 675.0f};
            case 3 -> new float[] {230.0f, 675.0f, 1120.0f};
            case 4 -> new float[] {230.0f, 527.0f, 824.0f, 1120.0f};
            case 5 -> new float[] {230.0f, 452.5f, 675.0f, 897.5f, 1120.0f};
            default -> throw new IllegalArgumentException("Option count must be 1..5");
        };
    }

    static Viewport contain(int width, int height) {
        return safeViewport(width, height, 0, 0, 0, 0);
    }

    static Viewport safeViewport(
            int width, int height, int insetLeft, int insetTop, int insetRight, int insetBottom) {
        if (width <= 0 || height <= 0) {
            return new Viewport(1.0f, 0.0f, 0.0f);
        }
        float safeLeft = Math.max(0, insetLeft);
        float safeTop = Math.max(0, insetTop);
        float safeRight = Math.max(safeLeft, width - Math.max(0, insetRight));
        float safeBottom = Math.max(safeTop, height - Math.max(0, insetBottom));
        float safeWidth = safeRight - safeLeft;
        float safeHeight = safeBottom - safeTop;
        float scale = Math.min(safeWidth / DESIGN_WIDTH, safeHeight / DESIGN_HEIGHT);
        return new Viewport(
                scale,
                safeLeft + (safeWidth - DESIGN_WIDTH * scale) * 0.5f,
                safeTop + (safeHeight - DESIGN_HEIGHT * scale) * 0.5f);
    }

    /**
     * 原版 ListView 垂直排版从 innerContainer 顶部开始；内容不足一屏时 cocos
     * {@code ScrollView::setInnerContainerSize} 会把 innerContainer 强制成视口尺寸，
     * 页签仍然贴顶，不居中。参数保留是为了调用方语义清晰。
     */
    static float gameContentTop(int gameCount) {
        return GAME_LIST_TOP;
    }

    record Viewport(float scale, float offsetX, float offsetY) {
        float designX(float screenX) {
            return (screenX - offsetX) / scale;
        }

        float designY(float screenY) {
            return (screenY - offsetY) / scale;
        }
    }
}
