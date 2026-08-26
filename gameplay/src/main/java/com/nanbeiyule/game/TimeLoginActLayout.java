package com.nanbeiyule.game;

/**
 * 原版 TimeLoginActLayer.csb 的 1920x1080 几何。
 *
 * <p>每个常量都直接写成 CSB 里的 {@code pos/size/anchor}，再由 {@link CocosNode} 做
 * 「Y 轴向上、原点左下、坐标相对父节点」到 Android「Y 轴向下、原点左上」的换算，
 * 便于逐值对照 {@code android/docs/ORIGINAL-TIME-LOGIN-ACT-EVIDENCE.md} 第 4 节。
 */
final class TimeLoginActLayout {
    static final float DESIGN_WIDTH = 1920f;
    static final float DESIGN_HEIGHT = 1080f;

    private TimeLoginActLayout() {}

    /** _KW_ROOT_LAYER pos(960,540) size 1920x1080 anchor(0.5,0.5)，左下角正好落在设计原点。 */
    private static final CocosNode ROOT = new CocosNode(0f, 0f, DESIGN_WIDTH, DESIGN_HEIGHT);

    /** _KW_IMG_BG pos(960.96,547) size 1860x956 anchor(0.5,0.5)，页面所有内容的父节点。 */
    private static final CocosNode PANEL = ROOT.child(960.96f, 547f, 1860f, 956f, 0.5f, 0.5f);

    static final Box PANEL_BOX = PANEL.box();
    static final Box TITLE = PANEL.child(728.004f, 813f, 958f, 284f, 0.5f, 0.5f).box();
    static final Box FRESH_TIPS = PANEL.child(147.721f, 758.228f, 177f, 199f, 0.5f, 0.5f).box();
    static final Box CLOSE = PANEL.child(1697.18f, 793.183f, 46f, 46f, 0.5f, 0.5f).box();
    static final Box SLOT_VIEWPORT = PANEL.child(700f, 394f, 1050f, 660f, 0.5f, 0.5f).box();

    private static final CocosNode GOLD_OVER_TIPS =
            PANEL.child(930f, 27.9998f, 449f, 40f, 0.5f, 0.5f);
    static final float GOLD_OVER_TIPS_CENTER_X = GOLD_OVER_TIPS.centerX();
    static final float GOLD_OVER_TIPS_CENTER_Y = GOLD_OVER_TIPS.androidCenterY();
    static final float GOLD_OVER_TIPS_FONT_SIZE = 34f;
    static final int GOLD_OVER_TIPS_COLOR = 0xFF9092BB;

    // ---------------------------------------------------------------- 时段卡

    /** _KW_PANEL_ITEM size 350x670；ListView 横向排列，三张卡正好铺满 1050 宽。 */
    static final float SLOT_WIDTH = 350f;
    static final float SLOT_HEIGHT = 670f;

    private static final CocosNode SLOT = new CocosNode(0f, 0f, SLOT_WIDTH, SLOT_HEIGHT);

    static final Box SLOT_BACKGROUND = slotBox(175f, 326f, 326f, 648f);
    static final Box SLOT_COIN_STACK = slotBox(175f, 425f, 271f, 347f);
    static final Box SLOT_LIGHT = slotBox(175f, 383f, 360f, 584f);
    static final Box SLOT_CLAIMED_STAMP = slotBox(252.972f, 386.599f, 130f, 120f);
    static final Box SLOT_CLAIM_BUTTON = slotBox(175f, 177.1f, 290f, 90f);

    /** _KW_TEXT_TIME anchor(0,0.5)：左对齐，基线中心固定，时段文案由服务端秒数格式化。 */
    static final float SLOT_TIME_LEFT = 37.45f;
    static final float SLOT_TIME_CENTER_Y = slotCenterY(543.82f);
    static final float SLOT_TIME_FONT_SIZE = 33f;

