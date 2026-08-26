package com.nanbeiyule.game.mahjong;

/** Original waiting-state geometry recovered from GameLayer CSB files. */
public final class TaizhouMahjongWaitingLayout {
    /** A ready sprite from {@code MahjongNew/GameLayer/CSB/Layer.csb}. */
    public static final class ReadyIndicator {
        public final String name;
        public final int localSeat;
        public final float centerX;
        public final float centerY;
        public final float width;
        public final float height;

        ReadyIndicator(
                String name,
                int localSeat,
                float centerX,
                float centerY,
                float width,
                float height) {
            this.name = name;
            this.localSeat = localSeat;
            this.centerX = centerX;
            this.centerY = centerY;
            this.width = width;
            this.height = height;
        }
    }

    /** A button node from {@code MahjongNew/GameLayer/CSB/CenterBtnsLayer.csb}. */
    public static final class CenterButton {
        public final String name;
        public final String frameName;
        public final float centerX;
        public final float centerY;
        public final float width;
        public final float height;

        CenterButton(
                String name,
                String frameName,
                float centerX,
                float centerY,
                float width,
                float height) {
            this.name = name;
            this.frameName = frameName;
            this.centerX = centerX;
            this.centerY = centerY;
            this.width = width;
            this.height = height;
        }

        public float left() {
            return centerX - width / 2.0f;
        }

        public float top() {
            return centerY - height / 2.0f;
        }

        public float right() {
            return centerX + width / 2.0f;
        }

        public float bottom() {
            return centerY + height / 2.0f;
        }

        public boolean contains(float designX, float designY) {
            return designX >= left()
                    && designX <= right()
                    && designY >= top()
                    && designY <= bottom();
        }
    }

    public static final ReadyIndicator READY_LEFT =
            new ReadyIndicator(
                    "_KW_IMG_READY_1",
                    TaizhouMahjongTableLayout.SEAT_LEFT,
                    378.0f,
                    485.0f,
                    88.0f,
                    122.0f);

    public static final ReadyIndicator READY_BOTTOM =
            new ReadyIndicator(
                    "_KW_IMG_READY_2",
                    TaizhouMahjongTableLayout.SEAT_BOTTOM,
                    960.0f,
                    590.0f,
                    88.0f,
                    122.0f);

    public static final ReadyIndicator READY_RIGHT =
            new ReadyIndicator(
                    "_KW_IMG_READY_3",
                    TaizhouMahjongTableLayout.SEAT_RIGHT,
                    1536.0f,
                    484.9999f,
                    88.0f,
                    122.0f);

    public static final ReadyIndicator READY_TOP =
            new ReadyIndicator(
                    "_KW_IMG_READY_4",
                    TaizhouMahjongTableLayout.SEAT_TOP,
                    960.0f,
                    170.0f,
                    88.0f,
                    122.0f);

    public static final CenterButton INVITE_BUTTON =
            new CenterButton(
                    "_KW_BTN_INVITE", "mah_invite_btn.png", 547.0f, 692.0f, 346.0f, 136.0f);

    public static final CenterButton START_BUTTON =
            new CenterButton(
                    "_KW_BTN_START", "mah_gamestart_btn.png", 960.0f, 692.0f, 346.0f, 136.0f);

    public static final CenterButton COPY_BUTTON =
            new CenterButton(
                    "_KW_BTN_COPY", "mah_copy_btn.png", 1370.0f, 692.0f, 346.0f, 136.0f);

    /**
     * 回看态左键「查看账单」：{@code _KW_BTN_SHOW_SETTLE}，与 {@code _KW_BTN_INVITE} 同位互斥。
     *
     * <p>CSB {@code CenterBtnsLayer.csb} 局部 (-413,388)、346×136，图素
     * {@code result_checkbill_btn.png}（Common/Image/total_result.plist）。
     * 由 {@code CenterBtns/View.luac:38-40 onShowSettleButton} 置显——入口是结算页
     * 「显示桌面」{@code WinLost/View.luac:396-400 onCheckTableClicked} → {@code showWinLostButton()}。
     */
    public static final CenterButton SHOW_SETTLE_BUTTON =
            new CenterButton(
                    "_KW_BTN_SHOW_SETTLE",
                    "result_checkbill_btn.png",
                    547.0f,
                    692.0f,
                    346.0f,
                    136.0f);

    /**
     * 回看态右键「下一局」：{@code _KW_BTN_CONTINUE}，与 {@code _KW_BTN_COPY} 同位互斥。
     *
     * <p>CSB 局部 (410,388)、346×136，图素 {@code next_btn.png}。同样由
     * {@code onShowSettleButton} 置显，点击走 {@code View.luac:82-87 onNextBtnClicked}
     * → 基类 {@code onStartGameEvent()} 并收起两个按钮。
     *
     * <p>子节点 {@code _KW_CONTINE_TIME}（局部 (281.6638,73.083) 43×47）是倒计时，
     * 原版仅在 {@code curCount ~= maxCount 且 lobbyID == LISHUI}（丽水）时启用
     * （{@code View.luac:42-64}），台州大厅不跑，故此处不实现。
     */
    public static final CenterButton CONTINUE_BUTTON =
            new CenterButton(
                    "_KW_BTN_CONTINUE", "next_btn.png", 1370.0f, 692.0f, 346.0f, 136.0f);

