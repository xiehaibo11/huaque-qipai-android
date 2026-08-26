package com.nanbeiyule.game;

import com.nanbeiyule.game.goldroom.GoldChooseRoomLayout;

/** Background fills the window while interactive content keeps its original proportions. */
final class GoldChooseRoomViewport {
    static final float BACKDROP_WIDTH = 2340.0f;

    private GoldChooseRoomViewport() {}

    static Transform content(int width, int height) {
        float safeWidth = Math.max(1, width);
        float safeHeight = Math.max(1, height);
        float scale =
                Math.min(
                        safeWidth / GoldChooseRoomLayout.DESIGN_WIDTH,
                        safeHeight / GoldChooseRoomLayout.DESIGN_HEIGHT);
        return centered(safeWidth, safeHeight, scale);
    }

    static Transform backdrop(int width, int height) {
        float safeWidth = Math.max(1, width);
        float safeHeight = Math.max(1, height);
        float scale =
                Math.max(
                        safeWidth / BACKDROP_WIDTH,
                        safeHeight / GoldChooseRoomLayout.DESIGN_HEIGHT);
        return centered(safeWidth, safeHeight, scale);
    }

    private static Transform centered(float width, float height, float scale) {
        return new Transform(
                scale,
                (width - GoldChooseRoomLayout.DESIGN_WIDTH * scale) / 2.0f,
                (height - GoldChooseRoomLayout.DESIGN_HEIGHT * scale) / 2.0f);
    }

    record Transform(float scale, float offsetX, float offsetY) {
        float screenX(float designX) {
            return offsetX + designX * scale;
        }

        float screenY(float designY) {
            return offsetY + designY * scale;
        }

        float designX(float screenX) {
            return (screenX - offsetX) / scale;
        }

        float designY(float screenY) {
            return (screenY - offsetY) / scale;
        }
    }
}
