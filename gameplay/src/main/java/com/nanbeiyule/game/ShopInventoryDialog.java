package com.nanbeiyule.game;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import java.util.List;
import java.util.Objects;

final class ShopInventoryDialog extends Dialog {
    private final ShopInventoryView inventoryView;

    ShopInventoryDialog(Context context) {
        this(context, defaultShopAction(context), null);
    }

    ShopInventoryDialog(
            Context context,
            Runnable openShopAction,
            LobbyBackpackUseAction useAction) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Runnable shopAction = Objects.requireNonNull(openShopAction, "openShopAction");
        inventoryView =
                new ShopInventoryView(
                        context,
                        new ShopInventoryView.Actions() {
                            @Override
                            public void onClose() {
                                dismiss();
                            }

                            @Override
                            public void onOpenShop() {
                                dismiss();
                                shopAction.run();
                            }

                            @Override
                            public boolean canUse(LobbyBackpackEntry entry) {
                                return useAction != null && useAction.canUse(entry);
                            }

                            @Override
                            public void onUse(LobbyBackpackEntry entry) {
                                if (useAction != null && useAction.canUse(entry)) {
                                    useAction.use(entry);
                                }
                            }
                        });
        applyCurrentWallet(context);
        setContentView(
                inventoryView,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            FullscreenWindowPolicy.apply(window);
        }
    }

    void setCatalog(ShopCatalogState catalog) {
        inventoryView.setCatalog(catalog);
    }

    void setInventory(List<ShopInventoryItem> inventory) {
        inventoryView.setInventory(inventory);
    }

    void setWallet(ShopWalletState wallet) {
        inventoryView.setWallet(wallet);
    }

    void setLoading(boolean loading) {
        inventoryView.setLoading(loading);
    }

    void setError(String message) {
        inventoryView.setError(message);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window != null) {
            window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            FullscreenWindowPolicy.apply(window);
            window.getDecorView().setSystemUiVisibility(MainActivityState.IMMERSIVE_UI_FLAGS);
            window.getDecorView().requestApplyInsets();
        }
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }

    private void applyCurrentWallet(Context context) {
        if (!(context instanceof MainActivityState host) || host.currentHomeState == null) {
            return;
        }
        GameHomeState.Wallet wallet = host.currentHomeState.wallet();
        inventoryView.setWallet(
                new ShopWalletState(
                        wallet.roomCards(), wallet.coins(), wallet.diamonds(), 0L));
    }

    private static Runnable defaultShopAction(Context context) {
        if (context instanceof MainActivityShopFlow host) {
            return host::showShop;
        }
        return () -> { };
    }
}
