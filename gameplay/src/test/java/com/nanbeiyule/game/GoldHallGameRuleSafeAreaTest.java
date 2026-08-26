package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.goldroom.GoldHallGameRuleLayout;
import java.lang.reflect.Method;
import org.junit.Test;

public final class GoldHallGameRuleSafeAreaTest {
    private static final float EPSILON = 0.01f;

    @Test
    public void ruleDialogFitsRequiredScreensAndWindowInsets() throws Exception {
        assertTrue(AdaptiveCanvasView.class.isAssignableFrom(GoldHallGameRuleView.class));
        Method projection =
                GoldHallGameRuleView.class.getDeclaredMethod(
                        "projection",
                        float.class,
                        float.class,
                        AdaptiveViewport.Insets.class);
        projection.setAccessible(true);
        float[][] screens = {
            {1600f, 900f, 0f, 24f, 0f, 80f},
            {1800f, 900f, 0f, 24f, 0f, 48f},
            {1950f, 900f, 96f, 24f, 0f, 48f},
            {2000f, 900f, 72f, 24f, 72f, 48f},
            {1920f, 1200f, 24f, 24f, 24f, 80f},
            {2200f, 1800f, 24f, 24f, 24f, 80f},
            {1440f, 1080f, 24f, 24f, 24f, 48f}
        };

        for (float[] screen : screens) {
            AdaptiveViewport.Insets insets =
                    new AdaptiveViewport.Insets(
                            screen[2], screen[3], screen[4], screen[5]);
            AdaptiveViewport viewport =
                    AdaptiveViewport.create(
                            screen[0],
                            screen[1],
                            GoldHallGameRuleLayout.DESIGN_WIDTH,
                            GoldHallGameRuleLayout.DESIGN_HEIGHT,
                            insets);
            AdaptiveViewport.Transform transform =
                    (AdaptiveViewport.Transform)
                            projection.invoke(null, screen[0], screen[1], insets);
            AdaptiveViewport.Rect rendered =
                    transform.map(
                            new AdaptiveViewport.Rect(
                                    0f,
                                    0f,
                                    GoldHallGameRuleLayout.DESIGN_WIDTH,
                                    GoldHallGameRuleLayout.DESIGN_HEIGHT));
            AdaptiveViewport.Rect safe = viewport.safeViewportRect();

            assertTrue(transform.scaleX() > 0f);
            assertEquals(transform.scaleX(), transform.scaleY(), EPSILON);
            assertTrue(rendered.left() >= safe.left() - EPSILON);
            assertTrue(rendered.top() >= safe.top() - EPSILON);
            assertTrue(rendered.right() <= safe.right() + EPSILON);
            assertTrue(rendered.bottom() <= safe.bottom() + EPSILON);
            assertEquals(
                    GoldHallGameRuleLayout.DESIGN_WIDTH / 2f,
                    transform.unmapX(
                            transform.mapX(GoldHallGameRuleLayout.DESIGN_WIDTH / 2f)),
                    EPSILON);
            assertEquals(
                    GoldHallGameRuleLayout.DESIGN_HEIGHT / 2f,
                    transform.unmapY(
                            transform.mapY(GoldHallGameRuleLayout.DESIGN_HEIGHT / 2f)),
                    EPSILON);
        }
    }
}
