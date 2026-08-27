package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

final class TaizhouVoiceLoadOverlayRenderer {
    private static final float CENTER_X = 960.0f;
    private static final float CENTER_Y = 630.0f;
    private static final float WIDTH = 620.0f;
    private static final float HEIGHT = 82.0f;
    private static final Rect BG_FRAME = new Rect(2, 2, 84, 84);
    private static final Rect BAR_BACK_FRAME = new Rect(86, 19, 611, 34);
    private static final Rect BAR_FILL_FRAME = new Rect(86, 2, 611, 17);
    private static final float BAR_LEFT = 47.5f;
    private static final float BAR_TOP = 46.5f;
    private static final float BAR_WIDTH = 525.0f;
    private static final float BAR_HEIGHT = 15.0f;
    private static final int TEXT_COLOR = Color.rgb(123, 241, 165);
    private static final int OUTLINE_COLOR = Color.RED;

    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Bitmap background;
    private final Bitmap progressBack;
    private final Bitmap progressFill;

    TaizhouVoiceLoadOverlayRenderer(Context context) {
        Bitmap atlas =
                BitmapFactory.decodeResource(
                        context.getResources(), R.drawable.taizhou_game_sound_update);
        background = Bitmap.createBitmap(atlas, BG_FRAME.left, BG_FRAME.top, BG_FRAME.width(), BG_FRAME.height());
        progressBack =
                Bitmap.createBitmap(
                        atlas,
                        BAR_BACK_FRAME.left,
                        BAR_BACK_FRAME.top,
                        BAR_BACK_FRAME.width(),
                        BAR_BACK_FRAME.height());
        progressFill =
                Bitmap.createBitmap(
                        atlas,
                        BAR_FILL_FRAME.left,
                        BAR_FILL_FRAME.top,
                        BAR_FILL_FRAME.width(),
                        BAR_FILL_FRAME.height());
        textPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/fangzhengcuyuan.ttf"));
    }

    void draw(Canvas canvas, TaizhouVoiceLoadProgress progress) {
        if (progress == null || !progress.visible()) {
            return;
        }
        RectF box =
                new RectF(
                        CENTER_X - WIDTH / 2.0f,
                        CENTER_Y - HEIGHT / 2.0f,
                        CENTER_X + WIDTH / 2.0f,
                        CENTER_Y + HEIGHT / 2.0f);
        TaizhouNineSlice.draw(canvas, background, bitmapPaint, box, 27, 27, 28, 28, 1.0f);
        RectF bar =
                new RectF(
                        box.left + BAR_LEFT,
                        box.top + BAR_TOP,
                        box.left + BAR_LEFT + BAR_WIDTH,
                        box.top + BAR_TOP + BAR_HEIGHT);
        TaizhouNineSlice.draw(canvas, progressBack, bitmapPaint, bar, 173, 4, 179, 7, 1.0f);
        int save = canvas.save();
        canvas.clipRect(bar.left, bar.top, bar.left + bar.width() * progress.percent() / 100.0f, bar.bottom);
        canvas.drawBitmap(progressFill, null, bar, bitmapPaint);
        canvas.restoreToCount(save);
        float textCenterY = box.bottom - 56.0f;
        drawOutlinedText(canvas, "(" + progress.percent() + "%)", box.left + 217.0f, textCenterY, Paint.Align.RIGHT);
        drawOutlinedText(canvas, "语音正在加载中", box.left + 220.0f, textCenterY, Paint.Align.LEFT);
    }

    private void drawOutlinedText(Canvas canvas, String text, float x, float centerY, Paint.Align align) {
        textPaint.setTextSize(28.0f);
        textPaint.setTextAlign(align);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseline = centerY - (metrics.ascent + metrics.descent) * 0.5f;
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(1.0f);
        textPaint.setColor(OUTLINE_COLOR);
        canvas.drawText(text, x, baseline, textPaint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(TEXT_COLOR);
        canvas.drawText(text, x, baseline, textPaint);
    }
}
