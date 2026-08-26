package com.huaque.ui;

final class LobbyActivityCardsModel {
    private LobbyActivityCardsModel() {
    }

    static CardSpec[] specs() {
        return new CardSpec[]{
                new CardSpec(0, 0, 226, 150, 645, 18, 144, 95),
                new CardSpec(0, 186, 226, 150, 809, 18, 144, 95)
        };
    }

    static final class CardSpec {
        private final int sourceX;
        private final int sourceY;
        private final int sourceWidth;
        private final int sourceHeight;
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        CardSpec(
                int sourceX,
                int sourceY,
                int sourceWidth,
                int sourceHeight,
                int x,
                int y,
                int width,
                int height) {
            this.sourceX = sourceX;
            this.sourceY = sourceY;
            this.sourceWidth = sourceWidth;
            this.sourceHeight = sourceHeight;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        int sourceX() {
            return sourceX;
        }

        int sourceY() {
            return sourceY;
        }

        int sourceWidth() {
            return sourceWidth;
        }

        int sourceHeight() {
            return sourceHeight;
        }

        int x() {
            return x;
        }

        int y() {
            return y;
        }

        int width() {
            return width;
        }

        int height() {
            return height;
        }
    }
}
