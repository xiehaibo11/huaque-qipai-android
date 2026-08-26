package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import java.util.List;

/** Canvas recreation of the original SxvipShopLayer.csb "开通会员" purchase carousel. */
final class MembershipPurchaseView extends View {
    interface Actions {
        void onBuyProduct(MembershipPurchaseSelection selection);
    }

    interface ScrollOffsetListener {
        void onScrollOffsetChanged(float scrollOffset);
    }

    private static final float DESIGN_WIDTH = 1920f;
    private static final float DESIGN_HEIGHT = 1080f;
    private static final RectF CLOSE_BOUNDS =
            new RectF(1806.3283f, 37.7296f, 1860.3283f, 91.7296f);
    private static final RectF TITLE_BOUNDS = new RectF(732f, 36f, 1188f, 215f);
    private static final RectF SCROLL_CLIP = new RectF(80f, 125f, 1840f, 955f);
    private static final float CARD_LEFT = 80f;
    private static final float CARD_TOP = 130.5f;
    private static final float CARD_STEP_X = 478f;
    private static final float MAX_SCROLL_OFFSET = 620f;
    private static final float TAP_SLOP = 18f;
    private static final int TAP_TARGET_CLOSE = 1;
    private static final int TAP_TARGET_PRODUCT_BASE = 100;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();
    private final Runnable closeAction;
    private final Actions actions;
    private final ScrollOffsetListener scrollOffsetListener;
    private final MembershipProductCardRenderer cardRenderer;
    private final TapGestureGuard tapGestureGuard = new TapGestureGuard(TAP_SLOP);
    private final Bitmap titleOpenBitmap;
    private final Bitmap closeBitmap;
    private Runnable buttonClickSound = () -> {};
    private List<MembershipPurchasePlan> plans = MembershipPurchasePlan.originalPlans();
    private float scrollOffset;
    private float lastTouchX;
    private boolean trackingScroll;

    MembershipPurchaseView(
            Context context,
            Runnable closeAction,
            Actions actions,
            ScrollOffsetListener scrollOffsetListener) {
        super(context);
        this.closeAction = closeAction == null ? () -> {} : closeAction;
        this.actions = actions == null ? selection -> {} : actions;
        this.scrollOffsetListener =
                scrollOffsetListener == null ? offset -> {} : scrollOffsetListener;
        cardRenderer = new MembershipProductCardRenderer(getResources());
        titleOpenBitmap = loadBitmap(R.drawable.svip_buy_title_open);
        closeBitmap = loadBitmap(R.drawable.svip_shop_close);
    }

    void setButtonClickSound(Runnable buttonClickSound) {
        this.buttonClickSound = buttonClickSound == null ? () -> {} : buttonClickSound;
    }

    void setProducts(MembershipProductsState products) {
        if (products == null || products.products() == null || products.products().isEmpty()) {
            return;
        }
        plans =
                products.products().stream()
                        .map(MembershipPurchasePlan::fromProduct)
                        .toList();
        scrollOffset = clamp(scrollOffset, 0f, maxScrollOffset());
        scrollOffsetListener.onScrollOffsetChanged(scrollOffset);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float scale = Math.min(getWidth() / DESIGN_WIDTH, getHeight() / DESIGN_HEIGHT);
        float dx = (getWidth() - DESIGN_WIDTH * scale) * 0.5f;
        float dy = (getHeight() - DESIGN_HEIGHT * scale) * 0.5f;
        canvas.save();
        canvas.translate(dx, dy);
        canvas.scale(scale, scale);
        drawPurchaseLayer(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float scale = Math.min(getWidth() / DESIGN_WIDTH, getHeight() / DESIGN_HEIGHT);
        float x = (event.getX() - (getWidth() - DESIGN_WIDTH * scale) * 0.5f) / scale;
        float y = (event.getY() - (getHeight() - DESIGN_HEIGHT * scale) * 0.5f) / scale;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                lastTouchX = x;
                tapGestureGuard.begin(x, y, tapTargetAt(x, y));
                trackingScroll = SCROLL_CLIP.contains(x, y);
                return true;
            }
            case MotionEvent.ACTION_MOVE -> {
                tapGestureGuard.move(x, y);
                if (trackingScroll) {
                    scrollOffset =
                            clamp(scrollOffset + lastTouchX - x, 0f, maxScrollOffset());
                    lastTouchX = x;
                    scrollOffsetListener.onScrollOffsetChanged(scrollOffset);
                    invalidate();
                }
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                int target = tapTargetAt(x, y);
                if (tapGestureGuard.finish(x, y, target)) {
                    performClick();
                    handleTapTarget(target);
                }
                resetGesture();
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                resetGesture();
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

    void drawPrices(Canvas canvas) {
        for (int index = 0; index < plans.size(); index++) {
            cardRenderer.drawPrice(
                    canvas,
                    plans.get(index),
                    CARD_LEFT + index * CARD_STEP_X - scrollOffset,
                    CARD_TOP,
                    1f);
        }
    }

    private void drawPurchaseLayer(Canvas canvas) {
        drawBitmap(canvas, titleOpenBitmap, TITLE_BOUNDS);
        canvas.save();
        canvas.clipRect(SCROLL_CLIP);
        for (int index = 0; index < plans.size(); index++) {
            cardRenderer.drawCard(
                    canvas,
                    plans.get(index),
                    CARD_LEFT + index * CARD_STEP_X - scrollOffset,
                    CARD_TOP,
                    1f);
        }
        canvas.restore();
        cardRenderer.drawFooter(canvas, 0f, 0f, 1f, false);
        drawBitmap(canvas, closeBitmap, CLOSE_BOUNDS);
    }

    private int tapTargetAt(float x, float y) {
        if (CLOSE_BOUNDS.contains(x, y)) {
            return TAP_TARGET_CLOSE;
        }
        for (int index = 0; index < plans.size(); index++) {
            float left = CARD_LEFT + index * CARD_STEP_X - scrollOffset;
            RectF buyBounds =
                    new RectF(
                            left + 3f,
                            CARD_TOP + MembershipProductCardRenderer.BUY_TOP,
                            left + 465f,
                            CARD_TOP + MembershipProductCardRenderer.CARD_HEIGHT);
            if (buyBounds.contains(x, y)) {
                return TAP_TARGET_PRODUCT_BASE + index;
            }
        }
        return TapGestureGuard.NO_TARGET;
    }

    private void handleTapTarget(int target) {
        if (target == TAP_TARGET_CLOSE) {
            buttonClickSound.run();
            closeAction.run();
            return;
        }
        int index = target - TAP_TARGET_PRODUCT_BASE;
        if (index >= 0 && index < plans.size()) {
            MembershipPurchasePlan plan = plans.get(index);
            buttonClickSound.run();
            actions.onBuyProduct(
                    new MembershipPurchaseSelection(
                            plan.productCode(), plan.productName(), plan.price()));
        }
    }

    private float maxScrollOffset() {
        return Math.max(
                0f,
                Math.min(MAX_SCROLL_OFFSET, (plans.size() - 4) * CARD_STEP_X + 142f));
    }

    private void resetGesture() {
        trackingScroll = false;
        tapGestureGuard.reset();
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, RectF bounds) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        destination.set(bounds);
        paint.setAlpha(255);
        canvas.drawBitmap(bitmap, source, destination, paint);
    }

    private Bitmap loadBitmap(int resId) {
        return BitmapFactory.decodeResource(getResources(), resId);
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
