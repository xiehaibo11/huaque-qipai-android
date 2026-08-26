package com.nanbeiyule.game;

/** Geometry recovered from cocosStudio/hall/CSB/Activity/ActivityLayer.csd. */
final class LobbyActivityCenterLayout {
    enum Section {
        ACTIVITY,
        ANNOUNCEMENT,
        NONE
    }

    static final float DESIGN_WIDTH = 1920f;
    static final float DESIGN_HEIGHT = 1080f;
    static final AdaptiveViewport.Rect BACKGROUND = rect(0f, 0f, 1920f, 1080f);
    static final AdaptiveViewport.Rect TITLE_LEFT_OUTER =
            rect(220.9123f, 6.984f, 314.9123f, 96.984f);
    static final AdaptiveViewport.Rect TITLE_LEFT_INNER =
            rect(305.9123f, 48.984f, 346.9123f, 96.984f);
    static final AdaptiveViewport.Rect TITLE_RIGHT_OUTER =
            rect(774.912f, 6.984f, 868.912f, 96.984f);
    static final AdaptiveViewport.Rect TITLE_RIGHT_INNER =
            rect(742.912f, 48.984f, 783.912f, 96.984f);
    static final AdaptiveViewport.Rect ACTIVITY_TAB = rect(306.912f, 13.484f, 554.912f, 94.484f);
    static final AdaptiveViewport.Rect ANNOUNCEMENT_TAB = rect(544.912f, 13.484f, 792.912f, 94.484f);
    static final AdaptiveViewport.Rect ACTIVITY_LIST = rect(110f, 183.96f, 455f, 983.96f);
    static final AdaptiveViewport.Rect ACTIVITY_CONTENT = rect(488f, 183.956f, 1770f, 983.956f);
    static final AdaptiveViewport.Rect FREE_DRAW_CONTENT = rect(488f, 232f, 1770f, 970f);
    static final AdaptiveViewport.Rect FREE_DRAW_BUTTON = rect(950f, 860f, 1305f, 980f);
    static final AdaptiveViewport.Rect LOGIN_GIFT_BADGE = rect(410f, 469f, 438f, 497f);
    static final AdaptiveViewport.Rect AWARD_CENTER = rect(1328.76f, 43.012f, 1514.76f, 149.012f);
    static final AdaptiveViewport.Rect CLOSE = rect(1776.64f, 128.68f, 1856.64f, 213.68f);
    static final AdaptiveViewport.Rect DISCLAIMER = rect(953.76f, 979.22f, 1320.76f, 1025.22f);
    static final float ROW_HEIGHT = 124f;
    static final float ROW_GAP = 1f;

    private LobbyActivityCenterLayout() {}

    static AdaptiveViewport.Rect row(int index, float scroll) {
        if (index < 0) throw new IllegalArgumentException("row index must be non-negative");
        float top = ACTIVITY_LIST.top() + index * (ROW_HEIGHT + ROW_GAP) - scroll;
        return rect(107.5f, top, 457.5f, top + ROW_HEIGHT);
    }

    static int rowAt(float x, float y, float scroll, int count) {
        if (!contains(ACTIVITY_LIST, x, y)) return -1;
        for (int index = 0; index < count; index++) {
            if (contains(row(index, scroll), x, y)) return index;
        }
        return -1;
    }

    static float maxScroll(int count) {
        float content = Math.max(0f, count * (ROW_HEIGHT + ROW_GAP) - ROW_GAP);
        return Math.max(0f, content - ACTIVITY_LIST.height());
    }

    static float clampScroll(float value, int count) {
        return Math.max(0f, Math.min(value, maxScroll(count)));
    }

    static Section sectionAt(float x, float y) {
        // Notify is the later CSD sibling, so it owns the original 10 px overlap.
        if (contains(ANNOUNCEMENT_TAB, x, y)) return Section.ANNOUNCEMENT;
        if (contains(ACTIVITY_TAB, x, y)) return Section.ACTIVITY;
        return Section.NONE;
    }

    static boolean contentContains(float x, float y) {
        return contains(ACTIVITY_CONTENT, x, y);
    }

    static boolean freeDrawButtonContains(float x, float y) {
        return contains(FREE_DRAW_BUTTON, x, y);
    }

    static boolean awardCenterContains(float x, float y) {
        return contains(AWARD_CENTER, x, y);
    }

    static boolean closeContains(float x, float y) {
        return contains(CLOSE, x, y);
    }

    private static boolean contains(AdaptiveViewport.Rect rect, float x, float y) {
        return x >= rect.left() && x <= rect.right() && y >= rect.top() && y <= rect.bottom();
    }

    private static AdaptiveViewport.Rect rect(float left, float top, float right, float bottom) {
        return new AdaptiveViewport.Rect(left, top, right, bottom);
    }
}
