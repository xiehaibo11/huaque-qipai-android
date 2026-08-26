package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;

/** Draws the generated membership badge with the original lobby's eight-second rhythm. */
final class MembershipBadgeRenderer {
    private static final float PLAQUE_LEFT_RATIO = 0.49f;

    private final Bitmap badge;
    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint sweepPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sparklePaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Path sparklePath = new Path();

    MembershipBadgeRenderer(Resources resources, int badgeResourceId) {
        badge = BitmapFactory.decodeResource(resources, badgeResourceId);
        if (badge == null) {
            throw new IllegalStateException(
                    "Unable to decode generated membership badge");
        }
        sweepPaint.setXfermode(
                new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        sparklePaint.setStyle(Paint.Style.FILL);
        sparklePaint.setXfermode(
                new PorterDuffXfermode(PorterDuff.Mode.ADD));
    }

    void draw(Canvas canvas, RectF bounds, long timeMs) {
        MembershipBadgeAnimation.Frame frame =
                MembershipBadgeAnimation.sample(timeMs);
        RectF contentBounds = new RectF(bounds);
        contentBounds.inset(2.0f, 2.0f);
        RectF fitted = fitCenter(contentBounds);

        int transformSave = canvas.save();
        float pivotX = fitted.left + fitted.width() * 0.34f;
        canvas.scale(frame.scale(), frame.scale(), pivotX, fitted.centerY());

        int badgeLayer = canvas.saveLayer(fitted, null);
        canvas.drawBitmap(badge, null, fitted, bitmapPaint);
        drawPlaqueSweep(canvas, fitted, frame);
        canvas.restoreToCount(badgeLayer);

        drawSparkles(canvas, fitted, frame);
        canvas.restoreToCount(transformSave);
    }

    private RectF fitCenter(RectF bounds) {
        float bitmapAspect = badge.getWidth() / (float) badge.getHeight();
        float boundsAspect = bounds.width() / bounds.height();
        if (bitmapAspect > boundsAspect) {
            float height = bounds.width() / bitmapAspect;
            return new RectF(
                    bounds.left,
                    bounds.centerY() - height / 2.0f,
                    bounds.right,
                    bounds.centerY() + height / 2.0f);
        }
        float width = bounds.height() * bitmapAspect;
        return new RectF(
                bounds.centerX() - width / 2.0f,
                bounds.top,
                bounds.centerX() + width / 2.0f,
                bounds.bottom);
    }

    private void drawPlaqueSweep(
            Canvas canvas,
            RectF fitted,
            MembershipBadgeAnimation.Frame frame) {
        if (frame.sweepAlpha() <= 0.001f) {
            return;
        }
        RectF plaque =
                new RectF(
                        fitted.left + fitted.width() * PLAQUE_LEFT_RATIO,
                        fitted.top,
                        fitted.right,
                        fitted.bottom);
        float sweepWidth = Math.max(12.0f, plaque.width() * 0.18f);
        float centerX =
                plaque.left
                        - sweepWidth
                        + (plaque.width() + sweepWidth * 2.0f)
                                * frame.sweepProgress();
        int peakAlpha = Math.round(120.0f * frame.sweepAlpha());
        sweepPaint.setShader(
                new LinearGradient(
                        centerX - sweepWidth,
                        plaque.top,
                        centerX + sweepWidth,
                        plaque.bottom,
                        new int[] {
                            Color.TRANSPARENT,
                            Color.argb(peakAlpha, 255, 250, 218),
                            Color.TRANSPARENT
                        },
                        null,
                        Shader.TileMode.CLAMP));

        int saveCount = canvas.save();
        canvas.clipRect(plaque);
        canvas.drawRect(plaque, sweepPaint);
        canvas.restoreToCount(saveCount);
        sweepPaint.setShader(null);
    }

    private void drawSparkles(
            Canvas canvas,
            RectF fitted,
            MembershipBadgeAnimation.Frame frame) {
        int layer = canvas.saveLayer(fitted, null);
        for (MembershipBadgeAnimation.Sparkle sparkle : frame.sparkles()) {
            if (sparkle.alpha() <= 0.01f) {
                continue;
            }
            float x = fitted.left + fitted.width() * sparkle.x();
            float y = fitted.top + fitted.height() * sparkle.y();
            float radius =
                    Math.max(2.2f, fitted.width() * sparkle.size());
            sparklePaint.setColor(
                    Color.argb(
                            Math.round(230.0f * sparkle.alpha()),
                            255,
                            249,
                            210));
            buildSparklePath(x, y, radius);
            canvas.drawPath(sparklePath, sparklePaint);
            sparklePaint.setColor(
                    Color.argb(
                            Math.round(150.0f * sparkle.alpha()),
                            255,
                            255,
                            255));
            canvas.drawCircle(x, y, radius * 0.28f, sparklePaint);
        }
        canvas.restoreToCount(layer);
    }

    private void buildSparklePath(float x, float y, float radius) {
        float inner = radius * 0.18f;
        sparklePath.reset();
        sparklePath.moveTo(x, y - radius);
        sparklePath.lineTo(x + inner, y - inner);
        sparklePath.lineTo(x + radius, y);
        sparklePath.lineTo(x + inner, y + inner);
        sparklePath.lineTo(x, y + radius);
        sparklePath.lineTo(x - inner, y + inner);
        sparklePath.lineTo(x - radius, y);
        sparklePath.lineTo(x - inner, y - inner);
        sparklePath.close();
    }
}
