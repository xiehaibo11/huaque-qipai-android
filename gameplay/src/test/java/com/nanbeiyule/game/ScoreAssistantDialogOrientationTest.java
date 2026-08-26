package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ScoreAssistantDialogOrientationTest {
    private static final float EPSILON = 0.01f;

    @Test
    public void everyScoreSubDialogUsesTheMainPortrait270DegreeCoordinateSpace() {
        for (ScoreAssistantDialogOrientation.Surface surface
                : ScoreAssistantDialogOrientation.Surface.values()) {
            assertEquals(-90f, ScoreAssistantDialogOrientation.rotationDegrees(surface), EPSILON);
            assertEquals(1080f,
                    ScoreAssistantDialogOrientation.logicalWidth(1920f, 1080f), EPSILON);
            assertEquals(1920f,
                    ScoreAssistantDialogOrientation.logicalHeight(1920f, 1080f), EPSILON);
        }
    }

    @Test
    public void portraitCoordinatesMapToTheSameLandscapeRotationAsMainScoreView() {
        assertEquals(0f, ScoreAssistantDialogOrientation.landscapeX(0f, 0f), EPSILON);
        assertEquals(1080f,
                ScoreAssistantDialogOrientation.landscapeY(0f, 0f, 1080f), EPSILON);
        assertEquals(1920f,
                ScoreAssistantDialogOrientation.landscapeX(0f, 1920f), EPSILON);
        assertEquals(0f,
                ScoreAssistantDialogOrientation.landscapeY(1080f, 0f, 1080f), EPSILON);
    }
}
