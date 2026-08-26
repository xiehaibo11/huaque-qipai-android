package com.nanbeiyule.game;

import android.app.AlertDialog;
import android.widget.Toast;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Composed native flow owned by MainActivity's real-name layer. */
final class MainActivityCreateRoomFlow {
    private final MainActivityRealNameFlow owner;
    private final CreateRoomApiClient apiClient = new CreateRoomApiClient();
    private final CreateRoomSelectionStore selectionStore;
    private CreateRoomDialog dialog;
    private long requestGeneration;
    private boolean serverCatalogVerified;
    private boolean serverRuleVerified;
    private boolean enteringGameplay;

    MainActivityCreateRoomFlow(MainActivityRealNameFlow owner) {
        this.owner = owner;
        selectionStore = new CreateRoomSelectionStore(owner);
    }

    void show(long lobbyId) {
        show(lobbyId, 0L);
    }

    void show(long lobbyId, long initialGameId) {
        show(lobbyId, initialGameId, null);
    }

    void show(long lobbyId, long initialGameId, Runnable onUserDismissed) {
        if (owner.isFinishing()
                || dialog != null
                || lobbyId <= 0
                || owner.authSessionCoordinator == null) {
            return;
        }
        serverCatalogVerified = false;
        serverRuleVerified = false;
        enteringGameplay = false;
        CreateRoomDialog source =
                new CreateRoomDialog(
                        owner,
                        new CreateRoomView.Actions() {
                            @Override
                            public void onGameSelected(CreateRoomGame game) {
                                loadRule(game);
                            }

                            @Override
                            public void onExternalGameRequested() {
                                openShiSanZhangMiniProgram();
                            }

                            @Override
                            public void onSelectionChanged(CreateRoomState state) {
                                if (state != null) {
                                    selectionStore.save(state);
                                }
                            }

                            @Override
                            public void onCreateRequested(CreateRoomState state) {
                                create(state);
                            }

                            @Override
                            public void onFeedbackRequested() {
                                Toast.makeText(owner, "反馈入口暂未接入", Toast.LENGTH_SHORT).show();
                            }
                        });
        dialog = source;
        if (owner.originalLobbyAudioController != null) {
            source.setButtonClickSound(owner.originalLobbyAudioController::playButtonClick);
        }
        source.setOnDismissListener(
                ignored -> {
                    requestGeneration++;
                    apiClient.cancelPending();
                    dialog = null;
                    owner.applyImmersiveMode();
                    if (CreateRoomEntryPolicy.shouldReturnDirectEntryToLauncher(enteringGameplay)
                            && onUserDismissed != null) {
                        onUserDismissed.run();
                    }
                });
        source.show();
        loadGames(source, lobbyId, initialGameId);
    }

    void dismiss() {
        requestGeneration++;
        apiClient.cancelPending();
        if (dialog != null) {
            CreateRoomDialog source = dialog;
            dialog = null;
            source.setOnDismissListener(null);
            source.dismiss();
            owner.applyImmersiveMode();
        }
    }

    void destroy() {
        dismiss();
        apiClient.shutdown();
    }

    private void loadGames(CreateRoomDialog source, long lobbyId, long initialGameId) {
        long generation = ++requestGeneration;
        source.setLoading(true);
        owner.authSessionCoordinator.execute(
                (accessToken, callback) ->
                        apiClient.loadGames(accessToken, lobbyId, forwarding(callback)),
                new AuthSessionCoordinator.Callback<List<CreateRoomGame>>() {
                    @Override
                    public void onSuccess(List<CreateRoomGame> games) {
                        showGamesOrFallback(
                                source,
                                lobbyId,
                                initialGameId,
                                generation,
                                games,
                                true,
                                "当前地区暂无可创建的游戏");
                    }

                    @Override public void onLoginRequired() { loginIfCurrent(source, generation); }
                    @Override public void onError(String message) {
                        showGamesOrFallback(
                                source,
                                lobbyId,
                                initialGameId,
                                generation,
                                List.of(),
                                false,
                                message);
                    }
                });
    }

    private void loadRule(CreateRoomGame game) {
        CreateRoomDialog source = dialog;
        if (source == null
                || owner.currentHomeState == null
                || !CreateRoomEntryPolicy.shouldLoadRuleConfig(game.gameId())) {
            return;
        }
        long generation = ++requestGeneration;
        apiClient.cancelPending();
        serverRuleVerified = false;
        source.setLoading(true);
        loadRule(source, owner.currentHomeState.region().lobbyId(), game, generation);
    }

