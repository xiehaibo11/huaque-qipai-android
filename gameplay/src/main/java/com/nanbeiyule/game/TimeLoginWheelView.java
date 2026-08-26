package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import com.nanbeiyule.game.TimeLoginActLayout.Box;
import java.util.List;

/**
 * 原版 TimeLoginActWheelView 的八格转盘页。中奖格由服务端决定，客户端只按返回的
 * {@code wheelSliceIndex} 播放 5.7 秒缓出旋转并高亮停格，不实现任何概率。
 */
@SuppressLint("ViewConstructor")
final class TimeLoginWheelView extends View {
    interface Actions {
        void onClose();

        void onDraw();
    }

    private final Actions actions;
    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();

    private final Bitmap board;
    private final Bitmap ring;
    private final Bitmap pointer;
    private final Bitmap pointerLocked;
    private final Bitmap selectGlow;
    private final Bitmap rollText;
    private final Bitmap close;
    private final Bitmap coinIcon;

    private TimeLoginActState.Wheel wheel;
    private int targetSliceIndex = -1;
    private long rollStartUptimeMs;
    private boolean rolling;

    TimeLoginWheelView(Context context, Actions actions) {
        super(context);
        this.actions = actions;
        Resources resources = context.getResources();
        board = load(resources, R.drawable.time_login_act_wheel_board);
        ring = load(resources, R.drawable.time_login_act_wheel_ring);
        pointer = load(resources, R.drawable.time_login_act_wheel_pointer);
        pointerLocked = load(resources, R.drawable.time_login_act_wheel_pointer_off);
        selectGlow = load(resources, R.drawable.time_login_act_wheel_select);
        rollText = load(resources, R.drawable.time_login_act_wheel_roll_text);
        close = load(resources, R.drawable.time_login_act_close);
        coinIcon = load(resources, R.drawable.time_login_act_wheel_coin_icon);
        textPaint.setTypeface(
                Typeface.createFromAsset(
                        resources.getAssets(), TimeLoginActDrawables.TEXT_FONT_ASSET));
        setClickable(true);
    }

    void setWheel(TimeLoginActState.Wheel wheel) {
        this.wheel = wheel;
        invalidate();
    }

    /** 服务端已确定中奖格后启动旋转。 */
    void startRoll(int sliceIndex) {
        targetSliceIndex = sliceIndex;
        rollStartUptimeMs = SystemClock.elapsedRealtime();
        rolling = true;
        invalidate();
    }

    boolean rolling() {
        return rolling;
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
        drawScene(canvas);
        canvas.restore();
        if (rolling) {
            postInvalidateOnAnimation();
        }
    }

    private void drawScene(Canvas canvas) {
        float elapsed = (SystemClock.elapsedRealtime() - rollStartUptimeMs) / 1000f;
        float pointerDegrees = 0f;
        float ringDegrees = 0f;
        if (targetSliceIndex >= 0) {
            pointerDegrees =
                    TimeLoginWheelLayout.rollDegrees(
                            targetSliceIndex,
                            elapsed,
                            TimeLoginWheelLayout.ROLL_DURATION_SECONDS);
            ringDegrees =
                    TimeLoginWheelLayout.rollDegrees(
                            targetSliceIndex,
                            elapsed,
                            TimeLoginWheelLayout.RING_ROLL_DURATION_SECONDS);
            if (rolling && elapsed >= TimeLoginWheelLayout.ROLL_DURATION_SECONDS) {
                rolling = false;
            }
        }
        drawRotated(canvas, ring, TimeLoginWheelLayout.RING, ringDegrees);
        drawBitmap(canvas, board, TimeLoginWheelLayout.BOARD);
        drawSlices(canvas, pointerDegrees);
        boolean unlocked = wheel != null && wheel.unlocked();
        drawRotated(
                canvas,
                unlocked || rolling ? pointer : pointerLocked,
                unlocked || rolling ? TimeLoginWheelLayout.POINTER : TimeLoginWheelLayout.POINTER_LOCKED,
                pointerDegrees);
        drawBitmap(canvas, rollText, TimeLoginWheelLayout.ROLL_TEXT);
        drawText(
                canvas,
                "剩余" + (unlocked && !rolling ? 1 : 0) + "次",
                TimeLoginWheelLayout.ROLL_TIPS_CENTER_X,
                TimeLoginWheelLayout.ROLL_TIPS_CENTER_Y,
                TimeLoginWheelLayout.ROLL_TIPS_FONT_SIZE,
                0xFFFFFFFF,
                Paint.Align.CENTER);
        drawBitmap(canvas, close, TimeLoginWheelLayout.CLOSE);
    }

