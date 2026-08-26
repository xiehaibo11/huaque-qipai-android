package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import java.util.HashMap;
import java.util.Map;

/** Shared renderer for the original SxvipShopLayer/SxvipShopItem membership product card. */
final class MembershipProductCardRenderer {
    static final float CARD_WIDTH = 468f;
    static final float CARD_HEIGHT = 799f;
    static final float BUY_TOP = 699f;
    private static final float BUY_PRICE_CENTER_X = (3f + 465f) * 0.5f;
    private static final float BUY_PRICE_CENTER_Y = (BUY_TOP + CARD_HEIGHT) * 0.5f;
    private static final float GIFT_VALUE_CENTER_X = 229.1622f;
    private static final float GIFT_VALUE_CENTER_Y = 313.8708f;
    private static final float DAY_COUNT_CENTER_X = 126.8992f;
    private static final float DAY_COUNT_CENTER_Y = 62.1422f;
    private static final float MEMBER_PLATE_CENTER_X = 273.0529f;
    private static final float MEMBER_PLATE_CENTER_Y = 60.4724f;

    private final Resources resources;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint textPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();
    private final Typeface originalTypeface;
    private final Map<Integer, Bitmap> bitmapCache = new HashMap<>();

    private final Bitmap cardRedBitmap;
    private final Bitmap cardGreenBitmap;
    private final Bitmap cardPurpleBitmap;
    private final Bitmap tagHotBitmap;
    private final Bitmap tagValueBitmap;
    private final Bitmap memberPlateBitmap;
    private final Bitmap buyButtonBitmap;
    private final Bitmap propCellRedBitmap;
    private final Bitmap propCellGreenBitmap;
    private final Bitmap propCellPurpleBitmap;
    private final Bitmap vipProtocolBitmap;
    private final Bitmap userProtocolBitmap;
    private final Bitmap renewProtocolBitmap;
    private final Bitmap restoreBitmap;
    private final SxvipBitmapFont giftValueFont;
    private final SxvipBitmapFont dayCountFont;

    MembershipProductCardRenderer(Resources resources) {
        this.resources = resources;
        originalTypeface = loadOriginalTypeface(resources);
        cardRedBitmap = loadBitmap(R.drawable.svip_buy_card_red);
        cardGreenBitmap = loadBitmap(R.drawable.svip_buy_card_green);
        cardPurpleBitmap = loadBitmap(R.drawable.svip_buy_card_purple);
        tagHotBitmap = loadBitmap(R.drawable.svip_buy_tag_hot);
        tagValueBitmap = loadBitmap(R.drawable.svip_buy_tag_value);
        memberPlateBitmap = loadBitmap(R.drawable.svip_buy_plate_huiyuan);
        buyButtonBitmap = loadBitmap(R.drawable.svip_buy_btn_buy);
        propCellRedBitmap = loadBitmap(R.drawable.svip_buy_prop_cell_red);
        propCellGreenBitmap = loadBitmap(R.drawable.svip_buy_prop_cell_green);
        propCellPurpleBitmap = loadBitmap(R.drawable.svip_buy_prop_cell_purple);
        vipProtocolBitmap = loadBitmap(R.drawable.svip_buy_protocol_vip);
        userProtocolBitmap = loadBitmap(R.drawable.svip_buy_protocol_user);
        renewProtocolBitmap = loadBitmap(R.drawable.svip_buy_protocol_renew);
        restoreBitmap = loadBitmap(R.drawable.svip_buy_restore);
        giftValueFont = SxvipBitmapFont.load(getResources(), "membership/Txt_jz-export.fnt");
        dayCountFont = SxvipBitmapFont.load(getResources(), "membership/Txt_dsz-export.fnt");
    }

    void drawCard(
            Canvas canvas,
            MembershipPurchasePlan plan,
            float left,
            float top,
            float scale) {
        canvas.save();
        canvas.translate(left, top);
        canvas.scale(scale, scale);
        drawBitmap(canvas, cardBackground(plan.tint()), new RectF(0f, 0f, CARD_WIDTH, CARD_HEIGHT));
        drawTag(canvas, plan);
        drawPlanHeader(canvas, plan);
        drawPlanBenefits(canvas);
        drawGiftValue(canvas, plan);
        drawRewards(canvas, plan);
        drawDayCost(canvas, plan);
        drawBuyButton(canvas, plan);
        canvas.restore();
    }

    void drawPrice(
            Canvas canvas,
            MembershipPurchasePlan plan,
            float left,
            float top,
            float scale) {
        canvas.save();
        canvas.translate(left, top);
        canvas.scale(scale, scale);
        drawBuyButtonPrice(canvas, plan);
        canvas.restore();
    }

