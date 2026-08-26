package com.nanbeiyule.game.mahjong;

/**
 * {@code MahjongNew/GameLayer/CSB/TableClockLayer.csb} center direction-clock geometry.
 *
 * <p>The original CSB uses a 1920x1080 Cocos bottom-up design space. Values here are Android
 * top-left design coordinates derived from the local FlatBuffer CSB extraction in
 * {@code android/docs/reverse-runs/zhejiang-taizhou-mahjong/local-restore/}.
 */
public final class TaizhouCenterClockLayout {
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
    }

    public static final Node ROOT =
            new Node("_KW_PANAEL_CLOCK", 960.575989f, 479.736023f, 205.0f, 205.0f);

    public static final Node TABLE_BG =
            new Node("_KW_IMG_TABLEBG", 960.575989f, 479.736023f, 205.0f, 205.0f);

    public static final Node NORTH =
            new Node("_KW_IMG_NORTH", 959.075989f, 546.236023f, 58.0f, 44.0f);

    public static final Node SOUTH =
            new Node("_KW_IMG_SOUTH", 960.075989f, 411.236023f, 58.0f, 44.0f);

    public static final Node WEST =
            new Node("_KW_IMG_WEST", 892.075989f, 479.236023f, 58.0f, 44.0f);

    public static final Node EAST =
            new Node("_KW_IMG_EAST", 1028.075989f, 479.236023f, 58.0f, 44.0f);

    public static final Node NORTH_LIGHT =
            new Node("_KW_IMG_DIR_NORTH", 960.348892f, 544.235825f, 241.0f, 122.0f);

    public static final Node SOUTH_LIGHT =
            new Node("_KW_IMG_DIR_SOUTH", 961.982491f, 411.729019f, 241.0f, 122.0f);

    public static final Node WEST_LIGHT =
            new Node("_KW_IMG_DIR_WEST", 892.665989f, 477.532021f, 121.0f, 241.0f);

    public static final Node EAST_LIGHT =
            new Node("_KW_IMG_DIR_EAST", 1027.903992f, 477.590622f, 121.0f, 241.0f);

    /** Original XGSJ background source size after TableClockView skin replacement. */
    public static final float XGSJ_BACKGROUND_SIZE = 267.0f;

    /** XGSJ TableClockView scales the replacement skin to 0.9. */
    public static final float XGSJ_SKIN_SCALE = 0.9f;

    public static final float REMAINING_LABEL_X = 1044.0f;

    public static final float REMAINING_LABEL_BASELINE = 505.0f;

    public static final float REMAINING_VALUE_X = 1080.0f;

    public static final float REMAINING_VALUE_BASELINE = 565.0f;

    /** {@code MahjongLayer.csb/_KW_PANEL_MAH_COUNTS/KW_LEFTTEXT_IMG}. */
    public static final Node SURPLUS_LABEL =
            new Node("KW_LEFTTEXT_IMG", 1112.952026f, 438.023987f, 104.0f, 34.0f);

    /** {@code MahjongLayer.csb/_KW_PANEL_MAH_COUNTS/_KW_MAH_COUTNS}. */
    public static final Node SURPLUS_COUNT =
            new Node("_KW_MAH_COUTNS", 1121.952026f, 485.228287f, 118.0f, 54.0f);

    /**
     * 生牌态时整块牌墙剩余面板上移的 Cocos Y 量。
     *
     * <p>原版 {@code TaiZhou/BasicTaiZhouMahjong/Modules/GameLayer/View2D/UIMahLayer.luac:27-33}
     * 的 {@code onEventShengPaiAni} 把 {@code _KW_PANEL_MAH_COUNTS} 设成
     * {@code 原Y + 40}，{@code onEventClearTable}（同文件 :18-24）复位。台州麻将
     * {@code isHaveShengPaiJieDuan() = true}（{@code TaiZhou/TaiZhouMahjong/Data/ConfigData.luac:9}），
     * 生牌块 {@code _KW_IMG_LEFT_SHENG_PAI} 占 Cocos y 495..605，不上移就会和牌墙剩余糊在一起。
     */
    public static final float SHENG_PAI_SURPLUS_LIFT_COCOS_Y = 40.0f;

    /** {@code _KW_MAH_SURPLUS_NODE}; Lua adds one lie-down back tile here. */
    public static final float SURPLUS_TILE_X = 1087.952026f;

    public static final float SURPLUS_TILE_COCOS_Y = 618.976013f;

    public static final float SURPLUS_TILE_SCALE = 0.3f;

    private TaizhouCenterClockLayout() {}
}
