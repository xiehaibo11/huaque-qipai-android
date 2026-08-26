package com.nanbeiyule.game;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Add-friend dialog: numeric query input, a result card with the
 * relation-specific action, and no network access of its own.
 */
public final class FriendAddDialog extends Dialog {
    interface Listener {
        void onSearchRequested(FriendAddDialog dialog, String query);

        void onApplyRequested(
                FriendAddDialog dialog, FriendSearchResult result);
    }

    private final Activity activity;
    private final Listener listener;

    private EditText queryInput;
    private Button searchButton;
    private TextView errorView;
    private View resultGroup;
    private ImageView resultAvatar;
    private TextView resultName;
    private TextView resultId;
    private TextView resultStatus;
    private Button applyButton;
    private FriendSearchResult currentResult;
    private boolean searching;

    FriendAddDialog(Activity activity, Listener listener) {
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
        setContentView(R.layout.dialog_friend_add);

        queryInput = findViewById(R.id.friend_add_input);
        searchButton = findViewById(R.id.friend_add_search);
        errorView = findViewById(R.id.friend_add_error);
        resultGroup = findViewById(R.id.friend_add_result_group);
        resultAvatar = findViewById(R.id.friend_add_result_avatar);
        resultName = findViewById(R.id.friend_add_result_name);
        resultId = findViewById(R.id.friend_add_result_id);
        resultStatus = findViewById(R.id.friend_add_result_status);
        applyButton = findViewById(R.id.friend_add_apply);
        Button closeButton = findViewById(R.id.friend_add_close);

        searchButton.setOnClickListener(view -> submitSearch());
        applyButton.setOnClickListener(
                view -> {
                    if (!searching
                            && currentResult != null
                            && listener != null) {
                        listener.onApplyRequested(this, currentResult);
                    }
                });
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

    void setSearching(boolean searching) {
        this.searching = searching;
        if (searchButton != null) {
            searchButton.setEnabled(!searching);
        }
        if (applyButton != null) {
            applyButton.setEnabled(!searching);
        }
    }

    void showError(String message) {
        if (errorView == null) {
            return;
        }
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
        if (resultGroup != null) {
            resultGroup.setVisibility(View.GONE);
        }
    }

    void showResult(FriendSearchResult result) {
        currentResult = result;
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }
        if (resultGroup == null || result == null) {
            return;
        }
        resultGroup.setVisibility(View.VISIBLE);
        resultName.setText(result.displayName());
        resultId.setText(
                activity.getString(
                        R.string.friend_player_id_format,
                        result.publicPlayerId()));
        switch (result.relation()) {
            case NONE -> {
                resultStatus.setVisibility(View.GONE);
                applyButton.setVisibility(View.VISIBLE);
            }
            case PENDING -> {
                resultStatus.setText(R.string.friend_add_pending);
                resultStatus.setVisibility(View.VISIBLE);
                applyButton.setVisibility(View.GONE);
            }
            case FRIEND -> {
                resultStatus.setText(R.string.friend_add_already_friend);
                resultStatus.setVisibility(View.VISIBLE);
                applyButton.setVisibility(View.GONE);
            }
            case REJECTED -> {
                resultStatus.setText(R.string.friend_add_rejected);
                resultStatus.setVisibility(View.VISIBLE);
                applyButton.setVisibility(View.GONE);
            }
        }
    }

    /** Switches the card to the pending state after a successful apply. */
    void markApplied() {
        if (resultStatus == null || applyButton == null) {
            return;
        }
        resultStatus.setText(R.string.friend_add_pending);
        resultStatus.setVisibility(View.VISIBLE);
        applyButton.setVisibility(View.GONE);
    }

    void setResultAvatar(Bitmap bitmap) {
        if (resultAvatar != null
                && bitmap != null
                && !bitmap.isRecycled()) {
            resultAvatar.setImageBitmap(circular(bitmap));
        }
    }

    private void submitSearch() {
        if (searching || listener == null) {
            return;
        }
        String query = queryInput.getText().toString().trim();
        if (query.isEmpty()) {
            showError(
                    activity.getString(
                            R.string.friend_add_empty_query));
            return;
        }
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }
        listener.onSearchRequested(this, query);
    }

    private static Bitmap circular(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());
        Bitmap output =
                Bitmap.createBitmap(
                        size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Path path = new Path();
        path.addCircle(
                size / 2.0f, size / 2.0f, size / 2.0f,
                Path.Direction.CW);
        canvas.clipPath(path);
        int left = (source.getWidth() - size) / 2;
        int top = (source.getHeight() - size) / 2;
        canvas.drawBitmap(
                source,
                new Rect(left, top, left + size, top + size),
                new RectF(0.0f, 0.0f, size, size),
                new Paint(
                        Paint.ANTI_ALIAS_FLAG
                                | Paint.FILTER_BITMAP_FLAG));
        return output;
    }
}