    public static final CenterButton RULE_BUTTON =
            new CenterButton(
                    "_KW_BTN_RULE", "mah_btn_rule.png", 70.0f, 205.0f, 70.0f, 67.0f);

    public static final CenterButton RECORD_BUTTON =
            new CenterButton(
                    "_KW_BTN_SXVIP_RECORD",
                    "taizhou_mahjong_battle_record",
                    210.0f,
                    226.5f,
                    77.0f,
                    101.0f);

    public static final CenterButton RECORD_RED_POINT =
            new CenterButton(
                    "_KW_RED_POINT",
                    "taizhou_mahjong_battle_record_red_point",
                    239.0143f,
                    196.077f,
                    32.8f,
                    32.8f);

    public static final CenterButton FRIEND_BUTTON =
            new CenterButton(
                    "_KW_UI_OPEN_PIC_IN_GAME",
                    "taizhou_mahjong_friend_tab",
                    65.0f,
                    540.0f,
                    130.0f,
                    287.0f);

    /**
     * {@code RightBtnsLayer.csb} 的 {@code _KW_PANEL_RIGHT_TOP_BTNS}(1920,1080) 下
     * {@code _KW_BTN_ROBOT} 局部 (-220,-60)。CSB {@code visible=False}，
     * {@code GameBase/Modules/RightBtns/View.luac:63-70 isShowRobotBtn} 只在
     * {@code roomData:isGoldRoom()} 时置显——即 30400 金币场有托管入口、30109 包厢没有。
     */
    public static final CenterButton TRUST_BUTTON =
            new CenterButton(
                    "_KW_BTN_ROBOT", "mah_tuoguan_btn.png", 1700.0f, 60.0f, 101.0f, 101.0f);

    public static final CenterButton MENU_BUTTON =
            new CenterButton(
                    "_KW_BTN_SET", "mah_set_btn.png", 1855.0f, 60.0f, 101.0f, 101.0f);

    public static final CenterButton CHANGE_CARD_BUTTON =
            new CenterButton(
                    "_KW_MAH_POS",
                    "taizhou_mahjong_change_card",
                    1698.04f,
                    176.94f,
                    110.0f,
                    120.0f);

    public static final CenterButton SHUFFLE_BUTTON =
            new CenterButton(
                    "_KW_SHUFFLE_MAHJONG_POS",
                    "taizhou_mahjong_shuffle",
                    1855.0f,
                    178.0f,
                    110.0f,
                    120.0f);

    public static final CenterButton FORTUNE_BUTTON =
            new CenterButton(
                    "_KW_PANEL_ITEM_BTN",
                    "taizhou_mahjong_fortune",
                    1840.0f,
                    477.04f,
                    130.0f,
                    120.0f);

    /** RightBtnsLayer.csb: parent (1920,540), _KW_BTN_TING local (-65,72). */
    public static final CenterButton TING_BUTTON =
            new CenterButton(
                    "_KW_BTN_TING", "mah_ting_btn.png", 1855.0f, 468.0f, 101.0f, 101.0f);

    public static final CenterButton CHAT_BUTTON =
            new CenterButton(
                    "_KW_BTN_MSG", "mah_msg_btn.png", 1855.0f, 589.0f, 101.0f, 101.0f);

    public static final CenterButton VOICE_BUTTON =
            new CenterButton(
                    "_KW_BTN_SPEAK", "mah_speak_btn.png", 1855.0f, 710.0f, 101.0f, 101.0f);

    public static final CenterButton LUCKY_MISSION_BUTTON =
            new CenterButton(
                    "LuckyMissionView",
                    "taizhou_mahjong_lucky_mission",
                    1490.0f,
                    955.0f,
                    160.0f,
                    174.0f);

    public static final CenterButton TREASURE_POT_BUTTON =
            new CenterButton(
                    "JuBaoPenIconView",
                    "taizhou_mahjong_treasure_pot",
                    1640.0f,
                    955.0f,
                    160.0f,
                    174.0f);

    public static final CenterButton CAISHEN_BUTTON =
            new CenterButton(
                    "GamePropView",
                    "taizhou_mahjong_invite_caishen",
                    1790.0f,
                    955.0f,
                    160.0f,
                    174.0f);

    public static final CenterButton COPY_RECOMMENDATION =
            new CenterButton(
                    "_KW_IMG_COPY_TIPS_COPYROOM",
                    "taizhou_mahjong_copy_tip",
                    1478.0f,
                    600.0f,
                    246.0f,
                    140.0f);

    private static final ReadyIndicator[] READY_INDICATORS = {
        READY_LEFT, READY_BOTTOM, READY_RIGHT, READY_TOP,
    };

    private TaizhouMahjongWaitingLayout() {}

    /** Returns the original ready sprite geometry for one local seat. */
    public static ReadyIndicator ready(int localSeat) {
        if (localSeat < TaizhouMahjongTableLayout.SEAT_LEFT
                || localSeat > TaizhouMahjongTableLayout.SEAT_TOP) {
            throw new IllegalArgumentException("unknown local seat " + localSeat);
        }
        return READY_INDICATORS[localSeat - 1];
    }
}
