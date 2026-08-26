package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;

public class PersonalCenterStateTest {
    @Test
    public void parsesZhejiangHealthAndMembershipSnapshot() throws Exception {
        PersonalCenterState state =
                PersonalCenterState.fromJson(
                        new JSONObject(
                                """
                                {
                                  "player": {
                                    "userId": "2dc3ebc1-a47c-4695-aa74-6a86729cf566",
                                    "publicPlayerId": 1084375590,
                                    "displayName": "WhimSeeker",
                                    "avatarKey": "avatar-user",
                                    "membershipLevel": 1
                                  },
                                  "wallet": {
                                    "purchasedRoomCards": 9,
                                    "boundRoomCards": 2,
                                    "coins": 1835,
                                    "diamonds": 0
                                  },
                                  "account": {
                                    "phoneBound": true,
                                    "maskedPhone": "158****6092",
                                    "identityProviders": ["PHONE", "WECHAT"]
                                  },
                                  "region": {"lobbyId": 900021, "areaName": "台州"},
                                  "healthCertification": {
                                    "status": "VERIFIED",
                                    "realNameMasked": "张*",
                                    "idCardMasked": "330***********1234",
                                    "alipayOneTapEnabled": true
                                  },
                                  "membership": {
                                    "active": true,
                                    "level": 1,
                                    "expiresAt": "2026-09-30T12:00:00Z",
                                    "autoRenew": false,
                                    "remainingDays": 38
                                  },
                                  "capabilities": {
                                    "avatarRefresh": true,
                                    "regionSwitch": true,
                                    "accountSwitch": true,
                                    "accountDeletion": false,
                                    "phoneRebind": false,
                                    "healthCertification": true
                                  },
                                  "privacy": {
                                    "allowFriendRequests": true,
                                    "showGameRecord": true,
                                    "showOnlineStatus": true,
                                    "chatNotifications": true,
                                    "personalizedRecommendations": false
                                  }
                                }
                                """));

        assertEquals("VERIFIED", state.healthCertification().status());
        assertEquals("张*", state.healthCertification().realNameMasked());
        assertTrue(state.healthCertification().alipayOneTapEnabled());
        assertTrue(state.membership().active());
        assertEquals(1, state.membership().level());
        assertEquals(38L, state.membership().remainingDays());
        assertFalse(state.membership().autoRenew());
    }
}
