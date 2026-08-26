package com.nanbeiyule.game.spine37;

import java.util.List;
import java.util.Map;

public record Spine37Atlas(List<Page> pages, Map<String, Region> regions) {
    public Spine37Atlas {
        pages = List.copyOf(pages);
        regions = Map.copyOf(regions);
    }

    public record Page(String name, int width, int height) {}

    public record Region(
            String name,
            String pageName,
            boolean rotated,
            int x,
            int y,
            int width,
            int height,
            int originalWidth,
            int originalHeight,
            int offsetX,
            int offsetY) {
        public int packedWidth() {
            return rotated ? height : width;
        }

        public int packedHeight() {
            return rotated ? width : height;
        }
    }
}
