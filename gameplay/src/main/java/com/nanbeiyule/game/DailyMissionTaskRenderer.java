package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import java.time.Duration;
import java.time.Instant;

final class DailyMissionTaskRenderer {
    /** 每个任务条的 KW_PANEL_ITEMS 各有独立的横向滚动量。 */
    interface RewardScroll {
        float at(int taskIndex);

        RewardScroll NONE = taskIndex -> 0f;
    }

    // 字号与颜色取自 CSB 的 TextOptions.fontSize 与 WidgetOptions.color。
    private static final float TASK_TEXT_SIZE = 40f;
    private static final float TASK_TEXT_CENTER_Y = 57.46f;
    private static final float TASK_NAME_LEFT = 45.7071f;
    private static final float TASK_TARGET_RIGHT = 683f;
    private static final float TASK_NAME_MAX_WIDTH = 530f;
    private static final float TASK_NAME_MIN_SIZE = 5f;
    private static final int TASK_NAME_COLOR = Color.rgb(131, 41, 1);
    private static final int TASK_PROGRESS_COLOR = Color.rgb(223, 80, 31);
    private static final float TASK_LEFT_TEXT_SIZE = 26f;
    private static final int TASK_LEFT_TEXT_COLOR = Color.rgb(153, 153, 153);
    private static final float REWARD_COUNT_SIZE = 36f;
    private static final float REWARD_COUNT_RIGHT = 135f;
    private static final float REWARD_COUNT_CENTER_Y = 115f;
    private static final float BUTTON_TEXT_SIZE = 48f;
    private static final float BUTTON_TEXT_CENTER_Y = 50.3f;
    private static final int BUTTON_GET_COLOR = Color.rgb(166, 74, 0);
    private static final int BUTTON_GO_COLOR = Color.rgb(35, 102, 159);
    private static final int BUTTON_FINISH_COLOR = Color.rgb(93, 93, 93);

    /** 原版按下态：setScale(0.9) + setColor(160,160,160)。 */
    private static final float PRESSED_SCALE = 0.9f;
    private static final float PRESSED_TINT = 160f / 255f;

    private final DailyMissionDrawableSet drawables;
    private final DailyMissionEffects effects;
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final ColorMatrixColorFilter pressedFilter = new ColorMatrixColorFilter(
            new ColorMatrix(new float[] {
                    PRESSED_TINT, 0f, 0f, 0f, 0f,
                    0f, PRESSED_TINT, 0f, 0f, 0f,
                    0f, 0f, PRESSED_TINT, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f}));
    private final Rect source = new Rect();

    DailyMissionTaskRenderer(
            DailyMissionDrawableSet drawables,
            DailyMissionEffects effects,
            Typeface typeface) {
        this.drawables = drawables;
        this.effects = effects;
        textPaint.setTypeface(typeface);
    }

    void drawTasks(
            Canvas canvas,
            DailyMissionState state,
            float scroll,
            RewardScroll rewardScroll,
            int pressedTaskIndex,
            float elapsedSeconds,
            Instant now,
            boolean hasStages) {
        RewardScroll rewards = rewardScroll == null ? RewardScroll.NONE : rewardScroll;
        int count = state.tasks().size();
        for (int index = 0; index < count; index++) {
            DailyMissionLayout.Box box =
                    DailyMissionLayout.taskRect(index, count, scroll, hasStages);
            DailyMissionLayout.Box viewport = DailyMissionLayout.taskViewport(hasStages);
            if (box.bottom() < viewport.top() || box.top() > viewport.bottom()) {
                continue;
            }
            drawTask(
                    canvas,
                    state.tasks().get(index),
                    box,
                    index,
                    count,
                    scroll,
                    hasStages,
                    rewards.at(index),
                    index == pressedTaskIndex,
                    elapsedSeconds,
                    now,
                    state.page().expiresAt());
        }
    }

    private void drawTask(
            Canvas canvas,
            DailyMissionState.Task task,
            DailyMissionLayout.Box box,
            int index,
            int count,
            float scroll,
            boolean hasStages,
            float rewardScroll,
            boolean pressed,
            float elapsedSeconds,
            Instant now,
            Instant pageEndsAt) {
        drawBitmap(canvas, drawables.taskComplete, box);
        if (task.state() == DailyMissionState.TaskState.IN_PROGRESS) {
            drawBitmap(canvas, drawables.taskInProgress, box);
        }
        drawLimitedTime(canvas, task, box, now, pageEndsAt);
        drawTaskText(canvas, task, box);
        drawTaskProgress(canvas, task, box);
        drawRewards(canvas, task, box, rewardScroll);
        drawButton(
                canvas,
                task,
                DailyMissionLayout.taskButtonRect(index, count, scroll, hasStages),
                pressed,
                elapsedSeconds);
    }

