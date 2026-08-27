package com.nanbeiyule.game;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class TaizhouTipLayoutTest {
    @Test
    public void okAndCloseMatchShareTipsCsbGeometry() {
        // KW_BUTTON_HINT_OK：面板 (960,540) 内的 (394,90)，301×131 缩放 0.9。
        assertTrue(TaizhouTipLayout.okContains(960.0f, 704.5f));
        assertTrue(TaizhouTipLayout.okContains(960.0f - 134.0f, 704.5f));
        assertFalse(TaizhouTipLayout.okContains(960.0f, 640.0f));
        // KW_BUTTON_HINT_CLOSE：99×102，压在面板右上角。
        assertTrue(TaizhouTipLayout.closeContains(1334.5f, 316.5f));
        assertFalse(TaizhouTipLayout.closeContains(960.0f, 316.5f));
    }
}
