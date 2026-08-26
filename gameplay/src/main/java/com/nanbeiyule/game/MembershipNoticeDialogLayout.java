package com.nanbeiyule.game;

/** Exact 1920x1080 down-axis geometry restored from VipNoticeLayer.csb. */
final class MembershipNoticeDialogLayout {
    static final MembershipPaymentDialogLayout.DesignRect ROOT =
            rect(416.5f, 210f, 1503.5f, 870f);
    static final MembershipPaymentDialogLayout.DesignRect PANEL =
            rect(417.7f, 289f, 1503f, 870f);
    static final MembershipPaymentDialogLayout.DesignRect TITLE_LEFT =
            rect(416.5f, 210f, 960f, 291f);
    static final MembershipPaymentDialogLayout.DesignRect TITLE_RIGHT =
            rect(960f, 210f, 1503.5f, 291f);
    static final MembershipPaymentDialogLayout.DesignRect TITLE =
            rect(897.5f, 220f, 1022.5f, 281f);
    static final MembershipPaymentDialogLayout.DesignRect CLOSE =
            rect(1434.5f, 187.908f, 1533.5f, 289.908f);
    static final MembershipPaymentDialogLayout.DesignRect FLOWER_LEFT =
            rect(436.5f, 768f, 525.5f, 850f);
    static final MembershipPaymentDialogLayout.DesignRect FLOWER_RIGHT =
            rect(1394.5f, 768f, 1483.5f, 850f);
    static final MembershipPaymentDialogLayout.DesignRect NOTICE_TITLE =
            rect(510.0827f, 310.4446f, 678.0827f, 355.4446f);
    static final MembershipPaymentDialogLayout.DesignRect ITEM_ONE =
            rect(510.0827f, 372.9443f, 1410.0827f, 452.9443f);
    static final MembershipPaymentDialogLayout.DesignRect ITEM_TWO =
            rect(510.0827f, 466.6939f, 1410.0827f, 546.6939f);
    static final MembershipPaymentDialogLayout.DesignRect ITEM_THREE =
            rect(510.0827f, 560.4465f, 1235.0827f, 600.4465f);
    static final MembershipPaymentDialogLayout.DesignRect ITEM_FOUR =
            rect(510.0827f, 614.1937f, 1201.0827f, 654.1937f);
    static final MembershipPaymentDialogLayout.DesignRect CHANGE_NOTICE =
            rect(510.0827f, 667.9437f, 1410.0827f, 797.9437f);

    private MembershipNoticeDialogLayout() {}

    static Action actionAt(float x, float y) {
        if (CLOSE.contains(x, y)) {
            return Action.CLOSE;
        }
        if (!ROOT.contains(x, y)) {
            return Action.DISMISS;
        }
        return Action.NONE;
    }

    enum Action {
        NONE,
        CLOSE,
        DISMISS
    }

    private static MembershipPaymentDialogLayout.DesignRect rect(
            float left, float top, float right, float bottom) {
        return new MembershipPaymentDialogLayout.DesignRect(left, top, right, bottom);
    }
}
