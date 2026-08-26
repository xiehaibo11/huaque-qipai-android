package com.nanbeiyule.game;

import android.app.Dialog;
import android.widget.Toast;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Authenticated lifecycle for fortune, the dedicated JuBaoPen flow, and Caishen. */
final class TaizhouFortuneFlow {
    private enum Screen { FORTUNE, TREASURE, CAISHEN }

    private static final long ORIGINAL_TREASURE_DURATION_SECONDS = 3L * 60L * 60L;
    private final MainActivityGameHomeDisplayFlow owner;
    private final Runnable loginRequired;
    private FortuneApiClient fortuneApiClient;
    private FortuneState state;
    private Dialog dialog;
    private Dialog treasureChildDialog;
    private TaizhouTreasurePotDialog treasureDialog;
    private TaizhouTreasurePotView treasureView;
    private TaizhouTreasureResultDialog treasureResultDialog;
    private TaizhouTreasurePlacementStore placementStore;
    private Screen requestedScreen;
    private boolean loading;

    TaizhouFortuneFlow(MainActivityGameHomeDisplayFlow owner, Runnable loginRequired) {
        this.owner = owner;
        this.loginRequired = loginRequired;
    }

    void open() {
        close();
        fortuneApiClient = new FortuneApiClient();
        placementStore = new TaizhouTreasurePlacementStore(owner);
    }

    void showFortune() {
        loadAndShow(Screen.FORTUNE);
    }

    void showTreasurePot() {
        loadAndShow(Screen.TREASURE);
    }

    void showCaishen() {
        loadAndShow(Screen.CAISHEN);
    }

    void close() {
        loading = false;
        requestedScreen = null;
        state = null;
        dismissTreasureChild();
        dismissMainDialog();
        placementStore = null;
        if (fortuneApiClient != null) {
            fortuneApiClient.shutdown();
            fortuneApiClient = null;
        }
    }

    private void loadAndShow(Screen screen) {
        requestedScreen = screen;
        if (loading || fortuneApiClient == null || owner.authSessionCoordinator == null) return;
        loading = true;
        owner.authSessionCoordinator.execute(
                (token, callback) ->
                        fortuneApiClient.loadState(token, fortuneCallback(callback)),
                new AuthSessionCoordinator.Callback<FortuneState>() {
                    @Override
                    public void onSuccess(FortuneState result) {
                        loading = false;
                        state = result;
                        if (requestedScreen == screen) showDialog(screen, result);
                    }

                    @Override
                    public void onLoginRequired() {
                        loading = false;
                        requireLogin();
                    }

                    @Override
                    public void onError(String message) {
                        loading = false;
                        toast(message);
                    }
                });
    }

    private void showDialog(Screen screen, FortuneState current) {
        if (owner.isFinishing() || requestedScreen != screen) return;
        dismissTreasureChild();
        dismissMainDialog();
        Dialog next = switch (screen) {
            case FORTUNE -> new TaizhouFortuneDialog(
                    owner,
                    current,
                    new SharedActions(Screen.FORTUNE));
            case TREASURE -> createTreasureDialog(current);
            case CAISHEN -> new TaizhouCaishenDialog(
                    owner,
                    current,
                    new SharedActions(Screen.CAISHEN));
        };
        dialog = next;
        next.setOnDismissListener(
                ignored -> {
                    if (dialog == next) {
                        dialog = null;
                        treasureDialog = null;
                        treasureView = null;
                    }
                    owner.applyImmersiveMode();
                });
        next.show();
    }

    private TaizhouTreasurePotDialog createTreasureDialog(FortuneState current) {
        TaizhouTreasurePotDialog next =
                new TaizhouTreasurePotDialog(owner, current, new TreasureActions());
        treasureDialog = next;
        treasureView = next.view();
        return next;
    }

    private void pray(String productCode, int quantity) {
        this.<Boolean>executeSharedWrite(
                (token, key, callback) ->
                        fortuneApiClient.pray(
                                token, key, productCode, quantity, callback),
                Screen.FORTUNE);
    }

    private void activateCaishen(String productCode) {
        this.<Boolean>executeSharedWrite(
                (token, key, callback) ->
                        fortuneApiClient.activateCaishen(
                                token, key, productCode, callback),
                Screen.CAISHEN);
    }

