package com.nanbeiyule.game;

import java.util.List;

final class ZhejiangLobbyBottomEffectLayout {
    private static final float PAGE_SCALE = 3200.0f / 1920.0f;
    private static final float PAGE_Y_SCALE = 1792.0f / 1080.0f;

    private ZhejiangLobbyBottomEffectLayout() {}

    static List<OriginalLobbyEffectSpec> specs() {
        return List.of(
                OriginalLobbyEffectSpec.overlay(
                        "zzb_jbdt_sc",
                        "animation",
                        170.49f * PAGE_SCALE,
                        1017.73f * PAGE_Y_SCALE,
                        PAGE_SCALE),
                OriginalLobbyEffectSpec.overlay(
                        "zzb_jbdt_ksks",
                        "animation",
                        1651.0f * PAGE_SCALE,
                        992.0f * PAGE_Y_SCALE,
                        PAGE_SCALE));
    }
}
