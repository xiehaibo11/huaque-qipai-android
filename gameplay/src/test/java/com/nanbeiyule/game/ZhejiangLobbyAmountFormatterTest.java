package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ZhejiangLobbyAmountFormatterTest {
    @Test
    public void preservesTwoMeaningfulDecimalsForTenThousandBalances() {
        assertEquals("87.69万", ZhejiangLobbyAmountFormatter.format(876_900L));
        assertEquals("100.03万", ZhejiangLobbyAmountFormatter.format(1_000_300L));
    }

    @Test
    public void removesOnlyInsignificantDecimals() {
        assertEquals("2万", ZhejiangLobbyAmountFormatter.format(20_000L));
        assertEquals("6666", ZhejiangLobbyAmountFormatter.format(6_666L));
        assertEquals("0", ZhejiangLobbyAmountFormatter.format(0L));
    }
}
