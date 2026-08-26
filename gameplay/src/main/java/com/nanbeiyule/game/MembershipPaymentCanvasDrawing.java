package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

/** Drawing primitives shared by the original payment dialogs. */
final class MembershipPaymentCanvasDrawing {
    private MembershipPaymentCanvasDrawing() {}

    static Transform transform(int width, int height) {
        float scale =
                Math.min(
                        width / MembershipPaymentDialogLayout.DESIGN_WIDTH,
                        height / MembershipPaymentDialogLayout.DESIGN_HEIGHT);
        return new Transform(
                scale,
                (width - MembershipPaymentDialogLayout.DESIGN_WIDTH * scale) * 0.5f,
                (height - MembershipPaymentDialogLayout.DESIGN_HEIGHT * scale) * 0.5f);
    }

    static int apply(Canvas canvas, Transform transform) {
        int saveCount = canvas.save();
        canvas.translate(transform.offsetX(), transform.offsetY());
        canvas.scale(transform.scale(), transform.scale());
        return saveCount;
    }

    static void drawBitmap(
            Canvas canvas,
            Bitmap bitmap,
            MembershipPaymentDialogLayout.DesignRect bounds,
            Paint paint) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        canvas.drawBitmap(
                bitmap,
                new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                toRectF(bounds),
                paint);
    }

    static void drawNineSlice(
            Canvas canvas,
            Bitmap bitmap,
            MembershipPaymentDialogLayout.DesignRect bounds,
            Paint paint,
            int sourceLeft,
            int sourceTop,
            int sourceRight,
            int sourceBottom) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] sourceX = {0, sourceLeft, width - sourceRight, width};
        int[] sourceY = {0, sourceTop, height - sourceBottom, height};
        float[] destinationX = {
            bounds.left(),
            bounds.left() + sourceLeft,
            bounds.right() - sourceRight,
            bounds.right()
        };
        float[] destinationY = {
            bounds.top(),
            bounds.top() + sourceTop,
            bounds.bottom() - sourceBottom,
            bounds.bottom()
        };
        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 3; row++) {
                canvas.drawBitmap(
                        bitmap,
                        new Rect(
                                sourceX[column],
                                sourceY[row],
                                sourceX[column + 1],
                                sourceY[row + 1]),
                        new RectF(
                                destinationX[column],
                                destinationY[row],
                                destinationX[column + 1],
                                destinationY[row + 1]),
                        paint);
            }
        }
    }

    static void drawCenteredText(
            Canvas canvas,
            Paint paint,
            Typeface typeface,
            String text,
            float centerX,
            float centerY,
            float textSize,
            int color,
            int alpha) {
        paint.setTypeface(typeface);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(textSize);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setAlpha(alpha);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        float baseline = centerY - (metrics.ascent + metrics.descent) * 0.5f;
        canvas.drawText(text, centerX, baseline, paint);
        paint.setAlpha(255);
    }

    private static RectF toRectF(MembershipPaymentDialogLayout.DesignRect bounds) {
        return new RectF(bounds.left(), bounds.top(), bounds.right(), bounds.bottom());
    }

    record Transform(float scale, float offsetX, float offsetY) {
        float designX(float viewX) {
            return (viewX - offsetX) / scale;
        }

        float designY(float viewY) {
            return (viewY - offsetY) / scale;
        }
    }
}
