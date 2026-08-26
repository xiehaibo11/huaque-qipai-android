package com.nanbeiyule.game;

public final class GameRecordLayout {
    public static final float DESIGN_WIDTH = 1920f;
    public static final float DESIGN_HEIGHT = 1080f;

    public static final Box BACK = new Box(20f, 0f, 125f, 105f);
    public static final Box GOLD_TAB = new Box(1346f, 35f, 260f, 80f);
    public static final Box BATTLE_TAB = new Box(1606f, 35f, 277f, 80f);
    public static final Box DATE = new Box(180f, 130f, 265f, 80f);
    public static final Box GAME = new Box(455f, 130f, 365f, 80f);
    public static final Box TOTAL = new Box(1635f, 130f, 160f, 80f);
    public static final Box REFRESH = new Box(1795f, 130f, 120f, 80f);
    public static final Box MEMBER = new Box(745f, 604f, 430f, 165f);
    public static final Box REPLAY = new Box(1640f, 985f, 235f, 90f);

    private GameRecordLayout() {}

    public record Box(float left, float top, float width, float height) {
        public boolean contains(float x, float y) {
            return x >= left && x <= left + width && y >= top && y <= top + height;
        }
    }
}
