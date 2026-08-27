package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplayActionProtocol;
import com.nanbeiyule.game.gameplay.GameplayCommandResult;
import com.nanbeiyule.game.gameplay.GameplayEvent;
import com.nanbeiyule.game.gameplay.GameplayKongType;
import com.nanbeiyule.game.gameplay.GameplayReducer;
import com.nanbeiyule.game.gameplay.GameplaySeat;
import com.nanbeiyule.game.gameplay.GameplaySnapshot;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouMultipleState;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import org.json.JSONException;
import org.json.JSONObject;

/** Owns one authenticated gameplay session and its monotonic client projection. */
final class GameplaySessionCoordinator {
    interface Listener {
        void onState(GameplayTableState state);

        /** Called after recovered events are accepted by the projection. */
        default void onEvents(List<GameplayEvent> events) {}

        void onLoginRequired();

        void onError(String message);

        /**
         * 出牌命令没有落到服务端（未受理或提交失败）时回调，携带原 actionToken。
         *
         * <p>服务端此时仍然开着同一个出牌权，客户端必须把手牌交还给玩家，否则该 offer 的
         * actionToken 会永久作废、手牌再也点不动。
         */
        default void onPlayPermissionReleased(String actionToken) {}
    }

    private final AuthSessionCoordinator auth;
    private final GameplayTransport transport;
    private final Listener listener;
    private final Supplier<String> commandKeyFactory;
    private final GameplayEventPlaybackGate eventPlaybackGate;

    private long generation;
    private String roomNumber;
    private GameplayTableState state;
    private boolean closed = true;
    private boolean commandInFlight;
    /** 已提交、等待服务端确认的出牌 actionToken；失败时必须交还给界面。 */
    private String pendingDiscardToken;
    private final GameplayWaitingRoomAutopilot waitingRoomAutopilot =
            new GameplayWaitingRoomAutopilot();

    GameplaySessionCoordinator(
            AuthSessionCoordinator auth,
            Listener listener) {
        this(
                auth,
                new GameplayApiClient(),
                listener,
                () -> UUID.randomUUID().toString(), GameplayEventPlaybackGate.mainThreadScheduler());
    }

    GameplaySessionCoordinator(
            AuthSessionCoordinator auth,
            GameplayTransport transport,
            Listener listener,
            Supplier<String> commandKeyFactory) {
        this(auth, transport, listener, commandKeyFactory,
                GameplayEventPlaybackGate.immediateScheduler());
    }

    GameplaySessionCoordinator(
            AuthSessionCoordinator auth,
            GameplayTransport transport,
            Listener listener,
            Supplier<String> commandKeyFactory,
            GameplayEventPlaybackGate.Scheduler eventScheduler) {
        this.auth = Objects.requireNonNull(auth, "auth");
        this.transport = Objects.requireNonNull(transport, "transport");
        this.listener = Objects.requireNonNull(listener, "listener");
        this.commandKeyFactory = Objects.requireNonNull(commandKeyFactory, "commandKeyFactory");
        this.eventPlaybackGate = new GameplayEventPlaybackGate(eventScheduler);
    }

    void open(String nextRoomNumber) {
        begin(nextRoomNumber);
        requestSnapshot(true, generation, false);
    }

    void resume(String nextRoomNumber) {
        begin(nextRoomNumber);
        requestSnapshot(false, generation, false);
    }

    void refreshSnapshot() {
        if (!closed && state != null && !eventPlaybackGate.busy()) {
            requestSnapshot(false, generation, false);
        }
    }

    void setReady(boolean ready) {
        GameplayTableState current = state;
        if (closed || current == null || commandInFlight || localReady(current, ready)) {
            return;
        }
        submitCommand(current, ready ? "READY" : "UNREADY", null);
    }

    /**
     * @return true 当且仅当出牌命令已被受理并提交；false 表示命令没有发出，调用方必须释放该
     *     actionToken，否则手牌会卡死（服务端出牌权仍然开着）。
     */
    boolean submitDiscard(int tileValue, String actionToken) {
        GameplayTableState current = state;
        if (closed || current == null || commandInFlight
                || current.playPermission().isEmpty()
                || !current.playPermission().get().actionToken().equals(actionToken)) {
            pendingDiscardToken = null;
            listener.onPlayPermissionReleased(actionToken);
            return false;
        }
        pendingDiscardToken = actionToken;
        submitAction(current, GameplayActionProtocol.DISCARD,
                () -> GameplayActionProtocol.discardPayload(tileValue, actionToken));
        return true;
    }

