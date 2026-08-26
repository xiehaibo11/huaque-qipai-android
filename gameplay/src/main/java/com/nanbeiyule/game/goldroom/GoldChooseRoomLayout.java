package com.nanbeiyule.game.goldroom;

/**
 * Original choose-room geometry, in 1920x1080 design pixels with a top-left origin.
 *
 * <p>Recovered from {@code NewGoldHall/ChooseRoom.csb} and the list arrangement in
 * {@code ChooseRoom.lua updateRoomList}. Cocos y values are converted once here, so renderers and
 * hit tests never mix the two origins. See
 * android/docs/ORIGINAL-GOLD-CHOOSE-ROOM-EVIDENCE.md.
 */
public final class GoldChooseRoomLayout {
    public static final float DESIGN_WIDTH = 1920.0f;
    public static final float DESIGN_HEIGHT = 1080.0f;

    /** {@code _list}: Cocos (0,540) size 1920x768 anchor (0,0.5). */
    public static final float LIST_LEFT = 0.0f;

    public static final float LIST_TOP = 156.0f;
    public static final float LIST_WIDTH = 1920.0f;
    public static final float LIST_HEIGHT = 768.0f;

    /** {@code _item} / {@code _itemSmall} template size. */
    public static final float ITEM_WIDTH = 433.0f;

    public static final float ITEM_HEIGHT = 630.0f;

    /**
     * {@code _list:getItemsMargin()}. Calibrated from the 1.5.4 device screenshot and confirmed by
     * the centring math, the middle card landing on design x 960, and the reprojected card pitch.
     */
    public static final float ITEM_MARGIN = 20.0f;

    /**
     * The horizontal list centres its items vertically, so a 630-tall item inside the 768-tall
     * list starts 69 below the list top.
     */
    public static final float ITEM_TOP = LIST_TOP + (LIST_HEIGHT - ITEM_HEIGHT) / 2.0f;

    public static final float ITEM_BOTTOM = ITEM_TOP + ITEM_HEIGHT;

    /** {@code _imgBg}: Cocos (216.5,640.5) anchor (0.5,1) size 486x572, item-local. */
    public static final float CARD_WIDTH = 486.0f;

    public static final float CARD_HEIGHT = 572.0f;
    public static final float CARD_CENTER_X = 216.5f;
    public static final float CARD_TOP = ITEM_HEIGHT - 640.5f;

    /** {@code _fontBaseScore}: Cocos (216.5,282.54) anchor centre, item-local. */
    public static final float BASE_SCORE_CENTER_X = 216.5f;

    public static final float BASE_SCORE_CENTER_Y = ITEM_HEIGHT - 282.54f;

    /** {@code _txtGoldLimit}: Cocos (409.685,151) anchor (1,0.5) size 252x42, item-local. */
    public static final float GOLD_LIMIT_RIGHT = 409.685f;

    public static final float GOLD_LIMIT_CENTER_Y = ITEM_HEIGHT - 151.0f;
    public static final float GOLD_LIMIT_WIDTH = 252.0f;
    public static final float GOLD_LIMIT_HEIGHT = 42.0f;

    /** {@code _imgRS}: Cocos (36.4586,150.381) size 24x35, item-local. */
    public static final float PLAYER_ICON_CENTER_X = 36.4586f;

    public static final float PLAYER_ICON_CENTER_Y = ITEM_HEIGHT - 150.381f;
    public static final float PLAYER_ICON_WIDTH = 24.0f;
    public static final float PLAYER_ICON_HEIGHT = 35.0f;

    /** {@code _panelPlayerCount}: Cocos (10,151) anchor (0,0.5) size 200x61, item-local. */
    public static final float PLAYER_COUNT_LEFT = 10.0f;

    public static final float PLAYER_COUNT_CENTER_Y = ITEM_HEIGHT - 151.0f;

    /** {@code _ani} (zzb_jbdt_xfsg) and {@code _aniSelect} (zzb_jbdt_tjxf), item-local. */
    public static final float AMBIENT_ANI_X = 216.5f;

    public static final float AMBIENT_ANI_Y = ITEM_HEIGHT - 317.0f;
    public static final float SELECT_ANI_X = 216.5f;
    public static final float SELECT_ANI_Y = ITEM_HEIGHT - 360.0f;
    public static final float SELECT_ANI_SCALE_X = 1.0f;
    public static final float SELECT_ANI_SCALE_Y = 0.82f;

    /** {@code _imgColorRibbon1/2}: Cocos (216.5,66) and (216.5,-18) size 429x78, item-local. */
    public static final float RIBBON_CENTER_X = 216.5f;

    public static final float RIBBON_WIDTH = 429.0f;
    public static final float RIBBON_HEIGHT = 78.0f;
    public static final float RIBBON_1_CENTER_Y = ITEM_HEIGHT - 66.0f;
    public static final float RIBBON_2_CENTER_Y = ITEM_HEIGHT + 18.0f;

    /** {@code _imgTagLT}: Cocos (6,650) anchor (0,1) size 179x63, item-local. */
    public static final float TAG_LT_LEFT = 6.0f;

    public static final float TAG_LT_TOP = ITEM_HEIGHT - 650.0f;
    public static final float TAG_LT_HEIGHT = 63.0f;

    /** {@code _imgTagRT}: Cocos (427.318,625.88) anchor (1,1) size 92x94, item-local. */
    public static final float TAG_RT_RIGHT = 427.318f;

    public static final float TAG_RT_TOP = ITEM_HEIGHT - 625.88f;
    public static final float TAG_RT_WIDTH = 92.0f;
    public static final float TAG_RT_HEIGHT = 94.0f;

    /** {@code imgHide}: Cocos (216,372) size 421x510, item-local. */
    public static final float HIDE_CENTER_X = 216.0f;

    public static final float HIDE_CENTER_Y = ITEM_HEIGHT - 372.0f;
    public static final float HIDE_WIDTH = 421.0f;
    public static final float HIDE_HEIGHT = 510.0f;

    private GoldChooseRoomLayout() {}

    /**
     * Leading padding produced by {@code updateRoomList}: it pushes one spacer of
     * {@code bigNum/2 - margin} and then the first item after another margin, so the row ends up
     * centred whenever {@code bigNum/2 > margin}.
     */
    public static float rowLeftPadding(int itemCount) {
        if (itemCount <= 0) {
            return 0.0f;
        }
        float bigNum =
                LIST_WIDTH - ITEM_WIDTH * itemCount - (itemCount - 1) * ITEM_MARGIN;
        if (bigNum / 2.0f > ITEM_MARGIN) {
            return bigNum / 2.0f;
        }
        return 0.0f;
    }

    /** Left edge of the {@code index}-th card in a row of {@code itemCount}. */
    public static float itemLeft(int index, int itemCount) {
        return LIST_LEFT + rowLeftPadding(itemCount) + index * (ITEM_WIDTH + ITEM_MARGIN);
    }

    /** Centre x of the {@code index}-th card. */
    public static float itemCenterX(int index, int itemCount) {
        return itemLeft(index, itemCount) + ITEM_WIDTH / 2.0f;
    }

    /** True when the design-space point falls inside the {@code index}-th card's touch box. */
    public static boolean itemContains(
            int index, int itemCount, float designX, float designY) {
        float left = itemLeft(index, itemCount);
        return designX >= left
                && designX <= left + ITEM_WIDTH
                && designY >= ITEM_TOP
                && designY <= ITEM_BOTTOM;
    }
}