    /**
     * View.lua:368-370、497-499：任务自己的 endTime 与所在页签 endTime 不同才是限时任务，
     * 这时显示 KW_LEFT_TIME 角标与 KW_LEFT 倒计时；尚未开始的改写成「距离开始:」。
     */
    private void drawLimitedTime(
            Canvas canvas,
            DailyMissionState.Task task,
            DailyMissionLayout.Box box,
            Instant now,
            Instant pageEndsAt) {
        Instant endsAt = task.endsAt();
        if (endsAt == null || pageEndsAt == null || endsAt.equals(pageEndsAt)) return;
        drawBitmap(canvas, drawables.timerTip, new RectF(
                box.left() + DailyMissionLayout.TASK_LIMIT_BADGE_LEFT,
                box.top() + DailyMissionLayout.TASK_LIMIT_BADGE_TOP,
                box.left() + DailyMissionLayout.TASK_LIMIT_BADGE_LEFT
                        + DailyMissionLayout.TASK_LIMIT_BADGE_WIDTH,
                box.top() + DailyMissionLayout.TASK_LIMIT_BADGE_TOP
                        + DailyMissionLayout.TASK_LIMIT_BADGE_HEIGHT));
        Instant startsAt = task.startsAt();
        boolean pending = startsAt != null && !startsAt.isBefore(now);
        Instant target = pending ? startsAt : endsAt;
        String label = (pending ? "距离开始:" : "剩余:")
                + DailyMissionState.formatCountdown(
                        Duration.between(now, target).getSeconds());
        configureText(TASK_LEFT_TEXT_SIZE, TASK_LEFT_TEXT_COLOR, Paint.Align.CENTER);
        canvas.drawText(
                label,
                box.left() + DailyMissionLayout.TASK_LEFT_TEXT_X,
                baselineFor(box.top() + DailyMissionLayout.TASK_LEFT_TEXT_CENTER_Y),
                textPaint);
    }

    private void drawTaskText(
            Canvas canvas, DailyMissionState.Task task, DailyMissionLayout.Box box) {
        // KW_TASK_NAME pos(45.7071,126.54) anchor(0,0.5)，字号 40，色 rgb(131,41,1)
        configureText(TASK_TEXT_SIZE, TASK_NAME_COLOR, Paint.Align.LEFT);
        // View.lua:372 XH.UITool.adaptTextToWidth(KW_TASK_NAME, 530, 40, 5)：
        // 从 40 起逐 1 缩字号直到量得宽度不超过 530，最小 6（UITool 的循环条件是 > minFontSize）。
        float nameSize = TASK_TEXT_SIZE;
        while (nameSize > TASK_NAME_MIN_SIZE
                && textPaint.measureText(task.title()) > TASK_NAME_MAX_WIDTH) {
            nameSize -= 1f;
            textPaint.setTextSize(nameSize);
        }
        float nameBaseline = baselineFor(box.top() + TASK_TEXT_CENTER_Y);
        canvas.drawText(task.title(), box.left() + TASK_NAME_LEFT, nameBaseline, textPaint);

        // KW_TASK_TARGET anchor(1,0.5) 固定右端；View.lua:375 把 KW_TASK_CUR 顶到它左边。
        String target = "/" + task.target();
        configureText(TASK_TEXT_SIZE, TASK_NAME_COLOR, Paint.Align.RIGHT);
        float targetRight = box.left() + TASK_TARGET_RIGHT;
        canvas.drawText(target, targetRight, nameBaseline, textPaint);
        float targetWidth = textPaint.measureText(target);

        configureText(TASK_TEXT_SIZE, TASK_PROGRESS_COLOR, Paint.Align.RIGHT);
        canvas.drawText(
                Long.toString(task.progress()), targetRight - targetWidth, nameBaseline, textPaint);
    }

    private float baselineFor(float centerY) {
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        return centerY - (metrics.ascent + metrics.descent) * 0.5f;
    }

    private void drawTaskProgress(
            Canvas canvas, DailyMissionState.Task task, DailyMissionLayout.Box box) {
        RectF track = new RectF(
                box.left() + 50f,
                box.top() + 102f,
                box.left() + 683f,
                box.top() + 135f);
        drawBitmap(canvas, drawables.taskProgressTrack, track);
        float fraction = Math.min(1f, (float) task.progress() / Math.max(1f, task.target()));
        if (fraction > 0f) {
            canvas.save();
            canvas.clipRect(track.left, track.top, track.left + track.width() * fraction, track.bottom);
            drawBitmap(canvas, drawables.taskProgressFill, track);
            canvas.restore();
        }
    }