    /** 出牌命令失败后交还出牌权，否则该 offer 的 actionToken 永久作废、手牌卡死。 */
    private void releasePendingDiscard() {
        String token = pendingDiscardToken;
        pendingDiscardToken = null;
        if (token != null) {
            listener.onPlayPermissionReleased(token);
        }
    }

    void submitChow(int tileValue, int candidateIndex, String actionToken) {
        GameplayMeldCommands.chow(this, tileValue, candidateIndex, actionToken);
    }

    void submitPung(int tileValue, String actionToken) {
        GameplayMeldCommands.pung(this, tileValue, actionToken);
    }

    void submitKong(int tileValue, GameplayKongType kongType, String actionToken) {
        GameplayMeldCommands.kong(this, tileValue, kongType, actionToken);
    }

    void submitHu(String actionToken) {
        GameplayMeldCommands.hu(this, actionToken);
    }

    void submitPass(String actionToken) {
        GameplayMeldCommands.pass(this, actionToken);
    }

    GameplayTableState currentState() {
        return state;
    }

    void submitMultipleChoice(TaizhouMultipleState.Choice choice) {
        GameplayTableState current = state;
        if (closed || current == null || commandInFlight || choice == null) {
            return;
        }
        submitAction(current, GameplayActionProtocol.MULTIPLE_CHOICE,
                () -> GameplayActionProtocol.multipleChoicePayload(choice));
    }

    /**
     * 原版转发族（msgClientForward XY_ID=1043）：表情/GPS/语音等互动，任何阶段可发，
     * 无需 actionToken；服务端以 CLIENT_FORWARD 事件广播给同房间所有人。
     */
    void submitForward(int cfId, String data) {
        GameplayTableState current = state;
        if (closed || current == null || commandInFlight) {
            return;
        }
        submitAction(current, GameplayActionProtocol.CLIENT_FORWARD,
                () -> GameplayActionProtocol.forwardPayload(cfId, data));
    }

    void submitEarlyStart() {
        GameplayTableState current = state;
        if (closed || current == null || commandInFlight) {
            return;
        }
        submitCommand(current, "EARLY_START", null);
    }

    /**
     * 原版托管：{@code RightBtns/Module.luac:16-18 sendStartTrust} 与
     * {@code Trust/Module.luac:99-101 doSendTrust} 都走同一条 {@code sendTrust(bTrust)}
     * （{@code msgTrust} XY_ID=517，双向），置位与取消只差布尔值。
     */
    void submitTrust(boolean trusted) {
        GameplayTableState current = state;
        if (closed || current == null || commandInFlight) {
            return;
        }
        submitAction(current, GameplayActionProtocol.TRUST,
                () -> GameplayActionProtocol.trustPayload(trusted));
    }

    /** {@code Dismiss/Module.luac:181-189 sendRequestDismiss}。 */
    void submitDismissRequest() {
        GameplayTableState current = state;
        if (closed || current == null || commandInFlight) {
            return;
        }
        submitCommand(current, GameplayActionProtocol.DISMISS_REQUEST, null);
    }

    /** {@code Dismiss/Module.luac:191-198 sendRespondDismiss}。 */
    void submitDismissRespond(boolean agree) {
        GameplayTableState current = state;
        if (closed || current == null || commandInFlight) {
            return;
        }
        submitAction(current, GameplayActionProtocol.DISMISS_RESPOND,
                () -> GameplayActionProtocol.dismissRespondPayload(agree));
    }

    /** {@code PlayerInfo/View.luac:645-659 onBtnKick} → {@code sendReqKickUser}。 */
    void submitKick(int seatNumber) {
        GameplayTableState current = state;
        if (closed || current == null || commandInFlight) {
            return;
        }
        submitAction(current, GameplayActionProtocol.KICK,
                () -> GameplayActionProtocol.kickPayload(seatNumber));
    }

