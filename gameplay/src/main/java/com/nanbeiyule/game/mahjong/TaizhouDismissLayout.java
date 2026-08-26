package com.nanbeiyule.game.mahjong;

/**
 * 解散包厢几何：{@code Common/CSB/GameBase/DismissLayer.csb}。
 *
 * <p>与 {@link TaizhouEarlyStartLayout} 共用同一块 1087×660 公共面板（{@code common_layer_bg.png}
 * + 两半边 {@code common_title_bg.png} + 两下角 {@code common_img_huawen.png}），只换标题图与文案。
 *
 * <p>原版链路：{@code GameBase/Modules/Dismiss/Module.luac:161-179 onMsgRequestDismiss}
 * 收到他人申请时弹 TipLayer 二选一；{@code :240-268 onMsgDismissCountDown} 带倒计时的房间
 * 则弹本 CSB（{@code Dismiss/View.luac}），四个玩家位由 {@code _KW_PANEL_PLAYER_MOD} 克隆后
 * 按 {@code calPlayerInfo}(:97-104) 均分排布。
 *
 * <p>Cocos 的 Y 轴自下而上，这里全部换算成顶部原点。
 */
public final class TaizhouDismissLayout {
    public static final float DESIGN_WIDTH = 1920.0f;
    public static final float DESIGN_HEIGHT = 1080.0f;

    /** 一个矩形节点。 */
    public record Node(String name, float centerX, float centerY, float width, float height) {
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

        public boolean contains(float x, float y) {
            return x >= left() && x <= right() && y >= top() && y <= bottom();
        }
    }

    // ---- _KW_IMG_BG 1087×660 面板（设计中心 960,540） ----

    public static final float PANEL_WIDTH = 1087.0f;
    public static final float PANEL_HEIGHT = 660.0f;
    public static final float PANEL_LEFT = 960.0f - PANEL_WIDTH / 2.0f;
    public static final float PANEL_TOP = DESIGN_HEIGHT - (540.0f + PANEL_HEIGHT / 2.0f);

    /** {@code Image_26}：common_layer_bg.png，相对 (1.2,0)，1085.3×581。 */
    public static final float PANEL_BG_LEFT = PANEL_LEFT + 1.2f;
    public static final float PANEL_BG_TOP = PANEL_TOP + (PANEL_HEIGHT - 581.0f);
    public static final float PANEL_BG_WIDTH = 1085.3f;
    public static final float PANEL_BG_HEIGHT = 581.0f;
    /** {@code common_layer_bg.png}(100×100) 的 capInsets {x=33,y=33,w=34,h=34}。 */
    public static final int PANEL_BG_CAP = 33;

    /** {@code Image_30}/{@code Image_30_1}：543.5×81 标题底，左正右镜像。 */
    public static final float TITLE_BG_TOP = PANEL_TOP;
    public static final float TITLE_BG_HEIGHT = 81.0f;
    public static final float TITLE_BG_LEFT_LEFT = PANEL_LEFT + 1.0f;
    public static final float TITLE_BG_LEFT_RIGHT = PANEL_LEFT + 544.5f;
    public static final float TITLE_BG_RIGHT_LEFT = PANEL_LEFT + 0.0f;
    public static final float TITLE_BG_RIGHT_RIGHT = PANEL_LEFT + 543.5f;
    /** {@code common_title_bg.png}(393×81) 的 capInsets {x=31,y=26,w=95,h=29}。 */
    public static final int TITLE_BG_CAP_LEFT = 31;
    public static final int TITLE_BG_CAP_RIGHT = 267;
    public static final int TITLE_BG_CAP_Y = 26;

    /** {@code Image_2}：img_title_sqjs.png「申请解散」，223×71。 */
    public static final Node TITLE = node("Image_2", 543.5f, 616.0f, 223.0f, 71.0f);

    /** {@code Image_28}/{@code Image_27}：common_img_huawen.png 两下角花纹，89×82。 */
    public static final Node FLOWER_LEFT = node("Image_28", 64.5f, 61.0f, 89.0f, 82.0f);
    public static final Node FLOWER_RIGHT = node("Image_27", 1022.5f, 61.0f, 89.0f, 82.0f);

    /**
     * {@code _KW_BTN_CLOSE}：99×102。{@code Dismiss/View.luac:71-76} 只在回放里置显，
     * 正常牌局隐藏，避免关掉弹窗后收不到投票结果。
     */
    public static final Node BUTTON_CLOSE =
            node("_KW_BTN_CLOSE", 1067.5f, 631.092f, 99.0f, 102.0f);

    /** {@code _KW_BTN_REFUSE}：mah_btn_refuse.png，244×109。 */
    public static final Node BUTTON_REFUSE =
            node("_KW_BTN_REFUSE", 338.8793f, 95.0f, 244.0f, 109.0f);

    /** {@code _KW_BTN_AGREE}：mah_btn_agree.png，244×109。 */
    public static final Node BUTTON_AGREE =
            node("_KW_BTN_AGREE", 762.636f, 95.0f, 244.0f, 109.0f);

