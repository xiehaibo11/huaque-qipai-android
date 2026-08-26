package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class MailRowMotionTest {
    @Test
    public void reproducesTheOriginalStaggeredFadeAndOneHundredFiftyUnitSlide() {
        MailRowMotion.Frame start = MailRowMotion.frame(0, 0f);
        MailRowMotion.Frame middle = MailRowMotion.frame(0, 2f / 30f);
        MailRowMotion.Frame end = MailRowMotion.frame(0, 10f / 30f);
        MailRowMotion.Frame delayed = MailRowMotion.frame(3, 2f / 30f);

        assertEquals(150f, start.offsetY(), 0.01f);
        assertEquals(0f, start.alpha(), 0.01f);
        assertEquals(120f, middle.offsetY(), 0.01f);
        assertEquals(0.5f, middle.alpha(), 0.01f);
        assertEquals(0f, end.offsetY(), 0.01f);
        assertEquals(1f, end.alpha(), 0.01f);
        assertEquals(150f, delayed.offsetY(), 0.01f);
    }
}