    void submitNextRound() {
        GameplayTableState current = state;
        if (closed || current == null || commandInFlight) {
            return;
        }
        submitCommand(current, "NEXT_ROUND", null);
    }

    boolean hasMatchingOffer(GameplayTableState current, String actionToken) {
        return !closed && current != null && !commandInFlight && actionToken != null
                && current.actionOffer().isPresent()
                && current.actionOffer().get().actionToken().equals(actionToken);
    }

    void submitAction(GameplayTableState current, String type, PayloadBuilder builder) {
        JSONObject payload;
        try {
            payload = builder.build();
        } catch (JSONException | IllegalArgumentException exception) {
            error(generation, "牌局操作参数不正确");
            return;
        }
        submitCommand(current, type, payload);
    }

    interface PayloadBuilder {
        JSONObject build() throws JSONException;
    }

    private void submitCommand(GameplayTableState current, String type, JSONObject payload) {
        commandInFlight = true;
        long requestGeneration = generation;
        String commandKey = commandKeyFactory.get();
        auth.execute(
                (token, callback) ->
                        transport.submitCommand(
                                token,
                                roomNumber,
                                commandKey,
                                type,
                                current.revision(),
                                payload,
                                forwarding(callback)),
                new AuthSessionCoordinator.Callback<GameplayCommandResult>() {
                    @Override
                    public void onSuccess(GameplayCommandResult result) {
                        if (!isCurrent(requestGeneration)) {
                            return;
                        }
                        pendingDiscardToken = null;
                        applyCommandEvents(requestGeneration, result);
                    }

                    @Override
                    public void onLoginRequired() {
                        finishCommand(requestGeneration);
                        releasePendingDiscard();
                        loginRequired(requestGeneration);
                    }

                    @Override
                    public void onError(String message) {
                        finishCommand(requestGeneration);
                        releasePendingDiscard();
                        error(requestGeneration, message);
                    }
                });
    }

    /**
     * 命令受理后立即消费应答自带的权威事件，省掉一次 {@code GET /events} 往返。
     *
     * <p>事件仍然全部经过 {@link GameplayEventPlaybackGate}，只保留原版洗牌/骰子/发牌/结算动画
     * 等待；{@code GameplayReducer} 按 {@code (revision, eventOrder)} 判重，所以随后轮询到的同一批
     * 事件不会被二次应用。这不是乐观更新：客户端没有本地移牌，只是把服务端已经算好的权威事件立刻用上。
     * 旧版本后端不带事件时退回原来的补拉路径。
     */
    private void applyCommandEvents(long requestGeneration, GameplayCommandResult result) {
        if (result == null || result.events().isEmpty()) {
            recoverEvents(requestGeneration, true);
            return;
        }
        eventPlaybackGate.accept(this, requestGeneration, state, result.events(), true);
    }

    void recoverEvents() {
        if (!closed && state != null && !eventPlaybackGate.busy()) {
            recoverEvents(generation, false);
        }
    }

    void close() {
        generation++;
        closed = true;
        roomNumber = null;
        state = null;
        commandInFlight = false;
        pendingDiscardToken = null;
        waitingRoomAutopilot.reset();
        eventPlaybackGate.clear();
        transport.cancelPending();
    }

    void destroy() {
        close();
        transport.shutdown();
    }

    private void begin(String nextRoomNumber) {
        if (nextRoomNumber == null || !nextRoomNumber.matches("\\d{6}")) {
            throw new IllegalArgumentException("roomNumber must contain six digits");
        }
        transport.cancelPending();
        generation++;
        closed = false;
        roomNumber = nextRoomNumber;
        state = null;
        commandInFlight = false;
        waitingRoomAutopilot.reset();
        eventPlaybackGate.clear();
    }

