package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Draws JuBaoPenMainView.csb in its original 1920x1080 node order. */
final class TaizhouTreasurePotRenderer {
    private static final float COLLAPSE_SECONDS = 0.6667f;
    private static final int[] ICON_RESOURCES = {
        R.drawable.taizhou_treasure_icon_1, R.drawable.taizhou_treasure_icon_2,
        R.drawable.taizhou_treasure_icon_3, R.drawable.taizhou_treasure_icon_4,
        R.drawable.taizhou_treasure_icon_5, R.drawable.taizhou_treasure_icon_6,
        R.drawable.taizhou_treasure_icon_7, R.drawable.taizhou_treasure_icon_8,
        R.drawable.taizhou_treasure_icon_9, R.drawable.taizhou_treasure_icon_10,
        R.drawable.taizhou_treasure_icon_11, R.drawable.taizhou_treasure_icon_12,
        R.drawable.taizhou_treasure_icon_13, R.drawable.taizhou_treasure_icon_14,
        R.drawable.taizhou_treasure_icon_15,
        R.drawable.taizhou_treasure_icon_16
    };
    private static final int[] QUALITY_RESOURCES = {
        R.drawable.taizhou_treasure_quality_0, R.drawable.taizhou_treasure_quality_1,
        R.drawable.taizhou_treasure_quality_2, R.drawable.taizhou_treasure_quality_3,
        R.drawable.taizhou_treasure_quality_4
    };
    private static final int[] QUALITY_LABEL_RESOURCES = {
        R.drawable.taizhou_treasure_quality_label_1, R.drawable.taizhou_treasure_quality_label_2,
        R.drawable.taizhou_treasure_quality_label_3,
        R.drawable.taizhou_treasure_quality_label_4
    };

    record Frame(float sceneSeconds, String mainAnimation, float mainSeconds,
            float collapseSeconds, int tooltipIndex) {}

    private final Paint bitmapPaint = new Paint(
            Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ColorMatrixColorFilter grayscale;
    private final Bitmap background, pedestal, back, title, help, myButton;
    private final Bitmap fortuneBanner, drawOne, drawFive, discount, diamond;
    private final Bitmap diamondBackground, tooltipBackground;
    private final Bitmap[] icons = new Bitmap[16], qualities = new Bitmap[5];
    private final Bitmap[] qualityLabels = new Bitmap[4];
    private final TaizhouTreasureFonts fonts;
    private final TaizhouTreasureEffects effects;
    private final TaizhouTreasureNodeTimeline timeline;
    private final float[] collapseDelay = new float[16], collapseX = new float[16], collapseY = new float[16];

    TaizhouTreasurePotRenderer(Resources resources) {
        background = load(resources, R.drawable.taizhou_treasure_background);
        pedestal = load(resources, R.drawable.taizhou_treasure_pedestal);
        back = load(resources, R.drawable.taizhou_treasure_back);
        title = load(resources, R.drawable.taizhou_treasure_title);
        help = load(resources, R.drawable.taizhou_treasure_help);
        myButton = load(resources, R.drawable.taizhou_treasure_my_button);
        fortuneBanner = load(resources, R.drawable.taizhou_treasure_fortune);
        drawOne = load(resources, R.drawable.taizhou_treasure_draw_one);
        drawFive = load(resources, R.drawable.taizhou_treasure_draw_five);
        discount = load(resources, R.drawable.taizhou_treasure_discount);
        diamond = load(resources, R.drawable.taizhou_treasure_diamond);
        diamondBackground = load(resources, R.drawable.taizhou_treasure_diamond_bg);
        tooltipBackground = load(resources, R.drawable.taizhou_treasure_tooltip_bg);
        for (int index = 0; index < icons.length; index++) {
            icons[index] = load(resources, ICON_RESOURCES[index]);
        }
        for (int index = 0; index < qualities.length; index++) {
            qualities[index] = load(resources, QUALITY_RESOURCES[index]);
        }
        for (int index = 0; index < qualityLabels.length; index++) {
            qualityLabels[index] = load(resources, QUALITY_LABEL_RESOURCES[index]);
        }
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0.0f);
        grayscale = new ColorMatrixColorFilter(matrix);
        fonts = TaizhouTreasureFonts.load(resources);
        effects = new TaizhouTreasureEffects(resources.getAssets());
        timeline = TaizhouTreasureNodeTimeline.loadMain(resources);
        textPaint.setTypeface(fonts.text());
        prepareCollapse();
    }

