package com.nanbeiyule.game.goldroom;

/**
 * 选场页右上活动入口组 {@code _menuBarTopAct} 的原版按钮。
 *
 * <p>原版这一组由 RemoteConfig 按地区下发，{@code Views/BtnFactory.lua:30-72} 再按
 * {@code LocalConfig.BTN_ID} 造出各自的按钮类；造出来之后每颗按钮还要跑自己的
 * {@code checkShow()}，用所属模块的 {@code isValid()} 决定 {@code setVisible}，隐藏后由
 * {@code updateGroupBtnShow()} 重排。因此**原版没有「常驻入口」**，五个入口全是条件显示。
 * 本枚举只固定单个入口的原版视觉与几何，不假装恢复下发逻辑。
 *
 * <p>枚举顺序即组内左→右顺序（组右对齐，向左延伸）。
 *
 * <p>每个按钮都是自带 CSB 的 160x160 Layer，内部结构一致：{@code _panel} 满铺 160x160 负责点击，
 * {@code _imgBtn} 是 157x159 的图标，红点挂在面板右上角。坐标在 CSB 里是 Cocos Y 向上、
 * 面板左下为原点，这里已全部换算成相对按钮中心的 Android Y 向下偏移。
 *
 * <p>证据见 {@code android/docs/ORIGINAL-GOLD-HALL-ACT-ENTRIES-EVIDENCE.md}。
 */
public enum GoldHallActEntry {
    /**
     * {@code NewGoldHall/TimeLoginActBtn.csb} — 定时登录有礼（{@code ACT_TIME_LOGIN = 3041}）。
     *
     * <p>原版 {@code TimeLoginActBtn.lua:38-40} 用 {@code TimeLoginAct:isValid()}（即
     * {@code Module.lua:251-253} 的 {@code self._aid ~= 0}）决定显隐。放在枚举最前是因为
     * {@code _menuBarTopAct} 右对齐、新增入口向左延伸：三颗恒显入口的中心
     * {@code 1426/1576/1726} 不变，本入口有效时占新出现的最左槽位 {@code 1276}。
     *
     * <p>图标 {@code TimeLoginIcon.png} 不在 {@code ActBtns/_Plist} 里，而是随定时登录页一起
     * 抽取的独立位图，CSB 里 {@code _imgBtn} 就是未裁切的 128x127。
     */
    ACT_TIME_LOGIN("定时有礼", null, null, 55.378f, 49.387f, 38.0f, true, 128.0f, 127.0f),

    /** {@code Act/PeGPBtn.csb} — 周期礼包，Lua 里的埋点名是「限时周期礼包」。 */
    LIMITED_TIME_GIFT("限时礼包", "Btn_xslb.png", null, 35.5389f, 34.3359f, 38.0f),

    /** {@code LuckyMissionBtn.csb} — 幸运任务 2.0，图标是骨骼不是静态帧。 */
    LUCKY_MISSION("福利任务", null, "zzb_flrw_rk", 43.2f, 28.8f, 33.0f),

    /**
     * {@code Act/FirstRechargeFirstBtn.csb} — 首充礼包首档。
     *
     * <p>原版 {@code ActFirstRecharge:isValid()} 三条同时成立才显示：{@code _aid > 0}、玩家数字 ID
     * 末位命中 RemoteConfig {@code act.FR.lastId} 灰度、且 {@code actInfo[1].gift} 存在。因此它和
     * 定时有礼一样由服务端开关，不是常驻入口——用户提供的 1.5.4 实机截图里这一颗就没有出现，
     * 正是该账号未命中灰度的实证。南北娱乐没有首充活动与支付服务端，故不进组。
     */
    FIRST_RECHARGE_GIFT(
            "首充礼包", "Img_sclb.png", null, 35.5389f, 34.3359f, 38.0f, true, 0.0f, 0.0f),

    /** {@code MonthlyCardBtn.csb} — 月卡，道具 ID 150831。 */
    MONTHLY_CARD("财神月卡", "Img_jbyk.png", null, 40.0f, 40.0f, 33.0f);

    /** 各按钮 CSB 里 {@code _imgBtn} 的尺寸都是 157x159，位置都是面板正中。 */
    public static final float ICON_BOX_WIDTH = 157.0f;

    public static final float ICON_BOX_HEIGHT = 159.0f;

    /**
     * {@code _aniNode} 在面板局部 {@code (80,70.4)}，面板中心是 {@code (80,80)}，
     * 所以骨骼挂点比按钮中心低 9.6。
     */
    public static final float ANI_NODE_OFFSET_Y = 9.6f;

    private static final String ATLAS_PREFIX = "hall/Image/NewGoldHall/ActBtns/";

    private final String label;
    private final String atlasFrame;
    private final String spineSkeleton;
    private final float redPointOffsetX;
    private final float redPointOffsetY;
    private final float redPointSize;
    private final boolean serverGated;
    private final float standaloneIconWidth;
    private final float standaloneIconHeight;

