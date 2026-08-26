package com.huaque.ui;

final class LobbyRoomColumnLayoutModel {
    static final int PSD_X = 1947;
    static final int PSD_Y = 235;
    static final int PSD_WIDTH = 393;
    static final int PSD_HEIGHT = 613;

    static final CardSpec FRIEND = new CardSpec(0, 243);
    static final CardSpec CREATE = new CardSpec(270, 158);
    static final CardSpec JOIN = new CardSpec(455, 158);

    private LobbyRoomColumnLayoutModel() {
    }

    static FittedSize fitCenter(
            float availableWidth,
            float availableHeight,
            float sourceWidth,
            float sourceHeight) {
        float scale = Math.min(
                availableWidth / sourceWidth,
                availableHeight / sourceHeight);
        return new FittedSize(sourceWidth * scale, sourceHeight * scale);
    }

    record CardSpec(int top, int height) {
        int bottom() {
            return top + height;
        }
    }

    record FittedSize(float width, float height) {
    }
}
