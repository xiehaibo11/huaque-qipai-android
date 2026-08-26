package com.nanbeiyule.game;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

final class FreeDrawRewardDialog extends Dialog {
    FreeDrawRewardDialog(Activity activity, FreeDrawResult result) {
        super(activity, android.R.style.Theme_Translucent_NoTitleBar);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        setContentView(content(activity, result));
        Window window = getWindow();
        if (window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams params = window.getAttributes();
            params.dimAmount = 0.68f;
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window == null) return;
        int maxWidth = dp(520);
        int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        window.setLayout(Math.min(maxWidth, (int) (screenWidth * 0.64f)), ViewGroup.LayoutParams.WRAP_CONTENT);
        window.getDecorView().setSystemUiVisibility(MainActivityState.IMMERSIVE_UI_FLAGS);
    }

    private LinearLayout content(Activity activity, FreeDrawResult result) {
        Typeface font =
                Typeface.createFromAsset(activity.getAssets(), "fonts/zihun_jingdian_lihei.ttf");
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(34), dp(24), dp(34), dp(26));
        root.setBackground(panelBackground());

        TextView title = text(activity, "恭喜获得", 30, Color.rgb(139, 59, 18), font);
        root.addView(title, wrap());

        ImageView icon = new ImageView(activity);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        icon.setImageResource(
                "DIAMOND".equals(result.reward().type())
                        ? R.drawable.shop_product_diamond
                        : R.drawable.shop_product_coin_bag);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(128), dp(96));
        iconParams.topMargin = dp(4);
        root.addView(icon, iconParams);

        TextView reward =
                text(activity, result.reward().displayName(), 27, Color.rgb(190, 72, 18), font);
        root.addView(reward, wrap());

        TextView remaining =
                text(
                        activity,
                        "今日还可免费抽奖 " + result.remainingDraws() + " 次",
                        16,
                        Color.rgb(132, 91, 49),
                        font);
        LinearLayout.LayoutParams remainingParams = wrap();
        remainingParams.topMargin = dp(8);
        root.addView(remaining, remainingParams);

        Button confirm = new Button(activity);
        confirm.setAllCaps(false);
        confirm.setText("确定");
        confirm.setTextColor(Color.rgb(126, 43, 9));
        confirm.setTextSize(20);
        confirm.setTypeface(font);
        confirm.setBackground(buttonBackground());
        confirm.setOnClickListener(ignored -> dismiss());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(dp(190), dp(52));
        buttonParams.topMargin = dp(20);
        root.addView(confirm, buttonParams);
        return root;
    }

    private static TextView text(
            Activity activity, String value, int size, int color, Typeface font) {
        TextView view = new TextView(activity);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(font);
        view.setGravity(Gravity.CENTER);
        return view;
    }

    private static LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private static GradientDrawable panelBackground() {
        GradientDrawable background =
                new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {Color.rgb(255, 250, 220), Color.rgb(250, 218, 146)});
        background.setCornerRadius(28f);
        background.setStroke(5, Color.rgb(174, 105, 42));
        return background;
    }

    private static GradientDrawable buttonBackground() {
        GradientDrawable background =
                new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {Color.rgb(255, 234, 83), Color.rgb(244, 176, 41)});
        background.setCornerRadius(18f);
        background.setStroke(2, Color.rgb(226, 139, 31));
        return background;
    }

    private int dp(int value) {
        return Math.round(value * getContext().getResources().getDisplayMetrics().density);
    }
}
