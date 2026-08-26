package com.nanbeiyule.game;

/**
 * Original 1920x1080 geometry of the CanHuMahs 听牌可胡提示层, recovered from
 * {@code MahjongNew/GameLayer/CSB/CanHuMahs.csb} (the 30109 layer, not the
 * HangZhou {@code CanHuMahsNew.csb}) and the grid math of
 * {@code BasicMahjong/Modules/GameLayer/CanHuMahsUI.luac:initUI}. Local
 * coordinates are relative to the background's content box; Y stays in the
 * Cocos bottom-up axis.
 */
public final class TaizhouCanHuLayout {
    /** CSB {@code _bg}: selected-hand branch pos (960,230), anchor (0.5,0). */
    public static final float BG_ANCHOR_X = 960.0f;

    public static final float BG_ANCHOR_COCOS_Y = 230.0f;

    public static final float SELECTED_BG_ANCHOR_POINT_X = 0.5f;

    public static final float SELECTED_BG_ANCHOR_POINT_Y = 0.0f;

    /**
     * Original click-听 branch:
     * {@code CanHuMahsUI.luac:initUI} sets {@code _bg} anchor (1,0.5), then
     * moves it to {@code CanHuMahs.csb _model} at (1810,622).
     */
    public static final float TING_BUTTON_BG_ANCHOR_X = 1810.0f;

    public static final float TING_BUTTON_BG_ANCHOR_COCOS_Y = 622.0f;

    public static final float TING_BUTTON_BG_ANCHOR_POINT_X = 1.0f;

    public static final float TING_BUTTON_BG_ANCHOR_POINT_Y = 0.5f;

    /** CSB {@code _bg} frame {@code can_hu_bg.png}. */
    public static final String BG_FRAME = "can_hu_bg.png";

    /** CSB {@code _line} frame {@code division_line.png}. */
    public static final String LINE_FRAME = "division_line.png";

    /** Lua {@code self._line:setPosition(190, bgHeight)}. */
    public static final float LINE_LOCAL_X = 190.0f;

    /** Lua {@code self._line:setContentSize(2, bgHeight)}. */
    public static final float LINE_WIDTH = 2.0f;

    /** Lua {@code hu.png} logo frame. */
    public static final String HU_LOGO_FRAME = "hu.png";

    /** Lua logo position {@code (-35, bgHeight/2)} with anchor (0,0.5). */
    public static final float HU_LOGO_LOCAL_X = -35.0f;

    /** Lua logo {@code setScale(0.7)}. */
    public static final float HU_LOGO_SCALE = 0.7f;

    /** Source size of the {@code hu.png} frame. */
    public static final float HU_LOGO_WIDTH = 389.0f;

    public static final float HU_LOGO_HEIGHT = 338.0f;

    /** Lua first tile local position {@code (260, bgHeight-120)}, anchor (0.5,0.5). */
    public static final float TILE_FIRST_LOCAL_X = 260.0f;

    public static final float TILE_FIRST_TOP_INSET = 120.0f;

    /** Lua column step {@code x + 230} and row step {@code y - 190}. */
    public static final float TILE_STEP_X = 230.0f;

    public static final float TILE_STEP_Y = 190.0f;

    /** Lua {@code temp:setScale(0.7)} for every UIMah. */
    public static final float TILE_SCALE = 0.7f;

    /** Lua {@code bgWidth = (width + 1) * 235} column unit. */
    public static final float COLUMN_UNIT = 235.0f;

    /** Lua {@code bgHeight = height * 200} row unit. */
    public static final float ROW_UNIT = 200.0f;

    /** Lua one/two-row padding {@code bgHeight = bgHeight + 25}. */
    public static final float SHORT_GRID_PADDING = 25.0f;

    /** Lua 胡任意牌 single-cell width {@code bgWidth = 375}. */
    public static final float ANY_TILE_BG_WIDTH = 375.0f;

    /** Lua whole-node scale for 21..30 tiles ({@code setScale(0.85)}). */
    public static final float LARGE_GRID_SCALE = 0.85f;

    /** Lua whole-node scale for more than 30 tiles ({@code setScale(0.7)}). */
    public static final float HUGE_GRID_SCALE = 0.7f;

    /** {@code UIMahConfig2D.MahRenYiFrameName}: face frame of the 255 any-tile. */
    public static final String ANY_TILE_FRAME = "mahlayer_mah_any.png";

    /** Source size of the {@code mahlayer_mah_any.png} frame. */
    public static final float ANY_TILE_WIDTH = 64.0f;

    public static final float ANY_TILE_HEIGHT = 134.0f;

    private TaizhouCanHuLayout() {}
}
