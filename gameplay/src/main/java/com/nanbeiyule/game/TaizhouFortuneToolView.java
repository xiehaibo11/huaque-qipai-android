package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.view.MotionEvent;
import java.util.List;

/** Shared native renderer retained for the original fortune and Caishen surfaces. */
@SuppressLint("ViewConstructor")
final class TaizhouFortuneToolView extends TaizhouToolView {
    enum Mode { FORTUNE, CAISHEN }

    interface Actions {
        void onPray(String productCode, int quantity);

        void onCaishenActivate(String productCode);
    }

    private final Mode mode;
    private final FortuneState state;
    private final Actions actions;
    private final Bitmap fortuneBackground;
    private final Bitmap fortuneLeft;
    private final Bitmap fortuneTitle;
    private final Bitmap fortuneClose;
    private final Bitmap fortuneItem;
    private final Bitmap fortuneDragonItem;
    private final Bitmap fortuneButton;
    private final Bitmap fortuneDiamond;
    private final Bitmap caishenBackground;
    private final Bitmap caishenFigure;
    private final Bitmap caishenIce;
    private final Bitmap caishenClose;
    private final Bitmap caishenItem;
    private final Bitmap caishenButton;
    private final Bitmap caishenDiamond;
    private final TaizhouFortuneProductScroll productScroll;
    private Runnable dismissAction = () -> {};
    private int selectedPrayer;
    private int quantity = 1;
    private float productDownY;
    private float productLastY;
    private boolean productTouch;
    private boolean productDragging;

    TaizhouFortuneToolView(Context context, Mode mode, FortuneState state, Actions actions) {
        super(context);
        this.mode = mode;
        this.state = state;
        this.actions = actions;
        fortuneBackground = bitmap(R.drawable.taizhou_tool_fortune_background);
        fortuneLeft = bitmap(R.drawable.taizhou_tool_fortune_left_bg);
        fortuneTitle = bitmap(R.drawable.taizhou_tool_fortune_title);
        fortuneClose = bitmap(R.drawable.taizhou_tool_fortune_close);
        fortuneItem = bitmap(R.drawable.taizhou_tool_fortune_item_bg);
        fortuneDragonItem = bitmap(R.drawable.taizhou_tool_fortune_dragon_bg);
        fortuneButton = bitmap(R.drawable.taizhou_tool_fortune_button_bg);
        fortuneDiamond = bitmap(R.drawable.taizhou_tool_fortune_diamond);
        caishenBackground = bitmap(R.drawable.taizhou_tool_caishen_bg);
        caishenFigure = bitmap(R.drawable.taizhou_tool_caishen_figure);
        caishenIce = bitmap(R.drawable.taizhou_tool_caishen_ice);
        caishenClose = bitmap(R.drawable.taizhou_tool_caishen_close);
        caishenItem = bitmap(R.drawable.taizhou_tool_caishen_item_bg);
        caishenButton = bitmap(R.drawable.taizhou_tool_caishen_item_button);
        caishenDiamond = bitmap(R.drawable.taizhou_tool_caishen_diamond);
        productScroll = new TaizhouFortuneProductScroll(this.state.prayerProducts().size());
        setContentDescription(mode == Mode.FORTUNE ? "求财运" : "请财神");
    }

    void setDismissAction(Runnable action) {
        dismissAction = action == null ? () -> {} : action;
    }

    @Override
    protected void drawDesign(Canvas canvas) {
        if (mode == Mode.FORTUNE) drawFortune(canvas);
        else drawCaishen(canvas);
    }

    private void drawFortune(Canvas canvas) {
        drawBitmap(canvas, fortuneBackground, new RectF(0, 0, 1920, 1080));
        drawBitmap(canvas, fortuneLeft, new RectF(0, 0, 837, 1077));
        drawCentered(canvas, fortuneTitle, 1002, 77, 920, 137);
        drawCentered(canvas, fortuneClose, 1849, 63, 63, 61);
        drawProgress(canvas, "财", state.wealthPoints(), 387, 230, Color.rgb(226, 76, 28));
        drawProgress(canvas, "运", state.luckPoints(), 387, 362, Color.rgb(60, 160, 80));
        drawText(canvas, "钻石 " + state.wallet().diamonds(), 1710, 125, 30,
                Color.rgb(255, 231, 129));
        List<FortuneState.PrayerProduct> products = state.prayerProducts();
        int count = products.size();
        int save = canvas.save();
        canvas.clipRect(
                TaizhouFortuneProductScroll.LEFT,
                TaizhouFortuneProductScroll.TOP,
                TaizhouFortuneProductScroll.RIGHT,
                TaizhouFortuneProductScroll.BOTTOM);
        for (int index = 0; index < count; index++) {
            int row = index / 3;
            int column = index % 3;
            float centerX = 1320 + column * 220;
            float centerY = 280 + row * 275 - productScroll.offset();
            if (centerY < 20.0f || centerY > 980.0f) continue;
            boolean selected = index == selectedPrayer;
            drawCentered(canvas, selected && index == 0 ? fortuneDragonItem : fortuneItem,
                    centerX, centerY, selected && index == 0 ? 225 : 204,
                    selected && index == 0 ? 275 : 257);
            FortuneState.PrayerProduct product = products.get(index);
            drawText(canvas, product.name(), centerX, centerY - 48, 28, Color.rgb(129, 65, 26));
            drawCentered(canvas, fortuneDiamond, centerX - 34, centerY + 76, 42, 44);
            drawText(canvas, Long.toString(product.priceDiamonds()), centerX + 28,
                    centerY + 86, 25, Color.rgb(127, 75, 33));
        }
        canvas.restoreToCount(save);
        drawCentered(canvas, fortuneButton, 1560, 993, 608, 153);
        drawText(canvas, "−", 1420, 1010, 58, Color.rgb(121, 69, 27));
        drawText(canvas, Integer.toString(quantity), 1560, 1010, 46, Color.WHITE);
        drawText(canvas, "+", 1700, 1010, 58, Color.rgb(121, 69, 27));
        drawText(canvas, "祈 福", 1820, 1010, 36, Color.rgb(255, 249, 209));
    }

