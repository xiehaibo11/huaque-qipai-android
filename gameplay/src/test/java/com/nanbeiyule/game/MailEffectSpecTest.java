package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class MailEffectSpecTest {
    @Test
    public void placesTheRecoveredMailboxSpineAtItsCsbAnimationNode() {
        MailEffectSpec spec = MailEffectSpec.mainMailbox();

        assertEquals("mail_effects/zzb_jbdt_youxiang", spec.assetDirectory());
        assertEquals("zzb_jbdt_youxiang", spec.baseName());
        assertEquals("cx", spec.entranceAnimation());
        assertEquals("loop", spec.loopAnimation());
        assertEquals(960f, spec.anchorX(), 0.01f);
        assertEquals(540f, spec.anchorY(), 0.01f);
        assertEquals(1f, spec.scale(), 0.01f);
    }
}
