package com.nanbeiyule.game;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Real-name dialog with two forms: a verified summary showing masked
 * identity data, and an input form that validates locally and reports
 * submissions through the injected listener. It never talks to the network.
 */
public final class RealNameDialog extends Dialog {
    interface Listener {
        void onSubmitRequested(
                RealNameDialog dialog,
                String realName,
                String idCardNumber);

        void onAlipayRequested(RealNameDialog dialog);
    }

    private final Activity activity;
    private final RealNameStatus verifiedStatus;
    private final boolean alipayEnabled;
    private final Listener listener;

    private EditText nameInput;
    private EditText idCardInput;
    private TextView errorView;
    private Button submitButton;
    private Button alipayButton;
    private View root;
    private View title;
    private View closeButton;
    private MaxHeightScrollView scrollView;
    private boolean submitting;

    RealNameDialog(
            Activity activity,
            RealNameStatus verifiedStatus,
            boolean alipayEnabled,
            Listener listener) {
        super(activity);
        this.activity = activity;
        this.verifiedStatus = verifiedStatus;
        this.alipayEnabled = alipayEnabled;
        this.listener = listener;
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_real_name);

        root = findViewById(R.id.real_name_root);
        TextView title = findViewById(R.id.real_name_title);
        this.title = title;
        scrollView = findViewById(R.id.real_name_scroll);
        View verifiedGroup = findViewById(R.id.real_name_verified_group);
        View formGroup = findViewById(R.id.real_name_form_group);
        nameInput = findViewById(R.id.real_name_input_name);
        idCardInput = findViewById(R.id.real_name_input_id_card);
        errorView = findViewById(R.id.real_name_error);
        submitButton = findViewById(R.id.real_name_submit);
        alipayButton = findViewById(R.id.real_name_alipay);
        Button closeButton = findViewById(R.id.real_name_close);
        this.closeButton = closeButton;

        if (verifiedStatus != null) {
            title.setText(R.string.real_name_title_verified);
            verifiedGroup.setVisibility(View.VISIBLE);
            formGroup.setVisibility(View.GONE);
            TextView name = findViewById(R.id.real_name_verified_name);
            TextView idCard =
                    findViewById(R.id.real_name_verified_id_card);
            name.setText(verifiedStatus.realNameMasked());
            idCard.setText(verifiedStatus.idCardMasked());
        } else {
            title.setText(R.string.real_name_title_form);
            verifiedGroup.setVisibility(View.GONE);
            formGroup.setVisibility(View.VISIBLE);
            alipayButton.setVisibility(
                    alipayEnabled ? View.VISIBLE : View.GONE);
            submitButton.setOnClickListener(view -> submitForm());
            alipayButton.setOnClickListener(
                    view -> {
                        if (!submitting && listener != null) {
                            listener.onAlipayRequested(this);
                        }
                    });
        }
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
        capScrollHeight(metrics);
    }

    /**
     * Caps the scrollable form/summary region so the title and close button
     * always stay on screen, instead of the dialog silently growing past
     * the bottom of the display (e.g. with a larger system font or the
     * Alipay button shown).
     */
    private void capScrollHeight(DisplayMetrics metrics) {
        if (scrollView == null || root == null || title == null
                || closeButton == null) {
            return;
        }
        int unspecified =
                View.MeasureSpec.makeMeasureSpec(
                        0, View.MeasureSpec.UNSPECIFIED);
        title.measure(unspecified, unspecified);
        closeButton.measure(unspecified, unspecified);
        ViewGroup.MarginLayoutParams closeParams =
                (ViewGroup.MarginLayoutParams) closeButton.getLayoutParams();
        int chromeHeight =
                root.getPaddingTop()
                        + root.getPaddingBottom()
                        + title.getMeasuredHeight()
                        + closeButton.getMeasuredHeight()
                        + closeParams.topMargin;
        int maxDialogHeight = (int) (metrics.heightPixels * 0.85f);
        int maxScrollHeight = maxDialogHeight - chromeHeight;
        if (maxScrollHeight > 0) {
            scrollView.setMaxHeight(maxScrollHeight);
        }
    }

    void setSubmitting(boolean submitting) {
        this.submitting = submitting;
        if (submitButton != null) {
            submitButton.setEnabled(!submitting);
        }
        if (alipayButton != null) {
            alipayButton.setEnabled(!submitting);
        }
    }

    void showError(String message) {
        if (errorView == null) {
            return;
        }
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }

    private void submitForm() {
        if (submitting || listener == null) {
            return;
        }
        String realName =
                RealNameValidator.normalize(
                        nameInput.getText().toString());
        if (!RealNameValidator.isValid(realName)) {
            showError(
                    activity.getString(
                            R.string.real_name_error_name_invalid));
            return;
        }
        String idCardNumber =
                IdCardNumberValidator.normalize(
                        idCardInput.getText().toString());
        if (!IdCardNumberValidator.isValid(idCardNumber)) {
            showError(
                    activity.getString(
                            R.string
                                    .real_name_error_id_card_invalid));
            return;
        }
        if (!IdCardNumberValidator.isAdult(idCardNumber)) {
            showError(
                    activity.getString(
                            R.string.real_name_error_underage));
            return;
        }
        errorView.setVisibility(View.GONE);
        listener.onSubmitRequested(this, realName, idCardNumber);
    }
}
