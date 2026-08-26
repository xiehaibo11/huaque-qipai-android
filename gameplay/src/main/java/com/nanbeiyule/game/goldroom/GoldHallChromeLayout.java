package com.nanbeiyule.game.goldroom;

/**
 * Gold-hall chrome geometry from {@code NewGoldHall/GoldLayer.csb} and {@code QuickStartBtn.csb},
 * in 1920x1080 design pixels with a top-left origin.
 *
 * <p>The header lives inside {@code _panelLT}, a 800x150 panel anchored (0,0.5) at Cocos (26,999),
 * so its local origin is Cocos (26,924); every child below is already folded into design space.
 * Verified against the 1.5.4 device screenshot: the gold icon centre lands on screen x 534.0 at a
 * 1766/1920 scale, matching the measured 534.0.
 */
public final class GoldHallChromeLayout {
    /** {@code _btnBack}, 96x96 centred at design (53,81). */
    public static final float BACK_CENTER_X = 53.0f;

    public static final float BACK_CENTER_Y = 81.0f;
    public static final float BACK_SIZE = 96.0f;

    /** {@code _txtCurChooseGameName}: anchored left, centre y 81. */
    public static final float TITLE_LEFT = 122.7f;

    public static final float TITLE_CENTER_Y = 81.0f;
    public static final float TITLE_TEXT_SIZE = 56.0f;

    /** {@code _btnCurGameRule}, the "?" button: 77x77 centred at design (406,81). */
    public static final float RULE_CENTER_X = 406.0f;

    public static final float RULE_CENTER_Y = 81.0f;
    public static final float RULE_SIZE = 77.0f;

    /** {@code _panelGold} / {@code _panelDiamond}: 242x58 pills, top edge at design y 52. */
    public static final float PILL_WIDTH = 242.0f;

    public static final float PILL_HEIGHT = 58.0f;
    public static final float PILL_TOP = 52.0f;
    public static final float GOLD_PILL_LEFT = 575.628f;
    public static final float DIAMOND_PILL_LEFT = 853.869f;

    /** {@code imgGold} inside its pill: 73x73 centred at pill-local (4.94,26.97) in Cocos. */
    public static final float GOLD_ICON_CENTER_X = GOLD_PILL_LEFT + 4.94f;

    public static final float GOLD_ICON_CENTER_Y = PILL_TOP + PILL_HEIGHT - 26.97f;
    public static final float GOLD_ICON_WIDTH = 73.0f;
    public static final float GOLD_ICON_HEIGHT = 73.0f;

    /** {@code imgDiamond}: 92x71 centred at pill-local (12.94,26.97). */
    public static final float DIAMOND_ICON_CENTER_X = DIAMOND_PILL_LEFT + 12.94f;

    public static final float DIAMOND_ICON_CENTER_Y = PILL_TOP + PILL_HEIGHT - 26.97f;
    public static final float DIAMOND_ICON_WIDTH = 92.0f;
    public static final float DIAMOND_ICON_HEIGHT = 71.0f;

    /** {@code _txtGoldNum} / {@code _txtDiamondNum}: 164x46 centred inside the pill. */
    public static final float GOLD_TEXT_CENTER_X = GOLD_PILL_LEFT + 140.36f;

    public static final float DIAMOND_TEXT_CENTER_X = DIAMOND_PILL_LEFT + 142.78f;
    public static final float WALLET_TEXT_CENTER_Y = PILL_TOP + PILL_HEIGHT - 29.0f;
    public static final float WALLET_TEXT_SIZE = 42.0f;

    /** {@code _panelAddGold/_panelAddDiamond Image_16}: {@code Img_JH2.png}, 42x43. */
    public static final float PLUS_ICON_WIDTH = 42.0f;

    public static final float PLUS_ICON_HEIGHT = 43.0f;
    public static final float GOLD_PLUS_CENTER_X = GOLD_PILL_LEFT - 12.0f + 53.1997f;
    public static final float GOLD_PLUS_CENTER_Y = PILL_TOP + PILL_HEIGHT - 14.4145f;
    public static final float DIAMOND_PLUS_CENTER_X = DIAMOND_PILL_LEFT - 12.0f + 57.2658f;
    public static final float DIAMOND_PLUS_CENTER_Y = PILL_TOP + PILL_HEIGHT - 16.118f;

    /** {@code _menuBarTopAct}: 640x120 activity strip, right edge at design x 1806. */
    public static final float ACT_STRIP_RIGHT = 1806.0f;

    public static final float ACT_STRIP_TOP = 22.0f;
    public static final float ACT_STRIP_WIDTH = 640.0f;
    public static final float ACT_STRIP_HEIGHT = 120.0f;
    /**
     * Each activity button is a 160x160 {@code Layer} per its own CSB. The icon box, trim
     * correction, red-dot geometry and hit testing live on {@link GoldHallActEntry}.
     */
    public static final float ACT_BUTTON_SIZE = 160.0f;
    /** {@code LocalConfig.MENU_BAR_CFG.CHOOSEROOM_TOP_ACT.layoutParam.dtSize.x = -150}. */
    public static final float ACT_BUTTON_STEP = 150.0f;
    public static final float ACT_BUTTON_CENTER_Y = ACT_STRIP_TOP + ACT_BUTTON_SIZE / 2.0f;

