package com.nanbeiyule.game.mahjong;

/**
 * Canvas positions for the original table-centre throw-chip display.
 *
 * <p>Geometry is ported from the original 2D throw-chip layer instead of being estimated:
 * {@code MahjongNew/GameLayer/CSB/MahjongAniLayer.csb} pins both {@code _KW_PANEL_SHAI_ZI_1}
 * and {@code _KW_PANEL_SHAI_ZI_2} at Cocos {@code (960, 540)} with size {@code (0,0)} and
 * anchor {@code (0,0)}, and {@code BasicMahjong/Modules/GameLayer/AnimationLayer.luac:24-27}
 * declares the local dice offsets {@code POS_SHAI_ZI[1] = {(-145, 290)}} and
 * {@code POS_SHAI_ZI[2] = {(-200, 290), (-90, 290)}}.
 *
 * <p>Values below are those sums flipped into Android's top-down design axis, matching the
 * convention of {@link TaizhouCenterClockLayout}: a Cocos Y of {@code 540 + 290 = 830} becomes
 * {@code 1080 - 830 = 250}.
 */
public final class TaizhouDiceLayout {
    public record Node(float centerX, float centerY, float width, float height) {
        public float left() {
            return centerX - width / 2.0f;
        }

        public float top() {
            return centerY - height / 2.0f;
        }
    }

    /** Cocos position of {@code _KW_PANEL_SHAI_ZI_1} and {@code _KW_PANEL_SHAI_ZI_2}. */
    private static final float PANEL_COCOS_X = 960.0f;

    private static final float PANEL_COCOS_Y = 540.0f;

    /** Shared {@code POS_SHAI_ZI} Y offset; every original dice slot uses the same height. */
    private static final float OFFSET_COCOS_Y = 290.0f;

    /** Top-down design Y shared by all dice slots. */
    public static final float DESIGN_Y =
            TaizhouMahjongTableLayout.designY(PANEL_COCOS_Y + OFFSET_COCOS_Y);

    /**
     * The original throw-chip animation drives {@code saizi_ani}, whose frames are not present in
     * the recovered resource tree. The recovered still atlas
     * {@code GameLayer/Image/HuZhou/saizi.png} exposes 320x240 faces; this 0.525 uniform downscale
     * is a Nanbei-side display choice, not an original value.
     */
    public static final float SPRITE_WIDTH = 168.0f;

    public static final float SPRITE_HEIGHT = 126.0f;

    public static final Node SINGLE = slot(-145.0f);
    public static final Node DOUBLE_LEFT = slot(-200.0f);
    public static final Node DOUBLE_RIGHT = slot(-90.0f);

    /**
     * {@code POS_SHAI_ZI} only keys 1 and 2, and {@code _KW_PANEL_SHAI_ZI_} only binds indices 1
     * to 2, so the original 2D layer never places a third die. These slots keep the original 110
     * unit spacing centred on the single-die offset and are inferred Nanbei geometry.
     */
    public static final Node TRIPLE_LEFT = slot(-255.0f);

    public static final Node TRIPLE_MIDDLE = slot(-145.0f);
    public static final Node TRIPLE_RIGHT = slot(-35.0f);

    private static Node slot(float offsetCocosX) {
        return new Node(PANEL_COCOS_X + offsetCocosX, DESIGN_Y, SPRITE_WIDTH, SPRITE_HEIGHT);
    }

    public static Node nodeFor(int count, int index) {
        if (count == 1) {
            return SINGLE;
        }
        if (count == 2) {
            return index == 0 ? DOUBLE_LEFT : DOUBLE_RIGHT;
        }
        return switch (index) {
            case 0 -> TRIPLE_LEFT;
            case 1 -> TRIPLE_MIDDLE;
            case 2 -> TRIPLE_RIGHT;
            default -> throw new IllegalArgumentException("dice index is outside layout");
        };
    }

    private TaizhouDiceLayout() {}
}
