package com.nanbeiyule.game;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.nanbeiyule.game.gameplay.GameplayEvent;
import com.nanbeiyule.game.gameplay.GameplayKongType;
import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.gameplay.GameplaySeat;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouMahjongWaitingProjection;
import com.nanbeiyule.game.mahjong.TaizhouMultipleState;
import com.nanbeiyule.game.mahjong.protocol.ForwardMessages;
import com.nanbeiyule.game.wechat.WechatLoginManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Owns the Activity lifecycle and authenticated session for the native 30109 table. */
final class MainActivityTaizhouMahjongFlow {
    private static final long SNAPSHOT_REFRESH_INTERVAL_MILLIS = 1_000L;

    private final MainActivityGameHomeDisplayFlow owner;
    private final TaizhouWaitingToolsFlow roomToolsCoordinator;
    private final TaizhouTrustCoordinator trustCoordinator;
    private final TaizhouDismissCoordinator dismissCoordinator;
    private final TaizhouPlayerInfoFlow playerInfoFlow;
    private final TaizhouFortuneFlow fortuneApiClient;
    private GameplaySessionCoordinator coordinator;
    private TaizhouMahjongTableView activeTableView;
    private FrameLayout activeTableContainer;
    private GameplayTableState latestState;
    private TaizhouEarlyStartDialog earlyStartDialog;
    private TaizhouMahjongSoundPlayer roundSoundPlayer;
    private final Set<String> avatarRequests = new HashSet<>();
    private final TaizhouMahjongLoadPolicy loadPolicy = new TaizhouMahjongLoadPolicy(false);
    private final Handler snapshotRefreshHandler = new Handler(Looper.getMainLooper());
    /** 轮询只补别人的动作与惰性时钟；协调器会跳过回放中的那一拍，所以这里自行续期。 */
    private final Runnable snapshotRefresh =
            () -> {
                if (coordinator != null) {
                    coordinator.refreshSnapshot();
                    scheduleSnapshotRefresh();
                }
            };
    MainActivityTaizhouMahjongFlow(MainActivityGameHomeDisplayFlow owner) {
        this.owner = owner;
        fortuneApiClient = new TaizhouFortuneFlow(owner, this::requireLogin);
        roomToolsCoordinator =
                new TaizhouWaitingToolsFlow(
                        owner, this::leaveTable, this::requireLogin, this::requestTrust);
        trustCoordinator = new TaizhouTrustCoordinator(owner, this::cancelTrust);
        dismissCoordinator = new TaizhouDismissCoordinator(owner, this::leaveTable);
        dismissCoordinator.setRespondListener(this::respondDismiss);
        roomToolsCoordinator.setRequestDismissVote(this::requestDismiss);
        playerInfoFlow = new TaizhouPlayerInfoFlow(owner, owner::showMembershipCenter);
        playerInfoFlow.setKickListener(this::kickSeat);
    }

