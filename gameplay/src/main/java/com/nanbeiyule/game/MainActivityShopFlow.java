package com.nanbeiyule.game;

import android.widget.Toast;
import java.math.BigDecimal;
import java.util.List;

abstract class MainActivityShopFlow extends MainActivityMembershipPaymentFlow {
    private ShopCatalogState currentShopCatalog = ShopOriginalCatalog.create();
    private ShopCategory pendingShopCategory;

    @Override
    protected void showShop() {
        showShop(null);
    }

    @Override
    protected void showShop(ShopCategory initialCategory) {
        if (!canShowShop(isFinishing(), shopDialog != null)) {
            return;
        }
        pendingShopCategory = initialCategory;
        ShopDialog dialog =
                new ShopDialog(
                        this,
                        new ShopDialog.Actions() {
                            @Override
                            public void onProductSelected(ShopProduct product) {
                                onShopProductSelected(product);
                            }

                            @Override
                            public void onBagRequested() {
                                showShopInventory();
                            }

                            @Override
                            public void onMembershipNoticeRequested() {
                                showMembershipNotice();
                            }
                        });
        shopDialog = dialog;
        dialog.setCatalog(applyPendingShopCategory(currentShopCatalog));
        if (currentHomeState != null) {
            GameHomeState.Wallet homeWallet = currentHomeState.wallet();
            dialog.setWallet(
                    new ShopWalletState(
                            homeWallet.roomCards(),
                            homeWallet.coins(),
                            homeWallet.diamonds(),
                            0L));
        }
        if (originalLobbyAudioController != null) {
            dialog.setButtonClickSound(originalLobbyAudioController::playButtonClick);
        }
        dialog.setOnDismissListener(
                ignored -> {
                    if (shopDialog == dialog) {
                        shopDialog = null;
                    }
                    dismissMembershipNotice();
                    dismissShopInventory();
                    applyImmersiveMode();
                    onShopDismissed();
                });
        dialog.show();
        loadShopCatalog();
    }

    static boolean canShowShop(boolean finishing, boolean shopShowing) {
        return !finishing && !shopShowing;
    }

    protected void onShopDismissed() {}

    protected void onShopProductSelected(ShopProduct product) {
        if (product == null || !product.available()) {
            return;
        }
        // 下单前的实名闸门先问服务端，不能只信本机缓存。
        withVerifiedRealName(
                () -> {
                    if (product.currency() == ShopProduct.Currency.CNY) {
                        showShopPaymentChoice(product);
                    } else {
                        exchangeShopProduct(product);
                    }
                });
    }

    private void showMembershipNotice() {
        if (isFinishing() || membershipNoticeDialog != null) {
            return;
        }
        MembershipNoticeDialog dialog =
                new MembershipNoticeDialog(this, MembershipNotice.originalFallback());
        membershipNoticeDialog = dialog;
        if (originalLobbyAudioController != null) {
            dialog.setButtonClickSound(originalLobbyAudioController::playButtonClick);
        }
        dialog.setOnDismissListener(
                ignored -> {
                    if (membershipNoticeDialog == dialog) {
                        membershipNoticeDialog = null;
                    }
                    applyImmersiveMode();
                });
        dialog.show();
        loadMembershipNotice(dialog);
    }

    private void loadMembershipNotice(MembershipNoticeDialog dialog) {
        if (shopApiClient == null || authSessionCoordinator == null) {
            return;
        }
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        shopApiClient.loadMembershipNotice(
                                accessToken, shopCallback(callback)),
                new AuthSessionCoordinator.Callback<MembershipNoticeResult>() {
                    @Override
                    public void onSuccess(MembershipNoticeResult result) {
                        if (membershipNoticeDialog == dialog && result != null) {
                            dialog.setNotice(result.notice());
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissMembershipNotice();
                        dismissShopForLogin();
                    }

                    @Override
                    public void onError(String message) {
                        // Preserve the exact bundled CSB copy when the server is unavailable.
                    }
                });
    }

