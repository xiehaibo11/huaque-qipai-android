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

abstract class PersonalCenterControlRenderer extends PersonalCenterSidebarIconRenderer {
    protected PersonalCenterControlRenderer(
            Context context,
            PersonalCenterState state,
            PersonalCenterSystemSettings systemSettings,
            Bitmap avatarBitmap,
            Listener listener) {
        super(context, state, systemSettings, avatarBitmap, listener);
    }
    protected void drawCard(Canvas canvas, RectF bounds) {
        fillPaint.setShader(
                new LinearGradient(
                        bounds.left,
                        bounds.top,
                        bounds.right,
                        bounds.bottom,
                        new int[] {
                            CREAM_CARD,
                            Color.rgb(255, 248, 236)
                        },
                        null,
                        Shader.TileMode.CLAMP));
        canvas.drawRoundRect(bounds, 14.0f, 14.0f, fillPaint);
        fillPaint.setShader(null);
        strokePaint.setStrokeWidth(1.2f);
        strokePaint.setColor(Color.rgb(232, 211, 171));
        canvas.drawRoundRect(bounds, 14.0f, 14.0f, strokePaint);
        strokePaint.setStrokeWidth(1.0f);
        strokePaint.setColor(Color.argb(85, 255, 255, 255));
        canvas.drawLine(
                bounds.left + 12.0f,
                bounds.top + 3.0f,
                bounds.right - 12.0f,
                bounds.top + 3.0f,
                strokePaint);
    }

    protected void drawGoldButton(
            Canvas canvas,
            PersonalCenterLayout.Box box,
            String label) {
        drawMainButton(
                canvas,
                rect(box),
                label,
                new int[] {
                    Color.rgb(255, 223, 143),
                    Color.rgb(229, 165, 54),
                    Color.rgb(190, 114, 18)
                },
                Color.rgb(149, 86, 13));
    }

    protected void drawBlueButton(
            Canvas canvas,
            PersonalCenterLayout.Box box,
            String label) {
        drawMainButton(
                canvas,
                rect(box),
                label,
                new int[] {
                    Color.rgb(123, 174, 244),
                    Color.rgb(67, 108, 196),
                    Color.rgb(39, 58, 138)
                },
                Color.rgb(29, 43, 106));
    }

    protected void drawMainButton(
            Canvas canvas,
            RectF bounds,
            String label,
            int[] colors,
            int borderColor) {
        fillPaint.setShader(
                new LinearGradient(
                        bounds.left,
                        bounds.top,
                        bounds.left,
                        bounds.bottom,
                        colors,
                        null,
                        Shader.TileMode.CLAMP));
        canvas.drawRoundRect(bounds, 10.0f, 10.0f, fillPaint);
        fillPaint.setShader(null);
        strokePaint.setStrokeWidth(2.0f);
        strokePaint.setColor(borderColor);
        canvas.drawRoundRect(bounds, 10.0f, 10.0f, strokePaint);
        setText(30.0f, Color.WHITE, true, Paint.Align.CENTER);
        textPaint.setShadowLayer(2.0f, 0.0f, 1.5f, borderColor);
        drawBaselineCenteredText(
                canvas, label, bounds.centerX(), bounds.centerY());
        textPaint.clearShadowLayer();
    }

    protected void drawSmallGoldButton(
            Canvas canvas, RectF bounds, String label) {
        fillPaint.setShader(
                new LinearGradient(
                        bounds.left,
                        bounds.top,
                        bounds.left,
                        bounds.bottom,
                        new int[] {
                            Color.rgb(255, 238, 188),
                            Color.rgb(229, 183, 92)
                        },
                        null,
                        Shader.TileMode.CLAMP));
        canvas.drawRoundRect(bounds, 7.0f, 7.0f, fillPaint);
        fillPaint.setShader(null);
        strokePaint.setStrokeWidth(1.0f);
        strokePaint.setColor(Color.rgb(199, 147, 59));
        canvas.drawRoundRect(bounds, 7.0f, 7.0f, strokePaint);
        setText(20.0f, BROWN_TEXT, false, Paint.Align.CENTER);
        drawBaselineCenteredText(
                canvas, label, bounds.centerX(), bounds.centerY());
    }

