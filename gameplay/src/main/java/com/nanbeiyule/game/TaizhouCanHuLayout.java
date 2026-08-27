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

    /**
     * CSB {@code _bg} 的 {@code Scale9OriginX/Y = 33,33}、{@code Scale9Width/Height = 34,34}：
     * 100x100 的米色圆角底只能九宫拉伸，直接整图拉到 705x225 圆角就被拉成椭圆。
     */
    public static final float BG_CAP_X = 33.0f;

    public static final float BG_CAP_Y = 33.0f;

    public static final float BG_CAP_WIDTH = 34.0f;

    public static final float BG_CAP_HEIGHT = 34.0f;

    /** CSB {@code _line} frame {@code division_line.png}. */
    public static final String LINE_FRAME = "division_line.png";

    /** CSB {@code _line} 的 {@code Scale9OriginX/Y = 0,63}、{@code Scale9Width/Height = 2,66}。 */
    public static final float LINE_CAP_X = 0.0f;

    public static final float LINE_CAP_Y = 63.0f;

    public static final float LINE_CAP_WIDTH = 2.0f;

    public static final float LINE_CAP_HEIGHT = 66.0f;

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

    /** Lua {@code textX = x + 50}: 每格文字左缘相对牌位中心的偏移。 */
    public static final float INFO_TEXT_LOCAL_X = 50.0f;

    /**
     * Lua {@code huInfoPositionY}：按段数决定每段相对牌位中心的行基线偏移。
     * 只有「N张」一段时用 {@code {-30}}。
     */
    public static final float[][] INFO_ROW_OFFSETS = {
        {-30.0f},
        {0.0f, -70.0f},
        {20.0f, -30.0f, -80.0f},
    };

    /** {@code can_hu_mah_info.fnt} 的 {@code size=46 lineHeight=55 base=44}（FZCuYuan-M03S）。 */
    public static final float INFO_FONT_SIZE = 46.0f;

    public static final float INFO_LINE_HEIGHT = 55.0f;

    public static final float INFO_BASE_LINE = 44.0f;

    /** {@code can_hu_mah_info.png} 字模填充色（数字）。 */
    public static final int INFO_NUMBER_COLOR = 0xffad1c13;

    /** {@code can_hu_mah_info_2.png} 字模填充色（单位）。 */
    public static final int INFO_UNIT_COLOR = 0xff3d2916;

    /** Lua {@code GameModule:canHuInfo} 的 {@code bShowFanNum} 段单位。 */
    public static final String FAN_UNIT = "台";

    /** Lua {@code GameModule:canHuInfo} 的 {@code bShowHuNum} 段单位。 */
    public static final String HU_UNIT = "胡";

    /** Lua {@code data[i].huInfo = data[i].huInfo .. "张"}。 */
    public static final String SURPLUS_UNIT = "张";

    /** {@code UIMahConfig2D.MahRenYiFrameName}: face frame of the 255 any-tile. */
    public static final String ANY_TILE_FRAME = "mahlayer_mah_any.png";

    /** Source size of the {@code mahlayer_mah_any.png} frame. */
    public static final float ANY_TILE_WIDTH = 64.0f;

    public static final float ANY_TILE_HEIGHT = 134.0f;

    private TaizhouCanHuLayout() {}
}
