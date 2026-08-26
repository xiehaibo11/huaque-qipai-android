package com.nanbeiyule.game;

final class MailDetailTouchController {
    interface Actions {
        void onClose();
        void onDelete(String mailId);
        void onClaim(String mailId);
        void onDeleteBlocked();
    }

    private static final int CLOSE = 1;
    private static final int DELETE = 2;
    private static final int CLAIM = 3;
    private final Actions actions;
    private final TapGestureGuard tapGuard;
    private MailApiProtocol.MailDetail detail;

    MailDetailTouchController(Actions actions, float touchSlop) {
        this.actions = actions;
        tapGuard = new TapGestureGuard(touchSlop);
    }

    void setDetail(MailApiProtocol.MailDetail detail) {
        this.detail = detail;
        tapGuard.reset();
    }

    void onDown(float x, float y) {
        tapGuard.begin(x, y, targetAt(x, y));
    }

    void onMove(float x, float y) {
        tapGuard.move(x, y);
    }

    void onUp(float x, float y) {
        int target = targetAt(x, y);
        if (!tapGuard.finish(x, y, target) || detail == null) {
            return;
        }
        String mailId = detail.entry().mailId();
        if (target == CLOSE) {
            actions.onClose();
        } else if (target == DELETE) {
            if (detail.entry().hasAttachment() && !detail.entry().claimed()) {
                actions.onDeleteBlocked();
            } else {
                actions.onDelete(mailId);
            }
        } else if (target == CLAIM) {
            actions.onClaim(mailId);
        }
    }

    void cancel() {
        tapGuard.reset();
    }

    private int targetAt(float x, float y) {
        if (detail == null) {
            return TapGestureGuard.NO_TARGET;
        }
        if (MailLayout.DETAIL_CLOSE.contains(x, y)) {
            return CLOSE;
        }
        boolean hasAttachments = !detail.attachments().isEmpty();
        if (!hasAttachments) {
            return MailLayout.DETAIL_DELETE_ONLY.contains(x, y)
                    ? DELETE : TapGestureGuard.NO_TARGET;
        }
        if (MailLayout.DETAIL_DELETE.contains(x, y)) {
            return DELETE;
        }
        if (!detail.entry().claimed() && MailLayout.DETAIL_CLAIM.contains(x, y)) {
            return CLAIM;
        }
        return TapGestureGuard.NO_TARGET;
    }
}
