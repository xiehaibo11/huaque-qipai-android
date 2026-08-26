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

abstract class PersonalCenterIconPrimitivesView extends PersonalCenterViewState {
    protected PersonalCenterIconPrimitivesView(
            Context context,
            PersonalCenterState state,
            PersonalCenterSystemSettings systemSettings,
            Bitmap avatarBitmap,
            Listener listener) {
        super(context, state, systemSettings, avatarBitmap, listener);
    }
    protected void drawStar(
            Canvas canvas,
            float centerX,
            float centerY,
            float radius,
            int color) {
        reusablePath.reset();
        for (int index = 0; index < 10; index++) {
            double angle = -Math.PI / 2.0 + index * Math.PI / 5.0;
            float pointRadius = index % 2 == 0 ? radius : radius * 0.43f;
            float x =
                    centerX + (float) Math.cos(angle) * pointRadius;
            float y =
                    centerY + (float) Math.sin(angle) * pointRadius;
            if (index == 0) {
                reusablePath.moveTo(x, y);
            } else {
                reusablePath.lineTo(x, y);
            }
        }
        reusablePath.close();
        fillPaint.setColor(color);
        canvas.drawPath(reusablePath, fillPaint);
    }

    protected void drawCrown(
            Canvas canvas,
            float centerX,
            float centerY,
            float size,
            int color) {
        float half = size / 2.0f;
        reusablePath.reset();
        reusablePath.moveTo(centerX - half, centerY + half * 0.55f);
        reusablePath.lineTo(centerX - half * 0.8f, centerY - half * 0.5f);
        reusablePath.lineTo(centerX - half * 0.25f, centerY);
        reusablePath.lineTo(centerX, centerY - half);
        reusablePath.lineTo(centerX + half * 0.25f, centerY);
        reusablePath.lineTo(centerX + half * 0.8f, centerY - half * 0.5f);
        reusablePath.lineTo(centerX + half, centerY + half * 0.55f);
        reusablePath.close();
        fillPaint.setColor(color);
        canvas.drawPath(reusablePath, fillPaint);
    }

    protected void drawChevron(
            Canvas canvas, float centerX, float centerY, int color) {
        strokePaint.setStrokeWidth(2.5f);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setColor(color);
        canvas.drawLine(
                centerX - 6.0f,
                centerY - 10.0f,
                centerX + 4.0f,
                centerY,
                strokePaint);
        canvas.drawLine(
                centerX + 4.0f,
                centerY,
                centerX - 6.0f,
                centerY + 10.0f,
                strokePaint);
        strokePaint.setStrokeCap(Paint.Cap.BUTT);
    }

    protected IconType menuIcon(int index) {
        return switch (index) {
            case 0 -> IconType.PERSON;
            case 1 -> IconType.SHIELD;
            case 2 -> IconType.PRIVACY;
            case 3 -> IconType.SETTINGS;
            case 4 -> IconType.HELP;
            default -> throw new IllegalArgumentException(
                    "Unknown menu icon " + index);
        };
    }

    protected void setText(
            float size,
            int color,
            boolean bold,
            Paint.Align align) {
        textPaint.setTextSize(size);
        textPaint.setTextAlign(align);
        textPaint.setColor(color);
        textPaint.setTypeface(
                Typeface.create(
                        typeface,
                        bold ? Typeface.BOLD : Typeface.NORMAL));
    }

    protected void drawBaselineCenteredText(
            Canvas canvas,
            String value,
            float anchorX,
            float centerY) {
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseline =
                centerY
                        - (metrics.ascent + metrics.descent) / 2.0f;
        canvas.drawText(value, anchorX, baseline, textPaint);
    }

}
