package com.nanbeiyule.game;

import android.widget.Toast;

abstract class MainActivityMembershipFlow extends MainActivityGoldMembershipCardsFlow {
    protected void showMembershipCenter() {
        if (isFinishing() || membershipCenterDialog != null) {
            return;
        }
        membershipCenterDialog = new MembershipCenterDialog(
                this, this::showMembershipDailyGift, this::showMembershipPurchaseFromCenter);
        if (originalLobbyAudioController != null) {
            membershipCenterDialog.setButtonClickSound(
                    originalLobbyAudioController::playButtonClick);
        }
        membershipCenterDialog.setOnDismissListener(
                dialog -> {
                    membershipCenterDialog = null;
                    applyImmersiveMode();
                });
        membershipCenterDialog.show();
    }

    protected void showMembershipDailyGift() {
        if (isFinishing()) {
            return;
        }
        if (membershipCenterDialog != null) {
            membershipCenterDialog.dismiss();
            membershipCenterDialog = null;
        }
        if (membershipDailyGiftDialog != null) {
            return;
        }
        membershipDailyGiftDialog =
                new MembershipDailyGiftDialog(
                        this,
                        new MembershipDailyGiftDialog.Actions() {
                            @Override
                            public void onClaimGift(int giftId) {
                                claimMembershipDailyGift(giftId);
                            }

                            @Override
                            public void onOpenMembership() {
                                showMembershipPurchaseFromDailyGift();
                            }

                            @Override
                            public void onTip() {
                                Toast.makeText(
                                                MainActivityMembershipFlow.this,
                                                "每日可从两个会员礼包中选择一个领取，领取后当日不可更换。",
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }

                            @Override
                            public void onGoldStatisticsSelected() {
                                loadMembershipGoldStatistics();
                            }
                        });
        if (originalLobbyAudioController != null) {
            membershipDailyGiftDialog.setButtonClickSound(
                    originalLobbyAudioController::playButtonClick);
        }
        membershipDailyGiftDialog.setOnDismissListener(
                dialog -> {
                    membershipDailyGiftDialog = null;
                    applyImmersiveMode();
                });
        membershipDailyGiftDialog.show();
        loadMembershipDailyGift();
    }

    private void loadMembershipDailyGift() {
        MembershipDailyGiftDialog dialog = membershipDailyGiftDialog;
        if (dialog == null || membershipApiClient == null || authSessionCoordinator == null) {
            return;
        }
        dialog.setLoading(true);
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        membershipApiClient.loadDailyGift(
                                accessToken, membershipCallback(callback)),
                new AuthSessionCoordinator.Callback<MembershipDailyGiftState>() {
                    @Override
                    public void onSuccess(MembershipDailyGiftState state) {
                        if (membershipDailyGiftDialog != null) {
                            membershipDailyGiftDialog.setState(state);
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissMembershipDailyGiftForLogin();
                    }

                    @Override
                    public void onError(String message) {
                        if (membershipDailyGiftDialog != null) {
                            membershipDailyGiftDialog.setError(message);
                        }
                    }
                });
    }

    private void claimMembershipDailyGift(int giftId) {
        MembershipDailyGiftDialog dialog = membershipDailyGiftDialog;
        if (dialog == null || membershipApiClient == null || authSessionCoordinator == null) {
            return;
        }
        dialog.setLoading(true);
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        membershipApiClient.claimDailyGift(
                                accessToken,
                                giftId,
                                membershipCallback(callback)),
                new AuthSessionCoordinator.Callback<MembershipDailyGiftState>() {
                    @Override
                    public void onSuccess(MembershipDailyGiftState state) {
                        if (membershipDailyGiftDialog != null) {
                            membershipDailyGiftDialog.setState(state);
                            Toast.makeText(
                                            MainActivityMembershipFlow.this,
                                            "会员礼包领取成功",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissMembershipDailyGiftForLogin();
                    }

                    @Override
                    public void onError(String message) {
                        if (membershipDailyGiftDialog != null) {
                            membershipDailyGiftDialog.setError(message);
                        }
                    }
                });
    }

    private void loadMembershipGoldStatistics() {
        MembershipDailyGiftDialog dialog = membershipDailyGiftDialog;
        if (dialog == null || membershipApiClient == null || authSessionCoordinator == null) {
            return;
        }
        dialog.setGoldStatisticsLoading(true);
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        membershipApiClient.loadGoldStatistics(
                                accessToken,
                                0L,
                                membershipCallback(callback)),
                new AuthSessionCoordinator.Callback<MembershipGoldStatisticsState>() {
                    @Override
                    public void onSuccess(MembershipGoldStatisticsState state) {
                        if (membershipDailyGiftDialog != null) {
                            membershipDailyGiftDialog.setGoldStatisticsState(state);
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissMembershipDailyGiftForLogin();
                    }

                    @Override
                    public void onError(String message) {
                        if (membershipDailyGiftDialog != null) {
                            membershipDailyGiftDialog.setGoldStatisticsError(message);
                        }
                    }
                });
    }

    private void showMembershipPurchase() {
        if (isFinishing() || membershipPurchaseDialog != null) {
            return;
        }
        membershipPurchaseDialog =
                new MembershipPurchaseDialog(
                        this, this::showMembershipPaymentChoice);
        if (originalLobbyAudioController != null) {
            membershipPurchaseDialog.setButtonClickSound(
                    originalLobbyAudioController::playButtonClick);
        }
        membershipPurchaseDialog.setOnDismissListener(
                dialog -> {
                    membershipPurchaseDialog = null;
                    applyImmersiveMode();
                });
        membershipPurchaseDialog.show();
        loadMembershipProducts();
    }

    private void showMembershipPaymentChoice(MembershipPurchaseSelection selection) {
        if (isFinishing()
                || membershipPaymentChoiceDialog != null
                || selection == null
                || !selection.isValid()) {
            return;
        }
        MembershipPaymentChoiceDialog dialog =
                new MembershipPaymentChoiceDialog(
                        this,
                        selection,
                        () -> createMembershipOrder(selection.productCode()));
        membershipPaymentChoiceDialog = dialog;
        if (originalLobbyAudioController != null) {
            dialog.setButtonClickSound(originalLobbyAudioController::playButtonClick);
        }
        dialog.setOnDismissListener(
                ignored -> {
                    if (membershipPaymentChoiceDialog == dialog) {
                        membershipPaymentChoiceDialog = null;
                    }
                    applyImmersiveMode();
                });
        membershipPaymentChoiceDialog.show();
    }

    protected void showMembershipPaymentCancelled(String cancelledOrderId) {
        if (isFinishing() || membershipPaymentCancelDialog != null) {
            return;
        }
        MembershipPaymentCancelDialog dialog =
                new MembershipPaymentCancelDialog(
                        this,
                        () -> {
                            if (pendingMembershipPaymentStore != null) {
                                pendingMembershipPaymentStore
                                        .acknowledgeCancellationNotice(cancelledOrderId);
                            }
                        });
        membershipPaymentCancelDialog = dialog;
        if (originalLobbyAudioController != null) {
            dialog.setButtonClickSound(originalLobbyAudioController::playButtonClick);
        }
        dialog.setOnDismissListener(
                ignored -> {
                    if (membershipPaymentCancelDialog == dialog) {
                        membershipPaymentCancelDialog = null;
                    }
                    applyImmersiveMode();
                });
        membershipPaymentCancelDialog.show();
    }

    private void loadMembershipProducts() {
        MembershipPurchaseDialog dialog = membershipPurchaseDialog;
        if (dialog == null || membershipApiClient == null || authSessionCoordinator == null) {
            return;
        }
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        membershipApiClient.loadProducts(
                                accessToken, membershipCallback(callback)),
                new AuthSessionCoordinator.Callback<MembershipProductsState>() {
                    @Override
                    public void onSuccess(MembershipProductsState result) {
                        if (membershipPurchaseDialog != null) {
                            membershipPurchaseDialog.setProducts(result);
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissMembershipPurchaseForLogin();
                        showLoginPage();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(
                                        MainActivityMembershipFlow.this,
                                        message,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void createMembershipOrder(String productCode) {
        if (membershipApiClient == null
                || authSessionCoordinator == null
                || membershipPaymentLauncher == null) {
            return;
        }
        if ("YISHOUMI".equals(membershipPaymentProvider())
                && !membershipPaymentLauncher.isAlipayAvailable()) {
            showMembershipPaymentMessage("无法打开支付宝，请安装支付宝后重试");
            return;
        }
        if (membershipPaymentLauncher.relaunchPending(
                this::showMembershipPaymentMessage,
                this::resetMembershipPaymentReturnForRelaunch)) {
            return;
        }
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        membershipApiClient.createMembershipOrder(
                                accessToken,
                                productCode,
                                membershipPaymentProvider(),
                                membershipCallback(callback)),
                new AuthSessionCoordinator.Callback<MembershipOrderState>() {
                    @Override
                    public void onSuccess(MembershipOrderState result) {
                        membershipPaymentLauncher.launch(result, this::showMembershipPaymentMessage);
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissMembershipPurchaseForLogin();
                        showLoginPage();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(
                                        MainActivityMembershipFlow.this,
                                        message,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    private void showMembershipPaymentMessage(String message) {
                        MainActivityMembershipFlow.this.showMembershipPaymentMessage(message);
                    }
                });
    }

    protected void resetMembershipPaymentReturnForRelaunch(String orderId) {
        // Implemented by the payment confirmation layer that owns return state.
    }

    private void showMembershipPaymentMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String membershipPaymentProvider() {
        return BuildConfig.PAYMENT_PROVIDER;
    }

    protected void refreshAfterConfirmedMembershipPayment() {
        loadGameHome();
        if (membershipDailyGiftDialog != null) {
            loadMembershipDailyGift();
        }
        if (membershipPurchaseDialog != null) {
            loadMembershipProducts();
        }
    }

    private <T> MembershipApiClient.ResponseCallback<T> membershipCallback(
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

    private void showMembershipPurchaseFromDailyGift() {
        if (membershipDailyGiftDialog != null) {
            membershipDailyGiftDialog.dismiss();
            membershipDailyGiftDialog = null;
        }
        showMembershipPurchase();
    }

    private void showMembershipPurchaseFromCenter() {
        if (membershipCenterDialog != null) {
            membershipCenterDialog.dismiss();
            membershipCenterDialog = null;
        }
        showMembershipPurchase();
    }

    private void dismissMembershipDailyGiftForLogin() {
        if (membershipDailyGiftDialog != null) {
            membershipDailyGiftDialog.dismiss();
            membershipDailyGiftDialog = null;
        }
        dismissMembershipCenterForLogin();
        dismissMembershipPurchaseForLogin();
        showLoginPage();
    }

    private void dismissMembershipCenterForLogin() {
        MembershipCenterDialog dialog = membershipCenterDialog;
        membershipCenterDialog = null;
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void dismissMembershipPurchaseForLogin() {
        MembershipPurchaseDialog dialog = membershipPurchaseDialog;
        membershipPurchaseDialog = null;
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