    void draw(Canvas canvas, FortuneState state, Frame frame) {
        Map<String, FortuneState.Treasure> active = activeTreasures(state);
        drawNode(canvas, background, TaizhouTreasurePotLayout.BACKGROUND,
                pose("bg", frame), 1.0f, null);
        drawNode(canvas, pedestal, TaizhouTreasurePotLayout.PEDESTAL,
                pose("di", frame), 1.0f, null);
        if ("loop".equals(frame.mainAnimation())) {
            effects.draw(canvas, TaizhouTreasureEffects.MAIN, frame.mainAnimation(),
                    frame.mainSeconds(), 960.0f, 540.0f, 1.0f, 1.0f);
        } else {
            effects.drawOnce(canvas, TaizhouTreasureEffects.MAIN, frame.mainAnimation(),
                    frame.mainSeconds(), 960.0f, 540.0f, 1.0f, 1.0f);
        }

        float controlsAlpha = controlsAlpha(frame.collapseSeconds());
        drawNode(canvas, myButton, TaizhouTreasurePotLayout.MY_TREASURES_ART,
                pose("xx", frame), controlsAlpha, null);
        drawFortune(canvas, active, frame, controlsAlpha);
        drawDrawButton(canvas, false, state, frame, controlsAlpha);
        drawDrawButton(canvas, true, state, frame, controlsAlpha);
        for (int index = 1; index <= 16; index++) {
            drawTreasure(canvas, index, active.get(code(index)), frame);
        }
        if (frame.tooltipIndex() > 0 && frame.collapseSeconds() < 0.0f) {
            drawTooltip(canvas, frame.tooltipIndex());
        }
        drawEntryAndButtonEffects(canvas, frame, controlsAlpha);
        drawTopPanels(canvas, state, frame, controlsAlpha);
    }

