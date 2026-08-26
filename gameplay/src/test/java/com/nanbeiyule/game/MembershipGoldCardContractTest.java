package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;

public class MembershipGoldCardContractTest {
    private static final float EPSILON = 0.01f;

    @Test
    public void parsesTheServerAuthoritativeGoldCardsWithoutFallbackData() throws Exception {
        GoldMembershipCardsState state =
                GoldMembershipCardsState.fromJson(
                        new JSONObject(
                                """
                                {
                                  "cards":[
                                    {"productCode":"GOLD_MEMBER_WEEK","title":"金币周卡",
                                     "durationDays":7,"dailyCoins":10000,"state":"NOT_AWARD",
                                     "remainingSeconds":604799},
                                    {"productCode":"GOLD_MEMBER_MONTH","title":"金币月卡",
                                     "durationDays":30,"dailyCoins":15000,"state":"HAS_AWARD",
                                     "remainingSeconds":2591999}
                                  ]
                                }
                                """));

        assertEquals(2, state.cards().size());
        GoldMembershipCardsState.Card week = state.cards().get(0);
        assertEquals("GOLD_MEMBER_WEEK", week.productCode());
        assertEquals(7, week.durationDays());
        assertEquals(10_000L, week.dailyCoins());
        assertEquals(GoldMembershipCardsState.CardState.NOT_AWARD, week.state());
        assertEquals(604_799L, week.remainingSeconds());
        assertTrue(week.canClaim());
        assertFalse(state.cards().get(1).canClaim());
    }

    @Test
    public void parsesAClaimAsOneUpdatedCardAndMergesItByProductCode() throws Exception {
        GoldMembershipCardsState state =
                GoldMembershipCardsState.fromJson(
                        new JSONObject(
                                """
                                {"cards":[
                                  {"productCode":"GOLD_MEMBER_WEEK","title":"金币周卡",
                                   "durationDays":7,"dailyCoins":10000,"state":"NOT_AWARD",
                                   "remainingSeconds":604799},
                                  {"productCode":"GOLD_MEMBER_MONTH","title":"金币月卡",
                                   "durationDays":30,"dailyCoins":15000,"state":"NOT_ACTIVE",
                                   "remainingSeconds":0}
                                ]}
                                """));
        GoldMembershipCardsState.Card claimed =
                GoldMembershipCardsState.cardFromJson(
                        new JSONObject(
                                """
                                {"productCode":"GOLD_MEMBER_WEEK","title":"金币周卡",
                                 "durationDays":7,"dailyCoins":10000,"state":"HAS_AWARD",
                                 "remainingSeconds":604700}
                                """));

        GoldMembershipCardsState updated = state.withUpdatedCard(claimed);

        assertEquals(GoldMembershipCardsState.CardState.HAS_AWARD, updated.cards().get(0).state());
        assertEquals(GoldMembershipCardsState.CardState.NOT_ACTIVE, updated.cards().get(1).state());
    }

    @Test
    public void neverInventsTheMissingSecondCard() throws Exception {
        GoldMembershipCardsState state =
                GoldMembershipCardsState.fromJson(
                        new JSONObject(
                                """
                                {"cards":[{
                                  "productCode":"GOLD_MEMBER_WEEK","title":"金币周卡",
                                  "durationDays":7,"dailyCoins":10000,"state":"NOT_ACTIVE",
                                  "remainingSeconds":0
                                }]}
                                """));

        assertEquals(1, state.cards().size());
        assertFalse(state.cards().get(0).isActive());
    }

    @Test
    public void usesTheExactAuthenticatedGoldCardRoutes() {
        assertEquals("/api/v1/membership/gold-cards", GoldMembershipCardsApi.cardsPath());
        assertEquals(
                "/api/v1/membership/gold-cards/GOLD_MEMBER_WEEK/claim",
                GoldMembershipCardsApi.claimPath("GOLD_MEMBER_WEEK"));
    }

    @Test
    public void laysOutTwoOriginalSizeCardsLeftAlignedWithFiftyPixelGap() {
        GoldMembershipCardsLayout.Bounds list = GoldMembershipCardsLayout.welfareList();
        GoldMembershipCardsLayout.Bounds first = GoldMembershipCardsLayout.welfareCard(0);
        GoldMembershipCardsLayout.Bounds second = GoldMembershipCardsLayout.welfareCard(1);

        assertEquals(1206f, list.width(), EPSILON);
        assertEquals(750f, list.height(), EPSILON);
        assertEquals(list.left(), first.left(), EPSILON);
        assertEquals(400f, first.width(), EPSILON);
        assertEquals(740f, first.height(), EPSILON);
        assertEquals(first.right() + 50f, second.left(), EPSILON);
    }

    @Test
    public void mapsOriginalCardButtonsFromRealServerStates() {
        GoldMembershipCardsLayout.Bounds primary = GoldMembershipCardsLayout.primaryButton(0);
        GoldMembershipCardsLayout.Bounds renew = GoldMembershipCardsLayout.renewButton(0);

        assertEquals(
                GoldMembershipCardsPanel.Action.OPEN_SHOP,
                actionAt(card(GoldMembershipCardsState.CardState.NOT_ACTIVE), primary));
        assertEquals(
                GoldMembershipCardsPanel.Action.CLAIM,
                actionAt(card(GoldMembershipCardsState.CardState.NOT_AWARD), primary));
        assertEquals(
                GoldMembershipCardsPanel.Action.NONE,
                actionAt(card(GoldMembershipCardsState.CardState.HAS_AWARD), primary));
        assertEquals(
                GoldMembershipCardsPanel.Action.OPEN_SHOP,
                actionAt(card(GoldMembershipCardsState.CardState.NOT_AWARD), renew));
    }

    @Test
    public void keepsTheShaoxingDailyGiftContractIndependent() throws Exception {
        MembershipDailyGiftState legacy =
                MembershipDailyGiftState.fromJson(
                        new JSONObject(
                                """
                                {"membershipActive":true,"claimedToday":false,
                                 "options":[{"giftId":1,"title":"礼包",
                                  "buttonStyle":"red","rewards":[{
                                    "code":"COIN","displayName":"金币","quantity":10000
                                  }]}]}
                                """));

        assertTrue(legacy.membershipActive());
        assertEquals(1, legacy.options().size());
        assertEquals(10_000L, legacy.options().get(0).rewards().get(0).quantity());
    }

    private static GoldMembershipCardsPanel.Action actionAt(
            GoldMembershipCardsState.Card card, GoldMembershipCardsLayout.Bounds bounds) {
        GoldMembershipCardsState state = new GoldMembershipCardsState(java.util.List.of(card));
        return GoldMembershipCardsPanel.targetAt(
                        state,
                        false,
                        (bounds.left() + bounds.right()) * 0.5f,
                        (bounds.top() + bounds.bottom()) * 0.5f)
                .action();
    }

    private static GoldMembershipCardsState.Card card(
            GoldMembershipCardsState.CardState state) {
        return new GoldMembershipCardsState.Card(
                "GOLD_MEMBER_WEEK", "金币周卡", 7, 10_000L, state, 604_800L);
    }
}