    private void requestSnapshot(
            boolean create, long requestGeneration, boolean finishesCommand) {
        auth.execute(
                (token, callback) -> {
                    GameplayTransport.Callback<GameplaySnapshot> forwarded = forwarding(callback);
                    if (create) {
                        transport.open(token, roomNumber, forwarded);
                    } else {
                        transport.loadSnapshot(token, roomNumber, forwarded);
                    }
                },
                new AuthSessionCoordinator.Callback<GameplaySnapshot>() {
                    @Override
                    public void onSuccess(GameplaySnapshot snapshot) {
                        if (!isCurrent(requestGeneration)) {
                            return;
                        }
                        // 事件回放中的快照一律丢弃：快照只有最终态，应用它会把当前动画和牌局音频整段跳过，
                        // 随后门控刷新还会把画面倒回去。
                        // 这一拍由 MainActivityTaizhouMahjongFlow 的轮询自行续期，不会停摆。
                        if (!finishesCommand && eventPlaybackGate.busy()) {
                            return;
                        }
                        GameplayTableState next = GameplayReducer.fromSnapshot(snapshot);
                        long previousRevision = state == null ? Long.MIN_VALUE : state.revision();
                        boolean accepted = state == null || next.revision() >= state.revision();
                        if (accepted) {
                            state = next;
                        }
                        if (finishesCommand
                                || (commandInFlight && accepted && next.revision() > previousRevision)) {
                            commandInFlight = false;
                        }
                        if (accepted) {
                            listener.onState(state);
                        }
                        driveWaitingRoom();
                    }

                    @Override
                    public void onLoginRequired() {
                        if (finishesCommand) {
                            finishCommand(requestGeneration);
                        }
                        loginRequired(requestGeneration);
                    }

                    @Override
                    public void onError(String message) {
                        if (finishesCommand) {
                            finishCommand(requestGeneration);
                        }
                        error(requestGeneration, message);
                    }
                });
    }

    private void recoverEvents(long requestGeneration, boolean finishesCommand) {
        long afterRevision = state.revision();
        auth.execute(
                (token, callback) ->
                        transport.loadEvents(
                                token,
                                roomNumber,
                                afterRevision,
                                forwarding(callback)),
                new AuthSessionCoordinator.Callback<List<GameplayEvent>>() {
                    @Override
                    public void onSuccess(List<GameplayEvent> events) {
                        if (!isCurrent(requestGeneration)) {
                            return;
                        }
                        eventPlaybackGate.accept(
                                GameplaySessionCoordinator.this,
                                requestGeneration,
                                state,
                                events,
                                finishesCommand);
                    }

                    @Override
                    public void onLoginRequired() {
                        if (finishesCommand) {
                            finishCommand(requestGeneration);
                        }
                        loginRequired(requestGeneration);
                    }

                    @Override
                    public void onError(String message) {
                        if (finishesCommand) {
                            finishCommand(requestGeneration);
                        }
                        error(requestGeneration, message);
                    }
                });
    }

    boolean isRecoveryCurrent(long requestGeneration) {
        return isCurrent(requestGeneration);
    }

    void onRecoveredEventsAccepted(
            long requestGeneration,
            GameplayTableState nextState,
            List<GameplayEvent> events,
            boolean finishesCommand) {
        if (!isCurrent(requestGeneration)) {
            return;
        }
        state = nextState;
        if (finishesCommand) {
            commandInFlight = false;
        }
        listener.onState(state);
        listener.onEvents(events);
        driveWaitingRoom();
    }

    void onRecoveredEventsResyncRequired(long requestGeneration, boolean finishesCommand) {
        requestSnapshot(false, requestGeneration, finishesCommand);
    }

    private void loginRequired(long requestGeneration) {
        if (isCurrent(requestGeneration)) {
            listener.onLoginRequired();
        }
    }

    private void error(long requestGeneration, String message) {
        if (isCurrent(requestGeneration)) {
            listener.onError(message);
        }
    }

    private boolean isCurrent(long requestGeneration) {
        return !closed && generation == requestGeneration;
    }

    private void finishCommand(long requestGeneration) {
        if (isCurrent(requestGeneration)) {
            commandInFlight = false;
        }
    }

    private void driveWaitingRoom() {
        GameplayTableState current = state;
        if (closed || current == null || commandInFlight) {
            return;
        }
        String command = waitingRoomAutopilot.nextCommand(current);
        if (command != null) {
            submitCommand(current, command, null);
        }
    }

    private static boolean localReady(GameplayTableState state, boolean expectedReady) {
        for (GameplaySeat seat : state.seats()) {
            if (seat.seatNumber() == state.mySeat()) {
                return seat.ready() == expectedReady;
            }
        }
        return true;
    }

    private static <T> GameplayTransport.Callback<T> forwarding(
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
}
