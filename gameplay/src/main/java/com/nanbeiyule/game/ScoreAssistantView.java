package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

/** Inset-aware interaction surface for every score-assistant workflow state. */
@SuppressLint("ViewConstructor")
final class ScoreAssistantView extends AdaptiveCanvasView {
    interface Actions {
        void onDismissRequested();
        void onRetryRequested();
        void onTabRequested(ScoreAssistantState.Tab tab);
        void onCreateRequested();
        void onLedgerRequested(UUID ledgerId);
        void onRoundRequested(ScoreAssistantApiProtocol.LedgerDetail detail);
        void onEndRequested(UUID ledgerId);
        void onFavoriteRequested(UUID ledgerId, boolean favorite);
        void onDeleteRequested(UUID ledgerId);
        void onHistoryPageRequested(int page);
        void onMonthRequested(YearMonth month);
    }

    private final ScoreAssistantState state = new ScoreAssistantState();
    private final ScoreAssistantRenderer renderer;
    private final Actions actions;
    private final int touchSlop;
    private Runnable buttonClickSound = () -> {};
    private float scroll;
    private float startX;
    private float startY;
    private float lastY;
    private boolean dragging;
    private boolean outsidePressed;

    ScoreAssistantView(Context context, Actions actions) {
        super(context);
        this.actions = actions;
        renderer = new ScoreAssistantRenderer(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setClickable(true);
        setFocusable(true);
        setContentDescription("麻将计分器，首页、记录和我的");
    }

    ScoreAssistantState state() { return state; }
    void setButtonClickSound(Runnable sound) { buttonClickSound = sound == null ? () -> {} : sound; }

    void beginLoad() {
        state.beginLoad();
        invalidate();
    }

    void showError(String message) {
        state.showError(message);
        invalidate();
    }

    void showActive(List<ScoreAssistantApiProtocol.LedgerSummary> ledgers) {
        state.showActive(ledgers);
        scroll = 0f;
        invalidate();
    }

    void showHistory(ScoreAssistantApiProtocol.HistoryPage page) {
        state.showHistory(page);
        scroll = 0f;
        invalidate();
    }

    void showMonthly(ScoreAssistantApiProtocol.MonthlyStatistics monthly) {
        state.showMonthly(monthly);
        scroll = 0f;
        invalidate();
    }

    void showDetail(ScoreAssistantApiProtocol.LedgerDetail detail) {
        state.showDetail(detail);
        scroll = 0f;
        invalidate();
    }

    void applyRound(ScoreAssistantApiProtocol.RoundResult round) {
        state.applyRound(round);
        if (state.detail() != null) {
            scroll = ScoreAssistantLayout.maxScroll(state.detail().rounds().size());
        }
        invalidate();
    }

    void applyLedgerState(ScoreAssistantApiProtocol.LedgerState update) {
        state.applyLedgerState(update);
        invalidate();
    }

    void removeLedger(UUID ledgerId) {
        state.removeLedger(ledgerId);
        scroll = 0f;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) return;
        int save = AdaptiveCanvasDrawing.apply(canvas,
                ScoreAssistantLayout.panelTransform(adaptiveViewport(
                        ScoreAssistantLayout.LANDSCAPE_WIDTH,
                        ScoreAssistantLayout.LANDSCAPE_HEIGHT)));
        canvas.translate(0f, ScoreAssistantLayout.DESIGN_WIDTH);
        canvas.rotate(-90f);
        renderer.draw(canvas, state, scroll);
        canvas.restoreToCount(save);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0) return false;
        AdaptiveViewport.Transform transform = ScoreAssistantLayout.panelTransform(
                adaptiveViewport(ScoreAssistantLayout.LANDSCAPE_WIDTH,
                        ScoreAssistantLayout.LANDSCAPE_HEIGHT));
        float landscapeX = transform.unmapX(event.getX());
        float landscapeY = transform.unmapY(event.getY());
        float x = ScoreAssistantLayout.logicalX(landscapeX, landscapeY);
        float y = ScoreAssistantLayout.logicalY(landscapeX, landscapeY);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                startX = x;
                startY = y;
                lastY = y;
                outsidePressed = !ScoreAssistantLayout.panelContains(x, y);
                dragging = false;
                return true;
            }
            case MotionEvent.ACTION_MOVE -> {
                if (!dragging && ScoreAssistantLayout.CARDS.contains(startX, startY)
                        && (Math.abs(x - startX) + Math.abs(y - startY)) * transform.scaleY()
                        > touchSlop) dragging = true;
                if (dragging) {
                    scroll = ScoreAssistantLayout.clampScroll(scroll + lastY - y, cardCount());
                    invalidate();
                }
                lastY = y;
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                if (!dragging) handleTap(x, y);
                resetGesture();
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                resetGesture();
                return true;
            }
            default -> { return true; }
        }
    }

    private void handleTap(float x, float y) {
        if (ScoreAssistantLayout.CLOSE.contains(x, y)
                || (outsidePressed && !ScoreAssistantLayout.panelContains(x, y))) {
            clicked();
            actions.onDismissRequested();
            return;
        }
        ScoreAssistantState.Tab tab = ScoreAssistantLayout.tabAt(x, y);
        if (tab != null && tab != state.tab()) {
            clicked();
            state.selectTab(tab);
            scroll = 0f;
            invalidate();
            actions.onTabRequested(tab);
            return;
        }
        if (state.loadState() == ScoreAssistantState.LoadState.ERROR
                && ScoreAssistantLayout.RETRY.contains(x, y)) {
            clicked();
            actions.onRetryRequested();
            return;
        }
        if (state.loadState() != ScoreAssistantState.LoadState.CONTENT) return;
        ScoreAssistantApiProtocol.LedgerDetail detail = state.detail();
        if (detail != null) {
            handleDetailTap(x, y, detail);
            return;
        }
        if (state.tab() == ScoreAssistantState.Tab.ACTIVE
                && ScoreAssistantLayout.CREATE.contains(x, y)) {
            clicked();
            actions.onCreateRequested();
            return;
        }
        if (state.tab() == ScoreAssistantState.Tab.HISTORY) {
            ScoreAssistantApiProtocol.HistoryPage page = state.history();
            if (page != null && ScoreAssistantLayout.PAGE_PREVIOUS.contains(x, y) && page.page() > 1) {
                clicked(); actions.onHistoryPageRequested(page.page() - 1); return;
            }
            if (page != null && ScoreAssistantLayout.PAGE_NEXT.contains(x, y)
                    && page.page() < page.totalPages()) {
                clicked(); actions.onHistoryPageRequested(page.page() + 1); return;
            }
        }
        if (state.tab() == ScoreAssistantState.Tab.MONTHLY && state.monthly() != null) {
            YearMonth month = state.monthly().month();
            if (ScoreAssistantLayout.PAGE_PREVIOUS.contains(x, y)) {
                clicked(); actions.onMonthRequested(month.minusMonths(1)); return;
            }
            if (ScoreAssistantLayout.PAGE_NEXT.contains(x, y)) {
                clicked(); actions.onMonthRequested(month.plusMonths(1)); return;
            }
        }
        int index = ScoreAssistantLayout.cardAt(x, y, scroll, cardCount());
        if (index >= 0) {
            clicked();
            actions.onLedgerRequested(currentLedgers().get(index).ledgerId());
        }
    }

    private void handleDetailTap(
            float x, float y, ScoreAssistantApiProtocol.LedgerDetail detail) {
        if (ScoreAssistantLayout.TERTIARY.contains(x, y)) {
            clicked();
            state.clearDetail();
            scroll = 0f;
            invalidate();
            actions.onRetryRequested();
        } else if (ScoreAssistantLayout.PRIMARY.contains(x, y)) {
            clicked();
            if (detail.status() == ScoreAssistantApiProtocol.Status.IN_PROGRESS) {
                actions.onRoundRequested(detail);
            } else {
                actions.onFavoriteRequested(detail.ledgerId(), !detail.favorite());
            }
        } else if (ScoreAssistantLayout.SECONDARY.contains(x, y)) {
            clicked();
            if (detail.status() == ScoreAssistantApiProtocol.Status.IN_PROGRESS) {
                actions.onEndRequested(detail.ledgerId());
            } else {
                actions.onDeleteRequested(detail.ledgerId());
            }
        }
    }

    private List<ScoreAssistantApiProtocol.LedgerSummary> currentLedgers() {
        if (state.tab() == ScoreAssistantState.Tab.ACTIVE) return state.active();
        return state.history() == null ? List.of() : state.history().ledgers();
    }

    private int cardCount() {
        if (state.detail() != null) return state.detail().rounds().size();
        if (state.tab() == ScoreAssistantState.Tab.MONTHLY) return 0;
        return currentLedgers().size();
    }

    private void clicked() {
        performClick();
        buttonClickSound.run();
    }

    private void resetGesture() {
        dragging = false;
        outsidePressed = false;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
