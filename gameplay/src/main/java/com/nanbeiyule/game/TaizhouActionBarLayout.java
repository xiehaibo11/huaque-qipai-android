package com.nanbeiyule.game;

import com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout;

/**
 * Geometry of the Taizhou mahjong action bar, recovered node-for-node from the
 * original {@code MahjongNew/MahLayer/CSB/MahLayer.csb} (CocoStudio 2.1.0.0).
 * The task-brief reference {@code CenterBtnsLayer.csb} only contains the
 * waiting-state buttons (invite/start/copy/settle/continue); the action
 * buttons live in {@code _KW_ACTION_PANEL} here, and the candidate panel in
 * {@code _KW_ACTION_COMBS_PANEL}.
 *
 * <p>All values are 1920x1080 design coordinates with Cocos' bottom-up Y axis,
 * matching the CSB dump. {@link #slotRectAndroid(int)} converts to top-down
 * Android Y for Canvas drawing. The original slide-in/out animations and the
 * {@code adaptForLiuHai} notch shift are out of scope for this component; the
 * hidden resting anchors are recorded only so the off-screen state is lossless.
 *
 * <p>Comb-cell constants come from
 * {@code BasicMahjong/Modules/GameLayer/View2D/UIMahLayerAction.luac}
 * ({@code createActionCombPanel}: margin 25, disX 20, disY 20, max 4 per line,
 * comb scale 0.7), and the tile metrics are locked to
 * {@code OriginalMahjongTileGeometry} by {@code TaizhouActionBarLayoutTest}.
 */
public final class TaizhouActionBarLayout {
    // _KW_ACTION_PANEL at (1920,0); _KW_ACTION_MOVE_PANEL 1200x200 anchor (1,0)
    // slides to _KW_ACTION_IN_TARGET_POS (-100,250) when shown.
    public static final float BAR_LEFT = 620.0f; // 1920 + (-100) - 1200
    public static final float BAR_BOTTOM = 250.0f;
    public static final float BAR_WIDTH = 1200.0f;
    public static final float BAR_HEIGHT = 200.0f;
    /** _KW_ACTION_MOVE_PANEL initial position X: the off-screen resting anchor. */
    public static final float BAR_HIDDEN_ANCHOR_X = 2200.0f;

    /** _KW_ACTION_BTN_1..7 are 200x200, anchor (0.5,0.5), local Y 100. */
    public static final float BUTTON_SIZE = 200.0f;
    public static final float SLOT_CENTER_Y = 350.0f; // 250 + 100

    /**
     * Design X of each button slot, from the CSB local X 1100/870/620/370/120/
     * -130/-130 shifted by the shown panel origin 620. The original compacts
     * visible actions into slots 1..N ascending by action id
     * ({@code UIMahLayerAction.luac:12-35}).
     */
    public static final float[] SLOT_CENTER_X = {
        1720.0f, 1490.0f, 1240.0f, 990.0f, 740.0f, 490.0f, 490.0f,
    };

    // _KW_ACTION_COMBS_PANEL at (1950,0); move panel 1280x200 anchor (1,0),
    // shown at _KW_ACTION_COMBS_IN_TARGET_POS (-100,250).
    public static final float COMBS_LEFT = 570.0f; // 1950 + (-100) - 1280
    public static final float COMBS_BOTTOM = 250.0f;
    public static final float COMBS_WIDTH = 1280.0f;
    public static final float COMBS_HIDDEN_ANCHOR_X = 1900.0f;

    // _KW_ACTION_COMBS_BACK local (1092,0) anchor (1,0): right edge and bottom.
    public static final float COMBS_BACK_RIGHT = 1662.0f; // 570 + 1092
    public static final float COMBS_BACK_BOTTOM = 250.0f;

    // _KW_ACTION_CANCEL_BTN local (1244,107) anchor (1,0.5), 150x150.
    public static final float CANCEL_RIGHT = 1814.0f; // 570 + 1244
    public static final float CANCEL_CENTER_Y = 357.0f; // 250 + 107
    public static final float CANCEL_SIZE = 150.0f;

    // createActionCombPanel (UIMahLayerAction.luac:235-286).
    public static final float COMB_MARGIN = 25.0f;
    public static final float COMB_GAP_X = 20.0f;
    public static final float COMB_GAP_Y = 20.0f;
    public static final int COMB_MAX_PER_LINE = 4;
    public static final float COMB_SCALE = 0.7f;

