package com.nanbeiyule.game;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/** 定时登录有礼的原生全屏承载层，与每日任务、邮件等页面同形。 */
final class TimeLoginActDialog extends Dialog {
    interface Actions extends TimeLoginActView.Actions {}

    private final TimeLoginActView contentView;

    TimeLoginActDialog(Context context, Actions actions) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        contentView = new TimeLoginActView(context, actions);
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

    void setState(TimeLoginActState state) {
        contentView.setState(state);
    }

    void setLoading(boolean loading) {
        contentView.setLoading(loading);
    }

    void setError(String message) {
        contentView.setError(message);
    }

    TimeLoginActState state() {
        return contentView.state();
    }
}
