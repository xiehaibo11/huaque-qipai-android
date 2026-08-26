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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Four-column native reconstruction of the original JuBaoPenMyView.csb. */
@SuppressLint("ViewConstructor")
final class TaizhouTreasureInventoryView extends TaizhouToolView {
    static final String EVIDENCE_CSB = "JuBaoPenMyView.csb";
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
    private static final int[] QUALITY_RESOURCES = {
        R.drawable.taizhou_treasure_inventory_quality_0,
        R.drawable.taizhou_treasure_inventory_quality_1,
        R.drawable.taizhou_treasure_inventory_quality_2,
        R.drawable.taizhou_treasure_inventory_quality_3,
        R.drawable.taizhou_treasure_inventory_quality_4
    };
    private static final int[] LEVEL_RESOURCES = {
        R.drawable.taizhou_treasure_level_bg_1,
        R.drawable.taizhou_treasure_level_bg_2,
        R.drawable.taizhou_treasure_level_bg_3,
        R.drawable.taizhou_treasure_level_bg_4
    };
    private static final int[] LABEL_RESOURCES = {
        R.drawable.taizhou_treasure_quality_label_1,
        R.drawable.taizhou_treasure_quality_label_2,
        R.drawable.taizhou_treasure_quality_label_3,
        R.drawable.taizhou_treasure_quality_label_4
    };

    private final String userId;
    private final FortuneState state;
    private final TaizhouTreasurePlacementStore placementStore;
    private final long openedNanos = System.nanoTime();
    private final Bitmap panel, header, title, close, rightBackground;
    private final Bitmap emptyDecoration, place, cancel, selection;
    private final Bitmap[] icons = new Bitmap[16];
    private final Bitmap[] qualities = new Bitmap[5];
    private final Bitmap[] levelBackgrounds = new Bitmap[4];
    private final Bitmap[] qualityLabels = new Bitmap[4];
    private final Typeface typeface;
    private Runnable closeAction = () -> {};
    private String selectedCode = "";
    private float scroll;
    private float downY;
    private float lastY;
    private boolean listTouch;
    private boolean dragging;
    private boolean released;

    TaizhouTreasureInventoryView(
            Context context,
            String userId,
            FortuneState state,
            TaizhouTreasurePlacementStore placementStore) {
        super(context);
        this.userId = userId == null ? "" : userId;
        this.state = state;
        this.placementStore = placementStore;
        panel = bitmap(R.drawable.taizhou_treasure_inventory_detail_bg);
        header = bitmap(R.drawable.taizhou_treasure_inventory_panel);
        title = bitmap(R.drawable.taizhou_treasure_inventory_title);
        close = bitmap(R.drawable.taizhou_treasure_close);
        rightBackground = bitmap(R.drawable.taizhou_treasure_inventory_divider);
        emptyDecoration = bitmap(R.drawable.taizhou_treasure_inventory_cell_overlay);
        place = bitmap(R.drawable.taizhou_treasure_inventory_place);
        cancel = bitmap(R.drawable.taizhou_treasure_inventory_cancel);
        selection = bitmap(R.drawable.taizhou_treasure_inventory_selection);
        loadAll(icons, ICON_RESOURCES);
        loadAll(qualities, QUALITY_RESOURCES);
        loadAll(levelBackgrounds, LEVEL_RESOURCES);
        loadAll(qualityLabels, LABEL_RESOURCES);
        typeface = Typeface.createFromAsset(
                getResources().getAssets(), TaizhouTreasureFonts.TEXT_ASSET);
        List<FortuneState.Treasure> active = activeTreasures();
        if (!active.isEmpty()) selectedCode = active.get(0).treasureCode();
    }

    void setCloseAction(Runnable closeAction) {
        this.closeAction = closeAction == null ? () -> {} : closeAction;
    }

