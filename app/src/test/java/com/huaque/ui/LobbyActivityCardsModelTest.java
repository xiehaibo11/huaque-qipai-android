package com.huaque.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LobbyActivityCardsModelTest {
    @Test
    public void placesBothCardsInsideMarkedTopAreaWithoutOverlap() {
        LobbyActivityCardsModel.CardSpec[] cards = LobbyActivityCardsModel.specs();

        assertEquals(2, cards.length);
        for (LobbyActivityCardsModel.CardSpec card : cards) {
            assertTrue(card.x() >= 598);
            assertTrue(card.y() >= 16);
            assertTrue(card.x() + card.width() <= 1000);
            assertTrue(card.y() + card.height() <= 114);
        }
        assertTrue(cards[0].x() + cards[0].width() <= cards[1].x());
    }

    @Test
    public void cropsBothCardsWithinTheCombinedSourceBitmap() {
        for (LobbyActivityCardsModel.CardSpec card : LobbyActivityCardsModel.specs()) {
            assertTrue(card.sourceX() >= 0);
            assertTrue(card.sourceY() >= 0);
            assertTrue(card.sourceX() + card.sourceWidth() <= 226);
            assertTrue(card.sourceY() + card.sourceHeight() <= 336);
        }
    }
}
