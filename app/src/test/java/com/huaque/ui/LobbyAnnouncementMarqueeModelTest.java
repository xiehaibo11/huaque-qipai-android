package com.huaque.ui;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;

public final class LobbyAnnouncementMarqueeModelTest {
    @Test
    public void movesFromRightToLeftAtTheOriginalDesignSpeed() {
        LobbyAnnouncementMarqueeModel model = new LobbyAnnouncementMarqueeModel();
        model.setMessages(List.of("第一条公告"), 1_000f);

        model.advance(1f, 300f, 1_000f);

        assertEquals("第一条公告", model.currentMessage());
        assertEquals(890f, model.currentX(), 0.001f);
    }

    @Test
    public void switchesInOrderOnlyAfterTheCurrentTextFullyLeavesTheLeftEdge() {
        LobbyAnnouncementMarqueeModel model = new LobbyAnnouncementMarqueeModel();
        model.setMessages(List.of("第一条", "第二条"), 1_000f);

        model.advance(12f, 300f, 1_000f);
        assertEquals("第一条", model.currentMessage());
        assertEquals(-320f, model.currentX(), 0.001f);

        model.advance(0f, 300f, 1_000f);
        assertEquals("第二条", model.currentMessage());
        assertEquals(1_000f, model.currentX(), 0.001f);
    }

    @Test
    public void emptyServerResultDoesNotInventClientAnnouncementText() {
        LobbyAnnouncementMarqueeModel model = new LobbyAnnouncementMarqueeModel();
        model.setMessages(List.of(), 1_000f);

        model.advance(5f, 300f, 1_000f);

        assertEquals("", model.currentMessage());
    }
}
