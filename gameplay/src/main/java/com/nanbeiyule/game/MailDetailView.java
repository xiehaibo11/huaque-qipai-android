package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

final class MailDetailView extends View {
    interface Actions extends MailDetailTouchController.Actions {}

    private final MailDetailRenderer renderer;
    private final MailDetailTouchController touchController;
    private MailApiProtocol.MailDetail detail;
    private Runnable buttonClickSound = () -> {};

    MailDetailView(Context context, Actions actions) {
        super(context);
        setBackgroundColor(Color.TRANSPARENT);
        renderer = new MailDetailRenderer(context);
        touchController = new MailDetailTouchController(
                new SoundingActions(actions),
                ViewConfiguration.get(context).getScaledTouchSlop());
        setClickable(true);
        setFocusable(true);
    }

    void setDetail(MailApiProtocol.MailDetail detail) {
        this.detail = detail;
        touchController.setDetail(detail);
        invalidate();
    }

    void setButtonClickSound(Runnable sound) {
        buttonClickSound = sound == null ? () -> {} : sound;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (detail == null) return;
        MailLayout.Transform transform = MailLayout.Transform.contain(getWidth(), getHeight());
        canvas.save();
        canvas.translate(transform.offsetX(), transform.offsetY());
        canvas.scale(transform.scale(), transform.scale());
        renderer.draw(canvas, detail);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (detail == null || getWidth() <= 0 || getHeight() <= 0) return false;
        MailLayout.Transform transform = MailLayout.Transform.contain(getWidth(), getHeight());
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
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private final class SoundingActions implements MailDetailTouchController.Actions {
        private final Actions delegate;
        SoundingActions(Actions delegate) { this.delegate = delegate; }
        @Override public void onClose() { buttonClickSound.run(); delegate.onClose(); }
        @Override public void onDelete(String mailId) {
            buttonClickSound.run();
            delegate.onDelete(mailId);
        }
        @Override public void onClaim(String mailId) {
            buttonClickSound.run();
            delegate.onClaim(mailId);
        }
        @Override public void onDeleteBlocked() {
            buttonClickSound.run();
            delegate.onDeleteBlocked();
        }
    }
}
