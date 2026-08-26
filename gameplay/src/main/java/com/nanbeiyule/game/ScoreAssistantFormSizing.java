package com.nanbeiyule.game;

/** Pixel sizing shared by score forms in normal, split-screen and folded windows. */
final class ScoreAssistantFormSizing {
    private ScoreAssistantFormSizing() {}

    static int dialogWidth(int availableWidthPx, float density, float preferredFraction) {
        int available = Math.max(1, availableWidthPx);
        int preferred = Math.round(available * preferredFraction);
        int minimum = Math.round(420f * Math.max(0.1f, density));
        int maximum = Math.max(1, Math.round(available * 0.94f));
        return Math.min(maximum, Math.max(minimum, preferred));
    }
}
