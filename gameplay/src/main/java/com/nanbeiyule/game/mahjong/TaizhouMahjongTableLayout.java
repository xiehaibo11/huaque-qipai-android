package com.nanbeiyule.game.mahjong;

/**
 * Original mahjong table geometry, recovered from
 * {@code MahjongNew/MahLayer/CSB/MahjongLayer.csb} (CocoStudio 2.1.0.0).
 *
 * <p>The original design space is 1920x1080 with Cocos' bottom-up Y axis. Every
 * value here is copied from the CSB node tree; nothing is estimated from a
 * screenshot. Android renderers consume {@link #designY(float)} to flip a Cocos
 * Y into a top-down canvas Y.
 *
 * <p>The adapt containers {@code _KW_ADAPT_MAH_1..4} are numbered by the engine's
 * {@code GameDefine.LOCAL_SEAT} enum (LEFT 1, BOTTOM 2, RIGHT 3, TOP 4), and
 * their anchors pin each one to the matching screen edge so the table adapts to
 * wider windows. {@code _KW_ADAPT_MAH_5} is the shared centre. A slot's design
 * position is therefore its panel origin plus its local offset.
 *
 * <p>The local player's hand does not live in the BOTTOM container: the original
 * puts it in the separate 1960x100 strip {@code _KW_ADAPT_HAND_MAH_2}, which is
 * why BOTTOM carries only a river and a flower row.
 */
public final class TaizhouMahjongTableLayout {
    /** Original design width in points. */
    public static final float DESIGN_WIDTH = 1920.0f;

    /** Original design height in points. */
    public static final float DESIGN_HEIGHT = 1080.0f;

    /** {@code LOCAL_SEAT.LEFT}, container {@code _KW_ADAPT_MAH_1}. */
    public static final int SEAT_LEFT = 1;

    /** {@code LOCAL_SEAT.BOTTOM}, container {@code _KW_ADAPT_MAH_2}: the local player. */
    public static final int SEAT_BOTTOM = 2;

    /** {@code LOCAL_SEAT.RIGHT}, container {@code _KW_ADAPT_MAH_3}. */
    public static final int SEAT_RIGHT = 3;

    /** {@code LOCAL_SEAT.TOP}, container {@code _KW_ADAPT_MAH_4}. */
    public static final int SEAT_TOP = 4;

    /** The shared centre container {@code _KW_ADAPT_MAH_5}. */
    public static final int CENTER = 5;

    /** The local player's hand strip {@code _KW_ADAPT_HAND_MAH_2}. */
    public static final int HAND_STRIP = 6;

    /** A container pinned to one edge of the design space. */
    public static final class Panel {
        public final String name;
        public final float positionX;
        public final float positionY;
        public final float width;
        public final float height;
        public final float anchorX;
        public final float anchorY;

        Panel(
                String name,
                float positionX,
                float positionY,
                float width,
                float height,
                float anchorX,
                float anchorY) {
            this.name = name;
            this.positionX = positionX;
            this.positionY = positionY;
            this.width = width;
            this.height = height;
            this.anchorX = anchorX;
            this.anchorY = anchorY;
        }

        /** Design-space X of this panel's local origin. */
        public float originX() {
            return positionX - anchorX * width;
        }

        /** Design-space Y (Cocos, bottom-up) of this panel's local origin. */
        public float originY() {
            return positionY - anchorY * height;
        }
    }

    /** A named child node inside one container. */
    public static final class Slot {
        public final String name;
        public final int seat;
        public final float localX;
        public final float localY;
        public final float scale;

        Slot(String name, int seat, float localX, float localY, float scale) {
            this.name = name;
            this.seat = seat;
            this.localX = localX;
            this.localY = localY;
            this.scale = scale;
        }

        /** Design-space X of this slot. */
        public float designX() {
            return panel(seat).originX() + localX;
        }

        /** Design-space Y of this slot in Cocos' bottom-up axis. */
        public float cocosY() {
            return panel(seat).originY() + localY;
        }

        /** Design-space Y of this slot in Android's top-down axis. */
        public float androidY() {
            return designY(cocosY());
        }
    }

