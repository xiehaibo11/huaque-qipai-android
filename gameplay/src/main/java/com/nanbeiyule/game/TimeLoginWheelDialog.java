package com.nanbeiyule.game;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/** 八格转盘的原生全屏承载层。 */
final class TimeLoginWheelDialog extends Dialog {
    interface Actions extends TimeLoginWheelView.Actions {}

    private final TimeLoginWheelView contentView;

    TimeLoginWheelDialog(Context context, Actions actions) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        contentView = new TimeLoginWheelView(context, actions);
        setContentView(
                contentView,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
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
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.getDecorView().setSystemUiVisibility(MainActivityState.IMMERSIVE_UI_FLAGS);
    }

    void setWheel(TimeLoginActState.Wheel wheel) {
        contentView.setWheel(wheel);
    }

    void startRoll(int sliceIndex) {
        contentView.startRoll(sliceIndex);
    }

    boolean rolling() {
        return contentView.rolling();
    }
}
