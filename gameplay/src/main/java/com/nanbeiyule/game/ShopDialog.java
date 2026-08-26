package com.nanbeiyule.game;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

final class ShopDialog extends Dialog {
    interface Actions extends ShopView.Actions {}

    private final ShopView shopView;
    private final ShopProductEffectView productEffectView;
    private final ShopMembershipPriceOverlayView priceOverlayView;

    ShopDialog(Context context, Actions actions) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        shopView = new ShopView(context, this::dismiss, actions);
        productEffectView = new ShopProductEffectView(context, System.nanoTime());
        productEffectView.setVisibility(View.GONE);
        priceOverlayView = new ShopMembershipPriceOverlayView(context, shopView);
        priceOverlayView.setVisibility(View.GONE);
        FrameLayout root = new FrameLayout(context);
        root.addView(shopView, fullScreenLayoutParams());
        root.addView(productEffectView, fullScreenLayoutParams());
        root.addView(priceOverlayView, fullScreenLayoutParams());
        setContentView(
                root,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        shopView.setMembershipPageListener(this::onMembershipPageChanged);
        shopView.setProductEffectPageListener(this::onProductEffectPageChanged);
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            applyFullscreen(window);
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
        applyFullscreen(window);
        window.getDecorView().setSystemUiVisibility(MainActivityState.IMMERSIVE_UI_FLAGS);
    }

    void setCatalog(ShopCatalogState catalog) {
        shopView.setCatalog(catalog);
    }

    void setWallet(ShopWalletState wallet) {
        shopView.setWallet(wallet);
    }

    void setLoading(boolean loading) {
        shopView.setLoading(loading);
    }

    void setError(String error) {
        shopView.setError(error);
    }

    void setButtonClickSound(Runnable sound) {
        shopView.setButtonClickSound(sound);
    }

    private void onMembershipPageChanged(boolean visible, float scrollOffset) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        if (priceOverlayView.getVisibility() != visibility) {
            priceOverlayView.setVisibility(visibility);
        }
        priceOverlayView.invalidate();
    }

    private void onProductEffectPageChanged(
            ShopCategory category, float verticalScrollOffset) {
        boolean visible = ShopProductEffectView.supportsCategory(category);
        int visibility = visible ? View.VISIBLE : View.GONE;
        if (productEffectView.getVisibility() != visibility) {
            productEffectView.setVisibility(visibility);
        }
        productEffectView.setVerticalScrollOffset(verticalScrollOffset);
    }

    private static FrameLayout.LayoutParams fullScreenLayoutParams() {
        return new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private static void applyFullscreen(Window window) {
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.height = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.gravity = Gravity.TOP | Gravity.START;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
            attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        window.setAttributes(attributes);
    }
}
