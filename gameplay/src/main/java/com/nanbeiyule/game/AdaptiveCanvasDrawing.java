package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;

/** Canvas helpers that apply transforms produced by {@link AdaptiveViewport}. */
final class AdaptiveCanvasDrawing {
    private AdaptiveCanvasDrawing() {}

    static void drawFullBleedBitmap(
            Canvas canvas,
            Bitmap bitmap,
            Paint paint,
            AdaptiveViewport viewport,
            float contentWidth,
            float contentHeight) {
        AdaptiveViewport.Transform transform =
                viewport.fullBleedCover(contentWidth, contentHeight);
        drawTransformedBitmap(
                canvas,
                bitmap,
                paint,
                transform,
                viewport.viewportWidth(),
                viewport.viewportHeight(),
                contentWidth,
                contentHeight);
    }

    static void drawTransformedBitmap(
            Canvas canvas,
            Bitmap bitmap,
            Paint paint,
            AdaptiveViewport.Transform transform,
            float viewportWidth,
            float viewportHeight,
            float contentWidth,
            float contentHeight) {
        BitmapShader shader =
                new BitmapShader(
                        bitmap,
                        Shader.TileMode.MIRROR,
                        Shader.TileMode.MIRROR);
        Matrix matrix = new Matrix();
        matrix.setScale(
                transform.scaleX() * contentWidth / bitmap.getWidth(),
                transform.scaleY() * contentHeight / bitmap.getHeight());
        matrix.postTranslate(
                transform.offsetX(),
                transform.offsetY());
        shader.setLocalMatrix(matrix);

        Shader previousShader = paint.getShader();
        paint.setShader(shader);
        canvas.drawRect(
                0.0f,
                0.0f,
                viewportWidth,
                viewportHeight,
                paint);
        paint.setShader(previousShader);
    }

    static int apply(Canvas canvas, AdaptiveViewport.Transform transform) {
        int saveCount = canvas.save();
        canvas.translate(transform.offsetX(), transform.offsetY());
        canvas.scale(transform.scaleX(), transform.scaleY());
        return saveCount;
    }
}
