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

abstract class PersonalCenterLineIconView extends PersonalCenterIdentityIconView {
    protected PersonalCenterLineIconView(
            Context context,
            PersonalCenterState state,
            PersonalCenterSystemSettings systemSettings,
            Bitmap avatarBitmap,
            Listener listener) {
        super(context, state, systemSettings, avatarBitmap, listener);
    }
    protected void drawLineIcon(
            Canvas canvas,
            IconType icon,
            float centerX,
            float centerY,
            float size,
            int color) {
        float half = size / 2.0f;
        strokePaint.setColor(color);
        strokePaint.setStrokeWidth(Math.max(2.0f, size * 0.085f));
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
        fillPaint.setColor(color);
        reusablePath.reset();
        if (drawIdentityLineIcon(canvas, icon, centerX, centerY, half, size, color)) {
            strokePaint.setStrokeCap(Paint.Cap.BUTT);
            strokePaint.setStrokeJoin(Paint.Join.MITER);
            return;
        }
        switch (icon) {
            case FAVORITE -> drawStar(canvas, centerX, centerY, half, color);
            case GROWTH, GIFT, CACHE -> {
                canvas.drawRect(
                        centerX - half * 0.76f,
                        centerY - half * 0.12f,
                        centerX + half * 0.76f,
                        centerY + half * 0.72f,
                        strokePaint);
                canvas.drawRect(
                        centerX - half * 0.88f,
                        centerY - half * 0.42f,
                        centerX + half * 0.88f,
                        centerY - half * 0.08f,
                        strokePaint);
                canvas.drawLine(
                        centerX,
                        centerY - half * 0.42f,
                        centerX,
                        centerY + half * 0.72f,
                        strokePaint);
                canvas.drawArc(
                        new RectF(
                                centerX - half * 0.68f,
                                centerY - half,
                                centerX,
                                centerY - half * 0.35f),
                        10.0f,
                        230.0f,
                        false,
                        strokePaint);
                canvas.drawArc(
                        new RectF(
                                centerX,
                                centerY - half,
                                centerX + half * 0.68f,
                                centerY - half * 0.35f),
                        -60.0f,
                        230.0f,
                        false,
                        strokePaint);
            }
            case MESSAGE, FEEDBACK -> {
                RectF envelope =
                        new RectF(
                                centerX - half * 0.86f,
                                centerY - half * 0.62f,
                                centerX + half * 0.86f,
                                centerY + half * 0.62f);
                canvas.drawRoundRect(
                        envelope,
                        half * 0.1f,
                        half * 0.1f,
                        strokePaint);
                canvas.drawLine(
                        envelope.left,
                        envelope.top,
                        centerX,
                        centerY + half * 0.12f,
                        strokePaint);
                canvas.drawLine(
                        envelope.right,
                        envelope.top,
                        centerX,
                        centerY + half * 0.12f,
                        strokePaint);
            }
            case MEDAL, HISTORY -> {
                canvas.drawCircle(centerX, centerY, half * 0.78f, strokePaint);
                canvas.drawLine(
                        centerX,
                        centerY,
                        centerX,
                        centerY - half * 0.45f,
                        strokePaint);
                canvas.drawLine(
                        centerX,
                        centerY,
                        centerX + half * 0.38f,
                        centerY + half * 0.18f,
                        strokePaint);
                if (icon == IconType.HISTORY) {
                    canvas.drawArc(
                            new RectF(
                                    centerX - half,
                                    centerY - half,
                                    centerX + half,
                                    centerY + half),
                            150.0f,
                            235.0f,
                            false,
                            strokePaint);
                }
            }
            case MUSIC -> {
                canvas.drawLine(
                        centerX + half * 0.32f,
                        centerY - half * 0.84f,
                        centerX + half * 0.32f,
                        centerY + half * 0.45f,
                        strokePaint);
                canvas.drawLine(
                        centerX + half * 0.32f,
                        centerY - half * 0.84f,
                        centerX + half * 0.84f,
                        centerY - half,
                        strokePaint);
                canvas.drawCircle(
                        centerX,
                        centerY + half * 0.55f,
                        half * 0.34f,
                        fillPaint);
            }
            case SOUND -> {
                reusablePath.moveTo(
                        centerX - half * 0.85f,
                        centerY - half * 0.3f);
                reusablePath.lineTo(
                        centerX - half * 0.4f,
                        centerY - half * 0.3f);
                reusablePath.lineTo(
                        centerX + half * 0.05f,
                        centerY - half * 0.75f);
                reusablePath.lineTo(
                        centerX + half * 0.05f,
                        centerY + half * 0.75f);
                reusablePath.lineTo(
                        centerX - half * 0.4f,
                        centerY + half * 0.3f);
                reusablePath.lineTo(
                        centerX - half * 0.85f,
                        centerY + half * 0.3f);
                reusablePath.close();
                canvas.drawPath(reusablePath, fillPaint);
                canvas.drawArc(
                        new RectF(
                                centerX - half * 0.2f,
                                centerY - half * 0.65f,
                                centerX + half * 0.72f,
                                centerY + half * 0.65f),
                        -55.0f,
                        110.0f,
                        false,
                        strokePaint);
            }
            case MICROPHONE -> {
                RectF mic =
                        new RectF(
                                centerX - half * 0.35f,
                                centerY - half * 0.9f,
                                centerX + half * 0.35f,
                                centerY + half * 0.35f);
                canvas.drawRoundRect(
                        mic,
                        half * 0.35f,
                        half * 0.35f,
                        strokePaint);
                canvas.drawArc(
                        new RectF(
                                centerX - half * 0.7f,
                                centerY - half * 0.1f,
                                centerX + half * 0.7f,
                                centerY + half * 0.65f),
                        0.0f,
                        180.0f,
                        false,
                        strokePaint);
                canvas.drawLine(
                        centerX,
                        centerY + half * 0.62f,
                        centerX,
                        centerY + half * 0.9f,
                        strokePaint);
            }
            case NETWORK -> {
                for (int index = 0; index < 3; index++) {
                    float inset = half * (0.05f + index * 0.25f);
                    canvas.drawArc(
                            new RectF(
                                    centerX - half + inset,
                                    centerY - half + inset,
                                    centerX + half - inset,
                                    centerY + half - inset),
                            220.0f,
                            100.0f,
                            false,
                            strokePaint);
                }
                canvas.drawCircle(
                        centerX,
                        centerY + half * 0.62f,
                        half * 0.1f,
                        fillPaint);
            }
            case ALERT -> {
                reusablePath.moveTo(centerX, centerY - half);
                reusablePath.lineTo(
                        centerX + half * 0.9f,
                        centerY + half * 0.78f);
                reusablePath.lineTo(
                        centerX - half * 0.9f,
                        centerY + half * 0.78f);
                reusablePath.close();
                canvas.drawPath(reusablePath, strokePaint);
                canvas.drawLine(
                        centerX,
                        centerY - half * 0.42f,
                        centerX,
                        centerY + half * 0.2f,
                        strokePaint);
                canvas.drawCircle(
                        centerX,
                        centerY + half * 0.5f,
                        half * 0.08f,
                        fillPaint);
            }
        }
        strokePaint.setStrokeCap(Paint.Cap.BUTT);
        strokePaint.setStrokeJoin(Paint.Join.MITER);
    }
}
