package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import java.util.List;

final class MailView extends View {
    interface Actions extends MailTouchController.Actions {}

    private final MailRenderer renderer;
    private final MailTouchController touchController;
    private final MailState state = new MailState();
    private boolean loading = true;
    private String errorMessage = "";
    private Runnable buttonClickSound = () -> {};
    private Runnable detailClosed = () -> {};

    MailView(Context context, Actions actions) {
        super(context);
        setBackgroundColor(Color.TRANSPARENT);
        renderer = new MailRenderer(context);
        touchController = new MailTouchController(
                new SoundingActions(actions),
                state,
                ViewConfiguration.get(context).getScaledTouchSlop());
        touchController.setInvalidate(this::invalidate);
        setContentDescription(getResources().getString(R.string.mail_accessibility));
        setFocusable(true);
        setClickable(true);
    }

    MailState state() { return state; }

    void setSummary(MailApiProtocol.MailSummary summary) {
        state.setSummary(summary);
    }

    void setMails(List<MailApiProtocol.MailEntry> mails) {
        state.setMails(mails);
        loading = false;
        errorMessage = "";
        touchController.clampScroll();
        touchController.setContentInteractive(true);
        invalidate();
    }

    void setPage(MailApiProtocol.MailPage page) {
        if (page != null && page.page() == 1) {
            renderer.restartRowEntrance();
        }
        state.setPage(page);
        loading = false;
        errorMessage = "";
        touchController.clampScroll();
        touchController.setContentInteractive(true);
        invalidate();
    }

    void setDetail(MailApiProtocol.MailDetail detail) {
        state.setDetail(detail);
        invalidate();
    }

    void setAnimatedMailboxVisible(boolean visible) {
        renderer.setAnimatedMailboxVisible(visible);
        invalidate();
    }

    void setDetailClosedListener(Runnable listener) {
        detailClosed = listener == null ? () -> {} : listener;
    }

    void markRead(String mailId) {
        state.markRead(mailId);
        invalidate();
    }

    void markAllRead() {
        state.markAllRead();
        invalidate();
    }

    void markClaimed(List<String> mailIds) {
        state.markClaimed(mailIds);
        invalidate();
    }

    void removeMailIds(List<String> mailIds) {
        state.removeMailIds(mailIds);
        touchController.clampScroll();
        invalidate();
    }

    void setLoading(boolean loading) {
        this.loading = loading;
        if (loading) {
            errorMessage = "";
        }
        touchController.setContentInteractive(!loading);
        invalidate();
    }

    void setError(String message) {
        loading = false;
        errorMessage = message == null ? "" : message;
        touchController.setContentInteractive(true);
        invalidate();
    }

    void setButtonClickSound(Runnable sound) {
        buttonClickSound = sound == null ? () -> {} : sound;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        MailViewport viewport = MailViewport.fullBleed(getWidth(), getHeight());
        renderer.drawFullBleedBackground(
                canvas, viewport.backgroundWidth(), viewport.backgroundHeight());
        MailLayout.Transform transform = viewport.content();
        canvas.save();
        canvas.translate(transform.offsetX(), transform.offsetY());
        canvas.scale(transform.scale(), transform.scale());
        renderer.draw(canvas, state, touchController.scroll(), loading, errorMessage);
        canvas.restore();
        if (renderer.rowEntranceRunning(state.mails().size())) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return false;
        }
        MailLayout.Transform transform =
                MailViewport.fullBleed(getWidth(), getHeight()).content();
        float x = transform.designX(event.getX());
        float y = transform.designY(event.getY());
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> touchController.onDown(x, y);
            case MotionEvent.ACTION_MOVE -> touchController.onMove(x, y);
            case MotionEvent.ACTION_UP -> {
                touchController.onUp(x, y);
                performClick();
            }
            case MotionEvent.ACTION_CANCEL -> touchController.cancel();
            default -> { return false; }
        }
        invalidate();
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private final class SoundingActions implements MailTouchController.Actions {
        private final Actions delegate;

        SoundingActions(Actions delegate) { this.delegate = delegate; }

        @Override public void onClose() {
            buttonClickSound.run();
            delegate.onClose();
        }

        @Override public void onMailOpen(MailApiProtocol.MailEntry entry) {
            buttonClickSound.run();
            delegate.onMailOpen(entry);
        }

        @Override public void onReadAll() {
            buttonClickSound.run();
            delegate.onReadAll();
        }

        @Override public void onDelete(List<String> mailIds) {
            buttonClickSound.run();
            delegate.onDelete(mailIds);
        }

        @Override public void onDeleteBlocked() {
            buttonClickSound.run();
            delegate.onDeleteBlocked();
        }

        @Override public void onClaimAll() {
            buttonClickSound.run();
            delegate.onClaimAll();
        }

        @Override public void onLoadNextPage() {
            delegate.onLoadNextPage();
        }

        @Override public void onDetailClose() {
            buttonClickSound.run();
            detailClosed.run();
            delegate.onDetailClose();
        }
    }
}
