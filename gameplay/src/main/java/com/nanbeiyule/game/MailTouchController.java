package com.nanbeiyule.game;

import java.util.List;

/** Hit routing, list scrolling and select-mode gestures for the mail page. */
final class MailTouchController {
    interface Actions {
        void onClose();
        void onMailOpen(MailApiProtocol.MailEntry entry);
        void onReadAll();
        void onDelete(List<String> mailIds);
        void onClaimAll();
        default void onLoadNextPage() {}
        default void onDeleteBlocked() {}
        default void onDetailClose() {}
    }

    private static final int CLOSE = 1;
    private static final int DELETE_ALL = 2;
    private static final int READ_ALL = 3;
    private static final int CLAIM_ALL = 4;
    private static final int DETAIL_CLOSE = 5;
    private static final int ROW_BASE = 1000;

    private final Actions actions;
    private final MailState state;
    private final TapGestureGuard tapGuard;
    private float scroll;
    private float lastY;
    private boolean scrollGesture;
    private boolean contentInteractive = true;
    private int pressedTarget = TapGestureGuard.NO_TARGET;
    private Runnable invalidate = () -> {};

    MailTouchController(Actions actions, MailState state, float touchSlop) {
        this.actions = actions;
        this.state = state;
        tapGuard = new TapGestureGuard(touchSlop);
    }

    void setInvalidate(Runnable invalidate) {
        this.invalidate = invalidate == null ? () -> {} : invalidate;
    }

    void setContentInteractive(boolean interactive) {
        contentInteractive = interactive;
        if (!interactive) {
            cancel();
        }
    }

    void onDown(float x, float y) {
        pressedTarget = targetAt(x, y);
        lastY = y;
        scrollGesture = state.detail() == null && MailLayout.LIST.contains(x, y);
        tapGuard.begin(x, y, pressedTarget);
    }

    void onMove(float x, float y) {
        tapGuard.move(x, y);
        if (scrollGesture) {
            scroll = MailLayout.clampScroll(
                    scroll + lastY - y, state.mails().size());
        }
        lastY = y;
        if (targetAt(x, y) != pressedTarget) {
            pressedTarget = TapGestureGuard.NO_TARGET;
        }
    }

    void onUp(float x, float y) {
        int target = targetAt(x, y);
        boolean tap = tapGuard.finish(x, y, target);
        boolean finishedScroll = scrollGesture;
        pressedTarget = TapGestureGuard.NO_TARGET;
        scrollGesture = false;
        if (tap) {
            dispatch(target);
        } else if (finishedScroll
                && state.hasMore()
                && scroll >= MailLayout.maxScroll(state.mails().size())) {
            actions.onLoadNextPage();
        }
    }

    void cancel() {
        tapGuard.reset();
        pressedTarget = TapGestureGuard.NO_TARGET;
        scrollGesture = false;
    }

    float scroll() { return scroll; }

    void clampScroll() {
        scroll = MailLayout.clampScroll(scroll, state.mails().size());
    }

    private int targetAt(float x, float y) {
        if (state.detail() != null) {
            // 详情浮层打开时底层列表被遮挡，只注册 CSB 关闭按钮命中区。
            return MailLayout.DETAIL_CLOSE.contains(x, y)
                    ? DETAIL_CLOSE : TapGestureGuard.NO_TARGET;
        }
        if (MailLayout.CLOSE.contains(x, y)) {
            return CLOSE;
        }
        if (!contentInteractive) {
            return TapGestureGuard.NO_TARGET;
        }
        // Lua changeMailState：无邮件时三个底部按钮隐藏，同步不注册命中。
        if (MailLayout.bottomButtonsVisible(state.mails().size())) {
            if (MailLayout.BTN_DELETE_ALL.contains(x, y)) {
                return DELETE_ALL;
            }
            if (MailLayout.BTN_READ_ALL.contains(x, y)) {
                return READ_ALL;
            }
            if (MailLayout.BTN_CLAIM_ALL.contains(x, y)) {
                return CLAIM_ALL;
            }
        }
        if (!MailLayout.LIST.contains(x, y)) {
            return TapGestureGuard.NO_TARGET;
        }
        List<MailApiProtocol.MailEntry> mails = state.mails();
        for (int index = 0; index < mails.size(); index++) {
            if (MailLayout.rowRect(index, mails.size(), scroll).contains(x, y)) {
                return ROW_BASE + index;
            }
        }
        return TapGestureGuard.NO_TARGET;
    }

    private void dispatch(int target) {
        switch (target) {
            case CLOSE -> actions.onClose();
            case DETAIL_CLOSE -> {
                state.setDetail(null);
                actions.onDetailClose();
                invalidate.run();
            }
            case DELETE_ALL -> onDeleteAllTapped();
            case READ_ALL -> actions.onReadAll();
            case CLAIM_ALL -> actions.onClaimAll();
            default -> {
                int index = target - ROW_BASE;
                if (index >= 0 && index < state.mails().size()) {
                    onRowTapped(state.mails().get(index));
                }
            }
        }
    }

    private void onDeleteAllTapped() {
        if (!state.selectMode()) {
            if (!state.mails().isEmpty()) {
                state.enterSelectMode();
            }
        } else if (state.selectedMailIds().isEmpty()) {
            state.exitSelectMode();
        } else {
            List<String> deletableIds = state.deletableMailIds(state.selectedMailIds());
            if (deletableIds.isEmpty()) {
                actions.onDeleteBlocked();
            } else {
                actions.onDelete(deletableIds);
            }
        }
        invalidate.run();
    }

    private void onRowTapped(MailApiProtocol.MailEntry entry) {
        if (state.selectMode()) {
            state.toggleSelected(entry.mailId());
            invalidate.run();
        } else {
            actions.onMailOpen(entry);
        }
    }
}
