package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import org.junit.Test;

public final class TaizhouChatPanelLayoutTest {
    @Test
    public void chatPanelAndListMatchOriginalChatLayerCsb() {
        assertBox(1260.0f, 100.0f, 1920.0f, 980.0f, TaizhouWaitingToolLayout.CHAT_PANEL);
        assertBox(1283.0f, 128.0f, 1813.0f, 948.0f, TaizhouWaitingToolLayout.CHAT_CONTENT);
    }

    @Test
    public void quickPhraseHitRowsUseOriginalSeventyFivePointItems() {
        assertEquals(-1, TaizhouWaitingToolLayout.quickPhraseAt(1300.0f, 127.9f, 9));
        assertEquals(0, TaizhouWaitingToolLayout.quickPhraseAt(1300.0f, 128.0f, 9));
        assertEquals(0, TaizhouWaitingToolLayout.quickPhraseAt(1300.0f, 202.9f, 9));
        assertEquals(1, TaizhouWaitingToolLayout.quickPhraseAt(1300.0f, 203.0f, 9));
    }

    @Test
    public void chatRecordRowsUseOriginalListBounds() {
        TaizhouChatRecordScroll scroll = new TaizhouChatRecordScroll(3);

        assertEquals(0, scroll.messageAt(128.0f));
        assertEquals(0, scroll.messageAt(272.9f));
        assertEquals(1, scroll.messageAt(273.0f));
        assertEquals(2, scroll.messageAt(418.0f));
        assertEquals(-1, scroll.messageAt(948.1f));
    }

    @Test
    public void chatPanelResourcesAndTextStyleUseOriginalArtwork() throws Exception {
        assertEquals(
                R.drawable.taizhou_tool_chat_bg,
                intField(TaizhouWaitingToolLayout.class, "CHAT_PANEL_BACKGROUND"));
        assertEquals(
                R.drawable.taizhou_tool_chat_bg_2,
                intField(TaizhouWaitingToolLayout.class, "CHAT_CONTENT_BACKGROUND"));
        assertEquals(75.0f, floatField("CHAT_QUICK_PHRASE_ROW_HEIGHT"), 0.001f);
        assertEquals(36.0f, floatField("CHAT_QUICK_PHRASE_TEXT_SIZE"), 0.001f);
        assertEquals(
                (int) 0xFF9D613E,
                intField(TaizhouWaitingToolLayout.class, "CHAT_QUICK_PHRASE_TEXT_COLOR"));
    }

    private static void assertBox(
            float left, float top, float right, float bottom, TaizhouWaitingToolLayout.Box box) {
        assertEquals(left, box.left(), 0.001f);
        assertEquals(top, box.top(), 0.001f);
        assertEquals(right, box.right(), 0.001f);
        assertEquals(bottom, box.bottom(), 0.001f);
    }

    private static int intField(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(null);
    }

    private static float floatField(String name) throws Exception {
        Field field = TaizhouWaitingToolLayout.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getFloat(null);
    }
}