    /**
     * Lie-tile metrics from {@code UIMahConfig2D.LIE_FACE_UP_VERTICAL_TOUP}
     * (Back InitialThick 12 + FaceGround InitialThick 15, default ThickRatio 0),
     * verified against {@code OriginalMahjongTileGeometry} in the layout test.
     */
    public static final float COMB_TILE_WIDTH = 136.0f;
    public static final float COMB_TILE_HEIGHT = 193.0f;
    public static final float COMB_TILE_THICK = 27.0f;

    private static final String[] FRAME_NAMES = {
        null,
        "action_pass.png",
        "action_chi.png",
        "action_peng.png",
        "action_gang.png",
        "action_hu.png",
        null, // ACTION_FLOWER has no frame in majiang_action_btn.plist.
        "action_ting.png",
    };

    private TaizhouActionBarLayout() {}

    /** Design X of the centre of 1-based {@code slot}. */
    public static float slotCenterX(int slot) {
        return SLOT_CENTER_X[slot - 1];
    }

    /** Original atlas frame for a 1-based action id, or null when none exists. */
    public static String frameName(int actionId) {
        return FRAME_NAMES[actionId];
    }

    /** {left, top, right, bottom} of a slot in Android top-down Y. */
    public static float[] slotRectAndroid(int slot) {
        float centerX = slotCenterX(slot);
        return new float[] {
            centerX - BUTTON_SIZE / 2.0f,
            TaizhouMahjongTableLayout.designY(SLOT_CENTER_Y + BUTTON_SIZE / 2.0f),
            centerX + BUTTON_SIZE / 2.0f,
            TaizhouMahjongTableLayout.designY(SLOT_CENTER_Y - BUTTON_SIZE / 2.0f),
        };
    }

    /** Bounding-box width of one candidate comb at the original 0.7 scale. */
    public static float combCellWidth() {
        // _updateVerticalLayoutAndSize: three tiles per row, widths accumulate.
        return COMB_SCALE * 3.0f * COMB_TILE_WIDTH;
    }

    /**
     * Bounding-box height of one candidate comb. A kong comb stacks its fourth
     * tile on top of tile 2 ({@code CombAlignIndexConfig[BOTTOM][0][1] = 2}),
     * adding one tile thickness ({@code UIMahComb:getRealHeight}).
     */
    public static float combCellHeight(int tilesPerComb) {
        float layers = tilesPerComb > 3 ? 1.0f : 0.0f;
        return COMB_SCALE * (COMB_TILE_HEIGHT + layers * COMB_TILE_THICK);
    }

    /** createActionCombPanel panel width for {@code count} combs. */
    public static float combsBackWidth(float cellWidth, int count) {
        int perLine = Math.min(count, COMB_MAX_PER_LINE);
        return COMB_MARGIN * 2.0f + cellWidth * perLine + COMB_GAP_X * (perLine - 1);
    }

    /** createActionCombPanel panel height for {@code count} combs. */
    public static float combsBackHeight(float cellHeight, int count) {
        int lines = (count + COMB_MAX_PER_LINE - 1) / COMB_MAX_PER_LINE;
        return COMB_MARGIN * 2.0f + cellHeight * lines + COMB_GAP_Y * (lines - 1);
    }

    /** Left edge of the comb background panel; its right edge is pinned. */
    public static float combsBackLeft(float cellWidth, int count) {
        return COMBS_BACK_RIGHT - combsBackWidth(cellWidth, count);
    }

    /** Left edge of a 1-based comb cell inside the background panel. */
    public static float combCellLeft(int index, float cellWidth, float backLeft) {
        int column = (index - 1) % COMB_MAX_PER_LINE;
        return backLeft + COMB_MARGIN + column * (cellWidth + COMB_GAP_X);
    }

    /** Bottom edge of a 1-based comb cell (Cocos Y). */
    public static float combCellBottom(int index, float cellHeight) {
        int line = (index - 1) / COMB_MAX_PER_LINE;
        return COMBS_BACK_BOTTOM + COMB_MARGIN + line * (cellHeight + COMB_GAP_Y);
    }

    /** Left edge of the cancel button. */
    public static float cancelLeft() {
        return CANCEL_RIGHT - CANCEL_SIZE;
    }
}
