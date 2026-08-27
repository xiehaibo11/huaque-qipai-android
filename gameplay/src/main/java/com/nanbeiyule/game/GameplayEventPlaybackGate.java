package com.nanbeiyule.game;

import android.os.Handler;
import android.os.Looper;
import com.nanbeiyule.game.gameplay.GameplayEvent;
import com.nanbeiyule.game.gameplay.GameplayReducer;
import com.nanbeiyule.game.gameplay.GameplayResyncRequiredException;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Plays recovered event batches at the table cadence required by original throw-chip waits. */
final class GameplayEventPlaybackGate {
    static final long THROW_CHIP_PLAYBACK_MILLIS = 1_200L;
    static final long TURN_ADVANCED_PLAYBACK_MILLIS = 1_200L;
    /**
     * 原版洗牌动画时长。{@code GameLayer/Module.luac:1168-1170} needDelay 对
     * {@code msgToTalShuffle} 返回 {@code (true, 3.75, false)}：洗牌协议自己立即执行
     * （动画立刻开始），后续协议在队列里等 3.75 秒。
     */
    static final long SHUFFLE_PLAYBACK_MILLIS = 3_750L;
    /**
     * 原版首摸步延迟。{@code GameLayer/Module.luac:1164-1167} needDelay 对
     * {@code nStepID == GAME_STEP_TAKE_FIRST(6)} 的 {@code msgGameStep} 返回
     * {@code (true, 1, true)}：这条协议自身等 1 秒才执行，给骰子动画收尾后的定格缓冲；
     * 本工程对应 {@code DEALT}（起手发牌快照，即 TAKE_FIRST 步骤在客户端的落地）。
     */
    static final long TAKE_FIRST_PLAYBACK_MILLIS = 1_000L;
    /**
     * 原版结算窗延迟。{@code BasicTaiZhouMahjong/WinLost/Module.luac:35-42} onMsgResult
     * 先 {@code setDelayTime(1)} 再 {@code performWithDelayGlobal(showSettleWindow, 1)}：
     * 结算窗延迟 1 秒弹出，给胡牌/最后一个副露动画让路；本工程对应
     * {@code ROUND_RESULT_READY}（应用后 phase 变 ROUND_RESULT、结算页上屏）。
     * 近似：原版摊牌数据立即处理、仅弹窗延迟，这里整个事件含摊牌一起延 1s——
     * 观感一致（摊牌与弹窗几乎同时出现），实现不拆事件。原版随后的破产礼包
     * 检查（time+1）是周边功能，无对应实现。
     */
    static final long SETTLE_WINDOW_PLAYBACK_MILLIS = 1_000L;
    interface Scheduler {
        void postDelayed(Runnable task, long delayMillis);
    }

    interface Callback {
        boolean isCurrent(long generation);

        void onAccepted(
                GameplayTableState nextState, List<GameplayEvent> events, boolean finishesCommand);

        void onResyncRequired(boolean finishesCommand);
    }

    private final Scheduler scheduler;
    private Pending pending;

