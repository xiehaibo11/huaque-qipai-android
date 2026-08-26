package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class LobbySharePromptLayoutTest {
    @Test
    public void originalPromptButtonsUseTheCsdTouchRegions() {
        assertEquals(
                LobbySharePromptLayout.Target.CONFIRM,
                LobbySharePromptLayout.targetAt(960f, 760f, false));
        assertEquals(
                LobbySharePromptLayout.Target.CLOSE,
                LobbySharePromptLayout.targetAt(1480f, 235f, false));
        assertEquals(
                LobbySharePromptLayout.Target.NONE,
                LobbySharePromptLayout.targetAt(700f, 760f, false));
    }

    @Test
    public void copyLinkRegionOnlyExistsAfterWechatCannotOpen() {
        assertEquals(
                LobbySharePromptLayout.Target.NONE,
                LobbySharePromptLayout.targetAt(1280f, 760f, false));
        assertEquals(
                LobbySharePromptLayout.Target.COPY,
                LobbySharePromptLayout.targetAt(1280f, 760f, true));
    }
}
