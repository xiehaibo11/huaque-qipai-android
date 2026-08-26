package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import java.util.List;

final class LobbyBackpackRenderer {
    private static final RectF GRID_CLIP = new RectF(370f, 114f, 1414f, 1064f);
    private final LobbyBackpackDrawables images;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    LobbyBackpackRenderer(LobbyBackpackDrawables images, Typeface typeface) {
        this.images = images;
        paint.setTypeface(typeface);
    }

    void draw(
            Canvas canvas,
            LobbyBackpackState state,
            ShopWalletState wallet,
            float scroll,
            boolean canUse) {
        drawNineSlice(canvas, images.leftStrip, -220f, 0f, 268f, 1080f, 6f, 315f);
        drawBitmap(canvas, images.subStrip, 268f, 0f, 388f, 1080f);
        drawHeader(canvas, wallet);
        drawCategories(canvas, state.category());
        drawNineSlice(canvas, images.detailPanel, 1414f, 100f, 1941f, 1088f, 49f, 57f);
        if (state.phase() == LobbyBackpackState.Phase.LOADING) {
            drawStatus(canvas, "背包加载中…");
            return;
        }
        if (state.phase() == LobbyBackpackState.Phase.ERROR) {
            drawStatus(canvas, state.error());
            return;
        }
        List<LobbyBackpackEntry> entries = state.visibleEntries();
        if (entries.isEmpty()) {
            drawStatus(canvas, "暂无该类型道具~");
            return;
        }
        drawGrid(canvas, entries, state.selectedIndex(), scroll);
        drawDetails(canvas, state.selectedEntry(), canUse);
    }

    private void drawHeader(Canvas canvas, ShopWalletState wallet) {
        drawBitmap(canvas, images.shop.backArrow, 4f, 18f, 95f, 115f);
        drawBitmap(canvas, images.title, 101f, 31f, 230f, 103f);
        drawWallet(canvas, 820f, images.shop.headerRoomCardIcon, walletText(wallet, 0));
        drawWallet(canvas, 1116f, images.shop.headerCoinIcon, walletText(wallet, 1));
        drawWallet(canvas, 1403f, images.shop.headerDiamondIcon, walletText(wallet, 2));
        drawBitmap(canvas, images.shopButton, 1661.5f, 28.7f, 1898.5f, 115.7f);
    }

    private void drawWallet(Canvas canvas, float left, Bitmap icon, String value) {
        drawBitmap(canvas, images.shop.headerPanelBackground, left, 35f, left + 242f, 93f);
        drawBitmap(canvas, icon, left - 55f, 26f, left + 37f, 97f);
        drawText(canvas, value, left + 142f, 65f, 39f, Color.WHITE, 160f);
    }

    private void drawCategories(Canvas canvas, LobbyBackpackCategory selected) {
        LobbyBackpackCategory[] categories = LobbyBackpackCategory.values();
        for (int index = 0; index < categories.length; index++) {
            float top = 137f + index * 147f;
            boolean active = categories[index] == selected;
            if (active) drawBitmap(canvas, images.tabSelected, 0f, top, 298f, top + 147f);
            drawText(
                    canvas,
                    categories[index].title(),
                    135f,
                    top + 73.5f,
                    52f,
                    active ? Color.rgb(132, 61, 31) : Color.WHITE,
                    230f);
        }
    }

    private void drawGrid(
            Canvas canvas, List<LobbyBackpackEntry> entries, int selected, float scroll) {
        int save = canvas.save();
        canvas.clipRect(GRID_CLIP);
        for (int index = 0; index < entries.size(); index++) {
            int column = index % LobbyBackpackLayout.COLUMNS;
            int row = index / LobbyBackpackLayout.COLUMNS;
            float left = LobbyBackpackLayout.GRID_LEFT
                    + column * LobbyBackpackLayout.CELL_WIDTH;
            float top = LobbyBackpackLayout.GRID_TOP
                    + row * LobbyBackpackLayout.CELL_HEIGHT - scroll;
            if (top > GRID_CLIP.bottom || top + 420f < GRID_CLIP.top) continue;
            drawItem(canvas, entries.get(index), left, top, index == selected);
        }
        canvas.restoreToCount(save);
    }

