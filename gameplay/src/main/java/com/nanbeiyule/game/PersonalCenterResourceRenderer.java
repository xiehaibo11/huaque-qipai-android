package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.MotionEvent;
import java.util.EnumMap;
import java.util.List;

/** Dark-navy, champagne-gold personal center backed by authenticated first-party state. */

abstract class PersonalCenterResourceRenderer extends PersonalCenterControlRenderer {
    protected PersonalCenterResourceRenderer(
            Context context,
            PersonalCenterState state,
            PersonalCenterSystemSettings systemSettings,
            Bitmap avatarBitmap,
            Listener listener) {
        super(context, state, systemSettings, avatarBitmap, listener);
    }
    protected void drawCircularAvatar(Canvas canvas, RectF bounds) {
        float radius = Math.min(bounds.width(), bounds.height()) / 2.0f;
        float cx = bounds.centerX();
        float cy = bounds.centerY();
        fillPaint.setColor(Color.rgb(246, 225, 174));
        canvas.drawCircle(cx, cy, radius + 8.0f, fillPaint);
        strokePaint.setStrokeWidth(4.0f);
        strokePaint.setColor(Color.rgb(202, 146, 51));
        canvas.drawCircle(cx, cy, radius + 4.0f, strokePaint);
        int save = canvas.save();
        reusablePath.reset();
        reusablePath.addCircle(cx, cy, radius - 6.0f, Path.Direction.CW);
        canvas.clipPath(reusablePath);
        canvas.drawBitmap(avatarBitmap, null, bounds, bitmapPaint);
        canvas.restoreToCount(save);
        fillPaint.setShader(
                new LinearGradient(
                        cx,
                        bounds.bottom - 34.0f,
                        cx,
                        bounds.bottom + 14.0f,
                        new int[] {
                            Color.rgb(71, 130, 239),
                            Color.rgb(35, 68, 169)
                        },
                        null,
                        Shader.TileMode.CLAMP));
        canvas.drawCircle(cx, bounds.bottom - 4.0f, 31.0f, fillPaint);
        fillPaint.setShader(null);
        setText(22.0f, Color.WHITE, true, Paint.Align.CENTER);
        drawBaselineCenteredText(
                canvas,
                Integer.toString(state.player().membershipLevel()),
                cx,
                bounds.bottom - 4.0f);
    }

    protected void drawVipBadge(Canvas canvas, float centerX, float centerY) {
        drawCrown(canvas, centerX, centerY - 26.0f, 42.0f, GOLD);
        setText(42.0f, GOLD, true, Paint.Align.CENTER);
        drawBaselineCenteredText(
                canvas,
                "VIP" + state.player().membershipLevel(),
                centerX,
                centerY + 23.0f);
        setText(20.0f, BROWN_TEXT, false, Paint.Align.CENTER);
        drawBaselineCenteredText(
                canvas,
                "会员等级",
                centerX,
                centerY + 67.0f);
    }

    protected void drawRoomCardResourceRow(
            Canvas canvas,
            RectF row,
            String label,
            long value,
            boolean bound,
            boolean helpAction) {
        drawCard(canvas, row);
        float iconCenterX = row.left + 72.0f;
        float iconCenterY = row.centerY();
        if (roomCardIcon != null && !roomCardIcon.isRecycled()) {
            canvas.drawBitmap(
                    roomCardIcon,
                    null,
                    new RectF(
                            iconCenterX - 47.0f,
                            iconCenterY - 32.0f,
                            iconCenterX + 47.0f,
                            iconCenterY + 32.0f),
                    bitmapPaint);
        }
        if (bound) {
            float badgeX = iconCenterX + 37.0f;
            float badgeY = iconCenterY + 24.0f;
            fillPaint.setColor(Color.rgb(221, 71, 27));
            canvas.drawCircle(badgeX, badgeY, 20.0f, fillPaint);
            strokePaint.setStrokeWidth(2.0f);
            strokePaint.setColor(Color.rgb(255, 177, 72));
            canvas.drawCircle(badgeX, badgeY, 20.0f, strokePaint);
            setText(20.0f, Color.WHITE, true, Paint.Align.CENTER);
            drawBaselineCenteredText(canvas, "绑", badgeX, badgeY);
        }
        drawResourceLabelAndValue(canvas, row, label, value);
        drawResourceActionIcon(canvas, row, helpAction);
    }

    protected void drawDiamondResourceRow(
            Canvas canvas,
            RectF row,
            long value) {
        drawCard(canvas, row);
        float iconCenterX = row.left + 72.0f;
        if (diamondIcon != null && !diamondIcon.isRecycled()) {
            canvas.drawBitmap(
                    diamondIcon,
                    null,
                    new RectF(
                            iconCenterX - 39.0f,
                            row.centerY() - 39.0f,
                            iconCenterX + 39.0f,
                            row.centerY() + 39.0f),
                    bitmapPaint);
        }
        drawResourceLabelAndValue(canvas, row, "钻石", value);
        drawResourceActionIcon(canvas, row, false);
    }

    protected void drawResourceLabelAndValue(
            Canvas canvas,
            RectF row,
            String label,
            long value) {
        setText(31.0f, BROWN_TEXT, false, Paint.Align.RIGHT);
        drawBaselineCenteredText(
                canvas,
                label + "：",
                row.left + 350.0f,
                row.centerY());
        setText(33.0f, BROWN_TEXT, true, Paint.Align.LEFT);
        drawBaselineCenteredText(
                canvas,
                Long.toString(value),
                row.left + 366.0f,
                row.centerY());
    }

    protected void drawResourceActionIcon(
            Canvas canvas,
            RectF row,
            boolean helpAction) {
        float centerX = row.right - 56.0f;
        float centerY = row.centerY();
        if (helpAction) {
            fillPaint.setColor(Color.rgb(238, 86, 32));
            canvas.drawCircle(centerX, centerY, 38.0f, fillPaint);
            strokePaint.setStrokeWidth(2.0f);
            strokePaint.setColor(Color.rgb(255, 170, 72));
            canvas.drawCircle(centerX, centerY, 38.0f, strokePaint);
            setText(54.0f, Color.WHITE, true, Paint.Align.CENTER);
            drawBaselineCenteredText(canvas, "?", centerX, centerY);
            return;
        }
        if (addIcon != null && !addIcon.isRecycled()) {
            canvas.drawBitmap(
                    addIcon,
                    null,
                    new RectF(
                            centerX - 38.0f,
                            centerY - 38.0f,
                            centerX + 38.0f,
                            centerY + 38.0f),
                    bitmapPaint);
        }
    }

    protected void drawQuickAction(
            Canvas canvas,
            float left,
            float width,
            IconType icon,
            String label) {
        float centerX = left + width / 2.0f;
        if (left > 451.0f) {
            strokePaint.setStrokeWidth(1.0f);
            strokePaint.setColor(Color.argb(75, 199, 153, 82));
            canvas.drawLine(
                    left,
                    573.0f,
                    left,
                    686.0f,
                    strokePaint);
        }
        fillPaint.setColor(Color.rgb(250, 238, 209));
        canvas.drawCircle(centerX, 610.0f, 39.0f, fillPaint);
        strokePaint.setStrokeWidth(1.5f);
        strokePaint.setColor(Color.rgb(224, 185, 108));
        canvas.drawCircle(centerX, 610.0f, 39.0f, strokePaint);
        drawIcon(canvas, icon, centerX, 610.0f, 42.0f, GOLD_DARK);
        setText(22.0f, BROWN_TEXT, false, Paint.Align.CENTER);
        drawBaselineCenteredText(canvas, label, centerX, 670.0f);
    }
}
