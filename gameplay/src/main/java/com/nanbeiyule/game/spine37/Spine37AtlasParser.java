package com.nanbeiyule.game.spine37;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Spine37AtlasParser {
    private Spine37AtlasParser() {}

    public static Spine37Atlas parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Atlas text is empty");
        }
        String[] lines = text.replace("\r", "").split("\n", -1);
        List<Spine37Atlas.Page> pages = new ArrayList<>();
        Map<String, Spine37Atlas.Region> regions = new LinkedHashMap<>();
        String currentPage = null;
        int currentPageWidth = 0;
        int currentPageHeight = 0;
        boolean afterBlank = true;
        int index = 0;
        while (index < lines.length) {
            String raw = lines[index];
            String line = raw.trim();
            if (line.isEmpty()) {
                afterBlank = true;
                index++;
                continue;
            }
            if (raw.startsWith(" ") || raw.startsWith("\t")) {
                throw new IllegalArgumentException("Unexpected atlas property: " + raw);
            }
            if (afterBlank || currentPage == null) {
                if (!line.endsWith(".png")) {
                    throw new IllegalArgumentException("Atlas page must be a PNG: " + line);
                }
                if (currentPage != null) {
                    pages.add(
                            new Spine37Atlas.Page(
                                    currentPage,
                                    currentPageWidth,
                                    currentPageHeight));
                }
                currentPage = line;
                currentPageWidth = 0;
                currentPageHeight = 0;
                afterBlank = false;
                index++;
                while (index < lines.length && isPageProperty(lines[index])) {
                    Property property = property(lines[index]);
                    if ("size".equals(property.name())) {
                        int[] size = pair(property.value());
                        currentPageWidth = size[0];
                        currentPageHeight = size[1];
                    }
                    index++;
                }
                continue;
            }

            String regionName = line;
            boolean rotated = false;
            int x = 0;
            int y = 0;
            int width = 0;
            int height = 0;
            int originalWidth = 0;
            int originalHeight = 0;
            int offsetX = 0;
            int offsetY = 0;
            index++;
            while (index < lines.length && isIndentedProperty(lines[index])) {
                Property property = property(lines[index]);
                switch (property.name()) {
                    case "rotate" -> rotated = Boolean.parseBoolean(property.value());
                    case "xy" -> {
                        int[] pair = pair(property.value());
                        x = pair[0];
                        y = pair[1];
                    }
                    case "size" -> {
                        int[] pair = pair(property.value());
                        width = pair[0];
                        height = pair[1];
                    }
                    case "orig" -> {
                        int[] pair = pair(property.value());
                        originalWidth = pair[0];
                        originalHeight = pair[1];
                    }
                    case "offset" -> {
                        int[] pair = pair(property.value());
                        offsetX = pair[0];
                        offsetY = pair[1];
                    }
                    default -> {
                        // Index and future optional metadata do not change UVs.
                    }
                }
                index++;
            }
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("Atlas region has no size: " + regionName);
            }
            regions.put(
                    regionName,
                    new Spine37Atlas.Region(
                            regionName,
                            currentPage,
                            rotated,
                            x,
                            y,
                            width,
                            height,
                            originalWidth == 0 ? width : originalWidth,
                            originalHeight == 0 ? height : originalHeight,
                            offsetX,
                            offsetY));
            afterBlank = false;
        }
        if (currentPage != null) {
            pages.add(
                    new Spine37Atlas.Page(
                            currentPage,
                            currentPageWidth,
                            currentPageHeight));
        }
        if (pages.isEmpty() || regions.isEmpty()) {
            throw new IllegalArgumentException("Atlas contains no pages or regions");
        }
        return new Spine37Atlas(pages, regions);
    }

    private static boolean isIndentedProperty(String line) {
        return (line.startsWith(" ") || line.startsWith("\t")) && line.contains(":");
    }

    private static boolean isPageProperty(String raw) {
        String line = raw.trim();
        return line.startsWith("size:")
                || line.startsWith("format:")
                || line.startsWith("filter:")
                || line.startsWith("repeat:")
                || line.startsWith("pma:");
    }

    private static Property property(String raw) {
        String line = raw.trim();
        int separator = line.indexOf(':');
        if (separator <= 0) {
            throw new IllegalArgumentException("Invalid atlas property: " + raw);
        }
        return new Property(
                line.substring(0, separator).trim(),
                line.substring(separator + 1).trim());
    }

    private static int[] pair(String value) {
        String[] parts = value.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Expected atlas coordinate pair: " + value);
        }
        return new int[] {
            Integer.parseInt(parts[0].trim()),
            Integer.parseInt(parts[1].trim())
        };
    }

    private record Property(String name, String value) {}
}
