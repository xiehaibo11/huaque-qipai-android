package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Original GoldHall bag composition backed exclusively by authenticated inventory data. */
@SuppressLint("ViewConstructor")
final class ShopInventoryView extends AdaptiveCanvasView {
    interface Actions {
        void onClose();
        void onOpenShop();
        boolean canUse(LobbyBackpackEntry entry);
        void onUse(LobbyBackpackEntry entry);
    }

    private static final float WIDTH = LobbyBackpackLayout.DESIGN_WIDTH;
    private static final float HEIGHT = LobbyBackpackLayout.DESIGN_HEIGHT;
    private static final float GRID_BOTTOM = 1064f;
    private final Actions actions;
    private final LobbyBackpackDrawables drawables;
    private final LobbyBackpackRenderer renderer;
    private ShopCatalogState catalog = ShopOriginalCatalog.create();
    private List<ShopInventoryItem> inventory = Collections.emptyList();
    private LobbyBackpackState state = LobbyBackpackState.loading();
    private ShopWalletState wallet;
    private float scroll;
    private float downX;
    private float downY;
    private float lastY;
    private boolean dragging;

    ShopInventoryView(Context context, Actions actions) {
        super(context);
        this.actions = actions;
        drawables = new LobbyBackpackDrawables(getResources());
        renderer = new LobbyBackpackRenderer(drawables, loadTypeface(context));
        setClickable(true);
        setFocusable(true);
        updateContentDescription();
    }

    void setCatalog(ShopCatalogState catalog) {
        this.catalog = catalog == null ? ShopCatalogState.empty() : catalog;
        rebuildEntries();
    }

    void setInventory(List<ShopInventoryItem> inventory) {
        this.inventory = inventory == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(inventory));
        rebuildEntries();
    }

    void setWallet(ShopWalletState wallet) {
        this.wallet = wallet;
        invalidate();
    }

    void setLoading(boolean loading) {
        if (loading) {
            state = LobbyBackpackState.loading();
        } else if (state.phase() == LobbyBackpackState.Phase.LOADING) {
            rebuildEntries();
        }
        updateContentDescription();
        invalidate();
    }

    void setError(String message) {
        state = LobbyBackpackState.error(message);
        scroll = 0f;
        updateContentDescription();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) return;
        AdaptiveViewport viewport = adaptiveViewport(WIDTH, HEIGHT);
        AdaptiveCanvasDrawing.drawFullBleedBitmap(
                canvas, drawables.shop.sceneBackground, new android.graphics.Paint(),
                viewport, 2340f, 1080f);
        AdaptiveViewport.Transform transform = viewport.dialogTransform(WIDTH, HEIGHT, 1f, 1f);
        int save = AdaptiveCanvasDrawing.apply(canvas, transform);
        LobbyBackpackEntry selected = state.selectedEntry();
        renderer.draw(canvas, state, wallet, scroll, canUse(selected));
        canvas.restoreToCount(save);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0) return false;
        AdaptiveViewport.Transform transform =
                adaptiveViewport(WIDTH, HEIGHT).dialogTransform(WIDTH, HEIGHT, 1f, 1f);
        float x = transform.unmapX(event.getX());
        float y = transform.unmapY(event.getY());
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                downX = x;
                downY = y;
                lastY = y;
                dragging = inGrid(x, y);
                return true;
            }
            case MotionEvent.ACTION_MOVE -> {
                if (dragging) {
                    scroll = clamp(scroll + lastY - y, 0f, maxScroll());
                    lastY = y;
                    invalidate();
                }
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                boolean click = Math.abs(x - downX) < 18f && Math.abs(y - downY) < 18f;
                dragging = false;
                if (click) {
                    performClick();
                    activate(targetAt(x, y));
                }
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                dragging = false;
                return true;
            }
            default -> { return true; }
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private LobbyBackpackLayout.Target targetAt(float x, float y) {
        LobbyBackpackEntry selected = state.selectedEntry();
        LobbyBackpackLayout.Target fixed = LobbyBackpackLayout.targetAt(x, y, 0, canUse(selected));
        if (fixed.kind() != LobbyBackpackLayout.Kind.NONE) return fixed;
        if (!inGrid(x, y)) return LobbyBackpackLayout.Target.none();
        int index = LobbyBackpackLayout.itemIndexAt(
                x, y + scroll, state.visibleEntries().size());
        return index < 0
                ? LobbyBackpackLayout.Target.none()
                : LobbyBackpackLayout.Target.item(index);
    }

    private void activate(LobbyBackpackLayout.Target target) {
        switch (target.kind()) {
            case CLOSE -> actions.onClose();
            case SHOP -> actions.onOpenShop();
            case CATEGORY -> {
                LobbyBackpackCategory[] categories = LobbyBackpackCategory.values();
                if (target.index() >= 0 && target.index() < categories.length) {
                    state = state.selectCategory(categories[target.index()]);
                    scroll = 0f;
                    updateContentDescription();
                    invalidate();
                }
            }
            case ITEM -> {
                state = state.selectEntry(target.index());
                updateContentDescription();
                invalidate();
            }
            case USE -> {
                LobbyBackpackEntry selected = state.selectedEntry();
                if (canUse(selected)) actions.onUse(selected);
            }
            case NONE -> { }
        }
    }

    private void rebuildEntries() {
        state = LobbyBackpackState.ready(LobbyBackpackInventoryMapper.map(inventory, catalog));
        scroll = 0f;
        updateContentDescription();
        invalidate();
    }

    private boolean canUse(LobbyBackpackEntry entry) {
        return entry != null && actions.canUse(entry);
    }

    private float maxScroll() {
        int rows = (state.visibleEntries().size() + LobbyBackpackLayout.COLUMNS - 1)
                / LobbyBackpackLayout.COLUMNS;
        return Math.max(0f, rows * LobbyBackpackLayout.CELL_HEIGHT - (GRID_BOTTOM - LobbyBackpackLayout.GRID_TOP));
    }

    private static boolean inGrid(float x, float y) {
        return x >= LobbyBackpackLayout.GRID_LEFT
                && x < LobbyBackpackLayout.DETAIL_LEFT
                && y >= LobbyBackpackLayout.GRID_TOP
                && y <= GRID_BOTTOM;
    }

    private void updateContentDescription() {
        String status = switch (state.phase()) {
            case LOADING -> "背包，加载中";
            case ERROR -> "背包，" + state.error();
            case READY -> {
                LobbyBackpackEntry selected = state.selectedEntry();
                yield "背包，" + state.category().title()
                        + (selected == null ? "，暂无道具" : "，已选择" + selected.displayName());
            }
        };
        setContentDescription(status);
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static Typeface loadTypeface(Context context) {
        try {
            return Typeface.createFromAsset(context.getAssets(), "fonts/zihun_jingdian_lihei.ttf");
        } catch (RuntimeException ignored) {
            return Typeface.DEFAULT_BOLD;
        }
    }
}
