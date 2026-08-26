package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class LobbyBackpackLayoutTest {
    @Test
    public void headerAndFourCategoryTouchRegionsMatchTheOriginalComposition() {
        assertEquals(LobbyBackpackLayout.Target.close(), LobbyBackpackLayout.targetAt(50, 67, 0, false));
        assertEquals(LobbyBackpackLayout.Target.shop(), LobbyBackpackLayout.targetAt(1780, 72, 0, false));
        assertEquals(LobbyBackpackLayout.Target.category(0), LobbyBackpackLayout.targetAt(150, 210, 0, false));
        assertEquals(LobbyBackpackLayout.Target.category(3), LobbyBackpackLayout.targetAt(150, 651, 0, false));
    }

    @Test
    public void gridAndUseTouchRegionsAreIndexAndCapabilityAware() {
        assertEquals(LobbyBackpackLayout.Target.item(0), LobbyBackpackLayout.targetAt(500, 300, 6, false));
        assertEquals(LobbyBackpackLayout.Target.item(5), LobbyBackpackLayout.targetAt(1200, 800, 6, false));
        assertEquals(LobbyBackpackLayout.Target.none(), LobbyBackpackLayout.targetAt(1680, 785, 6, false));
        assertEquals(LobbyBackpackLayout.Target.use(), LobbyBackpackLayout.targetAt(1680, 785, 6, true));
    }
}
