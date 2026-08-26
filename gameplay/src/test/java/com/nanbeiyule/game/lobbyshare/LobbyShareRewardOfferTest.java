package com.nanbeiyule.game.lobbyshare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class LobbyShareRewardOfferTest {
    @Test
    public void absentOfferNeverClaimsAReward() {
        LobbyShareRewardOffer offer = LobbyShareRewardOffer.none();

        assertFalse(offer.hasReward());
        assertEquals("", offer.rewardLabel());
    }

    @Test
    public void serverDiamondCountDrivesTheVisibleReward() {
        LobbyShareRewardOffer offer = LobbyShareRewardOffer.diamondFromServer(100);

        assertTrue(offer.hasReward());
        assertEquals("x100", offer.rewardLabel());
    }

    @Test
    public void invalidServerRewardIsRejectedInsteadOfInvented() {
        assertThrows(
                IllegalArgumentException.class,
                () -> LobbyShareRewardOffer.diamondFromServer(0));
    }
}
