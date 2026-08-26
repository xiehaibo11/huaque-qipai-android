package com.nanbeiyule.game;

/** Joins the original Spine reveal event with the authoritative draw response. */
final class TaizhouTreasureDrawGate {
    enum Phase {
        IDLE,
        DRAWING,
        WAITING_RESULT
    }

    private Phase phase = Phase.IDLE;
    private int count;
    private boolean repeat;
    private boolean revealEventReached;
    private FortuneTreasureDrawResult result;

    boolean begin(int count) {
        return begin(count, false);
    }

    boolean begin(int count, boolean repeat) {
        if (phase != Phase.IDLE || (count != 1 && count != 5)) return false;
        this.count = count;
        this.repeat = repeat;
        revealEventReached = false;
        result = null;
        phase = Phase.DRAWING;
        return true;
    }

    void onAnimationEvent(String eventName) {
        if (phase == Phase.IDLE || !"cx".equals(eventName)) return;
        revealEventReached = true;
        if (result == null) phase = Phase.WAITING_RESULT;
    }

    void onServerResult(FortuneTreasureDrawResult result) {
        if (phase == Phase.IDLE || result == null || result.count() != count) return;
        this.result = result;
    }

    void onFailure() {
        reset();
    }

    boolean canReveal() {
        return phase != Phase.IDLE && revealEventReached && result != null;
    }

    FortuneTreasureDrawResult consumeReveal() {
        if (!canReveal()) return null;
        FortuneTreasureDrawResult revealed = result;
        reset();
        return revealed;
    }

    boolean drawing() {
        return phase != Phase.IDLE;
    }

    boolean repeat() {
        return repeat;
    }

    Phase phase() {
        return phase;
    }

    private void reset() {
        phase = Phase.IDLE;
        count = 0;
        repeat = false;
        revealEventReached = false;
        result = null;
    }
}
