package com.nanbeiyule.game;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/** Original MessageBox.csb single-button prompt used by TeaHouse.TipTool. */
final class OriginalMessageBoxDialog extends Dialog {
    private final OriginalMessageBoxView messageView;
    private final Runnable dismissed;
    private boolean acknowledged;

    OriginalMessageBoxDialog(Context context, String message, Runnable dismissed) {
        super(context);
        this.dismissed = dismissed == null ? () -> {} : dismissed;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        messageView = new OriginalMessageBoxView(context, message, this::acknowledgeAndDismiss);
        setContentView(
                messageView,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setOnCancelListener(ignored -> acknowledge());
        configureWindow();
    }

    void setButtonClickSound(Runnable sound) { messageView.setButtonClickSound(sound); }

    @Override
    protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.getDecorView().setSystemUiVisibility(MainActivityState.IMMERSIVE_UI_FLAGS);
        }
    }

    private void configureWindow() {
        Window window = getWindow();
        if (window == null) return;
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
            dismissed.run();
        }
    }
}
