package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import com.nanbeiyule.game.TimeLoginActLayout.Box;
import java.util.List;

/**
 * 按 TimeLoginActLayer.csb 的 1920x1080 坐标绘制定时登录有礼。
 * 只画服务端下发的状态，不在本地推导领取资格或伪造进度。
 */
final class TimeLoginActRenderer {
    private final TimeLoginActDrawables drawables;
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();

    TimeLoginActRenderer(TimeLoginActDrawables drawables) {
        this.drawables = drawables;
        textPaint.setTypeface(drawables.textFace());
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    void draw(
            Canvas canvas,
            TimeLoginActState state,
            long elapsedSeconds,
            int pressedSlotIndex,
            boolean wheelPressed,
            boolean loading,
            String errorMessage) {
        drawBitmap(canvas, drawables.panel, TimeLoginActLayout.PANEL_BOX);
        drawBitmap(canvas, drawables.title, TimeLoginActLayout.TITLE);
        drawBitmap(canvas, drawables.freshTips, TimeLoginActLayout.FRESH_TIPS);
        drawBitmap(canvas, drawables.close, TimeLoginActLayout.CLOSE);
        if (state == null) {
            drawCentre(canvas, loading ? "加载中…" : errorMessage, 0xFFFFFFFF, 40f);
            return;
        }
        drawSlots(canvas, state, elapsedSeconds, pressedSlotIndex);
        drawWheel(canvas, state, wheelPressed);
        drawText(
                canvas,
                state.goldOverTipsText(),
                TimeLoginActLayout.GOLD_OVER_TIPS_CENTER_X,
                TimeLoginActLayout.GOLD_OVER_TIPS_CENTER_Y,
                TimeLoginActLayout.GOLD_OVER_TIPS_FONT_SIZE,
                TimeLoginActLayout.GOLD_OVER_TIPS_COLOR);
        if (loading) {
            drawCentre(canvas, "处理中…", 0xFFFFFFFF, 40f);
        } else if (errorMessage != null && !errorMessage.isEmpty()) {
            drawCentre(canvas, errorMessage, 0xFFFF6666, 36f);
        }
    }

    private void drawSlots(
            Canvas canvas, TimeLoginActState state, long elapsedSeconds, int pressedSlotIndex) {
        List<TimeLoginActState.Slot> slots = state.slots();
        int countdownIndex = state.countdownSlotIndex();
        Box viewport = TimeLoginActLayout.SLOT_VIEWPORT;
        canvas.save();
        canvas.clipRect(viewport.left(), viewport.top(), viewport.right(), viewport.bottom());
        for (int index = 0; index < slots.size(); index++) {
            float dx = viewport.left() + index * TimeLoginActLayout.SLOT_WIDTH;
            float dy = viewport.top();
            canvas.save();
            canvas.translate(dx, dy);
            drawSlot(
                    canvas,
                    slots.get(index),
                    index == countdownIndex,
                    state.remainingSeconds(countdownIndex, elapsedSeconds),
                    index == pressedSlotIndex);
            canvas.restore();
        }
        canvas.restore();
    }

    private void drawSlot(
            Canvas canvas,
            TimeLoginActState.Slot slot,
            boolean showsCountdown,
            long remainingSeconds,
            boolean pressed) {
        int band = slot.timeBand();
        drawBitmap(
                canvas, drawables.slotBackground(band), TimeLoginActLayout.SLOT_BACKGROUND);
        drawBitmap(canvas, drawables.coinStack, TimeLoginActLayout.SLOT_COIN_STACK);
        if (slot.claimable()) {
            // 原版 View.lua:255-257：可领取时才亮金边并放出领取按钮。
            drawBitmap(canvas, drawables.slotLight, TimeLoginActLayout.SLOT_LIGHT);
        }
        int color = TimeLoginActLayout.SLOT_TIME_COLORS[band];
        textPaint.setTextAlign(Paint.Align.LEFT);
        drawText(
                canvas,
                slot.timeRangeText(),
                TimeLoginActLayout.SLOT_TIME_LEFT,
                TimeLoginActLayout.SLOT_TIME_CENTER_Y,
                TimeLoginActLayout.SLOT_TIME_FONT_SIZE,
                color);
        textPaint.setTextAlign(Paint.Align.CENTER);
        drawText(
                canvas,
                "x" + slot.rewardAmount(),
                TimeLoginActLayout.SLOT_COUNT_CENTER_X,
                TimeLoginActLayout.SLOT_COUNT_CENTER_Y,
                TimeLoginActLayout.SLOT_COUNT_FONT_SIZE,
                color);
        if (slot.rewarded()) {
            drawBitmap(canvas, drawables.slotClaimed, TimeLoginActLayout.SLOT_CLAIMED_STAMP);
        }
        if (slot.claimable()) {
            drawClaimButton(canvas, pressed);
        } else if (showsCountdown) {
            drawCountdown(canvas, remainingSeconds);
        } else {
            drawText(
                    canvas,
                    slot.stateText(),
                    TimeLoginActLayout.SLOT_STATE_CENTER_X,
                    TimeLoginActLayout.SLOT_STATE_CENTER_Y,
                    TimeLoginActLayout.SLOT_STATE_FONT_SIZE,
                    TimeLoginActLayout.SLOT_STATE_COLOR);
        }
    }

    private void drawClaimButton(Canvas canvas, boolean pressed) {
        bitmapPaint.setAlpha(pressed ? 200 : 255);
        drawBitmap(canvas, drawables.claimButton, TimeLoginActLayout.SLOT_CLAIM_BUTTON);
        bitmapPaint.setAlpha(255);
        drawText(
                canvas,
                "领 取",
                TimeLoginActLayout.SLOT_CLAIM_TEXT_CENTER_X,
                TimeLoginActLayout.SLOT_CLAIM_TEXT_CENTER_Y,
                TimeLoginActLayout.SLOT_CLAIM_TEXT_FONT_SIZE,
                0xFFFFFFFF);
    }

    private void drawCountdown(Canvas canvas, long remainingSeconds) {
        drawText(
                canvas,
                TimeLoginActState.formatCountdown(remainingSeconds),
                TimeLoginActLayout.SLOT_COUNTDOWN_CENTER_X,
                TimeLoginActLayout.SLOT_COUNTDOWN_CENTER_Y,
                TimeLoginActLayout.SLOT_COUNTDOWN_FONT_SIZE,
                TimeLoginActLayout.SLOT_COUNTDOWN_COLOR);
        drawText(
                canvas,
                "后领取",
                TimeLoginActLayout.SLOT_COUNTDOWN_SUFFIX_CENTER_X,
                TimeLoginActLayout.SLOT_COUNTDOWN_SUFFIX_CENTER_Y,
                TimeLoginActLayout.SLOT_COUNTDOWN_SUFFIX_FONT_SIZE,
                TimeLoginActLayout.SLOT_STATE_COLOR);
    }

    private void drawWheel(Canvas canvas, TimeLoginActState state, boolean pressed) {
        TimeLoginActState.Wheel wheel = state.wheel();
        if (wheel == null) {
            return;
        }
        drawBitmap(canvas, drawables.wheelPanel, TimeLoginActLayout.WHEEL_PANEL);
        drawBitmap(canvas, drawables.wheelBottom, TimeLoginActLayout.WHEEL_BOTTOM);
        drawBitmap(canvas, drawables.wheelOverlay, TimeLoginActLayout.WHEEL_OVERLAY);
        drawables
                .maxRewardFont()
                .drawCentered(
                        canvas,
                        state.maxRewardText(),
                        TimeLoginActLayout.MAX_REWARD_CENTER_X,
                        TimeLoginActLayout.MAX_REWARD_CENTER_Y,
                        TimeLoginActLayout.MAX_REWARD_SCALE);
        if (wheel.unlocked()) {
            // 原版 View.lua:285-289：达标后按钮出现，三格进度隐藏。
            bitmapPaint.setAlpha(pressed ? 200 : 255);
            drawBitmap(canvas, drawables.wheelButton, TimeLoginActLayout.WHEEL_BUTTON);
            bitmapPaint.setAlpha(255);
            drawText(
                    canvas,
                    "抽 奖",
                    TimeLoginActLayout.WHEEL_BUTTON_TEXT_CENTER_X,
                    TimeLoginActLayout.WHEEL_BUTTON_TEXT_CENTER_Y,
                    TimeLoginActLayout.WHEEL_BUTTON_TEXT_FONT_SIZE,
                    0xFFFFFFFF);
            return;
        }
        for (int index = 0; index < 3; index++) {
            Bitmap bar = wheel.currentCount() >= index + 1
                    ? drawables.progressOn
                    : drawables.progressOff;
            drawBitmap(canvas, bar, TimeLoginActLayout.progressBox(index));
        }
        drawables
                .wheelTipsFont()
                .drawCentered(
                        canvas,
                        "领取 次登录奖励开启",
                        TimeLoginActLayout.WHEEL_TIPS_BOX.centerX(),
                        TimeLoginActLayout.WHEEL_TIPS_BOX.centerY());
        // Txt_dlyl_3_3：提示语里那个数字是独立字图，不走 BMFont。
        drawBitmap(canvas, drawables.tipsDigit, TimeLoginActLayout.WHEEL_TIPS_DIGIT);
    }

    private void drawCentre(Canvas canvas, String text, int color, float size) {
        if (text == null || text.isEmpty()) {
            return;
        }
        textPaint.setTextAlign(Paint.Align.CENTER);
        drawText(
                canvas,
                text,
                TimeLoginActLayout.DESIGN_WIDTH * 0.5f,
                TimeLoginActLayout.DESIGN_HEIGHT * 0.5f,
                size,
                color);
    }

    private void drawText(
            Canvas canvas, String text, float x, float centerY, float size, int color) {
        if (text == null || text.isEmpty()) {
            return;
        }
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseline = centerY - (metrics.ascent + metrics.descent) * 0.5f;
        canvas.drawText(text, x, baseline, textPaint);
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, Box box) {
        if (bitmap == null) {
            return;
        }
        source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        destination.set(box.left(), box.top(), box.right(), box.bottom());
        canvas.drawBitmap(bitmap, source, destination, bitmapPaint);
    }

    /** 供渲染契约测试直接调用，避免测试重复实现命中几何。 */
    static int slotIndexAt(int slotCount, float x, float y) {
        Box viewport = TimeLoginActLayout.SLOT_VIEWPORT;
        if (!viewport.contains(x, y)) {
            return -1;
        }
        int index = (int) ((x - viewport.left()) / TimeLoginActLayout.SLOT_WIDTH);
        return index >= 0 && index < slotCount ? index : -1;
    }

    static boolean claimButtonHit(int slotIndex, float x, float y) {
        if (slotIndex < 0) {
            return false;
        }
        Box viewport = TimeLoginActLayout.SLOT_VIEWPORT;
        float localX = x - viewport.left() - slotIndex * TimeLoginActLayout.SLOT_WIDTH;
        float localY = y - viewport.top();
        return TimeLoginActLayout.SLOT_CLAIM_BUTTON.contains(localX, localY);
    }

    static int backgroundColor() {
        return Color.argb(178, 3, 10, 31);
    }
}
