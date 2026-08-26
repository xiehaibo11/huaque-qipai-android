package com.nanbeiyule.game;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import com.nanbeiyule.game.goldroom.GoldHallActEntry;
import com.nanbeiyule.game.goldroom.GoldHallActEntryGroup;
import com.nanbeiyule.game.goldroom.GoldRoomConf;
import com.nanbeiyule.game.goldroom.GoldRoomEvidenceCatalog;
import com.nanbeiyule.game.goldroom.GoldRoomLevel;
import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.gameplay.GameplaySnapshot;
import java.util.UUID;

/**
 * Authenticated owner for the original gold-room choose-room page.
 *
 * <p>Mirrors {@code GoldNew/Module.lua showChooseRoomPre → joinGoldRoomFirst}: the real-name gate
 * is already applied by the caller, the catalog is fetched, and only a game with more than one
 * level opens the page. Entering a level calls the gold join endpoint: ordinary responses show the
 * original MatchUI-shaped waiting layer; local/QA auto-round responses stay on that original
 * matching state briefly, then open the returned Taizhou test room.
 */
final class MainActivityGoldChooseRoomFlow {
    private static final long AUTO_GAMEPLAY_MATCH_DELAY_MILLIS = 1_200L;
    /** 匹配轮询间隔：对齐原版客户端 1 秒 join 防抖节奏，避免过密轮询压服务端。 */
    private static final long MATCH_STATUS_POLL_MILLIS = 1_500L;

    private final MainActivityRealNameFlow owner;
    private final GoldRoomApiClient apiClient = new GoldRoomApiClient();
    private final GameplayApiClient gameplayApiClient = new GameplayApiClient();
    private final MainActivityGoldRuleFlow ruleFlow;
    private final TimeLoginActApiClient timeLoginApiClient = new TimeLoginActApiClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private GoldChooseRoomDialog dialog;
    private String activeGameDisplayName = "";
    private GoldMatchTableView activeGoldMatchView;
    private Runnable pendingAutoGameplayOpen;
    private Runnable pendingMatchStatusPoll;
    private long requestGeneration;
    private long activeLobbyId;
    private long activeGameId;
    private long activeRuleGameId;
    private int activeRoomNameFlag;
    private boolean showingGoldMatch;

    MainActivityGoldChooseRoomFlow(MainActivityRealNameFlow owner) {
        this.owner = owner;
        this.ruleFlow = new MainActivityGoldRuleFlow(owner, this::dismiss);
    }

    /** 标题右侧「?」，原版 {@code GoldNew/View.lua onClickGameRule}。 */
    private void showRule() {
        ruleFlow.show(activeRuleGameId, activeGameDisplayName);
    }

    void show(long gameId) {
        if (owner.isFinishing()
                || dialog != null
                || owner.currentHomeState == null
                || owner.authSessionCoordinator == null) {
            return;
        }
        cancelPendingAutoGameplayOpen();
        cancelPendingMatchStatusPoll();
        activeGoldMatchView = null;
        long lobbyId = owner.currentHomeState.region().lobbyId();
        activeLobbyId = lobbyId;
        activeGameId = gameId;
        GoldRoomConf evidenceConf = GoldRoomEvidenceCatalog.confOrNull(lobbyId, gameId);
        activeRuleGameId = evidenceConf == null ? gameId : ruleGameId(evidenceConf);
        showingGoldMatch = false;
        GoldChooseRoomDialog source =
                new GoldChooseRoomDialog(
                        owner,
                        this::onLevelSelected,
                        this::handleChooseRoomBack,
                        this::onActEntrySelected,
                        this::showRule);
        dialog = source;
        long generation = ++requestGeneration;
        source.setOnDismissListener(
                ignored -> {
                    requestGeneration++;
                    if (dialog == source) {
                        dialog = null;
                    }
                    owner.applyImmersiveMode();
                });
        source.show();
        // 顶栏钱包用已鉴权的大厅状态，不另外造数。
        source.setWallet(owner.currentHomeState.wallet());
        loadConf(source, generation, lobbyId, gameId);
        loadTimeLoginEntry(source, generation);
    }

    private void handleChooseRoomBack() {
        dismiss();
        if (owner instanceof MainActivity
                && ((MainActivity) owner).isDirectGoldEntry()) {
            owner.finish();
        }
    }

    void dismiss() {
        requestGeneration++;
        ruleFlow.dismiss();
        cancelPendingAutoGameplayOpen();
        cancelPendingMatchStatusPoll();
        activeGoldMatchView = null;
        if (dialog != null) {
            GoldChooseRoomDialog source = dialog;
            dialog = null;
            source.setOnDismissListener(null);
            source.dismiss();
            owner.applyImmersiveMode();
        }
    }

