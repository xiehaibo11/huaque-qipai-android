package com.nanbeiyule.game;

import android.widget.Toast;
import java.util.UUID;

/**
 * 定时登录有礼（原版 {@code lobby.Modules.TimeLoginAct}）的受保护页面流程。
 *
 * <p>页面只渲染服务端下发的状态：时段可领性、补领、金币上限、转盘进度与中奖格全部由
 * Time Login API 判定；客户端不本地推导资格，也不实现任何抽奖概率。
 */
abstract class MainActivityTimeLoginActFlow extends MainActivityDailyMissionFlow {
    private TimeLoginActApiClient timeLoginApiClient;
    private TimeLoginActDialog timeLoginDialog;
    private TimeLoginWheelDialog timeLoginWheelDialog;
    private boolean writeInFlight;
    private long requestGeneration;

    /** 打开定时登录有礼全屏页。 */
    @Override
    protected void showTimeLoginAct() {
        if (isFinishing() || timeLoginDialog != null || authSessionCoordinator == null) {
            return;
        }
        if (timeLoginApiClient == null) {
            timeLoginApiClient = new TimeLoginActApiClient();
        }
        timeLoginDialog = new TimeLoginActDialog(this, new TimeLoginActions());
        timeLoginDialog.setOnDismissListener(
                ignored -> {
                    timeLoginDialog = null;
                    writeInFlight = false;
                    requestGeneration++;
                    applyImmersiveMode();
                });
        timeLoginDialog.show();
        loadState(false);
    }

    private void loadState(boolean afterClaim) {
        TimeLoginActDialog dialog = timeLoginDialog;
        if (dialog == null || timeLoginApiClient == null || authSessionCoordinator == null) {
            return;
        }
        long generation = ++requestGeneration;
        if (!afterClaim) {
            dialog.setLoading(true);
        }
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        timeLoginApiClient.loadState(accessToken, bridge(callback)),
                new AuthSessionCoordinator.Callback<TimeLoginActState>() {
                    @Override
                    public void onSuccess(TimeLoginActState state) {
                        if (generation != requestGeneration || timeLoginDialog == null) {
                            return;
                        }
                        writeInFlight = false;
                        timeLoginDialog.setState(state);
                        if (timeLoginWheelDialog != null) {
                            timeLoginWheelDialog.setWheel(state.wheel());
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissForLogin();
                    }

                    @Override
                    public void onError(String message) {
                        if (generation != requestGeneration || timeLoginDialog == null) {
                            return;
                        }
                        writeInFlight = false;
                        timeLoginDialog.setError(message);
                    }
                });
    }

    private void claimSlot(TimeLoginActState.Slot slot) {
        TimeLoginActDialog dialog = timeLoginDialog;
        if (writeInFlight || dialog == null || slot == null || !slot.claimable()) {
            return;
        }
        writeInFlight = true;
        dialog.setLoading(true);
        String idempotencyKey = "time-login-slot-" + UUID.randomUUID();
        submit(
                (accessToken, callback) ->
                        timeLoginApiClient.claimSlot(
                                accessToken, idempotencyKey, slot.rewardId(), bridge(callback)),
                null);
    }

    private void drawWheel() {
        if (writeInFlight || timeLoginWheelDialog == null || timeLoginApiClient == null) {
            return;
        }
        writeInFlight = true;
        String idempotencyKey = "time-login-wheel-" + UUID.randomUUID();
        submit(
                (accessToken, callback) ->
                        timeLoginApiClient.drawWheel(accessToken, idempotencyKey, bridge(callback)),
                timeLoginWheelDialog);
    }

    /** 领取与抽奖共用同一条提交路径；结果只按服务端 claimFlag 分支。 */
    private void submit(
            AuthSessionCoordinator.AuthenticatedCall<TimeLoginClaimResult> call,
            TimeLoginWheelDialog wheelDialog) {
        long generation = ++requestGeneration;
        authSessionCoordinator.execute(
                call,
                new AuthSessionCoordinator.Callback<TimeLoginClaimResult>() {
                    @Override
                    public void onSuccess(TimeLoginClaimResult result) {
                        if (generation != requestGeneration) {
                            return;
                        }
                        writeInFlight = false;
                        onClaimResult(result, wheelDialog);
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissForLogin();
                    }

                    @Override
                    public void onError(String message) {
                        if (generation != requestGeneration) {
                            return;
                        }
                        writeInFlight = false;
                        if (timeLoginDialog != null) {
                            timeLoginDialog.setError(message);
                        }
                    }
                });
    }

    private void onClaimResult(TimeLoginClaimResult result, TimeLoginWheelDialog wheelDialog) {
        TimeLoginActDialog dialog = timeLoginDialog;
        long goldOver = dialog == null || dialog.state() == null ? 0L : dialog.state().goldOver();
        if (!result.succeeded()) {
            Toast.makeText(this, result.toastText(goldOver), Toast.LENGTH_SHORT).show();
            loadState(true);
            return;
        }
        if (wheelDialog != null && result.wheelSliceIndex() != null) {
            // 原版 WheelView.lua:131-146：先播 5.7 秒缓出旋转，再刷新状态。
            wheelDialog.startRoll(result.wheelSliceIndex());
        }
        loadState(true);
    }

    private void openWheel() {
        TimeLoginActDialog dialog = timeLoginDialog;
        if (dialog == null || dialog.state() == null || dialog.state().wheel() == null) {
            return;
        }
        if (timeLoginWheelDialog != null) {
            return;
        }
        timeLoginWheelDialog = new TimeLoginWheelDialog(this, new WheelActions());
        timeLoginWheelDialog.setOnDismissListener(
                ignored -> {
                    timeLoginWheelDialog = null;
                    applyImmersiveMode();
                });
        timeLoginWheelDialog.show();
        timeLoginWheelDialog.setWheel(dialog.state().wheel());
    }

    private <T> TimeLoginActApiClient.ResponseCallback<T> bridge(
            AuthSessionCoordinator.CallCallback<T> callback) {
        return new TimeLoginActApiClient.ResponseCallback<>() {
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

    private void dismissForLogin() {
        if (timeLoginWheelDialog != null) {
            timeLoginWheelDialog.dismiss();
        }
        if (timeLoginDialog != null) {
            timeLoginDialog.dismiss();
        }
        showLoginPage();
    }

    @Override
    protected void onDestroy() {
        if (timeLoginWheelDialog != null) {
            timeLoginWheelDialog.dismiss();
            timeLoginWheelDialog = null;
        }
        if (timeLoginDialog != null) {
            timeLoginDialog.dismiss();
            timeLoginDialog = null;
        }
        if (timeLoginApiClient != null) {
            timeLoginApiClient.shutdown();
            timeLoginApiClient = null;
        }
        super.onDestroy();
    }

    private final class TimeLoginActions implements TimeLoginActDialog.Actions {
        @Override
        public void onClose() {
            if (timeLoginDialog != null) {
                timeLoginDialog.dismiss();
            }
        }

        @Override
        public void onClaimSlot(TimeLoginActState.Slot slot) {
            claimSlot(slot);
        }

        @Override
        public void onOpenWheel() {
            openWheel();
        }
    }

    private final class WheelActions implements TimeLoginWheelDialog.Actions {
        @Override
        public void onClose() {
            if (timeLoginWheelDialog != null) {
                timeLoginWheelDialog.dismiss();
            }
        }

        @Override
        public void onDraw() {
            drawWheel();
        }
    }
}
