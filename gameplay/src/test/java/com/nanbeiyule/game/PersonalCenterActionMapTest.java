package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class PersonalCenterActionMapTest {
    @Test
    public void mapsOriginalProfileControls() {
        assertEquals(
                PersonalCenterAction.SHOP_ROOM_CARDS,
                PersonalCenterActionMap.actionFor(0, 1560f, 263f));
        assertEquals(
                PersonalCenterAction.BOUND_ROOM_CARD_HELP,
                PersonalCenterActionMap.actionFor(0, 1560f, 377f));
        assertEquals(
                PersonalCenterAction.SHOP_DIAMONDS,
                PersonalCenterActionMap.actionFor(0, 1560f, 491f));
        assertEquals(
                PersonalCenterAction.ACCOUNT_DELETION,
                PersonalCenterActionMap.actionFor(0, 817f, 875f));
    }

    @Test
    public void mapsOriginalPrivacyAndPhoneControls() {
        assertEquals(
                PersonalCenterAction.OPEN_APP_PERMISSION_SETTINGS,
                PersonalCenterActionMap.actionFor(2, 1480f, 245f));
        assertEquals(
                PersonalCenterAction.TOGGLE_CLIPBOARD_PERMISSION,
                PersonalCenterActionMap.actionFor(2, 1480f, 365f));
        assertEquals(
                PersonalCenterAction.PHONE_SEND_CODE,
                PersonalCenterActionMap.actionFor(3, 1320f, 624f));
        assertEquals(
                PersonalCenterAction.PHONE_SUBMIT,
                PersonalCenterActionMap.actionFor(3, 1120f, 804f));
    }

    @Test
    public void mapsHealthAndMembershipControls() {
        assertEquals(
                PersonalCenterAction.REAL_NAME,
                PersonalCenterActionMap.actionFor(1, 1080f, 590f));
        assertEquals(
                PersonalCenterAction.MEMBERSHIP_PREVIOUS,
                PersonalCenterActionMap.actionFor(4, 603f, 398f));
        assertEquals(
                PersonalCenterAction.MEMBERSHIP_NEXT,
                PersonalCenterActionMap.actionFor(4, 1522f, 398f));
        assertEquals(
                PersonalCenterAction.MEMBERSHIP_CENTER,
                PersonalCenterActionMap.actionFor(4, 1100f, 400f));
        assertEquals(
                PersonalCenterAction.MEMBERSHIP_GIFT,
                PersonalCenterActionMap.actionFor(4, 1100f, 700f));
    }
}
