package com.nanbeiyule.game;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import java.util.List;

/** Native full-screen host for the recovered MailMainLayer composition. */
final class MailDialog extends Dialog {
    interface Actions extends MailView.Actions {
        void onDetailDelete(String mailId);
        void onDetailClaim(String mailId);
    }

    private final MailView mailView;
    private final MailEffectView effectView;
    private final MailEffectView detailEffectView;
    private final MailDetailView detailView;

    MailDialog(Context context, Actions actions) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        mailView = new MailView(context, actions);
        effectView = new MailEffectView(context, () -> mailView.setAnimatedMailboxVisible(true));
        detailEffectView = new MailEffectView(
                context, MailEffectSpec.detailPanel(), () -> {});
        detailView = new MailDetailView(context, new MailDetailActions(actions));
        mailView.setDetailClosedListener(this::closeDetail);
        FrameLayout content = new FrameLayout(context);
        FrameLayout.LayoutParams match = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        content.addView(mailView, match);
        content.addView(effectView, match);
        content.addView(detailEffectView, match);
        content.addView(detailView, match);
        detailEffectView.setVisibility(android.view.View.INVISIBLE);
        detailView.setVisibility(android.view.View.INVISIBLE);
        setContentView(content, match);
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            applyFullscreen(window);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window == null) {
            return;
        }
        window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        applyFullscreen(window);
        window.getDecorView().setSystemUiVisibility(MainActivityState.IMMERSIVE_UI_FLAGS);
    }

    MailState state() {
        return mailView.state();
    }

    void setSummary(MailApiProtocol.MailSummary summary) {
        mailView.setSummary(summary);
    }

    void setMails(List<MailApiProtocol.MailEntry> mails) {
        mailView.setMails(mails);
    }

    void setPage(MailApiProtocol.MailPage page) {
        mailView.setPage(page);
    }

    void setDetail(MailApiProtocol.MailDetail detail) {
        effectView.setVisibility(detail == null ? android.view.View.VISIBLE : android.view.View.INVISIBLE);
        mailView.setDetail(detail);
        detailEffectView.setVisibility(
                detail == null ? android.view.View.INVISIBLE : android.view.View.VISIBLE);
        detailView.setDetail(detail);
        detailView.setVisibility(
                detail == null ? android.view.View.INVISIBLE : android.view.View.VISIBLE);
    }

    void markRead(String mailId) {
        mailView.markRead(mailId);
    }

    void markAllRead() {
        mailView.markAllRead();
    }

    void markClaimed(List<String> mailIds) {
        mailView.markClaimed(mailIds);
        detailView.setDetail(mailView.state().detail());
    }

    void removeMailIds(List<String> mailIds) {
        mailView.removeMailIds(mailIds);
        if (mailView.state().detail() == null) {
            closeDetail();
        }
    }

    void setLoading(boolean loading) {
        mailView.setLoading(loading);
    }

    void setError(String message) {
        mailView.setError(message);
    }

    void setButtonClickSound(Runnable sound) {
        mailView.setButtonClickSound(sound);
        detailView.setButtonClickSound(sound);
    }

    private void closeDetail() {
        mailView.setDetail(null);
        detailView.setDetail(null);
        detailView.setVisibility(android.view.View.INVISIBLE);
        detailEffectView.setVisibility(android.view.View.INVISIBLE);
        effectView.setVisibility(android.view.View.VISIBLE);
    }

    private final class MailDetailActions implements MailDetailView.Actions {
        private final Actions actions;

        MailDetailActions(Actions actions) {
            this.actions = actions;
        }

        @Override public void onClose() {
            closeDetail();
        }

        @Override public void onDelete(String mailId) {
            actions.onDetailDelete(mailId);
        }

        @Override public void onClaim(String mailId) {
            actions.onDetailClaim(mailId);
        }

        @Override public void onDeleteBlocked() {
            actions.onDeleteBlocked();
        }
    }

    private static void applyFullscreen(Window window) {
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.height = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.gravity = Gravity.TOP | Gravity.START;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
            attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        window.setAttributes(attributes);
    }
}
