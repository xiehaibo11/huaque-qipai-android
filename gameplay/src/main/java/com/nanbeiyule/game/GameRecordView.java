package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("ViewConstructor")
final class GameRecordView extends AdaptiveCanvasView {
    interface Actions {
        void onClose();

        void onLoadRequested(String date, long gameId, boolean gold);

        void onMembershipRequested();

        void onReplayRequested();

        void onTotalRequested(boolean gold);

        void onRecordRequested(GameRecordPage.Record record);
    }

    private final Actions actions;
    private final GameRecordRenderer renderer;
    private final float touchSlop;
    private final String latestDate;
    private final List<Long> knownGameIds = new ArrayList<>();
    private GameRecordPage page;
    private String selectedDate;
    private String error = "";
    private long selectedGameId;
    private boolean gold;
    private boolean dateMenu;
    private boolean gameMenu;
    private boolean loading = true;
    private boolean listGesture;
    private float downX;
    private float downY;
    private float lastY;
    private float scroll;
    private long lastManualRefresh;
    private Runnable buttonClickSound = () -> {};

    GameRecordView(Context context, Actions actions) {
        super(context);
        this.actions = actions;
        renderer = new GameRecordRenderer(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        latestDate = LocalDate.now(ZoneId.of("Asia/Shanghai")).toString();
        selectedDate = latestDate;
        setClickable(true);
        setFocusable(true);
        setContentDescription("我的战绩");
    }

    void requestCurrent() {
        loading = true;
        error = "";
        invalidate();
        actions.onLoadRequested(selectedDate, selectedGameId, gold);
    }

    void setPage(GameRecordPage next) {
        page = next;
        loading = false;
        error = "";
        if (next != null) {
            for (Long id : next.gameIds()) {
                if (!knownGameIds.contains(id)) knownGameIds.add(id);
            }
        }
        clampScroll();
        invalidate();
    }

    void setLoading(boolean value) {
        loading = value;
        if (value) error = "";
        invalidate();
    }

    void setError(String message) {
        loading = false;
        error = message == null ? "" : message;
        invalidate();
    }

    void setButtonClickSound(Runnable sound) {
        buttonClickSound = sound == null ? () -> {} : sound;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) return;
        AdaptiveViewport viewport = adaptiveViewport(
                GameRecordLayout.DESIGN_WIDTH, GameRecordLayout.DESIGN_HEIGHT);
        int save = AdaptiveCanvasDrawing.apply(canvas, viewport.designTransform());
        renderer.draw(
                canvas,
                page,
                gold,
                selectedDate,
                latestDate,
                selectedGameId,
                knownGameIds,
                dateMenu,
                gameMenu,
                loading,
                error,
                scroll,
                safeLeft(viewport),
                safeRight(viewport),
                safeBottom(viewport));
        canvas.restoreToCount(save);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0) return false;
        AdaptiveViewport viewport = adaptiveViewport(
                GameRecordLayout.DESIGN_WIDTH, GameRecordLayout.DESIGN_HEIGHT);
        float x = viewport.designTransform().unmapX(event.getX());
        float y = viewport.designTransform().unmapY(event.getY());
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                downX = x;
                downY = y;
                lastY = y;
                listGesture = y >= 225 && y <= 975 && !dateMenu && !gameMenu;
                return true;
            }
            case MotionEvent.ACTION_MOVE -> {
                if (listGesture && Math.abs(y - downY) > touchSlop / viewport.scale()) {
                    scroll += lastY - y;
                    clampScroll();
                    invalidate();
                }
                lastY = y;
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                listGesture = false;
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                float tolerance = touchSlop / viewport.scale();
                if (Math.abs(x - downX) <= tolerance && Math.abs(y - downY) <= tolerance) {
                    dispatchTap(x, y, viewport);
                }
                listGesture = false;
                return true;
            }
            default -> { return true; }
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        buttonClickSound.run();
        return true;
    }

    private void dispatchTap(float x, float y, AdaptiveViewport viewport) {
        float left = safeLeft(viewport);
        float right = safeRight(viewport);
        float bottom = safeBottom(viewport);
        if (dateMenu) {
            if (selectDate(x - left, y)) return;
            dateMenu = false;
            invalidate();
            return;
        }
        if (gameMenu) {
            if (selectGame(x - left, y)) return;
            gameMenu = false;
            invalidate();
            return;
        }
        if (GameRecordLayout.BACK.contains(x - left, y)) {
            click(); actions.onClose();
        } else if (GameRecordLayout.GOLD_TAB.contains(x - right, y)) {
            if (!gold) { click(); gold = true; selectedGameId = 0; requestCurrent(); }
        } else if (GameRecordLayout.BATTLE_TAB.contains(x - right, y)) {
            if (gold) { click(); gold = false; selectedGameId = 0; requestCurrent(); }
        } else if (GameRecordLayout.DATE.contains(x - left, y)) {
            click(); dateMenu = true; invalidate();
        } else if (!gold && GameRecordLayout.GAME.contains(x - left, y)) {
            click(); gameMenu = true; invalidate();
        } else if (GameRecordLayout.TOTAL.contains(x - right, y)) {
            click(); actions.onTotalRequested(gold);
        } else if (!gold && GameRecordLayout.REFRESH.contains(x - right, y)) {
            long now = SystemClock.elapsedRealtime();
            if (lastManualRefresh == 0L || now - lastManualRefresh >= 10_000L) {
                lastManualRefresh = now;
                click();
                requestCurrent();
            }
        } else if (gold && page != null && !page.membershipActive()
                && GameRecordLayout.MEMBER.contains(x, y)) {
            click(); actions.onMembershipRequested();
        } else if (!gold && GameRecordLayout.REPLAY.contains(x - right, y - bottom)) {
            click(); actions.onReplayRequested();
        } else {
            GameRecordPage.Record record = recordAt(x, y);
            if (record != null) { click(); actions.onRecordRequested(record); }
        }
    }

    private boolean selectDate(float x, float y) {
        if (x < 115 || x > 445 || y < 223 || y > 643) return false;
        int index = Math.min(6, (int) ((y - 223) / 60f));
        selectedDate = LocalDate.parse(latestDate).minusDays(index).toString();
        dateMenu = false;
        click();
        requestCurrent();
        return true;
    }

    private boolean selectGame(float x, float y) {
        if (x < 465 || x > 815 || y < 223) return false;
        int index = (int) ((y - 223) / 64f);
        if (index < 0 || index > knownGameIds.size()) return false;
        selectedGameId = index == 0 ? 0L : knownGameIds.get(index - 1);
        gameMenu = false;
        click();
        requestCurrent();
        return true;
    }

    private GameRecordPage.Record recordAt(float x, float y) {
        if (page == null || x < 1600 || x > 1880 || y < 225 || y > 975) return null;
        int index = (int) ((y - 225 + scroll) / 220f);
        return index >= 0 && index < page.records().size() ? page.records().get(index) : null;
    }

    private void clampScroll() {
        int count = page == null ? 0 : page.records().size();
        scroll = Math.max(0f, Math.min(scroll, Math.max(0f, count * 220f - 750f)));
    }

    private void click() { performClick(); }
    private static float safeLeft(AdaptiveViewport v) { return Math.max(0, v.safeDesignRect().left()); }
    private static float safeRight(AdaptiveViewport v) { return Math.min(0, v.safeDesignRect().right() - 1920); }
    private static float safeBottom(AdaptiveViewport v) { return Math.min(0, v.safeDesignRect().bottom() - 1080); }
}
