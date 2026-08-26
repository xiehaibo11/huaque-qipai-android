package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;

/** Native rendering of JuBaoPenLotteryView.csb and JuBaoPenItemIconView.csb. */
@SuppressLint("ViewConstructor")
final class TaizhouTreasureResultView extends TaizhouToolView {
    interface Actions {
        void onCloseRequested();

        void onRepeatRequested(int count);
    }

    static final String LOTTERY_CSB = "JuBaoPenLotteryView.csb";
    static final String ITEM_CSB = "JuBaoPenItemIconView.csb";
    private static final String RESULT_TIMELINE =
            "taizhou_treasure_effects/zzb_jbp_gchd/zzb_jbp_gchd_animation.json";
    private static final int[] ICON_RESOURCES = {
        R.drawable.taizhou_treasure_icon_1, R.drawable.taizhou_treasure_icon_2,
        R.drawable.taizhou_treasure_icon_3, R.drawable.taizhou_treasure_icon_4,
        R.drawable.taizhou_treasure_icon_5, R.drawable.taizhou_treasure_icon_6,
        R.drawable.taizhou_treasure_icon_7, R.drawable.taizhou_treasure_icon_8,
        R.drawable.taizhou_treasure_icon_9, R.drawable.taizhou_treasure_icon_10,
        R.drawable.taizhou_treasure_icon_11, R.drawable.taizhou_treasure_icon_12,
        R.drawable.taizhou_treasure_icon_13, R.drawable.taizhou_treasure_icon_14,
        R.drawable.taizhou_treasure_icon_15, R.drawable.taizhou_treasure_icon_16
    };
    private static final int[] QUALITY_LABEL_RESOURCES = {
        R.drawable.taizhou_treasure_quality_label_1,
        R.drawable.taizhou_treasure_quality_label_2,
        R.drawable.taizhou_treasure_quality_label_3,
        R.drawable.taizhou_treasure_quality_label_4
    };

    private final TaizhouTreasureResultPresentation presentation;
    private final FortuneState beforeDraw;
    private final Actions actions;
    private final long openedNanos = System.nanoTime();
    private final String animation;
    private final TaizhouTreasureFonts fonts;
    private final TaizhouTreasureEffects effects;
    private final TaizhouTreasureNodeTimeline timeline;
    private final Typeface typeface;
    private final Bitmap close, title, drawButton, diamond, diamondBackground;
    private final Bitmap discount, nameBackground, newTag, upgradeTag, maxTag;
    private final Bitmap[] icons = new Bitmap[16];
    private final Bitmap[] qualityLabels = new Bitmap[4];
    private boolean repeatEnabled = true;
    private boolean released;

    TaizhouTreasureResultView(
            Context context,
            FortuneState beforeDraw,
            FortuneTreasureDrawResult result,
            Actions actions) {
        super(context);
        this.beforeDraw = beforeDraw;
        this.presentation = TaizhouTreasureResultPresentation.from(beforeDraw, result);
        this.actions = actions;
        animation = result.count() == 1 ? "1chou" : "5chou";
        fonts = TaizhouTreasureFonts.load(getResources());
        effects = new TaizhouTreasureEffects(getResources().getAssets());
        timeline = TaizhouTreasureNodeTimeline.load(
                getResources(), RESULT_TIMELINE, animation);
        typeface = fonts.text();
        close = bitmap(R.drawable.taizhou_treasure_result_close);
        title = bitmap(R.drawable.taizhou_treasure_result_title);
        drawButton = bitmap(R.drawable.taizhou_treasure_draw_five);
        diamond = bitmap(R.drawable.taizhou_treasure_diamond);
        diamondBackground = bitmap(R.drawable.taizhou_treasure_diamond_bg);
        discount = bitmap(R.drawable.taizhou_treasure_discount);
        nameBackground = bitmap(R.drawable.taizhou_treasure_result_name_bg);
        newTag = bitmap(R.drawable.taizhou_treasure_tag_new);
        upgradeTag = bitmap(R.drawable.taizhou_treasure_tag_upgrade);
        maxTag = bitmap(R.drawable.taizhou_treasure_tag_max);
        loadAll(icons, ICON_RESOURCES);
        loadAll(qualityLabels, QUALITY_LABEL_RESOURCES);
    }

