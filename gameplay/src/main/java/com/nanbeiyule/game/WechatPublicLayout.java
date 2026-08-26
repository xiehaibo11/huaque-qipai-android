package com.nanbeiyule.game;

import android.graphics.RectF;

/** Geometry recovered from WxPublicLayer.csd and fitted inside device safe insets. */
final class WechatPublicLayout {
    static final float DESIGN_WIDTH = 1920f;
    static final float DESIGN_HEIGHT = 1080f;
    static final float PANEL_WIDTH = 960f;
    static final float PANEL_HEIGHT = 560f;
    static final RectF CLOSE = new RectF(862f, 2f, 951f, 84f);
    static final RectF NOTICE = new RectF(60f, 119f, 900f, 215f);
    static final RectF QR = new RectF(149f, 236f, 329f, 416f);
    static final RectF COPY = new RectF(130f, 222f, 830f, 421f);
    static final RectF OPEN = new RectF(374f, 438f, 586f, 530f);

    private WechatPublicLayout() {}

    static AdaptiveViewport.Transform panelTransform(AdaptiveViewport viewport) {
        return viewport.dialogTransform(PANEL_WIDTH, PANEL_HEIGHT, 0.96f, 0.9f);
    }

    static boolean panelContains(float x, float y) {
        return x >= 0f && x <= PANEL_WIDTH && y >= 0f && y <= PANEL_HEIGHT;
    }
}
