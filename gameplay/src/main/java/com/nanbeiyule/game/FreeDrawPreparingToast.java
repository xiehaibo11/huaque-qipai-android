package com.nanbeiyule.game;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

final class FreeDrawPreparingToast {
    private static final int TOP_OFFSET_DP = 44;

    private FreeDrawPreparingToast() {}

    @SuppressWarnings("deprecation")
    static Toast show(Activity owner) {
        LinearLayout content = new LinearLayout(owner);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        content.setPadding(dp(owner, 18), dp(owner, 14), dp(owner, 18), dp(owner, 14));

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(owner, 26));
        content.setBackground(background);
        content.setElevation(dp(owner, 6));

        ImageView icon = new ImageView(owner);
        icon.setImageResource(R.mipmap.ic_launcher);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams iconParams =
                new LinearLayout.LayoutParams(dp(owner, 24), dp(owner, 24));
        iconParams.setMarginEnd(dp(owner, 12));
        content.addView(icon, iconParams);

        TextView message = new TextView(owner);
        message.setText("正在准备激励视频");
        message.setTextColor(Color.rgb(31, 31, 31));
        message.setTextSize(16);
        message.setGravity(Gravity.CENTER_VERTICAL);
        message.setIncludeFontPadding(false);
        content.addView(
                message,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

        Toast toast = new Toast(owner.getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, dp(owner, TOP_OFFSET_DP));
        toast.setView(content);
        toast.show();
        return toast;
    }

    private static int dp(Activity owner, int value) {
        return Math.round(value * owner.getResources().getDisplayMetrics().density);
    }
}
