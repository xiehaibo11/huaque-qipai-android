package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import java.util.List;

/** Draws the two reward panels cloned from _KW_PANEL1/_KW_PANEL2. */
final class MembershipDailyGiftOptionRenderer {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();
    private final Typeface typeface;

    MembershipDailyGiftOptionRenderer(Typeface typeface) {
        this.typeface = typeface == null ? Typeface.DEFAULT_BOLD : typeface;
    }

    void drawGiftPanel(
            Canvas canvas,
            MembershipDailyGiftState.Option option,
            RectF panel,
            Bitmap panelBitmap,
            Bitmap rewardCellBitmap,
            MembershipDailyGiftRewardIconSet iconSet) {
        drawBitmap(canvas, panelBitmap, panel);
        List<MembershipDailyGiftState.Reward> rewards = option.rewards();
        for (int index = 0; index < Math.min(4, rewards.size()); index++) {
            int row = index / 2;
            int column = index % 2;
            RectF cell = new RectF(
                    panel.left + 60.0f + column * 220.0f,
                    panel.top + 72.0f + row * 238.0f,
                    panel.left + 254.0f + column * 220.0f,
                    panel.top + 259.0f + row * 238.0f);
            drawReward(canvas, rewards.get(index), cell, rewardCellBitmap, iconSet);
        }
    }

    private void drawReward(
            Canvas canvas,
            MembershipDailyGiftState.Reward reward,
            RectF cell,
            Bitmap rewardCellBitmap,
            MembershipDailyGiftRewardIconSet iconSet) {
        drawBitmap(canvas, rewardCellBitmap, cell);
        drawText(canvas, reward.displayName(), cell.centerX(), cell.top + 45.0f, 30.0f,
                Color.rgb(126, 74, 35), Paint.Align.CENTER);
        drawBitmap(canvas, iconSet.iconFor(reward),
                new RectF(cell.centerX() - 54.0f, cell.top + 58.0f,
                        cell.centerX() + 54.0f, cell.top + 150.0f));
        drawText(canvas, "x" + reward.quantity(), cell.centerX(), cell.top + 170.0f, 28.0f,
                Color.rgb(218, 78, 52), Paint.Align.CENTER);
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, RectF bounds) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        destination.set(bounds);
        paint.setShader(null);
        paint.setAlpha(255);
        canvas.drawBitmap(bitmap, source, destination, paint);
    }

    private void drawText(Canvas canvas, String text, float x, float baseline, float size, int color, Paint.Align align) {
        textPaint.setTypeface(typeface);
        textPaint.setTextSize(size);
        textPaint.setTextAlign(align);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(color);
        textPaint.clearShadowLayer();
        canvas.drawText(text, x, baseline, textPaint);
    }
}
