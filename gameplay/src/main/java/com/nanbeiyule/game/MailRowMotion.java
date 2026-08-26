package com.nanbeiyule.game;

final class MailRowMotion {
    private static final float FADE_SECONDS = 4f / 30f;
    private static final float MOVE_SECONDS = 10f / 30f;
    private static final float STAGGER_SECONDS = 1f / 30f;

    record Frame(float offsetY, float alpha) {}

    static Frame frame(int index, float elapsedSeconds) {
        float local = Math.max(0f, elapsedSeconds - index * STAGGER_SECONDS);
        float move = Math.min(1f, local / MOVE_SECONDS);
        float alpha = Math.min(1f, local / FADE_SECONDS);
        return new Frame(150f * (1f - move), alpha);
    }

    static float totalDuration(int count) {
        return MOVE_SECONDS + Math.max(0, count - 1) * STAGGER_SECONDS;
    }

    private MailRowMotion() {}
}
