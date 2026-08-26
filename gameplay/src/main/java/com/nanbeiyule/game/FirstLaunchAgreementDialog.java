package com.nanbeiyule.game;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public final class FirstLaunchAgreementDialog extends Dialog {
    public interface OnRejectRequestedListener {
        void onRejectRequested();
    }

    public interface OnAcceptRequestedListener {
        void onAcceptRequested();
    }

    public interface OnAgreementLinkRequestedListener {
        void onAgreementLinkRequested(LoginAgreementLink link);
    }

    private final Activity activity;
    private OnRejectRequestedListener rejectRequestedListener;
    private OnAcceptRequestedListener acceptRequestedListener;
    private OnAgreementLinkRequestedListener
            agreementLinkRequestedListener;
    private boolean actionConsumed;

    public FirstLaunchAgreementDialog(Activity activity) {
        super(activity);
        this.activity = activity;
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    public void setOnRejectRequestedListener(
            OnRejectRequestedListener listener) {
        rejectRequestedListener = listener;
    }

    public void setOnAcceptRequestedListener(
            OnAcceptRequestedListener listener) {
        acceptRequestedListener = listener;
    }

    public void setOnAgreementLinkRequestedListener(
            OnAgreementLinkRequestedListener listener) {
        agreementLinkRequestedListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        inflateWithOriginalContentDensity();

        TextView links =
                findViewById(R.id.first_launch_agreement_links);
        links.setText(createLegalLinks());
        links.setMovementMethod(LinkMovementMethod.getInstance());
        links.setHighlightColor(Color.TRANSPARENT);

        Button reject =
                findViewById(R.id.first_launch_agreement_reject);
        Button accept =
                findViewById(R.id.first_launch_agreement_accept);
        reject.setOnClickListener(
                view -> {
                    if (actionConsumed) {
                        return;
                    }
                    actionConsumed = true;
                    if (rejectRequestedListener != null) {
                        rejectRequestedListener.onRejectRequested();
                    }
                });
        accept.setOnClickListener(
                view -> {
                    if (actionConsumed) {
                        return;
                    }
                    actionConsumed = true;
                    if (acceptRequestedListener != null) {
                        acceptRequestedListener.onAcceptRequested();
                    }
                });
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
        FirstLaunchAgreementLayout.Size size =
                FirstLaunchAgreementLayout.windowSize(
                        metrics.widthPixels,
                        metrics.heightPixels);
        window.setLayout(size.width(), size.height());
    }

    private SpannableStringBuilder createLegalLinks() {
        SpannableStringBuilder text =
                new SpannableStringBuilder();
        text.append(
                activity.getString(
                        R.string.first_launch_agreement_link_prefix));
        appendLink(
                text,
                "《"
                        + activity.getString(
                                R.string.login_service_agreement)
                        + "》",
                LoginAgreementLink.SERVICE);
        text.append(
                activity.getString(
                        R.string.first_launch_agreement_link_connector));
        appendLink(
                text,
                "《"
                        + activity.getString(
                                R.string.login_privacy_policy)
                        + "》",
                LoginAgreementLink.PRIVACY);
        text.append(
                activity.getString(
                        R.string.first_launch_agreement_link_suffix));
        return text;
    }

    private void inflateWithOriginalContentDensity() {
        DisplayMetrics metrics =
                activity.getResources().getDisplayMetrics();
        float originalDensity = metrics.density;
        float originalScaledDensity = metrics.scaledDensity;
        int originalDensityDpi = metrics.densityDpi;
        float fontScale =
                originalDensity == 0.0f
                        ? 1.0f
                        : originalScaledDensity / originalDensity;
        float targetDensity =
                FirstLaunchAgreementLayout.originalContentDensity(
                        metrics.widthPixels,
                        metrics.heightPixels);
        metrics.density = targetDensity;
        metrics.scaledDensity = targetDensity * fontScale;
        metrics.densityDpi = (int) (targetDensity * 160.0f);
        try {
            setContentView(
                    R.layout.dialog_first_launch_agreement);
        } finally {
            metrics.density = originalDensity;
            metrics.scaledDensity = originalScaledDensity;
            metrics.densityDpi = originalDensityDpi;
        }
    }

    private void appendLink(
            SpannableStringBuilder text,
            String label,
            LoginAgreementLink link) {
        int start = text.length();
        text.append(label);
        int end = text.length();
        text.setSpan(
                new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        if (agreementLinkRequestedListener != null) {
                            agreementLinkRequestedListener
                                    .onAgreementLinkRequested(link);
                        }
                    }

                    @Override
                    public void updateDrawState(TextPaint drawState) {
                        super.updateDrawState(drawState);
                        drawState.setColor(
                                activity.getResources().getColor(
                                        R.color
                                                .first_launch_agreement_link_text));
                        drawState.setUnderlineText(true);
                    }
                },
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
