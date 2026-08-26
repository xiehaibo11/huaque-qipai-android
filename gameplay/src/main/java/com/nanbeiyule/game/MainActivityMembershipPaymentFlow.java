package com.nanbeiyule.game;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

abstract class MainActivityMembershipPaymentFlow
        extends MainActivityMembershipFlow {
    private final Handler membershipPaymentHandler =
            new Handler(Looper.getMainLooper());

    private final MembershipPaymentConfirmationSession paymentConfirmationSession =
            new MembershipPaymentConfirmationSession();
    private Runnable membershipPaymentRetry;
    private boolean confirmationInFlight;

    @Override
    protected void handleMembershipPaymentIntent(Intent intent) {
        if (intent == null
                || intent.getData() == null
                || pendingMembershipPaymentStore == null) {
            return;
        }
        PendingMembershipPaymentStore.PendingPayment pending =
                pendingMembershipPaymentStore.load();
        if (pending == null) {
            return;
        }
        MembershipPaymentReturn parsed =
                MembershipPaymentReturn.parse(
                        intent.getData().toString(), pending.orderId());
        if (parsed == null) {
            return;
        }
        cancelScheduledMembershipPaymentRetry();
        confirmationInFlight = false;
        paymentConfirmationSession.invalidateQueries();
        paymentConfirmationSession.record(parsed);
        queryPendingMembershipPayment(1);
    }

    @Override
    protected void confirmPendingMembershipPaymentAfterForegroundReturn() {
        if (pendingMembershipPaymentStore == null
                || pendingMembershipPaymentStore.load() == null
                || confirmationInFlight
                || membershipPaymentRetry != null) {
            return;
        }
        queryPendingMembershipPayment(1);
    }

    private void queryPendingMembershipPayment(int completedQueries) {
        if (confirmationInFlight
                || membershipApiClient == null
                || authSessionCoordinator == null
                || pendingMembershipPaymentStore == null) {
            return;
        }
        PendingMembershipPaymentStore.PendingPayment pending =
                pendingMembershipPaymentStore.load();
        if (pending == null) {
            return;
        }
        queryPendingMembershipPayment(
                completedQueries,
                pending.orderId(),
                paymentConfirmationSession.generation());
    }

    private void queryPendingMembershipPayment(
            int completedQueries,
            String expectedOrderId,
            long queryGeneration) {
        if (confirmationInFlight
                || membershipApiClient == null
                || authSessionCoordinator == null
                || pendingMembershipPaymentStore == null
                || !paymentConfirmationSession.isCurrentGeneration(queryGeneration)
                || !pendingMembershipPaymentStore.isCurrentOrder(expectedOrderId)) {
            return;
        }
        PendingMembershipPaymentStore.PendingPayment pending =
                pendingMembershipPaymentStore.load();
        if (pending == null
                || !pending.orderId().equalsIgnoreCase(expectedOrderId)) {
            return;
        }
        confirmationInFlight = true;
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        membershipApiClient.loadOrder(
                                accessToken,
                                pending.orderId(),
                                membershipCallbackForPayment(callback)),
                new AuthSessionCoordinator.Callback<MembershipOrderState>() {
                    @Override
                    public void onSuccess(MembershipOrderState order) {
                        if (!paymentConfirmationSession
                                .isCurrentGeneration(queryGeneration)) {
                            return;
                        }
                        confirmationInFlight = false;
                        if (!pendingMembershipPaymentStore
                                .isCurrentOrder(pending.orderId())) {
                            return;
                        }
                        if (order == null
                                || !pending.orderId().equalsIgnoreCase(order.id())) {
                            showPaymentConfirmationMessage(
                                    "支付订单校验失败，请稍后在会员中心查看");
                            return;
                        }
                        applyPaymentDecision(
                                order, completedQueries, queryGeneration);
                    }

                    @Override
                    public void onLoginRequired() {
                        if (!paymentConfirmationSession
                                .isCurrentGeneration(queryGeneration)) {
                            return;
                        }
                        confirmationInFlight = false;
                    }

                    @Override
                    public void onError(String message) {
                        if (!paymentConfirmationSession
                                .isCurrentGeneration(queryGeneration)) {
                            return;
                        }
                        confirmationInFlight = false;
                        if (!pendingMembershipPaymentStore
                                .isCurrentOrder(pending.orderId())) {
                            return;
                        }
                        showPaymentConfirmationMessage(
                                message == null || message.isBlank()
                                        ? "支付结果查询失败，请稍后重试"
                                        : message);
                    }
                });
    }

    private void applyPaymentDecision(
            MembershipOrderState order,
            int completedQueries,
            long queryGeneration) {
        if (pendingMembershipPaymentStore == null
                || !pendingMembershipPaymentStore.isCurrentOrder(order.id())
                || !paymentConfirmationSession
                        .isCurrentGeneration(queryGeneration)) {
            return;
        }
        MembershipPaymentReturn.Outcome outcome =
                paymentConfirmationSession.outcomeFor(order.id());
        MembershipPaymentConfirmation.Decision decision =
                MembershipPaymentConfirmation.decide(
                        order.status(),
                        outcome,
                        completedQueries,
                        pendingMembershipPaymentStore
                                .isCancellationNoticeAcknowledged(order.id()));
        if (decision.clearPendingOrder()) {
            if (!pendingMembershipPaymentStore.clear(order.id())) {
                return;
            }
            paymentConfirmationSession.clearReturn(order.id());
        }
        switch (decision.action()) {
            case CONFIRMED_PAID -> {
                showPaymentConfirmationMessage("支付已确认，商品已到账");
                if (decision.refreshMembership()) {
                    refreshAfterConfirmedMembershipPayment();
                }
            }
            case SHOW_CANCELLED ->
                    showMembershipPaymentCancelled(order.id());
            case RETRY ->
                    scheduleMembershipPaymentRetry(
                            completedQueries + 1,
                            order.id(),
                            queryGeneration);
            case SHOW_CONFIRMING ->
                    showPaymentConfirmationMessage(
                            "支付结果确认中，请稍后在会员中心查看");
            case SHOW_FAILED ->
                    showPaymentConfirmationMessage("支付未完成，请重新发起支付");
            case NO_ACTION -> {
                // Keep querying the pending order on future foreground entries without
                // repeating an already acknowledged cancellation prompt.
            }
        }
    }

    private void scheduleMembershipPaymentRetry(
            int nextQuery, String orderId, long queryGeneration) {
        cancelScheduledMembershipPaymentRetry();
        membershipPaymentRetry =
                () -> {
                    membershipPaymentRetry = null;
                    queryPendingMembershipPayment(
                            nextQuery, orderId, queryGeneration);
                };
        membershipPaymentHandler.postDelayed(
                membershipPaymentRetry,
                MembershipPaymentConfirmation.RETRY_DELAY_MILLIS);
    }

    private void cancelScheduledMembershipPaymentRetry() {
        if (membershipPaymentRetry != null) {
            membershipPaymentHandler.removeCallbacks(membershipPaymentRetry);
            membershipPaymentRetry = null;
        }
    }

    @Override
    protected void cancelMembershipPaymentConfirmation() {
        cancelScheduledMembershipPaymentRetry();
        membershipPaymentHandler.removeCallbacksAndMessages(null);
        confirmationInFlight = false;
        paymentConfirmationSession.invalidateQueries();
    }

    @Override
    protected void resetMembershipPaymentReturnForRelaunch(String orderId) {
        cancelScheduledMembershipPaymentRetry();
        confirmationInFlight = false;
        paymentConfirmationSession.resetForRelaunch(orderId);
    }

    private <T> MembershipApiClient.ResponseCallback<T>
            membershipCallbackForPayment(
                    AuthSessionCoordinator.CallCallback<T> callback) {
        return new MembershipApiClient.ResponseCallback<>() {
            @Override
            public void onSuccess(T result) {
                callback.onSuccess(result);
            }

            @Override
            public void onUnauthorized() {
                callback.onUnauthorized();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        };
    }

    private void showPaymentConfirmationMessage(String message) {
        if (!isFinishing()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
}
