package com.nanbeiyule.game;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import com.nanbeiyule.game.goldroom.GoldRoomConf;

/** Native full-screen host for the recovered gold-room choose-room page. */
final class GoldChooseRoomDialog extends Dialog {
    private final GoldChooseRoomView chooseRoomView;

    GoldChooseRoomDialog(
            Context context,
            GoldChooseRoomView.OnLevelSelectedListener levelSelectedListener,
            GoldChooseRoomView.OnBackRequestedListener backRequestedListener,
            GoldChooseRoomView.OnActEntrySelectedListener actEntrySelectedListener,
            GoldChooseRoomView.OnRuleRequestedListener ruleRequestedListener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        chooseRoomView = new GoldChooseRoomView(context);
        chooseRoomView.setOnLevelSelectedListener(levelSelectedListener);
        chooseRoomView.setOnBackRequestedListener(backRequestedListener);
        chooseRoomView.setOnActEntrySelectedListener(actEntrySelectedListener);
        chooseRoomView.setOnRuleRequestedListener(ruleRequestedListener);
        setContentView(
                chooseRoomView,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            FullscreenWindowPolicy.apply(window);
        }
        setOnDismissListener(dialog -> chooseRoomView.release());
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
        FullscreenWindowPolicy.apply(window);
        window.getDecorView().setSystemUiVisibility(MainActivityState.IMMERSIVE_UI_FLAGS);
    }

    void setConf(GoldRoomConf conf) {
        chooseRoomView.setConf(conf);
    }

    /** 活动入口组构成与红点，来自服务端活动状态。 */
    void setActEntries(
            com.nanbeiyule.game.goldroom.GoldHallActEntryGroup group,
            java.util.Set<com.nanbeiyule.game.goldroom.GoldHallActEntry> redPoints) {
        chooseRoomView.setActEntries(group, redPoints);
    }

    void setWallet(GameHomeState.Wallet wallet) {
        chooseRoomView.setWallet(wallet);
    }

    void setStatusText(String statusText) {
        chooseRoomView.setStatusText(statusText);
    }
}