    void drawFooter(
            Canvas canvas,
            float originX,
            float originY,
            float scale,
            boolean inCollection) {
        canvas.save();
        canvas.translate(originX, originY);
        canvas.scale(scale, scale);
        drawCenteredText(
                canvas,
                "连续包月可随时取消",
                49.4586f,
                1017.1244f,
                34f,
                inCollection ? Color.argb(127, 0, 0, 0) : Color.argb(150, 255, 255, 255),
                Paint.Align.LEFT,
                false);
        drawBitmapCentered(canvas, vipProtocolBitmap, 1209.434f, 1018.67f);
        drawBitmapCentered(canvas, userProtocolBitmap, 1392.7685f, 1018.67f);
        drawBitmapCentered(canvas, renewProtocolBitmap, 1576.1069f, 1018.67f);
        drawBitmapCentered(canvas, restoreBitmap, 1759.4417f, 1018.67f);
        canvas.restore();
    }

    private void drawPlanHeader(Canvas canvas, MembershipPurchasePlan plan) {
        drawBitmapCentered(canvas, memberPlateBitmap, MEMBER_PLATE_CENTER_X, MEMBER_PLATE_CENTER_Y);
        drawBitmapFontCentered(
                canvas,
                dayCountFont,
                String.valueOf(plan.days()),
                DAY_COUNT_CENTER_X,
                DAY_COUNT_CENTER_Y,
                plan.days() >= 100 ? 0.75f : 1.0f);
    }

    private void drawPlanBenefits(Canvas canvas) {
        drawStrokeFillText(
                canvas,
                "15项",
                205f,
                183f,
                52f,
                Color.rgb(255, 255, 120),
                Color.rgb(34, 130, 97),
                5f);
        drawStrokeFillText(
                canvas, "游戏特权", 350f, 183f, 38f, Color.WHITE, Color.rgb(34, 130, 97), 4f);
        drawStrokeFillText(
                canvas, "每日领", 223f, 244f, 34f, Color.WHITE, Color.rgb(77, 151, 116), 4f);
        drawStrokeFillText(
                canvas,
                "18元",
                323f,
                244f,
                34f,
                Color.rgb(255, 248, 87),
                Color.rgb(77, 151, 116),
                4f);
        drawStrokeFillText(
                canvas, "礼品", 403f, 244f, 34f, Color.WHITE, Color.rgb(77, 151, 116), 4f);
    }

    private void drawGiftValue(Canvas canvas, MembershipPurchasePlan plan) {
        drawBitmapFontCentered(
                canvas, giftValueFont, plan.giftValueText(), GIFT_VALUE_CENTER_X, GIFT_VALUE_CENTER_Y);
    }

    private void drawRewards(Canvas canvas, MembershipPurchasePlan plan) {
        if (plan.rewards().size() == 4) {
            drawCompactRewards(canvas, plan);
            return;
        }
        float[] xs = {26f, 161f, 296f};
        float[] ys = {351f, 496f};
        for (int index = 0; index < plan.rewards().size(); index++) {
            drawReward(
                    canvas,
                    plan.rewards().get(index),
                    xs[index % 3],
                    ys[index / 3],
                    propCell(plan.tint()));
        }
    }

    private void drawCompactRewards(Canvas canvas, MembershipPurchasePlan plan) {
        float[] xs = {82f, 254f};
        float[] ys = {351f, 496f};
        for (int index = 0; index < plan.rewards().size(); index++) {
            drawReward(
                    canvas,
                    plan.rewards().get(index),
                    xs[index % 2],
                    ys[index / 2],
                    propCellPurpleBitmap);
        }
    }

    private void drawReward(
            Canvas canvas,
            MembershipPurchasePlan.Reward reward,
            float left,
            float top,
            Bitmap cellBitmap) {
        drawBitmap(canvas, cellBitmap, new RectF(left, top, left + 132f, top + 140f));
        drawText(
                canvas,
                reward.name(),
                left + 66f,
                top + 31f,
                28f,
                Color.rgb(89, 48, 27),
                Paint.Align.CENTER,
                true);
        drawBitmap(
                canvas,
                loadBitmap(reward.iconResId()),
                new RectF(left + 26f, top + 47f, left + 106f, top + 127f));
        drawText(
                canvas,
                reward.count(),
                left + 66f,
                top + 128f,
                28f,
                Color.rgb(143, 58, 37),
                Paint.Align.CENTER,
                true);
    }

    private void drawDayCost(Canvas canvas, MembershipPurchasePlan plan) {
        if ("SXVIP_CONTINUOUS_MONTH".equals(plan.productCode())) {
            drawBitmap(
                    canvas,
                    loadBitmap(R.drawable.svip_buy_day_cost_flame),
                    new RectF(78.5f, 652f, 389.5f, 717f));
            Bitmap glow = loadBitmap(R.drawable.svip_buy_day_cost_highlight);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
            drawBitmap(canvas, glow, new RectF(109.5f, 656f, 358.5f, 692f));
            drawBitmap(canvas, glow, new RectF(109.5f, 672f, 358.5f, 708f));
            paint.setXfermode(null);
        }
        drawText(
                canvas,
                plan.dayCost(),
                234f,
                680f,
                37f,
                Color.rgb(101, 135, 111),
                Paint.Align.CENTER,
                true);
    }

