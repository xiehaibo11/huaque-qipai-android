package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MainActivityEntryModeTest {
    @Test
    public void routesZhejiangShopActionToDirectShop() {
        MainActivityEntryMode mode =
                MainActivityEntryMode.fromAction("OPEN_ZHEJIANG_SHOP");

        assertEquals(MainActivityEntryMode.ZHEJIANG_SHOP, mode);
        assertTrue(mode.isDirect());
        assertTrue(mode.opensShop());
        assertFalse(mode.opensGoldRoom());
    }

    @Test
    public void routesCreateRoomActionToDirectCreateRoom() {
        MainActivityEntryMode mode =
                MainActivityEntryMode.fromAction("OPEN_ZHEJIANG_CREATE_ROOM");

        assertTrue(mode.isDirect());
        assertEquals(MainActivityDestination.CREATE_ROOM, mode.destination());
        assertTrue(mode.loadsGameHomeBeforeDestination());
        assertFalse(mode.rendersGameHomeBeforeDestination());
    }

    @Test
    public void routesJoinRoomActionToDirectJoinRoom() {
        MainActivityEntryMode mode =
                MainActivityEntryMode.fromAction("OPEN_ZHEJIANG_JOIN_ROOM");

        assertTrue(mode.isDirect());
        assertEquals("JOIN_ROOM", mode.destination().name());
        assertTrue(mode.loadsGameHomeBeforeDestination());
        assertFalse(mode.rendersGameHomeBeforeDestination());
    }

    @Test
    public void routesZhejiangDressUpActionToDecorationShop() {
        MainActivityEntryMode mode =
                MainActivityEntryMode.fromAction("OPEN_ZHEJIANG_DECORATION_SHOP");

        assertEquals(MainActivityEntryMode.ZHEJIANG_DECORATION_SHOP, mode);
        assertTrue(mode.isDirect());
        assertTrue(mode.opensShop());
        assertEquals(ShopCategory.DECORATION, mode.initialShopCategory());
        assertFalse(mode.loadsGameHomeBeforeDestination());
    }

    @Test
    public void preservesExistingGoldAndStandardEntryRoutes() {
        MainActivityEntryMode gold =
                MainActivityEntryMode.fromAction("OPEN_TAIZHOU_GOLD");
        MainActivityEntryMode standard = MainActivityEntryMode.fromAction(null);

        assertEquals(MainActivityEntryMode.TAIZHOU_GOLD, gold);
        assertTrue(gold.opensGoldRoom());
        assertFalse(gold.opensShop());
        assertEquals(MainActivityEntryMode.STANDARD, standard);
        assertFalse(standard.isDirect());
    }

    @Test
    public void directShopSkipsGameHomeRenderingBeforeDestination() {
        assertFalse(
                MainActivityEntryMode.ZHEJIANG_SHOP
                        .rendersGameHomeBeforeDestination());
    }

    @Test
    public void directShopSkipsGameHomeLoadingBeforeDestination() {
        assertFalse(
                MainActivityEntryMode.ZHEJIANG_SHOP
                        .loadsGameHomeBeforeDestination());
    }

    @Test
    public void routesZhejiangPersonalCenterWithoutRenderingGameHome() {
        MainActivityEntryMode mode =
                MainActivityEntryMode.fromAction("OPEN_ZHEJIANG_PERSONAL_CENTER");

        assertEquals(MainActivityEntryMode.ZHEJIANG_PERSONAL_CENTER, mode);
        assertTrue(mode.isDirect());
        assertTrue(mode.loadsGameHomeBeforeDestination());
        assertFalse(mode.rendersGameHomeBeforeDestination());
        assertTrue(mode.opensPersonalCenter());
        assertFalse(mode.opensShop());
        assertFalse(mode.opensGoldRoom());
    }

    @Test
    public void directPersonalCenterReturnsAccountSwitchToTheLobby() {
        assertTrue(
                MainActivityEntryMode.ZHEJIANG_PERSONAL_CENTER
                        .returnsAccountSwitchToLauncher());
        assertTrue(
                MainActivityEntryMode.ZHEJIANG_PHONE_BINDING
                        .returnsAccountSwitchToLauncher());
        assertFalse(
                MainActivityEntryMode.STANDARD
                        .returnsAccountSwitchToLauncher());
    }

    @Test
    public void routesEveryLobbyFeatureActionToItsDirectDestination() {
        assertEquals(
                MainActivityEntryMode.ZHEJIANG_ACTIVITIES,
                MainActivityEntryMode.fromAction("OPEN_ZHEJIANG_ACTIVITIES"));
        assertEquals(
                MainActivityEntryMode.ZHEJIANG_SHARE,
                MainActivityEntryMode.fromAction("OPEN_ZHEJIANG_SHARE"));
        assertEquals(
                MainActivityEntryMode.ZHEJIANG_BAG,
                MainActivityEntryMode.fromAction("OPEN_ZHEJIANG_BAG"));
        assertEquals(
                MainActivityEntryMode.ZHEJIANG_SCORING_ASSISTANT,
                MainActivityEntryMode.fromAction("OPEN_ZHEJIANG_SCORING_ASSISTANT"));
        assertEquals(
                MainActivityEntryMode.ZHEJIANG_WECHAT_PUBLIC,
                MainActivityEntryMode.fromAction("OPEN_ZHEJIANG_WECHAT_PUBLIC"));
        assertEquals(
                MainActivityEntryMode.ZHEJIANG_NEWS,
                MainActivityEntryMode.fromAction("OPEN_ZHEJIANG_NEWS"));
        assertEquals(
                MainActivityEntryMode.ZHEJIANG_PHONE_BINDING,
                MainActivityEntryMode.fromAction("OPEN_ZHEJIANG_PHONE_BINDING"));
        assertEquals(
                MainActivityEntryMode.ZHEJIANG_RULES,
                MainActivityEntryMode.fromAction("OPEN_ZHEJIANG_RULES"));
        assertEquals(
                MainActivityEntryMode.ZHEJIANG_HEALTH_NOTICE,
                MainActivityEntryMode.fromAction("OPEN_ZHEJIANG_HEALTH_NOTICE"));
        assertEquals(
                MainActivityEntryMode.ZHEJIANG_ANNOUNCEMENTS,
                MainActivityEntryMode.fromAction("OPEN_ZHEJIANG_ANNOUNCEMENTS"));
    }

    @Test
    public void classifiesEveryDirectDestinationForTheLauncherFlow() {
        assertEquals(
                MainActivityDestination.ACTIVITY_CENTER,
                MainActivityEntryMode.ZHEJIANG_ACTIVITIES.destination());
        assertEquals(
                MainActivityDestination.SHARE,
                MainActivityEntryMode.ZHEJIANG_SHARE.destination());
        assertEquals(
                MainActivityDestination.BAG,
                MainActivityEntryMode.ZHEJIANG_BAG.destination());
        assertEquals(
                MainActivityDestination.SCORING_ASSISTANT,
                MainActivityEntryMode.ZHEJIANG_SCORING_ASSISTANT.destination());
        assertEquals(
                MainActivityDestination.WECHAT_PUBLIC,
                MainActivityEntryMode.ZHEJIANG_WECHAT_PUBLIC.destination());
        assertEquals(
                MainActivityDestination.ZHEJIANG_NEWS,
                MainActivityEntryMode.ZHEJIANG_NEWS.destination());
        assertEquals(
                MainActivityDestination.PHONE_BINDING,
                MainActivityEntryMode.ZHEJIANG_PHONE_BINDING.destination());
        assertEquals(
                MainActivityDestination.RULES,
                MainActivityEntryMode.ZHEJIANG_RULES.destination());
        assertEquals(
                MainActivityDestination.HEALTH_NOTICE,
                MainActivityEntryMode.ZHEJIANG_HEALTH_NOTICE.destination());
        assertEquals(
                MainActivityDestination.ANNOUNCEMENTS,
                MainActivityEntryMode.ZHEJIANG_ANNOUNCEMENTS.destination());
    }

    @Test
    public void phoneBindingUsesTheRealPersonalCenterPhoneTab() {
        assertTrue(MainActivityEntryMode.ZHEJIANG_PHONE_BINDING.loadsGameHomeBeforeDestination());
        assertEquals(3, PersonalCenterView.PHONE_BINDING_TAB);
    }
}
