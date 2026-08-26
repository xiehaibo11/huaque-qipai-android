package com.nanbeiyule.game;

import android.widget.Toast;
import java.util.function.Consumer;

abstract class MainActivityFreeDrawFlow extends MainActivityTimeLoginActFlow {
    private final FreeDrawRewardGate freeDrawGate = new FreeDrawRewardGate();
    private FreeDrawApiClient freeDrawApiClient;
    private RewardedAdGateway rewardedAdGateway;
    private FreeDrawState freeDrawState;
    private FreeDrawSession freeDrawSession;
    private Consumer<FreeDrawState> freeDrawStateSink = ignored -> {};
    private FreeDrawRewardDialog freeDrawRewardDialog;
    private Toast freeDrawPreparingToast;
    private long freeDrawGeneration;

    protected final void loadFreeDrawState(Consumer<FreeDrawState> sink) {
        if (sink != null) freeDrawStateSink = sink;
        ensureClient();
        long generation = ++freeDrawGeneration;
        authSessionCoordinator.<FreeDrawState>execute(
                (token, callback) -> freeDrawApiClient.loadState(token, bridge(callback)),
                new AuthSessionCoordinator.Callback<FreeDrawState>() {
                    @Override
                    public void onSuccess(FreeDrawState state) {
                        if (generation != freeDrawGeneration || isFinishing()) return;
                        freeDrawState = state;
                        freeDrawStateSink.accept(state);
                    }

                    @Override
                    public void onLoginRequired() {
                        showLoginPage();
                    }

                    @Override
                    public void onError(String message) {
                        freeDrawToast(message);
                    }
                });
    }

    protected final void startFreeDraw() {
        if (freeDrawState == null) {
            loadFreeDrawState(freeDrawStateSink);
            freeDrawToast("正在获取免费抽奖状态，请稍候");
            return;
        }
        if (freeDrawState.remainingDraws() <= 0) {
            freeDrawToast("今日免费抽奖次数已用完");
            return;
        }
        if (!freeDrawGate.begin()) return;
        ensureClient();
        showFreeDrawPreparingToast();
        authSessionCoordinator.<FreeDrawSession>execute(
                (token, callback) -> freeDrawApiClient.openSession(token, bridge(callback)),
                new AuthSessionCoordinator.Callback<FreeDrawSession>() {
                    @Override
                    public void onSuccess(FreeDrawSession session) {
                        freeDrawSession = session;
                        freeDrawGate.onSessionOpened();
                        ensureGateway();
                        rewardedAdGateway.loadAndShow(
                                MainActivityFreeDrawFlow.this, session, new AdListener());
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissFreeDrawPreparingToast();
                        freeDrawGate.fail();
                        showLoginPage();
                    }

                    @Override
                    public void onError(String message) {
                        dismissFreeDrawPreparingToast();
                        freeDrawGate.fail();
                        freeDrawToast(message);
                    }
                });
    }

    private void claimReward(RewardedAdGateway.Evidence evidence) {
        FreeDrawSession session = freeDrawSession;
        if (session == null) {
            freeDrawGate.fail();
            freeDrawToast("广告奖励会话已失效，请重新抽奖");
            return;
        }
        authSessionCoordinator.<FreeDrawResult>execute(
                (token, callback) -> freeDrawApiClient.claim(token, session, evidence, bridge(callback)),
                new AuthSessionCoordinator.Callback<FreeDrawResult>() {
                    @Override
                    public void onSuccess(FreeDrawResult result) {
                        freeDrawGate.complete();
                        freeDrawSession = null;
                        if (freeDrawState != null) {
                            freeDrawState = freeDrawState.withRemaining(result.remainingDraws());
                            freeDrawStateSink.accept(freeDrawState);
                        }
                        showReward(result);
                        loadGameHome();
                    }

                    @Override
                    public void onLoginRequired() {
                        freeDrawGate.complete();
                        showLoginPage();
                    }

                    @Override
                    public void onError(String message) {
                        freeDrawGate.complete();
                        freeDrawToast(message);
                    }
                });
    }

    private void showReward(FreeDrawResult result) {
        if (isFinishing()) return;
        if (freeDrawRewardDialog != null) freeDrawRewardDialog.dismiss();
        freeDrawRewardDialog = new FreeDrawRewardDialog(this, result);
        freeDrawRewardDialog.setOnDismissListener(
                ignored -> {
                    freeDrawRewardDialog = null;
                    applyImmersiveMode();
                });
        freeDrawRewardDialog.show();
    }

    private void ensureClient() {
        if (freeDrawApiClient == null) freeDrawApiClient = new FreeDrawApiClient();
    }

    private void ensureGateway() {
        if (rewardedAdGateway == null) rewardedAdGateway = new CsjRewardedAdGateway();
    }

    private <T> FreeDrawApiClient.ResponseCallback<T> bridge(
            AuthSessionCoordinator.CallCallback<T> callback) {
        return new FreeDrawApiClient.ResponseCallback<>() {
            @Override public void onSuccess(T result) { callback.onSuccess(result); }
            @Override public void onUnauthorized() { callback.onUnauthorized(); }
            @Override public void onError(String message) { callback.onError(message); }
        };
    }

    private void freeDrawToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showFreeDrawPreparingToast() {
        dismissFreeDrawPreparingToast();
        freeDrawPreparingToast = FreeDrawPreparingToast.show(this);
    }

    private void dismissFreeDrawPreparingToast() {
        if (freeDrawPreparingToast == null) return;
        freeDrawPreparingToast.cancel();
        freeDrawPreparingToast = null;
    }

    @Override
    protected void onDestroy() {
        freeDrawGeneration++;
        dismissFreeDrawPreparingToast();
        if (freeDrawRewardDialog != null) {
            freeDrawRewardDialog.setOnDismissListener(null);
            freeDrawRewardDialog.dismiss();
            freeDrawRewardDialog = null;
        }
        if (rewardedAdGateway != null) {
            rewardedAdGateway.release();
            rewardedAdGateway = null;
        }
        if (freeDrawApiClient != null) {
            freeDrawApiClient.shutdown();
            freeDrawApiClient = null;
        }
        super.onDestroy();
    }

    private final class AdListener implements RewardedAdGateway.Listener {
        @Override
        public void onShown() {
            dismissFreeDrawPreparingToast();
            freeDrawGate.onAdShown();
        }

        @Override
        public void onRewardVerified(RewardedAdGateway.Evidence evidence) {
            if (freeDrawGate.onRewardVerified() == FreeDrawRewardGate.Action.CLAIM_REWARD) {
                claimReward(evidence);
            }
        }

        @Override
        public void onClosed() {
            boolean claiming = freeDrawGate.claimInFlight();
            freeDrawGate.onAdClosed();
            if (!claiming) freeDrawToast("完整观看广告后才能获得抽奖奖励");
            applyImmersiveMode();
        }

        @Override
        public void onError(String message) {
            dismissFreeDrawPreparingToast();
            freeDrawGate.fail();
            freeDrawToast(message);
            applyImmersiveMode();
        }
    }
}