    protected void drawTextLink(
            Canvas canvas,
            PersonalCenterLayout.Box box,
            String label) {
        setText(22.0f, BROWN_TEXT, false, Paint.Align.CENTER);
        drawBaselineCenteredText(
                canvas, label, box.centerX(), box.centerY());
        strokePaint.setStrokeWidth(1.0f);
        strokePaint.setColor(Color.rgb(182, 137, 78));
        canvas.drawLine(
                box.centerX() - 48.0f,
                box.centerY() + 18.0f,
                box.centerX() + 48.0f,
                box.centerY() + 18.0f,
                strokePaint);
    }

    protected void drawToggle(
            Canvas canvas, float centerX, float centerY, boolean checked) {
        RectF track =
                new RectF(
                        centerX - 38.0f,
                        centerY - 19.0f,
                        centerX + 38.0f,
                        centerY + 19.0f);
        fillPaint.setColor(
                checked ? Color.rgb(102, 188, 91) : Color.rgb(183, 188, 192));
        canvas.drawRoundRect(track, 19.0f, 19.0f, fillPaint);
        strokePaint.setStrokeWidth(1.0f);
        strokePaint.setColor(
                checked ? Color.rgb(69, 151, 61) : Color.rgb(145, 151, 155));
        canvas.drawRoundRect(track, 19.0f, 19.0f, strokePaint);
        float knobX = checked ? centerX + 19.0f : centerX - 19.0f;
        fillPaint.setColor(Color.WHITE);
        canvas.drawCircle(knobX, centerY, 16.0f, fillPaint);
        strokePaint.setColor(Color.argb(80, 60, 60, 60));
        canvas.drawCircle(knobX, centerY, 16.0f, strokePaint);
    }

    protected void drawSlider(
            Canvas canvas,
            RectF bounds,
            float progress,
            int activeColor) {
        float centerY = bounds.centerY();
        float startX = bounds.left + 10.0f;
        float endX = bounds.right - 10.0f;
        float activeX = startX + (endX - startX) * progress;
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeWidth(8.0f);
        strokePaint.setColor(Color.rgb(224, 215, 198));
        canvas.drawLine(startX, centerY, endX, centerY, strokePaint);
        strokePaint.setColor(activeColor);
        canvas.drawLine(startX, centerY, activeX, centerY, strokePaint);
        fillPaint.setColor(Color.rgb(255, 244, 213));
        canvas.drawCircle(activeX, centerY, 13.0f, fillPaint);
        strokePaint.setStrokeWidth(1.5f);
        strokePaint.setColor(Color.rgb(199, 153, 68));
        canvas.drawCircle(activeX, centerY, 13.0f, strokePaint);
        strokePaint.setStrokeCap(Paint.Cap.BUTT);
    }

    protected void drawClose(Canvas canvas) {
        RectF bounds = rect(layout.closeHit());
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeWidth(7.0f);
        strokePaint.setColor(Color.rgb(255, 235, 184));
        canvas.drawLine(
                bounds.left + 20.0f,
                bounds.top + 20.0f,
                bounds.right - 20.0f,
                bounds.bottom - 20.0f,
                strokePaint);
        canvas.drawLine(
                bounds.right - 20.0f,
                bounds.top + 20.0f,
                bounds.left + 20.0f,
                bounds.bottom - 20.0f,
                strokePaint);
        strokePaint.setStrokeWidth(2.0f);
        strokePaint.setColor(CHAMPAGNE_DARK);
        canvas.drawLine(
                bounds.left + 21.0f,
                bounds.top + 21.0f,
                bounds.right - 19.0f,
                bounds.bottom - 19.0f,
                strokePaint);
        canvas.drawLine(
                bounds.right - 19.0f,
                bounds.top + 21.0f,
                bounds.left + 21.0f,
                bounds.bottom - 19.0f,
                strokePaint);
        strokePaint.setStrokeCap(Paint.Cap.BUTT);
    }
}
