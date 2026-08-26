package com.nanbeiyule.game.mahjong;

/**
 * 提前开局几何：{@code MahjongNew/GameLayer/CSB/CenterBtnsLayer.csb} 的
 * {@code _KW_BTN_QUICK} 等待态按钮与
 * {@code MahjongNew/GameLayer/CSB/TaiZhou/EarlyStart.csb} 确认弹层（1087×660）。
 *
 * <p><b>注意不是 {@code TaiZhou/TableInfo.csb} 的 {@code _KW_BTN_EARLY_START}。</b>
 * 那个节点在 CSB 里 {@code visible=False}，而唯一会把它置显的
 * {@code TaiZhou/BasicTaiZhouMahjong/Modules/GameLayer/TableInfoLayer.luac:115-129
 * earlyBeginStart} 在 1.0.0.687 全树只有定义、没有任何调用，也不在同文件
 * {@code getProxyEvents()} 的绑定表里——即它在本版本永远不显示。同文件 {@code :132-145}
 * 的 {@code onReadyStateChanged}（Cocos Y 528/388 双态）虽然绑定着，但移动的是这个
 * 不可见节点。
 *
 * <p>台州真正走的是通用中央按钮条：{@code TaiZhou/BasicTaiZhouMahjong/Modules/EarlyStart/
 * Module.luac:30,44,46,48} 调 {@code CenterBtns:showQuickButton{isShow, playerCount}}
 * → {@code BasicMahjong/Modules/CenterBtns/View.luac:29-32 onShowQuickButton}
 * 置显 {@code _KW_BTN_QUICK} 并把气泡文案设为 {@code playerCount.."人也能开"}
 * （**无感叹号**；带感叹号的 {@code "%d人也能开!"} 是上面那条死路径的文案）。
 * 点击走 {@code :34-36 onQuickBtnClicked} → {@code sendGameQuickStart(2)}。
 *
 * <p>Cocos 的 Y 轴自下而上，这里全部换算成顶部原点。
 */
public final class TaizhouEarlyStartLayout {
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

    // ---- CenterBtnsLayer.csb：_KW_PANEL_TOP_BTNS（挂在 Cocos (960,0) anchor(0,0)） ----

    /**
     * {@code _KW_BTN_QUICK}：局部 (0,388)、346×136，与 {@code _KW_BTN_START} 同位互斥。
     * 图素 {@code result_total_quick.png}（NingBo/NingBoImg.plist）。
     */
    public static final Node BUTTON =
            new Node("_KW_BTN_QUICK", 960.0f, DESIGN_HEIGHT - 388.0f, 346.0f, 136.0f);

    /**
     * {@code Image_2} 气泡：按钮子节点，局部 (418.9351,165.5261)、300×148，
     * 图素 {@code settle_title_tips.png}。按钮内容原点为 Cocos (787,320)，
     * 故气泡中心 Cocos (1205.9351,485.5261) → 设计 (1205.9351,594.4739)。
     */
    public static final Node BUBBLE =
            new Node("Image_2", 1205.9351f, DESIGN_HEIGHT - 485.5261f, 300.0f, 148.0f);

    /**
     * {@code _KW_TEXT_QUICK_TIPS}：气泡子节点，局部 (145.0929,96.0466)、221×56。
     * 气泡内容原点为 Cocos (1055.9351,411.5261)，故文字中心
     * Cocos (1201.028,507.5727) → 设计 (1201.028,572.4273)。
     */
    public static final Node TIP_TEXT =
            new Node("_KW_TEXT_QUICK_TIPS", 1201.028f, DESIGN_HEIGHT - 507.5727f, 221.0f, 56.0f);

    /** 气泡文案：{@code playerCount.."人也能开"}，无感叹号（CenterBtns/View.luac:31）。 */
    public static String quickTipText(int playerCount) {
        return playerCount + "人也能开";
    }

    // ---- EarlyStart.csb：_KW_IMG_BG 1087×660 面板（设计中心 960,540） ----

    public static final Node PANEL = new Node("_KW_IMG_BG", 960.0f, 540.0f, 1087.0f, 660.0f);

    /** 面板左缘/顶缘（顶部原点）。 */
    public static final float PANEL_LEFT = 960.0f - 1087.0f / 2.0f;

    public static final float PANEL_TOP = DESIGN_HEIGHT - (540.0f + 660.0f / 2.0f);

    /** {@code Image_26}：common_layer_bg.png 面板底，相对 (1,0)，1087×620.4。 */
    public static final float PANEL_BG_LEFT = PANEL_LEFT + 1.0f;

    public static final float PANEL_BG_TOP = PANEL_TOP + (660.0f - 620.4f);