    void open(String roomNumber) {
        if (owner.isFinishing() || owner.authSessionCoordinator == null) {
            return;
        }
        close();
        coordinator = AuthenticatedCocosSessionLauncher.openIfRequested(owner, roomNumber);
        if (coordinator != null) return;
        if (owner.loginRequestLoadingController != null) {
            owner.loginRequestLoadingController.bind(null);
        }
        if (owner.originalLobbyAudioController != null) {
            owner.originalLobbyAudioController.setLobbyActive(false);
        }
        boolean showFullLoader = loadPolicy.shouldShowFullLoader();
        TaizhouMahjongLoadingView loadingView =
                showFullLoader ? new TaizhouMahjongLoadingView(owner) : null;
        TaizhouMahjongTableView tableView = new TaizhouMahjongTableView(owner, "台州麻将");
        FrameLayout tableContainer = new FrameLayout(owner);
        tableContainer.addView(
                tableView,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        activeTableView = tableView;
        activeTableContainer = tableContainer;
        tableView.setOnReadyRequestedListener(
                () -> {
                    GameplaySessionCoordinator active = coordinator;
                    if (active != null) {
                        active.setReady(true);
                    }
                });
        tableView.setOnInviteRequestedListener(this::shareInvitation);
        tableView.setOnCopyRequestedListener(this::copyInvitation);
        tableView.setOnEarlyStartRequestedListener(this::showEarlyStartDialog);
        tableView.setOnPlayerHeadTappedListener(
                seatNumber -> playerInfoFlow.show(latestState, seatNumber));
        tableView.setOnChromeActionRequestedListener(this::handleChromeAction);
        tableView.setOnVoiceGestureListener(roomToolsCoordinator::onVoiceGesture);
        tableView.setOnRoundActionRequestedListener(
                new TaizhouTableRoundActionListener() {
                    @Override
                    public void onSettleActionRequested(
                            TaizhouSettleInteraction.Action action) {
                        if (action == TaizhouSettleInteraction.Action.NEXT_ROUND) {
                            requestNextRound();
                        } else if (action == TaizhouSettleInteraction.Action.SHUFFLE) {
                            roomToolsCoordinator.showReservation(TaizhouRoomToolType.SHUFFLE);
                        }
                    }

                    @Override
                    public void onTotalResultActionRequested(
                            TaizhouTotalResultInteraction.Action action) {
                        if (action == TaizhouTotalResultInteraction.Action.BACK_LOBBY) {
                            leaveTable();
                        } else if (action == TaizhouTotalResultInteraction.Action.SHARE) {
                            shareTotalResult();
                        }
                    }

                    @Override
                    public void onMultipleChoiceRequested(
                            TaizhouMultipleState.Choice choice) {
                        GameplaySessionCoordinator active = coordinator;
                        if (active != null) {
                            active.submitMultipleChoice(choice);
                        }
                    }
                });
        tableView.setOnPlayRequestedListener(
                (originalHandIndex, tileValue, actionToken) -> {
                    GameplaySessionCoordinator active = coordinator;
                    if (active != null) {
                        active.submitDiscard(tileValue, actionToken);
                    }
                });
        tableView.setOnActionRequestedListener(
                new TaizhouActionCommandRouter(() -> coordinator));
        roomToolsCoordinator.open(roomNumber, tableView, tableContainer);
        fortuneApiClient.open();
        loadRecordAccess(tableView);
        if (showFullLoader) {
            loadingView.setStatusText("正在加载台州麻将...");
            loadingView.setOnProgressCompleteListener(
                    () -> {
                        if (owner.isFinishing() || owner.loadingView != loadingView) return;
                        loadPolicy.markTableEntered();
                        owner.loadingView = tableView;
                        owner.setContentView(tableContainer);
                        owner.applyImmersiveMode();
                    });
            owner.loadingView = loadingView;
            owner.setContentView(loadingView);
            owner.applyImmersiveMode();
            loadingView.setTargetProgress(0.0f);
        } else {
            owner.loadingView = tableView;
            owner.setContentView(tableContainer);
            owner.applyImmersiveMode();
        }

        TaizhouMahjongSoundPlayer soundPlayer = new TaizhouMahjongSoundPlayer(owner);
        PersonalCenterSystemSettings audioSettings = owner.personalCenterSystemSettings;
        soundPlayer.applySettings(audioSettings);
        TaizhouRoundAudioBridge audioBridge =
                new TaizhouRoundAudioBridge(
                        soundPlayer::playAll,
                        audioSettings == null || audioSettings.maleVoice());
        roundSoundPlayer = soundPlayer;
        coordinator =
                new GameplaySessionCoordinator(
                        owner.authSessionCoordinator,
                        new GameplaySessionCoordinator.Listener() {
                            @Override
                            public void onState(GameplayTableState state) {
                                latestState = state;
                                if (state.phase() != GameplayPhase.WAITING) {
                                    owner.hideTableFriendDrawer();
                                }
                                tableView.render(state);
                                roomToolsCoordinator.onGameplayState(state);
                                loadSeatAvatars(tableView);
                                if (showFullLoader && loadingView != null) {
                                    loadingView.setStatusText("正在进入牌局...");
                                    loadingView.setTargetProgress(100.0f);
                                }
                                scheduleSnapshotRefresh();
                            }

                            @Override
                            public void onEvents(List<GameplayEvent> events) {
                                audioBridge.onEvents(events);
                                trustCoordinator.onEvents(events, latestState);
                                dismissCoordinator.onEvents(events, latestState);
                                showForwardNotices(events);
                            }

                            @Override
                            public void onPlayPermissionReleased(String actionToken) {
                                tableView.releasePlayPermission(actionToken);
                            }

                            @Override
                            public void onLoginRequired() {
                                close();
                                owner.showLoginPage();
                            }

                            @Override
                            public void onError(String message) {
                                if (owner.loadingView == loadingView) {
                                    loadingView.setStatusText(message);
                                } else {
                                    Toast.makeText(owner, message, Toast.LENGTH_SHORT).show();
                                }
                                scheduleSnapshotRefresh();
                            }
                        });
        coordinator.open(roomNumber);
    }

    boolean handleBack() {
        if (!(owner.loadingView instanceof TaizhouMahjongLoadingView)
                && !(owner.loadingView instanceof TaizhouMahjongTableView)) {
            return false;
        }
        close();
        if (owner.currentHomeState != null) {
            owner.displayGameHome(owner.currentHomeState);
        } else {
            owner.loadGameHome();
        }
        return true;
    }

    void close() {
        snapshotRefreshHandler.removeCallbacks(snapshotRefresh);
        owner.hideTableFriendDrawer();
        dismissEarlyStartDialog();
        activeTableView = null;
        activeTableContainer = null;
        latestState = null;
        avatarRequests.clear();
        roomToolsCoordinator.close();
        trustCoordinator.close();
        dismissCoordinator.close();
        playerInfoFlow.close();
        fortuneApiClient.close();
        if (roundSoundPlayer != null) {
            roundSoundPlayer.stopAll();
            roundSoundPlayer.release();
            roundSoundPlayer = null;
        }
        if (coordinator != null) {
            coordinator.destroy();
            coordinator = null;
        }
    }

    void onRecordAudioPermissionResult(boolean granted) {
        roomToolsCoordinator.onRecordAudioPermissionResult(granted);
    }

    private void scheduleSnapshotRefresh() {
        snapshotRefreshHandler.removeCallbacks(snapshotRefresh);
        snapshotRefreshHandler.postDelayed(
                snapshotRefresh, SNAPSHOT_REFRESH_INTERVAL_MILLIS);
    }

    private void loadSeatAvatars(TaizhouMahjongTableView tableView) {
        for (String avatarKey : tableView.missingAvatarKeys()) {
            if (!avatarRequests.add(avatarKey)) {
                continue;
            }
            owner.loadAvatarBitmap(
                    avatarKey,
                    bitmap -> {
                        if (activeTableView == tableView) {
                            tableView.setAvatarBitmap(avatarKey, bitmap);
                        }
                    });
        }
    }

    private void loadRecordAccess(TaizhouMahjongTableView tableView) {
        if (owner.membershipApiClient == null || owner.authSessionCoordinator == null) {
            return;
        }
        owner.authSessionCoordinator.execute(
                (accessToken, callback) ->
                        owner.membershipApiClient.loadStatus(
                                accessToken, MembershipCallbackAdapter.from(callback)),
                new AuthSessionCoordinator.Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean membershipActive) {
                        if (activeTableView == tableView) {
                            tableView.setRecordAccessGranted(membershipActive);
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        if (activeTableView == tableView) {
                            close();
                            owner.showLoginPage();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        // Keep the badge hidden until the authority can be checked.
                    }
                });
    }

    private void shareInvitation() {
        GameplayTableState state = latestState;
        if (state == null) {
            return;
        }
        TaizhouMahjongShareInfo share = TaizhouMahjongShareInfo.from(state);
        WechatLoginManager.StartResult result =
                owner.wechatLoginManager == null
                        ? WechatLoginManager.StartResult.REJECTED
                        : owner.wechatLoginManager.shareWebpage(
                                share.title(), share.description(), share.webpageUrl());
        if (result != WechatLoginManager.StartResult.STARTED) {
            copyInvitation();
        }
    }

    private void shareTotalResult() {
        // 微信能力属于我们自己的独立身份边界，图片不上传浙江游戏大厅服务。
        TaizhouTotalResultShareFlow.share(owner, activeTableView);
    }

    private void copyInvitation() {
        GameplayTableState state = latestState;
        if (state == null) {
            return;
        }
        String text =
                TaizhouMahjongShareInfo.copyText(
                        state, owner.getString(R.string.app_name));
        ClipboardManager clipboard =
                (ClipboardManager) owner.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            return;
        }
        clipboard.setPrimaryClip(ClipData.newPlainText("房间邀请", text));
        Toast.makeText(
                        owner,
                        "邀请信息已复制成功，粘贴分享给您的好友吧！",
                        Toast.LENGTH_SHORT)
                .show();
    }

