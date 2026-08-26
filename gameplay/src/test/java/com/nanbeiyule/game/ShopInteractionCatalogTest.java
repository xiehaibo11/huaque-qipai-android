package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;

public class ShopInteractionCatalogTest {
    @Test
    public void interactionUsesTheOriginalEmoticonAndChatVoiceTabs() {
        assertEquals(
                List.of(ShopInteractionSection.EMOTICON, ShopInteractionSection.CHAT_VOICE),
                ShopInteractionSection.ordered());
        assertEquals("prop_emoji", ShopInteractionSection.EMOTICON.id());
        assertEquals("表情包", ShopInteractionSection.EMOTICON.title());
        assertEquals("yuyin", ShopInteractionSection.CHAT_VOICE.id());
        assertEquals("聊天语音", ShopInteractionSection.CHAT_VOICE.title());
    }

    @Test
    public void chatVoiceTabShowsTheOriginalXiaoguVoicePack() {
        ShopCatalogState catalog =
                ShopOriginalCatalog.create()
                        .select(ShopCategory.INTERACTION)
                        .selectInteractionSection(ShopInteractionSection.CHAT_VOICE);

        assertEquals(
                List.of("CHAT_VOICE_XIAOGU_1_DAY"),
                catalog.selectedProducts().stream().map(ShopProduct::productCode).toList());
        assertEquals("小谷专属语音包1天", catalog.selectedProducts().get(0).displayName());
        assertEquals("yuyin", catalog.selectedProducts().get(0).section());
        assertEquals(100L, catalog.selectedProducts().get(0).priceMinor());
        assertEquals(1L, catalog.selectedProducts().get(0).rewardQuantity());
    }

    @Test
    public void interactionTabHitTestingCoversBothOriginalRows() {
        assertEquals(0, ShopRuntimeLayout.interactionSectionIndexAt(319f, 180f));
        assertEquals(1, ShopRuntimeLayout.interactionSectionIndexAt(319f, 400f));
        assertEquals(-1, ShopRuntimeLayout.interactionSectionIndexAt(500f, 400f));
    }
}
