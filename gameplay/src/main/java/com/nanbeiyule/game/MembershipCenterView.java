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
import android.view.MotionEvent;
import android.view.View;

/** Canvas recreation of the original Zhejiang membership-privilege dialog. */
final class MembershipCenterView extends View {
    private static final float DESIGN_WIDTH = 1920.0f;
    private static final float DESIGN_HEIGHT = 1080.0f;
    private static final float POPUP_SCALE_FACTOR = 1.0f;
    private static final RectF PRIVILEGE_SCROLL_CLIP =
            new RectF(123.5f, 210.0f, 1276.5f, 970.0f);
    private static final RectF FEEDBACK_TIP_BOUNDS =
            new RectF(549.3164f, 65.7133f, 884.3164f, 111.7133f);
    private static final RectF OPEN_PURCHASE_BOUNDS =
            new RectF(1266.0f, 875.0f, 1818.0f, 1060.0f);
    private static final RectF HEADER_TIP_TEXT_BOUNDS =
            new RectF(482.69f, 114.63f, 862.69f, 171.63f);
    private static final float PRIVILEGE_CARD_LEFT = 123.5f;
    private static final float PRIVILEGE_CARD_TOP = 292.0f;
    private static final float PRIVILEGE_CARD_WIDTH = 282.0f;
    private static final float PRIVILEGE_CARD_HEIGHT = 320.0f;
    private static final float PRIVILEGE_CARD_COLUMN_SPACING = 282.0f;
    private static final float PRIVILEGE_CARD_ROW_SPACING = 320.0f;
    private static final int GOLD_PRIVILEGE_START_INDEX = 8;
    private static final float GOLD_SECTION_TITLE_Y = 976.0f;
    private static final float GOLD_PRIVILEGE_CARD_TOP = 1002.0f;
    private static final float MAX_PRIVILEGE_SCROLL_OFFSET = 672.0f;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint textPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final MembershipCenterHeaderRenderer headerRenderer =
            new MembershipCenterHeaderRenderer();
    private final RectF closeBounds = new RectF(1772.0f, 39.0f, 1827.0f, 94.0f);
    private final Rect bitmapSource = new Rect();
    private final RectF bitmapDestination = new RectF();
    private final Runnable closeAction;
    private final MembershipPrivilegeCardRenderer cardRenderer;
    private Bitmap closeBitmap;
    private Bitmap feedbackTipBitmap;
    private Bitmap headerTipTextBitmap;
    private Bitmap privilegeTitleDividerBitmap;
    private Runnable buttonClickSound = () -> {};
    private final Runnable openDailyGiftAction;
    private final Runnable openMembershipPurchaseAction;
    private float privilegeScrollOffset;
    private float lastTouchY;
    private boolean trackingPrivilegeScroll;

    MembershipCenterView(
            Context context,
            Runnable closeAction,
            Runnable openDailyGiftAction,
            Runnable openMembershipPurchaseAction) {
        super(context);
        this.closeAction = closeAction;
        this.openDailyGiftAction =
                openDailyGiftAction == null ? () -> {} : openDailyGiftAction;
        this.openMembershipPurchaseAction =
                openMembershipPurchaseAction == null ? () -> {} : openMembershipPurchaseAction;
        setFocusable(true);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        cardRenderer = new MembershipPrivilegeCardRenderer(getResources());
        closeBitmap = loadBitmap(R.drawable.svip_shop_close);
        feedbackTipBitmap = loadBitmap(R.drawable.sxvip_privilege_feedback_tip);
        headerTipTextBitmap = loadBitmap(R.drawable.sxvip_privilege_header_tip_text);
        privilegeTitleDividerBitmap =
                loadBitmap(R.drawable.sxvip_privilege_title_divider);
    }

