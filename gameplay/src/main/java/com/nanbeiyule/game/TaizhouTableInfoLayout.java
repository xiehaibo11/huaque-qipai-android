package com.nanbeiyule.game;

/**
 * Original 1920x1080 geometry of the TableInfo 生牌信息层, recovered from
 * {@code MahjongNew/GameLayer/CSB/TaiZhou/TableInfo.csb}. The CSB node chain is
 * {@code KW_PANEL_GAME_INFO(960,540)} (zero-size) →
 * {@code _KW_IMG_LEFT_SHENG_PAI(100,10)} anchor (0,0.5) size 200x110; children
 * are positioned relative to that panel's origin. Y values stay in the Cocos
 * bottom-up axis; renderers flip them with
 * {@link com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout#designY(float)}.
 */
public final class TaizhouTableInfoLayout {
    /** {@code _KW_IMG_LEFT_SHENG_PAI} left edge: 960+100, anchor (0,0.5). */
    public static final float SHENG_PAI_PANEL_LEFT = 1060.0f;

    /** {@code _KW_IMG_LEFT_SHENG_PAI} vertical centre: 540+10. */
    public static final float SHENG_PAI_PANEL_CENTER_COCOS_Y = 550.0f;

    public static final float SHENG_PAI_PANEL_WIDTH = 200.0f;

    public static final float SHENG_PAI_PANEL_HEIGHT = 110.0f;

    /** {@code KW_IMG_SP_BG} frame {@code tz_shengPai.png} (150x42, scale 1.1). */
    public static final String SP_BG_FRAME = "tz_shengPai.png";

    /** Centre of {@code KW_IMG_SP_BG}: panel origin (1060,495) + (100,75). */
    public static final float SP_BG_CENTER_X = 1160.0f;

    public static final float SP_BG_CENTER_COCOS_Y = 570.0f;

    public static final float SP_BG_WIDTH = 165.0f;

    public static final float SP_BG_HEIGHT = 46.2f;

    /** {@code KW_IMG_LEFT_BG} frame {@code mah_img_surplus.png} (104x34, scale 1.2). */
    public static final String SURPLUS_BG_FRAME = "mah_img_surplus.png";

    /** Left edge of {@code KW_IMG_LEFT_BG}: panel left + 15, anchor (0,0.5). */
    public static final float SURPLUS_BG_LEFT = 1075.0f;

    public static final float SURPLUS_BG_CENTER_COCOS_Y = 525.0f;

    public static final float SURPLUS_BG_WIDTH = 124.8f;

    public static final float SURPLUS_BG_HEIGHT = 40.8f;

    /** Left edge of {@code _KW_TXT_LEFT_SHENG_PAI}: panel left + 135, anchor (0,0.5). */
    public static final float COUNT_LEFT = 1195.0f;

    public static final float COUNT_CENTER_COCOS_Y = 527.0f;

    /** {@code mah_number-export.fnt} node scale. */
    public static final float COUNT_SCALE = 0.8f;

    private TaizhouTableInfoLayout() {}
}
