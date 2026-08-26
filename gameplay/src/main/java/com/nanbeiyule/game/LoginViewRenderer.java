package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.widget.Toast;

/**
 * Draws the 南北娱乐 login page with controls recovered from the original Zhejiang client.
 *
 * <p>The background is the selected 南北娱乐 artwork. Buttons, service, region, agreement, and
 * age-rating graphics are unmodified sprite frames recovered from {@code LoginScene.csb} and its
 * original Cocos atlases.
 */

abstract class LoginViewRenderer extends LoginViewState {
    protected LoginViewRenderer(Context context) {
        super(context);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        LoginViewportLayout layout =
                LoginViewportLayout.calculate(
                        getWidth(),
                        getHeight(),
                        adaptiveSafeInsets());
        AdaptiveViewport viewport = layout.adaptiveViewport();
        AdaptiveCanvasDrawing.drawFullBleedBitmap(
                canvas,
                background,
                bitmapPaint,
                viewport,
                PAGE_WIDTH,
                PAGE_HEIGHT);

        int saveCount =
                AdaptiveCanvasDrawing.apply(
                        canvas,
                        viewport.designTransform());

        drawSafeEdgeBitmap(
                canvas,
                viewport,
                customerService,
                1800.0f,
                972.0f,
                88.0f,
                107.0f);
        drawCsbBitmap(canvas, gpsBackground, 960.0f, 523.0f, 840.0f, 73.0f);
        drawRegion(canvas);

        drawCsbBitmap(canvas, oneTapPlate, 497.088f, BUTTON_Y, 428.0f, 163.0f);
        drawCsbBitmap(canvas, oneTapLabel, 503.088f, BUTTON_Y - 1.5f, 338.0f, 62.0f);
        drawCsbBitmap(canvas, phoneButton, 972.096f, BUTTON_Y, 428.0f, 163.0f);
        drawCsbBitmap(canvas, wechatButton, 1448.064f, BUTTON_Y, 428.0f, 163.0f);

        if (!agreementChecked) {
            drawAgreementFrameLight(canvas);
        }
        drawCsbBitmap(
                canvas,
                checkboxFrame,
                CHECKBOX_CENTER_X,
                CHECKBOX_CENTER_Y,
                104.0f,
                106.0f);
        if (agreementChecked) {
            drawCsbBitmap(
                    canvas,
                    checkboxCheck,
                    CHECKBOX_CENTER_X,
                    CHECKBOX_CENTER_Y,
                    84.0f,
                    84.0f);
        }
        drawAgreement(canvas);

        drawSafeEdgeBitmap(
                canvas,
                viewport,
                ageRating,
                1737.7896f,
                143.9973f,
                160.0f,
                207.0f);
        drawNotices(canvas);
        canvas.restoreToCount(saveCount);
        if (!agreementChecked && isAttachedToWindow()) {
            postInvalidateOnAnimation();
        }
    }
    protected void drawRegion(Canvas canvas) {
        String region =
                getResources()
                        .getString(
                                R.string.login_region_format,
                                selectedRegionName);
        regionPaint.setTextAlign(Paint.Align.CENTER);
        drawCenteredText(canvas, region, csbX(790.0f), csbTop(525.0f), regionPaint);

        drawCsbBitmap(canvas, gpsLine, 880.0f, 503.0f, 82.0f, 3.0f);
        drawCsbBitmap(canvas, location, 941.5f, 522.0f, 41.0f, 66.0f);
        drawCsbBitmap(canvas, chooseArea, 1132.5f, 521.0f, 323.0f, 44.0f);
    }

    protected void drawAgreement(Canvas canvas) {
        float checkboxOriginX = CHECKBOX_CENTER_X - 53.0f;
        float agreementCenterY = CHECKBOX_CENTER_Y - 53.0f + 58.0f;
        drawCsbBitmapFromLeft(
                canvas, agreementRead, checkboxOriginX + 118.0f, agreementCenterY);
        drawCsbBitmapFromLeft(
                canvas, agreementService, checkboxOriginX + 346.0f, agreementCenterY);
        drawCsbBitmapFromLeft(
                canvas, agreementAnd, checkboxOriginX + 548.0f, agreementCenterY);
        drawCsbBitmapFromLeft(
                canvas, agreementParent, checkboxOriginX + 598.0f, agreementCenterY);
        drawCsbBitmapFromLeft(
                canvas, agreementPrivacy, checkboxOriginX + 877.0f, agreementCenterY);
    }

