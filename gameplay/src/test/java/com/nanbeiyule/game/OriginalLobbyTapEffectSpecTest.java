package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;

public final class OriginalLobbyTapEffectSpecTest {
    @Test
    public void exposesTheFourRecoveredOneShotTapAnimations() {
        List<OriginalLobbyTapEffectSpec> specs = OriginalLobbyTapEffectSpec.variants();

        assertEquals(4, specs.size());
        for (int index = 0; index < specs.size(); index++) {
            int variant = index + 1;
            OriginalLobbyTapEffectSpec spec = specs.get(index);
            assertEquals("lobby_tap_effects/diani" + variant, spec.assetDirectory());
            assertEquals("diani" + variant, spec.baseName());
            assertEquals("animation", spec.animationName());
            assertEquals(0.7f, spec.scale(), 0.001f);
        }
    }
}
