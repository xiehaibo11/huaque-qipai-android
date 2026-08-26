package com.nanbeiyule.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

/** Draws the recovered Zhejiang-style membership privilege header effects. */
final class MembershipCenterHeaderRenderer {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint textPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);

    MembershipCenterHeaderRenderer() {
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    void drawSuperMembershipTitleBacking(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new RadialGradient(
                170.0f,
                152.0f,
                190.0f,
                new int[] {
                    Color.argb(190, 109, 180, 255),
                    Color.argb(120, 42, 100, 218),
                    Color.argb(0, 42, 100, 218)
                },
                new float[] {0.0f, 0.56f, 1.0f},
                Shader.TileMode.CLAMP));
        canvas.drawOval(new RectF(0.0f, 93.0f, 358.0f, 210.0f), paint);
        paint.setShader(new LinearGradient(
                28.0f,
                105.0f,
                335.0f,
                194.0f,
                Color.argb(150, 82, 151, 243),
                Color.argb(45, 20, 62, 170),
                Shader.TileMode.CLAMP));
        canvas.drawRoundRect(
                new RectF(15.0f, 108.0f, 346.0f, 199.0f), 42.0f, 42.0f, paint);
        paint.setShader(new LinearGradient(
                32.0f,
                114.0f,
                332.0f,
                114.0f,
                new int[] {
                    Color.argb(0, 255, 255, 255),
                    Color.argb(120, 255, 255, 224),
                    Color.argb(0, 255, 255, 255)
                },
                new float[] {0.0f, 0.5f, 1.0f},
                Shader.TileMode.CLAMP));
        canvas.drawRoundRect(
                new RectF(30.0f, 116.0f, 326.0f, 139.0f), 18.0f, 18.0f, paint);
        paint.setShader(null);
    }

    void drawGlowingSuperMembershipTitle(Canvas canvas) {
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(74.0f);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(9.0f);
        textPaint.setColor(Color.rgb(58, 102, 198));
        textPaint.setShadowLayer(14.0f, 0.0f, 0.0f, Color.rgb(122, 196, 255));
        canvas.drawText("超值会员", 30.0f, 174.0f, textPaint);
        textPaint.clearShadowLayer();
        textPaint.setStrokeWidth(3.5f);
        textPaint.setColor(Color.rgb(230, 247, 255));
        canvas.drawText("超值会员", 30.0f, 174.0f, textPaint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setShader(new LinearGradient(
                30.0f,
                100.0f,
                30.0f,
                184.0f,
                Color.rgb(255, 255, 238),
                Color.rgb(255, 246, 159),
                Shader.TileMode.CLAMP));
        textPaint.setShadowLayer(5.0f, 3.0f, 4.0f, Color.rgb(41, 86, 189));
        canvas.drawText("超值会员", 30.0f, 174.0f, textPaint);
        textPaint.setShader(null);
        textPaint.clearShadowLayer();
        textPaint.setStrokeWidth(0.0f);
    }

    void drawGlowingNumberText(
            Canvas canvas, String value, float x, float baseline, float size) {
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(size);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setShader(new LinearGradient(
                x,
                baseline - size,
                x,
                baseline,
                Color.rgb(255, 255, 196),
                Color.rgb(196, 255, 93),
                Shader.TileMode.CLAMP));
        textPaint.setShadowLayer(10.0f, 0.0f, 0.0f, Color.rgb(232, 255, 118));
        canvas.drawText(value, x, baseline, textPaint);
        textPaint.setShader(null);
        textPaint.clearShadowLayer();
    }
}
