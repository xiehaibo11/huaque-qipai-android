package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Cocos {@code Scale9Sprite} 的 3x3 拉伸：四角原样、四边单向拉伸、中心双向拉伸。
 *
 * <p>CSD 的 {@code Scale9OriginX/Y} 与 {@code Scale9Width/Height} 以**图片左上角**
 * 为原点，四条边距因此是 {@code left = originX}、{@code top = originY}、
 * {@code right = 图宽 - originX - width}、{@code bottom = 图高 - originY - height}。
 * 节点被整体缩放时边距一起缩放，所以 {@code scale} 同时作用在源边距上。
 */
final class TaizhouNineSlice {
    private TaizhouNineSlice() {}

    static void draw(
            Canvas canvas,
            Bitmap bitmap,
            Paint paint,
            RectF destination,
            float capX,
            float capY,
            float capWidth,
            float capHeight,
            float scale) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        int leftCap = Math.round(capX);
        int topCap = Math.round(capY);
        int rightCap = Math.round(bitmap.getWidth() - capX - capWidth);
        int bottomCap = Math.round(bitmap.getHeight() - capY - capHeight);
        float leftEdge = leftCap * scale;
        float topEdge = topCap * scale;
        float rightEdge = rightCap * scale;
        float bottomEdge = bottomCap * scale;
        if (leftCap < 0
                || rightCap < 0
                || topCap < 0
                || bottomCap < 0
                || destination.width() < leftEdge + rightEdge
                || destination.height() < topEdge + bottomEdge) {
            canvas.drawBitmap(bitmap, null, destination, paint);
            return;
        }
        int[] sourceX = {0, leftCap, bitmap.getWidth() - rightCap, bitmap.getWidth()};
        int[] sourceY = {0, topCap, bitmap.getHeight() - bottomCap, bitmap.getHeight()};
        float[] targetX = {
            destination.left,
            destination.left + leftEdge,
            destination.right - rightEdge,
            destination.right,
        };
        float[] targetY = {
            destination.top,
            destination.top + topEdge,
            destination.bottom - bottomEdge,
            destination.bottom,
        };
        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 3; row++) {
                if (sourceX[column] >= sourceX[column + 1]
                        || sourceY[row] >= sourceY[row + 1]
                        || targetX[column] >= targetX[column + 1]
                        || targetY[row] >= targetY[row + 1]) {
                    continue;
                }
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
                        paint);
            }
        }
    }
}