    private void handleChromeAction(TaizhouMahjongWaitingProjection.Action action) {
        if (action == null) {
            return;
        }
        switch (action) {
            case FRIENDS -> owner.showTableFriendDrawer(activeTableContainer);
            case LUCKY_MISSION -> owner.showDailyMission();
            case RULE -> showRules();
            case RECORD -> owner.showInformationDialog("战绩", "暂无牌局战绩");
            case MENU -> showTableSettings();
            case CHANGE_CARD -> showReservation(TaizhouRoomToolType.CHANGE_CARD);
            case SHUFFLE -> showReservation(TaizhouRoomToolType.SHUFFLE);
            case FORTUNE -> showFortune();
            case CHAT -> showChat();
            case TREASURE_POT -> showTreasurePot();
            case CAISHEN -> showCaishen();
            case TRUST -> requestTrust();
            case VOICE -> {
                // Hold-to-talk is dispatched by TaizhouMahjongVoiceGesture.
            }
            case NONE, INVITE, READY, COPY -> {
                // Center actions are dispatched by their dedicated listeners.
            }
        }
    }

    /**
     * {@code RightBtns/Module.luac:3-14 doStartTrust}：非进行中（{@code psPlaying}）时
     * 只弹「等待中不能托管！」，不发消息；进行中才 {@code sendTrust(true)}。
     * 服务端回执把 {@code MahjongTrustState} 置位后才弹托管层，与原版
     * {@code Trust/Module.luac:19-38 onMsgTrust} 的顺序一致。
     */
    private void requestTrust() {
        GameplayTableState state = latestState;
        if (state == null) {
            return;
        }
        if (state.phase() == GameplayPhase.WAITING) {
            Toast.makeText(owner, "等待中不能托管！", Toast.LENGTH_SHORT).show();
            return;
        }
        GameplaySessionCoordinator active = coordinator;
        if (active != null) {
            active.submitTrust(true);
        }
    }

