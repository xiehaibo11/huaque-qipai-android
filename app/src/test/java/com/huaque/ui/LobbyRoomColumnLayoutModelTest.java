package com.huaque.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LobbyRoomColumnLayoutModelTest {
    @Test
    public void alignsRoomColumnToTheExistingTwoRowGameGrid() {
        LobbyRoomColumnLayoutModel.CardSpec friend =
                LobbyRoomColumnLayoutModel.FRIEND;
        LobbyRoomColumnLayoutModel.CardSpec create =
                LobbyRoomColumnLayoutModel.CREATE;
        LobbyRoomColumnLayoutModel.CardSpec join =
                LobbyRoomColumnLayoutModel.JOIN;

        assertEquals(243, friend.bottom());
        assertEquals(270, create.top());
        assertEquals(613, join.bottom());
        assertEquals(create.height(), join.height());
        assertEquals(27, create.top() - friend.bottom());
        assertEquals(27, join.top() - create.bottom());
    }

    @Test
    public void fitsEverySourceInsideItsSlotWithoutChangingAspectRatio() {
        LobbyRoomColumnLayoutModel.FittedSize fitted =
                LobbyRoomColumnLayoutModel.fitCenter(308f, 158f, 393f, 222f);

        assertTrue(fitted.width() <= 308f);
        assertTrue(fitted.height() <= 158f);
        assertEquals(393f / 222f, fitted.width() / fitted.height(), 0.001f);
        assertEquals(158f, fitted.height(), 0.001f);
    }
}