    /** {@code _menuBarTopBtns}: the 100x100 settings slot, right edge at design x 1900. */
    public static final float GEAR_CENTER_X = 1850.0f;

    public static final float GEAR_CENTER_Y = 102.0f;
    public static final float GEAR_SLOT_SIZE = 100.0f;
    /** {@code Btn_sz.png} native size inside the slot. */
    public static final float GEAR_ICON_WIDTH = 80.0f;

    public static final float GEAR_ICON_HEIGHT = 81.0f;

    /** {@code _posQuickStart}: 430x180 anchored (1,0) at Cocos (1866,10). */
    public static final float QUICK_START_LEFT = 1436.0f;

    public static final float QUICK_START_TOP = 890.0f;
    public static final float QUICK_START_WIDTH = 430.0f;
    public static final float QUICK_START_HEIGHT = 180.0f;

    /**
     * {@code QuickStartBtn.csb} places a 430x150 layer at the container's bottom, so its own top
     * sits 30 below the container top.
     */
    public static final float QUICK_START_BUTTON_TOP = QUICK_START_TOP + 30.0f;

    public static final float QUICK_START_BUTTON_HEIGHT = 150.0f;

    /** {@code _imgBg} = Btn_anniu.png 431x173 centred at button-local (215,75). */
    public static final float QUICK_START_BG_CENTER_X = QUICK_START_LEFT + 215.0f;

    public static final float QUICK_START_BG_CENTER_Y =
            QUICK_START_BUTTON_TOP + QUICK_START_BUTTON_HEIGHT - 75.0f;
    public static final float QUICK_START_BG_WIDTH = 431.0f;
    public static final float QUICK_START_BG_HEIGHT = 173.0f;

    /** {@code Image_1} = Img_KS.png 289x88 centred at button-local (215,109.6). */
    public static final float QUICK_START_LABEL_CENTER_X = QUICK_START_LEFT + 215.0f;

    public static final float QUICK_START_LABEL_CENTER_Y =
            QUICK_START_BUTTON_TOP + QUICK_START_BUTTON_HEIGHT - 109.6028f;
    public static final float QUICK_START_LABEL_WIDTH = 289.0f;
    public static final float QUICK_START_LABEL_HEIGHT = 88.0f;

    /** {@code _txtGameName}: the "<玩法> <档位>" line under the label. */
    public static final float QUICK_START_NAME_CENTER_X = QUICK_START_LEFT + 215.5f;

    public static final float QUICK_START_NAME_CENTER_Y =
            QUICK_START_BUTTON_TOP + QUICK_START_BUTTON_HEIGHT - 40.0f;
    public static final float QUICK_START_NAME_TEXT_SIZE = 34.0f;

    private GoldHallChromeLayout() {}

    /** True when the design-space point falls inside the back button's touch box. */
    public static boolean backContains(float designX, float designY) {
        return centred(designX, designY, BACK_CENTER_X, BACK_CENTER_Y, BACK_SIZE, BACK_SIZE);
    }

    /** True when the point falls inside the "?" rule button. */
    public static boolean ruleContains(float designX, float designY) {
        return centred(designX, designY, RULE_CENTER_X, RULE_CENTER_Y, RULE_SIZE, RULE_SIZE);
    }

    /** True when the point falls inside the settings slot. */
    public static boolean gearContains(float designX, float designY) {
        return centred(
                designX, designY, GEAR_CENTER_X, GEAR_CENTER_Y, GEAR_SLOT_SIZE, GEAR_SLOT_SIZE);
    }

    /** True when the point falls inside the quick-start button. */
    public static boolean quickStartContains(float designX, float designY) {
        return designX >= QUICK_START_LEFT
                && designX <= QUICK_START_LEFT + QUICK_START_WIDTH
                && designY >= QUICK_START_BUTTON_TOP
                && designY <= QUICK_START_BUTTON_TOP + QUICK_START_BUTTON_HEIGHT;
    }

    /** Centre x of the {@code index}-th visible choose-room top activity button. */
    public static float activityButtonCenterX(int index, int count) {
        return ACT_STRIP_RIGHT
                - ACT_BUTTON_SIZE / 2.0f
                - ACT_BUTTON_STEP * (count - 1 - index);
    }

    private static boolean centred(
            float x, float y, float centerX, float centerY, float width, float height) {
        return x >= centerX - width / 2.0f
                && x <= centerX + width / 2.0f
                && y >= centerY - height / 2.0f
                && y <= centerY + height / 2.0f;
    }
}
