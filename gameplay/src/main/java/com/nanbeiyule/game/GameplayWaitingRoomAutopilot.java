package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.gameplay.GameplaySeat;
import com.nanbeiyule.game.gameplay.GameplayTableState;

/**
 * 等待阶段的自动推进判定：原版自动准备房间替本人补 READY，满员且全员准备后由房主发 START_ROUND。
 *
 * <p>每个修订号最多触发一次，避免同一份权威状态被重复渲染时重复发命令。
 * 判定只读服务端权威状态，不做任何本地推断。
 */
final class GameplayWaitingRoomAutopilot {
    static final String READY = "READY";
    static final String START_ROUND = "START_ROUND";

    private long lastAutoReadyRevision = Long.MIN_VALUE;
    private long lastStartRevision = Long.MIN_VALUE;

    void reset() {
        lastAutoReadyRevision = Long.MIN_VALUE;
        lastStartRevision = Long.MIN_VALUE;
    }

    /** 返回本次权威状态应提交的命令类型；没有要发的命令时返回 {@code null}。 */
    String nextCommand(GameplayTableState state) {
        if (state.phase() != GameplayPhase.WAITING) {
            return null;
        }
        if (state.autoReady() && !allOccupiedReady(state)) {
            if (lastAutoReadyRevision == state.revision()) {
                return null;
            }
            lastAutoReadyRevision = state.revision();
            return READY;
        }
        if (state.seats().size() == state.chairCount()
                && allOccupiedReady(state)
                && localHost(state)
                && lastStartRevision != state.revision()) {
            lastStartRevision = state.revision();
            return START_ROUND;
        }
        return null;
    }

    private static boolean allOccupiedReady(GameplayTableState state) {
        return !state.seats().isEmpty()
                && state.seats().stream().allMatch(GameplaySeat::ready);
    }

    private static boolean localHost(GameplayTableState state) {
        return state.seats().stream()
                .anyMatch(seat -> seat.seatNumber() == state.mySeat() && seat.host());
    }
}
