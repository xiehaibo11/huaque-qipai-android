package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WechatPublicContractTest {
    private static final float EPSILON = 0.01f;

    @Test
    public void taizhouConfigurationDrivesVisibleAndClipboardCopy() {
        WechatPublicModel model = WechatPublicModel.taizhou();

        assertEquals(900023L, model.lobbyId());
        assertEquals("台州休闲", model.clipboardText());
        assertEquals("【台州休闲】", model.displayName());
        assertTrue(model.notice().contains("关注官方微信公众号【台州休闲】"));
        assertTrue(model.notice().contains("最新官方活动"));
    }

    @Test
    public void openActionReflectsTheRealWechatInstallationCheck() {
        assertEquals(
                WechatPublicModel.OpenAction.OPEN_WECHAT,
                WechatPublicModel.openAction(true));
        assertEquals(
                WechatPublicModel.OpenAction.SHOW_NOT_INSTALLED,
                WechatPublicModel.openAction(false));
    }

    @Test
    public void panelStaysInsideSafeAreaOnEveryRequiredScreenShape() {
        float[][] screens = {
            {1600f, 900f}, {1800f, 900f}, {1950f, 900f},
            {2000f, 900f}, {1920f, 1200f}, {2200f, 1800f}
        };
        AdaptiveViewport.Insets insets = new AdaptiveViewport.Insets(96f, 24f, 72f, 80f);

        for (float[] screen : screens) {
            AdaptiveViewport viewport =
                    AdaptiveViewport.create(
                            screen[0], screen[1], 1920f, 1080f, insets);
            AdaptiveViewport.Rect rendered =
                    WechatPublicLayout.panelTransform(viewport)
                            .map(
                                    new AdaptiveViewport.Rect(
                                            0f,
                                            0f,
                                            WechatPublicLayout.PANEL_WIDTH,
                                            WechatPublicLayout.PANEL_HEIGHT));
            AdaptiveViewport.Rect safe = viewport.safeViewportRect();

            assertTrue(rendered.left() >= safe.left() - EPSILON);
            assertTrue(rendered.top() >= safe.top() - EPSILON);
            assertTrue(rendered.right() <= safe.right() + EPSILON);
            assertTrue(rendered.bottom() <= safe.bottom() + EPSILON);
        }
    }
}
