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

final class JoinRoomDialog extends Dialog {
    private final JoinRoomView roomView;

    JoinRoomDialog(Context context, String areaName, JoinRoomView.Actions actions) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        roomView = new JoinRoomView(context, areaName, this::dismiss, actions);
        setContentView(
                roomView,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            applyFullscreen(window);
        }
    }

    void setInitialRoomNumber(String roomNumber) {
        roomView.setInitialRoomNumber(roomNumber);
    }

    void submitInitialRoomNumber() {
        roomView.post(roomView::submitInitialRoomNumber);
    }

    void setSubmitting(boolean submitting) {
        roomView.setSubmitting(submitting);
    }

    void setButtonClickSound(Runnable sound) {
        roomView.setButtonClickSound(sound);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            applyFullscreen(window);
            window.getDecorView().setSystemUiVisibility(MainActivityState.IMMERSIVE_UI_FLAGS);
        }
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
