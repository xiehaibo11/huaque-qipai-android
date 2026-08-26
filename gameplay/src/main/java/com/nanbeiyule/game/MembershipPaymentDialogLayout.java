package com.nanbeiyule.game;

/** CSB-derived 1920x1080 geometry shared by the two membership payment dialogs. */
final class MembershipPaymentDialogLayout {
    static final float DESIGN_WIDTH = 1920.0f;
    static final float DESIGN_HEIGHT = 1080.0f;

    static final DesignRect PAYMENT_ROOT =
            new DesignRect(340.0f, 164.0f, 1580.0f, 884.0f);
    static final DesignRect PAYMENT_PANEL =
            new DesignRect(344.5f, 177.0f, 1575.5f, 871.0f);
    static final DesignRect PAYMENT_CLOSE =
            new DesignRect(1587.4985f, 177.9215f, 1641.4985f, 231.9215f);
    static final DesignRect ALIPAY_ICON =
            new DesignRect(695.0f, 402.5f, 863.0f, 573.5f);
    static final DesignRect ALIPAY_SELECTED =
            new DesignRect(674.8f, 384.81f, 879.8f, 592.31f);
    static final DesignRect RESERVED_WECHAT_SLOT =
            new DesignRect(1062.0f, 402.5f, 1230.0f, 573.5f);
    static final DesignRect PAYMENT_RECOMMEND =
            new DesignRect(663.0f, 640.5f, 885.0f, 693.5f);
    static final DesignRect PAYMENT_CONFIRM =
            new DesignRect(782.5f, 712.3f, 1137.5f, 825.3f);

    static final float PAYMENT_TITLE_CENTER_X = 960.0f;
    static final float PAYMENT_TITLE_CENTER_Y = 257.6f;
    static final float PAYMENT_PROMPT_CENTER_X = 960.0f;
    static final float PAYMENT_PROMPT_CENTER_Y = 346.3127f;
    static final float ALIPAY_LABEL_CENTER_X = 774.0f;
    static final float ALIPAY_LABEL_CENTER_Y = 610.0f;
    static final float RECOMMEND_CENTER_X = 774.0f;
    static final float RECOMMEND_CENTER_Y = 667.0f;
    static final float PAYMENT_CONFIRM_CENTER_X = 960.0f;
    static final float PAYMENT_CONFIRM_CENTER_Y = 768.8f;

    static final DesignRect CANCEL_ROOT =
            new DesignRect(550.0f, 275.5f, 1370.0f, 804.5f);
    static final DesignRect CANCEL_PANEL =
            new DesignRect(551.2f, 324.5f, 1371.2f, 804.5f);
    static final DesignRect CANCEL_TITLE_LEFT =
            new DesignRect(550.0f, 275.5f, 960.0f, 356.5f);
    static final DesignRect CANCEL_TITLE_RIGHT =
            new DesignRect(960.0f, 275.5f, 1370.0f, 356.5f);
    static final DesignRect CANCEL_TITLE =
            new DesignRect(897.5f, 285.5f, 1022.5f, 346.5f);
    static final DesignRect CANCEL_CLOSE =
            new DesignRect(1301.0f, 255.0762f, 1400.0f, 357.0762f);
    static final DesignRect CANCEL_CONFIRM =
            new DesignRect(809.5f, 643.5f, 1110.5f, 774.5f);
    static final float CANCEL_MESSAGE_CENTER_X = 960.0f;
    static final float CANCEL_MESSAGE_CENTER_Y = 517.7393f;

    private MembershipPaymentDialogLayout() {}

    static PaymentAction paymentActionAt(float x, float y) {
        if (PAYMENT_CLOSE.contains(x, y)) {
            return PaymentAction.CLOSE;
        }
        if (PAYMENT_CONFIRM.contains(x, y)) {
            return PaymentAction.CONFIRM;
        }
        if (!PAYMENT_ROOT.contains(x, y)) {
            return PaymentAction.DISMISS;
        }
        return PaymentAction.NONE;
    }

    static CancelAction cancelActionAt(float x, float y) {
        if (CANCEL_CLOSE.contains(x, y)) {
            return CancelAction.CLOSE;
        }
        if (CANCEL_CONFIRM.contains(x, y)) {
            return CancelAction.CONFIRM;
        }
        if (!CANCEL_ROOT.contains(x, y)) {
            return CancelAction.DISMISS;
        }
        return CancelAction.NONE;
    }

    enum PaymentAction {
        NONE,
        CLOSE,
        CONFIRM,
        DISMISS
    }

    enum CancelAction {
        NONE,
        CLOSE,
        CONFIRM,
        DISMISS
    }

    record DesignRect(float left, float top, float right, float bottom) {
        boolean contains(float x, float y) {
            return x >= left && x <= right && y >= top && y <= bottom;
        }

        float width() {
            return right - left;
        }

        float height() {
            return bottom - top;
        }
    }
}
