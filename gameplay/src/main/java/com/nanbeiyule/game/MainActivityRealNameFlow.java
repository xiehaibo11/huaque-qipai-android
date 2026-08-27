package com.nanbeiyule.game;

import android.widget.Toast;

/**
 * Real-name verification flow: checks status after a fresh home load, gates
 * game entries on verification, backs the personal-center entry, and owns
 * the RealNameDialog lifecycle. The dialog itself stays offline.
 */
abstract class MainActivityRealNameFlow
        extends MainActivityFriendDialogFlow {

    /**
     * 原版每次回到大厅都会 {@code PlayerData:flushPlayerPosition()}
     * （{@code lobby/Modules/Lobby/View.lua:358}），据此决定主入口显示创建还是返场。
     */
    @Override
    protected void displayGameHome(GameHomeState state) {
        super.displayGameHome(state);
        refreshBoxRoomEntryState();
    }

    /**
     * 刷新大厅主入口的「创建房间 / 返回房间」两态。
     *
     * <p>原版在登录、回大厅、离开房间后都会 {@code PlayerData:flushPlayerPosition()}，再由
     * {@code LobbyView:showBackBoom()} 换贴图（{@code app/Data/PlayerData.lua:965}、
     * {@code lobby/Modules/Lobby/View.lua:725}）。查询失败时保持当前贴图，不把大厅卡在加载态。
     */
    protected void refreshBoxRoomEntryState() {
        if (isFinishing() || currentHomeState == null || currentHomeView == null) {
            return;
        }
        if (createRoomFlow == null) {
            createRoomFlow = new MainActivityCreateRoomFlow(this);
        }
        createRoomFlow.loadPlacement(
                placement -> {
                    if (!isFinishing() && currentHomeView != null) {
                        currentHomeView.setInRoom(placement.hasRoom());
                    }
                });
    }

    @Override
    protected void checkRealNameAfterHomeLoad() {
        if (isFinishing() || currentHomeState == null || realNameStatusStore == null) {
            return;
        }
        String userId = currentHomeState.player().userId();
        loadStatus(
                new AuthSessionCoordinator.Callback<>() {
                    @Override
                    public void onSuccess(RealNameStatus status) {
                        if (isFinishing() || realNameStatusStore == null) {
                            return;
                        }
                        realNameStatusStore.save(userId, status.status());
                        if (status.status() == RealNameStatus.Status.UNVERIFIED) {
                            showRealNameDialog(null, status.alipayOneTapEnabled());
                        } else {
                            if (pendingJoinRoomNumber != null && realNameDialog != null) {
                                dismissRealNameDialog();
                            }
                            joinPendingRoomAfterHomeDisplay();
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        requireLogin();
                    }

                    @Override
                    public void onError(String message) {
                        // The automatic check stays silent on failure and
                        // never blocks the game home.
                    }
                });
    }

    @Override
    protected void onGameHomeEntryRequested(GameHomeState.Entry entry) {
        withVerifiedRealName(() -> routeVerifiedEntry(entry));
    }

    private void routeVerifiedEntry(GameHomeState.Entry entry) {
        if ("CREATE_ROOM".equals(entry.code())) {
            routeBoxRoomEntry(this::showCreateRoom);
            return;
        }
        if ("JOIN_ROOM".equals(entry.code())) {
            openJoinRoomEntry(() -> {});
            return;
        }
        if ("MATCH".equals(entry.code())) {
            showMatchArena();
            return;
        }
        if ("TAIZHOU_MAHJONG".equals(entry.code())) {
            // 原版大厅主格子进的是金币场 30400（GoldTaiZhouMahjong），不是房卡玩法 30109；
            // 见 android/docs/ORIGINAL-GOLD-CHOOSE-ROOM-EVIDENCE.md。
            showGoldChooseRoom(30400L);
            return;
        }
        super.onGameHomeEntryRequested(entry);
    }

    /**
     * 实名闸门：先问服务端 {@code /status}，用结果覆盖本机缓存，通过才执行 {@code onVerified}。
     *
     * <p>以前这里只读 {@link RealNameStatusStore} 的缓存，服务端记录被清掉后本机仍是 VERIFIED，
     * 闸门会继续放行。取不到服务端结果（断网、接口异常）时退回缓存，行为与改动前一致，
     * 不会因为网络抖动把已实名用户挡在门外。
     */
    protected void withVerifiedRealName(Runnable onVerified) {
        if (isFinishing() || currentHomeState == null || realNameStatusStore == null) {
            return;
        }
        String userId = currentHomeState.player().userId();
        if (realNameApiClient == null || authSessionCoordinator == null) {
            fallBackToCachedRealName(userId, onVerified);
            return;
        }
        loadStatus(
                new AuthSessionCoordinator.Callback<>() {
                    @Override
                    public void onSuccess(RealNameStatus status) {
                        if (isFinishing() || realNameStatusStore == null) {
                            return;
                        }
                        realNameStatusStore.save(userId, status.status());
                        if (status.status() == RealNameStatus.Status.VERIFIED) {
                            onVerified.run();
                        } else {
                            showRealNameDialog(null, status.alipayOneTapEnabled());
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        requireLogin();
                    }

                    @Override
                    public void onError(String message) {
                        fallBackToCachedRealName(userId, onVerified);
                    }
                });
    }

    private void fallBackToCachedRealName(String userId, Runnable onVerified) {
        if (isFinishing() || realNameStatusStore == null) {
            return;
        }
        if (realNameStatusStore.statusFor(userId) == RealNameStatus.Status.VERIFIED) {
            onVerified.run();
            return;
        }
        showRealNameDialog(null, true);
    }

    protected void showRealNameCenter() {
        if (isFinishing() || realNameDialog != null
                || currentHomeState == null || realNameStatusStore == null) {
            return;
        }
        String userId = currentHomeState.player().userId();
        loadStatus(
                new AuthSessionCoordinator.Callback<>() {
                    @Override
                    public void onSuccess(RealNameStatus status) {
                        if (isFinishing() || realNameStatusStore == null) {
                            return;
                        }
                        realNameStatusStore.save(userId, status.status());
                        if (status.status()
                                == RealNameStatus.Status.VERIFIED) {
                            showRealNameDialog(status, false);
                        } else {
                            showRealNameDialog(
                                    null, status.alipayOneTapEnabled());
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        requireLogin();
                    }

                    @Override
                    public void onError(String message) {
                        if (!isFinishing()) {
                            Toast.makeText(MainActivityRealNameFlow.this, message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void showLoginPage() {
        if (createRoomFlow != null) {
            createRoomFlow.dismiss();
        }
        if (joinRoomFlow != null) {
            joinRoomFlow.dismiss();
        }
        if (matchArenaFlow != null) {
            matchArenaFlow.dismiss();
        }
        if (realNameStatusStore != null) {
            realNameStatusStore.clear();
        }
        dismissRealNameDialog();
        super.showLoginPage();
    }

    /**
     * 原版包厢入口闸门：先问「我现在在哪个房间」，在房间里就返场，不在才打开建房 / 加入页。
     *
     * <p>原版 {@code lobby/Modules/Lobby/View.lua:794} 与 {@code :862} 用同一个
     * {@code position.gameID == 0} 判断分别守住这两个入口，所以玩家不可能带着未结束的房间走进
     * 建房页；查不到位置（断网、接口异常）时按原版的「不在房间」默认放行，不把玩家挡在大厅。
     */
    private void routeBoxRoomEntry(Runnable whenNotInRoom) {
        if (currentHomeState == null) {
            return;
        }
        if (createRoomFlow == null) {
            createRoomFlow = new MainActivityCreateRoomFlow(this);
        }
        createRoomFlow.routeByPlacement(whenNotInRoom);
    }

    private void showCreateRoom() {
        showCreateRoom(0L);
    }

    private void showCreateRoom(long initialGameId) {
        if (currentHomeState == null) {
            return;
        }
        if (createRoomFlow == null) {
            createRoomFlow = new MainActivityCreateRoomFlow(this);
        }
        createRoomFlow.show(currentHomeState.region().lobbyId(), initialGameId);
    }

    /** 结算后确认「重新匹配队友」：用上一次的档位原地重排（GameManager:onReqPlayerPlace）。 */
    @Override
    void rematchGoldRoom(long gameId) {
        if (currentHomeState == null) {
            return;
        }
        if (goldChooseRoomFlow == null) {
            goldChooseRoomFlow = new MainActivityGoldChooseRoomFlow(this);
        }
        goldChooseRoomFlow.rematchLastLevel(gameId);
    }

    private void showGoldChooseRoom(long gameId) {
        if (currentHomeState == null) {
            return;
        }
        if (goldChooseRoomFlow == null) {
            goldChooseRoomFlow = new MainActivityGoldChooseRoomFlow(this);
        }
        goldChooseRoomFlow.show(gameId);
    }

    private void showJoinRoom(String roomNumber) {
        showJoinRoom(roomNumber, () -> {});
    }

    protected void openJoinRoomEntry(Runnable onDismiss) {
        routeBoxRoomEntry(() -> showJoinRoom(null, onDismiss));
    }

    private void showJoinRoom(String roomNumber, Runnable onDismiss) {
        if (currentHomeState == null) {
            return;
        }
        if (joinRoomFlow == null) {
            joinRoomFlow = new MainActivityJoinRoomFlow(this);
        }
        joinRoomFlow.show(roomNumber, onDismiss);
    }

    @Override
    protected void joinPendingRoomAfterHomeDisplay() {
        String roomNumber = pendingJoinRoomNumber;
        if (roomNumber == null || currentHomeState == null || realNameStatusStore == null) {
            return;
        }
        RealNameStatus.Status status =
                realNameStatusStore.statusFor(currentHomeState.player().userId());
        if (status == null) {
            return;
        }
        if (status != RealNameStatus.Status.VERIFIED) {
            showRealNameDialog(null, true);
            return;
        }
        pendingJoinRoomNumber = null;
        showJoinRoom(roomNumber);
    }

    private void showMatchArena() {
        if (currentHomeState == null) {
            return;
        }
        if (matchArenaFlow == null) {
            matchArenaFlow = new MainActivityMatchArenaFlow(this);
        }
        matchArenaFlow.show(currentHomeState.region().lobbyId());
    }

    protected void dismissRealNameDialog() {
        if (realNameDialog != null) {
            RealNameDialog dialog = realNameDialog;
            realNameDialog = null;
            dialog.setOnDismissListener(null);
            dialog.dismiss();
            applyImmersiveMode();
        }
    }

    private void loadStatus(
            AuthSessionCoordinator.Callback<RealNameStatus> callback) {
        if (realNameApiClient == null || authSessionCoordinator == null) {
            return;
        }
        authSessionCoordinator.execute(
                (accessToken, callCallback) ->
                        realNameApiClient.getStatus(
                                accessToken, forwarding(callCallback)),
                callback);
    }

    private void showRealNameDialog(
            RealNameStatus verifiedStatus, boolean serverAllowsAlipay) {
        if (isFinishing() || realNameDialog != null) {
            return;
        }
        boolean alipayEnabled =
                verifiedStatus == null
                        && serverAllowsAlipay
                        && alipayRealNameGateway != null
                        && alipayRealNameGateway.isAvailable();
        RealNameDialog dialog =
                new RealNameDialog(
                        this,
                        verifiedStatus,
                        alipayEnabled,
                        new RealNameDialog.Listener() {
                            @Override
                            public void onSubmitRequested(
                                    RealNameDialog source,
                                    String realName,
                                    String idCardNumber) {
                                submitRealNameVerification(
                                        source, realName, idCardNumber);
                            }

                            @Override
                            public void onAlipayRequested(
                                    RealNameDialog source) {
                                startAlipayRealName(source);
                            }
                        });
        realNameDialog = dialog;
        dialog.setOnDismissListener(
                dismissed -> {
                    realNameDialog = null;
                    applyImmersiveMode();
                });
        dialog.show();
    }

    private void submitRealNameVerification(
            RealNameDialog dialog,
            String realName,
            String idCardNumber) {
        if (dialog != realNameDialog) {
            return;
        }
        dialog.setSubmitting(true);
        authSessionCoordinator.execute(
                (accessToken, callCallback) ->
                        realNameApiClient.verify(
                                accessToken,
                                realName,
                                idCardNumber,
                                forwarding(callCallback)),
                verificationCallback(dialog));
    }

    private void startAlipayRealName(RealNameDialog dialog) {
        if (dialog != realNameDialog || alipayRealNameGateway == null) {
            return;
        }
        if (!alipayRealNameGateway.isAvailable()) {
            dialog.showError(
                    getString(R.string.real_name_alipay_unavailable));
            return;
        }
        alipayRealNameGateway.startAuth(
                this,
                new AlipayRealNameGateway.Callback() {
                    @Override
                    public void onAuthCode(String authCode) {
                        verifyAlipayRealName(dialog, authCode);
                    }

                    @Override
                    public void onUnavailable() {
                        if (dialog == realNameDialog) {
                            dialog.showError(
                                    getString(
                                            R.string
                                                    .real_name_alipay_unavailable));
                        }
                    }

                    @Override
                    public void onCancel() {
                        // Cancelling the authorization sheet is not an error.
                    }
                });
    }

    private void verifyAlipayRealName(
            RealNameDialog dialog, String authCode) {
        if (dialog != realNameDialog) {
            return;
        }
        dialog.setSubmitting(true);
        authSessionCoordinator.execute(
                (accessToken, callCallback) ->
                        realNameApiClient.verifyAlipay(
                                accessToken,
                                authCode,
                                forwarding(callCallback)),
                verificationCallback(dialog));
    }

    private AuthSessionCoordinator.Callback<RealNameStatus>
            verificationCallback(RealNameDialog dialog) {
        return new AuthSessionCoordinator.Callback<>() {
            @Override
            public void onSuccess(RealNameStatus status) {
                if (currentHomeState != null && realNameStatusStore != null) {
                    realNameStatusStore.save(
                            currentHomeState.player().userId(),
                            status.status());
                }
                if (dialog == realNameDialog) {
                    dismissRealNameDialog();
                }
                if (!isFinishing()) {
                    Toast.makeText(
                                    MainActivityRealNameFlow.this,
                                    R.string.real_name_success,
                                    Toast.LENGTH_LONG)
                            .show();
                }
                joinPendingRoomAfterHomeDisplay();
            }

            @Override
            public void onLoginRequired() {
                dismissRealNameDialog();
                requireLogin();
            }

            @Override
            public void onError(String message) {
                if (dialog == realNameDialog) {
                    dialog.setSubmitting(false);
                    dialog.showError(message);
                }
            }
        };
    }

    private static RealNameApiClient.Callback forwarding(
            AuthSessionCoordinator.CallCallback<RealNameStatus> callback) {
        return new RealNameApiClient.Callback() {
            @Override
            public void onSuccess(RealNameStatus status) {
                callback.onSuccess(status);
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

    private void requireLogin() {
        if (!isFinishing()) {
            showLoginPage();
        }
    }
}
