package com.nanbeiyule.game;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.TextView;

/** Explicit confirmation for irreversible ledger end and delete commands. */
final class ScoreAssistantConfirmDialog {
    private ScoreAssistantConfirmDialog() {}

    static void show(Activity activity, String title, String message, Runnable confirmed) {
        Typeface font = Typeface.createFromAsset(
                activity.getAssets(), "fonts/zihun_jingdian_lihei.ttf");
        TextView content = new TextView(activity);
        int padding = Math.round(28 * activity.getResources().getDisplayMetrics().density);
        content.setPadding(padding, padding, padding, padding);
        content.setText(message);
        content.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f);
        content.setTextColor(Color.rgb(91, 63, 39));
        content.setTypeface(font);
        ScoreAssistantPortraitDialog.show(activity, title, content, "确认", 0.42f, () -> {
            confirmed.run();
            return true;
        });
    }
}
