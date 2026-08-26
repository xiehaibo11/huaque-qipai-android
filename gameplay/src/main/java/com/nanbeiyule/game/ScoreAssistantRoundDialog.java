package com.nanbeiyule.game;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/** Manual signed-score form backed by the shared full-roster/zero-sum validator. */
final class ScoreAssistantRoundDialog {
    interface Submit {
        void submit(List<ScoreAssistantInputValidator.ScoreDelta> scores);
    }

    private static final int BROWN = Color.rgb(91, 63, 39);

    private ScoreAssistantRoundDialog() {}

    static void show(
            Activity activity,
            List<ScoreAssistantApiProtocol.Player> players,
            int roundNumber,
            Submit submit) {
        Typeface font = Typeface.createFromAsset(
                activity.getAssets(), "fonts/zihun_jingdian_lihei.ttf");
        LinearLayout body = new LinearLayout(activity);
        body.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(activity, 20);
        body.setPadding(padding, padding, padding, padding);
        TextView hint = text(activity, "输入每人本局增减分，所有分数之和必须为 0", font);
        body.addView(hint);
        List<ScoreRow> rows = new ArrayList<>();
        for (ScoreAssistantApiProtocol.Player player : players) {
            LinearLayout row = new LinearLayout(activity);
            android.util.DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
            boolean narrow = Math.min(metrics.widthPixels, metrics.heightPixels)
                    / metrics.density < 520f;
            row.setOrientation(narrow ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            TextView name = text(activity,
                    player.name() + (player.ownerPlayer() ? "（本人）" : "")
                            + "  当前 " + signed(player.totalScore()), font);
            EditText score = new EditText(activity);
            score.setHint("例如 +18 或 -18");
            score.setSingleLine(true);
            score.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            score.setTypeface(font);
            score.setTextColor(BROWN);
            score.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17f);
            row.addView(name, narrow
                    ? new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 46))
                    : new LinearLayout.LayoutParams(0, dp(activity, 58), 1.4f));
            row.addView(score, narrow
                    ? new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 52))
                    : new LinearLayout.LayoutParams(0, dp(activity, 58), 1f));
            rows.add(new ScoreRow(player.playerId(), score));
            body.addView(row);
        }
        TextView error = text(activity, "", font);
        error.setTextColor(Color.rgb(191, 58, 47));
        body.addView(error);
        ScrollView scroll = new ScrollView(activity);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(251, 244, 225));
        scroll.addView(body);
        ScoreAssistantPortraitDialog.show(activity, "录入第 " + roundNumber + " 局", scroll,
                "提交本局", 0.78f, () -> {
                    List<ScoreAssistantInputValidator.ScoreDraft> drafts = new ArrayList<>();
                    for (ScoreRow row : rows) {
                        drafts.add(new ScoreAssistantInputValidator.ScoreDraft(
                                row.playerId(), row.score().getText().toString()));
                    }
                    ScoreAssistantInputValidator.Validation<
                            List<ScoreAssistantInputValidator.ScoreDelta>> validation =
                            ScoreAssistantInputValidator.validateRound(players, drafts);
                    if (!validation.valid()) {
                        error.setText(validation.error());
                        return false;
                    }
                    submit.submit(validation.value());
                    return true;
                });
    }

    private static TextView text(Activity activity, String value, Typeface font) {
        TextView result = new TextView(activity);
        result.setText(value);
        result.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17f);
        result.setTextColor(BROWN);
        result.setTypeface(font);
        result.setGravity(android.view.Gravity.CENTER_VERTICAL);
        return result;
    }

    private static String signed(long value) {
        return value > 0 ? "+" + value : Long.toString(value);
    }

    private static int dp(android.content.Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private record ScoreRow(java.util.UUID playerId, EditText score) {}
}
