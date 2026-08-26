package com.nanbeiyule.game;

final class FreeDrawRewardGate {
    enum Action {
        NONE,
        CLAIM_REWARD
    }

    private enum State {
        IDLE,
        OPENING_SESSION,
        LOADING_AD,
        SHOWING_AD,
        CLAIMING
    }

    private State state = State.IDLE;

    boolean begin() {
        if (state != State.IDLE) return false;
        state = State.OPENING_SESSION;
        return true;
    }

    void onSessionOpened() {
        if (state == State.OPENING_SESSION) state = State.LOADING_AD;
    }

    void onAdShown() {
        if (state == State.LOADING_AD) state = State.SHOWING_AD;
    }

    Action onRewardVerified() {
        if (state != State.SHOWING_AD) return Action.NONE;
        state = State.CLAIMING;
        return Action.CLAIM_REWARD;
    }

    void onAdClosed() {
        if (state == State.SHOWING_AD || state == State.LOADING_AD) state = State.IDLE;
    }

    void fail() {
        if (state != State.CLAIMING) state = State.IDLE;
    }

    void complete() {
        state = State.IDLE;
    }

    boolean claimInFlight() {
        return state == State.CLAIMING;
    }
}
