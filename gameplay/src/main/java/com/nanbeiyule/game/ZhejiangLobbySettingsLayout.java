package com.nanbeiyule.game;

/** Recovered SettingLayer.csd geometry shared by drawing and touch handling. */
final class ZhejiangLobbySettingsLayout {
    static final float DESIGN_WIDTH = 1920f;
    static final float DESIGN_HEIGHT = 1080f;
    static final float PANEL_LEFT = 416.5f;
    static final float PANEL_TOP = 210f;
    static final float PANEL_WIDTH = 1087f;
    static final float PANEL_HEIGHT = 660f;
    static final float SLIDER_LEFT = 309.5f;
    static final float SLIDER_WIDTH = 621f;

    private ZhejiangLobbySettingsLayout() {}

    static int percentForSliderX(float panelX) {
        float progress = (panelX - SLIDER_LEFT) / SLIDER_WIDTH;
        return Math.round(Math.max(0f, Math.min(1f, progress)) * 100f);
    }
}
