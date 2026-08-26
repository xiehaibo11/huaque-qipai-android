package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.nanbeiyule.game.goldroom.GoldHallGameRuleDocument;
import com.nanbeiyule.game.goldroom.GoldHallGameRuleLayout;
import com.nanbeiyule.game.goldroom.GoldHallGameRuleTextLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * 原生规则弹层，对应原版 {@code GoldHallGameRuleView}
 * （{@code lobby/Modules/GoldNew/SubModules/Rule/RuleView.lua} + {@code GameRuleLayer.csb}）。
 *
 * <p>页签、外框与关闭键按原版 CSB 绘制；正文由南北娱乐后端下发，替代原版指向浙江服务器的
 * WebView。加载完成前显示原版 {@code _panelLoading} 的等待文案。
 */
final class GoldHallGameRuleView extends AdaptiveCanvasView {
    interface OnCloseRequestedListener {
        void onCloseRequested();
    }

    /** 页签切换：原版 {@code onTouchChangeGame} 按 tag 重新拉取该玩法的规则。 */
    interface OnGameSelectedListener {
        void onGameSelected(long gameId);
    }

    private final GoldChooseRoomBitmaps bitmaps;
    private final GoldHallGameRuleRenderer renderer;
    private final int touchSlop;

    private final List<GoldHallGameRuleDocument> documents = new ArrayList<>();
    private int selectedIndex = 0;
    private boolean loading = true;
    private GoldHallGameRuleTextLayout textLayout;
    private float scroll;
    private float lastTouchY;
    private float touchStartY;
    private boolean dragging;
    private float scale = 1.0f;
    private float offsetX;
    private float offsetY;
    private OnCloseRequestedListener closeRequestedListener;
    private OnGameSelectedListener gameSelectedListener;

    GoldHallGameRuleView(Context context) {
        super(context);
        bitmaps = new GoldChooseRoomBitmaps(context);
        renderer = new GoldHallGameRuleRenderer(context, bitmaps);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    void setOnCloseRequestedListener(OnCloseRequestedListener listener) {
        closeRequestedListener = listener;
    }

    void setOnGameSelectedListener(OnGameSelectedListener listener) {
        gameSelectedListener = listener;
    }

    /**
     * 设置页签。原版 {@code initGameBtnsList} 在带 leisureID 打开时只建当前玩法一个页签，
     * 不带时才铺开全部金币场玩法，这里保持同一语义，由调用方决定传几项。
     */
    void setDocuments(List<GoldHallGameRuleDocument> next, long selectedGameId) {
        documents.clear();
        documents.addAll(next);
        selectedIndex = 0;
        for (int index = 0; index < documents.size(); index++) {
            if (documents.get(index).gameId() == selectedGameId) {
                selectedIndex = index;
                break;
            }
        }
        loading = documents.isEmpty() || documents.get(selectedIndex).isEmpty();
        scroll = 0.0f;
        textLayout = null;
        invalidate();
    }

    /** 网络失败等待时保持原版等待态，不伪造正文。 */
    void setLoading(boolean loading) {
        this.loading = loading;
        invalidate();
    }

    void release() {
        bitmaps.recycle();
    }

    private GoldHallGameRuleDocument selectedDocument() {
        if (selectedIndex < 0 || selectedIndex >= documents.size()) {
            return null;
        }
        return documents.get(selectedIndex);
    }

    private GoldHallGameRuleTextLayout textLayout() {
        if (textLayout == null) {
            GoldHallGameRuleDocument document = selectedDocument();
            if (document == null) {
                return null;
            }
            float available =
                    GoldHallGameRuleLayout.CONTENT_WIDTH
                            - 2.0f * GoldHallGameRuleLayout.SCREENSHOT_CONTENT_PADDING_LEFT;
            textLayout =
                    GoldHallGameRuleTextLayout.wrap(document, available, renderer.measurer());
        }
        return textLayout;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        updateProjection();
        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.scale(scale, scale);
        renderer.drawChrome(canvas);
        List<String> titles = new ArrayList<>();
        for (GoldHallGameRuleDocument document : documents) {
            titles.add(document.title());
        }
        renderer.drawTabs(canvas, titles, selectedIndex);
        if (loading) {
            renderer.drawLoading(canvas);
        } else {
            renderer.drawContent(canvas, textLayout(), scroll);
        }
        GoldHallGameRuleDocument document = selectedDocument();
        if (document != null && document.gameId() == GoldHallGameRuleLayout.IMAGE_TEXT_GAME_ID) {
            renderer.drawImageTextButton(canvas);
        }
        canvas.restore();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        updateProjection();
        float designY = (event.getY() - offsetY) / scale;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchY = designY;
                touchStartY = designY;
                dragging = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!dragging && Math.abs(designY - touchStartY) * scale > touchSlop) {
                    dragging = true;
                }
                if (dragging) {
                    applyScroll(lastTouchY - designY);
                }
                lastTouchY = designY;
                return true;
            case MotionEvent.ACTION_UP:
                if (!dragging) {
                    handleTap((event.getX() - offsetX) / scale, designY);
                }
                return true;
            default:
                return true;
        }
    }

    private void applyScroll(float delta) {
        GoldHallGameRuleTextLayout layout = textLayout();
        if (layout == null) {
            return;
        }
        float max = layout.maxScroll(GoldHallGameRuleLayout.contentHeight(false));
        float next = Math.max(0.0f, Math.min(max, scroll + delta));
        if (next != scroll) {
            scroll = next;
            invalidate();
        }
    }

    private void handleTap(float designX, float designY) {
        if (GoldHallGameRuleLayout.closeContains(designX, designY)) {
            if (closeRequestedListener != null) {
                closeRequestedListener.onCloseRequested();
            }
            return;
        }
        for (int index = 0; index < documents.size(); index++) {
            if (!GoldHallGameRuleLayout.itemContains(index, designX, designY)) {
                continue;
            }
            // 原版 setBtnSelectState 对选中项关触摸，重复点当前页签不做任何事。
            if (index == selectedIndex) {
                return;
            }
            selectedIndex = index;
            scroll = 0.0f;
            textLayout = null;
            loading = documents.get(index).isEmpty();
            invalidate();
            if (gameSelectedListener != null) {
                gameSelectedListener.onGameSelected(documents.get(index).gameId());
            }
            return;
        }
        // 原版 Panel_4 满铺 1920x1080 只吃点击，不关闭弹层。
    }

    private void updateProjection() {
        float viewWidth = getWidth();
        float viewHeight = getHeight();
        if (viewWidth <= 0 || viewHeight <= 0) {
            scale = 1.0f;
            offsetX = 0.0f;
            offsetY = 0.0f;
            return;
        }
        AdaptiveViewport.Transform projection =
                projection(viewWidth, viewHeight, adaptiveSafeInsets());
        scale = projection.scaleX();
        offsetX = projection.offsetX();
        offsetY = projection.offsetY();
    }

    static AdaptiveViewport.Transform projection(
            float viewWidth, float viewHeight, AdaptiveViewport.Insets insets) {
        return AdaptiveViewport.create(
                        viewWidth,
                        viewHeight,
                        GoldHallGameRuleLayout.DESIGN_WIDTH,
                        GoldHallGameRuleLayout.DESIGN_HEIGHT,
                        insets)
                .dialogTransform(
                        GoldHallGameRuleLayout.DESIGN_WIDTH,
                        GoldHallGameRuleLayout.DESIGN_HEIGHT,
                        1.0f,
                        1.0f);
    }
}