    GoldHallActEntry(
            String label,
            String atlasFrame,
            String spineSkeleton,
            float redPointOffsetX,
            float redPointOffsetY,
            float redPointSize) {
        this(label, atlasFrame, spineSkeleton, redPointOffsetX, redPointOffsetY, redPointSize,
                false, 0.0f, 0.0f);
    }

    GoldHallActEntry(
            String label,
            String atlasFrame,
            String spineSkeleton,
            float redPointOffsetX,
            float redPointOffsetY,
            float redPointSize,
            boolean serverGated,
            float standaloneIconWidth,
            float standaloneIconHeight) {
        this.label = label;
        this.atlasFrame = atlasFrame;
        this.spineSkeleton = spineSkeleton;
        this.redPointOffsetX = redPointOffsetX;
        this.redPointOffsetY = redPointOffsetY;
        this.redPointSize = redPointSize;
        this.serverGated = serverGated;
        this.standaloneIconWidth = standaloneIconWidth;
        this.standaloneIconHeight = standaloneIconHeight;
    }

    /**
     * 该入口是否由南北娱乐的真实活动状态开关。
     *
     * <p>原版每个入口都跑 {@code checkShow()}→{@code isValid()}，所以严格说这里应当全为
     * {@code true}。当前只有定时有礼与首充礼包标成开关：前者有自研 {@code timeloginact}
     * 服务端可给出真实有效性，后者原版判据含玩家灰度、我们没有对应服务因此恒不进组。
     *
     * <p>限时礼包、福利任务、财神月卡仍恒进组，是**已登记的偏差**：缺活动服务端时不按假状态
     * 隐藏，依据是 1.5.4 实机截图这三颗可见。见
     * {@code ORIGINAL-GOLD-HALL-ACT-ENTRIES-EVIDENCE.md} 第 4 节。
     */
    public boolean serverGated() {
        return serverGated;
    }

    /** 图标是否是独立位图而不是 {@code ActBtns/_Plist} 里的帧。 */
    public boolean usesStandaloneIcon() {
        return standaloneIconWidth > 0.0f;
    }

    /** 原版按钮名，取自 {@code LocalConfig.BTN_CFG} 与各按钮 Lua 的埋点文案。 */
    public String label() {
        return label;
    }

    /** {@code ActBtns/_Plist} 里的帧名；骨骼驱动的入口为 null。 */
    public String atlasFrame() {
        return atlasFrame;
    }

    /** 挂在 {@code _aniNode} 上的骨架名；静态图标入口为 null。 */
    public String spineSkeleton() {
        return spineSkeleton;
    }

    /** 红点相对按钮中心的设计坐标偏移，Y 向下。 */
    public float redPointOffsetX() {
        return redPointOffsetX;
    }

    public float redPointOffsetY() {
        return redPointOffsetY;
    }

    public float redPointSize() {
        return redPointSize;
    }

    /**
     * 图标裁切帧相对按钮中心的水平偏移。
     *
     * <p>图集是裁掉透明边后打包的，plist 的 {@code offset} 是裁切矩形中心相对原始精灵中心的
     * 位移（Y 向上）。{@code _imgBtn} 按未裁切尺寸摆放，所以要把这个位移按同样的缩放补回来。
     */
    public float iconOffsetX() {
        int[] frame = frame();
        return frame == null ? 0.0f : frame[5] * scaleX(frame);
    }

    /** 图标裁切帧相对按钮中心的垂直偏移，plist 的 Y 向上在这里取反。 */
    public float iconOffsetY() {
        int[] frame = frame();
        return frame == null ? 0.0f : -frame[6] * scaleY(frame);
    }

    /** 图标裁切帧在设计空间的绘制宽度。 */
    public float iconWidth() {
        if (usesStandaloneIcon()) {
            return standaloneIconWidth;
        }
        int[] frame = frame();
        return frame == null ? ICON_BOX_WIDTH : frame[2] * scaleX(frame);
    }

    /** 图标裁切帧在设计空间的绘制高度。 */
    public float iconHeight() {
        if (usesStandaloneIcon()) {
            return standaloneIconHeight;
        }
        int[] frame = frame();
        return frame == null ? ICON_BOX_HEIGHT : frame[3] * scaleY(frame);
    }

    /**
     * 组内位置与命中测试都依赖当前可见入口的数量，因此交给
     * {@link GoldHallActEntryGroup}；本枚举只保留单个入口的原版视觉属性。
     */

    private int[] frame() {
        if (atlasFrame == null) {
            return null;
        }
        String qualified = ATLAS_PREFIX + atlasFrame;
        for (int index = 0; index < GoldChooseRoomAtlas.ACT_BUTTON_NAMES.length; index++) {
            if (GoldChooseRoomAtlas.ACT_BUTTON_NAMES[index].equals(qualified)) {
                return GoldChooseRoomAtlas.ACT_BUTTON_FRAMES[index];
            }
        }
        return null;
    }

    private static float scaleX(int[] frame) {
        return ICON_BOX_WIDTH / frame[7];
    }

    private static float scaleY(int[] frame) {
        return ICON_BOX_HEIGHT / frame[8];
    }
}
