package com.nanbeiyule.game.mahjong;

/**
 * 玩家信息几何：{@code Common/CSB/GameBase/PlayerInfoLayer.csb}。
 *
 * <p>层级是 {@code _KW_PANAEL_USER_INFO}(1700×774，设计中心 960,550.044) 下再挂
 * {@code _KW_PANEL_INFO}(左，1230×750，anchor 0,1) 与 {@code _KW_VIP_INFO}(右，425×730，
 * anchor 0,1)。为了可核对，下面按「面板内局部坐标」建常量，再由 {@link #designX}/
 * {@link #designY} 统一换算到 1920×1080 顶部原点设计坐标。
 *
 * <p>Cocos 的 Y 轴自下而上；{@code _KW_PANAEL_USER_INFO} 的内容原点（左下角）在
 * Cocos (110, 163.044)。
 */
public final class TaizhouPlayerInfoLayout {
    public static final float DESIGN_WIDTH = 1920.0f;
    public static final float DESIGN_HEIGHT = 1080.0f;

    /** 一个矩形节点（设计坐标，顶部原点）。 */
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

    // ---- _KW_PANAEL_USER_INFO：1700×774，设计中心 (960,550.044) ----

    public static final float PANEL_WIDTH = 1700.0f;
    public static final float PANEL_HEIGHT = 774.0f;
    /** 面板内容原点（Cocos 左下角）。 */
    public static final float PANEL_ORIGIN_X = 960.0f - PANEL_WIDTH / 2.0f;
    public static final float PANEL_ORIGIN_COCOS_Y = 550.044f - PANEL_HEIGHT / 2.0f;

    /** 面板内局部 X → 设计 X。 */
    public static float designX(float localX) {
        return PANEL_ORIGIN_X + localX;
    }

    /** 面板内局部 Cocos Y → 设计 Y（顶部原点）。 */
    public static float designY(float localCocosY) {
        return DESIGN_HEIGHT - (PANEL_ORIGIN_COCOS_Y + localCocosY);
    }

    private static Node node(String name, float localX, float localY, float width, float height) {
        return new Node(name, designX(localX), designY(localY), width, height);
    }

    /**
     * {@code _KW_PANAEL_USER_INFO_BG}：PlayerInfoNew_Img_di.png(92×741)，局部 (-11,-10)
     * anchor(0,0)，1723×791，capInsets {x=30,y=138,w=32,h=465}。
     */
    public static final float BACKGROUND_LEFT = designX(-11.0f);
    public static final float BACKGROUND_TOP = designY(-10.0f + 791.0f);
    public static final float BACKGROUND_WIDTH = 1723.0f;
    public static final float BACKGROUND_HEIGHT = 791.0f;
    public static final int BACKGROUND_CAP_X = 30;
    public static final int BACKGROUND_CAP_Y = 138;
    public static final int BACKGROUND_CAP_WIDTH = 32;
    public static final int BACKGROUND_CAP_HEIGHT = 465;

    /** {@code Image_2}：左上花纹 163×338，背景内局部 (16,779) anchor(0,1)。 */
    public static final Node ORNAMENT_LEFT =
            new Node(
                    "Image_2",
                    designX(-11.0f + 16.0f + 163.0f / 2.0f),
                    designY(-10.0f + 779.0f - 338.0f / 2.0f),
                    163.0f,
                    338.0f);

    /** {@code Image_3}：横向分隔线 1175×21，背景内局部 (640,534)。 */
    public static final Node DIVIDER_THICK =
            new Node(
                    "Image_3", designX(-11.0f + 640.0f), designY(-10.0f + 534.0f), 1175.0f, 21.0f);

    /** {@code Image_4}：细分隔线 1185×1，背景内局部 (53,114) anchor(0,0.5)。 */
    public static final Node DIVIDER_THIN =
            new Node(
                    "Image_4",
                    designX(-11.0f + 53.0f + 1185.0f / 2.0f),
                    designY(-10.0f + 114.0f),
                    1185.0f,
                    1.0f);

