package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.View;

/** Redraws time-membership prices above the shop foreground button animation. */
final class ShopMembershipPriceOverlayView extends View {
    private static final RectF MEMBERSHIP_LIST_CLIP =
            new RectF(
                    ShopMembershipEffectView.SHOP_LIST_CLIP_RECT.left(),
                    ShopMembershipEffectView.SHOP_LIST_CLIP_RECT.top(),
                    ShopMembershipEffectView.SHOP_LIST_CLIP_RECT.right(),
                    ShopMembershipEffectView.SHOP_LIST_CLIP_RECT.bottom());
    private final ShopView shopView;

    ShopMembershipPriceOverlayView(Context context, ShopView shopView) {
        super(context);
        this.shopView = shopView;
        setClickable(false);
        setFocusable(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        ShopLayout.Transform transform = ShopLayout.contain(getWidth(), getHeight());
        canvas.save();
        canvas.translate(transform.offsetX(), transform.offsetY());
        canvas.scale(transform.scale(), transform.scale());
        canvas.clipRect(MEMBERSHIP_LIST_CLIP);
        shopView.drawMembershipPrices(canvas);
        canvas.restore();
    }
}