    GameplayEventPlaybackGate(Scheduler scheduler) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    }

    static Scheduler immediateScheduler() {
        return (task, ignored) -> task.run();
    }

    static Scheduler mainThreadScheduler() {
        Handler handler = new Handler(Looper.getMainLooper());
        return (task, delayMillis) -> handler.postDelayed(task, delayMillis);
    }

    boolean busy() {
        return pending != null;
    }

    void clear() {
        pending = null;
    }

    void accept(
            GameplaySessionCoordinator coordinator,
            long generation,
            GameplayTableState current,
            List<GameplayEvent> events,
            boolean finishesCommand) {
        accept(
                generation,
                current,
                events,
                finishesCommand,
                new Callback() {
                    @Override
                    public boolean isCurrent(long generation) {
                        return coordinator.isRecoveryCurrent(generation);
                    }

                    @Override
                    public void onAccepted(
                            GameplayTableState nextState,
                            List<GameplayEvent> events,
                            boolean finishesCommand) {
                        coordinator.onRecoveredEventsAccepted(
                                generation, nextState, events, finishesCommand);
                    }

                    @Override
                    public void onResyncRequired(boolean finishesCommand) {
                        coordinator.onRecoveredEventsResyncRequired(generation, finishesCommand);
                    }
                });
    }

    void accept(
            long generation,
            GameplayTableState current,
            List<GameplayEvent> events,
            boolean finishesCommand,
            Callback callback) {
        accept(generation, current, events, finishesCommand, callback, null);
    }

    private void accept(
            long generation,
            GameplayTableState current,
            List<GameplayEvent> events,
            boolean finishesCommand,
            Callback callback,
            String releasedSelfDelayedType) {
        if (!callback.isCurrent(generation)) {
            return;
        }
        Pending inFlight = pending;
        List<GameplayEvent> batch;
        boolean finishesPendingCommand;
        if (inFlight != null) {
            // 命令应答与回放中批次按游标合并（同代），而不是覆盖 pending：覆盖会丢掉
            // 未回放的 suffix，下一次次轮询就触发游标 gap，resync 快照直接跳到终态，
            // 洗牌/骰子/发牌整段节奏被吞。合并后重建 Pending，延迟链继续接力。
            List<GameplayEvent> merged = new ArrayList<>(inFlight.events().size() + events.size());
            merged.addAll(inFlight.events());
            merged.addAll(unapplied(inFlight.state(), events));
            merged.sort(
                    Comparator.comparingLong(GameplayEvent::revision)
                            .thenComparingInt(GameplayEvent::eventOrder));
            batch = List.copyOf(merged);
            current = inFlight.state();
            finishesPendingCommand = inFlight.finishesCommand() || finishesCommand;
        } else {
            batch = unapplied(current, events);
            finishesPendingCommand = finishesCommand;
        }
        Split split = playbackSplit(current, batch, releasedSelfDelayedType);
        if (split == null) {
            applyAndReport(generation, current, batch, finishesPendingCommand, callback);
            return;
        }
        List<GameplayEvent> prefix = List.copyOf(batch.subList(0, split.immediateCount()));
        List<GameplayEvent> suffix = List.copyOf(batch.subList(split.immediateCount(), batch.size()));
        try {
            if (prefix.isEmpty()) {
                // 原版 isIncludeSelf=true 语义：延迟中的队头协议（如 TAKE_FIRST 的 msgGameStep）
                // 尚未执行，不派发任何回调，UI 维持上一拍画面直到计时结束。
                GameplayTableState unchanged = apply(current, List.of());
                Pending queued = new Pending(
                        generation,
                        unchanged,
                        suffix,
                        finishesPendingCommand,
                        callback,
                        split.selfDelayedType());
                pending = queued;
                scheduler.postDelayed(() -> flush(queued), split.delayMillis());
                return;
            }
            GameplayTableState next = apply(current, prefix);
            callback.onAccepted(next, prefix, false);
            Pending queued = new Pending(
                    generation,
                    next,
                    suffix,
                    finishesPendingCommand,
                    callback,
                    split.selfDelayedType());
            pending = queued;
            scheduler.postDelayed(() -> flush(queued), split.delayMillis());
        } catch (GameplayResyncRequiredException exception) {
            pending = null;
            callback.onResyncRequired(finishesPendingCommand);
        }
    }

    private void flush(Pending queued) {
        if (pending != queued) {
            return;
        }
        pending = null;
        if (queued.callback.isCurrent(queued.generation)) {
            accept(
                    queued.generation,
                    queued.state,
                    queued.events,
                    queued.finishesCommand,
                    queued.callback,
                    queued.selfDelayedType);
        }
    }

    private static void applyAndReport(
            long generation,
            GameplayTableState current,
            List<GameplayEvent> events,
            boolean finishesCommand,
            Callback callback) {
        try {
            callback.onAccepted(apply(current, events), events, finishesCommand);
        } catch (GameplayResyncRequiredException exception) {
            callback.onResyncRequired(finishesCommand);
        }
    }

    /**
     * 丢掉游标已经越过的事件。
     *
     * <p>同一批权威事件会经两条路到达：命令应答直接带回，随后 {@code GET /events} 轮询又拉一次。
     * {@code GameplayReducer} 本身按 {@code (revision, eventOrder)} 判重，状态不会被应用两次；
     * 这里把重复项在进门时就摘掉，让 {@code onEvents} 也只报一次，牌局音频不会重播。
     */
    private static List<GameplayEvent> unapplied(
            GameplayTableState current, List<GameplayEvent> events) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }
        List<GameplayEvent> batch = new java.util.ArrayList<>(events.size());
        for (GameplayEvent event : events) {
            boolean applied =
                    current != null
                            && (event.revision() < current.revision()
                                    || (event.revision() == current.revision()
                                            && event.eventOrder() <= current.eventOrder()));
            if (!applied) {
                batch.add(event);
            }
        }
        return List.copyOf(batch);
    }

    private static GameplayTableState apply(GameplayTableState current, List<GameplayEvent> events) {
        GameplayTableState next = current;
        for (GameplayEvent event : events) {
            next = GameplayReducer.reduce(next, event);
        }
        return next;
    }

    /**
     * 原版 {@code GameLayer/Module.luac:1163-1171} needDelay 的两种延迟形状：
     *
     * <ul>
     *   <li>{@code isIncludeSelf == false}（msgToTalShuffle 3.75s）：命中事件立即应用，
     *       它之后的事件等满 delay —— {@code immediateCount = index + 1}。
     *   <li>{@code isIncludeSelf == true}（GAME_STEP_TAKE_FIRST 的 msgGameStep 1s）：
     *       命中事件自身也要等 delay —— {@code immediateCount = index}，命中事件留在
     *       suffix 里，prefix 为空时不派发回调，对应原版队列头 delayTime 置 0 前的空转。
     * </ul>
     *
     * <p>每批只返回第一个延迟点；计时结束 flush 后对剩余事件重新扫描，
     * [WALL_SHUFFLED, DICE_ROLLED, DEALT, …] 自然链成洗牌 3.75s → 骰子 1.2s →
     * 发牌定格 1s 的原版节奏。原版永不延迟的 msgPlayCount/msgCurPanShu/msgQuanCount
     * 在本工程没有独立事件（计数并入快照），无需豁免名单。
     */
    private static Split playbackSplit(
            GameplayTableState current, List<GameplayEvent> events, String releasedSelfDelayedType) {
        for (int index = 0; index < events.size() - 1; index++) {
            String type = events.get(index).type();
            if (index == 0 && type.equals(releasedSelfDelayedType)) {
                continue;
            }
            if ("WALL_SHUFFLED".equals(type)) {
                return new Split(index + 1, SHUFFLE_PLAYBACK_MILLIS, null);
            }
            if ("DICE_ROLLED".equals(type)) {
                return new Split(index + 1, THROW_CHIP_PLAYBACK_MILLIS, null);
            }
            if ("TURN_ADVANCED".equals(type) && isNonSelfTurn(current, events.get(index))) {
                return new Split(index + 1, TURN_ADVANCED_PLAYBACK_MILLIS, null);
            }
            if ("DEALT".equals(type)) {
                return new Split(index, TAKE_FIRST_PLAYBACK_MILLIS, type);
            }
            if ("ROUND_RESULT_READY".equals(type)) {
                return new Split(index, SETTLE_WINDOW_PLAYBACK_MILLIS, type);
            }
        }
        return null;
    }

    private static boolean isNonSelfTurn(GameplayTableState current, GameplayEvent event) {
        if (current == null) {
            return false;
        }
        int activeSeat = event.payload().optInt("activeSeat", current.mySeat());
        return activeSeat > 0 && activeSeat != current.mySeat();
    }

    private record Pending(
            long generation,
            GameplayTableState state,
            List<GameplayEvent> events,
            boolean finishesCommand,
            Callback callback,
            String selfDelayedType) {}

    /** Count of leading events applied before the delay; the rest wait it out. */
    private record Split(int immediateCount, long delayMillis, String selfDelayedType) {}
}
