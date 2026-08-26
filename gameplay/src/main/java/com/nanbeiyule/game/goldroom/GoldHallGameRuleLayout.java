package com.nanbeiyule.game.goldroom;

/**
 * 规则弹层 {@code GoldHallGameRuleView} 的原版几何，单位是 1920x1080 设计像素、左上原点。
 *
 * <p>节点、尺寸与图素来自 {@code hall/CSB/NewGoldHall/GameRuleLayer.csb}；交互与选中态来自
 * {@code lobby/Modules/GoldNew/SubModules/Rule/RuleView.lua}。CSB 里 {@code body} 是
 * (960,540) anchor (0.5,0.5) 的 1920x1080 面板，其局部原点与屏幕原点重合，因此这里只做
 * 一次 Cocos Y 向上到 Android Y 向下的换算，渲染与命中不再混用两套原点。
 *
 * <p>正文排版（字号、行距、内边距）不在归档内：原版正文是 WebView 远程 HTML
 * （{@code UrlConf.GAME_RULE_HTML_ADDR}），页面本身没有随包留存。带 {@code SCREENSHOT_}
 * 前缀的常量是按 1.5.4 实机截图反算的推断值，不是原版 CSB 或 Lua 事实。
 *
 * <p>证据见 android/docs/ORIGINAL-GOLD-HALL-GAME-RULE-EVIDENCE.md。
 */
public final class GoldHallGameRuleLayout {
    public static final float DESIGN_WIDTH = 1920.0f;
    public static final float DESIGN_HEIGHT = 1080.0f;

    /** {@code Image_1} {@code Img_tc_huang_di2.png}: Cocos (975,473.4) anchor 中心 1590x820。 */
    public static final float PANEL_LEFT = 180.0f;

    public static final float PANEL_TOP = 196.6f;
    public static final float PANEL_WIDTH = 1590.0f;
    public static final float PANEL_HEIGHT = 820.0f;

    /**
     * 白底图源尺寸 954x566 被拉到 1590x820，是 CSB 里唯一带 scale9 尺寸字段的 ImageView。
     * 原版 capInsets 字段语义尚未解出，这里按图素 alpha 实测圆角约 12px 取 24px 安全切边，
     * 属推断值；中间是竖直渐变，拉伸中段与直接缩放等价，只保证四角不变形。
     */
    public static final int PANEL_CAP_INSET = 24;

    /** {@code Image_2} {@code Img_tc_huang_zuo.png}: Cocos (321,468.53) anchor 中心 321x803。 */
    public static final float LEFT_COLUMN_LEFT = 160.5f;

    public static final float LEFT_COLUMN_TOP = 209.97f;
    public static final float LEFT_COLUMN_WIDTH = 321.0f;
    public static final float LEFT_COLUMN_HEIGHT = 803.0f;

    /** {@code Image_3} {@code Img_tc_huang_big.png}: Cocos (937.8,875.61) anchor 中心 1655x285。 */
    public static final float HEADER_LEFT = 110.3f;

    public static final float HEADER_TOP = 61.89f;
    public static final float HEADER_WIDTH = 1655.0f;
    public static final float HEADER_HEIGHT = 285.0f;

    /** {@code Image_4} {@code Img_tc_title4.png}: Cocos (282.73,899.547) anchor (0,0.5) 133x67。 */
    public static final float TITLE_LEFT = 282.73f;

    public static final float TITLE_CENTER_Y = 180.453f;
    public static final float TITLE_WIDTH = 133.0f;
    public static final float TITLE_HEIGHT = 67.0f;

    /** {@code _btnClose} {@code Btn_guanbi.png}: Cocos (1708.88,903.514) anchor 中心 53x54。 */
    public static final float CLOSE_CENTER_X = 1708.88f;

    public static final float CLOSE_CENTER_Y = 176.486f;
    public static final float CLOSE_WIDTH = 53.0f;
    public static final float CLOSE_HEIGHT = 54.0f;

