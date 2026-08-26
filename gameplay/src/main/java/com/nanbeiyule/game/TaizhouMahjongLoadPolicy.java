package com.nanbeiyule.game;

/** Tracks whether the native Taizhou table shell needs a full-screen loading transition. */
final class TaizhouMahjongLoadPolicy {
    private boolean fullLoaderRequired;

    TaizhouMahjongLoadPolicy(boolean fullLoaderRequired) {
        this.fullLoaderRequired = fullLoaderRequired;
    }

    boolean shouldShowFullLoader() {
        return fullLoaderRequired;
    }

    void markTableEntered() {
        fullLoaderRequired = false;
    }
}
