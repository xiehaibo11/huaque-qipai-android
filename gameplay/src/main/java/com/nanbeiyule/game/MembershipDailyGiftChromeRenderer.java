package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

/** Draws the non-interactive SxvipDailyGiftView chrome from original CSB geometry. */
final class MembershipDailyGiftChromeRenderer {
    private static final float DESIGN_WIDTH = 1920.0f;
    private static final float DESIGN_HEIGHT = 1080.0f;
    private static final float CSB_BODY_LEFT = 433.0f;
    private static final float CSB_BODY_TOP = 75.0f;
    private static final float CSB_BODY_WIDTH = 1440.0f;
    private static final float CSB_BODY_HEIGHT = 930.0f;
    private static final RectF TOP_BACKGROUND_BOUNDS = new RectF(0.0f, 0.0f, DESIGN_WIDTH, 180.0f);
    private static final RectF BACK_PANEL_BOUNDS = new RectF(-0.08f, 4.016f, 147.92f, 126.016f);
    private static final RectF BACK_ARROW_BOUNDS = new RectF(15.16f, 33.516f, 97.16f, 96.516f);
    private static final RectF MEMBER_TITLE_BOUNDS = new RectF(159.425f, 5.016f, 318.425f, 110.016f);
    private static final RectF MEMBER_TITLE_BACKGROUND_BOUNDS =
            new RectF(293.42f, 1.016f, 973.42f, 118.016f);
    private static final RectF TOP_OPEN_BUTTON_BOUNDS = new RectF(1325.0f, 17.0f, 1604.0f, 118.0f);
    private static final RectF LEFT_NAV_BASE_BOUNDS = new RectF(0.0f, 124.0f, 398.0f, DESIGN_HEIGHT);
    private static final RectF LEFT_NAV_PATTERN_BOUNDS = new RectF(0.0f, 429.0f, 704.0f, DESIGN_HEIGHT);
    private static final RectF LEFT_NAV_SELECTED_BOUNDS = new RectF(0.0f, 136.0f, 430.0f, 272.0f);
    private static final float[] LEFT_NAV_SELECTED_TOPS = {136.0f, 246.0f, 395.0f, 544.0f, 693.0f};
    private static final float LEFT_NAV_TEXT_CENTER_X = 192.9979f;
    private static final float[] LEFT_NAV_TEXT_TOPS = {181.0f, 291.0f, 440.0f, 589.0f, 738.0f};
    private static final float[] LEFT_NAV_DIVIDER_TOPS = {382.0f, 531.0f, 680.0f, 829.0f};

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();
    private final Typeface typeface;

    MembershipDailyGiftChromeRenderer(Typeface typeface) {
        this.typeface = typeface == null ? Typeface.DEFAULT_BOLD : typeface;
        textPaint.setTypeface(this.typeface);
    }

    void draw(
            Canvas canvas,
            Bitmap shopBackground,
            Bitmap panelBackground,
            Bitmap topBackground,
            Bitmap backPanel,
            Bitmap backArrow,
            Bitmap memberTitle,
            Bitmap memberTitleBackground,
            Bitmap tipButton,
            Bitmap topOpenButton,
            Bitmap leftNavigationBase,
            Bitmap leftNavigationBackground,
            Bitmap selectedTab,
            Bitmap leftNavigationDivider,
            Bitmap[] navigationTextBitmaps,
            int selectedNavigationIndex) {
        drawBackgroundLayer(canvas, shopBackground, panelBackground, topBackground);
        drawForegroundLayer(canvas, backPanel, backArrow, memberTitle, memberTitleBackground,
                tipButton, topOpenButton, leftNavigationBase, leftNavigationBackground, selectedTab,
                leftNavigationDivider, navigationTextBitmaps, selectedNavigationIndex);
    }

    void drawBackgroundLayer(
            Canvas canvas,
            Bitmap shopBackground,
            Bitmap panelBackground,
            Bitmap topBackground) {
        drawBaseLayer(canvas, topBackground);
        drawPanelBackgroundLayer(canvas, shopBackground, panelBackground);
    }

