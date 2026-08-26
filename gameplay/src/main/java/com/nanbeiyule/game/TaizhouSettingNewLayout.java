package com.nanbeiyule.game;

import android.graphics.RectF;

/**
 * {@code MahjongNew/GameLayer/CSB/SettingNew.csb} 的 1920×1080 节点几何。
 *
 * <p>30109 加载的是这份 CSB：{@code BasicMahjong/Modules/Setting/View.lua:103-105} 返回
 * {@code SettingNew.csb}，TaiZhou 的两层子类都没有覆写 {@code getCSBPath}。
 *
 * <p>结构是「右侧滑入的 667 宽菜单 + 2000 宽详情区」。CSB 的 Cocos 坐标 Y 轴自下而上，
 * {@link Frame} 负责换算成顶部原点。
 */
final class TaizhouSettingNewLayout {
    static final float DESIGN_WIDTH = 1920.0f;
    static final float DESIGN_HEIGHT = 1080.0f;

    /** {@code _KW_PANAEL_SET_ROOT}：667×1080，anchor (0,0.5)，从右侧滑入。 */
    static final float MENU_WIDTH = 667.0f;

    /** 0.2 秒 moveInAnimation 之后的 root X。 */
    static final float MENU_OPEN_X = DESIGN_WIDTH - MENU_WIDTH;

    /** {@code _KW_PANAEL_SET_DETAIL}：2000×1080，挂在菜单右侧。 */
    static final float DETAIL_LOCAL_X = MENU_WIDTH;

    static final float DETAIL_WIDTH = 2000.0f;

    /** 顶部原点的绘制盒。 */
    record Box(float centerX, float centerY, float width, float height) {
        float left() {
            return centerX - width / 2.0f;
        }

        float top() {
            return centerY - height / 2.0f;
        }

        RectF rect() {
            return new RectF(left(), top(), left() + width, top() + height);
        }

        Box offset(float dx, float dy) {
            return new Box(centerX + dx, centerY + dy, width, height);
        }

        Box resize(float newWidth, float newHeight) {
            return new Box(centerX, centerY, newWidth, newHeight);
        }

        boolean contains(float x, float y) {
            return x >= left() && x <= left() + width && y >= top() && y <= top() + height;
        }
    }

    /** CSB 父容器：Cocos 左下原点在设计坐标里的位置。 */
    record Frame(float left, float bottom) {
        Frame at(float x, float y) {
            return new Frame(left + x, bottom + y);
        }

        /** anchor(0.5,0.5) 子节点。 */
        Box box(float x, float y, float width, float height) {
            return new Box(left + x, DESIGN_HEIGHT - (bottom + y), width, height);
        }

        /** anchor(0,0.5) 子节点。 */
        Box boxLeft(float x, float y, float width, float height) {
            return box(x + width / 2.0f, y, width, height);
        }

        /** anchor(0,0) 子节点。 */
        Box boxCorner(float x, float y, float width, float height) {
            return box(x + width / 2.0f, y + height / 2.0f, width, height);
        }
    }

    /** 菜单面板自身的坐标系。 */
    static final Frame ROOT = new Frame(0.0f, 0.0f);

    static final Box BACKGROUND = ROOT.box(MENU_WIDTH / 2.0f, 540.0f, MENU_WIDTH, 1080.0f);

    /** {@code set_mah_set_bg.png} 665×1080 的 scale9。 */
    static final float[] BACKGROUND_CAPS = {255.0f, 356.0f, 155.0f, 368.0f};

    /** {@code set_mah_set_bg3.png} 104×104。 */
    static final float[] CARD_CAPS = {32.0f, 33.0f, 40.0f, 38.0f};

    /** {@code set_mah_set_select_2.png} 187×151，六个卡片各自的 capInsets。 */
    static final float[] CARD_FRAME_CAPS = {61.0f, 50.0f, 65.0f, 1.0f};

    static final float[] WIDE_CARD_FRAME_CAPS = {17.0f, 26.0f, 155.0f, 75.0f};

    /** {@code set_mah_set_select_1.png} 183×147。 */
    static final float[] CARD_SELECTED_CAPS = {61.0f, 47.0f, 61.0f, 1.0f};

    static final float[] WIDE_CARD_SELECTED_CAPS = {17.0f, 26.0f, 147.0f, 71.0f};

    /** {@code _KW_PANAEL_SET_SWITCH}：588×90 的基础功能开关区。 */
    private static final Frame SWITCH = ROOT.at(39.2196f, 853.46f);

    static final Box VOICE_LABEL = SWITCH.boxLeft(15.0f, 72.0f, 146.0f, 42.0f);
    static final Box VOICE_SWITCH = SWITCH.box(235.0f, 75.0f, 126.75f, 59.25f);
    static final Box TRUST_LABEL = SWITCH.boxLeft(15.0f, 5.0f, 146.0f, 42.0f);
    static final Box TRUST_BUTTON = SWITCH.box(243.261f, 5.0f, 150.0f, 53.0f);

    /** {@code _KW_BTN_DISMISS}：包厢场退出，恒用 {@code set_mah_btn_quit.png}。 */
    static final Box DISMISS_BUTTON = ROOT.box(333.0f, 74.0f, 327.0f, 132.0f);

    /** {@code _KW_BTN_BACK_NEW}：新金币场返回大厅，{@code set_mah_btn_back.png}。 */
    static final Box BACK_BUTTON = ROOT.box(333.5f, 72.95f, 327.0f, 132.0f);

