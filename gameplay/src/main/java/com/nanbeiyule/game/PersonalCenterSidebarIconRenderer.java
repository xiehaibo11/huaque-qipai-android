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

abstract class PersonalCenterSidebarIconRenderer extends PersonalCenterLineIconView {
    protected PersonalCenterSidebarIconRenderer(
            Context context,
            PersonalCenterState state,
            PersonalCenterSystemSettings systemSettings,
            Bitmap avatarBitmap,
            Listener listener) {
        super(context, state, systemSettings, avatarBitmap, listener);
    }
    protected void drawSidebarLandscape(Canvas canvas) {
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(1.2f);
        strokePaint.setColor(Color.argb(55, 216, 162, 74));
        reusablePath.reset();
        reusablePath.moveTo(20.0f, 970.0f);
        reusablePath.cubicTo(
                72.0f, 910.0f, 114.0f, 990.0f, 164.0f, 930.0f);
        reusablePath.cubicTo(
                214.0f, 872.0f, 262.0f, 976.0f, 330.0f, 904.0f);
        reusablePath.lineTo(392.0f, 960.0f);
        canvas.drawPath(reusablePath, strokePaint);
        canvas.drawLine(26.0f, 1005.0f, 382.0f, 1005.0f, strokePaint);

        float towerX = 292.0f;
        canvas.drawRect(
                towerX - 34.0f,
                918.0f,
                towerX + 34.0f,
                1005.0f,
                strokePaint);
        canvas.drawLine(
                towerX,
                875.0f,
                towerX,
                1005.0f,
                strokePaint);
        for (int index = 0; index < 3; index++) {
            float y = 905.0f + index * 31.0f;
            canvas.drawLine(
                    towerX - 58.0f,
                    y,
                    towerX,
                    y - 15.0f,
                    strokePaint);
            canvas.drawLine(
                    towerX,
                    y - 15.0f,
                    towerX + 58.0f,
                    y,
                    strokePaint);
        }
        strokePaint.setStyle(Paint.Style.STROKE);
    }

    protected void drawIcon(
            Canvas canvas,
            IconType icon,
            float centerX,
            float centerY,
            float size,
            int fallbackColor) {
        if (!drawAssetIcon(canvas, icon, centerX, centerY, size)) {
            drawLineIcon(
                    canvas,
                    icon,
                    centerX,
                    centerY,
                    size,
                    fallbackColor);
        }
    }

    protected boolean drawAssetIcon(
            Canvas canvas,
            IconType icon,
            float centerX,
            float centerY,
            float size) {
        Bitmap bitmap = personalCenterIcons.get(icon);
        if (bitmap == null || bitmap.isRecycled()) {
            return false;
        }
        float targetSize = size * 1.2f;
        canvas.drawBitmap(
                bitmap,
                null,
                new RectF(
                        centerX - targetSize / 2.0f,
                        centerY - targetSize / 2.0f,
                        centerX + targetSize / 2.0f,
                        centerY + targetSize / 2.0f),
                bitmapPaint);
        return true;
    }

    protected void drawMenuAssetIcon(
            Canvas canvas,
            IconType icon,
            float centerX,
            float centerY,
            float size,
            int fallbackColor,
            boolean selected) {
        Bitmap bitmap = personalCenterIcons.get(icon);
        if (bitmap == null || bitmap.isRecycled()) {
            drawLineIcon(
                    canvas,
                    icon,
                    centerX,
                    centerY,
                    size,
                    fallbackColor);
            return;
        }
        if (selected) {
            bitmapPaint.setColorFilter(
                    new PorterDuffColorFilter(
                            fallbackColor,
                            PorterDuff.Mode.SRC_IN));
        }
        float targetSize = size * 1.2f;
        canvas.drawBitmap(
                bitmap,
                null,
                new RectF(
                        centerX - targetSize / 2.0f,
                        centerY - targetSize / 2.0f,
                        centerX + targetSize / 2.0f,
                        centerY + targetSize / 2.0f),
                bitmapPaint);
        bitmapPaint.setColorFilter(null);
    }
}
