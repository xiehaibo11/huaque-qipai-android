package com.nanbeiyule.game;

/** Shared 270-degree projection used by every score-assistant sub-dialog. */
final class ScoreAssistantDialogOrientation {
    enum Surface { CREATE, ROUND, CONFIRM }

    private ScoreAssistantDialogOrientation() {}

    static float rotationDegrees(Surface ignored) { return -90f; }
    static float logicalWidth(float landscapeWidth, float landscapeHeight) {
        return landscapeHeight;
    }
    static float logicalHeight(float landscapeWidth, float landscapeHeight) {
        return landscapeWidth;
    }
    static float landscapeX(float portraitX, float portraitY) { return portraitY; }
    static float landscapeY(float portraitX, float portraitY, float logicalWidth) {
        return logicalWidth - portraitX;
    }
}
