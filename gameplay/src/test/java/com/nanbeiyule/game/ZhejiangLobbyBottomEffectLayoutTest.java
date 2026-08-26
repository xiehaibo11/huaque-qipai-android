package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class ZhejiangLobbyBottomEffectLayoutTest {
    private static final float EPSILON = 0.01f;

    @Test
    public void placesCompleteOriginalStoreAndQuickStartAnimations() {
        List<OriginalLobbyEffectSpec> specs =
                ZhejiangLobbyBottomEffectLayout.specs();

        assertEquals(2, specs.size());

        OriginalLobbyEffectSpec store = specs.get(0);
        assertEquals("lobby_effects/zzb_jbdt_sc", store.assetDirectory());
        assertEquals("zzb_jbdt_sc", store.baseName());
        assertEquals("animation", store.animationName());
        assertEquals(284.15f, store.anchorX(), EPSILON);
        assertEquals(1688.6779f, store.anchorY(), EPSILON);
        assertEquals(1.6666667f, store.scale(), EPSILON);
        assertTrue(store.drawsAttachment("cc"));
        assertTrue(store.drawsAttachment("zi"));
        assertTrue(store.drawsAttachment("coin_0"));

        OriginalLobbyEffectSpec quickStart = specs.get(1);
        assertEquals("lobby_effects/zzb_jbdt_ksks", quickStart.assetDirectory());
        assertEquals("zzb_jbdt_ksks", quickStart.baseName());
        assertEquals("animation", quickStart.animationName());
        assertEquals(2751.6667f, quickStart.anchorX(), EPSILON);
        assertEquals(1645.9852f, quickStart.anchorY(), EPSILON);
        assertEquals(1.6666667f, quickStart.scale(), EPSILON);
        assertTrue(quickStart.drawsAttachment("btn"));
        assertTrue(quickStart.drawsAttachment("ksks"));
        assertTrue(quickStart.drawsAttachment("btng/btng_00002"));
    }
}
