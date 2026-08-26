package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FreeDrawRewardGateTest {
    @Test
    public void onlyTheVerifiedRewardCallbackCanStartAClaim() {
        FreeDrawRewardGate gate = new FreeDrawRewardGate();

        assertTrue(gate.begin());
        gate.onSessionOpened();
        gate.onAdShown();

        assertEquals(FreeDrawRewardGate.Action.CLAIM_REWARD, gate.onRewardVerified());
        assertTrue(gate.claimInFlight());
        assertEquals(FreeDrawRewardGate.Action.NONE, gate.onRewardVerified());
    }

    @Test
    public void closingAnAdWithoutRewardNeverAuthorizesAClaim() {
        FreeDrawRewardGate gate = new FreeDrawRewardGate();

        gate.begin();
        gate.onSessionOpened();
        gate.onAdShown();
        gate.onAdClosed();

        assertFalse(gate.claimInFlight());
        assertEquals(FreeDrawRewardGate.Action.NONE, gate.onRewardVerified());
        assertTrue(gate.begin());
    }

    @Test
    public void closingAfterRewardDoesNotCancelTheAuthoritativeClaim() {
        FreeDrawRewardGate gate = new FreeDrawRewardGate();

        gate.begin();
        gate.onSessionOpened();
        gate.onAdShown();
        gate.onRewardVerified();
        gate.onAdClosed();

        assertTrue(gate.claimInFlight());
        gate.complete();
        assertTrue(gate.begin());
    }
}
