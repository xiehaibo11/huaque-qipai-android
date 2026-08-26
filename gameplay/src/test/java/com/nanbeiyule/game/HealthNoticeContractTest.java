package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HealthNoticeContractTest {
    private static final float EPSILON = 0.01f;

    @Test
    public void panelStaysInsideSafeAreaOnEveryRequiredScreenShape() {
        float[][] screens = {
            {1600f, 900f},   // 16:9
            {1800f, 900f},   // 18:9
            {1950f, 900f},   // 19.5:9
            {2000f, 900f},   // 20:9
            {1920f, 1200f},  // tablet
            {2200f, 1800f}   // unfolded foldable
        };
        AdaptiveViewport.Insets insets = new AdaptiveViewport.Insets(96f, 24f, 72f, 80f);

        for (float[] screen : screens) {
            AdaptiveViewport viewport =
                    AdaptiveViewport.create(
                            screen[0], screen[1], 1920f, 1080f, insets);
            AdaptiveViewport.Rect rendered =
                    HealthNoticeLayout.panelTransform(viewport)
                            .map(
                                    new AdaptiveViewport.Rect(
                                            0f,
                                            0f,
                                            HealthNoticeLayout.PANEL_WIDTH,
                                            HealthNoticeLayout.PANEL_HEIGHT));
            AdaptiveViewport.Rect safe = viewport.safeViewportRect();

            assertTrue(rendered.left() >= safe.left() - EPSILON);
            assertTrue(rendered.top() >= safe.top() - EPSILON);
            assertTrue(rendered.right() <= safe.right() + EPSILON);
            assertTrue(rendered.bottom() <= safe.bottom() + EPSILON);
        }
    }

    @Test
    public void longContentDragScrollsAndClampsAtBothEnds() {
        HealthNoticeScrollState state =
                new HealthNoticeScrollState(1258f, HealthNoticeLayout.CONTENT_VIEWPORT_HEIGHT);

        state.moveByFingerDelta(-240f);
        assertEquals(240f, state.offset(), EPSILON);

        state.moveByFingerDelta(-10_000f);
        assertEquals(1258f - HealthNoticeLayout.CONTENT_VIEWPORT_HEIGHT, state.offset(), EPSILON);

        state.moveByFingerDelta(10_000f);
        assertEquals(0f, state.offset(), EPSILON);
    }

    @Test
    public void accessibilityCopyContainsTheRecoveredNoticeRatherThanPlaceholderText() {
        String content = HealthNoticeContent.accessibilityText();

        assertTrue(content.contains("公司始终为用户提供公平公正、健康绿色的游戏环境"));
        assertTrue(content.contains("不良信息举报公告内容"));
        assertTrue(content.contains("在线客服"));
    }
}
