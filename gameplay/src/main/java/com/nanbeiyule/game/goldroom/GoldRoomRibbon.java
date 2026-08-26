package com.nanbeiyule.game.goldroom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A parsed colour ribbon (原版 {@code Tag.CR}) shown under a choose-room card.
 *
 * <p>Ported from {@code ChooseRoom.lua createItemTagUI}. The configured string is split on
 * {@code #}: the first segment is the ribbon skin index ({@code Img_cd_<type>.png}), every later
 * segment is one inline node. A node is {@code RRGGBB_文本}; a segment without an underscore is
 * plain white text. The literals {@code zs} and {@code jb} become the diamond and gold icons
 * instead of text. See android/docs/ORIGINAL-GOLD-CHOOSE-ROOM-EVIDENCE.md.
 */
public final class GoldRoomRibbon {
    /** Inline icon names recognised by the original {@code IMG_CR} table. */
    public static final String ICON_DIAMOND = "zs";

    public static final String ICON_GOLD = "jb";

    /** Text outline colours per ribbon type, {@code IMG_CFG.crTxtOutLineColor}. */
    private static final int[] OUTLINE_COLORS = {
        0xFF458D40, 0xFF4B69A6, 0xFF584BA6, 0xFFBD7F1E, 0xFFD04C1D, 0xFF55568D
    };

    private static final int DEFAULT_TYPE = 1;
    private static final int DEFAULT_TEXT_COLOR = 0xFFFFFFFF;
    /** Every inline node sits at local Y 42 inside the 429x78 ribbon. */
    public static final float NODE_CENTER_Y = 42.0f;

    /** One inline node: either text (with colour) or one of the two currency icons. */
    public record Segment(String text, int color, boolean diamondIcon, boolean goldIcon) {
        public boolean isIcon() {
            return diamondIcon || goldIcon;
        }
    }

    private final int type;
    private final List<Segment> segments;

    private GoldRoomRibbon(int type, List<Segment> segments) {
        this.type = type;
        this.segments = segments;
    }

    /** Ribbon skin index; drives {@code Img_cd_<type>.png} and the text outline colour. */
    public int type() {
        return type;
    }

    public List<Segment> segments() {
        return segments;
    }

    /** {@code IMG_CFG.crTxtOutLineColor[crType]}, clamped to the configured table. */
    public int outlineColor() {
        int index = type - 1;
        if (index < 0 || index >= OUTLINE_COLORS.length) {
            index = 0;
        }
        return OUTLINE_COLORS[index];
    }

    /** Returns null when the slot has no ribbon configured, matching the hidden original node. */
    public static GoldRoomRibbon parse(String configured) {
        if (configured == null || configured.isEmpty()) {
            return null;
        }
        String[] parts = configured.split("#", -1);
        int type = DEFAULT_TYPE;
        try {
            type = Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException ignored) {
            // 原版 tonumber(infos[1]) or 1：解析失败退回第一套绶带皮肤。
        }
        List<Segment> segments = new ArrayList<>();
        for (int index = 1; index < parts.length; index++) {
            Segment segment = parseSegment(parts[index]);
            if (segment != null) {
                segments.add(segment);
            }
        }
        if (segments.isEmpty()) {
            return null;
        }
        return new GoldRoomRibbon(type, Collections.unmodifiableList(segments));
    }

    private static Segment parseSegment(String raw) {
        if (raw.isEmpty()) {
            return null;
        }
        String[] fields = raw.split("_", -1);
        String content;
        int color;
        if (fields.length == 1) {
            content = fields[0];
            color = DEFAULT_TEXT_COLOR;
        } else {
            content = fields[1];
            color = parseColor(fields[0]);
        }
        if (content.isEmpty()) {
            return null;
        }
        if (ICON_DIAMOND.equals(content)) {
            return new Segment(content, color, true, false);
        }
        if (ICON_GOLD.equals(content)) {
            return new Segment(content, color, false, true);
        }
        return new Segment(content, color, false, false);
    }

    private static int parseColor(String hex) {
        if (hex.length() < 6) {
            return DEFAULT_TEXT_COLOR;
        }
        try {
            int red = Integer.parseInt(hex.substring(0, 2), 16);
            int green = Integer.parseInt(hex.substring(2, 4), 16);
            int blue = Integer.parseInt(hex.substring(4, 6), 16);
            return 0xFF000000 | (red << 16) | (green << 8) | blue;
        } catch (NumberFormatException ignored) {
            // 原版每段颜色都有 "or 255" 兜底，解析失败按白色处理。
            return DEFAULT_TEXT_COLOR;
        }
    }
}
