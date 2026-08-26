package com.nanbeiyule.game;

final class OriginalLobbyTapEffectFrame {
    private OriginalLobbyTapEffectFrame() {}

    static float[] toCanvasMesh(
            float[] spineVertices,
            float centerX,
            float centerY,
            float scale,
            boolean atlasRegionRotated) {
        if (spineVertices.length != 8) {
            throw new IllegalArgumentException("Tap effect attachment must be a quad");
        }
        float[] canvas = new float[8];
        for (int index = 0; index < 4; index++) {
            canvas[index * 2] = centerX + spineVertices[index * 2] * scale;
            canvas[index * 2 + 1] = centerY - spineVertices[index * 2 + 1] * scale;
        }
        int[] order = atlasRegionRotated
                ? new int[] {2, 3, 1, 0}
                : new int[] {1, 2, 0, 3};
        float[] mesh = new float[8];
        for (int index = 0; index < order.length; index++) {
            int source = order[index] * 2;
            mesh[index * 2] = canvas[source];
            mesh[index * 2 + 1] = canvas[source + 1];
        }
        return mesh;
    }

    static boolean isFinished(float elapsedSeconds, float durationSeconds) {
        return elapsedSeconds >= durationSeconds;
    }
}
