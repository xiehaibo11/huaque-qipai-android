package com.nanbeiyule.game;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.UUID;

final class PendingMembershipPaymentStore {
    private static final String PREFERENCES = "nanbei_pending_membership_payment";
    private static final String ORDER_ID = "order_id";
    private static final String STARTED_AT = "started_at";
    private static final String PAYMENT_URL = "payment_url";
    private static final String CANCELLATION_NOTICE_ACKNOWLEDGED =
            "cancellation_notice_acknowledged";

    private final SharedPreferences preferences;

    PendingMembershipPaymentStore(Context context) {
        preferences =
                context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    synchronized void save(String orderId, String paymentUrl) {
        if (!validOrderId(orderId)) {
            throw new IllegalArgumentException("Invalid membership payment order id");
        }
        if (MembershipPaymentUrl.parse(paymentUrl) == null) {
            throw new IllegalArgumentException("Invalid membership payment URL");
        }
        preferences
                .edit()
                .putString(ORDER_ID, orderId.toLowerCase(java.util.Locale.ROOT))
                .putLong(STARTED_AT, System.currentTimeMillis())
                .putString(PAYMENT_URL, paymentUrl)
                .putBoolean(CANCELLATION_NOTICE_ACKNOWLEDGED, false)
                .apply();
    }

    synchronized PendingPayment load() {
        String orderId = preferences.getString(ORDER_ID, "");
        long startedAt = preferences.getLong(STARTED_AT, 0L);
        String paymentUrl = preferences.getString(PAYMENT_URL, "");
        if (!validOrderId(orderId) || startedAt <= 0L) {
            return null;
        }
        return new PendingPayment(orderId, startedAt, paymentUrl);
    }

    synchronized boolean clear(String orderId) {
        if (!isCurrentOrder(orderId)) {
            return false;
        }
        return preferences.edit().clear().commit();
    }

    synchronized boolean isCancellationNoticeAcknowledged(String orderId) {
        return isCurrentOrder(orderId)
                && preferences.getBoolean(
                        CANCELLATION_NOTICE_ACKNOWLEDGED, false);
    }

    synchronized void acknowledgeCancellationNotice(String orderId) {
        if (isCurrentOrder(orderId)) {
            preferences
                    .edit()
                    .putBoolean(CANCELLATION_NOTICE_ACKNOWLEDGED, true)
                    .commit();
        }
    }

    synchronized void resetCancellationNotice(String orderId) {
        if (isCurrentOrder(orderId)) {
            preferences
                    .edit()
                    .putBoolean(CANCELLATION_NOTICE_ACKNOWLEDGED, false)
                    .commit();
        }
    }

    synchronized boolean isCurrentOrder(String orderId) {
        if (!validOrderId(orderId)) {
            return false;
        }
        String currentOrderId = preferences.getString(ORDER_ID, "");
        return validOrderId(currentOrderId)
                && currentOrderId.equalsIgnoreCase(orderId);
    }

    private static boolean validOrderId(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            return UUID.fromString(value).toString().equalsIgnoreCase(value);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    record PendingPayment(String orderId, long startedAtMillis, String paymentUrl) {}
}