    private void loadRule(
            CreateRoomDialog source,
            long lobbyId,
            CreateRoomGame game,
            long generation) {
        owner.authSessionCoordinator.execute(
                (accessToken, callback) ->
                        apiClient.loadRuleConfig(
                                accessToken,
                                lobbyId,
                                game.gameId(),
                                forwarding(callback)),
                new AuthSessionCoordinator.Callback<CreateRoomRuleConfig>() {
                    @Override
                    public void onSuccess(CreateRoomRuleConfig config) {
                        showRule(source, lobbyId, game, generation, config, true);
                    }

                    @Override public void onLoginRequired() { loginIfCurrent(source, generation); }
                    @Override public void onError(String message) {
                        if (!isCurrent(source, generation)) {
                            return;
                        }
                        CreateRoomRuleConfig fallback =
                                CreateRoomEvidenceCatalog.ruleConfigOrNull(
                                        owner, lobbyId, game.gameId());
                        if (fallback != null) {
                            showRule(source, lobbyId, game, generation, fallback, false);
                        } else {
                            source.setError(message);
                        }
                    }
                });
    }

    private void showGamesOrFallback(
            CreateRoomDialog source,
            long lobbyId,
            long initialGameId,
            long generation,
            List<CreateRoomGame> games,
            boolean serverResponseVerified,
            String emptyMessage) {
        if (!isCurrent(source, generation)) {
            return;
        }
        serverCatalogVerified = serverResponseVerified && games != null && !games.isEmpty();
        serverRuleVerified = false;
        List<CreateRoomGame> sourceGames =
                games == null || games.isEmpty()
                        ? CreateRoomEvidenceCatalog.gamesOrEmpty(lobbyId)
                        : games;
        List<CreateRoomGame> available = CreateRoomEntryPolicy.gamesForLobby(lobbyId, sourceGames);
        if (available.isEmpty()) {
            source.setError(emptyMessage);
            return;
        }
        source.setGames(available, initialGameId);
        int selectedIndex = CreateRoomView.initialGameIndex(available, initialGameId);
        if (selectedIndex < 0) {
            source.setError(emptyMessage);
            return;
        }
        loadRule(source, lobbyId, available.get(selectedIndex), generation);
    }

    private void showRule(
            CreateRoomDialog source,
            long lobbyId,
            CreateRoomGame game,
            long generation,
            CreateRoomRuleConfig config,
            boolean ruleVerified) {
        if (!isCurrent(source, generation)) {
            return;
        }
        Map<Integer, List<String>> cached = new LinkedHashMap<>();
        for (int category = 1; category <= config.categoryCount(); category++) {
            int currentCategory = category;
            selectionStore
                    .load(game.gameId(), category, config.version())
                    .ifPresent(value -> cached.put(currentCategory, value));
        }
        serverRuleVerified = ruleVerified;
        CreateRoomState state =
                CreateRoomState.restore(
                        lobbyId,
                        game.gameId(),
                        config,
                        config.defaultCategoryIndex(),
                        cached,
                        serverCatalogVerified,
                        serverRuleVerified);
        source.setState(state);
        if (!CreateRoomEntryPolicy.supportsRoomCreation(game.gameId())) {
            source.setError(CreateRoomEntryPolicy.ROOM_CREATION_UNAVAILABLE_MESSAGE);
        } else if (!state.isCreateReady()) {
            source.setError("规则配置尚未通过服务器校验，仅供预览");
        }
    }

    private void create(CreateRoomState state) {
        CreateRoomDialog source = dialog;
        if (source == null || state == null) {
            return;
        }
        if (!state.isCreateReady()) {
            source.setError("规则配置尚未通过服务器校验，暂不能创建房间");
            return;
        }
        long generation = ++requestGeneration;
        String attemptKey = state.createAttemptKey();
        source.setLoading(true);
        selectionStore.save(state);
        owner.authSessionCoordinator.execute(
                (accessToken, callback) ->
                        apiClient.create(
                                accessToken,
                                state,
                                attemptKey,
                                forwardingCreate(callback, state)),
                new AuthSessionCoordinator.Callback<CreateRoomResult>() {
                    @Override public void onSuccess(CreateRoomResult result) {
                        if (isCurrent(source, generation)) {
                            state.rotateCreateAttemptKey();
                            if (result.gameId() == 30109L) {
                                enteringGameplay = true;
                                dismiss();
                                owner.openTaizhouMahjongSession(result.roomNumber());
                            } else if (result.gameId() == 30588L) {
                                enteringGameplay = true;
                                dismiss();
                                owner.openWuLongSession(result.roomNumber());
                            } else {
                                source.setResult(result);
                            }
                        }
                    }

                    @Override public void onLoginRequired() { loginIfCurrent(source, generation); }
                    @Override public void onError(String message) {
                        if (isCurrent(source, generation)) {
                            source.setError(message);
                        }
                    }
                });
    }

    private void loginIfCurrent(CreateRoomDialog source, long generation) {
        if (isCurrent(source, generation)) {
            owner.showLoginPage();
        }
    }

    private boolean isCurrent(CreateRoomDialog source, long generation) {
        return !owner.isFinishing() && dialog == source && requestGeneration == generation;
    }

