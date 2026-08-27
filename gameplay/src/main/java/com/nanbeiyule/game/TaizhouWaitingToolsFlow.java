package com.nanbeiyule.game;

import android.app.Dialog;
import android.os.SystemClock;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.nanbeiyule.game.gameplay.GameplaySeat;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouMahjongWaitingProjection;

/** Room-scoped lifecycle for settings, reservations, chat and hold-to-talk. */
final class TaizhouWaitingToolsFlow {
    private static final long REPEATED_ERROR_WINDOW_MILLIS = 5_000L;

    private final MainActivityGameHomeDisplayFlow owner;
    private final Runnable leaveTable;
    private final Runnable loginRequired;
    private final Runnable requestTrust;
    private Runnable requestDismissVote = () -> {};
    private final TaizhouVoiceFlow voiceFlow;
    private TaizhouMahjongPreferencesStore preferencesStore;
    private TaizhouSettingStyleStore styleStore;
    private TaizhouQuickPhraseVoicePlayer quickPhraseVoicePlayer;
    private final TaizhouIncomingVoiceTracker incomingVoiceTracker =
            new TaizhouIncomingVoiceTracker();
    private TaizhouRoomToolsCoordinator coordinator;
    private CreateRoomApiClient roomApiClient;
    private TaizhouMahjongTableView tableView;
    private FrameLayout tableContainer;
    private GameplayTableState gameplayState;
    private TaizhouRoomToolsState toolsState;
    private Dialog dialog;
    private String roomNumber;
    private boolean toolsOpened;
    private String lastError;
    private long lastErrorAt;

    TaizhouWaitingToolsFlow(
            MainActivityGameHomeDisplayFlow owner,
            Runnable leaveTable,
            Runnable loginRequired,
            Runnable requestTrust) {
        this.owner = owner;
        this.leaveTable = leaveTable;
        this.loginRequired = loginRequired;
        this.requestTrust = requestTrust;
        voiceFlow = new TaizhouVoiceFlow(owner);
    }

    /**
     * 已开过局的包厢，房主退出要走 {@code Dismiss/Module.luac:181-189 sendRequestDismiss}
     * 的投票流；未开局时按 {@code onMsgDismissFlag}(:117-125) 由房主直接回大厅。
     */
    void setRequestDismissVote(Runnable action) {
        requestDismissVote = action == null ? () -> {} : action;
    }

    void open(
            String roomNumber,
            TaizhouMahjongTableView tableView,
            FrameLayout tableContainer) {
        close();
        if (preferencesStore == null) {
            preferencesStore = new TaizhouMahjongPreferencesStore(owner);
        }
        if (styleStore == null) {
            styleStore = new TaizhouSettingStyleStore(owner);
        }
        this.roomNumber = roomNumber;
        this.tableView = tableView;
        this.tableContainer = tableContainer;
        tableView.applyPreferences(preferencesStore.load());
        tableView.applyStyle(styleStore.load());
        incomingVoiceTracker.reset();
        roomApiClient = new CreateRoomApiClient();
        coordinator =
                new TaizhouRoomToolsCoordinator(
                        owner.authSessionCoordinator,
                        new TaizhouRoomToolsCoordinator.Listener() {
                            @Override
                            public void onState(TaizhouRoomToolsState state) {
                                toolsState = state;
                                if (TaizhouWaitingToolsFlow.this.tableView != null) {
                                    TaizhouWaitingToolsFlow.this.tableView.setRoomToolsState(state);
                                }
                                TaizhouIncomingVoiceTracker.Incoming incoming =
                                        incomingVoiceTracker.accept(
                                                state.messages(), localUserId());
                                for (String messageId : incoming.voiceMessageIds()) {
                                    voiceFlow.play(messageId);
                                }
                                playQuickPhraseVoices(incoming.quickPhrases());
                            }

                            @Override
                            public void onLoginRequired() {
                                loginRequired.run();
                            }

                            @Override
                            public void onError(String message) {
                                showErrorOnce(message);
                            }
                        });
        voiceFlow.open(tableContainer, coordinator);
    }

    void onGameplayState(GameplayTableState state) {
        gameplayState = state;
        if (coordinator == null) return;
        if (!toolsOpened) {
            toolsOpened = true;
            coordinator.open(roomNumber);
        } else {
            coordinator.refreshState();
        }
    }

