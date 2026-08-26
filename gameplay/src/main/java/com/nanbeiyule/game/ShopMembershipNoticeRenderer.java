package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

/** Draws the original ShopLayer.csb VIP notice footer without guessed replacement art. */
final class ShopMembershipNoticeRenderer {
    private static final float TEXT_SIZE = 30f;
    private static final int TEXT_COLOR = Color.rgb(212, 100, 51);

    private final Bitmap helpBitmap;
    private final Bitmap linkBitmap;
    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();

    ShopMembershipNoticeRenderer(ShopDrawableSet drawables) {
        helpBitmap = drawables.membershipNoticeHelp;
        linkBitmap = drawables.membershipNoticeLink;
        textPaint.setTypeface(
                Typeface.createFromAsset(
                        drawables.resources().getAssets(), "fonts/fangzhengcuyuan.ttf"));
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setColor(TEXT_COLOR);
    }

    void drawMembershipNotice(Canvas canvas) {
        drawBitmap(canvas, helpBitmap, ShopMembershipNoticeLayout.HELP);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseline =
                ShopMembershipNoticeLayout.TEXT.centerY()
                        - (metrics.ascent + metrics.descent) * 0.5f;
        canvas.drawText(
                ShopMembershipNoticeLayout.PREFIX,
                ShopMembershipNoticeLayout.TEXT.left(),
                baseline,
                textPaint);
        drawBitmap(canvas, linkBitmap, ShopMembershipNoticeLayout.LINK);
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, ShopLayout.Rect bounds) {
        source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        destination.set(bounds.left(), bounds.top(), bounds.right(), bounds.bottom());
        canvas.drawBitmap(bitmap, source, destination, bitmapPaint);
    }
}