    private static final Panel[] PANELS = {
        new Panel("_KW_ADAPT_MAH_1", 0.0f, 540.0f, 1920.0f, 1080.0f, 0.0f, 0.5f),
        new Panel("_KW_ADAPT_MAH_2", 960.0f, 0.0f, 1960.0f, 1080.0f, 0.5f, 0.0f),
        new Panel("_KW_ADAPT_MAH_3", 1920.0f, 540.0f, 1920.0f, 1080.0f, 1.0f, 0.5f),
        new Panel("_KW_ADAPT_MAH_4", 960.0f, 1080.0f, 1920.0f, 1080.0f, 0.5f, 1.0f),
        new Panel("_KW_ADAPT_MAH_5", 960.0f, 536.976f, 1920.0f, 1080.0f, 0.5f, 0.5f),
        new Panel("_KW_ADAPT_HAND_MAH_2", 960.0f, 0.0f, 1960.0f, 100.0f, 0.5f, 0.0f),
    };

    /** The local player's hand, melds and winning hand all share this root. */
    public static final Slot HAND_BOTTOM =
            new Slot("KW_HAND_MAH", HAND_STRIP, 25.0f, 10.0f, 1.0f);

    public static final Slot COMB_BOTTOM = new Slot("KW_COMB", HAND_STRIP, 25.0f, 10.0f, 0.85f);

    /** Opponent hand roots, {@code KW_HAND_MAH}. */
    public static final Slot HAND_LEFT = new Slot("KW_HAND_MAH", SEAT_LEFT, 280.0f, 1070.0f, 0.46f);

    public static final Slot HAND_RIGHT =
            new Slot("KW_HAND_MAH", SEAT_RIGHT, 1640.0f, 205.0f, 0.46f);

    public static final Slot HAND_TOP = new Slot("KW_HAND_MAH", SEAT_TOP, 1385.0f, 968.0f, 0.5f);

    /** Opponent meld roots, {@code KW_COMB}. */
    public static final Slot COMB_LEFT = new Slot("KW_COMB", SEAT_LEFT, 280.0f, 1070.0f, 0.46f);

    public static final Slot COMB_RIGHT = new Slot("KW_COMB", SEAT_RIGHT, 1640.0f, 205.0f, 0.46f);

    public static final Slot COMB_TOP = new Slot("KW_COMB", SEAT_TOP, 1385.0f, 968.0f, 0.5f);

    /** Discard-river roots, {@code KW_OUT_MAH}. */
    public static final Slot OUT_LEFT = new Slot("KW_OUT_MAH", SEAT_LEFT, 565.0f, 833.976f, 0.5f);

    public static final Slot OUT_BOTTOM =
            new Slot("KW_OUT_MAH", SEAT_BOTTOM, 704.032f, 395.0f, 0.5f);

    public static final Slot OUT_RIGHT = new Slot("KW_OUT_MAH", SEAT_RIGHT, 1350.0f, 324.0f, 0.5f);

    public static final Slot OUT_TOP = new Slot("KW_OUT_MAH", SEAT_TOP, 1219.968f, 700.0f, 0.5f);

    /** Big discard preview roots, {@code KW_PANEL_SHOW_OUT_MAH_1..4}. */
    public static final Slot SHOW_OUT_LEFT =
            new Slot("KW_PANEL_SHOW_OUT_MAH_1", CENTER, 380.0f, 540.0f, 1.0f);

    public static final Slot SHOW_OUT_BOTTOM =
            new Slot("KW_PANEL_SHOW_OUT_MAH_2", CENTER, 960.0f, 310.0f, 1.0f);

    public static final Slot SHOW_OUT_RIGHT =
            new Slot("KW_PANEL_SHOW_OUT_MAH_3", CENTER, 1540.0f, 540.0f, 1.0f);

    public static final Slot SHOW_OUT_TOP =
            new Slot("KW_PANEL_SHOW_OUT_MAH_4", CENTER, 960.0f, 783.0f, 1.0f);

    /** Secondary river roots {@code KW_OUT_MAH_2}, used by the two vertical seats. */
    public static final Slot OUT_BOTTOM_SECOND =
            new Slot("KW_OUT_MAH_2", SEAT_BOTTOM, 440.02f, 395.0f, 0.5f);

    public static final Slot OUT_TOP_SECOND =
            new Slot("KW_OUT_MAH_2", SEAT_TOP, 1500.0961f, 700.0f, 0.5f);

    /** Flower roots, {@code KW_FLOWER}; the original scales these to 0.4. */
    public static final Slot FLOWER_LEFT = new Slot("KW_FLOWER", SEAT_LEFT, 360.0f, 950.0f, 0.4f);

