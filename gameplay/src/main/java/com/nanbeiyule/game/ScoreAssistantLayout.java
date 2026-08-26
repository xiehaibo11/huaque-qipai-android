package com.nanbeiyule.game;

/** Geometry recovered from ScoringAssistantView.csd after its Lua 270-degree rotation. */
final class ScoreAssistantLayout {
    static final float DESIGN_WIDTH = 1080f;
    static final float DESIGN_HEIGHT = 1920f;
    static final float LANDSCAPE_WIDTH = 1920f;
    static final float LANDSCAPE_HEIGHT = 1080f;
    static final float PANEL_WIDTH = DESIGN_WIDTH;
    static final float PANEL_HEIGHT = DESIGN_HEIGHT;

    static final Box CLOSE = new Box(936.85f, 60.97f, 1052.85f, 176.97f);
    static final Box ACTIVE_TAB = new Box(0f, 1736f, 360f, 1920f);
    static final Box HISTORY_TAB = new Box(360f, 1736f, 720f, 1920f);
    static final Box MONTHLY_TAB = new Box(720f, 1736f, 1080f, 1920f);
    static final Box CREATE = new Box(135f, 1432.2f, 945f, 1582.2f);
    static final Box CARDS = new Box(0f, 422.4f, 1080f, 1572.4f);
    static final Box RETRY = new Box(335f, 1050f, 745f, 1190f);
    static final Box PRIMARY = new Box(81.94f, 1579f, 496.94f, 1707f);
    static final Box SECONDARY = new Box(581.06f, 1579f, 996.06f, 1707f);
    static final Box TERTIARY = new Box(35f, 170f, 230f, 275f);
    static final Box PAGE_PREVIOUS = new Box(795f, 1582f, 895f, 1682f);
    static final Box PAGE_NEXT = new Box(971f, 1582f, 1071f, 1682f);
    static final float CARD_HEIGHT = 360f;

    private ScoreAssistantLayout() {}

    static AdaptiveViewport.Transform panelTransform(AdaptiveViewport viewport) {
        return viewport.dialogTransform(LANDSCAPE_WIDTH, LANDSCAPE_HEIGHT, 1f, 1f);
    }

    /** Inverse of logical portrait (x,y) -> rotated landscape (y,1080-x). */
    static float logicalX(float landscapeX, float landscapeY) {
        return DESIGN_WIDTH - landscapeY;
    }

    static float logicalY(float landscapeX, float landscapeY) {
        return landscapeX;
    }

    static ScoreAssistantState.Tab tabAt(float x, float y) {
        if (ACTIVE_TAB.contains(x, y)) return ScoreAssistantState.Tab.ACTIVE;
        if (HISTORY_TAB.contains(x, y)) return ScoreAssistantState.Tab.HISTORY;
        if (MONTHLY_TAB.contains(x, y)) return ScoreAssistantState.Tab.MONTHLY;
        return null;
    }

    static Box cardRect(int index, float scroll) {
        float top = CARDS.top() + index * CARD_HEIGHT - scroll;
        return new Box(CARDS.left(), top, CARDS.right(), top + CARD_HEIGHT);
    }

    static int cardAt(float x, float y, float scroll, int count) {
        if (!CARDS.contains(x, y)) return -1;
        for (int index = 0; index < count; index++) {
            if (cardRect(index, scroll).contains(x, y)) return index;
        }
        return -1;
    }

    static float maxScroll(int count) {
        return Math.max(0f, count * CARD_HEIGHT - CARDS.height());
    }

    static float clampScroll(float value, int count) {
        return Math.max(0f, Math.min(value, maxScroll(count)));
    }

    static boolean panelContains(float x, float y) {
        return x >= 0f && x <= DESIGN_WIDTH && y >= 0f && y <= DESIGN_HEIGHT;
    }

    record Box(float left, float top, float right, float bottom) {
        float width() { return right - left; }
        float height() { return bottom - top; }
        float centerX() { return (left + right) * 0.5f; }
        float centerY() { return (top + bottom) * 0.5f; }
        boolean contains(float x, float y) {
            return x >= left && x <= right && y >= top && y <= bottom;
        }
    }
}