    void showTableSettings() {
        GameplayTableState current = gameplayState;
        if (current == null) return;
        boolean host = localHost(current);
        boolean goldRoom = TaizhouMahjongWaitingProjection.isGoldRoom(current);
        boolean canDismissAsHost = host && !goldRoom;
        TaizhouSettingDialog next =
                new TaizhouSettingDialog(
                        owner,
                        owner.personalCenterSystemSettings,
                        preferencesStore.load(),
                        styleStore.load(),
                        goldRoom,
                        new TaizhouSettingDialog.Actions() {
                            @Override
                            public void onSettingsChanged(PersonalCenterSystemSettings settings) {
                                owner.personalCenterSystemSettings = settings;
                                if (owner.personalCenterSettingsStore != null) {
                                    owner.personalCenterSettingsStore.save(settings);
                                }
                                if (owner.originalLobbyAudioController != null) {
                                    owner.originalLobbyAudioController.applySettings(settings);
                                }
                            }

                            @Override
                            public void onPreferencesChanged(
                                    TaizhouMahjongPreferences preferences) {
                                preferencesStore.save(preferences);
                                if (tableView != null) {
                                    tableView.applyPreferences(preferences);
                                }
                            }

                            @Override
                            public void onStyleChanged(TaizhouSettingStyle style) {
                                styleStore.save(style);
                                if (tableView != null) {
                                    tableView.applyStyle(style);
                                }
                            }

                            @Override
                            public void onExitRequested() {
                                if (!canDismissAsHost) {
                                    leaveRoom();
                                } else if (hasPlayedRound()) {
                                    requestDismissVote.run();
                                } else {
                                    dissolveRoom();
                                }
                            }

                            @Override
                            public void onTrustRequested() {
                                requestTrust.run();
                            }
                        });
        // View.lua:onBtnClose 关面板时重新下发已保存的配置，保证牌桌与存档一致。
        showDialog(
                next,
                () -> {
                    if (tableView != null) {
                        tableView.applyStyle(styleStore.load());
                    }
                });
    }

    void showReservation(TaizhouRoomToolType type) {
        TaizhouRoomToolsState current = toolsState;
        if (current == null || coordinator == null) {
            toast("正在同步" + (type == TaizhouRoomToolType.CHANGE_CARD ? "换牌" : "洗牌") + "信息");
            if (coordinator != null) coordinator.refreshState();
            return;
        }
        TaizhouPropReservationDialog next =
                new TaizhouPropReservationDialog(
                        owner,
                        current.tool(type),
                        current.isReserved(type),
                        active -> coordinator.setReservation(type, active));
        showDialog(next);
    }

    void showChat() {
        TaizhouRoomToolsState current = toolsState;
        if (current == null || coordinator == null) {
            toast("正在同步聊天记录");
            if (coordinator != null) coordinator.refreshState();
            return;
        }
        int mySeat = gameplayState == null ? 0 : gameplayState.mySeat();
        TaizhouChatDialog next =
                new TaizhouChatDialog(
                        owner,
                        current,
                        mySeat,
                        new TaizhouChatDialog.Actions() {
                            @Override
                            public void onQuickPhrase(int index) {
                                coordinator.sendMessage("QUICK_PHRASE", index);
                            }

                            @Override
                            public void onEmoji(int index) {
                                coordinator.sendMessage("EMOJI", index);
                            }

                            @Override
                            public void onVoiceMessage(String messageId) {
                                voiceFlow.play(messageId);
                            }
                        });
        showDialog(next);
    }

    void onVoiceGesture(TaizhouMahjongVoiceGesture.Result result) {
        voiceFlow.onGesture(result);
    }

    void onRecordAudioPermissionResult(boolean granted) {
        voiceFlow.onPermissionResult(granted);
    }

    void close() {
        toolsOpened = false;
        gameplayState = null;
        toolsState = null;
        roomNumber = null;
        tableView = null;
        tableContainer = null;
        incomingVoiceTracker.reset();
        if (quickPhraseVoicePlayer != null) {
            quickPhraseVoicePlayer.release();
            quickPhraseVoicePlayer = null;
        }
        dismissDialog();
        voiceFlow.close();
        if (coordinator != null) {
            coordinator.destroy();
            coordinator = null;
        }
        if (roomApiClient != null) {
            roomApiClient.shutdown();
            roomApiClient = null;
        }
    }

    /** {@code roomData:getPlayCount() > 0}：开过局才需要投票解散。 */
    private boolean hasPlayedRound() {
        GameplayTableState current = gameplayState;
        return current != null && current.roundNumber() > 0;
    }

