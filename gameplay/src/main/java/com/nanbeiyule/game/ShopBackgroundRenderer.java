package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import java.util.List;

/** Draws the blue runtime skin, fixed header, primary menu and observed secondary strips. */
final class ShopBackgroundRenderer {
    private static final int NAVY = Color.rgb(39, 74, 132);
    private static final int DARK_BLUE = Color.rgb(38, 97, 151);
    private static final int SELECTED_BROWN = Color.rgb(148, 74, 25);
    private static final RectF HEADER_BACK =
            new RectF(4.5f, 18.5f, 95.5f, 115.5f);
    private static final RectF HEADER_TITLE =
            new RectF(100.49f, 31f, 231.49f, 103f);
    private final ShopDrawableSet drawables;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();
    private final Typeface typeface;

    ShopBackgroundRenderer(ShopDrawableSet drawables) {
        this.drawables = drawables;
        typeface =
                Typeface.createFromAsset(
                        drawables.resources().getAssets(), "fonts/fangzhengcuyuan.ttf");
    }

    void draw(
            Canvas canvas,
            ShopCatalogState state,
            ShopWalletState wallet,
            float categoryScroll) {
        drawBlueScene(canvas);
        drawHeader(canvas, wallet);
        drawPrimaryCategories(canvas, state, categoryScroll);
        drawSecondaryStrip(canvas, state);
    }

    private void drawBlueScene(Canvas canvas) {
        drawBitmap(canvas, drawables.sceneBackground, -210f, 0f, 2340f, 1080f);
    }

    private void drawHeader(Canvas canvas, ShopWalletState wallet) {
        drawBitmap(canvas, drawables.backArrow, new RectF(4.5f, 18.5f, 95.5f, 115.5f));
        drawBitmap(canvas, drawables.headerTitle, new RectF(100.49f, 31f, 231.49f, 103f));
        drawCouponValue(canvas, wallet.coupons());
        drawCurrencyValue(canvas, drawables.headerRoomCardIcon, wallet.roomCards(),
                ShopHeaderLayout.ROOM_CARD_PANEL,
                ShopHeaderLayout.ROOM_CARD_ICON,
                ShopHeaderLayout.ROOM_CARD_ADD,
                ShopHeaderLayout.ROOM_CARD_VALUE_CENTER_X);
        drawCurrencyValue(canvas, drawables.headerCoinIcon, wallet.coins(),
                ShopHeaderLayout.GOLD_PANEL,
                ShopHeaderLayout.GOLD_ICON,
                ShopHeaderLayout.GOLD_ADD,
                ShopHeaderLayout.GOLD_VALUE_CENTER_X);
        drawCurrencyValue(canvas, drawables.headerDiamondIcon, wallet.diamonds(),
                ShopHeaderLayout.DIAMOND_PANEL,
                ShopHeaderLayout.DIAMOND_ICON,
                ShopHeaderLayout.DIAMOND_ADD,
                ShopHeaderLayout.DIAMOND_VALUE_CENTER_X);
        drawBitmap(canvas, drawables.bag, toRectF(ShopHeaderLayout.BAG));
        drawBitmap(
                canvas,
                drawables.customerService,
                toRectF(ShopHeaderLayout.CUSTOMER_SERVICE));
    }

    private void drawCouponValue(Canvas canvas, long value) {
        drawBitmap(
                canvas,
                drawables.headerPanelBackground,
                toRectF(ShopHeaderLayout.COUPON_PANEL));
        drawBitmap(canvas, drawables.headerCouponIcon, toRectF(ShopHeaderLayout.COUPON_ICON));
        drawCenteredText(
                canvas,
                compact(value),
                ShopHeaderLayout.COUPON_VALUE_CENTER_X,
                ShopHeaderLayout.VALUE_CENTER_Y,
                ShopHeaderLayout.VALUE_TEXT_SIZE,
                Color.WHITE,
                true);
    }