    private void drawProgress(
            Canvas canvas, String label, int value, float centerX, float centerY, int color) {
        fillPaint.setColor(Color.argb(180, 75, 36, 20));
        canvas.drawRoundRect(new RectF(centerX - 175, centerY - 31, centerX + 175, centerY + 31),
                28, 28, fillPaint);
        fillPaint.setColor(color);
        float width = 300.0f * Math.min(1.0f, Math.max(0.0f, value / 100.0f));
        canvas.drawRoundRect(new RectF(centerX - 150, centerY - 20,
                centerX - 150 + width, centerY + 20), 18, 18, fillPaint);
        drawText(canvas, label + " " + value, centerX, centerY + 12, 28, Color.WHITE);
    }

    private void drawCaishen(Canvas canvas) {
        fillPaint.setColor(Color.argb(170, 0, 0, 0));
        canvas.drawRect(0, 0, 1920, 1080, fillPaint);
        drawBitmap(canvas, caishenBackground, new RectF(260, 125, 1660, 960));
        drawBitmap(canvas, caishenFigure, new RectF(280, 148, 861, 938));
        if (state.caishenRemainingSeconds() <= 0) {
            drawBitmap(canvas, caishenIce, new RectF(280, 148, 861, 938));
        }
        drawCentered(canvas, caishenClose, 1354, 289, 99, 102);
        drawText(canvas, "请财神", 1110, 270, 52, Color.rgb(143, 63, 23));
        drawText(canvas, state.caishenRemainingSeconds() > 0
                        ? "剩余 " + formatTime(state.caishenRemainingSeconds()) : "当前未请财神",
                570, 890, 30, Color.rgb(144, 79, 27));
        List<FortuneState.CaishenProduct> products = state.caishenProducts();
        for (int index = 0; index < Math.min(3, products.size()); index++) {
            float centerX = 800 + index * 230;
            float centerY = 550;
            FortuneState.CaishenProduct product = products.get(index);
            drawCentered(canvas, caishenItem, centerX, centerY, 223, 283);
            drawText(canvas, product.name(), centerX, centerY - 92, 29,
                    Color.rgb(133, 69, 25));
            drawText(canvas, duration(product.durationSeconds()), centerX, centerY + 22, 25,
                    Color.rgb(133, 79, 38));
            drawCentered(canvas, caishenButton, centerX, centerY + 105, 209, 65);
            drawCentered(canvas, caishenDiamond, centerX - 40, centerY + 105, 32, 33);
            drawText(canvas, Long.toString(product.priceDiamonds()), centerX + 28,
                    centerY + 116, 27, Color.WHITE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = designX(event);
        float y = designY(event);
        if (mode == Mode.FORTUNE) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (productScroll.contains(x, y)) {
                        productTouch = true;
                        productDragging = false;
                        productDownY = y;
                        productLastY = y;
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (productTouch) {
                        productScroll.dragBy(y - productLastY);
                        productLastY = y;
                        productDragging =
                                productDragging || Math.abs(y - productDownY) > 12.0f;
                        invalidate();
                    }
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    productTouch = false;
                    productDragging = false;
                    return true;
                case MotionEvent.ACTION_UP:
                    if (productTouch) {
                        boolean wasDragging = productDragging;
                        productTouch = false;
                        productDragging = false;
                        if (!wasDragging) touchFortune(x, y);
                        performClick();
                        return true;
                    }
                    break;
                default:
                    return true;
            }
        } else if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return true;
        }
        if (mode == Mode.FORTUNE) {
            touchFortune(x, y);
        } else {
            touchCaishen(x, y);
        }
        performClick();
        return true;
    }

    private void touchFortune(float x, float y) {
        if (TaizhouWaitingToolLayout.FORTUNE_CLOSE.contains(x, y)) {
            dismissAction.run();
            return;
        }
        if (TaizhouWaitingToolLayout.FORTUNE_MINUS.contains(x, y)) {
            quantity = Math.max(1, quantity - 1);
        } else if (TaizhouWaitingToolLayout.FORTUNE_ADD.contains(x, y)) {
            quantity = Math.min(10, quantity + 1);
        } else {
            int index = productScroll.productAt(x, y);
            if (index >= 0) {
                selectedPrayer = index;
            } else if (TaizhouWaitingToolLayout.FORTUNE_BUY.contains(x, y)
                    && !state.prayerProducts().isEmpty()) {
                actions.onPray(state.prayerProducts().get(selectedPrayer).productCode(), quantity);
            }
        }
        invalidate();
    }

    private void touchCaishen(float x, float y) {
        if (TaizhouWaitingToolLayout.CAISHEN_CLOSE.contains(x, y)) {
            dismissAction.run();
            return;
        }
        int index = TaizhouWaitingToolLayout.caishenProductAt(
                x, y, state.caishenProducts().size());
        if (index >= 0) {
            actions.onCaishenActivate(state.caishenProducts().get(index).productCode());
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private static String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = seconds % 3600 / 60;
        long rest = seconds % 60;
        return String.format(java.util.Locale.ROOT, "%02d:%02d:%02d", hours, minutes, rest);
    }

    private static String duration(long seconds) {
        long hours = seconds / 3600;
        return hours >= 24 && hours % 24 == 0 ? hours / 24 + "天" : hours + "小时";
    }
}
