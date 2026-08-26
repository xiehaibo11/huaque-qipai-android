package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ScoreAssistantLayoutTest {
    private static final float EPSILON = 0.01f;

    @Test
    public void originalPortraitSurfaceRotatesInsideEveryRequiredSafeLandscape() {
        float[][] screens = {
            {1600f, 900f}, {1800f, 900f}, {1950f, 900f},
            {2000f, 900f}, {1920f, 1200f}, {2200f, 1800f}
        };
        AdaptiveViewport.Insets insets = new AdaptiveViewport.Insets(96f, 24f, 72f, 80f);

        for (float[] screen : screens) {
            AdaptiveViewport viewport = AdaptiveViewport.create(
                    screen[0], screen[1], 1920f, 1080f, insets);
            AdaptiveViewport.Rect rendered =
                    ScoreAssistantLayout.panelTransform(viewport)
                            .map(new AdaptiveViewport.Rect(
                                    0f,
                                    0f,
                                    ScoreAssistantLayout.DESIGN_HEIGHT,
                                    ScoreAssistantLayout.DESIGN_WIDTH));
            AdaptiveViewport.Rect safe = viewport.safeViewportRect();
            assertTrue(rendered.left() >= safe.left() - EPSILON);
            assertTrue(rendered.top() >= safe.top() - EPSILON);
            assertTrue(rendered.right() <= safe.right() + EPSILON);
            assertTrue(rendered.bottom() <= safe.bottom() + EPSILON);
        }
    }

    @Test
    public void recoveredCsdUsesPortraitGeometryAndExactBottomTabs() {
        assertEquals(1080f, ScoreAssistantLayout.DESIGN_WIDTH, EPSILON);
        assertEquals(1920f, ScoreAssistantLayout.DESIGN_HEIGHT, EPSILON);
        assertEquals(ScoreAssistantState.Tab.ACTIVE, ScoreAssistantLayout.tabAt(180f, 1828f));
        assertEquals(ScoreAssistantState.Tab.HISTORY, ScoreAssistantLayout.tabAt(540f, 1828f));
        assertEquals(ScoreAssistantState.Tab.MONTHLY, ScoreAssistantLayout.tabAt(900f, 1828f));
        assertEquals(null, ScoreAssistantLayout.tabAt(540f, 1700f));
        assertTrue(ScoreAssistantLayout.CLOSE.contains(994f, 119f));
        assertTrue(ScoreAssistantLayout.CREATE.contains(540f, 1507f));
    }

    @Test
    public void portraitTouchCoordinatesInvertTheOriginalRotation() {
        assertEquals(994f, ScoreAssistantLayout.logicalX(119f, 86f), EPSILON);
        assertEquals(119f, ScoreAssistantLayout.logicalY(119f, 86f), EPSILON);
    }

    @Test
    public void recordRowsRemainClippedAndScrollable() {
        assertEquals(0, ScoreAssistantLayout.cardAt(300f, 500f, 0f, 4));
        assertEquals(1, ScoreAssistantLayout.cardAt(300f, 800f, 0f, 4));
        assertEquals(-1, ScoreAssistantLayout.cardAt(300f, 1700f, 0f, 4));
        assertEquals(
                ScoreAssistantLayout.maxScroll(12),
                ScoreAssistantLayout.clampScroll(100_000f, 12),
                EPSILON);
    }
}
