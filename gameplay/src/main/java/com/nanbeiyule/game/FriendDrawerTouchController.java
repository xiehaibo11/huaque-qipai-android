package com.nanbeiyule.game;

import android.graphics.RectF;
import android.view.MotionEvent;

/**
 * Hit testing, drag scrolling and click dispatch for the friend drawer.
 * The view forwards raw touch events here together with the shared
 * layout and state.
 */
final class FriendDrawerTouchController {
    enum HitRegion {
        NONE,
        ARROW,
        OUTSIDE,
        TAB_LIST,
        TAB_STARTING,
        TAB_RECALL,
        AVATAR,
        INVITE,
        INVITE_ALL,
        RECALL,
        LIST
    }

    private final FriendDrawerLayout layout;
    private final FriendDrawerState state;
    private final float touchSlopPx;

    private FriendDrawerView.Listener listener;
    private HitRegion downRegion = HitRegion.NONE;
    private int downIndex = -1;
    private float downPageY;
    private float lastPageY;
    private boolean dragging;

    FriendDrawerTouchController(
            FriendDrawerLayout layout,
            FriendDrawerState state,
            float touchSlopPx) {
        this.layout = layout;
        this.state = state;
        this.touchSlopPx = touchSlopPx;
    }

    void setListener(FriendDrawerView.Listener listener) {
        this.listener = listener;
    }

    FriendDrawerView.Listener listener() {
        return listener;
    }

