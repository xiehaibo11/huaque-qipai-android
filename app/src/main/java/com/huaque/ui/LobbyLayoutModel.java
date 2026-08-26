package com.huaque.ui;

final class LobbyLayoutModel {
    private static final float PSD_WIDTH = 2448f;
    private static final float VIRTUAL_WIDTH = 1920f;

    private LobbyLayoutModel() {
    }

    static int x(int psdX) {
        return Math.round(psdX * VIRTUAL_WIDTH / PSD_WIDTH);
    }

    static int width(int psdWidth) {
        return Math.round(psdWidth * VIRTUAL_WIDTH / PSD_WIDTH);
    }
}
