package com.nanbeiyule.game;

/** Pixel geometry of the existing 2448 x 130 lobby top-control artwork. */
final class ZhejiangLobbyHeaderOverlayLayout {
    static final float DESIGN_WIDTH = 2448.0f;
    static final float DESIGN_HEIGHT = 130.0f;

    static final float COIN_VALUE_CENTER_X = 1448.0f;
    static final float DIAMOND_VALUE_CENTER_X = 1828.0f;
    static final float ROOM_CARD_VALUE_CENTER_X = 2193.0f;

    static final Box AVATAR_IMAGE = new Box(48, 19, 135, 106);
    static final Box AVATAR_CHROME = new Box(28, 4, 150, 120);
    static final Box PLAYER_PANEL = new Box(92, 18, 668, 108);
    static final Box PLAYER_NAME_PATCH = new Box(150, 22, 340, 64);
    static final Box PLAYER_ID_PATCH = new Box(150, 64, 340, 102);
    static final Box PLAYER_NAME_PATCH_SOURCE = new Box(415, 22, 430, 64);
    static final Box PLAYER_ID_PATCH_SOURCE = new Box(415, 64, 430, 102);

    static final Box COIN_VALUE_PATCH = new Box(1365, 31, 1534, 99);
    static final Box COIN_VALUE_PATCH_SOURCE = new Box(1355, 31, 1370, 99);
    static final Box DIAMOND_VALUE_PATCH = new Box(1765, 31, 1905, 99);
    static final Box DIAMOND_VALUE_PATCH_SOURCE = new Box(1735, 31, 1760, 99);
    static final Box ROOM_CARD_VALUE_PATCH = new Box(2148, 31, 2260, 99);
    static final Box ROOM_CARD_VALUE_PATCH_SOURCE = new Box(2140, 31, 2155, 99);

    static final Box COIN_ICON = new Box(1288, 43, 1355, 100);
    static final Box DIAMOND_ICON = new Box(1662, 43, 1729, 97);
    static final Box ROOM_CARD_ICON = new Box(2049, 40, 2135, 97);
    static final Box COIN_CONTROL = new Box(1280, 27, 1605, 105);
    static final Box DIAMOND_CONTROL = new Box(1655, 27, 1975, 105);
    static final Box ROOM_CARD_CONTROL = new Box(2038, 27, 2448, 105);
    static final float COIN_GLINT_CENTER_X = 1317.0f;
    static final float COIN_GLINT_CENTER_Y = 69.0f;
    static final float DIAMOND_GLINT_CENTER_X = 1691.0f;
    static final float DIAMOND_GLINT_CENTER_Y = 66.0f;
    static final float ROOM_CARD_GLINT_CENTER_X = 2091.0f;
    static final float ROOM_CARD_GLINT_CENTER_Y = 64.0f;

    private ZhejiangLobbyHeaderOverlayLayout() {}

    record Box(int left, int top, int right, int bottom) {}
}
