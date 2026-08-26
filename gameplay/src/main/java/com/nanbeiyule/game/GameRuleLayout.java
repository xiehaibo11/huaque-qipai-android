package com.nanbeiyule.game;

/** Geometry from hall/CSB/GameRuleLayer.csd, in 1920x1080 design pixels. */
final class GameRuleLayout {
    static final float DESIGN_WIDTH = 1920f;
    static final float DESIGN_HEIGHT = 1080f;
    static final float HEADER_HEIGHT = 121f;
    static final float LIST_LEFT = 1.9814f;
    static final float LIST_TOP = 142f;
    static final float LIST_WIDTH = 386f;
    static final float LIST_HEIGHT = 920f;
    static final float ITEM_WIDTH = 360f;
    static final float ITEM_HEIGHT = 136f;
    static final float ITEM_LEFT = LIST_LEFT + (LIST_WIDTH - ITEM_WIDTH) * 0.5f;
    static final float CONTENT_LEFT = 407f;
    static final float CONTENT_TOP = 130f;
    static final float CONTENT_WIDTH = 1500f;
    static final float CONTENT_HEIGHT = 950f;
    static final float SELECTED_TEXT_SIZE = 60f;
    static final float UNSELECTED_TEXT_SIZE = 54f;
    static final int SELECTED_TEXT_COLOR = 0xFFFFFBCD;
    static final int UNSELECTED_TEXT_COLOR = 0xFFA36F48;
    static final float CLOSE_LEFT = 43f;
    static final float CLOSE_TOP = 2f;
    static final float CLOSE_RIGHT = 231f;
    static final float CLOSE_BOTTOM = 104f;
    static final long IMAGE_TUTORIAL_GAME_ID = 30579L;
    static final float IMAGE_TUTORIAL_LEFT = 1514f;
    static final float IMAGE_TUTORIAL_TOP = 0f;
    static final float IMAGE_TUTORIAL_RIGHT = 1846f;
    static final float IMAGE_TUTORIAL_BOTTOM = 105f;

    private GameRuleLayout() {}

    static float itemTop(int index, float scroll) {
        return LIST_TOP + index * ITEM_HEIGHT - scroll;
    }

    static int itemAt(float x, float y, float scroll, int count) {
        if (x < LIST_LEFT || x > LIST_LEFT + LIST_WIDTH || y < LIST_TOP
                || y > LIST_TOP + LIST_HEIGHT) return -1;
        int index = (int) ((y - LIST_TOP + scroll) / ITEM_HEIGHT);
        if (index < 0 || index >= count) return -1;
        float top = itemTop(index, scroll);
        return x >= ITEM_LEFT && x <= ITEM_LEFT + ITEM_WIDTH && y >= top
                && y <= top + ITEM_HEIGHT ? index : -1;
    }

    static boolean closeContains(float x, float y) {
        return x >= CLOSE_LEFT && x <= CLOSE_RIGHT && y >= CLOSE_TOP && y <= CLOSE_BOTTOM;
    }

    static boolean imageTutorialContains(float x, float y) {
        return x >= IMAGE_TUTORIAL_LEFT && x <= IMAGE_TUTORIAL_RIGHT
                && y >= IMAGE_TUTORIAL_TOP && y <= IMAGE_TUTORIAL_BOTTOM;
    }

    static float maxListScroll(int count) {
        return Math.max(0f, count * ITEM_HEIGHT - LIST_HEIGHT);
    }

    static float clampListScroll(float value, int count) {
        return Math.max(0f, Math.min(value, maxListScroll(count)));
    }
}
