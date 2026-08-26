package com.nanbeiyule.game.mahjong;

/**
 * {@code MahjongNew/GameLayer/CSB/AddMultipleLayer.csb} 的 1920×1080 节点几何。
 *
 * <p>注意不是通用的 {@code Common/CSB/GameBase/AddMultipleLayer.csb}：麻将子类
 * {@code BasicMahjong/Modules/AddMultiple/View.luac:5-7} 覆盖了 {@code getCSBPath()}，
 * 指向 {@code cocosStudio/MahjongNew/GameLayer/CSB/AddMultipleLayer.csb}。两份 CSB 的按钮
 * Cocos Y 不同（通用 475 / 麻将 420），用错会让加倍条上移 55，压住中央风位盘和右侧生牌信息。
 *
 * <p>开局加倍选择层，实机画面见
 * {@code android/docs/evidence/taizhou-live-round-20260811/01-add-multiple-choice.png}。
 * 该层属于金币场（{@code GameSub.lua:140} 的 30400 {@code IsGoldMode = "BOTYes"}），房卡场
 * 30109 不出现；这里先按原版几何实现，接入条件由调用方按玩法判定。
 *
 * <p>CSB 的三个按钮挂在 {@code _KW_PANEL_ADD_MULTIPLE_BTNS} 下，该面板本身位于 {@code x=960}，
 * 因此按钮的设计坐标是面板 x 加上各自的相对 x。Cocos 的 Y 轴自下而上，这里换算成顶部原点。
 */
public final class TaizhouMultipleLayout {
    public static final float DESIGN_WIDTH = 1920.0f;
    public static final float DESIGN_HEIGHT = 1080.0f;

    /** {@code _KW_PANEL_ADD_MULTIPLE_BTNS} 的挂点。 */
    private static final float BUTTON_PANEL_X = 960.0f;

    /**
     * 三个按钮共用的 Cocos Y。
     *
     * <p>取自麻将版 CSB：{@code _KW_BTN_NOT_ADD_MULTIPLE/_KW_BTN_ADD_MULTIPLE} 的
     * {@code pos=(...,420)}，{@code _KW_BTN_SUPER_ADD_MULTIPLE} 同为 420。
     */
    private static final float BUTTON_COCOS_Y = 420.0f;

    /** 一个矩形按钮或标牌。 */
    public record Node(String name, float centerX, float centerY, float width, float height) {
        public float left() {
            return centerX - width / 2.0f;
        }

        public float top() {
            return centerY - height / 2.0f;
        }

        public float right() {
            return centerX + width / 2.0f;
        }

        public float bottom() {
            return centerY + height / 2.0f;
        }

        public boolean contains(float x, float y) {
            return x >= left() && x <= right() && y >= top() && y <= bottom();
        }
    }

    /** 不加倍：{@code _KW_BTN_NOT_ADD_MULTIPLE} 相对 x=-350，308×133。 */
    public static final Node BUTTON_NONE =
            button("_KW_BTN_NOT_ADD_MULTIPLE", -350.0f, 308.0f, 133.0f);

    /** 加倍：{@code _KW_BTN_ADD_MULTIPLE} 相对 x=0，308×132。 */
    public static final Node BUTTON_ADD = button("_KW_BTN_ADD_MULTIPLE", 0.0f, 308.0f, 132.0f);

    /** 超级加倍：{@code _KW_BTN_SUPER_ADD_MULTIPLE} 相对 x=373.483，360×132。 */
    public static final Node BUTTON_SUPER =
            button("_KW_BTN_SUPER_ADD_MULTIPLE", 373.483f, 360.0f, 132.0f);

    /**
     * 四个方位的结果标牌 {@code _KW_IMG_ADD_MULTIPLE_1..4}，各 {@code 228×78}。
     *
     * <p>父面板挂点分别是 {@code (380,540) (960,0) (1540,540) (960,1080)}，标牌相对 y 依次为
     * {@code 125 370 125 -285}，对应实机画面里左、下、右、上四家的「不加倍/加倍」提示。
     *
     * <p>序号按原版 {@code GameDefine.LOCAL_SEAT}（LEFT 1 / BOTTOM 2 / RIGHT 3 / TOP 4）排列，
     * 而不是「左上右下」。Cocos 的 y 轴自下而上：{@code _2} 的父挂点 {@code y=0} 是屏幕下沿，
     * 所以 {@code _2} 是**本家（下）**；{@code _4} 的父挂点 {@code y=1080} 是上沿，所以 {@code _4}
     * 是**对家（上）**。此前 {@code _2}/{@code _4} 被写反，导致上下两家的标牌互换（相差 400+ 设计
     * 单位）；截图实测上家标牌 y≈296，与 {@code _4} 的 285 吻合。
     */
    public static final Node PLATE_LEFT = plate("_KW_IMG_ADD_MULTIPLE_1", 380.0f, 540.0f + 125.0f);

    public static final Node PLATE_BOTTOM =
            plate("_KW_IMG_ADD_MULTIPLE_2", 960.0f, 0.0f + 370.0f);

    public static final Node PLATE_RIGHT = plate("_KW_IMG_ADD_MULTIPLE_3", 1540.0f, 540.0f + 125.0f);

    public static final Node PLATE_TOP = plate("_KW_IMG_ADD_MULTIPLE_4", 960.0f, 1080.0f + -285.0f);

    /** 标牌统一尺寸。 */
    public static final float PLATE_WIDTH = 228.0f;

    public static final float PLATE_HEIGHT = 78.0f;

    private TaizhouMultipleLayout() {}

    private static Node button(String name, float relativeX, float width, float height) {
        return new Node(
                name,
                BUTTON_PANEL_X + relativeX,
                DESIGN_HEIGHT - BUTTON_COCOS_Y,
                width,
                height);
    }

    private static Node plate(String name, float centerX, float cocosCenterY) {
        return new Node(
                name, centerX, DESIGN_HEIGHT - cocosCenterY, PLATE_WIDTH, PLATE_HEIGHT);
    }
}
