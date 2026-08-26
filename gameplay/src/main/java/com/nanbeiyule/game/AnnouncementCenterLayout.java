package com.nanbeiyule.game;

/** Announcement geometry recovered from the shared original ActivityLayer.csd. */
final class AnnouncementCenterLayout {
    static final float DESIGN_WIDTH = 1920f;
    static final float DESIGN_HEIGHT = 1080f;
    static final float PANEL_WIDTH = 1920f;
    static final float PANEL_HEIGHT = 1080f;
    static final Box ACTIVITY_TAB = new Box(306.912f, 13.484f, 554.912f, 94.484f);
    static final Box ANNOUNCEMENT_TAB = new Box(544.912f, 13.484f, 792.912f, 94.484f);
    static final Box CLOSE = new Box(1776.64f, 128.68f, 1856.64f, 213.68f);
    static final Box AWARD_CENTER = new Box(1328.76f, 43.012f, 1514.76f, 149.012f);
    static final Box LIST = new Box(110f, 183.96f, 455f, 983.96f);
    static final Box DETAIL = new Box(488f, 183.956f, 1770f, 983.956f);
    static final Box DETAIL_BODY = new Box(513f, 258.956f, 1745f, 930f);
    static final Box RETRY = new Box(970f, 560f, 1270f, 650f);
    static final Box OPEN_PAGE = new Box(980f, 835f, 1280f, 925f);
    static final float ROW_HEIGHT = 124f;
    static final float ROW_GAP = 1f;

    private AnnouncementCenterLayout() {}

    static AdaptiveViewport.Transform panelTransform(AdaptiveViewport viewport) {
        return viewport.designTransform();
    }

    static Box rowRect(int index, float scroll) {
        float top = LIST.top() + index * (ROW_HEIGHT + ROW_GAP) - scroll;
        return new Box(107.5f, top, 457.5f, top + ROW_HEIGHT);
    }

    static int rowAt(float x, float y, float scroll, int count) {
        if (!LIST.contains(x, y)) return -1;
        for (int index = 0; index < count; index++) {
            if (rowRect(index, scroll).contains(x, y)) return index;
        }
        return -1;
    }

    static float maxListScroll(int count) {
        float contentHeight = Math.max(0f, count * (ROW_HEIGHT + ROW_GAP) - ROW_GAP);
        return Math.max(0f, contentHeight - LIST.height());
    }

    static float clampListScroll(float value, int count) {
        return Math.max(0f, Math.min(value, maxListScroll(count)));
    }

    static float maxDetailScroll(float contentHeight) {
        return Math.max(0f, contentHeight - DETAIL_BODY.height());
    }

    static float clampDetailScroll(float value, float contentHeight) {
        return Math.max(0f, Math.min(value, maxDetailScroll(contentHeight)));
    }

    static boolean panelContains(float x, float y) {
        return x >= 0f && x <= PANEL_WIDTH && y >= 0f && y <= PANEL_HEIGHT;
    }

    static boolean emptyStateIsBlank() {
        return true;
    }

    record Box(float left, float top, float right, float bottom) {
        float width() {
            return right - left;
        }

        float height() {
            return bottom - top;
        }

        float centerX() {
            return (left + right) * 0.5f;
        }

        float centerY() {
            return (top + bottom) * 0.5f;
        }

        boolean contains(float x, float y) {
            return x >= left && x <= right && y >= top && y <= bottom;
        }
    }
}
