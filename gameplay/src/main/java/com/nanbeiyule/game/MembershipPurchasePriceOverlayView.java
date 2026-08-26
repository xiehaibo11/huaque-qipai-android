package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.View;

/** Redraws purchase prices above the original foreground button animation. */
final class MembershipPurchasePriceOverlayView extends View {
    private static final float DESIGN_WIDTH = 1920.0f;
    private static final float DESIGN_HEIGHT = 1080.0f;
    private static final RectF SCROLL_CLIP = new RectF(80.0f, 125.0f, 1840.0f, 955.0f);
    private final MembershipPurchaseView purchaseView;

    MembershipPurchasePriceOverlayView(
            Context context, MembershipPurchaseView purchaseView) {
        super(context);
        this.purchaseView = purchaseView;
        setClickable(false);
        setFocusable(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float scale = Math.min(getWidth() / DESIGN_WIDTH, getHeight() / DESIGN_HEIGHT);
        float dx = (getWidth() - DESIGN_WIDTH * scale) * 0.5f;
        float dy = (getHeight() - DESIGN_HEIGHT * scale) * 0.5f;
        canvas.save();
        canvas.translate(dx, dy);
        canvas.scale(scale, scale);
        canvas.clipRect(SCROLL_CLIP);
        purchaseView.drawPrices(canvas);
        canvas.restore();
    }
}
