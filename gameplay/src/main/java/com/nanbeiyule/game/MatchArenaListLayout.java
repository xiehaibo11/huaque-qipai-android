package com.nanbeiyule.game;

/** Exact 1920x1080 geometry recovered from TeaHouseListView.csb. */
final class MatchArenaListLayout {
    static final float DESIGN_WIDTH = 1920f;
    static final float DESIGN_HEIGHT = 1080f;
    static final float ITEM_START_X = 174.912f;
    static final float ITEM_STRIDE = 516f;
    static final float ITEM_WIDTH = 516f;
    static final float ITEM_HEIGHT = 1000f;
    static final float CREATE_CENTER_X = 1797f;
    static final float CREATE_CENTER_Y = 130f;
    static final float CREATE_WIDTH = 272f;
    static final float CREATE_HEIGHT = 259f;
    static final float BACK_CENTER_X = 83.5f;
    static final float BACK_CENTER_Y = 1007f;
    static final float BACK_WIDTH = 154f;
    static final float BACK_HEIGHT = 118f;

    private MatchArenaListLayout() {}

    static float innerWidth(int itemCount) {
        return Math.max(DESIGN_WIDTH, ITEM_START_X + Math.max(0, itemCount) * ITEM_STRIDE);
    }
}