    private void drawTreasures(int count) {
        if (loading || fortuneApiClient == null || owner.authSessionCoordinator == null) {
            if (treasureView != null) treasureView.onDrawError();
            return;
        }
        loading = true;
        String key = UUID.randomUUID().toString();
        owner.authSessionCoordinator.<FortuneTreasureDrawResult>execute(
                (token, callback) -> fortuneApiClient.drawTreasures(
                        token, key, count, treasureCallback(callback)),
                new AuthSessionCoordinator.Callback<>() {
                    @Override
                    public void onSuccess(FortuneTreasureDrawResult result) {
                        loading = false;
                        if (treasureView != null) treasureView.onDrawResult(result);
                    }

                    @Override
                    public void onLoginRequired() {
                        loading = false;
                        requireLogin();
                    }

                    @Override
                    public void onError(String message) {
                        loading = false;
                        if (treasureView != null) treasureView.onDrawError();
                        if (treasureResultDialog != null) {
                            treasureResultDialog.setRepeatEnabled(true);
                        }
                        toast(message);
                    }
                });
    }

    private <T> void executeSharedWrite(WriteCall<T> call, Screen screen) {
        if (loading || fortuneApiClient == null || owner.authSessionCoordinator == null) return;
        loading = true;
        String key = UUID.randomUUID().toString();
        owner.authSessionCoordinator.<T>execute(
                (token, callback) -> call.execute(token, key, fortuneCallback(callback)),
                new AuthSessionCoordinator.Callback<T>() {
                    @Override
                    public void onSuccess(T ignored) {
                        loading = false;
                        dismissMainDialog();
                        loadAndShow(screen);
                    }

                    @Override
                    public void onLoginRequired() {
                        loading = false;
                        requireLogin();
                    }

                    @Override
                    public void onError(String message) {
                        loading = false;
                        toast(message);
                    }
                });
    }

    private void showDescription() {
        showTreasureChild(new TaizhouTreasureDescriptionDialog(owner));
    }

    private void showInventory() {
        if (state == null || placementStore == null) return;
        showTreasureChild(new TaizhouTreasureInventoryDialog(
                owner,
                currentUserId(),
                state,
                placementStore));
    }

    private void showResult(
            FortuneTreasureDrawResult result, FortuneState beforeDraw) {
        if (treasureView == null || owner.isFinishing()) return;
        state = mergeDrawResult(state, result);
        treasureView.replaceState(state);
        TaizhouTreasureResultDialog next = new TaizhouTreasureResultDialog(
                owner,
                beforeDraw,
                result,
                new TaizhouTreasureResultView.Actions() {
                    @Override
                    public void onCloseRequested() {
                        dismissTreasureChild();
                        reloadTreasureState(true);
                    }

                    @Override
                    public void onRepeatRequested(int count) {
                        if (loading || treasureView == null) {
                            if (treasureResultDialog != null) {
                                treasureResultDialog.setRepeatEnabled(true);
                            }
                            return;
                        }
                        if (treasureView.startRepeatDraw(count)) {
                            dismissTreasureChild();
                        } else if (treasureResultDialog != null) {
                            treasureResultDialog.setRepeatEnabled(true);
                        }
                    }
                });
        treasureResultDialog = next;
        showTreasureChild(next);
    }

