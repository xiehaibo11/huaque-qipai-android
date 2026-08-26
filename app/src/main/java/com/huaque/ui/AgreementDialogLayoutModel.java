package com.huaque.ui;

final class AgreementDialogLayoutModel {
    private static final float ORIGINAL_CANVAS_WIDTH = 1334f;
    private static final float ORIGINAL_CANVAS_HEIGHT = 750f;
    private static final int ORIGINAL_DIALOG_WIDTH = 946;
    private static final int ORIGINAL_DIALOG_HEIGHT = 634;
    private static final int LEGACY_DIALOG_WIDTH = 1549;
    private static final int LEGACY_DIALOG_HEIGHT = 1023;

    private AgreementDialogLayoutModel() {
    }

    static int[] dialogBounds(int canvasWidth, int canvasHeight) {
        float scale = Math.min(
                canvasWidth / ORIGINAL_CANVAS_WIDTH,
                canvasHeight / ORIGINAL_CANVAS_HEIGHT);
        int width = Math.round(ORIGINAL_DIALOG_WIDTH * scale);
        int height = Math.round(ORIGINAL_DIALOG_HEIGHT * scale);
        return new int[]{
                Math.round((canvasWidth - width) / 2f),
                Math.round((canvasHeight - height) / 2f),
                width,
                height
        };
    }

    static int[] mapLegacyBox(
            int[] dialog,
            int x,
            int y,
            int width,
            int height
    ) {
        float scaleX = dialog[2] / (float) LEGACY_DIALOG_WIDTH;
        float scaleY = dialog[3] / (float) LEGACY_DIALOG_HEIGHT;
        return new int[]{
                dialog[0] + Math.round(x * scaleX),
                dialog[1] + Math.round(y * scaleY),
                Math.round(width * scaleX),
                Math.round(height * scaleY)
        };
    }
}
