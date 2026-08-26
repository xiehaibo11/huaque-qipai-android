package com.nanbeiyule.game;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/** Full-screen owner for the original single-button payment-cancel prompt. */
final class MembershipPaymentCancelDialog extends Dialog {
    private final MembershipPaymentCancelView cancelView;
    private final Runnable acknowledgeAction;
    private boolean acknowledged;

    MembershipPaymentCancelDialog(
            Context context, Runnable acknowledgeAction) {
        super(context);
        this.acknowledgeAction =
                acknowledgeAction == null ? () -> {} : acknowledgeAction;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        cancelView =
                new MembershipPaymentCancelView(
                        context, this::acknowledgeAndDismiss);
        setContentView(
                cancelView,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setOnCancelListener(ignored -> acknowledge());
        configureWindow();
    }

    void setButtonClickSound(Runnable buttonClickSound) {
        cancelView.setButtonClickSound(buttonClickSound);
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

    private void configureWindow() {
        Window window = getWindow();
        if (window == null) {
            return;
        }
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.addFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.dimAmount = 0.62f;
        window.setAttributes(attributes);
    }

    private void acknowledgeAndDismiss() {
        acknowledge();
        dismiss();
    }

    private void acknowledge() {
        if (!acknowledged) {
            acknowledged = true;
            acknowledgeAction.run();
        }
    }
}
