package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MailViewportTest {
    @Test
    public void backgroundIsFullBleedWhileContentKeepsOneUniformScale() {
        int[][] screens = {
            {1600, 900},
            {1800, 900},
            {1950, 900},
            {2000, 900},
            {1920, 1200},
            {2200, 1800}
        };

        for (int[] screen : screens) {
            MailViewport viewport = MailViewport.fullBleed(screen[0], screen[1]);

            assertEquals(screen[0], viewport.backgroundWidth());
            assertEquals(screen[1], viewport.backgroundHeight());
            assertTrue(MailLayout.DESIGN_WIDTH * viewport.content().scale() <= screen[0] + 0.01f);
            assertTrue(MailLayout.DESIGN_HEIGHT * viewport.content().scale() <= screen[1] + 0.01f);
        }
    }
}