    public static final float PANEL_BG_WIDTH = 1087.0f;

    public static final float PANEL_BG_HEIGHT = 620.4f;

    /**
     * {@code Image_30}/{@code Image_30_1}：common_title_bg.png 两半边标题底，
     * 543.5×81，与已验证的同族 1087×660 面板（VipNoticeLayer）一致按左正右镜像绘制。
     */
    public static final float TITLE_BG_TOP = PANEL_TOP;

    public static final float TITLE_BG_HEIGHT = 81.0f;

    public static final float TITLE_BG_LEFT_LEFT = PANEL_LEFT + 1.0f;

    public static final float TITLE_BG_LEFT_RIGHT = PANEL_LEFT + 544.5f;

    public static final float TITLE_BG_RIGHT_LEFT = PANEL_LEFT + 543.5f;

    public static final float TITLE_BG_RIGHT_RIGHT = PANEL_LEFT + 1087.0f;

    /** {@code Image_2}：tz_early_start_title.png，216×64。 */
    public static final Node TITLE = node("Image_2", 543.5f, 618.0f, 216.0f, 64.0f);

    /** {@code Image_28}/{@code Image_27}：common_img_huawen.png 两下角花纹，89×82。 */
    public static final Node FLOWER_LEFT = node("Image_28", 64.5f, 61.0f, 89.0f, 82.0f);

    public static final Node FLOWER_RIGHT = node("Image_27", 1022.5f, 61.0f, 89.0f, 82.0f);

    /** {@code _KW_BTN_REFUSE}：mah_btn_refuse.png，244×109。 */
    public static final Node BUTTON_REFUSE =
            node("_KW_BTN_REFUSE", 338.8793f, 97.8754f, 244.0f, 109.0f);

    /** {@code _KW_BTN_AGREE}：mah_btn_agree.png，244×109。 */
    public static final Node BUTTON_AGREE =
            node("_KW_BTN_AGREE", 762.636f, 91.6448f, 244.0f, 109.0f);

    /** {@code _KW_BTN_CLOSE}：99×102，CSB 默认 {@code visible=False}，保持隐藏。 */
    public static final Node BUTTON_CLOSE =
            node("_KW_BTN_CLOSE", 1052.0007f, 631.5005f, 99.0f, 102.0f);

    /**
     * {@code _KW_IMG_CLOCK} 与 {@code _KW_TEXT_CLOCK_TIP} 倒计时：
     * {@code EarlyStartView.luac:131-151} 在 time=0 时隐藏；QA 链路没有服务端倒计时，
     * 保持隐藏（多人同意流未接入，见视图注释）。
     */
    public static final Node CLOCK = node("_KW_IMG_CLOCK", 402.0f, 100.0f, 56.0f, 62.0f);

    /** {@code _KW_TEXT_OUT_TIME_TIP}：time=0 时同样隐藏（EarlyStartView.luac:135）。 */
    public static final Node OUT_TIME_TIP =
            node("_KW_TEXT_OUT_TIME_TIP", 543.5f, 418.0005f, 486.0f, 45.0f);

    /** {@code _KW_TEXT_NICK_NAME}：申请人昵称，anchor (1,0.5)，右缘在面板内 x=478。 */
    public static final float REQUEST_NAME_RIGHT = PANEL_LEFT + 478.0f;

    /** {@code text}：「申请立即开局」，anchor (0,0.5)，左缘在面板内 x=517.9555。 */
    public static final float REQUEST_LABEL_LEFT = PANEL_LEFT + 517.9555f;

    public static final float REQUEST_LINE_CENTER_Y = cocosToDesignY(534.0f);

    /** {@code text_0}：「是否同意更换为【2人/3人玩法】立即开局」。 */
    public static final Node CONFIRM_TEXT = node("text_0", 543.5f, 478.0f, 707.0f, 45.0f);

    /** {@code _KW_PANEL_PLAYER_POS}：玩家位列表挂点；多人同意流未接入，仅保留挂点常量。 */
    public static final float PLAYER_POS_CENTER_Y = cocosToDesignY(260.0f);

    private TaizhouEarlyStartLayout() {}

    /** 面板内 Cocos 坐标 → 顶部原点设计坐标。 */
    private static Node node(String name, float x, float cocosY, float width, float height) {
        return new Node(name, PANEL_LEFT + x, cocosToDesignY(cocosY), width, height);
    }

    /** 面板底边在 Cocos 体系是 210，因此设计 Y = 1080 - (210 + cocosY)。 */
    private static float cocosToDesignY(float cocosY) {
        return PANEL_TOP + 660.0f - cocosY;
    }
}
