package com.nanbeiyule.game;

import java.util.List;

/** Recovered HallScene touch-effect variants and their original Cocos scale. */
record OriginalLobbyTapEffectSpec(
        String assetDirectory,
        String baseName,
        String animationName,
        float scale) {
    private static final List<OriginalLobbyTapEffectSpec> VARIANTS =
            List.of(variant(1), variant(2), variant(3), variant(4));

    static List<OriginalLobbyTapEffectSpec> variants() {
        return VARIANTS;
    }

    private static OriginalLobbyTapEffectSpec variant(int number) {
        String baseName = "diani" + number;
        return new OriginalLobbyTapEffectSpec(
                "lobby_tap_effects/" + baseName,
                baseName,
                "animation",
                0.7f);
    }
}
