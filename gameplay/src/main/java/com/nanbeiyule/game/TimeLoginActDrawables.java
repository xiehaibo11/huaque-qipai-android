package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;

/**
 * 原版 TimeLoginAct 图素与字体。位图由
 * {@code android/tools/extract_original_time_login_act_assets.py} 从
 * {@code TimeLogin.plist}/{@code Plist.plist} 按未裁剪 sourceSize 还原。
 */
final class TimeLoginActDrawables {
    /** 两套 BMFont 必须按原目录与原文件名加载，页图靠 {@code page file=} 同目录解析。 */
    static final String MAX_REWARD_FONT_ASSET = "time-login-act/fnt/fnt_zi-export.fnt";
    static final String WHEEL_TIPS_FONT_ASSET = "time-login-act/fnt2/fnt-export.fnt";
    /** 页面所有 Text 节点在 CSB 里都绑定 Common/Font/fangzhengcuyuan.TTF。 */
    static final String TEXT_FONT_ASSET = "fonts/fangzhengcuyuan.ttf";

    final Bitmap panel;
    final Bitmap title;
    final Bitmap freshTips;
    final Bitmap close;
    final Bitmap slotLight;
    final Bitmap slotClaimed;
    final Bitmap claimButton;
    final Bitmap coinStack;
    final Bitmap wheelPanel;
    final Bitmap wheelBottom;
    final Bitmap wheelOverlay;
    final Bitmap wheelButton;
    final Bitmap progressOff;
    final Bitmap progressOn;
    final Bitmap tipsDigit;

    private final Bitmap[] slotBackgrounds;
    private final SxvipBitmapFont maxRewardFont;
    private final SxvipBitmapFont wheelTipsFont;
    private final Typeface textFace;

    TimeLoginActDrawables(Resources resources) {
        panel = load(resources, R.drawable.time_login_act_panel);
        title = load(resources, R.drawable.time_login_act_title);
        freshTips = load(resources, R.drawable.time_login_act_fresh_tips);
        close = load(resources, R.drawable.time_login_act_close);
        slotLight = load(resources, R.drawable.time_login_act_slot_light);
        slotClaimed = load(resources, R.drawable.time_login_act_slot_claimed);
        claimButton = load(resources, R.drawable.time_login_act_claim_button);
        coinStack = load(resources, R.drawable.time_login_act_coin_stack);
        wheelPanel = load(resources, R.drawable.time_login_act_wheel_panel);
        wheelBottom = load(resources, R.drawable.time_login_act_wheel_bottom);
        wheelOverlay = load(resources, R.drawable.time_login_act_wheel_overlay);
        wheelButton = load(resources, R.drawable.time_login_act_wheel_button);
        progressOff = load(resources, R.drawable.time_login_act_progress_off);
        progressOn = load(resources, R.drawable.time_login_act_progress_on);
        tipsDigit = load(resources, R.drawable.time_login_act_tips_digit_3);
        slotBackgrounds =
                new Bitmap[] {
                    load(resources, R.drawable.time_login_act_slot_bg_1),
                    load(resources, R.drawable.time_login_act_slot_bg_2),
                    load(resources, R.drawable.time_login_act_slot_bg_3),
                };
        maxRewardFont = SxvipBitmapFont.load(resources, MAX_REWARD_FONT_ASSET);
        wheelTipsFont = SxvipBitmapFont.load(resources, WHEEL_TIPS_FONT_ASSET);
        textFace = Typeface.createFromAsset(resources.getAssets(), TEXT_FONT_ASSET);
    }

    /** 三张卡底图已经把「早间/午间/晚间」烤进美术，换图即换档位名。 */
    Bitmap slotBackground(int timeBand) {
        return slotBackgrounds[Math.max(0, Math.min(slotBackgrounds.length - 1, timeBand))];
    }

    SxvipBitmapFont maxRewardFont() {
        return maxRewardFont;
    }

    SxvipBitmapFont wheelTipsFont() {
        return wheelTipsFont;
    }

    Typeface textFace() {
        return textFace;
    }

    private static Bitmap load(Resources resources, int resourceId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return BitmapFactory.decodeResource(resources, resourceId, options);
    }
}
