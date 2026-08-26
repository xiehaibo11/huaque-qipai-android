package com.nanbeiyule.game;

/** 原版 {@code MahjongNew/GameLayer/CSB/BigWinLost.csb} 的 1920×1080 几何。 */
final class TaizhouTotalResultLayout {
    static final float DESIGN_WIDTH = 1920.0f;
    static final float DESIGN_HEIGHT = 1080.0f;

    record Node(String name, float centerX, float centerY, float width, float height) {
        float left() {
            return centerX - width / 2.0f;
        }

        float top() {
            return centerY - height / 2.0f;
        }

        float right() {
            return centerX + width / 2.0f;
        }

        float bottom() {
            return centerY + height / 2.0f;
        }

        boolean contains(float x, float y) {
            return x >= left() && x <= right() && y >= top() && y <= bottom();
        }
    }

    static final Node TITLE = cocos("KW_IMG_TITLE", 960.768f, 1080.0f - 117.0f / 2.0f,
            892.0f, 117.0f);
    static final Node TITLE_LEFT = cocos("KW_IMG_TITLE_L", 205.5f, 1081.0f - 44.0f,
            413.0f, 88.0f);
    static final Node TITLE_RIGHT = cocos("KW_IMG_TITLE_R", 1713.5f, 1080.0f - 44.0f,
            413.0f, 88.0f);
    static final Node TOP_BACK = cocos("_KW_BTN_BACK", 133.0f, 1049.46f, 266.0f, 121.0f);
    static final Node ROOM_INFO = cocos("KW_IMG_ROOM_INFO", 264.5f, 971.88f, 529.0f, 91.0f);

    /** 续桌隐藏后，Lua updateBtnsPosition() 把两个可见按钮均分到 640/1280。 */
    static final Node BUTTON_BACK_LOBBY = cocos("_KW_BTN_BACK_LOBBY", 640.0f, 88.0f,
            387.0f, 132.0f);
    static final Node BUTTON_SHARE = cocos("_KW_BTN_SHARE", 1280.0f, 88.0f,
            387.0f, 131.0f);

    static final float PLAYER_CENTER_Y = DESIGN_HEIGHT - 540.0f;
    static final float PLAYER_WIDTH = 480.0f;
    static final float PLAYER_HEIGHT = 706.0f;
    static final float PLAYER_FIRST_CENTER_X = 240.0f;
    static final float PLAYER_STEP_X = 480.0f;

    static float playerCenterX(int index) {
        return PLAYER_FIRST_CENTER_X + index * PLAYER_STEP_X;
    }

    private TaizhouTotalResultLayout() {}

    private static Node cocos(String name, float x, float cocosY, float width, float height) {
        return new Node(name, x, DESIGN_HEIGHT - cocosY, width, height);
    }
}
