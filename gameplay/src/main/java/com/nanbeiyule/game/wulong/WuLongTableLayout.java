package com.nanbeiyule.game.wulong;

import android.graphics.RectF;

/** Recovered BaseWuLong CSB coordinate system; Canvas and touch share this sole geometry source. */
public final class WuLongTableLayout {
    public static final int DESIGN_WIDTH = 1920;
    public static final int DESIGN_HEIGHT = 1080;
    /** BaseCardGame's direct component source face dimensions. */
    public static final float CARD_FACE_WIDTH = 232f;
    public static final float CARD_FACE_HEIGHT = 294f;
    public static final float BOTTOM_HAND_SCALE = 1f;
    public static final float SIDE_AND_TOP_HAND_SCALE = .6f;
    public static final float SELECT_ADD_Y = 50f;
    public static final int HORIZONTAL_HAND_SPACING = 63;
    public static final int VERTICAL_HAND_SPACING = 120;
    /** CardLayerConfig's 60×75 are out-card distances; base face 232×294 is scaled at 0.65. */
    public static final int OUT_CARD_SPACING_X = 60;
    public static final int OUT_CARD_SPACING_Y = 75;
    public static final float OUT_CARD_SCALE = .65f;
    public static final float OUT_CARD_WIDTH = CARD_FACE_WIDTH * OUT_CARD_SCALE;
    public static final float OUT_CARD_HEIGHT = CARD_FACE_HEIGHT * OUT_CARD_SCALE;
    // Direct CardLayer.csb / PlayerLayer.csb positions in Cocos left-bottom coordinates.
    private static final float[][] COCOS_HAND_ANCHORS = {{320, 600}, {960, 160}, {1600, 600}, {960, 985}};
    private static final float[][] COCOS_OUT_ANCHORS = {{500, 720}, {960, 480}, {1420, 720}, {960, 760}};
    private static final float[][] COCOS_PLAYER_ANCHORS = {{110, 770}, {960, 120}, {1810, 770}, {1920, 1000}};
    // NewGameLayer.csb _KW_PANEL_PLAYER_CLOCK_* + _KW_PLAYER_CLOCK_POS_* + BG(0,64).
    private static final float[][] COCOS_CLOCK_CENTERS = {{340, 665}, {390, 480}, {1565, 665}, {960, 795}};
    public static final float CLOCK_WIDTH = 122f;
    public static final float CLOCK_HEIGHT = 128f;

    private WuLongTableLayout() {}

    public static RectF scale(RectF source, float width, float height) {
        return new RectF(source.left * width / DESIGN_WIDTH, source.top * height / DESIGN_HEIGHT,
                source.right * width / DESIGN_WIDTH, source.bottom * height / DESIGN_HEIGHT);
    }

    /** Cocos panel (960,0), relative (-350,475), 312×136; converted once for Android. */
    public static ActionRect passButton() { return fromCocosCenter(610, 475, 312, 136); }
    /** Cocos panel (960,0), relative (200,475), 312×136; converted once for Android. */
    public static ActionRect playButton() { return fromCocosCenter(1160, 475, 312, 136); }
    // Waiting/result use the same recovered action panel positions; only command authority differs.
    public static ActionRect readyButton() { return playButton(); }
    public static ActionRect startButton() { return passButton(); }

    public record ActionRect(float left, float top, float width, float height) {
        public boolean contains(float x, float y) {
            return x >= left && x <= left + width && y >= top && y <= top + height;
        }
        public RectF asRectF() { return new RectF(left, top, left + width, top + height); }
    }

    /** Maps backend 1-based seats to BaseWuLong local seats: self=bottom, next=right. */
    public static int localSeatFor(int serverSeat, int mySeat) {
        return (Math.floorMod(serverSeat - mySeat, 4) + 1) % 4 + 1;
    }

    public static float[] handAnchor(int serverSeat, int mySeat) {
        return androidPoint(COCOS_HAND_ANCHORS[localSeatFor(serverSeat, mySeat) - 1]);
    }

    public static float[] outAnchor(int serverSeat, int mySeat) {
        return androidPoint(COCOS_OUT_ANCHORS[localSeatFor(serverSeat, mySeat) - 1]);
    }

    /** Original CardLayer out-position node center in Cocos coordinates; projection applies .65. */
    static float[] cocosOutAnchor(int serverSeat, int mySeat) {
        float[] source = COCOS_OUT_ANCHORS[localSeatFor(serverSeat, mySeat) - 1];
        return new float[] {source[0], source[1]};
    }