    void setRepeatEnabled(boolean enabled) {
        repeatEnabled = enabled;
        invalidate();
    }

    @Override
    protected void drawDesign(Canvas canvas) {
        float elapsed = Math.max(0.0f,
                (System.nanoTime() - openedNanos) / 1_000_000_000.0f);
        drawMask(canvas, elapsed);
        effects.drawOnce(canvas, TaizhouTreasureEffects.RESULT, animation,
                elapsed, 960.0f, 540.0f, 1.0f, 1.0f);
        for (int index = 0; index < presentation.items().size(); index++) {
            drawItem(canvas, presentation.items().get(index), index, elapsed);
        }
        drawTitle(canvas, elapsed);
        drawRepeatButton(canvas, elapsed);
        drawClose(canvas, elapsed);
        drawWallet(canvas, elapsed);
        if (isAttachedToWindow() && !released) postInvalidateOnAnimation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_UP) return true;
        float x = designX(event);
        float y = designY(event);
        if (TaizhouTreasureResultLayout.CLOSE.contains(x, y)) {
            actions.onCloseRequested();
        } else if (repeatEnabled && TaizhouTreasureResultLayout.DRAW.contains(x, y)) {
            repeatEnabled = false;
            actions.onRepeatRequested(presentation.drawCount());
        }
        performClick();
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (released) return;
        released = true;
        effects.release();
        TaizhouTreasureCanvas.recycle(close, title, drawButton, diamond,
                diamondBackground, discount, nameBackground, newTag, upgradeTag, maxTag);
        TaizhouTreasureCanvas.recycle(icons);
        TaizhouTreasureCanvas.recycle(qualityLabels);
    }

    private void drawMask(Canvas canvas, float elapsed) {
        TaizhouTreasureNodeTimeline.NodePose pose = timeline.pose("zhezhao", elapsed);
        fillPaint.setColor(Color.argb(Math.round(180.0f * pose.alpha()), 0, 0, 0));
        canvas.drawRect(0.0f, 0.0f, 1920.0f, 1080.0f, fillPaint);
    }

    private void drawItem(
            Canvas canvas,
            TaizhouTreasureResultPresentation.Item item,
            int index,
            float elapsed) {
        float x = TaizhouTreasureResultLayout.itemCenterX(
                presentation.drawCount(), index);
        float y = TaizhouTreasureResultLayout.itemCenterY();
        float baseScale = TaizhouTreasureResultLayout.itemScale(presentation.drawCount());
        TaizhouTreasureNodeTimeline.NodePose pose =
                timeline.pose(Integer.toString(index + 1), elapsed);
        float scaleX = baseScale * pose.scaleX();
        float scaleY = baseScale * pose.scaleY();
        int layer = saveAlphaLayer(canvas, pose.alpha());
        effects.draw(canvas, "zzb_ty_bhxz", "animation",
                elapsed, x, y, scaleX, scaleY);
        drawCentered(canvas, icons[item.catalog().index() - 1], x, y - 25.0f * scaleY,
                360.0f * scaleX, 360.0f * scaleY);
        drawCentered(canvas, tag(item.tag()),
                x - 185.993f * scaleX, y - 141.664f * scaleY,
                tagWidth(item.tag()) * scaleX, tagHeight(item.tag()) * scaleY);
        drawCentered(canvas, nameBackground,
                x + 2.454f * scaleX, y + 175.137f * scaleY,
                367.0f * scaleX, 90.0f * scaleY);
        drawCentered(canvas, qualityLabels[item.catalog().quality() - 1],
                x + 115.988f * scaleX, y + 122.3f * scaleY,
                91.0f * scaleX, 41.0f * scaleY);
        text(canvas, "Lv", x - 146.762f * scaleX, y + 178.317f * scaleY,
                28.0f * baseScale, Color.rgb(210, 251, 255), Paint.Align.CENTER);
        text(canvas, Integer.toString(item.draw().level()),
                x - 116.089f * scaleX, y + 173.347f * scaleY,
                44.0f * baseScale, Color.rgb(103, 240, 254), Paint.Align.CENTER);
        text(canvas, TaizhouTreasureCatalog.qualityName(item.catalog().quality()),
                x + 117.486f * scaleX, y + 123.278f * scaleY,
                28.0f * baseScale, Color.WHITE, Paint.Align.CENTER);
        text(canvas, item.catalog().name(),
                x - 69.804f * scaleX, y + 175.375f * scaleY,
                42.0f * baseScale, Color.rgb(179, 248, 255), Paint.Align.LEFT);
        canvas.restoreToCount(layer);
    }

    private void drawTitle(Canvas canvas, float elapsed) {
        TaizhouTreasureNodeTimeline.NodePose pose = timeline.pose("yunshi", elapsed);
        drawNode(canvas, title, TaizhouTreasureResultLayout.TITLE, pose, 1.0f);
        if (presentation.totalFortuneDelta() > 0) {
            drawFont(canvas, fonts.fortuneValue(),
                    "运势:+" + presentation.totalFortuneDelta(),
                    TaizhouTreasureResultLayout.FORTUNE.centerX(),
                    TaizhouTreasureResultLayout.FORTUNE.centerY(), pose, false);
        }
    }

    private void drawRepeatButton(Canvas canvas, float elapsed) {
        TaizhouTreasureNodeTimeline.NodePose pose = timeline.pose("btn", elapsed);
        drawNode(canvas, drawButton, TaizhouTreasureResultLayout.DRAW_ART, pose,
                repeatEnabled ? 1.0f : 0.72f);
        String label = presentation.drawCount() == 1 ? "再抽1次" : "再抽5次";
        drawFont(canvas, fonts.drawCount(), label,
                TaizhouTreasureResultLayout.DRAW_LABEL.centerX(),
                TaizhouTreasureResultLayout.DRAW_LABEL.centerY(), pose, false);
        drawNode(canvas, diamond, TaizhouTreasureResultLayout.DRAW_DIAMOND, pose, 1.0f);
        long price = presentation.drawCount() == 1
                ? beforeDraw.treasureOneDrawPriceDiamonds()
                : beforeDraw.treasureFiveDrawPriceDiamonds();
        drawFont(canvas, fonts.diamondPrice(), Long.toString(price),
                TaizhouTreasureResultLayout.DRAW_PRICE.left(),
                TaizhouTreasureResultLayout.DRAW_PRICE.centerY(), pose, true);
        if (presentation.drawCount() == 5) {
            drawNode(canvas, discount, TaizhouTreasureResultLayout.DISCOUNT, pose, 1.0f);
            text(canvas, beforeDraw.treasureFiveDrawDiscountTenths() + "折",
                    TaizhouTreasureResultLayout.DISCOUNT_TEXT.centerX(),
                    TaizhouTreasureResultLayout.DISCOUNT_TEXT.centerY(),
                    40.0f, Color.WHITE, Paint.Align.CENTER);
        }
        if (elapsed >= 3.0f) {
            float sweep = (elapsed - 3.0f) % 3.0f;
            float duration = effects.animationDuration(
                    TaizhouTreasureEffects.BUTTON_SWEEP, "animation");
            if (sweep <= duration) {
                effects.drawOnce(canvas, TaizhouTreasureEffects.BUTTON_SWEEP, "animation",
                        sweep, TaizhouTreasureResultLayout.DRAW.centerX(),
                        TaizhouTreasureResultLayout.DRAW.centerY(), 1.0f, 1.0f);
            }
        }
    }

    private void drawClose(Canvas canvas, float elapsed) {
        drawNode(canvas, close, TaizhouTreasureResultLayout.CLOSE_ART,
                timeline.pose("gb", elapsed), 1.0f);
    }

    private void drawWallet(Canvas canvas, float elapsed) {
        TaizhouTreasureNodeTimeline.NodePose pose = timeline.pose("zuanshi", elapsed);
        drawNode(canvas, diamondBackground, TaizhouTreasurePotLayout.DIAMOND_BACKGROUND,
                pose, 1.0f);
        drawNode(canvas, diamond, TaizhouTreasurePotLayout.DIAMOND_ICON, pose, 1.0f);
        int layer = saveAlphaLayer(canvas, pose.alpha());
        int transform = canvas.save();
        canvas.scale(pose.scaleX(), pose.scaleY(),
                TaizhouTreasurePotLayout.DIAMOND_TEXT.left(),
                TaizhouTreasurePotLayout.DIAMOND_TEXT.centerY());
        text(canvas, Long.toString(presentation.wallet().diamonds()),
                TaizhouTreasurePotLayout.DIAMOND_TEXT.left(),
                TaizhouTreasurePotLayout.DIAMOND_TEXT.centerY(),
                39.0f, Color.WHITE, Paint.Align.LEFT);
        canvas.restoreToCount(transform);
        canvas.restoreToCount(layer);
    }

    private void drawNode(
            Canvas canvas,
            Bitmap bitmap,
            TaizhouTreasurePotLayout.Node node,
            TaizhouTreasureNodeTimeline.NodePose pose,
            float alpha) {
        bitmapPaint.setAlpha(Math.round(255.0f * alpha * pose.alpha()));
        drawCentered(canvas, bitmap, node.centerX(), node.centerY(),
                node.width() * pose.scaleX(), node.height() * pose.scaleY());
        bitmapPaint.setAlpha(255);
    }

    private void drawFont(
            Canvas canvas,
            SxvipBitmapFont font,
            String value,
            float x,
            float y,
            TaizhouTreasureNodeTimeline.NodePose pose,
            boolean left) {
        int layer = saveAlphaLayer(canvas, pose.alpha());
        int transform = canvas.save();
        canvas.scale(pose.scaleX(), pose.scaleY(), x, y);
        if (left) font.drawLeft(canvas, value, x, y);
        else font.drawCentered(canvas, value, x, y);
        canvas.restoreToCount(transform);
        canvas.restoreToCount(layer);
    }

    private void text(
            Canvas canvas,
            String value,
            float x,
            float y,
            float size,
            int color,
            Paint.Align align) {
        TaizhouTreasureCanvas.text(
                canvas, textPaint, typeface, value, x, y, size, color, align);
    }

    private Bitmap tag(TaizhouTreasureResultPresentation.Tag tag) {
        return switch (tag) {
            case NEW -> newTag;
            case UPGRADE -> upgradeTag;
            case MAX -> maxTag;
        };
    }

    private static float tagWidth(TaizhouTreasureResultPresentation.Tag tag) {
        return switch (tag) {
            case NEW -> 109.0f;
            case UPGRADE -> 110.0f;
            case MAX -> 121.0f;
        };
    }

    private static float tagHeight(TaizhouTreasureResultPresentation.Tag tag) {
        return switch (tag) {
            case NEW -> 128.0f;
            case UPGRADE -> 101.0f;
            case MAX -> 109.0f;
        };
    }

    private void loadAll(Bitmap[] target, int[] resources) {
        for (int index = 0; index < target.length; index++) {
            target[index] = bitmap(resources[index]);
        }
    }

    private static int saveAlphaLayer(Canvas canvas, float alpha) {
        return canvas.saveLayerAlpha(
                null, Math.round(255.0f * Math.max(0.0f, Math.min(1.0f, alpha))));
    }
}
