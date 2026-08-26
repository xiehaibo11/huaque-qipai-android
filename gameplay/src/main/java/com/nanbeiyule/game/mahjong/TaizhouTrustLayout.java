package com.nanbeiyule.game.mahjong;

/**
 * 托管几何：{@code Common/CSB/GameBase/TrustLayer.csb}（1920×1080 全屏层）与
 * {@code MahjongNew/GameLayer/CSB/RightBtnsLayer.csb} 的 {@code _KW_BTN_ROBOT} 入口按钮。
 *
 * <p>原版链路：{@code GameBase/Modules/RightBtns/View.luac:24,196 onRobotBtnClicked}
 * → {@code RightBtns/Module.luac:3-14 doStartTrust}（非 {@code psPlaying} 时弹
 * 「等待中不能托管！」）→ {@code sendTrust(true)}；服务端回 {@code msgTrust} 后
 * {@code Trust/Module.luac:19-38 onMsgTrust} 只对自己座位弹
 * {@code Trust/View.luac} 全屏层，点击 {@code _KW_PANEL_ROBOT} 发 {@code sendTrust(false)} 取消。
 *
 * <p>Cocos 的 Y 轴自下而上，这里全部换算成顶部原点。
 */
public final class TaizhouTrustLayout {
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

    /**
     * {@code RightBtnsLayer.csb} 的 {@code _KW_PANEL_RIGHT_TOP_BTNS}(1920,1080) 下
     * {@code _KW_BTN_ROBOT} 局部 (-220,-60)，101×101，CSB 里 {@code visible=False}，
     * 由 {@code RightBtns/View.luac} 在牌局进行中置显。
     */
    public static final Node BUTTON = new Node("_KW_BTN_ROBOT", 1700.0f, 60.0f, 101.0f, 101.0f);

    /** {@code _KW_PANEL_ROBOT} 全屏遮罩，alpha=178；点击任意处取消托管。 */
    public static final int OVERLAY_ALPHA = 178;

    /** {@code _KW_PANEL_TRUST_ANI_POS} Cocos (960,540) 处的骨骼动画原点。 */
    public static final float ANIMATION_ORIGIN_X = 960.0f;
    public static final float ANIMATION_ORIGIN_Y = 540.0f;

    /**
     * {@code _KW_TRUST_TIP_BG} 746×300。CSB {@code visible=False}，
     * {@code Trust/View.luac:62-69 showTrustPanel} 仅在 {@code getTrustPunishTime()>0}
     * 且 {@code roomData:is220Model()} 时置显。
     */
    public static final Node TIP_BACKGROUND =
            new Node("_KW_TRUST_TIP_BG", 443.048f, 840.0f, 746.0f, 300.0f);

    /** {@code trust_tip_bg.png}(227×128) 的 capInsets {x=150,y=33,w=7,h=5}。 */
    public static final int TIP_CAP_X = 150;
    public static final int TIP_CAP_Y = 33;
    public static final int TIP_CAP_WIDTH = 7;
    public static final int TIP_CAP_HEIGHT = 5;

    /** {@code _KW_TEXT_TIME_STR}「已托管 %d 秒」。 */
    public static final Node TIME_TEXT =
            new Node("_KW_TEXT_TIME_STR", 443.048f, 766.74f, 339.0f, 80.0f);
    public static final float TIME_TEXT_FONT_SIZE = 68.0f;

    /** {@code Text_2}「托管超过」。 */
    public static final Node PUNISH_PREFIX_TEXT =
            new Node("Text_2", 205.048f, 860.0f, 186.0f, 54.0f);

    /** {@code _KW_TEXT_MAX_TIME} 惩罚秒数。 */
    public static final Node PUNISH_SECONDS_TEXT =
            new Node("_KW_TEXT_MAX_TIME", 329.944f, 860.0f, 55.0f, 54.0f);

    /** {@code Text_4}「秒，将触发托管惩罚」。 */
    public static final Node PUNISH_SUFFIX_TEXT =
            new Node("Text_4", 568.606f, 860.0f, 414.0f, 54.0f);

    public static final float PUNISH_FONT_SIZE = 46.0f;

    /** CSB 里四条文案同为 {@code #FF6E6E6E}。 */
    public static final int TEXT_COLOR = 0xFF6E6E6E;

    /** {@code Trust/View.luac:5-6} 的骨骼动画资源。 */
    public static final String ANIMATION_EXPORT_JSON =
            "taizhou_trust_effects/tuoguan_ani/ios_tuoguan.ExportJson";
    public static final String ANIMATION_TEXTURE =
            "taizhou_trust_effects/tuoguan_ani/ios_tuoguan0.png";
    public static final String ANIMATION_FRAMES =
            "taizhou_trust_effects/tuoguan_ani/ios_tuoguan0.json";
    /** {@code KW_JSON_NAME_ROBOT} 对应的动作名。 */
    public static final String ANIMATION_MOVEMENT = "tuoguan_ani";

    private TaizhouTrustLayout() {}
}