    private void drawRewards(
            Canvas canvas,
            DailyMissionState.Task task,
            DailyMissionLayout.Box box,
            float rewardScroll) {
        DailyMissionLayout.Box viewport = DailyMissionLayout.taskRewardViewport(box);
        float scroll = DailyMissionLayout.clampRewardScroll(
                rewardScroll, task.rewardCellCount());
        float top = box.top() + DailyMissionLayout.TASK_REWARD_CELL_TOP;
        canvas.save();
        canvas.clipRect(viewport.left(), viewport.top(), viewport.right(), viewport.bottom());
        int slot = 0;
        if (task.activityPoints() > 0) {
            drawRewardSlot(
                    canvas,
                    rewardSlotLeft(viewport, slot, scroll),
                    top,
                    drawables.activityIcon,
                    task.activityPoints());
            slot++;
        }
        for (DailyMissionState.Reward reward : task.rewards()) {
            drawRewardSlot(
                    canvas,
                    rewardSlotLeft(viewport, slot, scroll),
                    top,
                    drawables.rewardIcon(reward),
                    reward.quantity());
            slot++;
        }
        canvas.restore();
    }

    private static float rewardSlotLeft(
            DailyMissionLayout.Box viewport, int slot, float scroll) {
        return viewport.left() + slot * DailyMissionLayout.TASK_REWARD_CELL - scroll;
    }

    private void drawRewardSlot(
            Canvas canvas, float left, float top, Bitmap icon, long quantity) {
        RectF cell = new RectF(left, top, left + 150f, top + 150f);
        drawBitmap(canvas, drawables.rewardCell, cell);
        if (icon != null) {
            float width = icon == drawables.activityIcon ? 100f : 104f;
            float height = icon == drawables.activityIcon ? 118f : 96f;
            RectF iconBounds = new RectF(
                    cell.centerX() - width * 0.5f,
                    cell.top + 8f,
                    cell.centerX() + width * 0.5f,
                    cell.top + 8f + height);
            drawBitmap(canvas, icon, iconBounds);
        }
        configureText(REWARD_COUNT_SIZE, DailyMissionRenderer.TEXT_OUTLINE_COLOR,
                Paint.Align.RIGHT);
        float countRight = cell.left + REWARD_COUNT_RIGHT;
        float countBaseline = baselineFor(cell.top + REWARD_COUNT_CENTER_Y);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(DailyMissionRenderer.TEXT_OUTLINE_WIDTH);
        canvas.drawText("x" + quantity, countRight, countBaseline, textPaint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.WHITE);
        canvas.drawText("x" + quantity, countRight, countBaseline, textPaint);
    }

    private void drawButton(
            Canvas canvas,
            DailyMissionState.Task task,
            DailyMissionLayout.Box box,
            boolean pressed,
            float elapsedSeconds) {
        Bitmap bitmap;
        String label;
        int labelColor;
        switch (task.state()) {
            case CLAIMABLE -> {
                bitmap = drawables.buttonYellow;
                label = "立即领取";
                labelColor = BUTTON_GET_COLOR;
            }
            case IN_PROGRESS -> {
                bitmap = drawables.buttonBlue;
                label = "去完成";
                labelColor = BUTTON_GO_COLOR;
            }
            case CLAIMED -> {
                bitmap = drawables.buttonDisabled;
                label = "已领取";
                labelColor = BUTTON_FINISH_COLOR;
            }
            default -> throw new IllegalStateException("unknown task state");
        }
        if (pressed) {
            canvas.save();
            canvas.scale(PRESSED_SCALE, PRESSED_SCALE, box.centerX(), box.centerY());
            bitmapPaint.setColorFilter(pressedFilter);
            textPaint.setColorFilter(pressedFilter);
        }
        drawBitmap(canvas, bitmap, box);
        // Text_4/5/6 pos(123.5,51.7) anchor(0.5,0.5) 于 247x94 按钮内 -> 中心下移 3.3
        configureText(BUTTON_TEXT_SIZE, labelColor, Paint.Align.CENTER);
        canvas.drawText(
                label,
                box.centerX(),
                baselineFor(box.top() + BUTTON_TEXT_CENTER_Y),
                textPaint);
        // View.lua:378 只有 KW_BTN_GET 挂扫光。
        if (task.state() == DailyMissionState.TaskState.CLAIMABLE) {
            effects.draw(
                    canvas,
                    DailyMissionEffects.BUTTON_SWEEP,
                    elapsedSeconds,
                    box.centerX(),
                    box.centerY() + DailyMissionLayout.EFFECT_BUTTON_OFFSET_Y,
                    DailyMissionLayout.EFFECT_BUTTON_SCALE_X,
                    DailyMissionLayout.EFFECT_BUTTON_SCALE_Y);
        }
        if (pressed) {
            bitmapPaint.setColorFilter(null);
            textPaint.setColorFilter(null);
            canvas.restore();
        }
    }

    private void configureText(float size, int color, Paint.Align align) {
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        textPaint.setTextAlign(align);
        textPaint.setStyle(Paint.Style.FILL);
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, DailyMissionLayout.Box box) {
        drawBitmap(canvas, bitmap, new RectF(box.left(), box.top(), box.right(), box.bottom()));
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, RectF destination) {
        source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawBitmap(bitmap, source, destination, bitmapPaint);
    }
}