    void drawBaseLayer(Canvas canvas, Bitmap topBackground) {
        drawBaseFills(canvas, topBackground);
    }

    void drawPanelBackgroundLayer(Canvas canvas, Bitmap shopBackground, Bitmap panelBackground) {
        drawDailyGiftPanelBackground(canvas, shopBackground, panelBackground);
    }

    void drawForegroundLayer(
            Canvas canvas,
            Bitmap backPanel,
            Bitmap backArrow,
            Bitmap memberTitle,
            Bitmap memberTitleBackground,
            Bitmap tipButton,
            Bitmap topOpenButton,
            Bitmap leftNavigationBase,
            Bitmap leftNavigationBackground,
            Bitmap selectedTab,
            Bitmap leftNavigationDivider,
            Bitmap[] navigationTextBitmaps,
            int selectedNavigationIndex) {
        drawOriginalHeaderIcons(canvas, backPanel, backArrow, memberTitle, memberTitleBackground);
        drawHeaderText(canvas);
        drawLeftNavigation(canvas, leftNavigationBase, leftNavigationBackground, selectedTab,
                leftNavigationDivider, navigationTextBitmaps, selectedNavigationIndex);
        if (selectedNavigationIndex == 0) {
            drawTitleAndTip(canvas, tipButton);
        }
        drawOpenMembershipButton(canvas, topOpenButton);
    }

    private void drawBaseFills(Canvas canvas, Bitmap topBackground) {
        paint.setShader(new LinearGradient(
                0.0f, 0.0f, 0.0f, DESIGN_HEIGHT,
                Color.rgb(255, 225, 165), Color.rgb(198, 138, 83), Shader.TileMode.CLAMP));
        canvas.drawRect(0.0f, 0.0f, DESIGN_WIDTH, DESIGN_HEIGHT, paint);
        paint.setShader(null);
        drawBitmap(canvas, topBackground, TOP_BACKGROUND_BOUNDS);
        paint.setColor(Color.argb(76, 255, 255, 255));
        canvas.drawRect(0.0f, TOP_BACKGROUND_BOUNDS.bottom, DESIGN_WIDTH, DESIGN_HEIGHT, paint);
    }

    private void drawOriginalHeaderIcons(
            Canvas canvas,
            Bitmap backPanel,
            Bitmap backArrow,
            Bitmap memberTitle,
            Bitmap memberTitleBackground) {
        drawBitmap(canvas, backPanel, BACK_PANEL_BOUNDS);
        drawBitmap(canvas, memberTitleBackground, MEMBER_TITLE_BACKGROUND_BOUNDS);
        drawBitmap(canvas, backArrow, BACK_ARROW_BOUNDS);
        drawBitmap(canvas, memberTitle, MEMBER_TITLE_BOUNDS);
    }

    private void drawHeaderText(Canvas canvas) {
        drawText(canvas, "15个特权", 360.0f, 68.0f, 30.0f, Color.rgb(118, 52, 255), Paint.Align.LEFT);
        drawText(canvas, "等您开启", 531.0f, 68.0f, 30.0f, Color.rgb(121, 84, 43), Paint.Align.LEFT);
    }

    private void drawLeftNavigation(
            Canvas canvas,
            Bitmap leftNavigationBase,
            Bitmap leftNavigationBackground,
            Bitmap selectedTab,
            Bitmap leftNavigationDivider,
            Bitmap[] navigationTextBitmaps,
            int selectedNavigationIndex) {
        drawBitmap(canvas, leftNavigationBase, LEFT_NAV_BASE_BOUNDS);
        drawBitmap(canvas, leftNavigationBackground, LEFT_NAV_PATTERN_BOUNDS);
        float selectedTop = selectedNavigationIndex >= 0
                && selectedNavigationIndex < LEFT_NAV_SELECTED_TOPS.length
                ? LEFT_NAV_SELECTED_TOPS[selectedNavigationIndex]
                : LEFT_NAV_SELECTED_TOPS[0];
        RectF selectedBounds = selectedNavigationIndex == 0
                ? LEFT_NAV_SELECTED_BOUNDS
                : new RectF(0.0f, selectedTop, 430.0f, selectedTop + 136.0f);
        drawBitmap(canvas, selectedTab, selectedBounds);
        for (int index = 0; index < navigationTextBitmaps.length && index < LEFT_NAV_TEXT_TOPS.length; index++) {
            Bitmap textBitmap = navigationTextBitmaps[index];
            if (textBitmap != null) {
                drawBitmap(canvas, textBitmap, new RectF(
                        LEFT_NAV_TEXT_CENTER_X - textBitmap.getWidth() / 2.0f,
                        LEFT_NAV_TEXT_TOPS[index],
                        LEFT_NAV_TEXT_CENTER_X + textBitmap.getWidth() / 2.0f,
                        LEFT_NAV_TEXT_TOPS[index] + textBitmap.getHeight()));
            }
        }
        for (float dividerTop : LEFT_NAV_DIVIDER_TOPS) {
            drawBitmap(canvas, leftNavigationDivider,
                    new RectF(22.0f, dividerTop, 378.0f, dividerTop + 11.0f));
        }
    }