    /** {@code _KW_IMG_1}：右下花纹 226×270，面板内局部 (1583,145)。 */
    public static final Node ORNAMENT_RIGHT = node("_KW_IMG_1", 1583.0f, 145.0f, 226.0f, 270.0f);

    /** {@code _KW_BTN_CLOSE}：60×60，面板内局部 (1680,755)。 */
    public static final Node BUTTON_CLOSE = node("_KW_BTN_CLOSE", 1680.0f, 755.0f, 60.0f, 60.0f);

    // ---- _KW_PANEL_INFO：局部 (5,760) anchor(0,1)，1230×750；内容原点 (5,10) ----

    private static final float INFO_ORIGIN_X = 5.0f;
    private static final float INFO_ORIGIN_Y = 10.0f;

    private static Node infoNode(
            String name, float localX, float localY, float width, float height) {
        return node(name, INFO_ORIGIN_X + localX, INFO_ORIGIN_Y + localY, width, height);
    }

    /** {@code _KW_PANAEL_HEAD_POS}：头像挂点 145×155，局部 (100,662)。 */
    public static final Node HEAD = infoNode("_KW_PANAEL_HEAD_POS", 100.0f, 662.0f, 145.0f, 155.0f);

    /** {@code _KW_TEXT_NICK_NAME}：anchor(0,0.5)，左缘局部 x=190、中心 y=704.43。 */
    public static final float NICKNAME_LEFT = designX(INFO_ORIGIN_X + 190.0f);
    public static final float NICKNAME_CENTER_Y = designY(INFO_ORIGIN_Y + 704.43f);
    public static final float NICKNAME_FONT_SIZE = 38.0f;

    /** {@code _KW_TEXT_ID}：anchor(0,0.5)，局部 (190,655)。 */
    public static final float ID_LEFT = designX(INFO_ORIGIN_X + 190.0f);
    public static final float ID_CENTER_Y = designY(INFO_ORIGIN_Y + 655.0f);
    public static final float ID_FONT_SIZE = 34.0f;

    /** {@code _KW_TEXT_IP}：anchor(0,0.5)，局部 (190,611)。 */
    public static final float IP_LEFT = designX(INFO_ORIGIN_X + 190.0f);
    public static final float IP_CENTER_Y = designY(INFO_ORIGIN_Y + 611.0f);
    public static final float IP_FONT_SIZE = 30.0f;

    /** {@code _KW_TEXT_GPS}：anchor(0,1)，左缘局部 x=190、顶缘 y=594.3。 */
    public static final float GPS_LEFT = designX(INFO_ORIGIN_X + 190.0f);
    public static final float GPS_TOP = designY(INFO_ORIGIN_Y + 594.3f);
    public static final float GPS_FONT_SIZE = 30.0f;

    /** {@code _KW_BTN_GPS}：测距按钮 150×60，局部 (1135,573)。 */
    public static final Node BUTTON_RANGE = infoNode("_KW_BTN_GPS", 1135.0f, 573.0f, 150.0f, 60.0f);

    /** {@code _KW_BTN_KICK}：请出房间 190×80，局部 (1115,700)。 */
    public static final Node BUTTON_KICK = infoNode("_KW_BTN_KICK", 1115.0f, 700.0f, 190.0f, 80.0f);
    public static final float KICK_FONT_SIZE = 36.0f;

    // ---- _KW_BLOCK：局部 (490,6) anchor(0,0)，650×100 ----

    private static Node blockNode(
            String name, float localX, float localY, float width, float height) {
        return node(name, 490.0f + localX, 6.0f + localY, width, height);
    }

    /** {@code Text_1}「屏蔽TA：」局部 (163,53)。 */
    public static final Node BLOCK_LABEL = blockNode("Text_1", 163.0f, 53.0f, 120.0f, 40.0f);

    /** {@code _KW_CHECKBOX_VOICE} 41×41，局部 (249,53)。 */
    public static final Node BLOCK_VOICE =
            blockNode("_KW_CHECKBOX_VOICE", 249.0f, 53.0f, 41.0f, 41.0f);

    /** {@code _KW_CHECKBOX_CHAT} 41×41，局部 (386,53)。 */
    public static final Node BLOCK_CHAT =
            blockNode("_KW_CHECKBOX_CHAT", 386.0f, 53.0f, 41.0f, 41.0f);