    private void dissolveRoom() {
        if (roomApiClient == null || owner.authSessionCoordinator == null || roomNumber == null) return;
        String targetRoom = roomNumber;
        owner.authSessionCoordinator.execute(
                (token, callback) ->
                        roomApiClient.dissolve(token, targetRoom, roomCallback(callback)),
                new AuthSessionCoordinator.Callback<CreateRoomResult>() {
                    @Override
                    public void onSuccess(CreateRoomResult ignored) {
                        leaveTable.run();
                    }

                    @Override
                    public void onLoginRequired() {
                        loginRequired.run();
                    }

                    @Override
                    public void onError(String message) {
                        toast(message);
                    }
                });
    }

    private void leaveRoom() {
        if (roomApiClient == null || owner.authSessionCoordinator == null || roomNumber == null) return;
        String targetRoom = roomNumber;
        owner.authSessionCoordinator.execute(
                (token, callback) ->
                        roomApiClient.leave(token, targetRoom, roomCallback(callback)),
                new AuthSessionCoordinator.Callback<RoomPlacement>() {
                    @Override
                    public void onSuccess(RoomPlacement ignored) {
                        leaveTable.run();
                    }

                    @Override
                    public void onLoginRequired() {
                        loginRequired.run();
                    }

                    @Override
                    public void onError(String message) {
                        toast(message);
                    }
                });
    }


    /**
     * 播放新到的俏皮话配音。
     *
     * <p>当前大厅与牌局 API 不下发说话人性别，因此按设置面板中的男声/女声选项选择
     * Man/Women 音频目录；音量与语音开关同样使用设置面板的持久化值。
     */
    private void playQuickPhraseVoices(
            java.util.List<TaizhouIncomingVoiceTracker.QuickPhrase> phrases) {
        if (phrases.isEmpty()) {
            return;
        }
        if (quickPhraseVoicePlayer == null) {
            quickPhraseVoicePlayer = new TaizhouQuickPhraseVoicePlayer(owner.getAssets());
        }
        boolean dialect = preferencesStore != null && preferencesStore.load().dialectEnabled();
        PersonalCenterSystemSettings settings = owner.personalCenterSystemSettings;
        boolean maleVoice = settings == null || settings.maleVoice();
        float voiceVolume =
                settings == null
                        ? 0.5f
                        : settings.voiceEnabled() ? settings.voiceVolume() / 100f : 0f;
        for (TaizhouIncomingVoiceTracker.QuickPhrase phrase : phrases) {
            quickPhraseVoicePlayer.play(
                    phrase.contentIndex(), maleVoice, dialect, voiceVolume);
        }
    }

    private void showDialog(Dialog next) {
        showDialog(next, null);
    }

    private void showDialog(Dialog next, Runnable onDismissed) {
        dismissDialog();
        dialog = next;
        next.setOnDismissListener(
                ignored -> {
                    if (dialog == next) dialog = null;
                    if (onDismissed != null) {
                        onDismissed.run();
                    }
                    owner.applyImmersiveMode();
                });
        next.show();
    }

    private void dismissDialog() {
        Dialog current = dialog;
        dialog = null;
        if (current != null && current.isShowing()) current.dismiss();
    }

    private void showErrorOnce(String message) {
        long now = SystemClock.elapsedRealtime();
        if (!String.valueOf(message).equals(lastError)
                || now - lastErrorAt >= REPEATED_ERROR_WINDOW_MILLIS) {
            lastError = message;
            lastErrorAt = now;
            toast(message);
        }
    }

    private void toast(String message) {
        if (!owner.isFinishing()) Toast.makeText(owner, message, Toast.LENGTH_SHORT).show();
    }

    private static boolean localHost(GameplayTableState state) {
        for (GameplaySeat seat : state.seats()) {
            if (seat.seatNumber() == state.mySeat()) return seat.host();
        }
        return false;
    }

    private String localUserId() {
        GameplayTableState current = gameplayState;
        if (current == null) return "";
        for (GameplaySeat seat : current.seats()) {
            if (seat.seatNumber() == current.mySeat()) return seat.userId();
        }
        return "";
    }

    private static <T> CreateRoomApiClient.ResponseCallback<T> roomCallback(
            AuthSessionCoordinator.CallCallback<T> callback) {
        return new CreateRoomApiClient.ResponseCallback<>() {
            @Override public void onSuccess(T value) { callback.onSuccess(value); }
            @Override public void onUnauthorized() { callback.onUnauthorized(); }
            @Override public void onError(
                    String message, CreateRoomApiClient.FailureKind ignored) {
                callback.onError(message);
            }
        };
    }
}