    private static <T> CreateRoomApiClient.ResponseCallback<T> forwarding(
            AuthSessionCoordinator.CallCallback<T> callback) {
        return new CreateRoomApiClient.ResponseCallback<>() {
            @Override public void onSuccess(T value) { callback.onSuccess(value); }
            @Override public void onUnauthorized() { callback.onUnauthorized(); }
            @Override public void onError(
                    String message, CreateRoomApiClient.FailureKind failureKind) {
                callback.onError(message);
            }
        };
    }

    private <T> CreateRoomApiClient.ResponseCallback<T> forwardingCreate(
            AuthSessionCoordinator.CallCallback<T> callback, CreateRoomState state) {
        return new CreateRoomApiClient.ResponseCallback<>() {
            @Override public void onSuccess(T value) { callback.onSuccess(value); }
            @Override public void onUnauthorized() { callback.onUnauthorized(); }
            @Override public void onError(
                    String message, CreateRoomApiClient.FailureKind failureKind) {
                if (failureKind == CreateRoomApiClient.FailureKind.DEFINITIVE) {
                    state.rotateCreateAttemptKey();
                }
                callback.onError(message);
            }

            @Override
            public void onAlreadyInRoom(RoomPlacement placement, String message) {
                // 原版从不把玩家留在建房页：ERROR_INAPPID 走「点击确认返场」。
                state.rotateCreateAttemptKey();
                reenter(placement, RoomReentry.Origin.CREATE_CONFLICT);
            }
        };
    }

    /**
     * 原版 {@code PlayerData:flushPlayerPosition()} 的等价查询：在房间里就返场，不在才放行。
     *
     * <p>查不到位置时按原版的「不在房间」默认执行 {@code whenNotInRoom}；真正的冲突仍由服务端
     * {@code ROOM_ALREADY_OPEN} 兜底，所以放行不会造成重复建房。
     */
    void routeByPlacement(Runnable whenNotInRoom) {
        loadPlacement(
                placement -> {
                    if (placement.hasRoom()) {
                        reenter(placement, RoomReentry.Origin.LOBBY_ENTRY);
                    } else {
                        whenNotInRoom.run();
                    }
                });
    }

    /**
     * 查询玩家当前位置。查询失败按原版的「不在房间」处理，调用方据此保持建房态。
     *
     * <p>{@code onLoginRequired} 交给统一登录门禁，不在这里吞掉。
     */
    void loadPlacement(java.util.function.Consumer<RoomPlacement> onPlacement) {
        owner.authSessionCoordinator.execute(
                (accessToken, callback) ->
                        apiClient.loadCurrentRoom(accessToken, forwarding(callback)),
                new AuthSessionCoordinator.Callback<RoomPlacement>() {
                    @Override public void onSuccess(RoomPlacement placement) {
                        if (!owner.isFinishing()) {
                            onPlacement.accept(placement);
                        }
                    }

                    @Override public void onLoginRequired() { owner.showLoginPage(); }
                    @Override public void onError(String message) {
                        if (!owner.isFinishing()) {
                            onPlacement.accept(RoomPlacement.none());
                        }
                    }
                });
    }

    /** 大厅第二状态直接返场；只有 {@code ERROR_INAPPID} 建房冲突才要求确认。 */
    private void reenter(RoomPlacement placement, RoomReentry.Origin origin) {
        if (owner.isFinishing()) {
            return;
        }
        RoomReentry.Action action = RoomReentry.actionFor(placement, origin);
        if (action == RoomReentry.Action.SHOW_UNAVAILABLE) {
            // 没有牌桌可返时如实说明，不伪造进桌，也不让入口静默失败。
            String message = RoomReentry.unavailableMessage(placement);
            CreateRoomDialog source = dialog;
            if (source != null) {
                source.setLoading(false);
                source.setError(message);
            } else {
                Toast.makeText(owner, message, Toast.LENGTH_LONG).show();
            }
            return;
        }
        if (action == RoomReentry.Action.OPEN_DIRECTLY) {
            dismiss();
            openPlacement(placement);
            return;
        }
        new AlertDialog.Builder(owner)
                .setMessage(RoomReentry.CONFIRM_MESSAGE)
                .setCancelable(false)
                .setPositiveButton(
                        android.R.string.ok,
                        (unusedDialog, unusedButton) -> {
                            dismiss();
                            openPlacement(placement);
                        })
                .show();
    }

    private void openPlacement(RoomPlacement placement) {
        if (placement.gameId() == RoomReentry.WULONG_GAME_ID) owner.openWuLongSession(placement.roomNumber());
        else owner.openTaizhouMahjongSession(placement.roomNumber());
    }

    private void openShiSanZhangMiniProgram() {
        String playerId = owner.currentHomeState == null
                ? "" : String.valueOf(owner.currentHomeState.player().publicPlayerId());
        String error = ShiSanZhangMiniProgram.open(owner, playerId);
        if (error != null) {
            Toast.makeText(owner, error, Toast.LENGTH_LONG).show();
        }
    }
}
