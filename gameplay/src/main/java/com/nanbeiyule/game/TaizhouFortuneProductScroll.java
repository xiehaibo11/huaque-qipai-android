package com.nanbeiyule.game;

/** Pure scroll geometry for CaiYunNewLayer's right-side product ScrollView. */
final class TaizhouFortuneProductScroll {
    static final float LEFT = 1_210.0f;
    static final float TOP = 160.0f;
    static final float RIGHT = 1_900.0f;
    static final float BOTTOM = 840.0f;
    static final float ROW_HEIGHT = 275.0f;

    private final int productCount;
    private final float maximumOffset;
    private float offset;

    TaizhouFortuneProductScroll(int productCount) {
        this.productCount = Math.max(0, productCount);
        int rows = (this.productCount + 2) / 3;
        maximumOffset = Math.max(0.0f, rows * ROW_HEIGHT - (BOTTOM - TOP));
    }

    float offset() {
        return offset;
    }

    void dragBy(float fingerDeltaY) {
        offset = Math.max(0.0f, Math.min(maximumOffset, offset - fingerDeltaY));
    }

    boolean contains(float x, float y) {
        return x >= LEFT && x <= RIGHT && y >= TOP && y <= BOTTOM;
    }

    int productAt(float x, float y) {
        if (!contains(x, y) || productCount == 0) {
            return -1;
        }
        int column = (int) ((x - 1_220.0f) / 220.0f);
        int row = (int) ((y - 165.0f + offset) / ROW_HEIGHT);
        int index = row * 3 + column;
        return column >= 0 && column < 3 && index >= 0 && index < productCount
                ? index
                : -1;
    }
}