    /** {@code _KW_CHECKBOX_EMOJIS} 41×41，局部 (558,53)。 */
    public static final Node BLOCK_EMOJIS =
            blockNode("_KW_CHECKBOX_EMOJIS", 558.0f, 53.0f, 41.0f, 41.0f);

    /** 三个复选框右侧文案 anchor(0,0.5)，局部 x 依次 274/414/585。 */
    public static final float BLOCK_VOICE_TEXT_LEFT = designX(490.0f + 274.0f);
    public static final float BLOCK_CHAT_TEXT_LEFT = designX(490.0f + 414.0f);
    public static final float BLOCK_EMOJIS_TEXT_LEFT = designX(490.0f + 585.0f);
    public static final float BLOCK_TEXT_CENTER_Y = designY(6.0f + 53.0f);
    public static final float BLOCK_FONT_SIZE = 34.0f;

    // ---- _KW_PLAYER_INFO：局部 (500,6) anchor(0,0)，650×100（仅自己座位显示） ----

    /** {@code Image_6}：钻石图标 92×71，局部 (230,55)。 */
    public static final Node WALLET_DIAMOND_ICON =
            node("Image_6", 500.0f + 230.0f, 6.0f + 55.0f, 92.0f, 71.0f);

    /** {@code Image_5}：房卡图标 128×72，局部 (530,55)。 */
    public static final Node WALLET_ROOM_CARD_ICON =
            node("Image_5", 500.0f + 530.0f, 6.0f + 55.0f, 128.0f, 72.0f);

    public static final float WALLET_DIAMOND_TEXT_LEFT = designX(500.0f + 280.0f);
    public static final float WALLET_ROOM_CARD_TEXT_LEFT = designX(500.0f + 583.0f);
    public static final float WALLET_TEXT_CENTER_Y = designY(6.0f + 55.0f);
    public static final float WALLET_FONT_SIZE = 34.0f;

    // ---- _KW_VIP_INFO：局部 (1255,753) anchor(0,1)，425×730；内容原点 (1255,23) ----

    private static final float VIP_ORIGIN_X = 1255.0f;
    private static final float VIP_ORIGIN_Y = 23.0f;

    /** {@code _KW_VIP_INFO} 背景 PlayerInfoNew_Img_di2.png(30×730)，alpha=204。 */
    public static final float VIP_LEFT = designX(VIP_ORIGIN_X);
    public static final float VIP_TOP = designY(VIP_ORIGIN_Y + 730.0f);
    public static final float VIP_WIDTH = 425.0f;
    public static final float VIP_HEIGHT = 730.0f;
    public static final int VIP_ALPHA = 204;

    /** {@code _KW_VIP_INFO_SELF}/{@code _KW_VIP_INFO_OTHER}：局部 (0,730) anchor(0,1)，425×600。 */
    private static final float VIP_BODY_ORIGIN_Y = VIP_ORIGIN_Y + 130.0f;

    private static Node vipNode(
            String name, float localX, float localY, float width, float height) {
        return node(name, VIP_ORIGIN_X + localX, VIP_BODY_ORIGIN_Y + localY, width, height);
    }

    /** {@code Image_1}/{@code Image_1_0}：分区标题底 237×51，局部 (107,576)。 */
    public static final Node VIP_TITLE_BACKGROUND =
            vipNode("Image_1", 107.0f, 576.0f, 237.0f, 51.0f);

    /** 标题文字局部 (85,576)。 */
    public static final Node VIP_TITLE_TEXT = vipNode("Text_3_2", 85.0f, 576.0f, 142.0f, 40.0f);