    private void drawBuyButton(Canvas canvas, MembershipPurchasePlan plan) {
        drawBitmap(canvas, buyButtonBitmap, new RectF(3f, BUY_TOP, 465f, CARD_HEIGHT));
        drawBuyButtonPrice(canvas, plan);
    }

    private void drawBuyButtonPrice(Canvas canvas, MembershipPurchasePlan plan) {
        drawCenteredText(
                canvas,
                plan.price(),
                BUY_PRICE_CENTER_X,
                BUY_PRICE_CENTER_Y,
                48f,
                Color.rgb(37, 34, 48),
                Paint.Align.CENTER,
                true);
    }

    private void drawTag(Canvas canvas, MembershipPurchasePlan plan) {
        Bitmap tag =
                plan.tag() == MembershipPurchasePlan.Tag.HOT
                        ? tagHotBitmap
                        : plan.tag() == MembershipPurchasePlan.Tag.VALUE ? tagValueBitmap : null;
        if (tag != null) {
            drawBitmap(canvas, tag, new RectF(354f, 31.5f, 453f, 148.5f));
        }
    }

    private Bitmap cardBackground(MembershipPurchasePlan.CardTint tint) {
        return switch (tint) {
            case RED -> cardRedBitmap;
            case GREEN -> cardGreenBitmap;
            case PURPLE -> cardPurpleBitmap;
        };
    }

    private Bitmap propCell(MembershipPurchasePlan.CardTint tint) {
        return switch (tint) {
            case RED -> propCellRedBitmap;
            case GREEN -> propCellGreenBitmap;
            case PURPLE -> propCellPurpleBitmap;
        };
    }

    private void drawBitmapCentered(Canvas canvas, Bitmap bitmap, float centerX, float centerY) {
        if (bitmap == null) {
            return;
        }
        drawBitmap(
                canvas,
                bitmap,
                new RectF(
                        centerX - bitmap.getWidth() * 0.5f,
                        centerY - bitmap.getHeight() * 0.5f,
                        centerX + bitmap.getWidth() * 0.5f,
                        centerY + bitmap.getHeight() * 0.5f));
    }

    private void drawBitmapFontCentered(
            Canvas canvas, SxvipBitmapFont font, String text, float centerX, float centerY) {
        drawBitmapFontCentered(canvas, font, text, centerX, centerY, 1.0f);
    }

    private void drawBitmapFontCentered(
            Canvas canvas,
            SxvipBitmapFont font,
            String text,
            float centerX,
            float centerY,
            float scale) {
        font.drawCentered(canvas, text, centerX, centerY, scale);
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

    private void drawStrokeFillText(
            Canvas canvas,
            String text,
            float x,
            float y,
            float size,
            int fillColor,
            int strokeColor,
            float strokeWidth) {
        textPaint.setTypeface(originalTypeface);
        textPaint.setTextSize(size);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(strokeWidth);
        textPaint.setColor(strokeColor);
        canvas.drawText(text, x, y, textPaint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(0f);
        textPaint.setColor(fillColor);
        canvas.drawText(text, x, y, textPaint);
    }

    private void drawCenteredText(
            Canvas canvas,
            String text,
            float x,
            float centerY,
            float size,
            int color,
            Paint.Align align,
            boolean bold) {
        textPaint.setTypeface(
                bold ? Typeface.create(originalTypeface, Typeface.BOLD) : originalTypeface);
        textPaint.setTextSize(size);
        textPaint.setTextAlign(align);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(0f);
        textPaint.clearShadowLayer();
        textPaint.setColor(color);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(text, x, centerY - (metrics.ascent + metrics.descent) * 0.5f, textPaint);
    }

    private void drawText(
            Canvas canvas,
            String text,
            float x,
            float y,
            float size,
            int color,
            Paint.Align align,
            boolean bold) {
        textPaint.setTypeface(
                bold ? Typeface.create(originalTypeface, Typeface.BOLD) : originalTypeface);
        textPaint.setTextSize(size);
        textPaint.setTextAlign(align);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(0f);
        textPaint.clearShadowLayer();
        textPaint.setColor(color);
        canvas.drawText(text, x, y, textPaint);
    }

    private Bitmap loadBitmap(int resId) {
        return bitmapCache.computeIfAbsent(
                resId, key -> BitmapFactory.decodeResource(resources, key));
    }

    private Resources getResources() {
        return resources;
    }

    private static Typeface loadOriginalTypeface(Resources resources) {
        try {
            return Typeface.createFromAsset(
                    resources.getAssets(), "fonts/fangzhengcuyuan.ttf");
        } catch (RuntimeException exception) {
            return Typeface.DEFAULT_BOLD;
        }
    }
}
