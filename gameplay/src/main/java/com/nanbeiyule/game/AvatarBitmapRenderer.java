package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

final class AvatarBitmapRenderer {
    private AvatarBitmapRenderer() {}

    static void drawCenterCrop(
            Canvas canvas,
            Bitmap bitmap,
            RectF destination,
            Paint bitmapPaint) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        float sourceRatio = bitmap.getWidth() / (float) bitmap.getHeight();
        float targetRatio = destination.width() / destination.height();
        Rect source;
        if (sourceRatio > targetRatio) {
            int width = Math.round(bitmap.getHeight() * targetRatio);
            int left = (bitmap.getWidth() - width) / 2;
            source = new Rect(left, 0, left + width, bitmap.getHeight());
        } else {
            int height = Math.round(bitmap.getWidth() / targetRatio);
            int top = (bitmap.getHeight() - height) / 2;
            source = new Rect(0, top, bitmap.getWidth(), top + height);
        }
        canvas.drawBitmap(bitmap, source, destination, bitmapPaint);
    }
}