    private void drawCurrencyValue(
            Canvas canvas,
            Bitmap icon,
            long value,
            ShopLayout.Rect panel,
            ShopLayout.Rect iconBounds,
            ShopLayout.Rect addBounds,
            float valueCenterX) {
        drawBitmap(canvas, drawables.headerPanelBackground, toRectF(panel));
        drawBitmap(canvas, icon, toRectF(iconBounds));
        drawBitmap(canvas, drawables.headerAddIcon, toRectF(addBounds));
        drawCenteredText(
                canvas,
                compact(value),
                valueCenterX,
                ShopHeaderLayout.VALUE_CENTER_Y,
                ShopHeaderLayout.VALUE_TEXT_SIZE,
                Color.WHITE,
                true);
    }

    private static RectF toRectF(ShopLayout.Rect rect) {
        return new RectF(rect.left(), rect.top(), rect.right(), rect.bottom());
    }

    private void drawPrimaryCategories(
            Canvas canvas, ShopCatalogState state, float categoryScroll) {
        paint.setColor(Color.rgb(42, 101, 155));
        canvas.drawRect(
                ShopRuntimeLayout.PRIMARY_CATEGORY_LIST.left(),
                ShopRuntimeLayout.PRIMARY_CATEGORY_LIST.top(),
                ShopRuntimeLayout.PRIMARY_CATEGORY_LIST.right(),
                ShopRuntimeLayout.PRIMARY_CATEGORY_LIST.bottom(),
                paint);
        canvas.save();
        canvas.clipRect(
                ShopRuntimeLayout.PRIMARY_CATEGORY_LIST.left(),
                ShopRuntimeLayout.PRIMARY_CATEGORY_LIST.top(),
                ShopRuntimeLayout.PRIMARY_CATEGORY_LIST.right(),
                ShopRuntimeLayout.PRIMARY_CATEGORY_LIST.bottom());
        List<ShopCategory> categories = ShopCategory.ordered();
        for (int index = 0; index < categories.size(); index++) {
            ShopCategory category = categories.get(index);
            ShopLayout.Rect row = ShopRuntimeLayout.categoryRow(index, categoryScroll);
            boolean selected = category == state.selectedCategory();
            if (selected) {
                drawSelectedCategory(canvas, row);
            }
            drawText(
                    canvas,
                    category.title(),
                    116f,
                    row.top() + 91f,
                    selected ? 48f : 45f,
                    selected ? SELECTED_BROWN : Color.WHITE,
                    Paint.Align.CENTER,
                    true);
            if (category == ShopCategory.HOT_RECOMMENDATION) {
                paint.setColor(Color.rgb(238, 54, 29));
                canvas.drawCircle(244f, row.top() + 20f, 10f, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3f);
                paint.setColor(Color.WHITE);
                canvas.drawCircle(244f, row.top() + 20f, 10f, paint);
                paint.setStyle(Paint.Style.FILL);
            }
        }
        canvas.restore();
    }

