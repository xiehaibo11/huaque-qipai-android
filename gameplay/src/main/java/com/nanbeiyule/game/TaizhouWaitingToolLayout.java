package com.nanbeiyule.game;

/** Original 1920x1080 CSB geometry shared by waiting-room tool views. */
final class TaizhouWaitingToolLayout {
    static final float DESIGN_WIDTH = 1920.0f;
    static final float DESIGN_HEIGHT = 1080.0f;

    static final Box CHAT_PANEL = new Box(1260.0f, 100.0f, 1920.0f, 980.0f);
    static final Box CHAT_CONTENT = new Box(1283.0f, 128.0f, 1813.0f, 948.0f);
    static final int CHAT_PANEL_BACKGROUND = R.drawable.taizhou_tool_chat_bg;
    static final int CHAT_CONTENT_BACKGROUND = R.drawable.taizhou_tool_chat_bg_2;
    static final float CHAT_QUICK_PHRASE_ROW_HEIGHT = 75.0f;
    static final float CHAT_QUICK_PHRASE_ITEM_LEFT = 1290.0f;
    static final float CHAT_QUICK_PHRASE_ITEM_WIDTH = 516.0f;
    static final float CHAT_QUICK_PHRASE_ITEM_HEIGHT = 75.0f;
    static final float CHAT_QUICK_PHRASE_TEXT_LEFT = 1300.0f;
    static final float CHAT_QUICK_PHRASE_TEXT_WIDTH = 516.0f;
    static final float CHAT_QUICK_PHRASE_TEXT_SIZE = 36.0f;
    static final int CHAT_QUICK_PHRASE_TEXT_COLOR = 0xFF9D613E;
    static final Box CHAT_TALK_TAB = centered(1877.0f, 209.0f, 86.0f, 175.0f);
    static final Box CHAT_EMOJI_TAB = centered(1868.0f, 388.0f, 86.0f, 175.0f);
    static final Box CHAT_RECORD_TAB = centered(1869.0f, 563.0f, 86.0f, 175.0f);

    static final Box RESERVATION_CONFIRM = centered(1186.0f, 705.0f, 301.0f, 131.0f);
    static final Box RESERVATION_CANCEL = centered(736.0f, 705.0f, 301.0f, 131.0f);

    static final Box FORTUNE_CLOSE = centered(1849.0f, 63.0f, 80.0f, 80.0f);
    static final Box FORTUNE_MINUS = centered(1515.0f, 990.0f, 90.0f, 90.0f);
    static final Box FORTUNE_ADD = centered(1717.0f, 990.0f, 90.0f, 90.0f);
    static final Box FORTUNE_BUY = new Box(1265.0f, 835.0f, 1870.0f, 1075.0f);

    static final Box CAISHEN_CLOSE = centered(1354.0f, 289.0f, 110.0f, 112.0f);
    static final Box CAISHEN_PRODUCT_AREA = new Box(690.0f, 360.0f, 1348.0f, 870.0f);

    private TaizhouWaitingToolLayout() {}

    static int quickPhraseAt(float x, float y, int count) {
        if (!CHAT_CONTENT.contains(x, y) || count <= 0) {
            return -1;
        }
        int index = (int) ((y - CHAT_CONTENT.top) / CHAT_QUICK_PHRASE_ROW_HEIGHT);
        return index >= 0 && index < count ? index : -1;
    }

    static int emojiAt(float x, float y, int count) {
        if (!CHAT_CONTENT.contains(x, y) || count <= 0) {
            return -1;
        }
        int column = Math.min(3, Math.max(0, (int) ((x - CHAT_CONTENT.left) / 128.75f)));
        int row = (int) ((y - CHAT_CONTENT.top) / 135.0f);
        int index = row * 4 + column;
        return index >= 0 && index < count ? index : -1;
    }

    static int fortuneProductAt(float x, float y, int count) {
        if (count <= 0 || x < 1210.0f || x > 1900.0f || y < 160.0f || y > 840.0f) {
            return -1;
        }
        int column = (int) ((x - 1220.0f) / 220.0f);
        int row = (int) ((y - 165.0f) / 275.0f);
        int index = row * 3 + column;
        return column >= 0 && column < 3 && index >= 0 && index < count ? index : -1;
    }

    static int caishenProductAt(float x, float y, int count) {
        if (!CAISHEN_PRODUCT_AREA.contains(x, y) || count <= 0) {
            return -1;
        }
        int column = (int) ((x - CAISHEN_PRODUCT_AREA.left) / 223.0f);
        return column >= 0 && column < count ? column : -1;
    }

    private static Box centered(float centerX, float centerY, float width, float height) {
        return new Box(
                centerX - width / 2.0f,
                centerY - height / 2.0f,
                centerX + width / 2.0f,
                centerY + height / 2.0f);
    }

    record Box(float left, float top, float right, float bottom) {
        float width() {
            return right - left;
        }

        float height() {
            return bottom - top;
        }

        boolean contains(float x, float y) {
            return x >= left && x <= right && y >= top && y <= bottom;
        }
    }
}
