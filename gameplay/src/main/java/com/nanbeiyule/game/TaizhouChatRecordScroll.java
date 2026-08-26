package com.nanbeiyule.game;

/** Pure scroll geometry for the original 530x820 chat-record ListView. */
final class TaizhouChatRecordScroll {
    static final float VIEW_TOP = 100.0f;
    static final float VIEW_BOTTOM = 920.0f;
    static final float ROW_TOP = 107.5f;
    static final float ROW_HEIGHT = 145.0f;

    private final int messageCount;
    private final float maximumOffset;
    private float offset;

    TaizhouChatRecordScroll(int messageCount) {
        this.messageCount = Math.max(0, messageCount);
        maximumOffset =
                Math.max(
                        0.0f,
                        this.messageCount * ROW_HEIGHT - (VIEW_BOTTOM - VIEW_TOP));
        offset = maximumOffset;
    }

    float offset() {
        return offset;
    }

    void dragBy(float fingerDeltaY) {
        offset = clamp(offset - fingerDeltaY, 0.0f, maximumOffset);
    }

    int messageAt(float y) {
        if (y < VIEW_TOP || y > VIEW_BOTTOM) {
            return -1;
        }
        int index = (int) ((y - ROW_TOP + offset) / ROW_HEIGHT);
        return index >= 0 && index < messageCount ? index : -1;
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
