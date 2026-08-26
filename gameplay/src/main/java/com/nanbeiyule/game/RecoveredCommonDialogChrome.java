package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

/** Shared native rendering of the recovered Zhejiang common dialog frame. */
final class RecoveredCommonDialogChrome {
    private static final String FONT_ASSET = "fonts/zihun_jingdian_lihei.ttf";

    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Bitmap panelPatch;
    private final Bitmap titleBackground;
    private final Bitmap corner;
    private final Bitmap close;

    RecoveredCommonDialogChrome(Context context) {
        panelPatch = load(context, R.drawable.zhejiang_settings_panel_patch);
        titleBackground = load(context, R.drawable.zhejiang_settings_title_bg);
        corner = load(context, R.drawable.zhejiang_settings_corner);
        close = load(context, R.drawable.zhejiang_settings_close);
        titlePaint.setColor(0xFF8A4B2A);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.createFromAsset(context.getAssets(), FONT_ASSET));
    }

    void draw(
            Canvas canvas,
            float width,
            float height,
            String title,
            RectF closeBounds) {
        drawNineSlice(canvas, panelPatch, new RectF(1f, 79f, width - 1f, height), 33);
        float half = width / 2f;
        drawBitmap(canvas, titleBackground, new RectF(1f, 0f, half + 1f, 81f));
        int save = canvas.save();
        canvas.scale(-1f, 1f, width * 0.75f, 40.5f);
        drawBitmap(canvas, titleBackground, new RectF(half, 0f, width, 81f));
        canvas.restoreToCount(save);
        drawBitmap(canvas, corner, new RectF(20f, height - 102f, 109f, height - 20f));
        save = canvas.save();
        canvas.scale(-1f, 1f, width - 64.5f, height - 61f);
        drawBitmap(
                canvas,
                corner,
                new RectF(width - 109f, height - 102f, width - 20f, height - 20f));
        canvas.restoreToCount(save);
        drawBitmap(canvas, close, closeBounds);

        titlePaint.setTextSize(46f);
        Paint.FontMetrics metrics = titlePaint.getFontMetrics();
        float baseline = 40.5f - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText(title, width / 2f, baseline, titlePaint);
    }

    private void drawNineSlice(
            Canvas canvas, Bitmap bitmap, RectF destination, int edge) {
        int[] sourceX = {0, edge, bitmap.getWidth() - edge, bitmap.getWidth()};
        int[] sourceY = {0, edge, bitmap.getHeight() - edge, bitmap.getHeight()};
        float[] targetX = {
            destination.left,
            destination.left + edge,
            destination.right - edge,
            destination.right
        };
        float[] targetY = {
            destination.top,
            destination.top + edge,
            destination.bottom - edge,
            destination.bottom
        };
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                canvas.drawBitmap(
                        bitmap,
                        new Rect(
                                sourceX[column],
                                sourceY[row],
                                sourceX[column + 1],
                                sourceY[row + 1]),
                        new RectF(
                                targetX[column],
                                targetY[row],
                                targetX[column + 1],
                                targetY[row + 1]),
                        bitmapPaint);
            }
        }
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, RectF destination) {
        canvas.drawBitmap(bitmap, null, destination, bitmapPaint);
    }

    private static Bitmap load(Context context, int resourceId) {
        return BitmapFactory.decodeResource(context.getResources(), resourceId);
    }
}
