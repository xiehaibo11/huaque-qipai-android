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

abstract class PersonalCenterIdentityIconView extends PersonalCenterIconPrimitivesView {
    protected PersonalCenterIdentityIconView(
            Context context,
            PersonalCenterState state,
            PersonalCenterSystemSettings systemSettings,
            Bitmap avatarBitmap,
            Listener listener) {
        super(context, state, systemSettings, avatarBitmap, listener);
    }
    protected boolean drawIdentityLineIcon(
            Canvas canvas,
            IconType icon,
            float centerX,
            float centerY,
            float half,
            float size,
            int color) {
        switch (icon) {
            case PERSON -> {
                canvas.drawCircle(
                        centerX,
                        centerY - half * 0.42f,
                        half * 0.28f,
                        fillPaint);
                reusablePath.moveTo(
                        centerX - half * 0.65f,
                        centerY + half * 0.62f);
                reusablePath.quadTo(
                        centerX,
                        centerY - half * 0.05f,
                        centerX + half * 0.65f,
                        centerY + half * 0.62f);
                reusablePath.close();
                canvas.drawPath(reusablePath, fillPaint);
            }
            case SHIELD -> {
                reusablePath.moveTo(centerX, centerY - half);
                reusablePath.lineTo(
                        centerX + half * 0.78f,
                        centerY - half * 0.62f);
                reusablePath.lineTo(
                        centerX + half * 0.62f,
                        centerY + half * 0.42f);
                reusablePath.quadTo(
                        centerX,
                        centerY + half,
                        centerX - half * 0.62f,
                        centerY + half * 0.42f);
                reusablePath.lineTo(
                        centerX - half * 0.78f,
                        centerY - half * 0.62f);
                reusablePath.close();
                canvas.drawPath(reusablePath, strokePaint);
                canvas.drawLine(
                        centerX,
                        centerY - half * 0.58f,
                        centerX,
                        centerY + half * 0.5f,
                        strokePaint);
            }
            case LOCK -> {
                RectF body =
                        new RectF(
                                centerX - half * 0.68f,
                                centerY - half * 0.05f,
                                centerX + half * 0.68f,
                                centerY + half * 0.82f);
                canvas.drawRoundRect(
                        body,
                        half * 0.12f,
                        half * 0.12f,
                        fillPaint);
                RectF shackle =
                        new RectF(
                                centerX - half * 0.43f,
                                centerY - half * 0.82f,
                                centerX + half * 0.43f,
                                centerY + half * 0.22f);
                canvas.drawArc(shackle, 180.0f, 180.0f, false, strokePaint);
                fillPaint.setColor(CREAM);
                canvas.drawCircle(
                        centerX,
                        centerY + half * 0.35f,
                        half * 0.1f,
                        fillPaint);
            }
            case PRIVACY, SETTINGS, REPAIR -> {
                canvas.drawCircle(centerX, centerY, half * 0.48f, strokePaint);
                canvas.drawCircle(centerX, centerY, half * 0.16f, strokePaint);
                for (int index = 0; index < 8; index++) {
                    double angle = Math.PI * index / 4.0;
                    float x1 =
                            centerX
                                    + (float) Math.cos(angle)
                                            * half
                                            * 0.58f;
                    float y1 =
                            centerY
                                    + (float) Math.sin(angle)
                                            * half
                                            * 0.58f;
                    float x2 =
                            centerX
                                    + (float) Math.cos(angle)
                                            * half
                                            * 0.9f;
                    float y2 =
                            centerY
                                    + (float) Math.sin(angle)
                                            * half
                                            * 0.9f;
                    canvas.drawLine(x1, y1, x2, y2, strokePaint);
                }
            }
            case HELP, FAQ -> {
                canvas.drawCircle(centerX, centerY, half * 0.82f, strokePaint);
                setText(
                        size * 0.75f, color, true, Paint.Align.CENTER);
                drawBaselineCenteredText(canvas, "?", centerX, centerY);
            }
            case PHONE, DEVICE, VIBRATION -> {
                RectF phone =
                        new RectF(
                                centerX - half * 0.48f,
                                centerY - half * 0.9f,
                                centerX + half * 0.48f,
                                centerY + half * 0.9f);
                canvas.drawRoundRect(
                        phone,
                        half * 0.13f,
                        half * 0.13f,
                        strokePaint);
                canvas.drawLine(
                        centerX - half * 0.2f,
                        centerY - half * 0.68f,
                        centerX + half * 0.2f,
                        centerY - half * 0.68f,
                        strokePaint);
                canvas.drawCircle(
                        centerX,
                        centerY + half * 0.67f,
                        half * 0.06f,
                        fillPaint);
                if (icon == IconType.VIBRATION) {
                    canvas.drawArc(
                            new RectF(
                                    centerX - half * 0.9f,
                                    centerY - half * 0.55f,
                                    centerX - half * 0.45f,
                                    centerY + half * 0.55f),
                            90.0f,
                            180.0f,
                            false,
                            strokePaint);
                    canvas.drawArc(
                            new RectF(
                                    centerX + half * 0.45f,
                                    centerY - half * 0.55f,
                                    centerX + half * 0.9f,
                                    centerY + half * 0.55f),
                            -90.0f,
                            180.0f,
                            false,
                            strokePaint);
                }
            }
            case ID_CARD -> {
                RectF card =
                        new RectF(
                                centerX - half * 0.88f,
                                centerY - half * 0.64f,
                                centerX + half * 0.88f,
                                centerY + half * 0.64f);
                canvas.drawRoundRect(
                        card,
                        half * 0.12f,
                        half * 0.12f,
                        strokePaint);
                canvas.drawCircle(
                        centerX - half * 0.46f,
                        centerY - half * 0.15f,
                        half * 0.16f,
                        strokePaint);
                canvas.drawArc(
                        new RectF(
                                centerX - half * 0.7f,
                                centerY,
                                centerX - half * 0.22f,
                                centerY + half * 0.42f),
                        180.0f,
                        180.0f,
                        false,
                        strokePaint);
                canvas.drawLine(
                        centerX,
                        centerY - half * 0.2f,
                        centerX + half * 0.62f,
                        centerY - half * 0.2f,
                        strokePaint);
                canvas.drawLine(
                        centerX,
                        centerY + half * 0.2f,
                        centerX + half * 0.48f,
                        centerY + half * 0.2f,
                        strokePaint);
            }
            default -> { return false; }
        }
        return true;
    }
}
