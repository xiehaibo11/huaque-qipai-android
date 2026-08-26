package com.nanbeiyule.game;

import android.graphics.RectF;

/** Geometry recovered from HealthTipLayer.csd, fitted inside the real safe area. */
final class HealthNoticeLayout {
    static final float DESIGN_WIDTH = 1920f;
    static final float DESIGN_HEIGHT = 1080f;
    static final float PANEL_WIDTH = 1087f;
    static final float PANEL_HEIGHT = 660f;
    static final float CONTENT_VIEWPORT_HEIGHT = 520f;
    static final RectF CONTENT = new RectF(18f, 86f, 1069f, 606f);
    static final RectF CLOSE = new RectF(988f, 2f, 1077f, 84f);

    private HealthNoticeLayout() {}

    static AdaptiveViewport.Transform panelTransform(AdaptiveViewport viewport) {
        return viewport.dialogTransform(PANEL_WIDTH, PANEL_HEIGHT, 0.96f, 0.92f);
    }

    static boolean panelContains(float x, float y) {
        return x >= 0f && x <= PANEL_WIDTH && y >= 0f && y <= PANEL_HEIGHT;
    }

    static boolean contentContains(float x, float y) {
        return CONTENT.contains(x, y);
    }

    static boolean closeContains(float x, float y) {
        return CLOSE.contains(x, y);
    }
}