    /** 关闭键的命中框按原版按钮尺寸放大到易点面积；原版 ccui.Button 命中即图素矩形。 */
    public static final float CLOSE_TOUCH_SIZE = 96.0f;

    /** {@code _listLeft}: Cocos (140,856) anchor (0,1) 322x778，Vertical + Align_Right。 */
    public static final float LEFT_LIST_LEFT = 140.0f;

    public static final float LEFT_LIST_TOP = 224.0f;
    public static final float LEFT_LIST_WIDTH = 322.0f;
    public static final float LEFT_LIST_HEIGHT = 778.0f;

    /** {@code _itemGame}: 280x128 面板模板，{@code RuleView:createGameBtn} 逐个 clone。 */
    public static final float ITEM_WIDTH = 280.0f;

    public static final float ITEM_HEIGHT = 128.0f;

    /** Vertical 列表 {@code Align_Right}：280 宽的条目在 322 宽列表里右对齐。 */
    public static final float ITEM_LEFT = LEFT_LIST_LEFT + LEFT_LIST_WIDTH - ITEM_WIDTH;

    /** {@code _imgBg}: 条目局部 Cocos (119,46.08) anchor 中心 322x184。 */
    public static final float ITEM_BG_CENTER_X = 119.0f;

    public static final float ITEM_BG_CENTER_Y = ITEM_HEIGHT - 46.08f;
    public static final float ITEM_BG_WIDTH = 322.0f;
    public static final float ITEM_BG_HEIGHT = 184.0f;

    /** {@code _txtName}: 条目局部 Cocos (131.6,64) anchor 中心，字号 50。 */
    public static final float ITEM_TEXT_CENTER_X = 131.6f;

    public static final float ITEM_TEXT_CENTER_Y = ITEM_HEIGHT - 64.0f;
    public static final float ITEM_TEXT_SIZE = 50.0f;

    /** {@code RuleView:setBtnSelectState}: 选中 {@code cc.c3b(0x9d,0x46,0)}。 */
    public static final int ITEM_TEXT_COLOR_SELECTED = 0xFF9D4600;

    /** {@code RuleView:setBtnSelectState}: 未选中 {@code cc.c3b(0x99,0x83,0x6e)}。 */
    public static final int ITEM_TEXT_COLOR_UNSELECTED = 0xFF99836E;

    /** {@code _listRight}: Cocos (503.221,815.768) anchor (0,1) 1250x706，正文承载区。 */
    public static final float CONTENT_LEFT = 503.221f;

    public static final float CONTENT_TOP = 348.0f;
    public static final float CONTENT_WIDTH = 1250.0f;

    /** {@code RuleView:initWebView}: 常规玩法 706 高，{@code GameID == 30579} 改 630。 */
    public static final float CONTENT_HEIGHT = 622.232f;

    public static final float CONTENT_HEIGHT_IMAGE_TEXT = 630.0f;

    /** {@code _panelLoading}: Cocos (1121.86,841.86) anchor (0.5,1) 1300x750。 */
    public static final float LOADING_CENTER_X = 1121.86f;

    public static final float LOADING_TOP = 238.14f;
    public static final float LOADING_WIDTH = 1300.0f;
    public static final float LOADING_HEIGHT = 750.0f;

    /** {@code Text_1}: 面板局部 (650,375) anchor 中心，字号 40，文案与颜色取自 CSB。 */
    public static final float LOADING_TEXT_CENTER_X = LOADING_CENTER_X;

    public static final float LOADING_TEXT_CENTER_Y = LOADING_TOP + 375.0f;
    public static final float LOADING_TEXT_SIZE = 40.0f;
    public static final int LOADING_TEXT_COLOR = 0xFFBF6C1D;
    public static final String LOADING_TEXT = "规则加载中请稍后...";

