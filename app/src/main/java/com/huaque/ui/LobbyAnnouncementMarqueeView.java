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
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.View;
import java.util.List;

final class LobbyAnnouncementMarqueeView extends View {
    private static final float DESIGN_WIDTH = 1365f;
    private static final float DESIGN_HEIGHT = 57f;
    private static final float CONTENT_LEFT = 345f;
    private static final float CONTENT_RIGHT = 1345f;
    private static final Rect SPEAKER_SOURCE = new Rect(275, 0, 345, 57);

    private final LobbyAnnouncementMarqueeModel model =
            new LobbyAnnouncementMarqueeModel();
    private final Bitmap artwork;
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint imagePaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private long previousFrameMillis;

    LobbyAnnouncementMarqueeView(Context context) {
        super(context);
        artwork = BitmapFactory.decodeResource(
                getResources(), R.drawable.lobby_announcement);
        backgroundPaint.setShader(
                new LinearGradient(
                        0f,
                        0f,
                        DESIGN_WIDTH,
                        0f,
                        new int[]{
                                Color.TRANSPARENT,
                                0x6B000000,
                                0x6B000000,
                                Color.TRANSPARENT
                        },
                        new float[]{0f, 0.18f, 0.82f, 1f},
                        Shader.TileMode.CLAMP));
        linePaint.setShader(
                new LinearGradient(
                        0f,
                        0f,
                        DESIGN_WIDTH,
                        0f,
                        new int[]{
                                Color.TRANSPARENT,
                                0x80FFFFFF,
                                0x80FFFFFF,
                                Color.TRANSPARENT
                        },
                        new float[]{0f, 0.18f, 0.82f, 1f},
                        Shader.TileMode.CLAMP));
        imagePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        textPaint.setColor(0xFFF2F2F2);
        textPaint.setTextSize(34f);
        textPaint.setTypeface(
                Typeface.createFromAsset(
                        context.getAssets(), "fonts/nanbei_lihei.ttf"));
        textPaint.setShadowLayer(1.5f, 1f, 1f, 0x99000000);
    }

    void setAnnouncements(List<String> messages) {
        model.setMessages(messages, CONTENT_RIGHT - CONTENT_LEFT);
        previousFrameMillis = 0L;
        setContentDescription(String.join("；", messages));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        int checkpoint = canvas.save();
        canvas.scale(getWidth() / DESIGN_WIDTH, getHeight() / DESIGN_HEIGHT);
        canvas.drawRect(0f, 0f, DESIGN_WIDTH, DESIGN_HEIGHT, backgroundPaint);
        canvas.drawRect(0f, 1f, DESIGN_WIDTH, 2f, linePaint);
        canvas.drawRect(0f, 55f, DESIGN_WIDTH, 56f, linePaint);
        if (artwork != null) {
            canvas.drawBitmap(artwork, SPEAKER_SOURCE, SPEAKER_SOURCE, imagePaint);
        }

        String message = model.currentMessage();
        if (!message.isEmpty()) {
            long now = SystemClock.uptimeMillis();
            float elapsedSeconds = previousFrameMillis == 0L
                    ? 0f
                    : Math.min(0.1f, (now - previousFrameMillis) / 1_000f);
            previousFrameMillis = now;
            float textWidth = textPaint.measureText(message);
            model.advance(
                    elapsedSeconds,
                    textWidth,
                    CONTENT_RIGHT - CONTENT_LEFT);
            float baseline = (DESIGN_HEIGHT - textPaint.ascent() - textPaint.descent()) / 2f;
            int textCheckpoint = canvas.save();
            canvas.clipRect(CONTENT_LEFT, 0f, CONTENT_RIGHT, DESIGN_HEIGHT);
            canvas.drawText(
                    message,
                    CONTENT_LEFT + model.currentX(),
                    baseline,
                    textPaint);
            canvas.restoreToCount(textCheckpoint);
            postInvalidateOnAnimation();
        } else {
            previousFrameMillis = 0L;
        }
        canvas.restoreToCount(checkpoint);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        previousFrameMillis = 0L;
    }
}
