package com.nanbeiyule.game;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

/** Full-screen owner for the original Zhejiang SxvipShopLayer.csb purchase popup. */
final class MembershipPurchaseDialog extends Dialog {
    private final MembershipPurchaseEffectView backgroundEffectView;
    private final MembershipPurchaseView purchaseView;
    private final MembershipPurchaseEffectView foregroundEffectView;
    private final MembershipPurchasePriceOverlayView priceOverlayView;

    MembershipPurchaseDialog(Context context, MembershipPurchaseView.Actions actions) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);
        long animationStartedNanos = System.nanoTime();
        backgroundEffectView =
                new MembershipPurchaseEffectView(
                        context,
                        MembershipPurchaseEffectView.Layer.BACKGROUND,
                        animationStartedNanos);
        foregroundEffectView =
                new MembershipPurchaseEffectView(
                        context,
                        MembershipPurchaseEffectView.Layer.FOREGROUND,
                        animationStartedNanos);
        purchaseView =
                new MembershipPurchaseView(
                        context,
                        this::dismiss,
                        actions,
                        this::onPurchaseScrollOffsetChanged);
        priceOverlayView = new MembershipPurchasePriceOverlayView(context, purchaseView);
        FrameLayout root = new FrameLayout(context);
        root.addView(backgroundEffectView, fullScreenLayoutParams());
        root.addView(
                purchaseView,
                fullScreenLayoutParams());
        root.addView(foregroundEffectView, fullScreenLayoutParams());
        root.addView(priceOverlayView, fullScreenLayoutParams());
        setContentView(
                root,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.dimAmount = 0.62f;
            window.setAttributes(attributes);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window == null) {
            return;
        }
        window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        window.getDecorView().setSystemUiVisibility(MainActivityState.IMMERSIVE_UI_FLAGS);
    }

    void setButtonClickSound(Runnable buttonClickSound) {
        purchaseView.setButtonClickSound(buttonClickSound);
    }

    void setProducts(MembershipProductsState products) {
        purchaseView.setProducts(products);
    }

    private void onPurchaseScrollOffsetChanged(float scrollOffset) {
        backgroundEffectView.setScrollOffset(scrollOffset);
        foregroundEffectView.setScrollOffset(scrollOffset);
        priceOverlayView.invalidate();
    }

    private static FrameLayout.LayoutParams fullScreenLayoutParams() {
        return new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
    }
}