    /** 「TA的信息」四项标签的局部坐标（other 版）。 */
    public static final Node VIP_OTHER_LABEL_ROUNDS =
            vipNode("Text_3", 119.0f, 496.0f, 173.0f, 40.0f);
    public static final Node VIP_OTHER_LABEL_WIN_RATE =
            vipNode("Text_3_0", 119.0f, 366.0f, 138.0f, 40.0f);
    public static final Node VIP_OTHER_LABEL_TOTAL_WIN_RATE =
            vipNode("Text_3_0_0", 119.0f, 224.0f, 172.0f, 40.0f);
    public static final Node VIP_OTHER_LABEL_SPEED =
            vipNode("Text_3_1", 310.25f, 498.0f, 138.0f, 40.0f);
    public static final Node VIP_OTHER_LABEL_OFFLINE =
            vipNode("Text_3_0_1", 310.25f, 367.001f, 139.0f, 40.0f);
    public static final Node VIP_OTHER_LABEL_DISMISS =
            vipNode("Text_3_0_0_0", 310.25f, 223.0f, 139.0f, 40.0f);
    public static final Node VIP_OTHER_LABEL_SCORE =
            vipNode("Text_3_1_0", 119.0f, 91.0f, 173.0f, 40.0f);

    /** 「我的信息」四项标签的局部坐标（self 版）。 */
    public static final Node VIP_SELF_LABEL_TOTAL_WIN_RATE =
            vipNode("Text_3", 119.0f, 496.0f, 172.0f, 40.0f);
    public static final Node VIP_SELF_LABEL_SPEED =
            vipNode("Text_3_1", 310.25f, 496.0f, 138.0f, 40.0f);
    public static final Node VIP_SELF_LABEL_OFFLINE =
            vipNode("Text_3_0_1", 119.0f, 364.0f, 139.0f, 40.0f);
    public static final Node VIP_SELF_LABEL_DISMISS =
            vipNode("Text_3_0_0_0", 310.25f, 364.0f, 139.0f, 40.0f);

    /** {@code KW_BLUR} 的马赛克图位（other 版七块、self 版四块）。 */
    public static final Node[] VIP_OTHER_BLUR = {
        vipNode("Image_14", 119.0f, 438.0f, 89.0f, 83.0f),
        vipNode("Image_14_0", 119.0f, 306.0f, 126.0f, 74.0f),
        vipNode("Image_14_1", 119.0f, 166.0f, 126.0f, 74.0f),
        vipNode("Image_14_2", 310.25f, 310.0f, 122.0f, 84.0f),
        vipNode("Image_14_2_0", 310.25f, 170.0f, 89.0f, 83.0f),
        vipNode("Image_14_2_1", 310.25f, 434.0f, 126.0f, 74.0f),
        vipNode("Image_14_2_1_0", 119.0f, 30.0f, 122.0f, 84.0f),
    };

    public static final Node[] VIP_SELF_BLUR = {
        vipNode("Image_14", 119.0f, 438.0f, 89.0f, 83.0f),
        vipNode("Image_14_0", 119.0f, 300.0f, 122.0f, 84.0f),
        vipNode("Image_14_1", 310.25f, 300.0f, 89.0f, 83.0f),
        vipNode("Image_14_2", 310.25f, 438.0f, 89.0f, 83.0f),
    };

    /** {@code _KW_BTN_BUY_VIP}：332.01×108，scale 0.75，VIP 面板内局部 (219,86)。 */
    public static final Node BUTTON_BUY_VIP =
            node(
                    "_KW_BTN_BUY_VIP",
                    VIP_ORIGIN_X + 219.0f,
                    VIP_ORIGIN_Y + 86.0f,
                    332.01f * 0.75f,
                    108.0f * 0.75f);
    public static final float BUY_VIP_FONT_SIZE = 60.0f * 0.75f;

    /** {@code _KW_TEXT_VIP}：到期时间，VIP 面板内局部 (212.5,20)。 */
    public static final Node VIP_EXPIRY_TEXT =
            node("_KW_TEXT_VIP", VIP_ORIGIN_X + 212.5f, VIP_ORIGIN_Y + 20.0f, 391.0f, 33.0f);
    public static final float VIP_EXPIRY_FONT_SIZE = 28.0f;

    public static final float VIP_LABEL_FONT_SIZE = 34.0f;

    /** CSB 里全部文案同为 {@code #FF6E6E6E}。 */
    public static final int TEXT_COLOR = 0xFF6E6E6E;

    private TaizhouPlayerInfoLayout() {}
}
