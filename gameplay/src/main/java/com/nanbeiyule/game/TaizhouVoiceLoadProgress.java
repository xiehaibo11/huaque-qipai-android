package com.nanbeiyule.game;

final class TaizhouVoiceLoadProgress {
    private int percent;
    private boolean visible;

    void start() {
        percent = 0;
        visible = true;
    }

    void onProgress(int loaded, int total) {
        if (total <= 0 || loaded >= total) {
            percent = 100;
            visible = false;
            return;
        }
        percent = Math.max(0, Math.min(99, Math.round(loaded * 100.0f / total)));
        visible = true;
    }

    int percent() {
        return percent;
    }

    boolean visible() {
        return visible;
    }
}
