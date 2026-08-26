package com.nanbeiyule.game.mahjong;

/** Original 1920 x 1080 room-information geometry after Lua's two-row relayout. */
public final class TaizhouMahjongRoomInfoLayout {
    public static final Sprite SYSTEM_BACKGROUND =
            new Sprite("doublekou_sys_bg.png", 139.5f, 36.0f, 255.0f, 58.0f);
    public static final float TIME_CENTER_X = 74.2809f;
    public static final float TIME_CENTER_Y = 33.7624f;
    public static final Sprite WIFI =
            new Sprite("doublekouwifi_icon_3.png", 143.8637f, 33.3756f, 29.0f, 21.0f);
    public static final Sprite POWER_BACKGROUND =
            new Sprite("doublekou_power_bg.png", 212.1616f, 32.6315f, 53.0f, 28.0f);
    public static final Sprite POWER_BAR =
            new Sprite("doublekou_power_bar.png", 210.2859f, 32.2843f, 37.0f, 18.0f);

    public static final Sprite ROOM_BACKGROUND =
            new Sprite("doublekou_roomnum_bg.png", 139.7f, 117.0f, 255.0f, 104.0f);
    public static final float ROOM_ROW_CENTER_Y = 95.5f;
    public static final float REMAINING_ROW_CENTER_Y = 138.5f;
    public static final float KEY_RIGHT_X = 124.2f;
    public static final float COLON_CENTER_X = 127.2f;
    public static final float VALUE_LEFT_X = 139.2f;
    public static final float LEFT_TEXT_SIZE = 30.0f;
    public static final int LEFT_TEXT_COLOR = 0xffecc57b;

    public static final Sprite CENTER_ROOM_LABEL =
            new Sprite("mah_sp_roomnum.png", 960.0f, 440.0f, 149.0f, 54.0f);
    public static final float CENTER_ROOM_NUMBER_X = 960.0f;
    public static final float CENTER_ROOM_NUMBER_Y = 502.0f;
    public static final float CENTER_RULE_X = 960.0f;
    public static final float CENTER_RULE_CSB_Y = 250.0f;
    // The supplied original runtime screenshot places this node 50 px above its CSB value.
    public static final float CENTER_RULE_Y = 200.0f;
    public static final float CENTER_RULE_WIDTH = 1200.0f;
    public static final float CENTER_RULE_HEIGHT = 150.0f;
    public static final float CENTER_RULE_TEXT_SIZE = 42.0f;
    public static final int CENTER_RULE_COLOR = 0x80000000;
    // The supplied original runtime screenshot fills the otherwise empty center CSB hook.
    public static final float OK_GESTURE_CENTER_X = 960.0f;
    public static final float OK_GESTURE_CENTER_Y = 580.0f;
    public static final float OK_GESTURE_TEXT_SIZE = 105.0f;
    public static final Sprite HEALTH_GAME =
            new Sprite("jiankangyouxi.png", 960.0f, 615.0f, 214.0f, 25.0f);

    private TaizhouMahjongRoomInfoLayout() {}

    public static final class Sprite {
        public final String frameName;
        public final float centerX;
        public final float centerY;
        public final float width;
        public final float height;

        private Sprite(
                String frameName, float centerX, float centerY, float width, float height) {
            this.frameName = frameName;
            this.centerX = centerX;
            this.centerY = centerY;
            this.width = width;
            this.height = height;
        }
    }
}