    private void loadShopCatalog() {
        ShopDialog dialog = shopDialog;
        if (dialog == null || shopApiClient == null || authSessionCoordinator == null) {
            return;
        }
        dialog.setLoading(true);
        dialog.setError(null);
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        shopApiClient.loadCatalog(accessToken, shopCallback(callback)),
                new AuthSessionCoordinator.Callback<ShopCatalogResult>() {
                    @Override
                    public void onSuccess(ShopCatalogResult result) {
                        if (shopDialog == null || result == null) {
                            return;
                        }
                        currentShopCatalog = result.catalog();
                        shopDialog.setCatalog(applyPendingShopCategory(result.catalog()));
                        shopDialog.setWallet(result.wallet());
                        shopDialog.setLoading(false);
                        shopDialog.setError(null);
                        pendingShopCategory = null;
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissShopForLogin();
                    }

                    @Override
                    public void onError(String message) {
                        if (shopDialog != null) {
                            shopDialog.setLoading(false);
                            shopDialog.setError(null);
                        }
                        showShopMessage("商城配置暂未同步，已使用内置商品目录");
                    }
                });
    }

    private void exchangeShopProduct(ShopProduct product) {
        ShopDialog dialog = shopDialog;
        if (dialog == null || shopApiClient == null || authSessionCoordinator == null) {
            return;
        }
        dialog.setLoading(true);
        dialog.setError(null);
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        shopApiClient.exchange(
                                accessToken,
                                product.productCode(),
                                shopCallback(callback)),
                new AuthSessionCoordinator.Callback<ShopPurchaseResult>() {
                    @Override
                    public void onSuccess(ShopPurchaseResult result) {
                        if (shopDialog != null) {
                            shopDialog.setLoading(false);
                            shopDialog.setWallet(result.wallet());
                            shopDialog.setError(null);
                        }
                        loadGameHome();
                        loadShopCatalog();
                        showShopMessage(result.duplicate() ? "商品已领取" : "购买成功");
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissShopForLogin();
                    }

                    @Override
                    public void onError(String message) {
                        if (shopDialog != null) {
                            shopDialog.setLoading(false);
                            shopDialog.setError(message);
                        }
                    }
                });
    }

    private void showShopPaymentChoice(ShopProduct product) {
        if (isFinishing() || membershipPaymentChoiceDialog != null) {
            return;
        }
        MembershipPurchaseSelection selection =
                new MembershipPurchaseSelection(
                        product.productCode(),
                        product.displayName(),
                        formatCny(product.priceMinor()));
        MembershipPaymentChoiceDialog dialog =
                new MembershipPaymentChoiceDialog(
                        this,
                        selection,
                        () -> createShopOrder(product.productCode()));
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
        dialog.show();
    }

    private void createShopOrder(String productCode) {
        if (shopApiClient == null
                || authSessionCoordinator == null
                || membershipPaymentLauncher == null) {
            return;
        }
        if ("YISHOUMI".equals(BuildConfig.PAYMENT_PROVIDER)
                && !membershipPaymentLauncher.isAlipayAvailable()) {
            showShopMessage("无法打开支付宝，请安装支付宝后重试");
            return;
        }
        if (membershipPaymentLauncher.relaunchPending(
                this::showShopMessage,
                this::resetMembershipPaymentReturnForRelaunch)) {
            return;
        }
        setShopLoading(true);
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        shopApiClient.createOrder(
                                accessToken,
                                productCode,
                                BuildConfig.PAYMENT_PROVIDER,
                                shopCallback(callback)),
                new AuthSessionCoordinator.Callback<MembershipOrderState>() {
                    @Override
                    public void onSuccess(MembershipOrderState result) {
                        setShopLoading(false);
                        membershipPaymentLauncher.launch(result, MainActivityShopFlow.this::showShopMessage);
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissShopForLogin();
                    }

                    @Override
                    public void onError(String message) {
                        setShopLoading(false);
                        showShopMessage(message);
                    }
                });
    }

    /** Reused by the Zhejiang bottom {@code 背包} action; loads the authenticated local inventory. */
    @Override
    protected void showShopInventory() {
        if (isFinishing()
                || shopInventoryDialog != null
                || shopApiClient == null
                || authSessionCoordinator == null) {
            return;
        }
        ShopInventoryDialog dialog = new ShopInventoryDialog(this);
        shopInventoryDialog = dialog;
        dialog.setCatalog(currentShopCatalog);
        dialog.setOnDismissListener(
                ignored -> {
                    if (shopInventoryDialog == dialog) {
                        shopInventoryDialog = null;
                    }
                    applyImmersiveMode();
                    onShopInventoryDismissed();
                });
        dialog.show();
        dialog.setLoading(true);
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        shopApiClient.loadInventory(accessToken, shopCallback(callback)),
                new AuthSessionCoordinator.Callback<List<ShopInventoryItem>>() {
                    @Override
                    public void onSuccess(List<ShopInventoryItem> result) {
                        if (shopInventoryDialog != null) {
                            shopInventoryDialog.setInventory(result);
                            shopInventoryDialog.setLoading(false);
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissShopForLogin();
                    }

                    @Override
                    public void onError(String message) {
                        if (shopInventoryDialog != null) {
                            shopInventoryDialog.setLoading(false);
                            shopInventoryDialog.setError(message);
                        }
                    }
                });
    }

    protected void onShopInventoryDismissed() {}

    @Override
    protected void refreshAfterConfirmedMembershipPayment() {
        super.refreshAfterConfirmedMembershipPayment();
        if (shopDialog != null) {
            loadShopCatalog();
        }
    }

    private void setShopLoading(boolean loading) {
        if (shopDialog != null) {
            shopDialog.setLoading(loading);
        }
    }

    private ShopCatalogState applyPendingShopCategory(ShopCatalogState catalog) {
        return pendingShopCategory == null ? catalog : catalog.select(pendingShopCategory);
    }

    private void dismissShopForLogin() {
        dismissMembershipNotice();
        dismissShopInventory();
        if (shopDialog != null) {
            ShopDialog dialog = shopDialog;
            shopDialog = null;
            dialog.dismiss();
        }
        showLoginPage();
    }

    private void dismissShopInventory() {
        if (shopInventoryDialog != null) {
            ShopInventoryDialog dialog = shopInventoryDialog;
            shopInventoryDialog = null;
            dialog.dismiss();
        }
    }

    private void dismissMembershipNotice() {
        if (membershipNoticeDialog != null) {
            MembershipNoticeDialog dialog = membershipNoticeDialog;
            membershipNoticeDialog = null;
            dialog.dismiss();
        }
    }

    private void showShopMessage(String message) {
        if (!isFinishing() && message != null && !message.isBlank()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private static String formatCny(long amountMinor) {
        return BigDecimal.valueOf(amountMinor, 2).stripTrailingZeros().toPlainString() + "元";
    }

    private static <T> ShopApiClient.ResponseCallback<T> shopCallback(
            AuthSessionCoordinator.CallCallback<T> callback) {
        return new ShopApiClient.ResponseCallback<>() {
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
}
