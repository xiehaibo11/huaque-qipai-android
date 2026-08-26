package com.nanbeiyule.game;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

/** Full-screen owner for the original Zhejiang-style membership privileges popup. */
final class MembershipCenterDialog extends Dialog {
    private final MembershipCenterView membershipCenterView;

    MembershipCenterDialog(
            Context context,
            Runnable openDailyGiftAction,
            Runnable openMembershipPurchaseAction) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);
        FrameLayout root = new FrameLayout(context);
        root.addView(
                new MembershipPrivilegeEffectView(context),
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        membershipCenterView =
                new MembershipCenterView(
                        context, this::dismiss, openDailyGiftAction, openMembershipPurchaseAction);
        root.addView(
                membershipCenterView,
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
            window.setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT));
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
        window.getDecorView()
                .setSystemUiVisibility(MainActivityState.IMMERSIVE_UI_FLAGS);
    }

    void setButtonClickSound(Runnable buttonClickSound) {
        membershipCenterView.setButtonClickSound(buttonClickSound);
    }
}
