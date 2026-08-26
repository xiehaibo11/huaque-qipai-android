package com.nanbeiyule.game;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/** Public entry point for the native Zhejiang Online news page. */
public final class ZhejiangNewsDialog extends Dialog {
    private final ZhejiangNewsPage page;
    private boolean released;

    public ZhejiangNewsDialog(Context context) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        page = new ZhejiangNewsPage(context, this::dismiss);
        setContentView(
                page,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            FullscreenWindowPolicy.apply(window);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window != null) {
            window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            FullscreenWindowPolicy.apply(window);
            window.getDecorView().setSystemUiVisibility(MainActivityState.IMMERSIVE_UI_FLAGS);
        }
        page.requestApplyInsets();
    }

    @Override
    public void onBackPressed() {
        if (!page.navigateBack()) {
            dismiss();
        }
    }

    @Override
    public void dismiss() {
        if (!released) {
            released = true;
            page.release();
        }
        super.dismiss();
    }
}
