package com.nanbeiyule.game.mahjong;

/** Canvas positions for the original table-centre throw-chip display. */
public final class TaizhouDiceLayout {
    public record Node(float centerX, float centerY, float width, float height) {
        public float left() {
            return centerX - width / 2.0f;
        }

        public float top() {
            return centerY - height / 2.0f;
        }
    }

    public static final int MAX_ORIGINAL_DICE = 2;
    public static final float SPRITE_SCALE = 0.525f;
    public static final float SPRITE_WIDTH = 320.0f * SPRITE_SCALE;
    public static final float SPRITE_HEIGHT = 240.0f * SPRITE_SCALE;

    private static final float CENTER_X = TaizhouCenterClockLayout.ROOT.centerX();

    private static final float CENTER_Y = TaizhouCenterClockLayout.ROOT.centerY();
    private static final float SPACING = 110.0f;

    public static final Node SINGLE = slot(0.0f);
    public static final Node DOUBLE_LEFT = slot(-SPACING / 2.0f);
    public static final Node DOUBLE_RIGHT = slot(SPACING / 2.0f);

    private static Node slot(float offsetX) {
        return new Node(CENTER_X + offsetX, CENTER_Y, SPRITE_WIDTH, SPRITE_HEIGHT);
    }

    public static Node nodeFor(int count, int index) {
        if (count == 1) {
            return SINGLE;
        }
        if (count == 2) {
            return index == 0 ? DOUBLE_LEFT : DOUBLE_RIGHT;
        }
        throw new IllegalArgumentException("original dice layout only supports one or two dice");
    }

    private TaizhouDiceLayout() {}
}
