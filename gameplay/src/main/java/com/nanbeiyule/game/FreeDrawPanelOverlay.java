package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

final class FreeDrawPanelOverlay {
    private static final float SOURCE_WIDTH = 1082f;
    private static final float SOURCE_HEIGHT = 623f;
    private static final float STRIP_LEFT = 159f;
    private static final float STRIP_TOP = 188f;
    private static final float STRIP_RIGHT = 891f;
    private static final float STRIP_BOTTOM = 411f;
    private static final float FIRST_CARD_CENTER = 194f;
    private static final float CARD_PITCH = 175f;
    private static final float CARD_WIDTH = 162f;
    private static final float CARD_HEIGHT = 220f;
    private static final long STEP_DURATION_MS = 1_500L;

    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint buttonText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint prizeText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Bitmap coinBag;
    private final Bitmap diamond;
    private final Bitmap video;
    private final Bitmap emptyStrip;
    private final Bitmap prizeCard;

    FreeDrawPanelOverlay(Context context) {
        coinBag = BitmapFactory.decodeResource(context.getResources(), R.drawable.shop_product_coin_bag);
        diamond = BitmapFactory.decodeResource(context.getResources(), R.drawable.shop_product_diamond);
        video = BitmapFactory.decodeResource(context.getResources(), R.drawable.original_free_draw_video);
        emptyStrip =
                BitmapFactory.decodeResource(
                        context.getResources(), R.drawable.original_free_draw_empty_strip);
        prizeCard =
                BitmapFactory.decodeResource(
                        context.getResources(), R.drawable.original_free_draw_prize_card);
        Typeface typeface =
                Typeface.createFromAsset(context.getAssets(), "fonts/zihun_jingdian_lihei.ttf");
        buttonText.setColor(Color.rgb(126, 53, 16));
        buttonText.setTextAlign(Paint.Align.CENTER);
        buttonText.setTypeface(typeface);
        prizeText.setColor(Color.rgb(120, 54, 0));
        prizeText.setTextAlign(Paint.Align.CENTER);
        prizeText.setTypeface(typeface);
    }

    void draw(
            Canvas canvas,
            AdaptiveViewport.Rect bounds,
            FreeDrawState state,
            long animationElapsedMillis) {
        int save = canvas.save();
        canvas.clipRect(bounds.left(), bounds.top(), bounds.right(), bounds.bottom());
        drawButton(canvas, bounds, state);
        if (state != null && !state.prizes().isEmpty()) {
            drawPrizeStrip(canvas, bounds, state, animationElapsedMillis);
        }
        canvas.restoreToCount(save);
    }

    private void drawPrizeStrip(
            Canvas canvas,
            AdaptiveViewport.Rect bounds,
            FreeDrawState state,
            long animationElapsedMillis) {
        RectF strip = rect(bounds, STRIP_LEFT, STRIP_TOP, STRIP_RIGHT, STRIP_BOTTOM);
        int save = canvas.save();
        canvas.clipRect(strip);
        canvas.drawBitmap(emptyStrip, null, strip, bitmapPaint);

        int prizeCount = state.prizes().size();
        float sourceX = FIRST_CARD_CENTER - scrollOffset(animationElapsedMillis, prizeCount);
        float cycleWidth = prizeCount * CARD_PITCH;
        while (sourceX > STRIP_LEFT - CARD_WIDTH * 0.5f) sourceX -= cycleWidth;

        int prizeIndex = 0;
        while (sourceX + CARD_WIDTH * 0.5f < STRIP_LEFT) {
            sourceX += CARD_PITCH;
            prizeIndex = (prizeIndex + 1) % prizeCount;
        }
        while (sourceX - CARD_WIDTH * 0.5f < STRIP_RIGHT) {
            drawPrize(canvas, bounds, sourceX, state.prizes().get(prizeIndex));
            sourceX += CARD_PITCH;
            prizeIndex = (prizeIndex + 1) % prizeCount;
        }
        canvas.restoreToCount(save);
    }

