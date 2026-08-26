package com.nanbeiyule.game.goldroom;

/** Geometry recovered from {@code Common/CSB/GameBase/MatchUI.csb}. */
public final class GoldMatchLayout {
    public static final float DESIGN_WIDTH = 1920.0f;
    public static final float DESIGN_HEIGHT = 1080.0f;

    public static final float MATCH_ANI_CENTER_X = 960.0f;
    public static final float MATCH_ANI_CENTER_Y = 540.0f;

    /** Cocos bottom-up Y coordinate of {@code KW_PANEL_CONTENT}. */
    public static final float CONTENT_CENTER_X = 960.0f;
    public static final float CONTENT_CENTER_Y = 150.0f;

    public static final float HEAD_FRAME_WIDTH = 105.0f;
    public static final float HEAD_FRAME_HEIGHT = 106.0f;

    public static final float START_BUTTON_WIDTH = 387.0f;
    public static final float START_BUTTON_HEIGHT = 132.0f;
    public static final boolean START_BUTTON_VISIBLE = false;

    private GoldMatchLayout() {}

    public static float androidY(float cocosY) {
        return DESIGN_HEIGHT - cocosY;
    }
}