    protected void drawAgreementFrameLight(Canvas canvas) {
        long elapsedMillis =
                SystemClock.uptimeMillis() - agreementLightStartedMillis;
        lightPaint.setAlpha(agreementLightAlpha(elapsedMillis));
        drawCsbBitmap(
                canvas,
                loginFrameLight,
                CHECKBOX_CENTER_X,
                CHECKBOX_CENTER_Y,
                150.0f,
                150.0f,
                lightPaint);
    }

    static int agreementLightAlpha(long elapsedMillis) {
        long cycleMillis = Math.floorMod(elapsedMillis, 2000L);
        float progress =
                cycleMillis < 1000L
                        ? 1.0f - cycleMillis / 1000.0f
                        : (cycleMillis - 1000L) / 1000.0f;
        return Math.round(255.0f * progress);
    }

    protected void drawNotices(Canvas canvas) {
        drawNotice(canvas, R.string.healthy_game_title, 24.0f, 172.0f);
        drawNotice(canvas, R.string.healthy_game_line_one, 20.0f, 139.0f);
        drawNotice(canvas, R.string.healthy_game_line_two, 20.0f, 112.0f);
        drawNotice(canvas, R.string.healthy_game_age_notice, 17.0f, 79.0f);
    }

    protected void drawNotice(Canvas canvas, int stringId, float size, float csbY) {
        noticePaint.setTextSize(size * CSB_SCALE_Y);
        drawCenteredText(
                canvas,
                getResources().getString(stringId),
                csbX(956.5f),
                csbTop(csbY),
                noticePaint);
    }

    protected void drawCsbBitmapFromLeft(
            Canvas canvas, Bitmap bitmap, float leftX, float centerY) {
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        drawCsbBitmap(canvas, bitmap, leftX + width / 2.0f, centerY, width, height);
    }

    protected void drawCsbBitmap(
            Canvas canvas,
            Bitmap bitmap,
            float centerX,
            float centerYFromBottom,
            float width,
            float height) {
        drawCsbBitmap(
                canvas,
                bitmap,
                centerX,
                centerYFromBottom,
                width,
                height,
                bitmapPaint);
    }

    protected void drawCsbBitmap(
            Canvas canvas,
            Bitmap bitmap,
            float centerX,
            float centerYFromBottom,
            float width,
            float height,
            Paint paint) {
        float targetCenterX = csbX(centerX);
        float targetCenterY = csbTop(centerYFromBottom);
        float targetWidth = width * CSB_SCALE_X;
        float targetHeight = height * CSB_SCALE_Y;
        canvas.drawBitmap(
                bitmap,
                null,
                new RectF(
                        targetCenterX - targetWidth / 2.0f,
                        targetCenterY - targetHeight / 2.0f,
                        targetCenterX + targetWidth / 2.0f,
                        targetCenterY + targetHeight / 2.0f),
                paint);
    }

    protected void drawSafeEdgeBitmap(
            Canvas canvas,
            AdaptiveViewport viewport,
            Bitmap bitmap,
            float centerX,
            float centerYFromBottom,
            float width,
            float height) {
        int saveCount = canvas.save();
        canvas.translate(
                viewport.safeEdgeOffsetX(csbX(centerX)),
                0.0f);
        drawCsbBitmap(
                canvas,
                bitmap,
                centerX,
                centerYFromBottom,
                width,
                height);
        canvas.restoreToCount(saveCount);
    }

    protected void showPlaceholder(int labelStringId) {
        String label = getResources().getString(labelStringId);
        String message =
                getResources().getString(R.string.login_action_placeholder, label);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    protected Bitmap loadBitmap(int resourceId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        if (bitmap == null) {
            throw new IllegalStateException("Unable to decode drawable " + resourceId);
        }
        return bitmap;
    }

    protected static RectF csbRect(
            float centerX,
            float centerYFromBottom,
            float width,
            float height) {
        float pageCenterX = csbX(centerX);
        float pageCenterY = csbTop(centerYFromBottom);
        float pageWidth = width * CSB_SCALE_X;
        float pageHeight = height * CSB_SCALE_Y;
        return new RectF(
                pageCenterX - pageWidth / 2.0f,
                pageCenterY - pageHeight / 2.0f,
                pageCenterX + pageWidth / 2.0f,
                pageCenterY + pageHeight / 2.0f);
    }

    protected static float csbX(float value) {
        return value * CSB_SCALE_X;
    }

    protected static float csbTop(float valueFromBottom) {
        return (CSB_HEIGHT - valueFromBottom) * CSB_SCALE_Y;
    }

    protected static void drawCenteredText(
            Canvas canvas, String text, float centerX, float centerY, Paint paint) {
        Paint.FontMetrics metrics = paint.getFontMetrics();
        float baseline = centerY - (metrics.ascent + metrics.descent) / 2.0f;
        canvas.drawText(text, centerX, baseline, paint);
    }
}