    static final float SLOT_COUNT_CENTER_X = 175f;
    static final float SLOT_COUNT_CENTER_Y = slotCenterY(296f);
    static final float SLOT_COUNT_FONT_SIZE = 60f;

    static final float SLOT_STATE_CENTER_X = 175f;
    static final float SLOT_STATE_CENTER_Y = slotCenterY(178f);
    static final float SLOT_STATE_FONT_SIZE = 48f;
    static final int SLOT_STATE_COLOR = 0xFF737D9A;

    static final float SLOT_COUNTDOWN_CENTER_X = 175f;
    static final float SLOT_COUNTDOWN_CENTER_Y = slotCenterY(199f);
    static final float SLOT_COUNTDOWN_FONT_SIZE = 48f;
    static final int SLOT_COUNTDOWN_COLOR = 0xFFEB2525;

    /** _KW_TEXT_TIME_CD_0「后领取」pos(95.5,-19) 挂在 200x56 的倒计时文本下。 */
    static final float SLOT_COUNTDOWN_SUFFIX_CENTER_X = 75f + 95.5f;
    static final float SLOT_COUNTDOWN_SUFFIX_CENTER_Y = SLOT_COUNTDOWN_CENTER_Y - 28f + (56f + 19f);
    static final float SLOT_COUNTDOWN_SUFFIX_FONT_SIZE = 36f;

    /** Text_3「领 取」pos(145,49) 挂在 290x90 的按钮下。 */
    static final float SLOT_CLAIM_TEXT_CENTER_X = SLOT_CLAIM_BUTTON.left() + 145f;
    static final float SLOT_CLAIM_TEXT_CENTER_Y = SLOT_CLAIM_BUTTON.top() + (90f - 49f);
    static final float SLOT_CLAIM_TEXT_FONT_SIZE = 48f;

    /**
     * 三档时段的文字色，逐值取自 {@code View.lua:237-248}。索引 0/1/2 依次是
     * 早间（realStart &lt; 32400）、午间、晚间。底图里已经烤好档位名，不另绘文字。
     */
    static final int[] SLOT_TIME_COLORS = {0xFF10755B, 0xFFC05220, 0xFF29379D};

    static final int SLOT_TIME_BAND_MORNING_END = 32400;
    static final int SLOT_TIME_BAND_NOON_END = 57600;

    // ---------------------------------------------------------------- 转盘侧栏

    /** _KW_PANEL_ITEM_WHEEL pos(1217,65) size 480x857 anchor(0,0)。 */
    private static final CocosNode WHEEL = PANEL.child(1217f, 65f, 480f, 857f, 0f, 0f);

    static final Box WHEEL_PANEL = WHEEL.child(240f, 381.5f, 506f, 628f, 0.5f, 0.5f).box();
    static final Box WHEEL_BOTTOM = WHEEL.child(240f, 595f, 479f, 479f, 0.5f, 0.5f).box();
    static final Box WHEEL_OVERLAY = WHEEL.child(240f, 595f, 479f, 479f, 0.5f, 0.5f).box();
    static final Box WHEEL_BUTTON = WHEEL.child(240f, 75.9999f, 382f, 176f, 0.5f, 0.5f).box();

    /** Text_3_0「抽 奖」pos(191,119) 挂在 382x176 的按钮下。 */
    static final float WHEEL_BUTTON_TEXT_CENTER_X = WHEEL_BUTTON.left() + 191f;
    static final float WHEEL_BUTTON_TEXT_CENTER_Y = WHEEL_BUTTON.top() + (176f - 119f);
    static final float WHEEL_BUTTON_TEXT_FONT_SIZE = 62f;

    private static final CocosNode MAX_REWARD = WHEEL.child(241.317f, 265.522f, 0f, 0f, 0.5f, 0.5f);
    static final float MAX_REWARD_CENTER_X = MAX_REWARD.centerX();
    static final float MAX_REWARD_CENTER_Y = MAX_REWARD.androidCenterY();
    /** _KW_MAX_REWARD 在 CSB 里 scale=0.8，BMFont 按该比例绘制。 */
    static final float MAX_REWARD_SCALE = 0.8f;

