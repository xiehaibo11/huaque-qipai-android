package com.nanbeiyule.game.goldroom;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class GoldHallGameRuleLayoutTest {
    @Test
    public void ruleTextStaysBelowHeaderAndInsidePanel() {
        float headerBottom =
                GoldHallGameRuleLayout.HEADER_TOP + GoldHallGameRuleLayout.HEADER_HEIGHT;
        float contentBottom =
                GoldHallGameRuleLayout.CONTENT_TOP + GoldHallGameRuleLayout.CONTENT_HEIGHT;
        float panelBottom =
                GoldHallGameRuleLayout.PANEL_TOP + GoldHallGameRuleLayout.PANEL_HEIGHT;

        assertTrue(GoldHallGameRuleLayout.CONTENT_TOP >= headerBottom);
        assertTrue(contentBottom <= panelBottom);
        assertTrue(GoldHallGameRuleLayout.SCREENSHOT_CONTENT_PADDING_TOP > 0.0f);
    }
}
