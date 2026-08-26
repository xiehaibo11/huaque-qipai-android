package com.nanbeiyule.game.goldroom;

import java.util.ArrayList;
import java.util.List;

/**
 * 把 {@link GoldHallGameRuleDocument} 的段落折成正文可视区内的行。
 *
 * <p>原版正文是 WebView 里的 HTML，换行由浏览器完成；这里用同样的「按可用宽度逐字折行」
 * 语义在原生侧复现，行距与字号取 {@link GoldHallGameRuleLayout} 的截图校准常量。
 * 测量函数由调用方注入，便于在不依赖 Android {@code Paint} 的情况下做契约测试。
 */
public final class GoldHallGameRuleTextLayout {
    /** 文本宽度测量；实现方通常包一层 {@code Paint#measureText}。 */
    public interface Measurer {
        float measure(String text, float textSize, boolean bold);
    }

    /** 一行排好版的文本及其在正文局部坐标里的左上位置。 */
    public static final class Line {
        private final String text;
        private final float x;
        private final float top;
        private final float textSize;
        private final boolean bold;

        Line(String text, float x, float top, float textSize, boolean bold) {
            this.text = text;
            this.x = x;
            this.top = top;
            this.textSize = textSize;
            this.bold = bold;
        }

        public String text() {
            return text;
        }

        /** 相对正文区左边的 x。 */
        public float x() {
            return x;
        }

        /** 相对正文内容顶端的 y，未减滚动量。 */
        public float top() {
            return top;
        }

        public float textSize() {
            return textSize;
        }

        public boolean bold() {
            return bold;
        }
    }

    private final List<Line> lines;
    private final float contentHeight;

    private GoldHallGameRuleTextLayout(List<Line> lines, float contentHeight) {
        this.lines = lines;
        this.contentHeight = contentHeight;
    }

    public List<Line> lines() {
        return lines;
    }

    /** 全部行占用的总高度，用于限制滚动范围。 */
    public float contentHeight() {
        return contentHeight;
    }

    /** 可滚动的最大偏移；内容短于可视区时为 0。 */
    public float maxScroll(float viewportHeight) {
        return Math.max(0.0f, contentHeight - viewportHeight);
    }

    /**
     * 折行。{@code availableWidth} 是正文区宽度减去左内边距后的可写宽度。
     */
    public static GoldHallGameRuleTextLayout wrap(
            GoldHallGameRuleDocument document, float availableWidth, Measurer measurer) {
        List<Line> lines = new ArrayList<>();
        float top = GoldHallGameRuleLayout.SCREENSHOT_CONTENT_PADDING_TOP;
        float x = GoldHallGameRuleLayout.SCREENSHOT_CONTENT_PADDING_LEFT;
        if (document == null) {
            return new GoldHallGameRuleTextLayout(lines, 0.0f);
        }
        for (GoldHallGameRuleDocument.Block block : document.blocks()) {
            boolean heading = block.type() == GoldHallGameRuleDocument.BlockType.HEADING;
            float textSize =
                    heading
                            ? GoldHallGameRuleLayout.SCREENSHOT_HEADING_TEXT_SIZE
                            : GoldHallGameRuleLayout.SCREENSHOT_BODY_TEXT_SIZE;
            for (String piece : splitLines(block.text())) {
                for (String wrapped : wrapOne(piece, availableWidth, textSize, heading, measurer)) {
                    lines.add(new Line(wrapped, x, top, textSize, heading));
                    top += GoldHallGameRuleLayout.SCREENSHOT_LINE_PITCH;
                }
            }
        }
        // top 已越过最后一行，末尾再留一个与顶端等宽的内边距。
        float height =
                lines.isEmpty()
                        ? 0.0f
                        : top + GoldHallGameRuleLayout.SCREENSHOT_CONTENT_PADDING_TOP;
        return new GoldHallGameRuleTextLayout(lines, height);
    }

    private static List<String> splitLines(String text) {
        List<String> pieces = new ArrayList<>();
        for (String piece : text.split("\n", -1)) {
            pieces.add(piece);
        }
        return pieces;
    }

    /** 按可用宽度逐字折行；CJK 没有词边界，原版浏览器同样是按字符断行。 */
    private static List<String> wrapOne(
            String text, float availableWidth, float textSize, boolean bold, Measurer measurer) {
        List<String> wrapped = new ArrayList<>();
        if (text.isEmpty()) {
            wrapped.add("");
            return wrapped;
        }
        StringBuilder current = new StringBuilder();
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            current.append(character);
            if (measurer.measure(current.toString(), textSize, bold) > availableWidth
                    && current.length() > 1) {
                current.deleteCharAt(current.length() - 1);
                wrapped.add(current.toString());
                current.setLength(0);
                current.append(character);
            }
        }
        if (current.length() > 0) {
            wrapped.add(current.toString());
        }
        return wrapped;
    }
}