    private void drawSlices(Canvas canvas, float pointerDegrees) {
        if (wheel == null) {
            return;
        }
        List<TimeLoginActState.Reward> props = wheel.props();
        int highlighted = highlightedSlice(pointerDegrees);
        for (int index = 0; index < TimeLoginWheelLayout.SLICE_COUNT; index++) {
            if (index == highlighted) {
                drawBitmap(canvas, selectGlow, TimeLoginWheelLayout.selectGlow(index));
            }
            if (index >= props.size()) {
                continue;
            }
            TimeLoginActState.Reward reward = props.get(index);
            if ("COIN".equals(reward.propId())) {
                // 非金币奖励在归档里没有对应帧，原版走远端图；未接入前不画占位图。
                drawBitmap(canvas, coinIcon, TimeLoginWheelLayout.rewardIcon(index));
            }
            drawRewardText(canvas, index, reward);
        }
    }

    private int highlightedSlice(float pointerDegrees) {
        if (targetSliceIndex < 0) {
            return -1;
        }
        return rolling
                ? TimeLoginWheelLayout.highlightedSlice(pointerDegrees)
                : targetSliceIndex % TimeLoginWheelLayout.SLICE_COUNT;
    }

    private void drawRewardText(Canvas canvas, int index, TimeLoginActState.Reward reward) {
        // WheelView.lua:94-98：金币只写数量，其他道具写「数量x」。
        String amount =
                "COIN".equals(reward.propId())
                        ? Long.toString(reward.propCnt())
                        : reward.propCnt() + "x";
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(TimeLoginWheelLayout.REWARD_COUNT_FONT_SIZE);
        float amountWidth = textPaint.measureText(amount);
        textPaint.setTextSize(TimeLoginWheelLayout.REWARD_NAME_FONT_SIZE);
        float nameWidth = textPaint.measureText(reward.name());
        float left = TimeLoginWheelLayout.rewardTextLeft(index, amountWidth + nameWidth);
        float centerY = TimeLoginWheelLayout.rewardTextCenterY(index);
        drawText(
                canvas,
                amount,
                left,
                centerY,
                TimeLoginWheelLayout.REWARD_COUNT_FONT_SIZE,
                TimeLoginWheelLayout.REWARD_TEXT_COLOR,
                Paint.Align.LEFT);
        drawText(
                canvas,
                reward.name(),
                left + amountWidth,
                centerY,
                TimeLoginWheelLayout.REWARD_NAME_FONT_SIZE,
                TimeLoginWheelLayout.REWARD_TEXT_COLOR,
                Paint.Align.LEFT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return false;
        }
        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return true;
        }
        TimeLoginActLayout.Transform transform =
                TimeLoginActLayout.Transform.contain(getWidth(), getHeight());
        float x = transform.designX(event.getX());
        float y = transform.designY(event.getY());
        performClick();
        if (rolling) {
            // WheelView.lua:213-217：旋转期间关闭按钮不响应。
            return true;
        }
        if (TimeLoginWheelLayout.CLOSE.contains(x, y)) {
            actions.onClose();
        } else if (TimeLoginWheelLayout.POINTER.contains(x, y)) {
            actions.onDraw();
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void drawRotated(Canvas canvas, Bitmap bitmap, Box box, float degrees) {
        canvas.save();
        canvas.rotate(
                degrees, TimeLoginWheelLayout.POINTER_PIVOT_X, TimeLoginWheelLayout.POINTER_PIVOT_Y);
        drawBitmap(canvas, bitmap, box);
        canvas.restore();
    }

    private void drawText(
            Canvas canvas,
            String text,
            float x,
            float centerY,
            float size,
            int color,
            Paint.Align align) {
        if (text == null || text.isEmpty()) {
            return;
        }
        textPaint.setTextAlign(align);
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(text, x, centerY - (metrics.ascent + metrics.descent) * 0.5f, textPaint);
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, Box box) {
        if (bitmap == null) {
            return;
        }
        source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        destination.set(box.left(), box.top(), box.right(), box.bottom());
        canvas.drawBitmap(bitmap, source, destination, bitmapPaint);
    }

    private static Bitmap load(Resources resources, int resourceId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return BitmapFactory.decodeResource(resources, resourceId, options);
    }
}
