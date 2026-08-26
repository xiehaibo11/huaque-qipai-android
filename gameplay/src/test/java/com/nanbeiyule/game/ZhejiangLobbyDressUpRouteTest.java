package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Method;
import org.junit.Test;

public class ZhejiangLobbyDressUpRouteTest {
    @Test
    public void dressUpOpensTheDecorationShopLikeTheOriginalLobby() throws Exception {
        ZhejiangLobbyAction.Route route = ZhejiangLobbyAction.bottom("DRESS_UP");

        assertNull(route.unavailableMessage());
        assertEquals("SHOP_DECORATION", route.destination().name());

        Method hitKind =
                GameHomeContentRenderer.class.getDeclaredMethod(
                        "hitKind", ZhejiangLobbyAction.Destination.class);
        hitKind.setAccessible(true);
        Enum<?> resolved = (Enum<?>) hitKind.invoke(null, route.destination());
        assertEquals("SHOP_DECORATION", resolved.name());
    }

    @Test
    public void storeStillOpensTheDefaultShopPage() {
        ZhejiangLobbyAction.Route route = ZhejiangLobbyAction.bottom("STORE");

        assertNull(route.unavailableMessage());
        assertEquals(ZhejiangLobbyAction.Destination.SHOP, route.destination());
    }
}