    public static final Slot FLOWER_BOTTOM =
            new Slot("KW_FLOWER", SEAT_BOTTOM, 380.0f, 210.0f, 0.4f);

    public static final Slot FLOWER_RIGHT =
            new Slot("KW_FLOWER", SEAT_RIGHT, 1560.0f, 260.0f, 0.4f);

    public static final Slot FLOWER_TOP = new Slot("KW_FLOWER", SEAT_TOP, 1385.0f, 965.0f, 0.4f);

    /** Centre furniture inside {@code _KW_ADAPT_MAH_5}. */
    public static final Slot CENTER_JOKER =
            new Slot("KW_JOKER_MAH", CENTER, 780.096f, 610.0f, 0.5f);

    public static final Slot CENTER_ROUND_WIND =
            new Slot("_KW_IMG_QUAN_FENG", CENTER, 780.0f, 680.0f, 1.0f);

    public static final Slot CENTER_DEALER_BADGE =
            new Slot("_KW_IMG_ZHUANG_TYPE", CENTER, 1096.0f, 547.1956f, 1.0f);

    public static final Slot CENTER_WALL_COUNT =
            new Slot("_KW_PANEL_MAH_COUNTS", CENTER, 1109.952f, 610.0f, 1.0f);

    /** {@code _KW_IMG_QUAN_FENG} bitmap size. */
    public static final float ROUND_WIND_WIDTH = 115.0f;

    public static final float ROUND_WIND_HEIGHT = 39.0f;

    /** {@code _KW_IMG_ZHUANG_TYPE} bitmap size. */
    public static final float DEALER_BADGE_WIDTH = 81.0f;

    public static final float DEALER_BADGE_HEIGHT = 56.0f;

    /** Resting position of the action bar while hidden off-screen. */
    public static final float ACTION_BAR_HIDDEN_X = 2200.0f;

    /** {@code _KW_ACTION_IN_TARGET_POS}: where the bar slides to when shown. */
    public static final float ACTION_BAR_SHOWN_X = -100.0f;

    public static final float ACTION_BAR_Y = 250.0f;

    public static final float ACTION_BAR_WIDTH = 1200.0f;

    public static final float ACTION_BAR_HEIGHT = 200.0f;

    /** Every action button is a 200x200 square centred on its listed position. */
    public static final float ACTION_BUTTON_SIZE = 200.0f;

    /** Action buttons in CSB order: pass, chi, peng, gang, hu, ting. */
    public static final String[] ACTION_BUTTON_NAMES = {
        "_KW_ACTION_BTN_1", "_KW_ACTION_BTN_2", "_KW_ACTION_BTN_3",
        "_KW_ACTION_BTN_4", "_KW_ACTION_BTN_5", "_KW_ACTION_BTN_7",
    };

    /** Original frames for {@link #ACTION_BUTTON_NAMES}, from majiang_action_btn.plist. */
    public static final String[] ACTION_BUTTON_FRAMES = {
        "action_pass.png", "action_chi.png", "action_peng.png",
        "action_gang.png", "action_hu.png", "action_ting.png",
    };

    /** Local X of each action button inside {@code _KW_ACTION_MOVE_PANEL}. */
    public static final float[] ACTION_BUTTON_X = {
        1100.0f, 870.0f, 620.0f, 370.0f, 120.0f, -130.0f,
    };

    /** Local Y of every action button inside {@code _KW_ACTION_MOVE_PANEL}. */
    public static final float ACTION_BUTTON_Y = 100.0f;

    private TaizhouMahjongTableLayout() {}

    /** Returns the container for {@code seat}, or the hand strip. */
    public static Panel panel(int seat) {
        if (seat < SEAT_LEFT || seat > HAND_STRIP) {
            throw new IllegalArgumentException("unknown container " + seat);
        }
        return PANELS[seat - 1];
    }

    public static Slot showOutMahPanel(int localSeat) {
        return switch (localSeat) {
            case SEAT_LEFT -> SHOW_OUT_LEFT;
            case SEAT_BOTTOM -> SHOW_OUT_BOTTOM;
            case SEAT_RIGHT -> SHOW_OUT_RIGHT;
            case SEAT_TOP -> SHOW_OUT_TOP;
            default -> throw new IllegalArgumentException("unknown local seat " + localSeat);
        };
    }

    /** Converts a Cocos bottom-up Y into an Android top-down Y. */
    public static float designY(float cocosY) {
        return DESIGN_HEIGHT - cocosY;
    }
}
