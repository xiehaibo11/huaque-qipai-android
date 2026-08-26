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

/** Full-screen host for the adaptive Zhejiang activity center. */
final class LobbyActivityCenterDialog extends Dialog {
    interface Actions {
        void onActivityRequested(LobbyActivityCenterModel.Destination destination);

        default void onGoldMembershipClaim(String productCode) {}

        default void onAnnouncementRequested() {}

        default void onAwardCenterRequested() {}
    }

    private final LobbyActivityCenterView activityView;

    LobbyActivityCenterDialog(Context context, Actions actions) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        activityView =
                new LobbyActivityCenterView(
                        context,
                        new LobbyActivityCenterView.Actions() {
                            @Override
                            public void onActivityRequested(
                                    LobbyActivityCenterModel.Destination destination) {
                                actions.onActivityRequested(destination);
                            }

                            @Override
                            public void onDismissRequested() {
                                dismiss();
                            }

                            @Override
                            public void onAnnouncementRequested() {
                                dismiss();
                                actions.onAnnouncementRequested();
                            }

                            @Override
                            public void onAwardCenterRequested() {
                                actions.onAwardCenterRequested();
                            }

                            @Override
                            public void onGoldMembershipClaim(String productCode) {
                                actions.onGoldMembershipClaim(productCode);
                            }
                        });
        setContentView(
                activityView,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            applyFullscreen(window);
        }
    }

    void setButtonClickSound(Runnable sound) {
        activityView.setButtonClickSound(sound);
    }

    void setFreeDrawState(FreeDrawState state) {
        activityView.setFreeDrawState(state);
    }

    void setGoldMembershipCardsState(GoldMembershipCardsState state) {
        activityView.setGoldMembershipCardsState(state);
    }

    void setGoldMembershipCardsLoading(boolean loading) {
        activityView.setGoldMembershipCardsLoading(loading);
    }

    void setGoldMembershipCardsError(String message) {
        activityView.setGoldMembershipCardsError(message);
    }

    void updateGoldMembershipCard(GoldMembershipCardsState.Card card) {
        activityView.updateGoldMembershipCard(card);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window == null) return;
        window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        applyFullscreen(window);
        window.getDecorView().setSystemUiVisibility(MainActivityState.IMMERSIVE_UI_FLAGS);
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
