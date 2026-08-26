package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/** Inset-aware interaction surface for announcement list, detail, retry and scrolling. */
@SuppressLint("ViewConstructor")
final class AnnouncementCenterView extends AdaptiveCanvasView {
    interface Actions {
        void onDismissRequested();

        void onRetryRequested();

        void onAnnouncementRequested(long announcementId);

        void onPageRequested(String pageUrl);

        default void onActivityRequested() {}

        default void onAwardCenterRequested() {}
    }

    private final AnnouncementCenterState state = new AnnouncementCenterState();
    private final AnnouncementCenterRenderer renderer;
    private final Actions actions;
    private final int touchSlop;
    private Runnable buttonClickSound = () -> {};
    private float listScroll;
    private float detailScroll;
    private float startX;
    private float startY;
    private float lastY;
    private int scrollArea;
    private boolean dragging;
    private boolean outsidePressed;

    AnnouncementCenterView(Context context, Actions actions) {
        super(context);
        this.actions = actions;
        renderer = new AnnouncementCenterRenderer(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setClickable(true);
        setFocusable(true);
        setContentDescription("公告；可切换活动");
    }

    AnnouncementCenterState state() {
        return state;
    }

    void beginPageLoad() {
        state.beginPageLoad();
        listScroll = 0f;
        detailScroll = 0f;
        invalidate();
    }

    void showPage(AnnouncementApiProtocol.AnnouncementPage page) {
        state.showPage(page);
        listScroll = 0f;
        detailScroll = 0f;
        invalidate();
        if (!state.announcements().isEmpty()) {
            actions.onAnnouncementRequested(state.announcements().get(0).announcementId());
        }
    }

    void showPageError(String message) {
        state.showPageError(message);
        invalidate();
    }

    boolean beginDetailLoad(long announcementId) {
        boolean accepted = state.beginDetailLoad(announcementId);
        if (accepted) {
            detailScroll = 0f;
            invalidate();
        }
        return accepted;
    }

    void showDetail(AnnouncementApiProtocol.AnnouncementDetail detail) {
        state.showDetail(detail);
        detailScroll = 0f;
        invalidate();
    }

    void showDetailError(long announcementId, String message) {
        state.showDetailError(announcementId, message);
        invalidate();
    }

    void setButtonClickSound(Runnable sound) {
        buttonClickSound = sound == null ? () -> {} : sound;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.argb(178, 0, 0, 0));
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        int save =
                AdaptiveCanvasDrawing.apply(
                        canvas,
                        AnnouncementCenterLayout.panelTransform(
                                adaptiveViewport(
                                        AnnouncementCenterLayout.DESIGN_WIDTH,
                                        AnnouncementCenterLayout.DESIGN_HEIGHT)));
        renderer.draw(canvas, state, listScroll, detailScroll);
        canvas.restoreToCount(save);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return false;
        }
        AdaptiveViewport.Transform transform =
                AnnouncementCenterLayout.panelTransform(
                        adaptiveViewport(
                                AnnouncementCenterLayout.DESIGN_WIDTH,
                                AnnouncementCenterLayout.DESIGN_HEIGHT));
        float x = transform.unmapX(event.getX());
        float y = transform.unmapY(event.getY());
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                startX = x;
                startY = y;
                lastY = y;
                scrollArea =
                        AnnouncementCenterLayout.LIST.contains(x, y)
                                ? 1
                                : AnnouncementCenterLayout.DETAIL_BODY.contains(x, y) ? 2 : 0;
                outsidePressed = !AnnouncementCenterLayout.panelContains(x, y);
                dragging = false;
                return true;
            }
            case MotionEvent.ACTION_MOVE -> {
                if (scrollArea != 0
                        && !dragging
                        && (Math.abs(x - startX) + Math.abs(y - startY)) * transform.scaleY()
                                > touchSlop) {
                    dragging = true;
                }
                if (dragging) {
                    scrollBy(lastY - y);
                }
                lastY = y;
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                resetGesture();
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                if (!dragging) {
                    handleTap(x, y);
                }
                resetGesture();
                return true;
            }
            default -> {
                return true;
            }
        }
    }

    private void scrollBy(float delta) {
        if (scrollArea == 1) {
            listScroll =
                    AnnouncementCenterLayout.clampListScroll(
                            listScroll + delta, state.announcements().size());
        } else if (scrollArea == 2) {
            detailScroll =
                    AnnouncementCenterLayout.clampDetailScroll(
                            detailScroll + delta,
                            renderer.detailContentHeight(state.detail()));
        }
        invalidate();
    }

    private void handleTap(float x, float y) {
        if (AnnouncementCenterLayout.CLOSE.contains(x, y)
                || (outsidePressed && !AnnouncementCenterLayout.panelContains(x, y))) {
            clicked();
            actions.onDismissRequested();
            return;
        }
        if (LobbyActivityCenterLayout.sectionAt(x, y)
                == LobbyActivityCenterLayout.Section.ACTIVITY) {
            clicked();
            actions.onActivityRequested();
            return;
        }
        if (AnnouncementCenterLayout.AWARD_CENTER.contains(x, y)) {
            clicked();
            actions.onAwardCenterRequested();
            return;
        }
        if (state.pageState() == AnnouncementCenterState.PageState.ERROR
                && AnnouncementCenterLayout.RETRY.contains(x, y)) {
            clicked();
            actions.onRetryRequested();
            return;
        }
        AnnouncementApiProtocol.AnnouncementDetail detail = state.detail();
        if (detail != null
                && AnnouncementPageUrlPolicy.isSafe(detail.pageUrl())
                && AnnouncementCenterLayout.OPEN_PAGE.contains(x, y)) {
            clicked();
            actions.onPageRequested(detail.pageUrl());
            return;
        }
        int row =
                AnnouncementCenterLayout.rowAt(
                        x, y, listScroll, state.announcements().size());
        if (row >= 0) {
            clicked();
            actions.onAnnouncementRequested(
                    state.announcements().get(row).announcementId());
        }
    }

    private void clicked() {
        performClick();
        buttonClickSound.run();
    }

    private void resetGesture() {
        scrollArea = 0;
        dragging = false;
        outsidePressed = false;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
