package com.nanbeiyule.game;

/** Rejects responses issued before the currently visible tab or command request. */
final class ScoreAssistantRequestGate {
    private long generation;

    long issue() {
        return ++generation;
    }

    boolean isCurrent(long candidate) {
        return candidate == generation;
    }

    void invalidate() {
        generation++;
    }
}
