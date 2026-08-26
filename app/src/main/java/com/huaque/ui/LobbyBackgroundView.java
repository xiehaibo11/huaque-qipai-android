package com.huaque.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

final class LobbyBackgroundView extends View {
    private final Bitmap bitmap;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    LobbyBackgroundView(Context context) {
        super(context);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lobby_bg);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap == null || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        int[] mapped = LobbyViewportModel.map(
                getWidth(), getHeight(), 0, 0, 1920, 1080);
        RectF content = new RectF(
                mapped[0],
                mapped[1],
                mapped[0] + mapped[2],
                mapped[1] + mapped[3]);
        RectF screen = new RectF(0f, 0f, getWidth(), getHeight());
        canvas.drawBitmap(bitmap, null, screen, paint);
        if (content.top <= 0f && content.bottom >= getHeight()) {
            return;
        }

        float fade = Math.min(content.height() * 0.1f, 120f);
        float fadeRatio = fade / content.height();
        int checkpoint = canvas.saveLayer(content, null);
        canvas.drawBitmap(bitmap, null, content, paint);
        maskPaint.setShader(new LinearGradient(
                0f,
                content.top,
                0f,
                content.bottom,
                new int[]{Color.TRANSPARENT, Color.WHITE, Color.WHITE, Color.TRANSPARENT},
                new float[]{0f, fadeRatio, 1f - fadeRatio, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawRect(content, maskPaint);
        maskPaint.setShader(null);
        canvas.restoreToCount(checkpoint);
    }
}
