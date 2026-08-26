package com.nanbeiyule.game;

/** Placement and animation sequence recovered from the mail CSB/Lua pair. */
record MailEffectSpec(
        String assetDirectory,
        String baseName,
        String entranceAnimation,
        String loopAnimation,
        float anchorX,
        float anchorY,
        float scale) {
    static MailEffectSpec mainMailbox() {
        return new MailEffectSpec(
                "mail_effects/zzb_jbdt_youxiang",
                "zzb_jbdt_youxiang",
                "cx",
                "loop",
                960f,
                540f,
                1f);
    }

    static MailEffectSpec detailPanel() {
        return new MailEffectSpec(
                "mail_effects/zzb_jbdt_youxiangtc",
                "zzb_jbdt_youxiangtc",
                "cx",
                "loop",
                960f,
                540f,
                1f);
    }
}
