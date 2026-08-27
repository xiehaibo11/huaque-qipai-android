package com.nanbeiyule.game;

import com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout;

final class TaizhouRoomMessageLayout {
    static final float SPEAK_WIDTH = 292.0f;
    static final float SPEAK_HEIGHT = 128.0f;
    static final float TEXT_SIZE = 36.0f;
    static final int TEXT_COLOR = 0xFF9D613E;
    static final float TEXT_LEFT_PADDING = 10.0f;
    static final float TEXT_VERTICAL_PADDING = 40.0f;
    static final float TEXT_AREA_WIDTH = SPEAK_WIDTH - TEXT_LEFT_PADDING;
    static final float SCALE9_X = 140.0f;
    static final float SCALE9_Y = 42.0f;
    static final float SCALE9_WIDTH = 12.0f;
    static final float SCALE9_HEIGHT = 44.0f;
    static final long VISIBLE_MILLIS = 1_200L;

    private TaizhouRoomMessageLayout() {}

    static long visibleUntil(long nowMillis) {
        return nowMillis + VISIBLE_MILLIS;
    }

    static int backgroundResIdFor(int localSeat) {
        return switch (localSeat) {
            case TaizhouMahjongTableLayout.SEAT_LEFT,
                    TaizhouMahjongTableLayout.SEAT_BOTTOM -> R.drawable.taizhou_tool_chat_speak_1;
            case TaizhouMahjongTableLayout.SEAT_RIGHT -> R.drawable.taizhou_tool_chat_speak_2;
            case TaizhouMahjongTableLayout.SEAT_TOP -> R.drawable.taizhou_tool_chat_speak_3;
            default -> throw new IllegalArgumentException("unknown local seat " + localSeat);
        };
    }

    static Bubble bubbleFor(
            int localSeat, float headCenterX, float headCenterY, float requestedHeight) {
        Placement placement = placementFor(localSeat);
        float height = Math.max(SPEAK_HEIGHT, requestedHeight);
        float anchorX = headCenterX + placement.offsetX;
        float anchorY = headCenterY - placement.offsetY;
        return new Bubble(
                backgroundResIdFor(localSeat),
                anchorX - placement.anchorX * SPEAK_WIDTH,
                anchorY - (1.0f - placement.anchorY) * height,
                anchorX + (1.0f - placement.anchorX) * SPEAK_WIDTH,
                anchorY + placement.anchorY * height,
                placement.anchorX,
                placement.anchorY);
    }

    private static Placement placementFor(int localSeat) {
        return switch (localSeat) {
            case TaizhouMahjongTableLayout.SEAT_LEFT -> new Placement(34.0f, 10.0f, 0.0f, 0.0f);
            case TaizhouMahjongTableLayout.SEAT_BOTTOM -> new Placement(30.0f, 10.0f, 0.0f, 0.0f);
            case TaizhouMahjongTableLayout.SEAT_RIGHT -> new Placement(-54.0f, -24.0f, 1.0f, 0.0f);
            case TaizhouMahjongTableLayout.SEAT_TOP -> new Placement(-64.0f, 0.0f, 1.0f, 1.0f);
            default -> throw new IllegalArgumentException("unknown local seat " + localSeat);
        };
    }

    private record Placement(float offsetX, float offsetY, float anchorX, float anchorY) {}

    record Bubble(
            int backgroundResId,
            float left,
            float top,
            float right,
            float bottom,
            float anchorX,
            float anchorY) {
        float width() {
            return right - left;
        }

        float height() {
            return bottom - top;
        }
    }
}
