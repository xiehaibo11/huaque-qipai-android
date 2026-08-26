package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;

final class ShopView extends View {
    interface Actions {
        void onProductSelected(ShopProduct product);

        void onBagRequested();

        void onMembershipNoticeRequested();
    }

    private static final float CATEGORY_MAX_SCROLL =
            ShopRuntimeLayout.maxCategoryScroll(ShopCategory.ordered().size());
    private static final float TAP_SLOP_DESIGN = 18f;
    private static final int TAP_TARGET_BACK = 1;
    private static final int TAP_TARGET_BAG = 2;
    private static final int TAP_TARGET_MEMBERSHIP_NOTICE = 3;
    private static final int TAP_TARGET_CATEGORY_BASE = 100;
    private static final int TAP_TARGET_PROP_SECTION_BASE = 500;
    private static final int TAP_TARGET_HOT_SECTION_BASE = 600;
    private static final int TAP_TARGET_DECORATION_SECTION_BASE = 700;
    private static final int TAP_TARGET_INTERACTION_SECTION_BASE = 800;
    private static final int TAP_TARGET_PRODUCT_BASE = 1000;

    private final Runnable closeAction;
    private final Actions actions;
    private final ShopRenderer renderer;
    private final TapGestureGuard tapGestureGuard =
            new TapGestureGuard(TAP_SLOP_DESIGN);
    private ShopCatalogState catalog = ShopOriginalCatalog.create();
    private boolean catalogAssigned;
    private ShopWalletState wallet = ShopWalletState.EMPTY;
    private float categoryScroll;
    private float productScroll;
    private float lastDesignX;
    private float lastDesignY;
    private boolean categoryGesture;
    private boolean productGesture;
    private boolean loading;
    private String error;
    private Runnable buttonClickSound = () -> {};
    private ShopMembershipPageListener membershipPageListener;
    private ShopProductEffectPageListener productEffectPageListener;

    ShopView(Context context, Runnable closeAction, Actions actions) {
        super(context);
        this.closeAction = closeAction == null ? () -> {} : closeAction;
        this.actions = actions == null ? new NoOpActions() : actions;
        renderer = new ShopRenderer(ShopDrawableSet.obtain(getResources()));
        setFocusable(true);
        setClickable(true);
    }

    void setCatalog(ShopCatalogState catalog) {
        if (catalog != null) {
            if (catalogAssigned) {
                catalog =
                        catalog.select(this.catalog.selectedCategory())
                                .selectPropSection(this.catalog.selectedPropSection())
                                .selectHotSection(this.catalog.selectedHotSection())
                                .selectInteractionSection(
                                        this.catalog.selectedInteractionSection())
                                .selectDecorationSection(
                                        this.catalog.selectedDecorationSection());
            }
            this.catalog = catalog;
            catalogAssigned = true;
            productScroll = 0f;
            notifyMembershipPage();
            notifyProductEffectPage();
            invalidate();
        }
    }

    void setMembershipPageListener(ShopMembershipPageListener listener) {
        membershipPageListener = listener;
        notifyMembershipPage();
    }

    void setProductEffectPageListener(ShopProductEffectPageListener listener) {
        productEffectPageListener = listener;
        notifyProductEffectPage();
    }

    void drawMembershipPrices(Canvas canvas) {
        renderer.drawMembershipPrices(canvas, catalog, productScroll);
    }

    private void notifyMembershipPage() {
        if (membershipPageListener != null) {
            membershipPageListener.onMembershipPageChanged(
                    catalog.selectedCategory() == ShopCategory.TIME_MEMBERSHIP, productScroll);
        }
    }

    private void notifyProductEffectPage() {
        if (productEffectPageListener != null) {
            productEffectPageListener.onProductEffectPageChanged(
                    catalog.selectedCategory(), productScroll);
        }
    }

    void setWallet(ShopWalletState wallet) {
        this.wallet = wallet == null ? ShopWalletState.EMPTY : wallet;
        invalidate();
    }

    void setLoading(boolean loading) {
        this.loading = loading;
        invalidate();
    }

    void setError(String error) {
        this.error = error;
        invalidate();
    }

