package com.nanbeiyule.game;

final class MembershipPaymentConfirmationSession {
    private MembershipPaymentReturn paymentReturn;
    private long generation;

    void record(MembershipPaymentReturn value) {
        if (value != null) {
            paymentReturn = value;
        }
    }

    MembershipPaymentReturn.Outcome outcomeFor(String orderId) {
        return belongsToOrder(paymentReturn, orderId)
                ? paymentReturn.outcome()
                : MembershipPaymentReturn.Outcome.CANCEL;
    }

    void resetForRelaunch(String orderId) {
        invalidateQueries();
        clearReturn(orderId);
    }

    void clearReturn(String orderId) {
        if (belongsToOrder(paymentReturn, orderId)) {
            paymentReturn = null;
        }
    }

    void invalidateQueries() {
        generation++;
    }

    long generation() {
        return generation;
    }

    boolean isCurrentGeneration(long candidate) {
        return generation == candidate;
    }

    private static boolean belongsToOrder(
            MembershipPaymentReturn value, String orderId) {
        return value != null
                && orderId != null
                && value.orderId().equalsIgnoreCase(orderId);
    }
}