    /** {@code PlayerInfo/View.luac:645-659}：房主在未开局的包厢里请出一位玩家。 */
    private void kickSeat(int seatNumber) {
        GameplaySessionCoordinator active = coordinator;
        if (active != null) {
            active.submitKick(seatNumber);
        }
    }

    /** {@code Dismiss/Module.luac:181-189}：开过局的包厢由房主发起解散投票。 */
    private void requestDismiss() {
        GameplaySessionCoordinator active = coordinator;
        if (active != null) {
            active.submitDismissRequest();
        }
    }

    /** {@code Dismiss/View.luac:196-206}：同意/拒绝各发一次 {@code sendRespondDismiss}。 */
    private void respondDismiss(boolean agree) {
        GameplaySessionCoordinator active = coordinator;
        if (active != null) {
            active.submitDismissRespond(agree);
        }
    }

    /** {@code Trust/View.luac:44-46}：点托管层任意处发 {@code sendTrust(false)} 取消。 */
    private void cancelTrust() {
        GameplaySessionCoordinator active = coordinator;
        if (active != null) {
            active.submitTrust(false);
        }
    }

    private void showRules() {
        GameplayTableState state = latestState;
        if (state != null) {
            owner.showInformationDialog("规则", state.gameRuleDisplay());
        }
    }