    /** {@code _KW_BTN_IMAGE_TEXT}: Cocos (1600,135) 332x108 缩放 0.7，仅 30579 可见。 */
    public static final float IMAGE_TEXT_CENTER_X = 1600.0f;

    public static final float IMAGE_TEXT_CENTER_Y = DESIGN_HEIGHT - 135.0f;
    public static final float IMAGE_TEXT_WIDTH = 332.0f * 0.7f;
    public static final float IMAGE_TEXT_HEIGHT = 108.0f * 0.7f;

    /** {@code _KW_TXT_BUTTON}: 与按钮同心，字号 40，文案「图文教程」。 */
    public static final float IMAGE_TEXT_TEXT_SIZE = 40.0f;

    public static final int IMAGE_TEXT_TEXT_COLOR = 0xFFCE5C04;
    public static final String IMAGE_TEXT_LABEL = "图文教程";

    /** {@code RuleView:updateRuleWebView}: 只有这个 GameID 显示图文教程按钮。 */
    public static final long IMAGE_TEXT_GAME_ID = 30579L;

    /**
     * 正文左内边距，相对 {@code _listRight} 左边。SCREENSHOT_CALIBRATED：实机正文左边缘
     * 落在设计 x≈561，减去 {@code CONTENT_LEFT} 得 58。
     */
    public static final float SCREENSHOT_CONTENT_PADDING_LEFT = 58.0f;

    /** 正文首行基线区顶端相对 {@code _listRight} 顶边的距离。SCREENSHOT_CALIBRATED。 */
    public static final float SCREENSHOT_CONTENT_PADDING_TOP = 24.0f;

    /** 行距。SCREENSHOT_CALIBRATED：实机相邻行顶差 48.7 屏幕像素 / 0.9198 缩放 ≈ 53。 */
    public static final float SCREENSHOT_LINE_PITCH = 53.0f;

    /**
     * 正文字号。SCREENSHOT_CALIBRATED：中日韩全角字的步进等于字号，实机纯汉字行
     * （「吃牌…」18 字、「抓牌…」22 字）量得每字 35.6~35.9 设计像素，取 36。
     */
    public static final float SCREENSHOT_BODY_TEXT_SIZE = 36.0f;

    /**
     * 小标题字号。SCREENSHOT_CALIBRATED：「一、游戏规则」「二、基本术语」量得每字
     * 35.3~35.5 设计像素，与正文同字号，区别只是加粗。
     */
    public static final float SCREENSHOT_HEADING_TEXT_SIZE = 36.0f;

    /** 正文与小标题都是纯黑，实机取色 {@code #000000}。SCREENSHOT_CALIBRATED。 */
    public static final int SCREENSHOT_TEXT_COLOR = 0xFF000000;

    private GoldHallGameRuleLayout() {}

    /** 第 index 个页签的顶边；原版 ListView 未设条目间距，条目首尾相接。 */
    public static float itemTop(int index) {
        return LEFT_LIST_TOP + index * ITEM_HEIGHT;
    }

    /** 页签命中框就是 280x128 的条目面板本身。 */
    public static boolean itemContains(int index, float designX, float designY) {
        float top = itemTop(index);
        return designX >= ITEM_LEFT
                && designX <= ITEM_LEFT + ITEM_WIDTH
                && designY >= top
                && designY <= top + ITEM_HEIGHT;
    }

    /** 关闭键命中框，以原版按钮中心为心。 */
    public static boolean closeContains(float designX, float designY) {
        float half = CLOSE_TOUCH_SIZE / 2.0f;
        return designX >= CLOSE_CENTER_X - half
                && designX <= CLOSE_CENTER_X + half
                && designY >= CLOSE_CENTER_Y - half
                && designY <= CLOSE_CENTER_Y + half;
    }

    /** 正文可视区，滚动裁剪用。 */
    public static float contentHeight(boolean imageTextVisible) {
        return imageTextVisible ? CONTENT_HEIGHT_IMAGE_TEXT : CONTENT_HEIGHT;
    }
}