    @Override
    protected void drawDesign(Canvas canvas) {
        List<FortuneState.Treasure> active = activeTreasures();
        keepValidSelection(active);
        TaizhouTreasureCanvas.drawNineSlice(
                canvas, panel, rect(TaizhouTreasureInventoryLayout.PANEL),
                159, 74, 164, 77, bitmapPaint);
        TaizhouTreasureCanvas.drawNineSlice(
                canvas, rightBackground,
                rect(TaizhouTreasureInventoryLayout.RIGHT_BACKGROUND),
                6, 271, 8, 280, bitmapPaint);
        drawBitmap(canvas, header, rect(TaizhouTreasureInventoryLayout.HEADER));
        drawBitmap(canvas, title, rect(TaizhouTreasureInventoryLayout.TITLE));
        drawBitmap(canvas, close, rect(TaizhouTreasureInventoryLayout.CLOSE_ART));
        drawBitmap(canvas, emptyDecoration,
                rect(TaizhouTreasureInventoryLayout.EMPTY_DECORATION));
        drawGrid(canvas, active);
        drawFooter(canvas);
        if (active.isEmpty()) drawEmpty(canvas);
        else drawDetails(canvas, selected(active));
        if (isAttachedToWindow() && !released) postInvalidateDelayed(1_000L);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = designX(event);
        float y = designY(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                listTouch = TaizhouTreasureInventoryLayout.LIST.contains(x, y);
                dragging = false;
                downY = y;
                lastY = y;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (listTouch) {
                    scroll = clampScroll(scroll + lastY - y);
                    lastY = y;
                    dragging = dragging || Math.abs(y - downY) > 12.0f;
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                listTouch = false;
                dragging = false;
                return true;
            case MotionEvent.ACTION_UP:
                if (TaizhouTreasureInventoryLayout.CLOSE.contains(x, y)) {
                    closeAction.run();
                } else if (listTouch && !dragging) {
                    chooseAt(x, y);
                } else if (TaizhouTreasureInventoryLayout.PLACE.contains(x, y)) {
                    togglePlacement();
                }
                listTouch = false;
                dragging = false;
                performClick();
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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (released) return;
        released = true;
        TaizhouTreasureCanvas.recycle(panel, header, title, close, rightBackground,
                emptyDecoration, place, cancel, selection);
        TaizhouTreasureCanvas.recycle(icons);
        TaizhouTreasureCanvas.recycle(qualities);
        TaizhouTreasureCanvas.recycle(levelBackgrounds);
        TaizhouTreasureCanvas.recycle(qualityLabels);
    }

    private void drawGrid(Canvas canvas, List<FortuneState.Treasure> active) {
        int save = canvas.save();
        RectF list = rect(TaizhouTreasureInventoryLayout.LIST);
        canvas.clipRect(list);
        for (int index = 0; index < 16; index++) {
            TaizhouTreasurePotLayout.Node cell =
                    TaizhouTreasureInventoryLayout.cell(index, scroll);
            float x = cell.centerX();
            float y = cell.centerY();
            if (index >= active.size()) {
                drawCentered(canvas, qualities[0], x, y, 192.0f, 192.0f);
                continue;
            }
            FortuneState.Treasure treasure = active.get(index);
            TaizhouTreasureCatalog.Item item =
                    TaizhouTreasureCatalog.itemForCode(treasure.treasureCode());
            if (item == null) continue;
            drawCentered(canvas, qualities[item.quality()], x, y, 192.0f, 192.0f);
            drawCentered(canvas, icons[item.index() - 1], x, y, 180.0f, 180.0f);
            drawCentered(canvas, levelBackgrounds[item.quality() - 1],
                    x + 60.355f, y - 77.946f, 70.0f, 34.0f);
            text(canvas, levelText(treasure.level()), x + 60.595f, y - 80.086f,
                    26.0f, Color.WHITE, Paint.Align.CENTER);
            text(canvas, "+" + item.fortunePerLevel() * treasure.level(), x, y + 66.0f,
                    32.0f, item.titleColor(), Paint.Align.CENTER);
            if (treasure.treasureCode().equals(selectedCode)) {
                drawCentered(canvas, selection, x, y, 240.0f, 240.0f);
            }
        }
        canvas.restoreToCount(save);
    }

    private void drawDetails(Canvas canvas, FortuneState.Treasure treasure) {
        if (treasure == null) return;
        TaizhouTreasureCatalog.Item item =
                TaizhouTreasureCatalog.itemForCode(treasure.treasureCode());
        if (item == null) return;
        drawBitmap(canvas, icons[item.index() - 1],
                rect(TaizhouTreasureInventoryLayout.DETAIL_ICON));
        drawBitmap(canvas, qualityLabels[item.quality() - 1],
                rect(TaizhouTreasureInventoryLayout.DETAIL_QUALITY));
        drawBitmap(canvas, qualityLabels[item.quality() - 1],
                rect(TaizhouTreasureInventoryLayout.DETAIL_LEVEL));
        text(canvas, item.name(), TaizhouTreasureInventoryLayout.DETAIL_NAME.centerX(),
                TaizhouTreasureInventoryLayout.DETAIL_NAME.centerY(), 48.0f,
                Color.rgb(124, 116, 70), Paint.Align.CENTER);
        text(canvas, TaizhouTreasureCatalog.qualityName(item.quality()),
                TaizhouTreasureInventoryLayout.DETAIL_QUALITY.centerX(),
                TaizhouTreasureInventoryLayout.DETAIL_QUALITY.centerY(), 32.0f,
                Color.WHITE, Paint.Align.CENTER);
        text(canvas, levelText(treasure.level()),
                TaizhouTreasureInventoryLayout.DETAIL_LEVEL.centerX(),
                TaizhouTreasureInventoryLayout.DETAIL_LEVEL.centerY(), 32.0f,
                Color.WHITE, Paint.Align.CENTER);
        text(canvas, "运势 +" + item.fortunePerLevel() * treasure.level(),
                TaizhouTreasureInventoryLayout.DETAIL_FORTUNE.left(),
                TaizhouTreasureInventoryLayout.DETAIL_FORTUNE.centerY(), 40.0f,
                Color.rgb(50, 137, 255), Paint.Align.LEFT);
        TaizhouTreasureCanvas.wrappedText(canvas, textPaint, typeface,
                item.description(), rect(TaizhouTreasureInventoryLayout.DETAIL_DESCRIPTION),
                36.0f, 44.0f, Color.rgb(160, 154, 119));
        text(canvas, remainingText(treasure.remainingSeconds()),
                TaizhouTreasureInventoryLayout.DETAIL_REMAINING.left(),
                TaizhouTreasureInventoryLayout.DETAIL_REMAINING.centerY(), 36.0f,
                Color.rgb(124, 116, 70), Paint.Align.LEFT);
        boolean placed = treasure.treasureCode().equals(
                placementStore.selectedFor(userId, activeTreasures()));
        drawBitmap(canvas, placed ? cancel : place,
                rect(TaizhouTreasureInventoryLayout.PLACE));
    }

    private void drawFooter(Canvas canvas) {
        text(canvas, "可拖拽移动桌面位置，摆放效果仅自己可见",
                TaizhouTreasureInventoryLayout.FOOTER_TEXT.centerX(),
                TaizhouTreasureInventoryLayout.FOOTER_TEXT.centerY(),
                32.0f, Color.WHITE, Paint.Align.CENTER);
    }

    private void drawEmpty(Canvas canvas) {
        text(canvas, "暂无宝物", 1447.41f, 580.58f, 68.0f,
                Color.rgb(175, 187, 175), Paint.Align.CENTER);
    }

    private void chooseAt(float x, float y) {
        int index = TaizhouTreasureInventoryLayout.cellAt(x, y, scroll);
        List<FortuneState.Treasure> active = activeTreasures();
        if (index >= 0 && index < active.size()) {
            selectedCode = active.get(index).treasureCode();
            invalidate();
        }
    }

    private void togglePlacement() {
        List<FortuneState.Treasure> active = activeTreasures();
        if (selectedCode.isEmpty()) return;
        if (selectedCode.equals(placementStore.selectedFor(userId, active))) {
            placementStore.cancel(userId);
        } else {
            placementStore.place(userId, selectedCode, active);
        }
        invalidate();
    }

    private List<FortuneState.Treasure> activeTreasures() {
        long elapsed = Math.max(0L, (System.nanoTime() - openedNanos) / 1_000_000_000L);
        List<FortuneState.Treasure> active = new ArrayList<>();
        for (FortuneState.Treasure treasure : state.treasures()) {
            long remaining = Math.max(0L, treasure.remainingSeconds() - elapsed);
            if (remaining <= 0) continue;
            active.add(new FortuneState.Treasure(
                    treasure.treasureCode(), treasure.name(), treasure.quality(),
                    treasure.fortuneScore(), treasure.level(), treasure.expiresAt(), remaining));
        }
        active.sort(Comparator.comparing(FortuneState.Treasure::treasureCode).reversed());
        return active;
    }

    private FortuneState.Treasure selected(List<FortuneState.Treasure> active) {
        for (FortuneState.Treasure treasure : active) {
            if (treasure.treasureCode().equals(selectedCode)) return treasure;
        }
        return active.isEmpty() ? null : active.get(0);
    }

    private void keepValidSelection(List<FortuneState.Treasure> active) {
        FortuneState.Treasure selected = selected(active);
        selectedCode = selected == null ? "" : selected.treasureCode();
        placementStore.selectedFor(userId, active);
    }

    private String remainingText(long remainingSeconds) {
        long hours = remainingSeconds / 3_600;
        long minutes = remainingSeconds % 3_600 / 60;
        long seconds = remainingSeconds % 60;
        return String.format(Locale.ROOT,
                "剩余时间 %02d时%02d分%02d秒", hours, minutes, seconds);
    }

    private static String levelText(int level) {
        return level < 10 ? level + "级" : "满级";
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

    private void loadAll(Bitmap[] target, int[] resources) {
        for (int index = 0; index < target.length; index++) {
            target[index] = bitmap(resources[index]);
        }
    }

    private static float clampScroll(float value) {
        return Math.max(0.0f,
                Math.min(TaizhouTreasureInventoryLayout.MAX_SCROLL, value));
    }

    private static RectF rect(TaizhouTreasurePotLayout.Node node) {
        return new RectF(node.left(), node.top(), node.right(), node.bottom());
    }
}