    private void showEarlyStartDialog() {
        GameplayTableState state = latestState;
        if (state == null) {
            return;
        }
        dismissEarlyStartDialog();
        TaizhouEarlyStartDialog next =
                new TaizhouEarlyStartDialog(
                        owner,
                        localDisplayName(state),
                        () -> {
                            GameplaySessionCoordinator active = coordinator;
                            if (active != null) {
                                active.submitEarlyStart();
                            }
                        });
        earlyStartDialog = next;
        next.setOnDismissListener(
                ignored -> {
                    if (earlyStartDialog == next) {
                        earlyStartDialog = null;
                    }
                    owner.applyImmersiveMode();
                });
        next.show();
    }

    private void requestNextRound() {
        if (!TaizhouSettleInteraction.canRequestNextRound(latestState)) {
            Toast.makeText(owner, "对局已结束", Toast.LENGTH_SHORT).show();
            return;
        }
        GameplaySessionCoordinator active = coordinator;
        if (active != null) {
            active.submitNextRound();
        }
    }

    private void dismissEarlyStartDialog() {
        android.app.Dialog current = earlyStartDialog;
        earlyStartDialog = null;
        if (current != null && current.isShowing()) {
            current.dismiss();
        }
    }

    private static String localDisplayName(GameplayTableState state) {
        for (GameplaySeat seat : state.seats()) {
            if (seat.seatNumber() == state.mySeat()) {
                return seat.displayName();
            }
        }
        return "";
    }

    /**
     * 原版转发族呈现（msgClientForward XY_ID=1043）。表情/头部特效类无动画资产前用
     * Toast 过渡；GPS/信号/测速类原版也不做强制呈现，静默忽略。
     */
    private void showForwardNotices(List<GameplayEvent> events) {
        GameplayTableState state = latestState;
        for (GameplayEvent event : events) {
            if (!"CLIENT_FORWARD".equals(event.type())) {
                continue;
            }
            org.json.JSONObject payload = event.payload();
            int cfId = payload.optInt("cfId", 0);
            if (cfId != ForwardMessages.MsgClientForward.CF_EXPRESSION
                    && cfId != ForwardMessages.MsgClientForward.CF_FACE_ANI
                    && cfId != ForwardMessages.MsgClientForward.CF_PLAYER_HEAD_EFFECT
                    && cfId != ForwardMessages.MsgClientForward.CF_PROP_ANI) {
                continue;
            }
            Toast.makeText(
                            owner,
                            forwardSeatName(state, payload.optInt("seatNumber", 0)) + " 发送了表情",
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private static String forwardSeatName(GameplayTableState state, int seatNumber) {
        if (state != null) {
            for (GameplaySeat seat : state.seats()) {
                if (seat.seatNumber() == seatNumber && !seat.displayName().isEmpty()) {
                    return seat.displayName();
                }
            }
        }
        return seatNumber + "号位";
    }

    private void showTableSettings() {
        roomToolsCoordinator.showTableSettings();
    }

    private void showReservation(TaizhouRoomToolType type) {
        roomToolsCoordinator.showReservation(type);
    }

    private void showChat() {
        roomToolsCoordinator.showChat();
    }

    private void showFortune() {
        fortuneApiClient.showFortune();
    }

    private void showTreasurePot() {
        fortuneApiClient.showTreasurePot();
    }

    private void showCaishen() {
        fortuneApiClient.showCaishen();
    }

    private void leaveTable() {
        close();
        if (owner.currentHomeState != null) owner.displayGameHome(owner.currentHomeState);
        else owner.loadGameHome();
    }

    private void requireLogin() {
        close();
        owner.showLoginPage();
    }
}
