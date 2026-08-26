package com.nanbeiyule.game;

import java.util.Locale;

final class MembershipPaymentConfirmation {
    static final int MAX_QUERY_ATTEMPTS = 5;
    static final long RETRY_DELAY_MILLIS = 3_000L;

    enum Action {
        CONFIRMED_PAID,
        SHOW_CANCELLED,
        RETRY,
        SHOW_CONFIRMING,
        SHOW_FAILED,
        NO_ACTION
    }

    record Decision(
            Action action,
            boolean clearPendingOrder,
            boolean refreshMembership) {}

    private MembershipPaymentConfirmation() {}

    static Decision decide(
            String serverStatus,
            MembershipPaymentReturn.Outcome outcome,
            int completedQueries,
            boolean cancellationNoticeAcknowledged) {
        String status =
                serverStatus == null
                        ? ""
                        : serverStatus.trim().toUpperCase(Locale.ROOT);
        if ("PAID".equals(status)) {
            return new Decision(Action.CONFIRMED_PAID, true, true);
        }
        if ("FAILED".equals(status)
                || "CANCELLED".equals(status)
                || "EXPIRED".equals(status)) {
            return new Decision(Action.SHOW_FAILED, true, false);
        }
        if ("PENDING".equals(status) || "CREATED".equals(status)) {
            if (outcome == MembershipPaymentReturn.Outcome.CANCEL) {
                return new Decision(
                        cancellationNoticeAcknowledged
                                ? Action.NO_ACTION
                                : Action.SHOW_CANCELLED,
                        false,
                        false);
            }
            if (outcome == MembershipPaymentReturn.Outcome.SUCCESS
                    && completedQueries < MAX_QUERY_ATTEMPTS) {
                return new Decision(Action.RETRY, false, false);
            }
            return new Decision(Action.SHOW_CONFIRMING, false, false);
        }
        return new Decision(Action.SHOW_CONFIRMING, false, false);
    }
}
