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

abstract class PersonalCenterActionRenderer extends PersonalCenterResourceRenderer {
    protected PersonalCenterActionRenderer(
            Context context,
            PersonalCenterState state,
            PersonalCenterSystemSettings systemSettings,
            Bitmap avatarBitmap,
            Listener listener) {
        super(context, state, systemSettings, avatarBitmap, listener);
    }
    protected void drawSecurityRow(
            Canvas canvas,
            float top,
            IconType icon,
            String title,
            String value,
            String button) {
        RectF row = new RectF(450.0f, top, 1830.0f, top + 88.0f);
        drawCard(canvas, row);
        drawIcon(
                canvas, icon, row.left + 63.0f, row.centerY(), 34.0f, GOLD_DARK);
        setText(27.0f, BROWN_TEXT, false, Paint.Align.LEFT);
        drawBaselineCenteredText(
                canvas, title, row.left + 120.0f, row.centerY());
        setText(24.0f, BROWN_TEXT, false, Paint.Align.RIGHT);
        drawBaselineCenteredText(
                canvas, value, row.right - 400.0f, row.centerY());
        drawSmallGoldButton(
                canvas,
                new RectF(
                        row.right - 280.0f,
                        row.centerY() - 25.0f,
                        row.right - 160.0f,
                        row.centerY() + 25.0f),
                button);
        drawChevron(canvas, row.right - 64.0f, row.centerY(), GOLD);
    }

    protected void drawPrivacyRow(
            Canvas canvas,
            float top,
            String title,
            String subtitle,
            boolean checked) {
        RectF row = new RectF(450.0f, top, 1830.0f, top + 100.0f);
        drawCard(canvas, row);
        setText(27.0f, BROWN_TEXT, false, Paint.Align.LEFT);
        drawBaselineCenteredText(
                canvas, title, row.left + 42.0f, row.top + 34.0f);
        setText(19.0f, MUTED_TEXT, false, Paint.Align.LEFT);
        drawBaselineCenteredText(
                canvas, subtitle, row.left + 42.0f, row.top + 72.0f);
        drawToggle(canvas, row.right - 82.0f, row.centerY(), checked);
    }

    protected void drawHelpRow(
            Canvas canvas,
            float top,
            IconType icon,
            String title,
            String subtitle) {
        RectF row = new RectF(450.0f, top, 1830.0f, top + 100.0f);
        drawCard(canvas, row);
        fillPaint.setColor(Color.rgb(250, 237, 206));
        canvas.drawCircle(row.left + 66.0f, row.centerY(), 34.0f, fillPaint);
        strokePaint.setStrokeWidth(1.5f);
        strokePaint.setColor(Color.rgb(223, 180, 94));
        canvas.drawCircle(row.left + 66.0f, row.centerY(), 34.0f, strokePaint);
        drawIcon(
                canvas,
                icon,
                row.left + 66.0f,
                row.centerY(),
                46.0f,
                GOLD_DARK);
        setText(27.0f, BROWN_TEXT, false, Paint.Align.LEFT);
        drawBaselineCenteredText(
                canvas, title, row.left + 120.0f, row.top + 34.0f);
        setText(19.0f, MUTED_TEXT, false, Paint.Align.LEFT);
        drawBaselineCenteredText(
                canvas, subtitle, row.left + 120.0f, row.top + 72.0f);
        drawChevron(canvas, row.right - 64.0f, row.centerY(), GOLD);
    }

    protected void drawSettingLabel(
            Canvas canvas,
            IconType icon,
            String title,
            float x,
            float centerY) {
        drawIcon(canvas, icon, x, centerY, 30.0f, GOLD_DARK);
        setText(25.0f, BROWN_TEXT, false, Paint.Align.LEFT);
        drawBaselineCenteredText(canvas, title, x + 42.0f, centerY);
    }

    protected void drawChoiceChips(
            Canvas canvas,
            String[] choices,
            float left,
            float top,
            float width,
            int selected) {
        for (int index = 0; index < choices.length; index++) {
            RectF chip =
                    new RectF(
                            left + width * index,
                            top,
                            left + width * (index + 1),
                            top + 48.0f);
            fillPaint.setColor(
                    index == selected
                            ? Color.rgb(232, 191, 107)
                            : Color.rgb(246, 237, 218));
            canvas.drawRoundRect(chip, 8.0f, 8.0f, fillPaint);
            strokePaint.setStrokeWidth(1.0f);
            strokePaint.setColor(Color.rgb(226, 202, 155));
            canvas.drawRoundRect(chip, 8.0f, 8.0f, strokePaint);
            setText(
                    21.0f,
                    index == selected ? BROWN_TEXT : MUTED_TEXT,
                    index == selected,
                    Paint.Align.CENTER);
            drawBaselineCenteredText(
                    canvas, choices[index], chip.centerX(), chip.centerY());
        }
    }

    protected void drawBottomTool(
            Canvas canvas,
            float centerX,
            float centerY,
            IconType icon,
            String label) {
        fillPaint.setColor(Color.rgb(250, 238, 209));
        canvas.drawCircle(centerX, centerY - 22.0f, 44.0f, fillPaint);
        strokePaint.setStrokeWidth(1.5f);
        strokePaint.setColor(Color.rgb(224, 185, 108));
        canvas.drawCircle(centerX, centerY - 22.0f, 44.0f, strokePaint);
        drawIcon(
                canvas,
                icon,
                centerX,
                centerY - 22.0f,
                38.0f,
                GOLD_DARK);
        setText(21.0f, BROWN_TEXT, false, Paint.Align.CENTER);
        drawBaselineCenteredText(
                canvas, label, centerX, centerY + 50.0f);
    }
}