    private static final CocosNode PROGRESS_TIPS =
            WHEEL.child(2.3792f, 10.575f, 474.217f, 200f, 0f, 0f);

    static final Box PROGRESS_1 = PROGRESS_TIPS.child(105f, 138f, 130f, 28f, 0.5f, 0.5f).box();
    static final Box PROGRESS_2 = PROGRESS_TIPS.child(238.8f, 138f, 130f, 28f, 0.5f, 0.5f).box();
    static final Box PROGRESS_3 = PROGRESS_TIPS.child(374.522f, 138f, 130f, 28f, 0.5f, 0.5f).box();

    private static final CocosNode WHEEL_TIPS =
            PROGRESS_TIPS.child(236.126f, 62.5138f, 461f, 87f, 0.5f, 0.5f);
    static final Box WHEEL_TIPS_BOX = WHEEL_TIPS.box();
    /** Txt_dlyl_3_3 pos(116.88,26.35) size 45x63，是提示语里那个数字「3」的字图。 */
    static final Box WHEEL_TIPS_DIGIT = WHEEL_TIPS.child(116.88f, 26.35f, 45f, 63f, 0.5f, 0.5f).box();

    static Box progressBox(int index) {
        return switch (index) {
            case 0 -> PROGRESS_1;
            case 1 -> PROGRESS_2;
            default -> PROGRESS_3;
        };
    }

    private static Box slotBox(float posX, float posY, float width, float height) {
        return SLOT.child(posX, posY, width, height, 0.5f, 0.5f).boxIn(SLOT_HEIGHT);
    }

    /** 这些文本节点 anchor 的 Y 都是 0.5，因此 {@code pos.y} 就是中心，直接翻转即可。 */
    private static float slotCenterY(float posY) {
        return SLOT_HEIGHT - posY;
    }

    /**
     * Cocos 节点：以设计空间（Y 轴向上、原点左下）保存左下角与尺寸。
     * {@code child} 的入参就是 CSB 里的 {@code pos}、{@code size}、{@code anchor}。
     */
    record CocosNode(float left, float bottom, float width, float height) {
        CocosNode child(
                float posX, float posY, float w, float h, float anchorX, float anchorY) {
            return new CocosNode(
                    left + posX - anchorX * w, bottom + posY - anchorY * h, w, h);
        }

        Box box() {
            return boxIn(DESIGN_HEIGHT);
        }

        /** 换算到容器高为 {@code containerHeight} 的 Y 轴向下坐标系。 */
        Box boxIn(float containerHeight) {
            float top = containerHeight - (bottom + height);
            return new Box(left, top, left + width, top + height);
        }

        float centerX() {
            return left + width * 0.5f;
        }

        float androidCenterY() {
            return DESIGN_HEIGHT - (bottom + height * 0.5f);
        }
    }

    record Box(float left, float top, float right, float bottom) {
        float width() {
            return right - left;
        }

        float height() {
            return bottom - top;
        }

        float centerX() {
            return (left + right) * 0.5f;
        }

        float centerY() {
            return (top + bottom) * 0.5f;
        }

        boolean contains(float x, float y) {
            return x >= left && x < right && y >= top && y < bottom;
        }

        Box translated(float dx, float dy) {
            return new Box(left + dx, top + dy, right + dx, bottom + dy);
        }
    }

    record Transform(float scale, float offsetX, float offsetY) {
        static Transform contain(int width, int height) {
            float scale = Math.min(width / DESIGN_WIDTH, height / DESIGN_HEIGHT);
            return new Transform(
                    scale,
                    (width - DESIGN_WIDTH * scale) * 0.5f,
                    (height - DESIGN_HEIGHT * scale) * 0.5f);
        }

        float designX(float x) {
            return (x - offsetX) / scale;
        }

        float designY(float y) {
            return (y - offsetY) / scale;
        }
    }
}
