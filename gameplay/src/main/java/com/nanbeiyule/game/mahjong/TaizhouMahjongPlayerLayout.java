package com.nanbeiyule.game.mahjong;

/** Player roots and head geometry recovered from PlayerLayer.csb and PlayerHeadBase.csb. */
public final class TaizhouMahjongPlayerLayout {
    public static final float HEAD_WIDTH = 105.0f;
    public static final float HEAD_HEIGHT = 106.0f;

    // HeadNode.lua places a (frame size - 7) RemoteImage at width / 2 - 0.5,
    // height / 2 + 0.3. Android design Y grows downward, hence -0.3 here.
    public static final float AVATAR_CENTER_OFFSET_X = -0.5f;
    public static final float AVATAR_CENTER_OFFSET_Y = -0.3f;
    public static final float AVATAR_WIDTH = 98.0f;
    public static final float AVATAR_HEIGHT = 99.0f;

    public static final float NICKNAME_CENTER_OFFSET_X = 0.0f;
    public static final float NICKNAME_CENTER_OFFSET_Y = 77.0f;
    public static final float NICKNAME_WIDTH = 104.0f;
    public static final float NICKNAME_HEIGHT = 26.0f;
    public static final float NICKNAME_FONT_SIZE = 26.0f;
    public static final int NICKNAME_RED = 251;
    public static final int NICKNAME_GREEN = 222;
    public static final int NICKNAME_BLUE = 115;

    public static final float SCORE_LEFT_OFFSET_X = -39.0f;
    public static final float SCORE_CENTER_OFFSET_Y = 110.0f;
    public static final float SCORE_WIDTH = 78.0f;
    public static final float SCORE_HEIGHT = 26.0f;
    public static final float SCORE_FONT_SIZE = 26.0f;
    public static final int SCORE_RED = 251;
    public static final int SCORE_GREEN = 222;
    public static final int SCORE_BLUE = 115;

    public static final float HOST_CENTER_OFFSET_X = 50.0f;
    public static final float HOST_CENTER_OFFSET_Y = 26.0f;
    public static final float HOST_WIDTH = 67.0f;
    public static final float HOST_HEIGHT = 71.0f;

    public static final float CHENG_BAO_SIZE = 43.2f;
    public static final float CHENG_BAO_CENTER_OFFSET_Y = 43.0f;

    public static float chengBaoCenterOffsetX(int localSeat) {
        return localSeat == TaizhouMahjongTableLayout.SEAT_RIGHT ? 37.5f : -42.5f;
    }

    private static final PlayerSlot[] PLAYER_SLOTS = {
        new PlayerSlot(TaizhouMahjongTableLayout.SEAT_LEFT, 80.0f, 295.0f),
        new PlayerSlot(TaizhouMahjongTableLayout.SEAT_BOTTOM, 80.0f, 720.0f),
        // BasicTaiZhouMahjong/Modules/Player/View.luac shifts this root left by 20.
        new PlayerSlot(TaizhouMahjongTableLayout.SEAT_RIGHT, 1835.0f, 295.0f),
        new PlayerSlot(TaizhouMahjongTableLayout.SEAT_TOP, 1455.0f, 80.0f),
    };

    private TaizhouMahjongPlayerLayout() {}

    public static PlayerSlot forLocalSeat(int localSeat) {
        if (localSeat < TaizhouMahjongTableLayout.SEAT_LEFT
                || localSeat > TaizhouMahjongTableLayout.SEAT_TOP) {
            throw new IllegalArgumentException("unknown local seat " + localSeat);
        }
        return PLAYER_SLOTS[localSeat - 1];
    }

    public record PlayerSlot(int localSeat, float centerX, float centerY) {}
}