    /** {@code _KW_BTN_SAVE}：进入详情页后替换底部按钮。 */
    static final Box SAVE_BUTTON = ROOT.box(333.0f, 74.0f, 387.0f, 132.0f);

    static final float[] SAVE_BUTTON_CAPS = {127.0f, 43.0f, 131.0f, 44.0f};

    static final Box CLOSE_BUTTON = ROOT.box(605.327f, 1025.28f, 99.0f, 102.0f);

    /**
     * 全屏详情态的关闭按钮：{@code View.lua:moveInDetailAnimation} 把它移到
     * {@code winSize.width - 10 - width/2}，纵向仍是 TopEdge。
     */
    static Box detailCloseButton(TaizhouSettingNewViewport viewport) {
        return new Box(
                viewport.right() - 10.0f - 49.5f,
                viewport.topOffset() + 54.72f,
                99.0f,
                102.0f);
    }

    /** CSB 里 Text_16 / 开启关闭 的 CColor(187,110,68)。 */
    static final int TEXT_COLOR = 0xFFBB6E44;

    /** CSB 里行标题的 CColor(191,119,79)。 */
    static final int LABEL_COLOR = 0xFFBF774F;

    /** {@code TEXT_COLOR_SELECTED = cc.c3b(255,255,255)}。 */
    static final int SELECTED_TEXT_COLOR = 0xFFFFFFFF;

    /**
     * 左侧菜单六项，值与 {@code View.lua:54-61} 的 {@code DATAIL_TAGS} 逐值对应。
     *
     * <p>第 6 项节点名叫「高级设置」，但 CSB 里它的选中框覆盖整块 608×291 的基础功能面板，
     * 标题文字是「基础功能」，底部 609×75 才是可点的高级设置条。
     */
    enum Page {
        MAH(1, "麻将", 170.001f, 538.0f, 295.0f, 290.0f, 142.5f, 30.0f, 142.5f, 260.0f, 74.0f),
        TABLE(2, "桌布", 490.001f, 538.0f, 295.0f, 290.0f, 142.5f, 30.0f, 142.5f, 260.0f, 74.0f),
        ANIMATION(3, "动画", 114.0f, 265.0f, 185.0f, 150.0f, 93.0f, 30.0f, 89.9994f, 103.002f, 74.0f),
        EFFECTS(4, "特效", 335.501f, 265.0f, 185.0f, 150.0f, 93.0f, 30.0f, 91.0f, 103.002f, 75.0f),
        HAND(5, "摆牌", 550.0f, 265.0f, 185.0f, 150.0f, 92.0f, 30.0f, 89.9999f, 103.002f, 74.0f),
        ADVANCED(6, "基础功能", 331.92f, 771.49f, 609.0f, 75.0f, 304.5f, 38.475f, 304.5f, 255.75f,
                161.0f);

        private final int tag;
        private final String label;
        private final Box card;
        private final Frame frame;
        private final Box plate;
        private final Box title;

        Page(
                int tag,
                String label,
                float centerX,
                float cocosCenterY,
                float width,
                float height,
                float plateX,
                float plateY,
                float titleX,
                float titleY,
                float titleWidth) {
            this.tag = tag;
            this.label = label;
            card = ROOT.box(centerX, cocosCenterY, width, height);
            frame = ROOT.at(centerX - width / 2.0f, cocosCenterY - height / 2.0f);
            boolean wide = tag == 6;
            plate = frame.box(plateX, plateY, wide ? 194.0f : 144.0f, wide ? 50.0f : 36.0f);
            title = frame.box(titleX, titleY, titleWidth, wide ? 47.0f : 42.0f);
        }

        int tag() {
            return tag;
        }

        String label() {
            return label;
        }

        /** {@code set_mah_set_bg3.png} 的卡片底。 */
        Box card() {
            return card;
        }

        /** {@code _KW_SELECT_IMG_BG}：常态描边，恒显示。 */
        Box frame() {
            return tag == 6 ? frame.box(307.18f, 147.54f, 608.0f, 291.0f) : card;
        }

        /** {@code _KW_TYPE_SELECT_IMG}：选中描边。 */
        Box selectedFrame() {
            return tag == 6
                    ? frame.box(306.205f, 147.533f, 608.0f, 291.0f)
                    : card.offset(0.0f, -2.0f);
        }

        /** {@code Image_27}：卡片底部的「高级设置」字牌。 */
        Box plate() {
            return plate;
        }

        /** {@code Text_16}：卡片标题。 */
        Box title() {
            return title;
        }

        float titleSize() {
            return tag == 6 ? 40.0f : 36.0f;
        }

        float[] frameCaps() {
            return tag == 6 ? WIDE_CARD_FRAME_CAPS : CARD_FRAME_CAPS;
        }

        float[] selectedCaps() {
            return tag == 6 ? WIDE_CARD_SELECTED_CAPS : CARD_SELECTED_CAPS;
        }

        /** {@code _KW_SCAN_MAH_IMG_BG}：麻将卡片里 0.85 缩放的立牌预览。 */
        Box preview() {
            return switch (this) {
                case MAH -> frame.box(146.7f, 151.667f, 101.0f * 0.85f, 144.0f * 0.85f);
                case TABLE -> frame.box(150.0f, 150.0f, 386.0f * 0.6f, 228.0f * 0.6f);
                default -> null;
            };
        }

        static Page ofTag(int tag) {
            for (Page page : values()) {
                if (page.tag == tag) {
                    return page;
                }
            }
            throw new IllegalArgumentException("unknown DATAIL_TAGS value: " + tag);
        }
    }

    private TaizhouSettingNewLayout() {}
}
