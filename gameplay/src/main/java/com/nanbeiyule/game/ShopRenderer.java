package com.nanbeiyule.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Composes the native shop from runtime-capture geometry and recovered original artwork. */
final class ShopRenderer {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final ShopBackgroundRenderer backgroundRenderer;
    private final ShopProductRenderer productRenderer;
    private final MembershipProductCardRenderer membershipCardRenderer;
    private final ShopMembershipNoticeRenderer membershipNoticeRenderer;

    ShopRenderer(ShopDrawableSet drawables) {
        backgroundRenderer = new ShopBackgroundRenderer(drawables);
        productRenderer = new ShopProductRenderer(drawables);
        membershipCardRenderer =
                new MembershipProductCardRenderer(drawables.resources());
        membershipNoticeRenderer = new ShopMembershipNoticeRenderer(drawables);
    }

    void draw(
            Canvas canvas,
            ShopCatalogState state,
            ShopWalletState wallet,
            float categoryScroll,
            float productScroll,
            boolean loading,
            String error) {
        backgroundRenderer.draw(canvas, state, wallet, categoryScroll);
        ShopLayout.Rect viewport =
                state.selectedCategory() == ShopCategory.TIME_MEMBERSHIP
                        ? ShopRuntimeLayout.MEMBERSHIP_VIEWPORT
                        : ShopRuntimeLayout.CONTENT_VIEWPORT;
        canvas.save();
        canvas.clipRect(
                viewport.left(), viewport.top(), viewport.right(), viewport.bottom());
        if (state.selectedCategory() == ShopCategory.TIME_MEMBERSHIP) {
            drawMembershipCollection(canvas, state, productScroll);
        } else {
            productRenderer.draw(canvas, state, productScroll);
        }
        canvas.restore();
        if (state.selectedCategory() == ShopCategory.TIME_MEMBERSHIP) {
            drawMembershipFooter(canvas);
        }
        if (state.selectedCategory() == ShopCategory.GOLD_MEMBERSHIP) {
            drawMembershipNotice(canvas);
        }
        if (loading) {
            drawOverlayMessage(canvas, "商城加载中…", Color.argb(170, 30, 75, 112));
        } else if (error != null && !error.trim().isEmpty()) {
            drawOverlayMessage(canvas, error, Color.argb(188, 70, 76, 103));
        }
    }

    private void drawMembershipCollection(
            Canvas canvas, ShopCatalogState state, float horizontalScroll) {
        List<MembershipPurchasePlan> plans = resolvedPlans(state);
        for (int index = 0; index < plans.size(); index++) {
            ShopLayout.Rect card = ShopRuntimeLayout.membershipCard(index, horizontalScroll);
            membershipCardRenderer.drawCard(
                    canvas,
                    plans.get(index),
                    card.left(),
                    card.top(),
                    ShopRuntimeLayout.MEMBERSHIP_CARD_SCALE);
        }
    }

    private void drawMembershipFooter(Canvas canvas) {
        membershipCardRenderer.drawFooter(
                canvas,
                ShopRuntimeLayout.MEMBERSHIP_FOOTER_ORIGIN_X,
                ShopRuntimeLayout.MEMBERSHIP_LAYER_ORIGIN_Y,
                ShopRuntimeLayout.MEMBERSHIP_CARD_SCALE,
                true);
    }

    private void drawMembershipNotice(Canvas canvas) {
        membershipNoticeRenderer.drawMembershipNotice(canvas);
    }

    /**
     * Redraws only the membership price texts; the transparent price overlay view calls
     * this above the foreground effect layer so the original button sweep animation stays
     * below the price text exactly as in the purchase dialog.
     */
    void drawMembershipPrices(Canvas canvas, ShopCatalogState state, float productScroll) {
        List<MembershipPurchasePlan> plans = resolvedPlans(state);
        for (int index = 0; index < plans.size(); index++) {
            ShopLayout.Rect card = ShopRuntimeLayout.membershipCard(index, productScroll);
            membershipCardRenderer.drawPrice(
                    canvas,
                    plans.get(index),
                    card.left(),
                    card.top(),
                    ShopRuntimeLayout.MEMBERSHIP_CARD_SCALE);
        }
    }

    private static List<MembershipPurchasePlan> resolvedPlans(ShopCatalogState state) {
        List<MembershipPurchasePlan> plans = MembershipPurchasePlan.originalPlans();
        List<MembershipPurchasePlan> resolved = new ArrayList<>(plans.size());
        for (MembershipPurchasePlan plan : plans) {
            ShopProduct product = state.findProduct(plan.productCode());
            resolved.add(
                    product != null
                            ? plan.withProduct(product.displayName(), membershipPrice(product))
                            : plan);
        }
        return resolved;
    }

    private static String membershipPrice(ShopProduct product) {
        if (!product.enabled()) {
            return "暂未开放";
        }
        String yuan =
                product.priceMinor() % 100 == 0
                        ? String.valueOf(product.priceMinor() / 100)
                        : String.format(Locale.CHINA, "%.2f", product.priceMinor() / 100f);
        return "SXVIP_CONTINUOUS_MONTH".equals(product.productCode())
                ? "连续包月:" + yuan + "元"
                : yuan + "元";
    }

    private void drawOverlayMessage(Canvas canvas, String message, int color) {
        paint.setColor(color);
        canvas.drawRoundRect(new RectF(720f, 465f, 1630f, 625f), 28f, 28f, paint);
        backgroundRenderer.drawCenteredText(
                canvas, message, 1175f, 545f, 40f, Color.WHITE, true);
    }
}
