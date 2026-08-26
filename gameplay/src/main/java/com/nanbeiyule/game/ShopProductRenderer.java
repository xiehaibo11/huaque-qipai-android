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
import java.util.List;
import java.util.Locale;
/** Product-card renderer split by the distinct original shop page families. */
final class ShopProductRenderer {
    private static final int NAVY = Color.rgb(39, 68, 122);
    private static final int MUTED_BLUE = Color.rgb(88, 119, 158);
    private static final int PRICE_BROWN = Color.rgb(157, 73, 15);
    private static final float GOLD_MEMBERSHIP_ICON_MAX_WIDTH = 360f;
    private static final float GOLD_MEMBERSHIP_ICON_MAX_HEIGHT = 340f;
    private final ShopDrawableSet drawables;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();
    private final Typeface typeface;
    ShopProductRenderer(ShopDrawableSet drawables) {
        this.drawables = drawables;
        typeface =
                Typeface.createFromAsset(
                        drawables.resources().getAssets(), "fonts/fangzhengcuyuan.ttf");
    }
    void draw(Canvas canvas, ShopCatalogState state, float productScroll) {
        List<ShopProduct> products = state.selectedProducts();
        for (int index = 0; index < products.size(); index++) {
            ShopProduct product = products.get(index);
            ShopLayout.Rect card =
                    ShopRuntimeLayout.productCard(
                            state.selectedCategory(), index, productScroll);
            switch (state.selectedCategory()) {
                case HOT_RECOMMENDATION -> drawHotProduct(canvas, product, card, index);
                case GOLD_MEMBERSHIP -> drawGoldMembershipProduct(canvas, product, card, index);
                default -> drawRegularProduct(canvas, product, card);
            }
        }
    }
    private void drawRegularProduct(
            Canvas canvas, ShopProduct product, ShopLayout.Rect card) {
        drawCardBody(canvas, card, Color.rgb(194, 226, 247), Color.rgb(255, 255, 246));
        drawWatermark(canvas, card);
        drawText(
                canvas,
                product.displayName(),
                card.centerX(),
                card.top() + 64f,
                43f,
                NAVY,
                Paint.Align.CENTER,
                true);
        drawRegularIcon(canvas, product, card);
        drawPriceBar(canvas, product, card, 78f);
    }
    private void drawRegularIcon(
            Canvas canvas, ShopProduct product, ShopLayout.Rect card) {
        Bitmap icon = drawables.productIcon(product);
        float maxWidth = 245f;
        float maxHeight = 245f;
        String code = product.productCode();
        if (code.startsWith("PROP_RECORDER_")) {
            maxWidth = 250f;
            maxHeight = 270f;
        }
        if (code.startsWith("DIAMOND_")) {
            long amount = product.rewardQuantity();
            if (amount >= 3000) {
                drawCenteredBitmap(
                        canvas,
                        drawables.diamondChest,
                        card.centerX(),
                        card.top() + 245f,
                        285f,
                        285f);
                return;
            }
            if (amount >= 300) {
                int count = amount >= 600 ? 3 : 2;
                float width = 130f;
                float height = width * icon.getHeight() / icon.getWidth();
                for (int index = 0; index < count; index++) {
                    float x = card.centerX() - width * 0.9f + index * width * 0.63f;
                    float y = card.top() + 135f + Math.abs(index - 1) * 20f;
                    drawBitmap(canvas, icon, x, y, width, height);
                }
                return;
            }
        }
        drawCenteredBitmap(canvas, icon, card.centerX(), card.top() + 245f, maxWidth, maxHeight);
    }
    private void drawHotProduct(
            Canvas canvas, ShopProduct product, ShopLayout.Rect card, int index) {
        boolean firstRecharge = product.productCode().equals("HOT_FIRST_RECHARGE");
        drawCardBody(
                canvas,
                card,
                firstRecharge ? Color.rgb(255, 224, 135) : Color.rgb(193, 227, 248),
                firstRecharge ? Color.rgb(255, 250, 223) : Color.rgb(255, 255, 248));
        drawWatermark(canvas, card);
        drawText(
                canvas,
                product.displayName(),
                card.centerX(),
                card.top() + 79f,
                57f,
                firstRecharge ? Color.rgb(177, 83, 19) : NAVY,
                Paint.Align.CENTER,
                true);
        String subtitle = hotSubtitle(product.productCode());
        if (!subtitle.isEmpty()) {
            drawText(
                    canvas,
                    subtitle,
                    card.centerX(),
                    card.top() + 137f,
                    35f,
                    MUTED_BLUE,
                    Paint.Align.CENTER,
                    true);
        }
        Bitmap icon = drawables.productIcon(product);
        drawCenteredBitmap(canvas, icon, card.centerX(), card.top() + 270f, 260f, 230f);
        drawHotRewards(canvas, product, card);
        drawPriceBar(canvas, product, card, 112f);
        if (index == 1) {
            paint.setColor(Color.rgb(240, 55, 29));
            canvas.drawCircle(card.right() - 25f, card.top() + 24f, 12f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3f);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(card.right() - 25f, card.top() + 24f, 12f, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }
    private void drawHotRewards(
            Canvas canvas, ShopProduct product, ShopLayout.Rect card) {
        float top = card.bottom() - 226f;
        float cellWidth = 104f;
        float gap = 16f;
        float total = cellWidth * 3f + gap * 2f;
        float left = card.centerX() - total * 0.5f;
        String[] labels = hotRewardLabels(product);
        Bitmap[] icons = {
            drawables.diamond,
            drawables.coin,
            drawables.productIcon(product)
        };
        for (int index = 0; index < 3; index++) {
            float x = left + index * (cellWidth + gap);
            paint.setColor(index == 1 ? Color.rgb(238, 206, 249) : Color.rgb(247, 250, 255));
            canvas.drawRoundRect(new RectF(x, top, x + cellWidth, top + 106f), 9f, 9f, paint);
            drawCenteredBitmap(canvas, icons[index], x + cellWidth * 0.5f, top + 47f, 70f, 58f);
            drawText(
                    canvas,
                    labels[index],
                    x + cellWidth * 0.5f,
                    top + 97f,
                    24f,
                    Color.BLACK,
                    Paint.Align.CENTER,
                    true);
        }
    }
    private void drawGoldMembershipProduct(
            Canvas canvas, ShopProduct product, ShopLayout.Rect card, int index) {
        int[] topColors = {
            Color.rgb(195, 222, 250),
            Color.rgb(253, 222, 124),
            Color.rgb(226, 190, 244)
        };
        drawCardBody(canvas, card, topColors[index % topColors.length], Color.rgb(255, 254, 234));
        drawText(canvas, product.displayName(), card.centerX(), card.top() + 72f, 46f,
                NAVY, Paint.Align.CENTER, true);
        drawCenteredBitmap(
                canvas,
                drawables.productIcon(product),
                card.centerX(),
                card.top() + 270f,
                GOLD_MEMBERSHIP_ICON_MAX_WIDTH,
                GOLD_MEMBERSHIP_ICON_MAX_HEIGHT);
        drawMembershipRewardLine(canvas, card, card.top() + 430f, "开通立赠",
                compact(product.rewardQuantity() * 25_000L) + "金币");
        drawMembershipRewardLine(canvas, card, card.top() + 495f, "连续领取",
                product.rewardQuantity() + "天福利");
        drawPriceBar(canvas, product, card, 95f);
    }
    private void drawCardBody(
            Canvas canvas, ShopLayout.Rect card, int topColor, int bottomColor) {
        RectF rect = new RectF(card.left(), card.top(), card.right(), card.bottom());
        paint.setShader(
                new LinearGradient(
                        card.left(),
                        card.top(),
                        card.left(),
                        card.bottom(),
                        topColor,
                        bottomColor,
                        Shader.TileMode.CLAMP));
        canvas.drawRoundRect(rect, 8f, 8f, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(Color.argb(205, 225, 243, 250));
        canvas.drawRoundRect(rect, 8f, 8f, paint);
        paint.setStyle(Paint.Style.FILL);
    }
    private void drawWatermark(Canvas canvas, ShopLayout.Rect card) {
        canvas.save();
        canvas.rotate(-90f, card.left() + 22f, card.centerY());
        drawText(
                canvas,
                "HUIYI  TEIBIEYOUHUI",
                card.left() + 22f,
                card.centerY() + 14f,
                25f,
                Color.argb(45, 255, 255, 255),
                Paint.Align.CENTER,
                true);
        canvas.restore();
    }
    private void drawPriceBar(
            Canvas canvas, ShopProduct product, ShopLayout.Rect card, float height) {
        drawPriceBarBackground(canvas, card, height);
        drawPriceText(canvas, product, card, height);
    }

    private void drawPriceBarBackground(
            Canvas canvas, ShopLayout.Rect card, float height) {
        float top = card.bottom() - height;
        paint.setShader(
                new LinearGradient(
                        card.left(),
                        top,
                        card.left(),
                        card.bottom(),
                        Color.rgb(255, 177, 67),
                        Color.rgb(255, 244, 124),
                        Shader.TileMode.CLAMP));
        canvas.drawRect(card.left(), top, card.right(), card.bottom(), paint);
        paint.setShader(null);
    }

    private void drawPriceText(
            Canvas canvas, ShopProduct product, ShopLayout.Rect card, float height) {
        float top = card.bottom() - height;
        if (product.currency() == ShopProduct.Currency.DIAMOND) {
            String value = compact(product.priceMinor());
            float textWidth = measuredWidth(value, 39f);
            float iconWidth = 45f;
            float total = iconWidth + 10f + textWidth;
            float left = card.centerX() - total * 0.5f;
            drawBitmap(canvas, drawables.diamond, left, top + (height - 36f) * 0.5f, iconWidth, 36f);
            drawCenteredBaselineText(canvas, value, left + iconWidth + 10f, (top + card.bottom()) * 0.5f,
                    39f, PRICE_BROWN, Paint.Align.LEFT, true);
        } else if (product.currency() == ShopProduct.Currency.ROOM_CARD) {
            String value = compact(product.priceMinor());
            float textWidth = measuredWidth(value, 39f);
            float iconWidth = 66f;
            float iconHeight = 51f;
            float total = iconWidth + 8f + textWidth;
            float left = card.centerX() - total * 0.5f;
            drawBitmap(
                    canvas,
                    drawables.headerRoomCardIcon,
                    left,
                    top + (height - iconHeight) * 0.5f,
                    iconWidth,
                    iconHeight);
            drawCenteredBaselineText(
                    canvas,
                    value,
                    left + iconWidth + 8f,
                    (top + card.bottom()) * 0.5f,
                    39f,
                    PRICE_BROWN,
                    Paint.Align.LEFT,
                    true);
        } else if (product.currency() == ShopProduct.Currency.COUPON) {
            drawCenteredText(canvas, "券 " + compact(product.priceMinor()), card.centerX(),
                    (top + card.bottom()) * 0.5f, 38f, PRICE_BROWN, true);
        } else {
            drawCenteredText(canvas, priceText(product), card.centerX(),
                    (top + card.bottom()) * 0.5f, 42f, PRICE_BROWN, true);
        }
    }
    private String priceText(ShopProduct product) {
        if (!product.enabled()) {
            return "暂未开放";
        }
        if (!product.available()) {
            return "已售罄";
        }
        if (product.currency() == ShopProduct.Currency.FREE) {
            return "免费领取";
        }
        if (product.dailyLimit() != null) {
            return formattedCny(product.priceMinor())
                    + "(限购"
                    + product.purchasedToday()
                    + "/"
                    + product.dailyLimit()
                    + ")";
        }
        if (product.productCode().equals("SXVIP_CONTINUOUS_MONTH")) {
            return "连续包月:28元";
        }
        if (product.priceMinor() % 100 == 0) {
            return (product.priceMinor() / 100) + "元";
        }
        return String.format(Locale.CHINA, "%.2f元", product.priceMinor() / 100f);
    }

    private static String formattedCny(long priceMinor) {
        return String.format(Locale.CHINA, "%.2f", priceMinor / 100f);
    }
    private void drawMembershipRewardLine(
            Canvas canvas, ShopLayout.Rect card, float baseline, String label, String value) {
        drawText(canvas, label, card.left() + 30f, baseline, 24f, MUTED_BLUE,
                Paint.Align.LEFT, true);
        drawText(canvas, value, card.right() - 30f, baseline, 24f, NAVY,
                Paint.Align.RIGHT, true);
    }
    private void drawCenteredBitmap(
            Canvas canvas,
            Bitmap bitmap,
            float centerX,
            float centerY,
            float maxWidth,
            float maxHeight) {
        float scale = Math.min(maxWidth / bitmap.getWidth(), maxHeight / bitmap.getHeight());
        float width = bitmap.getWidth() * scale;
        float height = bitmap.getHeight() * scale;
        drawBitmap(canvas, bitmap, centerX - width * 0.5f, centerY - height * 0.5f, width, height);
    }
    private void drawBitmap(
            Canvas canvas, Bitmap bitmap, float left, float top, float width, float height) {
        source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        destination.set(left, top, left + width, top + height);
        canvas.drawBitmap(bitmap, source, destination, paint);
    }
    private void drawCenteredText(
            Canvas canvas,
            String text,
            float centerX,
            float centerY,
            float size,
            int color,
            boolean bold) {
        textPaint.setTextSize(size);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        drawText(canvas, text, centerX, centerY - (metrics.ascent + metrics.descent) * 0.5f,
                size, color, Paint.Align.CENTER, bold);
    }

    private void drawCenteredBaselineText(
            Canvas canvas,
            String text,
            float x,
            float centerY,
            float size,
            int color,
            Paint.Align align,
            boolean bold) {
        textPaint.setTextSize(size);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        drawText(canvas, text, x, centerY - (metrics.ascent + metrics.descent) * 0.5f,
                size, color, align, bold);
    }

    private void drawText(
            Canvas canvas,
            String text,
            float x,
            float baseline,
            float size,
            int color,
            Paint.Align align,
            boolean bold) {
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        textPaint.setTextAlign(align);
        textPaint.setTypeface(bold ? typeface : Typeface.create(typeface, Typeface.NORMAL));
        canvas.drawText(text, x, baseline, textPaint);
    }

    private float measuredWidth(String text, float size) {
        textPaint.setTextSize(size);
        textPaint.setTypeface(typeface);
        return textPaint.measureText(text);
    }

    private static String hotSubtitle(String productCode) {
        return switch (productCode) {
            case "HOT_DAILY_BENEFIT" -> "领取获得";
            case "HOT_DAILY_GIFT" -> "购买立得翻倍金币";
            case "HOT_WEEK_GIFT", "HOT_MONTH_GIFT" -> "购买礼包立得";
            case "HOT_VALUE_MONTH_CARD" -> "购买月卡立得";
            case "GOLD_GIFT_6", "GOLD_GIFT_18", "GOLD_GIFT_30", "GOLD_GIFT_88" ->
                    "购买立得金币";
            default -> "";
        };
    }

    private static String[] hotRewardLabels(ShopProduct product) {
        return switch (product.productCode()) {
            case "HOT_FIRST_RECHARGE" -> new String[] {"x100", "x20000", "1天"};
            case "HOT_DAILY_BENEFIT" -> new String[] {"x100", "x1000", "x500"};
            case "HOT_DAILY_GIFT" -> new String[] {"第1次", "第2次", "第3次"};
            case "GOLD_GIFT_6", "GOLD_GIFT_18", "GOLD_GIFT_30", "GOLD_GIFT_88" ->
                    new String[] {"金币", "x" + compact(product.rewardQuantity()), "礼包"};
            default -> new String[] {"x1000", "x" + compact(product.rewardQuantity()), "礼包"};
        };
    }

    private static String compact(long value) {
        if (value >= 10_000 && value % 10_000 == 0) {
            return (value / 10_000) + "万";
        }
        if (value >= 10_000) {
            return String.format(Locale.CHINA, "%.1f万", value / 10_000f);
        }
        return String.valueOf(value);
    }
}
