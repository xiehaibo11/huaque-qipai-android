package com.nanbeiyule.game;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

/**
 * Incoming friend applications dialog: one row per application with
 * accept/reject actions. It renders supplied data and never talks to
 * the network.
 */
public final class FriendApplicationsDialog extends Dialog {
    interface Listener {
        void onAccept(FriendApplicationItem item);

        void onReject(FriendApplicationItem item);
    }

    private static final int ROW_TEXT = Color.WHITE;

    private final Activity activity;
    private final Listener listener;

    private TextView emptyView;
    private View scrollView;
    private LinearLayout listContainer;

    FriendApplicationsDialog(Activity activity, Listener listener) {
        super(activity);
        this.activity = activity;
        this.listener = listener;
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_friend_applications);

        emptyView = findViewById(R.id.friend_applications_empty);
        scrollView = findViewById(R.id.friend_applications_scroll);
        listContainer = findViewById(R.id.friend_applications_list);
        Button closeButton =
                findViewById(R.id.friend_applications_close);
        closeButton.setOnClickListener(view -> dismiss());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window == null) {
            return;
        }
        window.setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams attributes =
                window.getAttributes();
        attributes.dimAmount = 0.6f;
        window.setAttributes(attributes);

        DisplayMetrics metrics =
                activity.getResources().getDisplayMetrics();
        int width =
                Math.min(
                        metrics.widthPixels,
                        (int) (metrics.heightPixels * 0.9f));
        window.setLayout(
                width, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    void setApplications(List<FriendApplicationItem> applications) {
        if (listContainer == null || emptyView == null) {
            return;
        }
        listContainer.removeAllViews();
        boolean empty = applications == null || applications.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        scrollView.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            return;
        }
        for (FriendApplicationItem item : applications) {
            listContainer.addView(buildRow(item));
        }
    }

    private View buildRow(FriendApplicationItem item) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int verticalPadding = dp(8);
        row.setPadding(0, verticalPadding, 0, verticalPadding);

        TextView label = new TextView(activity);
        label.setText(
                activity.getString(
                        R.string.friend_application_row_format,
                        item.displayName(),
                        item.publicPlayerId()));
        label.setTextColor(ROW_TEXT);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        LinearLayout.LayoutParams labelParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f);
        row.addView(label, labelParams);

        Button accept = actionButton(
                R.drawable.friend_new_agree,
                activity.getString(R.string.friend_application_accept));
        Button reject = actionButton(
                R.drawable.friend_new_refuse,
                activity.getString(R.string.friend_application_reject));
        accept.setOnClickListener(
                view -> {
                    accept.setEnabled(false);
                    reject.setEnabled(false);
                    if (listener != null) {
                        listener.onAccept(item);
                    }
                });
        reject.setOnClickListener(
                view -> {
                    accept.setEnabled(false);
                    reject.setEnabled(false);
                    if (listener != null) {
                        listener.onReject(item);
                    }
                });
        LinearLayout.LayoutParams acceptParams =
                new LinearLayout.LayoutParams(dp(108), dp(47));
        acceptParams.setMarginStart(dp(8));
        row.addView(accept, acceptParams);
        LinearLayout.LayoutParams rejectParams =
                new LinearLayout.LayoutParams(dp(108), dp(47));
        rejectParams.setMarginStart(dp(8));
        row.addView(reject, rejectParams);
        return row;
    }

    private Button actionButton(
            int backgroundResource, String contentDescription) {
        Button button = new Button(activity);
        button.setBackgroundResource(backgroundResource);
        button.setContentDescription(contentDescription);
        button.setMinWidth(0);
        button.setMinHeight(0);
        button.setMinimumWidth(0);
        button.setMinimumHeight(0);
        int horizontal = dp(4);
        button.setPadding(horizontal, 0, horizontal, 0);
        return button;
    }

    private int dp(int value) {
        return (int)
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        value,
                        activity.getResources().getDisplayMetrics());
    }
}