    void setButtonClickSound(Runnable buttonClickSound) {
        this.buttonClickSound = buttonClickSound == null ? () -> {} : buttonClickSound;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float scale =
                Math.min(getWidth() / DESIGN_WIDTH, getHeight() / DESIGN_HEIGHT)
                        * POPUP_SCALE_FACTOR;
        float dx = (getWidth() - DESIGN_WIDTH * scale) * 0.5f;
        float dy = (getHeight() - DESIGN_HEIGHT * scale) * 0.5f;
        canvas.save();
        canvas.translate(dx, dy);
        canvas.scale(scale, scale);
        drawOriginalMembershipPopup(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float scale =
                Math.min(getWidth() / DESIGN_WIDTH, getHeight() / DESIGN_HEIGHT)
                        * POPUP_SCALE_FACTOR;
        float x = (event.getX() - (getWidth() - DESIGN_WIDTH * scale) * 0.5f) / scale;
        float y = (event.getY() - (getHeight() - DESIGN_HEIGHT * scale) * 0.5f) / scale;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                lastTouchY = y;
                trackingPrivilegeScroll = PRIVILEGE_SCROLL_CLIP.contains(x, y);
                return true;
            }
            case MotionEvent.ACTION_MOVE -> {
                if (trackingPrivilegeScroll) {
                    privilegeScrollOffset =
                            clamp(
                                    privilegeScrollOffset + lastTouchY - y,
                                    0.0f,
                                    MAX_PRIVILEGE_SCROLL_OFFSET);
                    lastTouchY = y;
                    invalidate();
                }
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                if (closeBounds.contains(x, y)) {
                    performClick();
                    buttonClickSound.run();
                    closeAction.run();
                } else if (FEEDBACK_TIP_BOUNDS.contains(x, y)) {
                    performClick();
                    buttonClickSound.run();
                    openDailyGiftAction.run();
                } else if (OPEN_PURCHASE_BOUNDS.contains(x, y)) {
                    performClick();
                    buttonClickSound.run();
                    openMembershipPurchaseAction.run();
                }
                trackingPrivilegeScroll = false;
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                trackingPrivilegeScroll = false;
                return true;
            }
            default -> {
                return true;
            }
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void drawOriginalMembershipPopup(Canvas canvas) {
        drawHeader(canvas);
        drawRightPanelTexts(canvas);
        drawPrivilegeGrid(canvas);
        drawBitmap(canvas, closeBitmap, closeBounds);
    }

    private void drawHeader(Canvas canvas) {
        drawBitmap(canvas, headerTipTextBitmap, HEADER_TIP_TEXT_BOUNDS);
        drawGlowingNumberText(canvas, "15", 723.7f, 170.0f, 76.0f);
        drawBubble(canvas);
    }

    private void drawBubble(Canvas canvas) {
        drawBitmap(canvas, feedbackTipBitmap, FEEDBACK_TIP_BOUNDS);
    }

    private void drawRightPanelTexts(Canvas canvas) {
        drawBannerText(canvas);
        drawDailyGiftText(canvas);
        drawGiftValueText(canvas);
        drawOpenButtonText(canvas);
    }

    private void drawBannerText(Canvas canvas) {
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(7.0f);
        textPaint.setShadowLayer(6.0f, 3.0f, 4.0f, Color.rgb(131, 59, 18));
        textPaint.setColor(Color.rgb(170, 75, 25));
        textPaint.setTextSize(88.0f);
        canvas.drawText("15", 1300.0f, 158.0f, textPaint);
        textPaint.setTextSize(55.0f);
        canvas.drawText("项游戏特权", 1415.0f, 151.0f, textPaint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(0.0f);
        textPaint.setColor(Color.rgb(255, 246, 177));
        textPaint.setTextSize(88.0f);
        canvas.drawText("15", 1300.0f, 158.0f, textPaint);
        textPaint.setColor(Color.rgb(255, 252, 214));
        textPaint.setTextSize(55.0f);
        canvas.drawText("项游戏特权", 1415.0f, 151.0f, textPaint);
        textPaint.clearShadowLayer();
    }

    private void drawDailyGiftText(Canvas canvas) {
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setShadowLayer(5.0f, 2.0f, 3.0f, Color.rgb(173, 96, 36));
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(42.0f);
        canvas.drawText("每日领取价值", 1360.0f, 270.0f, textPaint);
        textPaint.setTextSize(66.0f);
        canvas.drawText("18", 1650.0f, 274.0f, textPaint);
        textPaint.setTextSize(42.0f);
        canvas.drawText("元礼品", 1733.0f, 270.0f, textPaint);
        textPaint.clearShadowLayer();
    }

    private void drawGiftValueText(Canvas canvas) {
        drawRightTagText(canvas, "136元礼包", 1613.0f, 694.0f, 35.0f);
    }

    private void drawOpenButtonText(Canvas canvas) {
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.clearShadowLayer();
        textPaint.setColor(Color.rgb(37, 34, 48));
        textPaint.setTextSize(72.0f);
        canvas.drawText("立即开通", 1546.0f, 966.0f, textPaint);
        textPaint.setTextSize(37.0f);
        canvas.drawText("(查看详情)", 1546.0f, 1018.0f, textPaint);
    }

    private void drawRightTagText(
            Canvas canvas, String value, float x, float y, float size) {
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(size);
        textPaint.setColor(Color.rgb(130, 65, 19));
        textPaint.setShadowLayer(2.0f, 0.0f, 1.0f, Color.WHITE);
        canvas.drawText(value, x, y, textPaint);
        textPaint.clearShadowLayer();
    }

    private void drawPrivilegeGrid(Canvas canvas) {
        canvas.save();
        canvas.clipRect(PRIVILEGE_SCROLL_CLIP);
        canvas.translate(0.0f, -privilegeScrollOffset);
        drawSectionTitle(canvas, "对局特权", 622, 266);
        drawMatchPrivilegeCards(canvas);
        drawSectionTitle(canvas, "金币特权", 622, GOLD_SECTION_TITLE_Y);
        drawGoldPrivilegeCards(canvas);
        canvas.restore();
    }

    private void drawMatchPrivilegeCards(Canvas canvas) {
        for (int index = 0; index < GOLD_PRIVILEGE_START_INDEX; index++) {
            float x = PRIVILEGE_CARD_LEFT
                    + index % 4 * PRIVILEGE_CARD_COLUMN_SPACING;
            float y = PRIVILEGE_CARD_TOP
                    + index / 4 * PRIVILEGE_CARD_ROW_SPACING;
            cardRenderer.draw(canvas, index, x, y);
        }
    }

    private void drawGoldPrivilegeCards(Canvas canvas) {
        for (int index = GOLD_PRIVILEGE_START_INDEX; index < MembershipPrivilegeCardRenderer.PRIVILEGE_COUNT; index++) {
            float goldIndex = index - GOLD_PRIVILEGE_START_INDEX;
            float x = PRIVILEGE_CARD_LEFT
                    + goldIndex % 4 * PRIVILEGE_CARD_COLUMN_SPACING;
            float y = GOLD_PRIVILEGE_CARD_TOP
                    + (float) Math.floor(goldIndex / 4.0f) * PRIVILEGE_CARD_ROW_SPACING;
            cardRenderer.draw(canvas, index, x, y);
        }
    }

    private void drawSectionTitle(Canvas canvas, String title, float x, float y) {
        drawBitmap(
                canvas,
                privilegeTitleDividerBitmap,
                new RectF(362.5f, y - 33.0f, 1037.5f, y - 8.0f));
        drawText(canvas, title, 700.0f, y, 42, Color.rgb(83, 106, 154), Paint.Align.CENTER);
    }

    private void drawGlowingNumberText(
            Canvas canvas, String value, float x, float baseline, float size) {
        headerRenderer.drawGlowingNumberText(canvas, value, x, baseline, size);
    }

    private void drawText(Canvas canvas, String value, float x, float y, float size, int color, Paint.Align align) {
        textPaint.setTextSize(size); textPaint.setColor(color); textPaint.setTextAlign(align);
        textPaint.setStyle(Paint.Style.FILL); textPaint.clearShadowLayer(); canvas.drawText(value, x, y, textPaint);
    }


    private Bitmap loadBitmap(int resourceId) {
        return BitmapFactory.decodeResource(getResources(), resourceId);
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, RectF destination) {
        if (bitmap == null || bitmap.isRecycled()) return;
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(null);
        paint.setAlpha(255);
        bitmapSource.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        bitmapDestination.set(destination);
        canvas.drawBitmap(bitmap, bitmapSource, bitmapDestination, paint);
    }
}