    void prepareCollapse() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int index = 0; index < 16; index++) {
            collapseDelay[index] = Math.min(Math.max(random.nextFloat(), 0.1f), 0.3f);
            collapseX[index] = 960.0f + random.nextFloat() * 200.0f;
            collapseY[index] = 375.7f - random.nextFloat() * 100.0f;
        }
    }

    float animationDuration(String animation) {
        return effects.animationDuration(TaizhouTreasureEffects.MAIN, animation);
    }

    float revealEventTime() {
        return effects.eventTime(TaizhouTreasureEffects.MAIN, "cj", "cx");
    }

    void release() {
        recycle(background, pedestal, back, title, help, myButton, fortuneBanner,
                drawOne, drawFive, discount, diamond, diamondBackground, tooltipBackground);
        recycle(icons);
        recycle(qualities);
        recycle(qualityLabels);
        effects.release();
    }

    private void drawFortune(
            Canvas canvas,
            Map<String, FortuneState.Treasure> active,
            Frame frame,
            float controlsAlpha) {
        TaizhouTreasureNodeTimeline.NodePose pose = pose("xx", frame);
        drawNode(canvas, fortuneBanner, TaizhouTreasurePotLayout.FORTUNE_BANNER,
                pose, controlsAlpha, null);
        int fortune = 0;
        for (FortuneState.Treasure treasure : active.values()) {
            TaizhouTreasureCatalog.Item item =
                    TaizhouTreasureCatalog.itemForCode(treasure.treasureCode());
            if (item != null) fortune += item.fortunePerLevel() * treasure.level();
        }
        drawFontWithPose(canvas, fonts.fortune(), "+" + fortune,
                997.836f, 128.14f, pose, controlsAlpha, true);
    }

    private void drawDrawButton(
            Canvas canvas, boolean five, FortuneState state, Frame frame, float controlsAlpha) {
        String bone = five ? "btn2" : "btn";
        TaizhouTreasureNodeTimeline.NodePose pose = pose(bone, frame);
        drawNode(canvas, five ? drawFive : drawOne,
                five ? TaizhouTreasurePotLayout.DRAW_FIVE_ART
                        : TaizhouTreasurePotLayout.DRAW_ONE_ART,
                pose, controlsAlpha, null);
        TaizhouTreasurePotLayout.Node label = five
                ? TaizhouTreasurePotLayout.DRAW_FIVE_LABEL
                : TaizhouTreasurePotLayout.DRAW_ONE_LABEL;
        TaizhouTreasurePotLayout.Node gem = five
                ? TaizhouTreasurePotLayout.DRAW_FIVE_DIAMOND
                : TaizhouTreasurePotLayout.DRAW_ONE_DIAMOND;
        TaizhouTreasurePotLayout.Node price = five
                ? TaizhouTreasurePotLayout.DRAW_FIVE_PRICE
                : TaizhouTreasurePotLayout.DRAW_ONE_PRICE;
        drawFontWithPose(canvas, fonts.drawCount(), five ? "抽 5 次" : "抽 1 次",
                label.centerX(), label.centerY(), pose, controlsAlpha, false);
        drawNode(canvas, diamond, gem, pose, controlsAlpha, null);
        long amount = five ? state.treasureFiveDrawPriceDiamonds()
                : state.treasureOneDrawPriceDiamonds();
        drawFontLeftWithPose(canvas, fonts.diamondPrice(), Long.toString(amount),
                price.left(), price.centerY(), pose, controlsAlpha);
        if (five) drawDiscount(canvas, state, frame, controlsAlpha, pose);
    }

    private void drawDiscount(
            Canvas canvas,
            FortuneState state,
            Frame frame,
            float controlsAlpha,
            TaizhouTreasureNodeTimeline.NodePose parent) {
        TaizhouTreasureNodeTimeline.NodePose own = pose("zk", frame);
        TaizhouTreasureNodeTimeline.NodePose combined = combine(parent, own);
        drawNode(canvas, discount, TaizhouTreasurePotLayout.DRAW_FIVE_DISCOUNT,
                combined, controlsAlpha, null);
        drawText(canvas, state.treasureFiveDrawDiscountTenths() + "折",
                TaizhouTreasurePotLayout.DRAW_FIVE_DISCOUNT_TEXT.centerX(),
                TaizhouTreasurePotLayout.DRAW_FIVE_DISCOUNT_TEXT.centerY(),
                40.0f, Color.WHITE, combined, controlsAlpha);
    }

    private void drawTreasure(
            Canvas canvas,
            int index,
            FortuneState.Treasure treasure,
            Frame frame) {
        TaizhouTreasureCatalog.Item item = TaizhouTreasureCatalog.item(index);
        TaizhouTreasureNodeTimeline.NodePose entrance = pose("bw" + index, frame);
        float centerX = TaizhouTreasurePotLayout.item(index).centerX();
        float centerY = TaizhouTreasurePotLayout.item(index).centerY();
        float collapseScale = 1.0f;
        if (frame.collapseSeconds() >= 0.0f) {
            float delay = collapseDelay[index - 1];
            float progress = easeSineIn((frame.collapseSeconds() - delay)
                    / Math.max(0.0001f, COLLAPSE_SECONDS - delay));
            centerX = lerp(centerX, collapseX[index - 1], progress);
            centerY = lerp(centerY, collapseY[index - 1], progress);
            collapseScale = lerp(1.0f, 0.01f, progress);
        }
        float alpha = entrance.alpha();
        boolean owned = treasure != null;
        int quality = item == null ? 0 : item.quality();
        RectF frameRect = centered(centerX, centerY, 163.0f, 164.0f,
                entrance.scaleX() * collapseScale, entrance.scaleY() * collapseScale);
        RectF iconRect = centered(centerX, centerY - 5.1f, 156.0f, 156.0f,
                entrance.scaleX() * collapseScale, entrance.scaleY() * collapseScale);
        drawBitmap(canvas, qualities[Math.max(0, Math.min(4, quality))], frameRect,
                alpha, owned ? null : grayscale);
        drawBitmap(canvas, icons[index - 1], iconRect, alpha, owned ? null : grayscale);
        if (owned && collapseScale > 0.15f) {
            String level = treasure.level() < 10 ? "Lv:" + treasure.level() : "满级";
            float effectScaleX = entrance.scaleX() * collapseScale;
            float effectScaleY = entrance.scaleY() * collapseScale;
            drawFontScaledAlpha(canvas, fonts.level(), level, centerX,
                    centerY + 42.5f, effectScaleX, effectScaleY, alpha, false);
            int layer = saveAlphaLayer(canvas, alpha);
            effects.draw(canvas, TaizhouTreasureEffects.QUALITY, item.animationName(),
                    frame.sceneSeconds(), centerX, centerY, effectScaleX, effectScaleY);
            canvas.restoreToCount(layer);
        }
    }

    private void drawTooltip(Canvas canvas, int index) {
        TaizhouTreasureCatalog.Item item = TaizhouTreasureCatalog.item(index);
        if (item == null) return;
        TaizhouTreasurePotLayout.Node node = TaizhouTreasurePotLayout.item(index);
        boolean flip = TaizhouTreasureCatalog.flipsTooltip(index);
        float originX = node.centerX() - (flip ? 520.0f : 280.0f);
        float backgroundX = node.centerX() + (flip ? -120.0f : 120.0f);
        int save = canvas.save();
        if (flip) {
            canvas.scale(-1.0f, 1.0f, backgroundX, node.centerY() - 36.0f);
        }
        TaizhouTreasureCanvas.drawNineSlice(
                canvas,
                tooltipBackground,
                centered(backgroundX, node.centerY() - 36.0f,
                        421.0f, 100.0f, 1.0f, 1.0f),
                127, 16, 17, 11, bitmapPaint);
        canvas.restoreToCount(save);
        drawBitmap(canvas, qualityLabels[item.quality() - 1],
                centered(originX + 563.709f, node.centerY() - 65.269f,
                        91.0f, 41.0f, 1.0f, 1.0f), 1.0f, null);
        drawPlainText(canvas, TaizhouTreasureCatalog.qualityName(item.quality()),
                originX + 565.425f, node.centerY() - 65.452f, 34.0f, Color.WHITE);
        drawPlainText(canvas, item.name(), originX + 396.84f,
                node.centerY() - 62.845f, 46.0f, item.titleColor());
        drawPlainText(canvas, item.title(), originX + 408.595f,
                node.centerY() - 20.388f, 34.0f, Color.WHITE);
    }

    private void drawEntryAndButtonEffects(Canvas canvas, Frame frame, float alpha) {
        float entryDuration = effects.animationDuration(
                TaizhouTreasureEffects.ENTRY_LIGHT, "animation");
        if (frame.sceneSeconds() <= entryDuration) {
            effects.drawOnce(canvas, TaizhouTreasureEffects.ENTRY_LIGHT, "animation",
                    frame.sceneSeconds(), 960.0f, 540.0f, 1.0f, 1.0f);
        }
        if (frame.sceneSeconds() < 3.0f || alpha <= 0.0f) return;
        float elapsed = (frame.sceneSeconds() - 3.0f) % 3.0f;
        float duration = effects.animationDuration(TaizhouTreasureEffects.BUTTON_SWEEP, "animation");
        if (elapsed <= duration) {
            effects.drawOnce(canvas, TaizhouTreasureEffects.BUTTON_SWEEP, "animation", elapsed,
                    TaizhouTreasurePotLayout.DRAW_ONE.centerX(),
                    TaizhouTreasurePotLayout.DRAW_ONE.centerY(), 1.0f, 1.0f);
            effects.drawOnce(canvas, TaizhouTreasureEffects.BUTTON_SWEEP, "animation", elapsed,
                    TaizhouTreasurePotLayout.DRAW_FIVE.centerX(),
                    TaizhouTreasurePotLayout.DRAW_FIVE.centerY(), 1.0f, 1.0f);
        }
    }

    private void drawTopPanels(
            Canvas canvas, FortuneState state, Frame frame, float controlsAlpha) {
        float yOffset = -200.0f * (1.0f - clamp(frame.sceneSeconds() / 0.5f));
        drawBitmap(canvas, back, offset(TaizhouTreasurePotLayout.BACK_ART, yOffset),
                controlsAlpha, null);
        drawBitmap(canvas, title, offset(TaizhouTreasurePotLayout.TITLE, yOffset),
                controlsAlpha, null);
        drawBitmap(canvas, help, offset(TaizhouTreasurePotLayout.HELP_ART, yOffset),
                controlsAlpha, null);
        drawBitmap(canvas, diamondBackground,
                offset(TaizhouTreasurePotLayout.DIAMOND_BACKGROUND, yOffset),
                controlsAlpha, null);
        drawBitmap(canvas, diamond, offset(TaizhouTreasurePotLayout.DIAMOND_ICON, yOffset),
                controlsAlpha, null);
        drawPlainText(canvas, Long.toString(state.wallet().diamonds()),
                TaizhouTreasurePotLayout.DIAMOND_TEXT.left(),
                TaizhouTreasurePotLayout.DIAMOND_TEXT.centerY() + yOffset,
                39.0f, Color.WHITE, Paint.Align.LEFT, controlsAlpha);
    }

    private TaizhouTreasureNodeTimeline.NodePose pose(String name, Frame frame) {
        if (!"cx".equals(frame.mainAnimation())) {
            return new TaizhouTreasureNodeTimeline.NodePose(1.0f, 1.0f, 1.0f);
        }
        return timeline.pose(name, frame.mainSeconds());
    }

    private void drawNode(
            Canvas canvas,
            Bitmap bitmap,
            TaizhouTreasurePotLayout.Node node,
            TaizhouTreasureNodeTimeline.NodePose pose,
            float alpha,
            android.graphics.ColorFilter filter) {
        drawBitmap(canvas, bitmap,
                centered(node.centerX(), node.centerY(), node.width(), node.height(),
                        pose.scaleX(), pose.scaleY()),
                alpha * pose.alpha(), filter);
    }

    private void drawFontWithPose(
            Canvas canvas,
            SxvipBitmapFont font,
            String value,
            float x,
            float y,
            TaizhouTreasureNodeTimeline.NodePose pose,
            float alpha,
            boolean left) {
        if (alpha * pose.alpha() <= 0.01f) return;
        drawFontScaledAlpha(canvas, font, value, x, y,
                pose.scaleX(), pose.scaleY(), alpha * pose.alpha(), left);
    }

    private void drawFontLeftWithPose(
            Canvas canvas,
            SxvipBitmapFont font,
            String value,
            float x,
            float y,
            TaizhouTreasureNodeTimeline.NodePose pose,
            float alpha) {
        drawFontWithPose(canvas, font, value, x, y, pose, alpha, true);
    }

    private static void drawFontScaledAlpha(
            Canvas canvas,
            SxvipBitmapFont font,
            String value,
            float x,
            float y,
            float scaleX,
            float scaleY,
            float alpha,
            boolean left) {
        int layer = saveAlphaLayer(canvas, alpha);
        int transform = canvas.save();
        canvas.scale(scaleX, scaleY, x, y);
        if (left) font.drawLeft(canvas, value, x, y);
        else font.drawCentered(canvas, value, x, y);
        canvas.restoreToCount(transform);
        canvas.restoreToCount(layer);
    }

    private void drawText(
            Canvas canvas,
            String value,
            float x,
            float y,
            float size,
            int color,
            TaizhouTreasureNodeTimeline.NodePose pose,
            float alpha) {
        int save = canvas.save();
        canvas.scale(pose.scaleX(), pose.scaleY(), x, y);
        drawPlainText(canvas, value, x, y, size, color, Paint.Align.CENTER,
                alpha * pose.alpha());
        canvas.restoreToCount(save);
    }

    private void drawPlainText(
            Canvas canvas, String value, float x, float centerY, float size, int color) {
        drawPlainText(canvas, value, x, centerY, size, color, Paint.Align.CENTER, 1.0f);
    }

    private void drawPlainText(
            Canvas canvas,
            String value,
            float x,
            float centerY,
            float size,
            int color,
            Paint.Align align,
            float alpha) {
        textPaint.setTextAlign(align);
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        textPaint.setAlpha(Math.round(255.0f * clamp(alpha)));
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(value == null ? "" : value, x,
                centerY - (metrics.ascent + metrics.descent) * 0.5f, textPaint);
        textPaint.setAlpha(255);
    }

    private void drawBitmap(
            Canvas canvas,
            Bitmap bitmap,
            RectF destination,
            float alpha,
            android.graphics.ColorFilter filter) {
        if (bitmap == null || bitmap.isRecycled() || alpha <= 0.001f) return;
        bitmapPaint.setAlpha(Math.round(255.0f * clamp(alpha)));
        bitmapPaint.setColorFilter(filter);
        canvas.drawBitmap(bitmap, null, destination, bitmapPaint);
        bitmapPaint.setColorFilter(null);
        bitmapPaint.setAlpha(255);
    }

    private static Map<String, FortuneState.Treasure> activeTreasures(FortuneState state) {
        Map<String, FortuneState.Treasure> result = new LinkedHashMap<>();
        for (FortuneState.Treasure treasure : state.treasures()) {
            if (treasure.remainingSeconds() > 0) result.put(treasure.treasureCode(), treasure);
        }
        return result;
    }

    private static String code(int index) {
        return String.format("TREASURE_%02d", index);
    }

    private static float controlsAlpha(float collapseSeconds) {
        return collapseSeconds < 0.0f ? 1.0f
                : 1.0f - clamp(collapseSeconds / COLLAPSE_SECONDS);
    }

    private static TaizhouTreasureNodeTimeline.NodePose combine(
            TaizhouTreasureNodeTimeline.NodePose parent,
            TaizhouTreasureNodeTimeline.NodePose child) {
        return new TaizhouTreasureNodeTimeline.NodePose(
                parent.scaleX() * child.scaleX(),
                parent.scaleY() * child.scaleY(),
                parent.alpha() * child.alpha());
    }

    private static RectF centered(
            float x, float y, float width, float height, float scaleX, float scaleY) {
        float halfWidth = width * scaleX * 0.5f;
        float halfHeight = height * scaleY * 0.5f;
        return new RectF(x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight);
    }

    private static RectF offset(TaizhouTreasurePotLayout.Node node, float y) {
        return new RectF(node.left(), node.top() + y, node.right(), node.bottom() + y);
    }

    private static float easeSineIn(float value) {
        float clamped = clamp(value);
        return 1.0f - (float) Math.cos(clamped * Math.PI * 0.5);
    }

    private static float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static int saveAlphaLayer(Canvas canvas, float alpha) {
        return canvas.saveLayerAlpha(
                null, Math.round(255.0f * clamp(alpha)));
    }

    private static Bitmap load(Resources resources, int resource) {
        return BitmapFactory.decodeResource(resources, resource);
    }

    private static void recycle(Bitmap... bitmaps) {
        for (Bitmap bitmap : bitmaps) {
            if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
        }
    }
}
