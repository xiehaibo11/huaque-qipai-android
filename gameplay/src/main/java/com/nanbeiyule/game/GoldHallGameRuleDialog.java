package com.nanbeiyule.game;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import com.nanbeiyule.game.goldroom.GoldHallGameRuleDocument;
import java.util.List;

/**
 * 规则弹层的全屏宿主，对应原版 {@code ViewsConfig.GoldHallGameRuleView}
 * （{@code ZOrder = XH.ZORDER.DIALOG}）。不使用浏览器或 WebView。
 */
final class GoldHallGameRuleDialog extends Dialog {
    private final GoldHallGameRuleView ruleView;

    GoldHallGameRuleDialog(
            Context context, GoldHallGameRuleView.OnGameSelectedListener gameSelectedListener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        ruleView = new GoldHallGameRuleView(context);
        ruleView.setOnCloseRequestedListener(this::dismiss);
        ruleView.setOnGameSelectedListener(gameSelectedListener);
        setContentView(
                ruleView,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        setOnDismissListener(dialog -> ruleView.release());
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

    void setDocuments(List<GoldHallGameRuleDocument> documents, long selectedGameId) {
        ruleView.setDocuments(documents, selectedGameId);
    }

    void setLoading(boolean loading) {
        ruleView.setLoading(loading);
    }
}
