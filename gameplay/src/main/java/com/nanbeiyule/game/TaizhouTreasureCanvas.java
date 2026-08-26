package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import java.util.ArrayList;
import java.util.List;

/** Small native drawing helpers shared by the four recovered JuBaoPen CSB surfaces. */
final class TaizhouTreasureCanvas {
    private TaizhouTreasureCanvas() {}

    static Bitmap load(Resources resources, int resource) {
        return BitmapFactory.decodeResource(resources, resource);
    }

    static void draw(Canvas canvas, Bitmap bitmap, RectF target, Paint paint) {
        if (bitmap != null && !bitmap.isRecycled()) {
            canvas.drawBitmap(bitmap, null, target, paint);
        }
    }

    static void drawNineSlice(
            Canvas canvas,
            Bitmap bitmap,
            RectF target,
            int capX,
            int capY,
            int capWidth,
            int capHeight,
            Paint paint) {
        if (bitmap == null || bitmap.isRecycled()) return;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int topFixed = height - capY - capHeight;
        int rightFixed = width - capX - capWidth;
        int[] sourceX = {0, capX, capX + capWidth, width};
        int[] sourceY = {0, topFixed, topFixed + capHeight, height};
        float[] targetX = {target.left, target.left + capX,
                target.right - rightFixed, target.right};
        float[] targetY = {target.top, target.top + topFixed,
                target.bottom - capY, target.bottom};
        Rect source = new Rect();
        RectF destination = new RectF();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                source.set(sourceX[column], sourceY[row],
                        sourceX[column + 1], sourceY[row + 1]);
                destination.set(targetX[column], targetY[row],
                        targetX[column + 1], targetY[row + 1]);
                canvas.drawBitmap(bitmap, source, destination, paint);
            }
        }
    }

    static void text(
            Canvas canvas,
            Paint paint,
            Typeface typeface,
            String value,
            float x,
            float centerY,
            float size,
            int color,
            Paint.Align align) {
        paint.setTypeface(typeface);
        paint.setTextAlign(align);
        paint.setTextSize(size);
        paint.setColor(color);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        canvas.drawText(value == null ? "" : value, x,
                centerY - (metrics.ascent + metrics.descent) * 0.5f, paint);
    }

    static void wrappedText(
            Canvas canvas,
            Paint paint,
            Typeface typeface,
            String value,
            RectF bounds,
            float size,
            float lineHeight,
            int color) {
        paint.setTypeface(typeface);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(size);
        paint.setColor(color);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        float baseline = bounds.top - metrics.ascent;
        for (String line : wrap(paint, value, bounds.width())) {
            if (!line.isEmpty()) canvas.drawText(line, bounds.left, baseline, paint);
            baseline += lineHeight;
            if (baseline > bounds.bottom - metrics.descent) return;
        }
    }

    static void recycle(Bitmap... bitmaps) {
        for (Bitmap bitmap : bitmaps) {
            if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
        }
    }

    private static List<String> wrap(Paint paint, String value, float width) {
        List<String> lines = new ArrayList<>();
        String[] paragraphs = (value == null ? "" : value).split("\\n", -1);
        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }
            int start = 0;
            while (start < paragraph.length()) {
                int count = paint.breakText(
                        paragraph, start, paragraph.length(), true, width, null);
                if (count <= 0) count = 1;
                lines.add(paragraph.substring(start, start + count));
                start += count;
            }
        }
        return lines;
    }
}
