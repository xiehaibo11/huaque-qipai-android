package com.nanbeiyule.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

/** Draws off-screen privilege cards when the original remote icon is unavailable. */
final class MembershipPrivilegeFallbackRenderer {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint textPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);

    MembershipPrivilegeFallbackRenderer() {
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    void draw(Canvas canvas, float x, float y, String title, String description) {
        paint.setColor(Color.rgb(246, 252, 255));
        canvas.drawRoundRect(new RectF(x, y, x + 282, y + 320), 8, 8, paint);
        paint.setColor(Color.rgb(222, 235, 247));
        canvas.drawOval(new RectF(x + 24, y + 60, x + 258, y + 218), paint);
        paint.setColor(Color.rgb(64, 119, 204));
        canvas.drawRoundRect(new RectF(x, y, x + 282, y + 54), 6, 6, paint);
        drawText(canvas, title, x + 141, y + 42, 38, Color.WHITE);
        drawIcon(canvas, x + 141, y + 146);
        drawMultiLine(canvas, description, x + 141, y + 270);
    }

    void drawIcon(Canvas canvas, float cx, float cy) {
        paint.setColor(Color.rgb(232, 185, 78));
        canvas.drawRoundRect(new RectF(cx - 68, cy - 45, cx + 68, cy + 66), 18, 18, paint);
        paint.setColor(Color.rgb(255, 242, 160));
        canvas.drawRect(cx - 10, cy - 45, cx + 10, cy + 66, paint);
    }

    private void drawMultiLine(Canvas canvas, String value, float x, float y) {
        String[] lines = value.split("\\n");
        for (int index = 0; index < lines.length; index++) {
            drawText(canvas, lines[index], x, y + index * 40, 32, Color.rgb(80, 100, 147));
        }
    }

    private void drawText(Canvas canvas, String value, float x, float y, float size, int color) {
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(value, x, y, textPaint);
    }
}