    /** Base Card uses the default .5 anchor; do not convert out-card positions a second way. */
    public static float outCardTop(int serverSeat, int mySeat) {
        float cocosY = COCOS_OUT_ANCHORS[localSeatFor(serverSeat, mySeat) - 1][1];
        return cocosToAndroidTop(cocosY, OUT_CARD_HEIGHT, .5f);
    }

    /** CardLayerConfig hand scale/spacing projected from Cocos once for both draw and touch. */
    public record CardBounds(float left, float top, float width, float height) {
        public RectF asRectF() { return new RectF(left, top, left + width, top + height); }
        public boolean contains(float x, float y) {
            return x >= left && x <= left + width && y >= top && y <= top + height;
        }
    }

    public static CardBounds handCardBounds(int serverSeat, int mySeat, int cardIndex, int cardCount,
            boolean selected) {
        int localSeat = localSeatFor(serverSeat, mySeat);
        float scale = localSeat == 2 ? BOTTOM_HAND_SCALE : SIDE_AND_TOP_HAND_SCALE;
        float width = CARD_FACE_WIDTH * scale;
        float height = CARD_FACE_HEIGHT * scale;
        float[] cocos = COCOS_HAND_ANCHORS[localSeat - 1];
        float cocosX;
        float cocosY;
        if (localSeat == 1 || localSeat == 3) {
            int perLine = 5;
            int lineCount = Math.max(1, (cardCount + perLine - 1) / perLine);
            int line = cardIndex / perLine;
            int inLine = cardIndex % perLine;
            int cardsThisLine = Math.min(perLine, cardCount - line * perLine);
            cocosX = cocos[0] - line * 80f + (lineCount - 1) * 80f / 2f;
            cocosY = cocos[1] + inLine * VERTICAL_HAND_SPACING
                    - ((cardsThisLine - 1) * VERTICAL_HAND_SPACING / 2f - 60f);
        } else {
            cocosX = cocos[0] + (cardCount - 1 - 2f * cardIndex) * HORIZONTAL_HAND_SPACING / 2f;
            cocosY = cocos[1];
        }
        return handCardBoundsAtCocosCenter(cocosX, cocosY, scale, selected);
    }

    /**
     * Converts one BaseCardGame ImageView center (default anchor .5,.5) into the single Android
     * design-space rectangle shared by Canvas drawing and hit testing.
     */
    static CardBounds handCardBoundsAtCocosCenter(float cocosX, float cocosY, float scale,
            boolean selected) {
        float width = CARD_FACE_WIDTH * scale;
        float height = CARD_FACE_HEIGHT * scale;
        float lift = selected ? SELECT_ADD_Y : 0f;
        float top = cocosToAndroidTop(cocosY, height, .5f) - lift;
        return new CardBounds(cocosX - width / 2f, top, width, height);
    }

    /** Package-visible direct CardLayer.csb Cocos anchor; callers must not invent a second one. */
    static float[] cocosHandAnchor(int serverSeat, int mySeat) {
        float[] source = COCOS_HAND_ANCHORS[localSeatFor(serverSeat, mySeat) - 1];
        return new float[] {source[0], source[1]};
    }

    public static float[] playerAnchor(int serverSeat, int mySeat) {
        return androidPoint(COCOS_PLAYER_ANCHORS[localSeatFor(serverSeat, mySeat) - 1]);
    }

    /** Exact NewGameLayer local clock background bounds, mapped through the same Cocos conversion. */
    public static ClockBounds clockBounds(int serverSeat, int mySeat) {
        float[] center = COCOS_CLOCK_CENTERS[localSeatFor(serverSeat, mySeat) - 1];
        return new ClockBounds(center[0] - CLOCK_WIDTH / 2f,
                cocosToAndroidTop(center[1], CLOCK_HEIGHT, .5f), CLOCK_WIDTH, CLOCK_HEIGHT);
    }

    public record ClockBounds(float left, float top, float width, float height) {
        public RectF asRectF() { return new RectF(left, top, left + width, top + height); }
    }

    /** The only Cocos-bottom to Android-top conversion used by this table. */
    public static float cocosToAndroidTop(float cocosY, float height, float anchorY) {
        return DESIGN_HEIGHT - cocosY - height * (1f - anchorY);
    }

    private static float[] androidPoint(float[] cocosPoint) {
        return new float[] {cocosPoint[0], cocosToAndroidTop(cocosPoint[1], 0f, 0f)};
    }

    private static ActionRect fromCocosCenter(float x, float y, float width, float height) {
        return new ActionRect(x - width / 2f, cocosToAndroidTop(y, height, .5f), width, height);
    }
}
