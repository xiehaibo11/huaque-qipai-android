package com.nanbeiyule.game;

final class LobbySharePromptLayout {
    enum Target {
        NONE,
        CONFIRM,
        COPY,
        CLOSE
    }

    static final float PANEL_LEFT = 416.5f;
    static final float PANEL_TOP = 210f;
    static final float PANEL_RIGHT = 1503.5f;
    static final float PANEL_BOTTOM = 870f;
    static final float CONFIRM_LEFT = 809.5f;
    static final float CONFIRM_TOP = 694.5f;
    static final float CONFIRM_RIGHT = 1110.5f;
    static final float CONFIRM_BOTTOM = 825.5f;
    static final float COPY_LEFT = 1130f;
    static final float COPY_TOP = 704f;
    static final float COPY_RIGHT = 1430f;
    static final float COPY_BOTTOM = 816f;
    static final float CLOSE_LEFT = 1434.5f;
    static final float CLOSE_TOP = 187.9f;
    static final float CLOSE_RIGHT = 1533.5f;
    static final float CLOSE_BOTTOM = 289.9f;

    private LobbySharePromptLayout() {}

    static Target targetAt(float x, float y, boolean copyVisible) {
        if (inside(x, y, CONFIRM_LEFT, CONFIRM_TOP, CONFIRM_RIGHT, CONFIRM_BOTTOM)) {
            return Target.CONFIRM;
        }
        if (copyVisible && inside(x, y, COPY_LEFT, COPY_TOP, COPY_RIGHT, COPY_BOTTOM)) {
            return Target.COPY;
        }
        if (inside(x, y, CLOSE_LEFT, CLOSE_TOP, CLOSE_RIGHT, CLOSE_BOTTOM)) {
            return Target.CLOSE;
        }
        return Target.NONE;
    }

    private static boolean inside(
            float x, float y, float left, float top, float right, float bottom) {
        return x >= left && x <= right && y >= top && y <= bottom;
    }
}
