package com.nanbeiyule.game;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

final class PhoneLoginDialogChrome {
    static final int COLOR_BROWN = Color.rgb(154, 90, 56);
    static final int COLOR_MUTED_BROWN = Color.rgb(184, 126, 75);
    static final int COLOR_TURQUOISE = Color.rgb(35, 153, 148);
    static final int COLOR_ERROR = Color.rgb(177, 75, 45);

    private PhoneLoginDialogChrome() {}

    static PhoneLoginPanel panel(Activity activity) {
        return new PhoneLoginPanel(activity);
    }

    static TextView text(
            Activity activity,
            CharSequence value,
            int color,
            int gravity,
            boolean bold) {
        TextView text = new TextView(activity);
        text.setText(value);
        text.setTextColor(color);
        text.setGravity(gravity);
        text.setIncludeFontPadding(false);
        text.setTypeface(loadTypeface(activity), bold ? Typeface.BOLD : Typeface.NORMAL);
        return text;
    }

    static EditText input(Activity activity, String hint) {
        EditText input = new EditText(activity);
        input.setHint(hint);
        input.setSingleLine(true);
        input.setTextColor(COLOR_BROWN);
        input.setHintTextColor(Color.rgb(194, 151, 100));
        input.setGravity(Gravity.CENTER_VERTICAL);
        input.setIncludeFontPadding(false);
        input.setTypeface(loadTypeface(activity));
        input.setBackground(inputBackground(activity));
        return input;
    }

    static Button actionButton(Activity activity, String label) {
        Button button = new Button(activity);
        button.setText(label);
        button.setTextColor(Color.WHITE);
        button.setGravity(Gravity.CENTER);
        button.setIncludeFontPadding(false);
        button.setAllCaps(false);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setPadding(0, 0, 0, 0);
        button.setTypeface(loadTypeface(activity), Typeface.BOLD);
        button.setBackground(actionBackground(activity));
        return button;
    }

    static Button linkButton(Activity activity, String label) {
        Button button = new Button(activity);
        button.setText(label);
        button.setTextColor(COLOR_TURQUOISE);
        button.setGravity(Gravity.CENTER);
        button.setIncludeFontPadding(false);
        button.setAllCaps(false);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setPadding(0, 0, 0, 0);
        button.setTypeface(loadTypeface(activity), Typeface.BOLD);
        button.setBackgroundColor(Color.TRANSPARENT);
        return button;
    }

    static Button closeHitTarget(Activity activity, View.OnClickListener listener) {
        Button close = new Button(activity);
        close.setContentDescription(activity.getString(R.string.phone_login_close));
        close.setBackgroundColor(Color.TRANSPARENT);
        close.setMinWidth(0);
        close.setMinimumWidth(0);
        close.setMinHeight(0);
        close.setMinimumHeight(0);
        close.setPadding(0, 0, 0, 0);
        close.setOnClickListener(listener);
        return close;
    }

    static void show(Dialog dialog, Activity activity) {
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        View decorView = activity.getWindow().getDecorView();
        int availableWidth = decorView.getWidth();
        int availableHeight = decorView.getHeight();
        if (availableWidth <= 0 || availableHeight <= 0) {
            availableWidth = activity.getResources().getDisplayMetrics().widthPixels;
            availableHeight = activity.getResources().getDisplayMetrics().heightPixels;
        }
        WindowInsets rootWindowInsets =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? decorView.getRootWindowInsets()
                        : null;
        AdaptiveViewport.Insets safeInsets =
                AdaptiveCanvasView.safeInsetsFrom(rootWindowInsets);
        PhoneLoginDialogLayout.Dimensions dimensions =
                PhoneLoginDialogLayout.fit(
                        availableWidth,
                        availableHeight,
                        safeInsets);
        window.setGravity(Gravity.CENTER);
        window.setLayout(dimensions.width(), dimensions.height());
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.dimAmount = 0.58f;
        attributes.x = dimensions.xOffset();
        attributes.y = dimensions.yOffset();
        window.setAttributes(attributes);
        window.getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private static Typeface loadTypeface(Activity activity) {
        try {
            return Typeface.createFromAsset(
                    activity.getAssets(), "fonts/fangzhengcuyuan.ttf");
        } catch (RuntimeException ignored) {
            return Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        }
    }

    private static GradientDrawable inputBackground(Activity activity) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.argb(205, 255, 250, 230));
        drawable.setStroke(dp(activity, 2), Color.rgb(226, 171, 91));
        drawable.setCornerRadius(dp(activity, 14));
        return drawable;
    }

    private static GradientDrawable actionBackground(Activity activity) {
        GradientDrawable drawable =
                new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {
                            Color.rgb(92, 211, 202),
                            Color.rgb(43, 177, 170)
                        });
        drawable.setStroke(dp(activity, 2), Color.rgb(25, 135, 131));
        drawable.setCornerRadius(dp(activity, 18));
        return drawable;
    }

    private static int dp(Activity activity, int value) {
        return Math.round(
                value * activity.getResources().getDisplayMetrics().density);
    }

}
