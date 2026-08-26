package com.nanbeiyule.game;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

final class MembershipDailyGiftDialog extends Dialog {
    interface Actions {
        void onClaimGift(int giftId);

        void onOpenMembership();

        void onTip();

        void onGoldStatisticsSelected();
    }

    private final MembershipDailyGiftView dailyGiftView;

    MembershipDailyGiftDialog(Context context, Actions actions) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);
        FrameLayout root = new FrameLayout(context);
        dailyGiftView = new MembershipDailyGiftView(context, this::dismiss, actions);
        root.addView(
                new MembershipDailyGiftBackgroundView(context),
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(
                dailyGiftView,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(
                root,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            applyFullscreenWindowAttributes(window);
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
        applyFullscreenWindowAttributes(window);
        window.getDecorView().setSystemUiVisibility(MainActivityState.IMMERSIVE_UI_FLAGS);
    }

    private static void applyFullscreenWindowAttributes(Window window) {
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

    void setState(MembershipDailyGiftState state) {
        dailyGiftView.setState(state);
    }

    void setLoading(boolean loading) {
        dailyGiftView.setLoading(loading);
    }

    void setError(String message) {
        dailyGiftView.setError(message);
    }

    void setGoldStatisticsState(MembershipGoldStatisticsState state) {
        dailyGiftView.setGoldStatisticsState(state);
    }

    void setGoldStatisticsLoading(boolean loading) {
        dailyGiftView.setGoldStatisticsLoading(loading);
    }

    void setGoldStatisticsError(String message) {
        dailyGiftView.setGoldStatisticsError(message);
    }

    void setButtonClickSound(Runnable buttonClickSound) {
        dailyGiftView.setButtonClickSound(buttonClickSound);
    }
}