    private void drawButton(Canvas canvas, AdaptiveViewport.Rect bounds, FreeDrawState state) {
        canvas.drawBitmap(video, null, rect(bounds, 428f, 540f, 468f, 580f), bitmapPaint);
        drawCenteredText(canvas, bounds, "免费抽奖", 467f, 527f, 637f, 591f, 42f);
        if (state != null) {
            drawCenteredText(
                    canvas, bounds, displayCount(state.remainingDraws()), 476f, 575f, 586f, 619f, 24f);
        }
    }

    private void drawPrize(
            Canvas canvas,
            AdaptiveViewport.Rect bounds,
            float sourceCenterX,
            FreeDrawState.Prize prize) {
        RectF card =
                rect(
                        bounds,
                        sourceCenterX - CARD_WIDTH * 0.5f,
                        189f,
                        sourceCenterX + CARD_WIDTH * 0.5f,
                        189f + CARD_HEIGHT);
        canvas.drawBitmap(prizeCard, null, card, bitmapPaint);

        Bitmap icon = "DIAMOND".equals(prize.type()) ? diamond : coinBag;
        float sourceWidth = "DIAMOND".equals(prize.type()) ? 82f : 100f;
        float sourceHeight = "DIAMOND".equals(prize.type()) ? 66f : 79f;
        RectF target =
                rect(
                        bounds,
                        sourceCenterX - sourceWidth * 0.5f,
                        272f - sourceHeight * 0.5f,
                        sourceCenterX + sourceWidth * 0.5f,
                        272f + sourceHeight * 0.5f);
        canvas.drawBitmap(icon, null, target, bitmapPaint);
        drawPrizeText(canvas, bounds, sourceCenterX, prize.displayName());
    }

    private void drawPrizeText(
            Canvas canvas, AdaptiveViewport.Rect bounds, float sourceCenterX, String text) {
        RectF target =
                rect(
                        bounds,
                        sourceCenterX - CARD_WIDTH * 0.5f,
                        342f,
                        sourceCenterX + CARD_WIDTH * 0.5f,
                        407f);
        float textSize = bounds.height() * 28f / SOURCE_HEIGHT;
        prizeText.setTextSize(textSize);
        float maxWidth = bounds.width() * 148f / SOURCE_WIDTH;
        float measuredWidth = prizeText.measureText(text);
        if (measuredWidth > maxWidth) prizeText.setTextSize(textSize * maxWidth / measuredWidth);
        Paint.FontMetrics metrics = prizeText.getFontMetrics();
        float baseline = target.centerY() - (metrics.ascent + metrics.descent) * 0.5f;
        canvas.drawText(text, target.centerX(), baseline, prizeText);
    }

    static boolean shouldAnimate(int prizeCount) {
        return prizeCount > 4;
    }

    static float scrollOffset(long elapsedMillis, int prizeCount) {
        if (!shouldAnimate(prizeCount)) return 0f;
        long cycleDuration = STEP_DURATION_MS * prizeCount;
        long cycleTime = Math.floorMod(elapsedMillis, cycleDuration);
        return cycleTime * CARD_PITCH / STEP_DURATION_MS;
    }

    static String displayCount(int remaining) {
        return "（" + remaining + "次）";
    }

    private void drawCenteredText(
            Canvas canvas,
            AdaptiveViewport.Rect bounds,
            String text,
            float left,
            float top,
            float right,
            float bottom,
            float sourceTextSize) {
        RectF target = rect(bounds, left, top, right, bottom);
        buttonText.setTextSize(bounds.height() * sourceTextSize / SOURCE_HEIGHT);
        Paint.FontMetrics metrics = buttonText.getFontMetrics();
        float baseline = target.centerY() - (metrics.ascent + metrics.descent) * 0.5f;
        canvas.drawText(text, target.centerX(), baseline, buttonText);
    }

    private static RectF rect(
            AdaptiveViewport.Rect bounds,
            float left,
            float top,
            float right,
            float bottom) {
        return new RectF(
                bounds.left() + left / SOURCE_WIDTH * bounds.width(),
                bounds.top() + top / SOURCE_HEIGHT * bounds.height(),
                bounds.left() + right / SOURCE_WIDTH * bounds.width(),
                bounds.top() + bottom / SOURCE_HEIGHT * bounds.height());
    }
}
