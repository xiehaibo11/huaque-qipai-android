package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

/** Draws one 400x740 card from the original VipWelfareItem.csd coordinates. */
final class GoldMembershipCardRenderer {
    private static final float EXPLAIN_TOP = 397.32f;
    private static final float ACTIVE_EXPLAIN_TOP = EXPLAIN_TOP - 260.0f;

    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();
    private final Bitmap itemBackground;
    private final Bitmap week;
    private final Bitmap month;
    private final Bitmap weekSmall;
    private final Bitmap monthSmall;
    private final Bitmap explainBackground;
    private final Bitmap openButton;
    private final Bitmap receiveButton;
    private final Bitmap renewButton;
    private final ColorMatrixColorFilter disabledFilter;

    GoldMembershipCardRenderer(Resources resources, Typeface typeface) {
        textPaint.setTypeface(typeface == null ? Typeface.DEFAULT_BOLD : typeface);
        itemBackground = bitmap(resources, R.drawable.vip_welfare_item_bg);
        week = bitmap(resources, R.drawable.vip_welfare_week);
        month = bitmap(resources, R.drawable.vip_welfare_month);
        weekSmall = bitmap(resources, R.drawable.vip_welfare_week_small);
        monthSmall = bitmap(resources, R.drawable.vip_welfare_month_small);
        explainBackground = bitmap(resources, R.drawable.vip_welfare_explain_bg);
        openButton = bitmap(resources, R.drawable.vip_welfare_button_open);
        receiveButton = bitmap(resources, R.drawable.vip_welfare_button_receive);
        renewButton = bitmap(resources, R.drawable.vip_welfare_button_renew);
        ColorMatrix disabled = new ColorMatrix();
        disabled.setSaturation(0.2f);
        disabledFilter = new ColorMatrixColorFilter(disabled);
    }

    void draw(
            Canvas canvas,
            GoldMembershipCardsState.Card card,
            GoldMembershipCardsLayout.Bounds bounds,
            boolean loading) {
        float left = bounds.left();
        float top = bounds.top();
        int save = canvas.save();
        canvas.clipRect(bounds.left(), bounds.top(), bounds.right(), bounds.bottom());
        drawBitmap(canvas, itemBackground, rect(left, top, 400.0f, 740.0f));
        drawProduct(canvas, card, left, top);
        drawExplanation(canvas, card, left, top);
        drawActions(canvas, card, left, top, loading);
        canvas.restoreToCount(save);
    }

    private void drawProduct(
            Canvas canvas, GoldMembershipCardsState.Card card, float left, float top) {
        Bitmap fullProduct = fullProduct(card.productCode());
        Bitmap smallProduct = smallProduct(card.productCode());
        if (card.isActive()) {
            drawBitmap(
                    canvas,
                    smallProduct,
                    rect(left + 20.5f, top + 12.0f, 359.0f, 167.0f));
            drawOutlinedText(
                    canvas,
                    "续费+" + card.durationDays() + "天",
                    left + 200.0f,
                    top + 132.0f,
                    26.0f,
                    Color.WHITE,
                    Color.rgb(117, 50, 54));
        } else {
            drawBitmap(
                    canvas,
                    fullProduct,
                    rect(left + 23.0f, top + 12.0f, 354.0f, 382.0f));
        }
    }

    private void drawExplanation(
            Canvas canvas, GoldMembershipCardsState.Card card, float left, float top) {
        float panelTop = top + (card.isActive() ? ACTIVE_EXPLAIN_TOP : EXPLAIN_TOP);
        drawHorizontalThreeSlice(
                canvas,
                explainBackground,
                rect(left + 25.0f, panelTop, 350.0f, 167.0f),
                21);
        drawText(
                canvas,
                "权益说明：",
                left + 49.5f,
                panelTop + 37.0f,
                28.0f,
                Color.rgb(202, 79, 59),
                Paint.Align.LEFT);
        String coins = ZhejiangLobbyAmountFormatter.format(card.dailyCoins());
        drawText(canvas, "每天领取金币" + coins, left + 43.83f,
                panelTop + 83.0f, 28.0f, Color.rgb(191, 108, 27), Paint.Align.LEFT);
        drawText(canvas, "错过无法补领，记得每天", left + 43.83f,
                panelTop + 116.0f, 28.0f, Color.rgb(191, 108, 27), Paint.Align.LEFT);
        drawText(canvas, "来打卡哟~", left + 43.83f,
                panelTop + 149.0f, 28.0f, Color.rgb(191, 108, 27), Paint.Align.LEFT);
    }