    /** Returns true when the drawer consumes the event. */
    boolean onTouch(FriendDrawerView view, MotionEvent event) {
        GameHomeViewportLayout viewportLayout =
                GameHomeViewportLayout.calculate(
                        view.getWidth(),
                        view.getHeight(),
                        view.adaptiveSafeInsets());
        float pageX = viewportLayout.toPageX(event.getX());
        float pageY = viewportLayout.toPageY(event.getY());

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                dragging = false;
                downPageY = pageY;
                lastPageY = pageY;
                if (!view.isExpanded() && !view.isAnimating()) {
                    // Collapsed: the ready body and its arrow both open
                    // the panel (View.lua binds on_KW_BTND_OPEN to both).
                    if (layout.readyPanelRect().contains(pageX, pageY)
                            || layout.collapsedArrowRect()
                                    .contains(pageX, pageY)) {
                        downRegion = HitRegion.ARROW;
                        return true;
                    }
                    downRegion = HitRegion.NONE;
                    return false;
                }
                if (view.isAnimating()) {
                    downRegion = HitRegion.NONE;
                    return true;
                }
                if (view.isExpanded()
                        && layout.arrowRect(view.expandProgress())
                                .contains(pageX, pageY)) {
                    downRegion = HitRegion.OUTSIDE;
                    downIndex = -1;
                    return true;
                }
                downRegion = regionAt(pageX, pageY);
                downIndex =
                        downRegion == HitRegion.AVATAR
                                        || downRegion == HitRegion.INVITE
                                        || downRegion == HitRegion.RECALL
                                ? layout.itemIndexAt(
                                        pageY + state.scrollOffset())
                                : -1;
                return true;
            }
            case MotionEvent.ACTION_MOVE -> {
                if (isScrollable(downRegion)) {
                    float slop = touchSlopPx
                            / viewportLayout.pageScale();
                    if (!dragging
                            && Math.abs(pageY - downPageY) > slop) {
                        dragging = true;
                    }
                    if (dragging) {
                        state.scrollBy(
                                -(pageY - lastPageY),
                                layout.listRect().height(),
                                FriendDrawerLayout.ITEM_HEIGHT);
                        lastPageY = pageY;
                        view.invalidate();
                        maybeLoadMore();
                    }
                }
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                HitRegion region = downRegion;
                int index = downIndex;
                downRegion = HitRegion.NONE;
                downIndex = -1;
                if (dragging) {
                    dragging = false;
                    return true;
                }
                if (region == HitRegion.NONE) {
                    return true;
                }
                if (region == HitRegion.ARROW) {
                    if (layout.readyPanelRect().contains(pageX, pageY)
                            || layout.collapsedArrowRect()
                                    .contains(pageX, pageY)) {
                        view.performClick();
                        view.expand();
                    }
                    return true;
                }
                if (region == HitRegion.OUTSIDE) {
                    view.performClick();
                    view.collapse();
                    return true;
                }
                if (region == HitRegion.TAB_LIST
                        || region == HitRegion.TAB_STARTING
                        || region == HitRegion.TAB_RECALL) {
                    view.performClick();
                    view.setTab(
                            region == HitRegion.TAB_LIST
                                    ? FriendDrawerState.Tab.LIST
                                    : region == HitRegion.TAB_STARTING
                                            ? FriendDrawerState.Tab.STARTING
                                            : FriendDrawerState.Tab.RECALL);
                    return true;
                }
                if (region == HitRegion.INVITE_ALL) {
                    view.performClick();
                    if (listener != null) {
                        listener.onInviteAllRequested();
                    }
                    return true;
                }
                if (region == regionAt(pageX, pageY)
                        && (index < 0
                                || index
                                        == layout.itemIndexAt(
                                                pageY
                                                        + state
                                                                .scrollOffset()))) {
                    view.performClick();
                    dispatch(region, index);
                }
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                downRegion = HitRegion.NONE;
                downIndex = -1;
                dragging = false;
                return true;
            }
            default -> {
                return true;
            }
        }
    }

    private HitRegion regionAt(float pageX, float pageY) {
        if (!layout.panelRect().contains(pageX, pageY)) {
            return HitRegion.OUTSIDE;
        }
        for (int index = 0; index < 3; index++) {
            if (layout.tabRect(index).contains(pageX, pageY)) {
                return switch (index) {
                    case 0 -> HitRegion.TAB_LIST;
                    case 1 -> HitRegion.TAB_STARTING;
                    default -> HitRegion.TAB_RECALL;
                };
            }
        }
        if (state.tab() == FriendDrawerState.Tab.LIST
                && !state.friends().isEmpty()
                && layout.inviteAllRect().contains(pageX, pageY)) {
            return HitRegion.INVITE_ALL;
        }
        if (layout.listRect().contains(pageX, pageY)) {
            float contentY = pageY + state.scrollOffset();
            int index = layout.itemIndexAt(contentY);
            if (state.tab() == FriendDrawerState.Tab.RECALL) {
                java.util.List<FriendEntry> candidates =
                        state.recallCandidates(
                                System.currentTimeMillis());
                if (index >= 0 && index < candidates.size()) {
                    RectF item = layout.itemRect(index);
                    if (layout.inviteButtonRect(item)
                            .contains(pageX, contentY)) {
                        return HitRegion.RECALL;
                    }
                }
                return HitRegion.LIST;
            }
            if (index >= 0 && index < state.friends().size()) {
                FriendEntry friend = state.friends().get(index);
                RectF item = layout.itemRect(index);
                // The invite slot dispatches RECALL only for the
                // original offline state 1. Online/waiting/game states
                // stay on the invite path; the richer reserve/watch
                // actions need their missing original art before they
                // can be split safely.
                if (layout.inviteButtonRect(item)
                        .contains(pageX, contentY)) {
                    return friend.state() == FriendEntry.State.OFFLINE
                            ? HitRegion.RECALL
                            : HitRegion.INVITE;
                }
                float dx = pageX - layout.avatarCenterX();
                float dy = contentY - item.centerY();
                float radius = layout.avatarRadius() + 12.0f;
                if (dx * dx + dy * dy <= radius * radius) {
                    return HitRegion.AVATAR;
                }
            }
            return HitRegion.LIST;
        }
        return HitRegion.LIST;
    }

    private void dispatch(HitRegion region, int index) {
        if (listener == null) {
            return;
        }
        switch (region) {
            case AVATAR -> {
                FriendEntry friend = friendAt(index);
                if (friend != null) {
                    listener.onFriendAvatarRequested(friend);
                }
            }
            case INVITE -> {
                FriendEntry friend = friendAt(index);
                if (friend != null) {
                    listener.onInviteRequested(friend);
                }
            }
            case RECALL -> {
                // The same hit region serves the recall tab (candidates
                // list) and offline rows of the main friend list.
                FriendEntry friend =
                        state.tab() == FriendDrawerState.Tab.RECALL
                                ? recallCandidateAt(index)
                                : friendAt(index);
                if (friend != null) {
                    listener.onRecallRequested(friend);
                }
            }
            default -> {
                // LIST and header areas have no click action.
            }
        }
    }

    private FriendEntry friendAt(int index) {
        return index >= 0 && index < state.friends().size()
                ? state.friends().get(index)
                : null;
    }

    private FriendEntry recallCandidateAt(int index) {
        java.util.List<FriendEntry> candidates =
                state.recallCandidates(System.currentTimeMillis());
        return index >= 0 && index < candidates.size()
                ? candidates.get(index)
                : null;
    }

    private static boolean isScrollable(HitRegion region) {
        return region == HitRegion.LIST
                || region == HitRegion.AVATAR
                || region == HitRegion.INVITE;
    }

    private void maybeLoadMore() {
        if (listener != null
                && state.shouldLoadMore(
                        layout.listRect().height(),
                        FriendDrawerLayout.ITEM_HEIGHT)) {
            state.markLoadingMore();
            listener.onLoadMoreRequested();
        }
    }
}
