package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/** Inset-aware full-screen interaction surface for the recovered health notice. */
@SuppressLint("ViewConstructor")
final class HealthNoticeView extends AdaptiveCanvasView {
    private final HealthNoticeRenderer renderer;
    private final HealthNoticeScrollState scroll;
    private final int touchSlop;
    private Runnable dismissRequested = () -> {};
    private Runnable buttonClickSound = () -> {};
    private float touchStartY;
    private float lastY;
    private boolean contentGesture;
    private boolean dragging;
    private boolean outsidePressed;

    HealthNoticeView(Context context) {
        super(context);
        renderer = new HealthNoticeRenderer(context);
        scroll =
                new HealthNoticeScrollState(
                        renderer.contentHeight(), HealthNoticeLayout.CONTENT_VIEWPORT_HEIGHT);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setClickable(true);
        setFocusable(true);
        setContentDescription(HealthNoticeContent.accessibilityText());
    }

    void setOnDismissRequested(Runnable listener) {
        dismissRequested = listener == null ? () -> {} : listener;
    }

    void setButtonClickSound(Runnable sound) {
        buttonClickSound = sound == null ? () -> {} : sound;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.argb(178, 0, 0, 0));
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        AdaptiveViewport.Transform transform =
                HealthNoticeLayout.panelTransform(
                        adaptiveViewport(
                                HealthNoticeLayout.DESIGN_WIDTH,
                                HealthNoticeLayout.DESIGN_HEIGHT));
        int save = AdaptiveCanvasDrawing.apply(canvas, transform);
        renderer.draw(canvas, scroll.offset());
        canvas.restoreToCount(save);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return false;
        }
        AdaptiveViewport.Transform transform =
                HealthNoticeLayout.panelTransform(
                        adaptiveViewport(
                                HealthNoticeLayout.DESIGN_WIDTH,
                                HealthNoticeLayout.DESIGN_HEIGHT));
        float x = transform.unmapX(event.getX());
        float y = transform.unmapY(event.getY());
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                touchStartY = y;
                lastY = y;
                contentGesture = HealthNoticeLayout.contentContains(x, y);
                outsidePressed = !HealthNoticeLayout.panelContains(x, y);
                dragging = false;
                return true;
            }
            case MotionEvent.ACTION_MOVE -> {
                if (contentGesture) {
                    if (!dragging
                            && Math.abs(y - touchStartY) * transform.scaleY() > touchSlop) {
                        dragging = true;
                    }
                    if (dragging) {
                        scroll.moveByFingerDelta(y - lastY);
                        invalidate();
                    }
                }
                lastY = y;
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                resetGesture();
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                boolean wasDragging = dragging;
                boolean dismiss =
                        !wasDragging
                                && (HealthNoticeLayout.closeContains(x, y)
                                        || (outsidePressed
                                                && !HealthNoticeLayout.panelContains(x, y)));
                resetGesture();
                if (dismiss) {
                    performClick();
                    dismissRequested.run();
                }
                return true;
            }
            default -> {
                return true;
            }
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        buttonClickSound.run();
        return true;
    }

    private void resetGesture() {
        contentGesture = false;
        outsidePressed = false;
        dragging = false;
    }
}