    private void drawActions(
            Canvas canvas,
            GoldMembershipCardsState.Card card,
            float left,
            float top,
            boolean loading) {
        if (!card.isActive()) {
            drawBitmap(canvas, openButton, rect(left + 63.0f, top + 575.21f, 274.0f, 102.0f));
            return;
        }
        drawBitmap(canvas, renewButton, rect(left + 63.0f, top + 459.0f, 274.0f, 102.0f));
        boolean disabled = loading || card.state() == GoldMembershipCardsState.CardState.HAS_AWARD;
        drawBitmap(
                canvas,
                receiveButton,
                rect(left + 61.5f, top + 575.21f, 277.0f, 103.0f),
                disabled);
        drawText(
                canvas,
                remainingText(card.remainingSeconds()),
                left + 200.0f,
                top + 716.0f,
                30.0f,
                Color.rgb(197, 79, 96),
                Paint.Align.CENTER);
    }

    private Bitmap fullProduct(String productCode) {
        return switch (productCode) {
            case "GOLD_MEMBER_WEEK" -> week;
            case "GOLD_MEMBER_MONTH" -> month;
            default -> null;
        };
    }

    private Bitmap smallProduct(String productCode) {
        return switch (productCode) {
            case "GOLD_MEMBER_WEEK" -> weekSmall;
            case "GOLD_MEMBER_MONTH" -> monthSmall;
            default -> null;
        };
    }

    private static String remainingText(long remainingSeconds) {
        if (remainingSeconds < 3_600L) {
            return "即将到期";
        }
        long days = remainingSeconds / 86_400L;
        if (days < 1L) {
            return "剩余" + remainingSeconds / 3_600L + "小时";
        }
        return "剩余" + days + "天";
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, RectF bounds) {
        drawBitmap(canvas, bitmap, bounds, false);
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, RectF bounds, boolean disabled) {
        if (bitmap == null || bitmap.isRecycled()) return;
        source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        destination.set(bounds);
        bitmapPaint.setAlpha(disabled ? 135 : 255);
        bitmapPaint.setColorFilter(disabled ? disabledFilter : null);
        canvas.drawBitmap(bitmap, source, destination, bitmapPaint);
        bitmapPaint.setAlpha(255);
        bitmapPaint.setColorFilter(null);
    }

    private void drawHorizontalThreeSlice(
            Canvas canvas, Bitmap bitmap, RectF bounds, int edgeWidth) {
        if (bitmap == null || bitmap.isRecycled()) return;
        int rightSource = bitmap.getWidth() - edgeWidth;
        float rightDestination = bounds.right - edgeWidth;
        drawSlice(canvas, bitmap, 0, edgeWidth, bounds.left, bounds.left + edgeWidth, bounds);
        drawSlice(
                canvas,
                bitmap,
                edgeWidth,
                rightSource,
                bounds.left + edgeWidth,
                rightDestination,
                bounds);
        drawSlice(
                canvas,
                bitmap,
                rightSource,
                bitmap.getWidth(),
                rightDestination,
                bounds.right,
                bounds);
    }

    private void drawSlice(
            Canvas canvas,
            Bitmap bitmap,
            int sourceLeft,
            int sourceRight,
            float destinationLeft,
            float destinationRight,
            RectF bounds) {
        source.set(sourceLeft, 0, sourceRight, bitmap.getHeight());
        destination.set(destinationLeft, bounds.top, destinationRight, bounds.bottom);
        bitmapPaint.setAlpha(255);
        bitmapPaint.setColorFilter(null);
        canvas.drawBitmap(bitmap, source, destination, bitmapPaint);
    }

    private void drawText(
            Canvas canvas,
            String text,
            float x,
            float baseline,
            float size,
            int color,
            Paint.Align align) {
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(0.0f);
        textPaint.setTextSize(size);
        textPaint.setTextAlign(align);
        textPaint.setColor(color);
        canvas.drawText(text, x, baseline, textPaint);
    }

    private void drawOutlinedText(
            Canvas canvas, String text, float x, float baseline, float size, int fill, int outline) {
        textPaint.setTextSize(size);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(4.0f);
        textPaint.setColor(outline);
        canvas.drawText(text, x, baseline, textPaint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(0.0f);
        textPaint.setColor(fill);
        canvas.drawText(text, x, baseline, textPaint);
    }

    private static RectF rect(float left, float top, float width, float height) {
        return new RectF(left, top, left + width, top + height);
    }

    private static Bitmap bitmap(Resources resources, int id) {
        return BitmapFactory.decodeResource(resources, id);
    }
}
