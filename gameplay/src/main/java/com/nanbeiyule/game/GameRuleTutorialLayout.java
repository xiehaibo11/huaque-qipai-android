package com.nanbeiyule.game;

/** Geometry recovered from hall/CSB/ImageTextTutorial/View.csd. */
final class GameRuleTutorialLayout {
    static final float PANEL_LEFT = 239f;
    static final float PANEL_TOP = 126f;
    static final float PANEL_WIDTH = 1442f;
    static final float PANEL_HEIGHT = 828f;
    static final float NEXT_LEFT = 1320f;
    static final float NEXT_TOP = 826f;
    static final float NEXT_WIDTH = 332f;
    static final float NEXT_HEIGHT = 108f;
    static final float CLOSE_LEFT = 1602.5f;
    static final float CLOSE_TOP = 162.5f;
    static final float CLOSE_SIZE = 49f;
    static final float INDICATOR_BOTTOM_MARGIN = 60f;
    static final float INDICATOR_SPACING = 40f;

    private GameRuleTutorialLayout() {}

    static boolean nextContains(float x, float y) {
        return contains(x, y, NEXT_LEFT, NEXT_TOP, NEXT_WIDTH, NEXT_HEIGHT);
    }

    static boolean closeContains(float x, float y) {
        return contains(x, y, CLOSE_LEFT, CLOSE_TOP, CLOSE_SIZE, CLOSE_SIZE);
    }

    private static boolean contains(float x, float y, float left, float top,
            float width, float height) {
        return x >= left && x <= left + width && y >= top && y <= top + height;
    }
}