    void shutdown() {
        requestGeneration++;
        cancelPendingAutoGameplayOpen();
        showingGoldMatch = false;
        activeGoldMatchView = null;
        apiClient.shutdown();
        gameplayApiClient.shutdown();
        ruleFlow.shutdown();
    }

    boolean handleBack() {
        if (ruleFlow.handleBack()) {
            return true;
        }
        if (dialog != null) {
            dismiss();
            return true;
        }
        if (showingGoldMatch && owner.currentHomeState != null) {
            cancelPendingAutoGameplayOpen();
            cancelPendingMatchStatusPoll();
            leaveGoldMatch();
            showingGoldMatch = false;
            activeGoldMatchView = null;
            owner.displayGameHome(owner.currentHomeState);
            return true;
        }
        return false;
    }

    private void loadConf(
            GoldChooseRoomDialog source, long generation, long lobbyId, long gameId) {
        owner.authSessionCoordinator.execute(
                (accessToken, callback) ->
                        apiClient.loadConf(accessToken, lobbyId, gameId, forwarding(callback)),
                new AuthSessionCoordinator.Callback<GoldRoomConf>() {
                    @Override
                    public void onSuccess(GoldRoomConf conf) {
                        if (stale(generation)) {
                            return;
                        }
                        if (conf.levels().isEmpty()) {
                            // 原版 joinGoldRoomFirst 在 roomLevelInfos 为空时弹同样的提示并中止。
                            showEvidenceCatalogOrFail(
                                    source, lobbyId, gameId, "获取房间信息出错 - " + gameId);
                            return;
                        }
                        activeGameDisplayName = conf.displayName();
                        activeRuleGameId = ruleGameId(conf);
                        source.setConf(conf);
                    }

                    @Override
                    public void onLoginRequired() {
                        if (stale(generation)) {
                            return;
                        }
                        dismiss();
                        owner.showLoginPage();
                    }

                    @Override
                    public void onError(String message) {
                        if (stale(generation)) {
                            return;
                        }
                        showEvidenceCatalogOrFail(source, lobbyId, gameId, message);
                    }
                });
    }

    private void showEvidenceCatalogOrFail(
            GoldChooseRoomDialog source, long lobbyId, long gameId, String message) {
        GoldRoomConf evidenceConf = GoldRoomEvidenceCatalog.confOrNull(lobbyId, gameId);
        if (evidenceConf != null && !evidenceConf.levels().isEmpty()) {
            activeGameDisplayName = evidenceConf.displayName();
            activeRuleGameId = ruleGameId(evidenceConf);
            source.setConf(evidenceConf);
            return;
        }
        fail(source, message);
    }

    static long ruleGameId(GoldRoomConf conf) {
        return conf.boxGameId() > 0L ? conf.boxGameId() : conf.gameId();
    }

