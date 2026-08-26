package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class FreeDrawProtocolTest {
    @Test
    public void parsesTheServerAuthoritativePoolAndRemainingCount() throws Exception {
        FreeDrawState state =
                FreeDrawProtocol.parseState(
                        """
                        {
                          "activityCode":"DAILY_AD_DRAW",
                          "adPlacementId":"b5f8ceca962d11",
                          "dailyLimit":8,
                          "completedDraws":2,
                          "remainingDraws":6,
                          "prizes":[
                            {"prizeId":"p1","type":"COIN","amount":588,
                             "displayName":"588金币","iconKey":"coin_bag"},
                            {"prizeId":"p2","type":"DIAMOND","amount":10,
                             "displayName":"10钻石","iconKey":"diamond"}
                          ],
                          "serverTime":"2026-08-25T12:00:00Z"
                        }
                        """);

        assertEquals("b5f8ceca962d11", state.adPlacementId());
        assertEquals(6, state.remainingDraws());
        assertEquals(2, state.prizes().size());
        assertEquals(588L, state.prizes().get(0).amount());
    }

    @Test
    public void parsesTheServerIssuedAdSession() throws Exception {
        FreeDrawSession session =
                FreeDrawProtocol.parseSession(
                        """
                        {"sessionId":"27d6a2e5-696c-437c-8fd7-b4177c32f721",
                         "userCustomData":"27d6a2e5-696c-437c-8fd7-b4177c32f721",
                         "adPlacementId":"b5f8ceca962d11",
                         "expiresAt":"2026-08-25T12:10:00Z"}
                        """);

        assertEquals(session.sessionId(), session.userCustomData());
        assertEquals("b5f8ceca962d11", session.adPlacementId());
    }

    @Test
    public void parsesAnIdempotentRewardAndUpdatedWallet() throws Exception {
        FreeDrawResult result =
                FreeDrawProtocol.parseResult(
                        """
                        {"sessionId":"27d6a2e5-696c-437c-8fd7-b4177c32f721",
                         "replayed":false,
                         "reward":{"prizeId":"p1","type":"COIN","amount":588,
                                   "displayName":"588金币","iconKey":"coin_bag"},
                         "remainingDraws":5,
                         "wallet":{"roomCards":3,"boundRoomCards":1,"coins":1588,"diamonds":20}}
                        """);

        assertFalse(result.replayed());
        assertEquals("588金币", result.reward().displayName());
        assertEquals(1588L, result.wallet().coins());
        assertEquals(5, result.remainingDraws());
    }

    @Test
    public void displaysTheServerRemainingCountLikeTheOriginalClient() {
        assertEquals("（7次）", FreeDrawPanelOverlay.displayCount(7));
    }
}
