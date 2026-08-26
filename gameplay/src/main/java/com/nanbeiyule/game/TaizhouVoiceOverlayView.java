package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;

/** Center feedback mounted at VoiceLayer.csb's recording animation position. */
final class TaizhouVoiceOverlayView extends TaizhouToolView {
    private boolean cancelPending;

    TaizhouVoiceOverlayView(Context context) {
        super(context);
        setContentDescription("录音中");
    }

    void setCancelPending(boolean value) {
        cancelPending = value;
        setContentDescription(value ? "松开手指，取消发送" : "录音中，松开发送");
        invalidate();
    }

    @Override
    protected void drawDesign(Canvas canvas) {
        fillPaint.setColor(Color.argb(210, 31, 34, 35));
        canvas.drawRoundRect(new RectF(760, 350, 1160, 730), 28, 28, fillPaint);
        fillPaint.setStyle(android.graphics.Paint.Style.STROKE);
        fillPaint.setStrokeWidth(16);
        fillPaint.setColor(cancelPending ? Color.rgb(239, 103, 68) : Color.rgb(246, 213, 111));
        canvas.drawRoundRect(new RectF(895, 415, 1025, 590), 55, 55, fillPaint);
        canvas.drawLine(960, 590, 960, 640, fillPaint);
        canvas.drawLine(910, 640, 1010, 640, fillPaint);
        fillPaint.setStyle(android.graphics.Paint.Style.FILL);
        drawText(canvas, cancelPending ? "松开手指，取消发送" : "录音中，松开发送",
                960, 690, 34, Color.WHITE);
    }
}
