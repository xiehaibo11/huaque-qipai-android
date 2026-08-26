package com.nanbeiyule.game;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/** Full-screen owner for the restored original payment-choice layer. */
final class MembershipPaymentChoiceDialog extends Dialog {
    private final MembershipPaymentChoiceView choiceView;
    private boolean confirmConsumed;

    MembershipPaymentChoiceDialog(
            Context context,
            MembershipPurchaseSelection selection,
            Runnable confirmAction) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        choiceView =
                new MembershipPaymentChoiceView(
                        context,
                        selection,
                        new MembershipPaymentChoiceView.Actions() {
                            @Override
                            public void onConfirm() {
                                if (confirmConsumed) {
                                    return;
                                }
                                confirmConsumed = true;
                                dismiss();
                                if (confirmAction != null) {
                                    confirmAction.run();
                                }
                            }

                            @Override
                            public void onDismiss() {
                                dismiss();
                            }
                        });
        setContentView(
                choiceView,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        configureWindow();
    }

    void setButtonClickSound(Runnable buttonClickSound) {
        choiceView.setButtonClickSound(buttonClickSound);
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
}