    private void drawDailyGiftPanelBackground(Canvas canvas, Bitmap shopBackground, Bitmap panelBackground) {
        drawBitmap(canvas, shopBackground, new RectF(CSB_BODY_LEFT,
                CSB_BODY_TOP + MembershipDailyGiftLayout.CONTENT_OFFSET_Y,
                CSB_BODY_LEFT + CSB_BODY_WIDTH,
                CSB_BODY_TOP + CSB_BODY_HEIGHT + MembershipDailyGiftLayout.CONTENT_OFFSET_Y));
        drawBitmap(canvas, panelBackground,
                MembershipDailyGiftLayout.offsetY(new RectF(448.0f, 80.0f, 1858.0f, 1000.0f)));
        paint.setColor(Color.argb(42, 255, 210, 105));
        canvas.drawRoundRect(
                MembershipDailyGiftLayout.offsetY(new RectF(638.0f, 150.0f, 1703.0f, 940.0f)),
                22.0f,
                22.0f,
                paint);
    }

    private void drawTitleAndTip(Canvas canvas, Bitmap tipButton) {
        drawText(canvas, "每日可从以下两个礼包中选择一个领取", 1156.0f,
                MembershipDailyGiftLayout.offsetY(289.0f),
                34.0f, Color.rgb(194, 111, 61), Paint.Align.CENTER);
        drawBitmap(canvas, tipButton,
                MembershipDailyGiftLayout.offsetY(new RectF(1665.0f, 253.0f, 1713.0f, 303.0f)));
        drawText(canvas, "?", 1689.0f, MembershipDailyGiftLayout.offsetY(290.0f),
                34.0f, Color.rgb(190, 88, 56), Paint.Align.CENTER);
    }

    private void drawOpenMembershipButton(Canvas canvas, Bitmap topOpenButton) {
        drawBitmap(canvas, topOpenButton, TOP_OPEN_BUTTON_BOUNDS);
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, RectF bounds) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        destination.set(bounds);
        paint.setShader(null);
        paint.setAlpha(255);
        canvas.drawBitmap(bitmap, source, destination, paint);
    }

    private void drawText(Canvas canvas, String text, float x, float baseline, float size, int color, Paint.Align align) {
        textPaint.setTypeface(typeface);
        textPaint.setTextSize(size);
        textPaint.setTextAlign(align);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(color);
        textPaint.clearShadowLayer();
        canvas.drawText(text, x, baseline, textPaint);
    }

    private void drawStrokeText(
            Canvas canvas, String text, float x, float baseline, float size,
            int fillColor, int strokeColor, float strokeWidth, Paint.Align align) {
        textPaint.setTypeface(typeface);
        textPaint.setTextSize(size);
        textPaint.setTextAlign(align);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(strokeWidth);
        textPaint.setColor(strokeColor);
        textPaint.setShadowLayer(4.0f, 1.0f, 2.0f, Color.argb(150, 109, 68, 31));
        canvas.drawText(text, x, baseline, textPaint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(0.0f);
        textPaint.setColor(fillColor);
        canvas.drawText(text, x, baseline, textPaint);
        textPaint.clearShadowLayer();
    }
}