    private void reloadTreasureState(boolean resumeAfterResult) {
        if (loading || fortuneApiClient == null || owner.authSessionCoordinator == null) {
            if (resumeAfterResult && treasureView != null) treasureView.resumeAfterResult();
            return;
        }
        loading = true;
        owner.authSessionCoordinator.execute(
                (token, callback) ->
                        fortuneApiClient.loadState(token, fortuneCallback(callback)),
                new AuthSessionCoordinator.Callback<FortuneState>() {
                    @Override
                    public void onSuccess(FortuneState result) {
                        loading = false;
                        state = result;
                        if (treasureView != null) {
                            treasureView.replaceState(result);
                            if (resumeAfterResult) treasureView.resumeAfterResult();
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        loading = false;
                        requireLogin();
                    }

                    @Override
                    public void onError(String message) {
                        loading = false;
                        if (resumeAfterResult && treasureView != null) {
                            treasureView.resumeAfterResult();
                        }
                        toast(message);
                    }
                });
    }

    private void showTreasureChild(Dialog child) {
        dismissTreasureChild();
        treasureChildDialog = child;
        child.setOnDismissListener(
                ignored -> {
                    if (treasureChildDialog == child) {
                        treasureChildDialog = null;
                        if (treasureResultDialog == child) treasureResultDialog = null;
                    }
                    owner.applyImmersiveMode();
                });
        child.show();
    }

    private void dismissTreasureChild() {
        Dialog current = treasureChildDialog;
        treasureChildDialog = null;
        if (treasureResultDialog == current) treasureResultDialog = null;
        if (current != null && current.isShowing()) current.dismiss();
    }

    private void dismissMainDialog() {
        Dialog current = dialog;
        dialog = null;
        treasureDialog = null;
        treasureView = null;
        if (current != null && current.isShowing()) current.dismiss();
    }

    private String currentUserId() {
        return owner.currentHomeState == null
                ? ""
                : owner.currentHomeState.player().userId();
    }

    private void requireLogin() {
        close();
        loginRequired.run();
    }

    private void toast(String message) {
        if (!owner.isFinishing()) {
            Toast.makeText(owner, message, Toast.LENGTH_SHORT).show();
        }
    }

    private final class SharedActions implements TaizhouFortuneToolView.Actions {
        private final Screen screen;

        SharedActions(Screen screen) {
            this.screen = screen;
        }

        @Override
        public void onPray(String productCode, int quantity) {
            if (screen == Screen.FORTUNE) pray(productCode, quantity);
        }

        @Override
        public void onCaishenActivate(String productCode) {
            if (screen == Screen.CAISHEN) activateCaishen(productCode);
        }
    }

    private final class TreasureActions implements TaizhouTreasurePotView.Actions {
        @Override
        public void onCloseRequested() {
            requestedScreen = null;
            dismissTreasureChild();
            dismissMainDialog();
        }

        @Override
        public void onDescriptionRequested() {
            showDescription();
        }

        @Override
        public void onInventoryRequested() {
            showInventory();
        }

        @Override
        public void onDrawRequested(int count) {
            drawTreasures(count);
        }

        @Override
        public void onResultReady(
                FortuneTreasureDrawResult result, FortuneState beforeDraw) {
            showResult(result, beforeDraw);
        }
    }

    @FunctionalInterface
    private interface WriteCall<T> {
        void execute(String token, String key, FortuneApiClient.ResponseCallback<T> callback);
    }

    private static FortuneApiClient.ResponseCallback<FortuneTreasureDrawResult> treasureCallback(
            AuthSessionCoordinator.CallCallback<FortuneTreasureDrawResult> callback) {
        return fortuneCallback(callback);
    }

    private static <T> FortuneApiClient.ResponseCallback<T> fortuneCallback(
            AuthSessionCoordinator.CallCallback<T> callback) {
        return new FortuneApiClient.ResponseCallback<>() {
            @Override public void onSuccess(T result) { callback.onSuccess(result); }
            @Override public void onUnauthorized() { callback.onUnauthorized(); }
            @Override public void onError(String message) { callback.onError(message); }
        };
    }

    private static FortuneState mergeDrawResult(
            FortuneState current, FortuneTreasureDrawResult result) {
        if (current == null) return null;
        Map<String, FortuneState.Treasure> treasures = new LinkedHashMap<>();
        for (FortuneState.Treasure treasure : current.treasures()) {
            treasures.put(treasure.treasureCode(), treasure);
        }
        for (FortuneTreasureDrawResult.Draw draw : result.draws()) {
            long remaining = remainingSeconds(draw.expiresAt());
            treasures.put(
                    draw.treasureCode(),
                    new FortuneState.Treasure(
                            draw.treasureCode(),
                            draw.name(),
                            draw.quality(),
                            draw.fortuneScore(),
                            draw.level(),
                            draw.expiresAt(),
                            remaining));
        }
        return new FortuneState(
                result.wallet(),
                current.wealthPoints(),
                current.luckPoints(),
                current.prayerProducts(),
                current.treasureProducts(),
                current.caishenProducts(),
                treasures.values().stream().toList(),
                current.caishenExpiresAt(),
                current.caishenRemainingSeconds(),
                current.treasureOneDrawPriceDiamonds(),
                current.treasureFiveDrawPriceDiamonds(),
                current.treasureFiveDrawDiscountTenths());
    }

    private static long remainingSeconds(String expiresAt) {
        try {
            return Math.max(0L,
                    Instant.parse(expiresAt).getEpochSecond() - Instant.now().getEpochSecond());
        } catch (DateTimeParseException | NullPointerException ignored) {
            return ORIGINAL_TREASURE_DURATION_SECONDS;
        }
    }
}
