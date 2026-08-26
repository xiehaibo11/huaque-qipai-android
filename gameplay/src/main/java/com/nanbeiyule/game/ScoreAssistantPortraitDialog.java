package com.nanbeiyule.game;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Full-screen host that keeps native forms in the original portrait 270-degree coordinate space. */
final class ScoreAssistantPortraitDialog {
    interface PositiveAction { boolean run(); }

    private static final int PAPER = Color.rgb(251, 244, 225);
    private static final int BROWN = Color.rgb(91, 63, 39);

    private ScoreAssistantPortraitDialog() {}

    static void show(Activity activity, String title, View content, String positiveText,
            float heightFraction, PositiveAction positiveAction) {
        Typeface font = Typeface.createFromAsset(
                activity.getAssets(), "fonts/zihun_jingdian_lihei.ttf");
        FrameLayout portrait = new FrameLayout(activity);
        portrait.setBackgroundColor(Color.TRANSPARENT);
        portrait.setClickable(true);
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(activity, 24), dp(activity, 20), dp(activity, 24), dp(activity, 16));
        card.setBackground(cardBackground(activity));

        TextView heading = new TextView(activity);
        heading.setText(title);
        heading.setTextColor(BROWN);
        heading.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22f);
        heading.setTypeface(font);
        heading.setGravity(Gravity.CENTER);
        card.addView(heading, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 54)));
        card.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout buttons = new LinearLayout(activity);
        buttons.setGravity(Gravity.CENTER);
        Button cancel = button(activity, "取消", font, BROWN);
        Button positive = button(activity, positiveText, font, Color.rgb(234, 92, 64));
        buttons.addView(cancel, new LinearLayout.LayoutParams(0, dp(activity, 56), 1f));
        buttons.addView(positive, new LinearLayout.LayoutParams(0, dp(activity, 56), 1f));
        card.addView(buttons);

        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        int logicalWidth = Math.min(metrics.widthPixels, metrics.heightPixels);
        int logicalHeight = Math.max(metrics.widthPixels, metrics.heightPixels);
        int cardWidth = Math.round(logicalWidth * 0.88f);
        int cardHeight = Math.round(logicalHeight * heightFraction);
        FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(cardWidth, cardHeight,
                Gravity.CENTER);
        portrait.addView(card, cardParams);

        RotatedHost host = new RotatedHost(activity, portrait);
        Dialog dialog = new TaizhouFullscreenDialog(activity, host, true);
        cancel.setOnClickListener(ignored -> dialog.dismiss());
        positive.setOnClickListener(ignored -> {
            if (positiveAction.run()) dialog.dismiss();
        });
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    private static Button button(Activity activity, String value, Typeface font, int color) {
        Button button = new Button(activity);
        button.setText(value);
        button.setTextColor(color);
        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f);
        button.setTypeface(font);
        return button;
    }

    private static GradientDrawable cardBackground(Activity activity) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(PAPER);
        background.setCornerRadius(dp(activity, 18));
        background.setStroke(dp(activity, 2), Color.rgb(220, 188, 132));
        return background;
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }

    private static final class RotatedHost extends ViewGroup {
        private final View portrait;

        RotatedHost(Activity activity, View portrait) {
            super(activity);
            this.portrait = portrait;
            addView(portrait);
        }

        @Override protected void onMeasure(int widthSpec, int heightSpec) {
            int width = MeasureSpec.getSize(widthSpec);
            int height = MeasureSpec.getSize(heightSpec);
            setMeasuredDimension(width, height);
            portrait.measure(MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY));
        }

        @Override protected void onLayout(boolean changed, int left, int top, int right,
                int bottom) {
            int logicalWidth = bottom - top;
            int logicalHeight = right - left;
            portrait.layout(0, 0, logicalWidth, logicalHeight);
            portrait.setPivotX(0f);
            portrait.setPivotY(0f);
            portrait.setRotation(ScoreAssistantDialogOrientation.rotationDegrees(
                    ScoreAssistantDialogOrientation.Surface.CREATE));
            portrait.setTranslationX(0f);
            portrait.setTranslationY(logicalWidth);
        }
    }
}
