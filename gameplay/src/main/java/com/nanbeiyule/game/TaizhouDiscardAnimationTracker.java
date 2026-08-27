package com.nanbeiyule.game;

import com.nanbeiyule.game.mahjong.TaizhouMahjongVisibleRound;

final class TaizhouDiscardAnimationTracker {
    static final long DISCARD_FLIGHT_MILLIS = 200L;

    record LastDiscard(int serverSeat, int tileIndex, int tileValue) {}

    record RunningDiscard(int serverSeat, int tileIndex, int tileValue, float progress) {}

    private LastDiscard lastDiscard;
    private LastDiscard runningDiscard;
    private long runningStartedAt;

    boolean update(TaizhouMahjongVisibleRound round, long nowElapsed) {
        LastDiscard next = lastDiscard(round);
        if (next == null) {
            lastDiscard = null;
            runningDiscard = null;
            return false;
        }
        if (lastDiscard != null
                && lastDiscard.serverSeat() == next.serverSeat()
                && lastDiscard.tileIndex() == next.tileIndex()
                && lastDiscard.tileValue() == next.tileValue()) {
            return false;
        }
        lastDiscard = next;
        runningDiscard = next;
        runningStartedAt = nowElapsed;
        return true;
    }

    LastDiscard lastDiscard() {
        return lastDiscard;
    }

    long lastDiscardElapsedMillis(long nowElapsed) {
        return lastDiscard == null ? Long.MAX_VALUE : Math.max(0L, nowElapsed - runningStartedAt);
    }

    boolean hasRunningDiscard(long nowElapsed) {
        return running(nowElapsed) != null;
    }

    RunningDiscard running(long nowElapsed) {
        if (runningDiscard == null) {
            return null;
        }
        long elapsed = Math.max(0L, nowElapsed - runningStartedAt);
        if (elapsed >= DISCARD_FLIGHT_MILLIS) {
            runningDiscard = null;
            return null;
        }
        return new RunningDiscard(
                runningDiscard.serverSeat(),
                runningDiscard.tileIndex(),
                runningDiscard.tileValue(),
                elapsed / (float) DISCARD_FLIGHT_MILLIS);
    }

    long nextRepaintDelayMillis(long nowElapsed) {
        return hasRunningDiscard(nowElapsed) ? 16L : 0L;
    }

    private static LastDiscard lastDiscard(TaizhouMahjongVisibleRound round) {
        if (round == null || round.lastDiscard() == null) {
            return null;
        }
        TaizhouMahjongVisibleRound.LastDiscard marker = round.lastDiscard();
        int tileValue = round.riverAt(marker.seatNumber()).tiles().get(marker.tileIndex());
        return new LastDiscard(marker.seatNumber(), marker.tileIndex(), tileValue);
    }
}