    private void drawSelectedCategory(Canvas canvas, ShopLayout.Rect row) {
        paint.setShader(
                new LinearGradient(
                        0f,
                        row.top(),
                        0f,
                        row.bottom(),
                        Color.rgb(255, 210, 70),
                        Color.rgb(255, 255, 151),
                        Shader.TileMode.CLAMP));
        Path selected = new Path();
        selected.moveTo(8f, row.top() + 8f);
        selected.lineTo(258f, row.top() + 8f);
        selected.lineTo(276f, row.centerY());
        selected.lineTo(258f, row.bottom() - 8f);
        selected.lineTo(8f, row.bottom() - 8f);
        selected.close();
        canvas.drawPath(selected, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(Color.rgb(255, 235, 151));
        canvas.drawPath(selected, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawSecondaryStrip(Canvas canvas, ShopCatalogState state) {
        ShopCategory category = state.selectedCategory();
        if (category == ShopCategory.HOT_RECOMMENDATION) {
            drawHotSecondaryStrip(canvas, state.selectedHotSection());
            return;
        }
        if (category == ShopCategory.PROP) {
            drawPropSecondaryStrip(canvas, state.selectedPropSection());
            return;
        }
        if (category == ShopCategory.DECORATION) {
            drawDecorationSecondaryStrip(canvas, state.selectedDecorationSection());
            return;
        }
        if (category == ShopCategory.INTERACTION) {
            drawInteractionSecondaryStrip(canvas, state.selectedInteractionSection());
            return;
        }
        String[] labels = secondaryLabels(category);
        if (labels.length == 0) {
            return;
        }
        paint.setColor(Color.argb(155, 35, 82, 128));
        canvas.drawRect(
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.left(),
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.top(),
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.right(),
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.bottom(),
                paint);
        float top = 122f;
        for (int index = 0; index < labels.length; index++) {
            float height = Math.min(220f, 150f + labels[index].length() * 22f);
            if (index == 0) {
                paint.setShader(
                        new LinearGradient(
                                268f,
                                top,
                                370f,
                                top,
                                Color.rgb(232, 110, 39),
                                Color.rgb(255, 184, 83),
                                Shader.TileMode.CLAMP));
                canvas.drawRect(268f, top, 370f, top + height, paint);
                paint.setShader(null);
            }
            drawVerticalText(
                    canvas,
                    labels[index],
                    319f,
                    top + 26f,
                    34f,
                    index == 0 ? Color.WHITE : Color.rgb(244, 250, 255));
            top += height;
        }
    }

    private void drawHotSecondaryStrip(
            Canvas canvas, ShopHotSection selectedSection) {
        paint.setColor(Color.argb(155, 35, 82, 128));
        canvas.drawRect(
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.left(),
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.top(),
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.right(),
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.bottom(),
                paint);
        List<ShopHotSection> sections = ShopHotSection.ordered();
        for (int index = 0; index < sections.size(); index++) {
            ShopHotSection section = sections.get(index);
            ShopLayout.Rect row = ShopRuntimeLayout.hotSectionRow(index);
            boolean selected = section == selectedSection;
            if (selected) {
                paint.setShader(
                        new LinearGradient(
                                row.left(),
                                row.top(),
                                row.right(),
                                row.top(),
                                Color.rgb(232, 110, 39),
                                Color.rgb(255, 184, 83),
                                Shader.TileMode.CLAMP));
                canvas.drawRect(row.left(), row.top(), row.right(), row.bottom(), paint);
                paint.setShader(null);
            }
            drawVerticalText(
                    canvas,
                    section.title(),
                    row.centerX(),
                    row.top() + 26f,
                    34f,
                    selected ? Color.WHITE : Color.rgb(244, 250, 255));
        }
    }

    private void drawPropSecondaryStrip(
            Canvas canvas, ShopPropSection selectedSection) {
        paint.setColor(Color.argb(155, 35, 82, 128));
        canvas.drawRect(
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.left(),
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.top(),
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.right(),
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.bottom(),
                paint);
        List<ShopPropSection> sections = ShopPropSection.ordered();
        for (int index = 0; index < sections.size(); index++) {
            ShopPropSection section = sections.get(index);
            ShopLayout.Rect row = ShopRuntimeLayout.propSectionRow(index);
            boolean selected = section == selectedSection;
            if (selected) {
                paint.setShader(
                        new LinearGradient(
                                row.left(),
                                row.top(),
                                row.right(),
                                row.top(),
                                Color.rgb(232, 110, 39),
                                Color.rgb(255, 184, 83),
                                Shader.TileMode.CLAMP));
                canvas.drawRect(row.left(), row.top(), row.right(), row.bottom(), paint);
                paint.setShader(null);
            }
            drawVerticalText(
                    canvas,
                    section.title(),
                    row.centerX(),
                    row.top() + 26f,
                    34f,
                    selected ? Color.WHITE : Color.rgb(244, 250, 255));
        }
    }

    private void drawDecorationSecondaryStrip(
            Canvas canvas, ShopDecorationSection selectedSection) {
        paint.setColor(Color.argb(155, 35, 82, 128));
        canvas.drawRect(
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.left(),
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.top(),
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.right(),
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.bottom(),
                paint);
        List<ShopDecorationSection> sections = ShopDecorationSection.ordered();
        for (int index = 0; index < sections.size(); index++) {
            ShopDecorationSection section = sections.get(index);
            ShopLayout.Rect row = ShopRuntimeLayout.decorationSectionRow(index);
            boolean selected = section == selectedSection;
            if (selected) {
                paint.setShader(
                        new LinearGradient(
                                row.left(),
                                row.top(),
                                row.right(),
                                row.top(),
                                Color.rgb(232, 110, 39),
                                Color.rgb(255, 184, 83),
                                Shader.TileMode.CLAMP));
                canvas.drawRect(row.left(), row.top(), row.right(), row.bottom(), paint);
                paint.setShader(null);
            }
            drawVerticalText(
                    canvas,
                    section.title(),
                    row.centerX(),
                    row.top() + 26f,
                    34f,
                    selected ? Color.WHITE : Color.rgb(244, 250, 255));
        }
    }

    private void drawInteractionSecondaryStrip(
            Canvas canvas, ShopInteractionSection selectedSection) {
        paint.setColor(Color.argb(155, 35, 82, 128));
        canvas.drawRect(
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.left(),
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.top(),
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.right(),
                ShopRuntimeLayout.SECONDARY_CATEGORY_STRIP.bottom(),
                paint);
        List<ShopInteractionSection> sections = ShopInteractionSection.ordered();
        for (int index = 0; index < sections.size(); index++) {
            ShopInteractionSection section = sections.get(index);
            ShopLayout.Rect row = ShopRuntimeLayout.interactionSectionRow(index);
            boolean selected = section == selectedSection;
            if (selected) {
                paint.setShader(
                        new LinearGradient(
                                row.left(),
                                row.top(),
                                row.right(),
                                row.top(),
                                Color.rgb(232, 110, 39),
                                Color.rgb(255, 184, 83),
                                Shader.TileMode.CLAMP));
                canvas.drawRect(row.left(), row.top(), row.right(), row.bottom(), paint);
                paint.setShader(null);
            }
            drawVerticalText(
                    canvas,
                    section.title(),
                    row.centerX(),
                    row.top() + 26f,
                    34f,
                    selected ? Color.WHITE : Color.rgb(244, 250, 255));
        }
    }

    private static String[] secondaryLabels(ShopCategory category) {
        return switch (category) {
            default -> new String[0];
        };
    }

    private void drawVerticalText(
            Canvas canvas, String text, float centerX, float top, float size, int color) {
        float baseline = top + size;
        for (int index = 0; index < text.length(); index++) {
            drawText(
                    canvas,
                    String.valueOf(text.charAt(index)),
                    centerX,
                    baseline,
                    size,
                    color,
                    Paint.Align.CENTER,
                    true);
            baseline += size * 1.02f;
        }
    }

    void drawCenteredText(
            Canvas canvas,
            String text,
            float centerX,
            float centerY,
            float size,
            int color,
            boolean bold) {
        textPaint.setTextSize(size);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        drawText(
                canvas,
                text,
                centerX,
                centerY - (metrics.ascent + metrics.descent) * 0.5f,
                size,
                color,
                Paint.Align.CENTER,
                bold);
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

    private void drawBitmap(
            Canvas canvas, Bitmap bitmap, float left, float top, float width, float height) {
        paint.setAlpha(255);
        paint.setShader(null);
        source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        destination.set(left, top, left + width, top + height);
        canvas.drawBitmap(bitmap, source, destination, paint);
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, RectF bounds) {
        drawBitmap(canvas, bitmap, bounds.left, bounds.top, bounds.width(), bounds.height());
    }

    private void drawBitmapCentered(
            Canvas canvas, Bitmap bitmap, float centerX, float centerY, float width, float height) {
        drawBitmap(canvas, bitmap, centerX - width * 0.5f, centerY - height * 0.5f, width, height);
    }

    private static String compact(long value) {
        return ZhejiangLobbyAmountFormatter.format(value);
    }
}