    void setButtonClickSound(Runnable sound) {
        buttonClickSound = sound == null ? () -> {} : sound;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.rgb(31, 72, 111));
        ShopLayout.Transform transform = ShopLayout.contain(getWidth(), getHeight());
        canvas.save();
        canvas.translate(transform.offsetX(), transform.offsetY());
        canvas.scale(transform.scale(), transform.scale());
        renderer.draw(
                canvas, catalog, wallet, categoryScroll, productScroll, loading, error);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return false;
        }
        ShopLayout.Transform transform = ShopLayout.contain(getWidth(), getHeight());
        float x = transform.toDesignX(event.getX());
        float y = transform.toDesignY(event.getY());
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastDesignX = x;
                lastDesignY = y;
                tapGestureGuard.begin(x, y, tapTargetAt(x, y));
                categoryGesture = ShopRuntimeLayout.PRIMARY_CATEGORY_LIST.contains(x, y);
                productGesture =
                        (catalog.selectedCategory() == ShopCategory.TIME_MEMBERSHIP
                                        ? ShopRuntimeLayout.MEMBERSHIP_VIEWPORT
                                        : ShopRuntimeLayout.CONTENT_VIEWPORT)
                                .contains(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                tapGestureGuard.move(x, y);
                if (categoryGesture) {
                    categoryScroll =
                            clamp(
                                    categoryScroll + lastDesignY - y,
                                    0f,
                                    CATEGORY_MAX_SCROLL);
                    invalidate();
                } else if (productGesture) {
                    if (catalog.selectedCategory() == ShopCategory.TIME_MEMBERSHIP) {
                        productScroll =
                                clamp(
                                        productScroll + lastDesignX - x,
                                        0f,
                                        ShopRuntimeLayout.maxMembershipScroll(
                                                MembershipPurchasePlan.originalPlans().size()));
                        notifyMembershipPage();
                    } else {
                        productScroll =
                                clamp(
                                        productScroll + lastDesignY - y,
                                        0f,
                                        ShopRuntimeLayout.maxProductScroll(
                                                catalog.selectedCategory(),
                                                catalog.selectedProducts().size()));
                        notifyProductEffectPage();
                    }
                    invalidate();
                }
                lastDesignX = x;
                lastDesignY = y;
                return true;
            case MotionEvent.ACTION_UP:
                if (tapGestureGuard.finish(x, y, tapTargetAt(x, y))) {
                    performClick();
                    handleTap(x, y);
                }
                resetGesture();
                return true;
            case MotionEvent.ACTION_CANCEL:
                resetGesture();
                return true;
            default:
                return true;
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void handleTap(float x, float y) {
        if (ShopRuntimeLayout.BACK_BUTTON.contains(x, y)) {
            buttonClickSound.run();
            closeAction.run();
            return;
        }
        if (loading) {
            return;
        }
        if (ShopRuntimeLayout.BAG_BUTTON.contains(x, y)) {
            buttonClickSound.run();
            actions.onBagRequested();
            return;
        }
        if (catalog.selectedCategory() == ShopCategory.GOLD_MEMBERSHIP
                && ShopMembershipNoticeLayout.LINK.contains(x, y)) {
            buttonClickSound.run();
            actions.onMembershipNoticeRequested();
            return;
        }
        int categoryIndex = ShopRuntimeLayout.categoryIndexAt(
                x, y, categoryScroll, ShopCategory.ordered().size());
        if (categoryIndex >= 0) {
            buttonClickSound.run();
            catalog = catalog.select(ShopCategory.ordered().get(categoryIndex));
            productScroll = 0f;
            error = null;
            notifyMembershipPage();
            notifyProductEffectPage();
            invalidate();
            return;
        }
        int propSectionIndex = propSectionIndexAt(x, y);
        if (propSectionIndex >= 0) {
            buttonClickSound.run();
            catalog =
                    catalog.selectPropSection(
                            ShopPropSection.ordered().get(propSectionIndex));
            productScroll = 0f;
            error = null;
            notifyProductEffectPage();
            invalidate();
            return;
        }
        int hotSectionIndex = hotSectionIndexAt(x, y);
        if (hotSectionIndex >= 0) {
            buttonClickSound.run();
            catalog =
                    catalog.selectHotSection(
                            ShopHotSection.ordered().get(hotSectionIndex));
            productScroll = 0f;
            error = null;
            notifyProductEffectPage();
            invalidate();
            return;
        }
        int decorationSectionIndex = decorationSectionIndexAt(x, y);
        if (decorationSectionIndex >= 0) {
            buttonClickSound.run();
            catalog =
                    catalog.selectDecorationSection(
                            ShopDecorationSection.ordered().get(decorationSectionIndex));
            productScroll = 0f;
            error = null;
            notifyProductEffectPage();
            invalidate();
            return;
        }
        int interactionSectionIndex = interactionSectionIndexAt(x, y);
        if (interactionSectionIndex >= 0) {
            buttonClickSound.run();
            catalog =
                    catalog.selectInteractionSection(
                            ShopInteractionSection.ordered().get(interactionSectionIndex));
            productScroll = 0f;
            error = null;
            notifyProductEffectPage();
            invalidate();
            return;
        }
        int productIndex;
        ShopProduct product;
        if (catalog.selectedCategory() == ShopCategory.TIME_MEMBERSHIP) {
            productIndex =
                    ShopRuntimeLayout.membershipProductIndexAt(
                            x,
                            y,
                            productScroll,
                            MembershipPurchasePlan.originalPlans().size());
            product =
                    productIndex < 0
                            ? null
                            : catalog.findProduct(
                                    MembershipPurchasePlan.originalPlans()
                                            .get(productIndex)
                                            .productCode());
        } else {
            productIndex =
                    ShopRuntimeLayout.productIndexAt(
                            catalog.selectedCategory(),
                            x,
                            y,
                            productScroll,
                            catalog.selectedProducts().size());
            product =
                    productIndex < 0
                            ? null
                            : catalog.selectedProducts().get(productIndex);
        }
        if (productIndex >= 0) {
            if (product != null && product.available()) {
                buttonClickSound.run();
                actions.onProductSelected(product);
            }
        }
    }

    private int tapTargetAt(float x, float y) {
        if (ShopRuntimeLayout.BACK_BUTTON.contains(x, y)) {
            return TAP_TARGET_BACK;
        }
        if (loading) {
            return TapGestureGuard.NO_TARGET;
        }
        if (ShopRuntimeLayout.BAG_BUTTON.contains(x, y)) {
            return TAP_TARGET_BAG;
        }
        if (catalog.selectedCategory() == ShopCategory.GOLD_MEMBERSHIP
                && ShopMembershipNoticeLayout.LINK.contains(x, y)) {
            return TAP_TARGET_MEMBERSHIP_NOTICE;
        }
        int categoryIndex =
                ShopRuntimeLayout.categoryIndexAt(
                        x, y, categoryScroll, ShopCategory.ordered().size());
        if (categoryIndex >= 0) {
            return TAP_TARGET_CATEGORY_BASE + categoryIndex;
        }
        int propSectionIndex = propSectionIndexAt(x, y);
        if (propSectionIndex >= 0) {
            return TAP_TARGET_PROP_SECTION_BASE + propSectionIndex;
        }
        int hotSectionIndex = hotSectionIndexAt(x, y);
        if (hotSectionIndex >= 0) {
            return TAP_TARGET_HOT_SECTION_BASE + hotSectionIndex;
        }
        int decorationSectionIndex = decorationSectionIndexAt(x, y);
        if (decorationSectionIndex >= 0) {
            return TAP_TARGET_DECORATION_SECTION_BASE + decorationSectionIndex;
        }
        int interactionSectionIndex = interactionSectionIndexAt(x, y);
        if (interactionSectionIndex >= 0) {
            return TAP_TARGET_INTERACTION_SECTION_BASE + interactionSectionIndex;
        }
        int productIndex;
        if (catalog.selectedCategory() == ShopCategory.TIME_MEMBERSHIP) {
            productIndex =
                    ShopRuntimeLayout.membershipProductIndexAt(
                            x,
                            y,
                            productScroll,
                            MembershipPurchasePlan.originalPlans().size());
        } else {
            productIndex =
                    ShopRuntimeLayout.productIndexAt(
                            catalog.selectedCategory(),
                            x,
                            y,
                            productScroll,
                            catalog.selectedProducts().size());
        }
        return productIndex < 0
                ? TapGestureGuard.NO_TARGET
                : TAP_TARGET_PRODUCT_BASE + productIndex;
    }

    private int propSectionIndexAt(float x, float y) {
        return catalog.selectedCategory() == ShopCategory.PROP
                ? ShopRuntimeLayout.propSectionIndexAt(x, y)
                : -1;
    }

    private int hotSectionIndexAt(float x, float y) {
        return catalog.selectedCategory() == ShopCategory.HOT_RECOMMENDATION
                ? ShopRuntimeLayout.hotSectionIndexAt(x, y)
                : -1;
    }

    private int decorationSectionIndexAt(float x, float y) {
        return catalog.selectedCategory() == ShopCategory.DECORATION
                ? ShopRuntimeLayout.decorationSectionIndexAt(x, y)
                : -1;
    }

    private int interactionSectionIndexAt(float x, float y) {
        return catalog.selectedCategory() == ShopCategory.INTERACTION
                ? ShopRuntimeLayout.interactionSectionIndexAt(x, y)
                : -1;
    }

    private void resetGesture() {
        categoryGesture = false;
        productGesture = false;
        tapGestureGuard.reset();
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static final class NoOpActions implements Actions {
        @Override
        public void onProductSelected(ShopProduct product) {}

        @Override
        public void onBagRequested() {}

        @Override
        public void onMembershipNoticeRequested() {}
    }
}
