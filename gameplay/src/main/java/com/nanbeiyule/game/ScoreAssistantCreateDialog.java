package com.nanbeiyule.game;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/** Real manual 2–6 player entry form; it never invents names or an owner. */
final class ScoreAssistantCreateDialog {
    interface Submit {
        void submit(List<ScoreAssistantInputValidator.PlayerDraft> players);
    }

    private static final int PAPER = Color.rgb(251, 244, 225);
    private static final int BROWN = Color.rgb(91, 63, 39);

    private ScoreAssistantCreateDialog() {}

    static void show(Activity activity, Submit submit) {
        Typeface font = Typeface.createFromAsset(
                activity.getAssets(), "fonts/zihun_jingdian_lihei.ttf");
        LinearLayout body = column(activity, 20);
        TextView hint = label(activity, "手工输入 2–6 名玩家，并勾选且仅勾选一名“本人”", font, 17);
        body.addView(hint);
        LinearLayout rowsView = column(activity, 8);
        List<PlayerRow> rows = new ArrayList<>();
        body.addView(rowsView);
        TextView error = label(activity, "", font, 16);
        error.setTextColor(Color.rgb(191, 58, 47));
        LinearLayout controls = new LinearLayout(activity);
        controls.setGravity(android.view.Gravity.CENTER);
        Button remove = button(activity, "减少一人", font);
        Button add = button(activity, "添加一人", font);
        controls.addView(remove, weighted());
        controls.addView(add, weighted());
        body.addView(controls);
        body.addView(error);
        for (int index = 0; index < 4; index++) addRow(activity, rowsView, rows, font);

        ScrollView scroll = new ScrollView(activity);
        scroll.setFillViewport(true);
        scroll.setBackground(paperBackground());
        scroll.addView(body);
        add.setOnClickListener(view -> {
            if (rows.size() < 6) addRow(activity, rowsView, rows, font);
            else error.setText("最多只能添加 6 名玩家");
        });
        remove.setOnClickListener(view -> {
            if (rows.size() > 2) {
                PlayerRow removed = rows.remove(rows.size() - 1);
                rowsView.removeView(removed.container());
                error.setText("");
            } else {
                error.setText("至少需要 2 名玩家");
            }
        });
        ScoreAssistantPortraitDialog.show(activity, "新建麻将计分", scroll, "创建", 0.82f,
                () -> {
                    List<ScoreAssistantInputValidator.PlayerDraft> drafts = new ArrayList<>();
                    for (PlayerRow row : rows) {
                        drafts.add(new ScoreAssistantInputValidator.PlayerDraft(
                                row.name().getText().toString(), row.owner().isChecked()));
                    }
                    ScoreAssistantInputValidator.Validation<
                            List<ScoreAssistantInputValidator.PlayerDraft>> validation =
                            ScoreAssistantInputValidator.validatePlayers(drafts);
                    if (!validation.valid()) {
                        error.setText(validation.error());
                        return false;
                    }
                    submit.submit(validation.value());
                    return true;
                });
    }

    private static void addRow(
            Activity activity,
            LinearLayout target,
            List<PlayerRow> rows,
            Typeface font) {
        LinearLayout row = new LinearLayout(activity);
        android.util.DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        boolean narrow = Math.min(metrics.widthPixels, metrics.heightPixels)
                / metrics.density < 520f;
        row.setOrientation(narrow ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(0, 6, 0, 6);
        RadioButton owner = new RadioButton(activity);
        owner.setText("本人");
        owner.setTypeface(font);
        owner.setTextColor(BROWN);
        owner.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f);
        EditText name = new EditText(activity);
        name.setHint("请输入玩家名称");
        name.setSingleLine(true);
        name.setMaxLines(1);
        name.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        name.setTextColor(BROWN);
        name.setHintTextColor(Color.rgb(156, 132, 102));
        name.setTypeface(font);
        name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17f);
        row.addView(owner, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        row.addView(name, narrow
                ? new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 52))
                : new LinearLayout.LayoutParams(0, dp(activity, 52), 1f));
        PlayerRow playerRow = new PlayerRow(row, owner, name);
        rows.add(playerRow);
        owner.setOnClickListener(view -> {
            for (PlayerRow item : rows) item.owner().setChecked(item == playerRow);
        });
        target.addView(row);
    }

    private static LinearLayout column(Activity activity, int paddingDp) {
        LinearLayout view = new LinearLayout(activity);
        view.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(activity, paddingDp);
        view.setPadding(padding, padding, padding, padding);
        return view;
    }

    private static TextView label(Activity activity, String text, Typeface font, int size) {
        TextView view = new TextView(activity);
        view.setText(text);
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
        view.setTextColor(BROWN);
        view.setTypeface(font);
        return view;
    }

    private static Button button(Activity activity, String text, Typeface font) {
        Button view = new Button(activity);
        view.setText(text);
        view.setTypeface(font);
        view.setTextColor(BROWN);
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17f);
        return view;
    }

    private static LinearLayout.LayoutParams weighted() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
    }

    private static GradientDrawable paperBackground() {
        GradientDrawable result = new GradientDrawable();
        result.setColor(PAPER);
        result.setCornerRadius(18f);
        return result;
    }

    private static int dp(android.content.Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private record PlayerRow(LinearLayout container, RadioButton owner, EditText name) {}
}
