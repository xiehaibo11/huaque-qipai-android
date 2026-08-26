package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

/** 定时登录有礼的 1920x1080 自绘页面。绘制与命中共用同一个 {@link TimeLoginActLayout.Transform}。 */
@SuppressLint("ViewConstructor")
final class TimeLoginActView extends View {
    interface Actions {
        void onClose();

        void onClaimSlot(TimeLoginActState.Slot slot);

        void onOpenWheel();
    }

    private final Actions actions;
    private final TimeLoginActRenderer renderer;

    private TimeLoginActState state;
    private boolean loading;
    private String errorMessage = "";
    private long stateLoadedUptimeMs;
    private int pressedSlotIndex = -1;
    private boolean wheelPressed;

    TimeLoginActView(Context context, Actions actions) {
        super(context);
        this.actions = actions;
        renderer = new TimeLoginActRenderer(new TimeLoginActDrawables(context.getResources()));
        setClickable(true);
    }

    void setState(TimeLoginActState state) {
        this.state = state;
        stateLoadedUptimeMs = SystemClock.elapsedRealtime();
        loading = false;
        errorMessage = "";
        invalidate();
    }

    void setLoading(boolean loading) {
        this.loading = loading;
        if (loading) {
            errorMessage = "";
        }
        invalidate();
    }

    void setError(String message) {
        loading = false;
        errorMessage = message == null ? "" : message;
        invalidate();
    }

    TimeLoginActState state() {
        return state;
    }

    /** 自服务端快照以来本地已流逝的秒数，用于推进倒计时（原版每秒调度一次）。 */
    long elapsedSeconds() {
        if (state == null) {
            return 0;
        }
        return (SystemClock.elapsedRealtime() - stateLoadedUptimeMs) / 1000L;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(TimeLoginActRenderer.backgroundColor());
        TimeLoginActLayout.Transform transform =
                TimeLoginActLayout.Transform.contain(getWidth(), getHeight());
        canvas.save();
        canvas.translate(transform.offsetX(), transform.offsetY());
        canvas.scale(transform.scale(), transform.scale());
        renderer.draw(
                canvas,
                state,
                elapsedSeconds(),
                pressedSlotIndex,
                wheelPressed,
                loading,
                errorMessage);
        canvas.restore();
        if (state != null && state.countdownSlotIndex() >= 0) {
            postInvalidateDelayed(1_000L);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0 || loading) {
            return false;
        }
        TimeLoginActLayout.Transform transform =
                TimeLoginActLayout.Transform.contain(getWidth(), getHeight());
        float x = transform.designX(event.getX());
        float y = transform.designY(event.getY());
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> onDown(x, y);
            case MotionEvent.ACTION_UP -> {
                onUp(x, y);
                performClick();
            }
            case MotionEvent.ACTION_CANCEL -> clearPressed();
            default -> {
                return true;
            }
        }
        invalidate();
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void onDown(float x, float y) {
        if (state == null) {
            return;
        }
        int index = TimeLoginActRenderer.slotIndexAt(state.slots().size(), x, y);
        if (index >= 0
                && state.slots().get(index).claimable()
                && TimeLoginActRenderer.claimButtonHit(index, x, y)) {
            pressedSlotIndex = index;
            return;
        }
        wheelPressed = wheelButtonHit(x, y);
    }

    private void onUp(float x, float y) {
        if (state == null) {
            clearPressed();
            if (TimeLoginActLayout.CLOSE.contains(x, y)) {
                actions.onClose();
            }
            return;
        }
        int pressed = pressedSlotIndex;
        boolean wheel = wheelPressed;
        clearPressed();
        if (TimeLoginActLayout.CLOSE.contains(x, y)) {
            actions.onClose();
            return;
        }
        if (pressed >= 0
                && TimeLoginActRenderer.claimButtonHit(pressed, x, y)
                && state.slots().get(pressed).claimable()) {
            actions.onClaimSlot(state.slots().get(pressed));
            return;
        }
        if (wheel && wheelButtonHit(x, y)) {
            actions.onOpenWheel();
        }
    }

    /** 原版只有解锁后才存在抽奖按钮；未解锁时进度条不接收点击。 */
    private boolean wheelButtonHit(float x, float y) {
        return state != null
                && state.wheel() != null
                && state.wheel().unlocked()
                && TimeLoginActLayout.WHEEL_BUTTON.contains(x, y);
    }

    private void clearPressed() {
        pressedSlotIndex = -1;
        wheelPressed = false;
    }
}