    /** {@code _KW_IMG_CLOCK}：mah_img_clock.png，56×62；仅在倒计时大于 0 时显示。 */
    public static final Node CLOCK = node("_KW_IMG_CLOCK", 402.0f, 100.0f, 56.0f, 62.0f);

    /**
     * {@code _KW_TEXT_CLOCK_TIP}：时钟的子节点，局部 (71.7501,28.1251) anchor(0,0.5)，296×38。
     * 时钟内容原点为 Cocos (374,69)，故文字左缘 Cocos x=445.7501、中心 Y=97.1251。
     */
    public static final float CLOCK_TIP_LEFT = PANEL_LEFT + 445.7501f;
    public static final float CLOCK_TIP_CENTER_Y = cocosToDesignY(97.1251f);
    public static final float CLOCK_TIP_FONT_SIZE = 32.0f;

    /** {@code _KW_TEXT_NICK_NAME}：申请人昵称 anchor(1,0.5)，右缘在面板内 x=316.625。 */
    public static final float REQUEST_NAME_RIGHT = PANEL_LEFT + 316.625f;

    /** {@code text}：「申请解散房间，等待其他玩家选择」anchor(0,0.5)，左缘 x=357.2971。 */
    public static final float REQUEST_LABEL_LEFT = PANEL_LEFT + 357.2971f;
    public static final float REQUEST_LINE_CENTER_Y = cocosToDesignY(534.2f);
    public static final float REQUEST_FONT_SIZE = 38.0f;
    public static final String REQUEST_LABEL = "申请解散房间，等待其他玩家选择";

    /** {@code _KW_TEXT_OUT_TIME_TIP}：486×45，超时默认同意的说明。 */
    public static final Node OUT_TIME_TIP =
            node("_KW_TEXT_OUT_TIME_TIP", 545.5652f, 481.2091f, 486.0f, 45.0f);

    // ---- _KW_PANEL_PLAYER_MOD 180×250 玩家位模板 ----

    public static final float PLAYER_WIDTH = 180.0f;
    public static final float PLAYER_HEIGHT = 250.0f;
    /** {@code calPlayerInfo}(:97-104) 把玩家位竖直居中于 {@code _KW_IMG_BG}。 */
    public static final float PLAYER_CENTER_Y = PANEL_TOP + PANEL_HEIGHT / 2.0f;
    /** {@code KW_IMG_HEAD_FRAME}：局部 (90,164.922)，127×128。 */
    public static final float PLAYER_HEAD_OFFSET_Y = 164.922f - PLAYER_HEIGHT / 2.0f;
    public static final float PLAYER_HEAD_SIZE = 127.0f;
    /** {@code KW_TEXT_NICK_NAME}：局部 (90,76.523)，112×33。 */
    public static final float PLAYER_NAME_OFFSET_Y = 76.523f - PLAYER_HEIGHT / 2.0f;
    /** {@code KW_TEXT_STATE}：局部 (90,28.5005)，67×38。 */
    public static final float PLAYER_STATE_OFFSET_Y = 28.5005f - PLAYER_HEIGHT / 2.0f;
    public static final float PLAYER_FONT_SIZE = 32.0f;

    /**
     * {@code calPlayerInfo}(:97-104)：{@code averWidth = (面板宽 - 位宽×人数)/(人数+1)}，
     * 第 seat 位中心 X = {@code averWidth + 位宽/2 + (位宽 + averWidth)×seat}。
     */
    public static float playerCenterX(int seat, int playerCount) {
        float average = (PANEL_WIDTH - PLAYER_WIDTH * playerCount) / (playerCount + 1);
        return PANEL_LEFT + average + PLAYER_WIDTH / 2.0f + (PLAYER_WIDTH + average) * seat;
    }

    /** {@code Dismiss/View.luac:18} 的四态文案，索引与 {@code DismissView.Status} 一致。 */
    public static String statusLabel(TaizhouDismissStatus status) {
        return switch (status) {
            case DEFAULT -> "选择中...";
            case AGREE, REQUEST -> "同意";
            case REFUSE -> "拒绝";
        };
    }

    /** {@code Dismiss/View.luac:19} 的四态颜色。 */
    public static int statusColor(TaizhouDismissStatus status) {
        return switch (status) {
            case DEFAULT -> 0xFF868686;
            case AGREE, REQUEST -> 0xFF09A801;
            case REFUSE -> 0xFFF23333;
        };
    }

    private static Node node(String name, float x, float cocosY, float width, float height) {
        return new Node(name, PANEL_LEFT + x, cocosToDesignY(cocosY), width, height);
    }

    /** 面板底边在 Cocos 体系是 210，因此设计 Y = 1080 - (210 + cocosY)。 */
    private static float cocosToDesignY(float cocosY) {
        return PANEL_TOP + PANEL_HEIGHT - cocosY;
    }

    private TaizhouDismissLayout() {}
}
