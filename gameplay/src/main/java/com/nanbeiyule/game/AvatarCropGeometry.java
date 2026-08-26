package com.nanbeiyule.game;

final class AvatarCropGeometry {
    record Crop(int left, int top, int size) {}

    private AvatarCropGeometry() {}

    static Crop centerSquare(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Image dimensions must be positive");
        }
        int size = Math.min(width, height);
        return new Crop((width - size) / 2, (height - size) / 2, size);
    }
}