    private void drawItem(
            Canvas canvas, LobbyBackpackEntry entry, float left, float top, boolean selected) {
        drawNineSlice(canvas, images.itemBackground, left + 12.5f, top + 12.5f,
                left + 319.5f, top + 407.5f, 19f, 145f);
        drawBitmap(canvas, images.itemWatermark, left + 23f, top + 13f,
                left + 88f, top + 333f);
        Bitmap icon = images.shop.productIcon(entry.iconKey());
        drawBitmap(canvas, icon, left + 54f, top + 47f, left + 278f, top + 289f);
        drawNineSlice(canvas, images.itemNameBackground, left + 12.5f, top + 328f,
                left + 319.5f, top + 408f, 19f, 33f);
        drawText(canvas, entry.displayName(), left + 166f, top + 376f,
                42f, Color.rgb(53, 73, 116), 278f);
        drawRightText(canvas, "×" + entry.quantity(), left + 319f, top + 305f,
                36f, Color.rgb(96, 120, 135));
        if (selected) drawNineSlice(canvas, images.itemSelected, left - 8f, top - 8f,
                left + 340f, top + 428f, 55f, 55f);
    }

    private void drawDetails(Canvas canvas, LobbyBackpackEntry entry, boolean canUse) {
        drawNineSlice(canvas, images.remainingBackground, 1407f, 124f, 1781f, 200f, 49f, 25f);
        drawLeftText(canvas, entry.remainingText(), 1422f, 162f, 37f, Color.WHITE, 345f);
        drawText(canvas, entry.displayName(), 1667.5f, 280f, 58f,
                Color.rgb(23, 30, 47), 485f);
        Bitmap icon = images.shop.productIcon(entry.iconKey());
        drawBitmap(canvas, icon, 1504f, 316f, 1831f, 626f);
        String description = entry.description().isBlank() ? entry.itemCode() : entry.description();
        drawText(canvas, description, 1667.5f, 650f, 35f,
                Color.rgb(92, 111, 151), 470f);
        int alpha = canUse ? 255 : 115;
        paint.setAlpha(alpha);
        drawBitmap(canvas, images.useButton, 1500f, 728f, 1855f, 841f);
        paint.setAlpha(255);
        drawText(canvas, "使用", 1677.5f, 789f, 48f,
                canUse ? Color.rgb(152, 68, 7) : Color.rgb(105, 105, 105), 300f);
        if (!canUse) {
            drawText(canvas, "当前服务暂不支持使用", 1667.5f, 875f, 27f,
                    Color.rgb(92, 111, 151), 470f);
        }
        drawBitmap(canvas, images.pedestal, 1409f, 849f, 1943f, 1024f);
    }

    private void drawStatus(Canvas canvas, String value) {
        drawText(canvas, value, 890f, 565f, 48f, Color.rgb(89, 104, 137), 900f);
    }

    private static String walletText(ShopWalletState wallet, int field) {
        if (wallet == null) return "--";
        return Long.toString(switch (field) {
            case 0 -> wallet.roomCards();
            case 1 -> wallet.coins();
            default -> wallet.diamonds();
        });
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, float l, float t, float r, float b) {
        canvas.drawBitmap(bitmap, null, new RectF(l, t, r, b), paint);
    }

    private void drawNineSlice(
            Canvas canvas,
            Bitmap bitmap,
            float left,
            float top,
            float right,
            float bottom,
            float capX,
            float capY) {
        float[] sourceX = {0f, capX, bitmap.getWidth() - capX, bitmap.getWidth()};
        float[] sourceY = {0f, capY, bitmap.getHeight() - capY, bitmap.getHeight()};
        float[] targetX = {left, left + capX, right - capX, right};
        float[] targetY = {top, top + capY, bottom - capY, bottom};
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                android.graphics.Rect source = new android.graphics.Rect(
                        Math.round(sourceX[column]),
                        Math.round(sourceY[row]),
                        Math.round(sourceX[column + 1]),
                        Math.round(sourceY[row + 1]));
                RectF target = new RectF(
                        targetX[column], targetY[row], targetX[column + 1], targetY[row + 1]);
                canvas.drawBitmap(bitmap, source, target, paint);
            }
        }
    }

    private void drawText(
            Canvas canvas, String value, float x, float centerY, float size, int color, float width) {
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(color);
        fitText(value, size, width);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        canvas.drawText(value, x, centerY - (metrics.ascent + metrics.descent) / 2f, paint);
    }

    private void drawLeftText(
            Canvas canvas, String value, float x, float centerY, float size, int color, float width) {
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(color);
        fitText(value, size, width);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        canvas.drawText(value, x, centerY - (metrics.ascent + metrics.descent) / 2f, paint);
    }

    private void drawRightText(Canvas canvas, String value, float x, float centerY, float size, int color) {
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTextSize(size);
        paint.setColor(color);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        canvas.drawText(value, x, centerY - (metrics.ascent + metrics.descent) / 2f, paint);
    }

    private void fitText(String value, float preferred, float width) {
        float size = preferred;
        paint.setTextSize(size);
        while (size > 24f && paint.measureText(value) > width) {
            paint.setTextSize(--size);
        }
    }
}