    private static <T> GoldRoomApiClient.ResponseCallback<T> forwarding(
            AuthSessionCoordinator.CallCallback<T> callback) {
        return new GoldRoomApiClient.ResponseCallback<>() {
            @Override
            public void onSuccess(T value) {
                callback.onSuccess(value);
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

    private void fail(GoldChooseRoomDialog source, String message) {
        source.setStatusText(message);
        Toast.makeText(owner, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 右上活动入口的点击路由，对应原版各按钮 Lua 的 {@code onClick}。
     *
     * <p>「福利任务」原版打开 {@code LuckyMissionView}，南北娱乐已按同一份 CSB/Lua 证据实现了
     * 原生每日任务页，这里直接复用。其余入口的活动详情、档位与领取都依赖尚未实现的活动与
     * 支付服务端，接入前只如实提示，不弹空壳面板、不伪造礼包内容。
     */
    private void onActEntrySelected(GoldHallActEntry entry) {
        if (entry == GoldHallActEntry.LUCKY_MISSION) {
            owner.showDailyMission();
            return;
        }
        if (entry == GoldHallActEntry.ACT_TIME_LOGIN) {
            owner.showTimeLoginAct();
            return;
        }
        Toast.makeText(owner, entry.label() + "暂未开放", Toast.LENGTH_SHORT).show();
    }

    /**
     * 定时登录入口的显隐与红点，对应原版 {@code TimeLoginActBtn.lua:19-22,38-54}：
     * 按钮监听 {@code FLUSH_TIMELOGIN_ACT_INFO}，用 {@code isValid()} 决定显隐、
     * 用 {@code checkShowRedPoint()} 决定红点，两者都来自同一次活动状态请求。
     *
     * <p>门控判据是应答里的活动字段而不是 HTTP 状态码：原版活动关闭时服务端照常应答、只把
     * {@code _aid} 置 0，{@code isValid()} 判的就是 {@code _aid ~= 0}。南北娱乐后端同样在活动
     * 未开启时返回成功应答并置 {@code active=false}，入口保持隐藏，组仍是三个恒显入口。
     */
    private void loadTimeLoginEntry(GoldChooseRoomDialog source, long generation) {
        if (owner.authSessionCoordinator == null) {
            return;
        }
        owner.authSessionCoordinator.execute(
                (accessToken, callback) ->
                        timeLoginApiClient.loadState(
                                accessToken,
                                new TimeLoginActApiClient.ResponseCallback<TimeLoginActState>() {
                                    @Override
                                    public void onSuccess(TimeLoginActState result) {
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
                                }),
                new AuthSessionCoordinator.Callback<TimeLoginActState>() {
                    @Override
                    public void onSuccess(TimeLoginActState state) {
                        if (generation != requestGeneration || dialog != source) {
                            return;
                        }
                        java.util.Set<GoldHallActEntry> active =
                                state.isValid()
                                        ? java.util.Set.of(GoldHallActEntry.ACT_TIME_LOGIN)
                                        : java.util.Set.<GoldHallActEntry>of();
                        source.setActEntries(
                                GoldHallActEntryGroup.of(active),
                                state.isValid() && state.showsRedPoint()
                                        ? java.util.Set.of(GoldHallActEntry.ACT_TIME_LOGIN)
                                        : java.util.Set.of());
                    }

                    @Override
                    public void onLoginRequired() {
                        // 会话失效由其他链路统一处理，这里只保持入口隐藏。
                    }

                    @Override
                    public void onError(String message) {
                        // 活动未开启或暂时不可用时保持三个恒显入口，不提示、不占位。
                    }
                });
    }

    private void onLevelSelected(GoldRoomLevel level) {
        GoldChooseRoomDialog source = dialog;
        if (source == null || owner.currentHomeState == null || level == null) {
            return;
        }
        long generation = ++requestGeneration;
        activeRoomNameFlag = level.roomNameFlag();
        final long requestLobbyId = activeLobbyId;
        final long requestGameId = activeGameId;
        final int requestRoomNameFlag = level.roomNameFlag();
        String idempotencyKey = "gold-join-" + UUID.randomUUID();
        source.setStatusText("正在匹配玩家...");
        owner.authSessionCoordinator.execute(
                (accessToken, callback) ->
                        apiClient.join(
                                accessToken,
                                activeLobbyId,
                                activeGameId,
                                level.roomNameFlag(),
                                idempotencyKey,
                                forwarding(callback)),
                new AuthSessionCoordinator.Callback<GoldRoomJoinResponse>() {
                    @Override
                    public void onSuccess(GoldRoomJoinResponse response) {
                        if (stale(generation)) {
                            // 请求已被新选择/返回取代：撤掉它在服务端的占位（原版 PlayerLeaveRequest），
                            // 否则旧占位会用 GOLD_QUEUING 拦死后续 join；失败静默。
                            leaveGoldMatch(
                                    requestLobbyId, requestGameId, requestRoomNameFlag);
                            return;
                        }
                        dismiss();
                        if (response.roomNumber() != null) {
                            showGoldMatching(response);
                            if (response.autoGameplay()) {
                                scheduleAutoGameplayOpen(response.roomNumber());
                            } else {
                                scheduleMatchStatusPoll(response.roomNumber());
                            }
                        } else {
                            showGoldMatching(response);
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        if (stale(generation)) {
                            return;
                        }
                        dismiss();
                        owner.showLoginPage();
                    }

                    @Override
                    public void onError(String message) {
                        if (stale(generation)) {
                            // 响应丢失/超时时占位可能已落库，同样尽力撤位，避免旧占位残留。
                            leaveGoldMatch(
                                    requestLobbyId, requestGameId, requestRoomNameFlag);
                            return;
                        }
                        fail(source, message);
                    }
                });
    }

    private void showGoldMatching(GoldRoomJoinResponse response) {
        if (owner.isFinishing() || owner.currentHomeState == null) {
            return;
        }
        GoldMatchTableView tableView =
                new GoldMatchTableView(
                        owner,
                        owner.currentHomeState.player(),
                        owner.currentHomeState.wallet(),
                        response,
                        owner.currentAvatarBitmap);
        showingGoldMatch = true;
        activeGoldMatchView = tableView;
        owner.setContentView(tableView);
        owner.applyImmersiveMode();
        loadGoldMatchAvatar(tableView);
    }

    /**
     * 匹配等待页返回时的服务端撤位，对应原版 PlayerLeaveRequest：原版 Lua 客户端从未接
     * 线、只做本地 clearMatchUI，但我们的四人真满员模型里残留占位会永久卡住一间房，所以
     * 返回时尽力发出 leave，失败静默（服务端回归/重开房逻辑兑底）。
     */
    private void leaveGoldMatch() {
        leaveGoldMatch(activeLobbyId, activeGameId, activeRoomNameFlag);
    }

    private void leaveGoldMatch(long lobbyId, long gameId, int roomNameFlag) {
        if (lobbyId <= 0 || gameId <= 0 || roomNameFlag <= 0) {
            return;
        }
        owner.authSessionCoordinator.execute(
                (accessToken, callback) -> {
                    apiClient.leave(accessToken, lobbyId, gameId, roomNameFlag, forwarding(callback));
                },
                new AuthSessionCoordinator.Callback<Void>() {
                    @Override
                    public void onSuccess(Void result) {}

                    @Override
                    public void onLoginRequired() {}

                    @Override
                    public void onError(String message) {}
                });
    }

    private void scheduleAutoGameplayOpen(String roomNumber) {
        cancelPendingMatchStatusPoll();
        cancelPendingAutoGameplayOpen();
        long generation = ++requestGeneration;
        pendingAutoGameplayOpen =
                () -> {
                    if (owner.isFinishing()
                            || generation != requestGeneration
                            || !showingGoldMatch) {
                        return;
                    }
                    pendingAutoGameplayOpen = null;
                    showingGoldMatch = false;
                    activeGoldMatchView = null;
                    owner.openTaizhouMahjongSession(roomNumber);
                };
        mainHandler.postDelayed(pendingAutoGameplayOpen, AUTO_GAMEPLAY_MATCH_DELAY_MILLIS);
    }

    private void scheduleMatchStatusPoll(String roomNumber) {
        cancelPendingMatchStatusPoll();
        long generation = requestGeneration;
        pendingMatchStatusPoll =
                () -> {
                    pendingMatchStatusPoll = null;
                    if (owner.isFinishing()
                            || generation != requestGeneration
                            || !showingGoldMatch) {
                        return;
                    }
                    owner.authSessionCoordinator.execute(
                            (token, callback) ->
                                    gameplayApiClient.loadSnapshot(
                                            token, roomNumber, gameplayForwarding(callback)),
                            new AuthSessionCoordinator.Callback<GameplaySnapshot>() {
                                @Override
                                public void onSuccess(GameplaySnapshot snapshot) {
                                    if (generation != requestGeneration || !showingGoldMatch) {
                                        return;
                                    }
                                    if (snapshot.phase() == GameplayPhase.DISSOLVED) {
                                        showingGoldMatch = false;
                                        activeGoldMatchView = null;
                                        owner.displayGameHome(owner.currentHomeState);
                                        Toast.makeText(owner, "匹配已取消", Toast.LENGTH_SHORT).show();
                                    } else if (roundReady(snapshot.phase())) {
                                        scheduleAutoGameplayOpen(roomNumber);
                                    } else {
                                        scheduleMatchStatusPoll(roomNumber);
                                    }
                                }

                                @Override
                                public void onLoginRequired() {
                                    if (generation != requestGeneration) {
                                        return;
                                    }
                                    showingGoldMatch = false;
                                    activeGoldMatchView = null;
                                    owner.showLoginPage();
                                }

                                @Override
                                public void onError(String message) {
                                    if (generation == requestGeneration && showingGoldMatch) {
                                        scheduleMatchStatusPoll(roomNumber);
                                    }
                                }
                            });
                };
        mainHandler.postDelayed(pendingMatchStatusPoll, MATCH_STATUS_POLL_MILLIS);
    }

    static boolean roundReady(GameplayPhase phase) {
        return phase == GameplayPhase.DEALING
                || phase == GameplayPhase.PLAYING
                || phase == GameplayPhase.ROUND_RESULT
                || phase == GameplayPhase.COMPLETED;
    }

    private static <T> GameplayTransport.Callback<T> gameplayForwarding(
            AuthSessionCoordinator.CallCallback<T> callback) {
        return new GameplayTransport.Callback<>() {
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

    private void loadGoldMatchAvatar(GoldMatchTableView tableView) {
        if (owner.currentHomeState == null) {
            return;
        }
        String avatarKey = owner.currentHomeState.player().avatarKey();
        owner.loadAvatarBitmap(
                avatarKey,
                bitmap -> {
                    if (activeGoldMatchView != tableView || owner.isFinishing()) {
                        return;
                    }
                    owner.currentAvatarBitmap = bitmap;
                    tableView.setAvatarBitmap(bitmap);
                });
    }

    private void cancelPendingAutoGameplayOpen() {
        if (pendingAutoGameplayOpen == null) {
            return;
        }
        mainHandler.removeCallbacks(pendingAutoGameplayOpen);
        pendingAutoGameplayOpen = null;
    }

    private void cancelPendingMatchStatusPoll() {
        if (pendingMatchStatusPoll != null) {
            mainHandler.removeCallbacks(pendingMatchStatusPoll);
            pendingMatchStatusPoll = null;
        }
        gameplayApiClient.cancelPending();
    }

    private boolean stale(long generation) {
        return owner.isFinishing() || generation != requestGeneration;
    }
}
