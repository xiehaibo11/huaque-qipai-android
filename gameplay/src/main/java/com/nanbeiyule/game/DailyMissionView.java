package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

final class DailyMissionView extends View {
    interface Actions extends DailyMissionTouchController.Actions {}

    private final DailyMissionRenderer renderer;
    private final DailyMissionTouchController touchController;
    private DailyMissionState state;
    private boolean loading = true;
    private String errorMessage = "";
    private long stateReceivedElapsed;
    private final long effectStartedElapsed = SystemClock.elapsedRealtime();
    private Runnable buttonClickSound = () -> {};

    DailyMissionView(Context context, Actions actions) {
        super(context);
        setBackgroundColor(Color.TRANSPARENT);
        renderer = new DailyMissionRenderer(context);
        touchController = new DailyMissionTouchController(
                new SoundingActions(actions),
                ViewConfiguration.get(context).getScaledTouchSlop());
    }

    void setState(DailyMissionState state) {
        this.state = state;
        stateReceivedElapsed = SystemClock.elapsedRealtime();
        loading = false;
        errorMessage = "";
        touchController.setState(state);
        touchController.setContentInteractive(true);
        invalidate();
    }

    void setLoading(boolean loading) {
        this.loading = loading;
        if (loading) errorMessage = "";
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

    DailyMissionState state() { return state; }
    float taskScroll() { return touchController.taskScroll(); }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.argb(178, 3, 10, 31));
        DailyMissionLayout.Transform transform =
                DailyMissionLayout.Transform.contain(getWidth(), getHeight());
        canvas.save();
        canvas.translate(transform.offsetX(), transform.offsetY());
        canvas.scale(transform.scale(), transform.scale());
        renderer.draw(
                canvas,
                state,
                touchController.taskScroll(),
                touchController::rewardScroll,
                touchController.pressedTaskIndex(),
                countdownSeconds(),
                effectSeconds(),
                serverNow(),
                loading,
                errorMessage);
        canvas.restore();
        if (renderer.animated()) {
            // 原版六套骨骼动画常驻循环播放，按帧驱动。
            postInvalidateOnAnimation();
        } else if (state != null && countdownSeconds() > 0) {
            postInvalidateDelayed(1_000L);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0) return false;
        DailyMissionLayout.Transform transform =
                DailyMissionLayout.Transform.contain(getWidth(), getHeight());
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

    /** 倒计时以响应里的 serverTime 加单调时钟推导，不受设备系统时间被改动影响。 */
    private java.time.Instant serverNow() {
        if (state == null) return null;
        return state.serverTime().plusSeconds(
                Math.max(0L, (SystemClock.elapsedRealtime() - stateReceivedElapsed) / 1_000L));
    }

    private float effectSeconds() {
        return (SystemClock.elapsedRealtime() - effectStartedElapsed) / 1_000f;
    }

    private long countdownSeconds() {
        if (state == null) return 0L;
        long elapsed = Math.max(0L,
                (SystemClock.elapsedRealtime() - stateReceivedElapsed) / 1_000L);
        return Math.max(0L, state.secondsRemaining() - elapsed);
    }

    private final class SoundingActions implements DailyMissionTouchController.Actions {
        private final Actions delegate;

        SoundingActions(Actions delegate) { this.delegate = delegate; }

        @Override public void onClose() {
            buttonClickSound.run();
            delegate.onClose();
        }

        @Override public void onPageSelected(String pageCode) {
            buttonClickSound.run();
            delegate.onPageSelected(pageCode);
        }

        @Override public void onTaskClaim(String taskCode) {
            buttonClickSound.run();
            delegate.onTaskClaim(taskCode);
        }

        @Override public void onTaskGo(DailyMissionState.Task task) {
            buttonClickSound.run();
            delegate.onTaskGo(task);
        }

        @Override public void onMilestoneClaim(long target) {
            buttonClickSound.run();
            delegate.onMilestoneClaim(target);
        }
    }
}
