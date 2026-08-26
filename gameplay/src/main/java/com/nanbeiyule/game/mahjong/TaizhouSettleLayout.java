package com.nanbeiyule.game.mahjong;

/**
 * {@code MahjongNew/GameLayer/CSB/Settle/Settle.csb} 与逐行 {@code Settle/SettleItem.csb}
 * 的 1920×1080 节点几何。
 *
 * <p>实机画面见
 * {@code android/docs/evidence/taizhou-live-round-20260811/04-settlement-four-hands.png}，
 * 该目录 README 已把截图元素与下列节点逐项对上。Cocos 的 Y 轴自下而上，这里换算成顶部原点。
 *
 * <p>结算页只负责展示服务端下发的成绩，不参与任何牌墙或胡牌判定；在
 * {@code TAIZHOU-MAHJONG-WALL-EVIDENCE.md} 的阻断解除以前，牌局走不到这一步，本布局属于
 * 提前就位的展示层。
 */
public final class TaizhouSettleLayout {
    public static final float DESIGN_WIDTH = 1920.0f;
    public static final float DESIGN_HEIGHT = 1080.0f;

    /** {@code Panel_1} 内容区：1920×867，anchor (0.5,1)，挂在 {@code (960,1007)}。 */
    public static final float CONTENT_HEIGHT = 867.0f;

    /** 内容区顶边在顶部原点体系下的 Y：{@code 1080 - 1007}。 */
    public static final float CONTENT_TOP = DESIGN_HEIGHT - 1007.0f;

    /**
     * {@code KW_COVER_LAYER}：{@code Settle.csb} 的第一个子节点，1920×1080 全屏遮罩，排在
     * {@code KW_CONTENT_BG} 之前。只盖内容区会让牌桌层的房间号面板、玩家头像和座位分数从
     * 顶部 0..{@link #CONTENT_TOP} 与底部 {@code CONTENT_TOP+CONTENT_HEIGHT}..1080 露出来。
     */
    public static final float COVER_WIDTH = DESIGN_WIDTH;

    public static final float COVER_HEIGHT = DESIGN_HEIGHT;

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

    // ---- Settle.csb ----

    /** {@code _KW_IMG_RESULT}：959×171 的结果横幅（自摸/点炮/流局…）。 */
    public static final Node RESULT_BANNER = content("_KW_IMG_RESULT", 954.0f, 874.0f, 959.0f, 171.0f);

    /** {@code settle_title_bar_1}：1898×76 顶部条，anchor (0.5,1)。 */
    public static final Node TITLE_BAR = contentTop("settle_title_bar_1", 960.0f, 865.005f, 1898.0f, 76.0f);

    /** {@code _KW_IMG_FAN}：52×50 番列表头。 */
    public static final Node TITLE_FAN = content("_KW_IMG_FAN", 1437.0f, 827.5f, 52.0f, 50.0f);

    /**
     * {@code _KW_IMG_GANG}：98×50 杠列表头，CSB 默认 {@code visible=False}。原版台州结算把
     * 杠分并进「胡数/台数/总胡数」明细行，实机截图的表头也只有合计，因此不绘制。
     */
    public static final Node TITLE_GANG = content("_KW_IMG_GANG", 1559.0f, 828.5f, 98.0f, 50.0f);

    /** {@code KW_IMG_HEJI}：97×51 合计列表头。 */
    public static final Node TITLE_HEJI = content("KW_IMG_HEJI", 1785.0f, 827.5f, 97.0f, 51.0f);

    /**
     * {@code _KW_LABEL_ROOM_NUM} / {@code _KW_LABEL_JUSHU} / {@code _KW_LABEL_TIME}：三个
     * anchor (0,0.5) 的文本挂在标题栏同一行，Cocos Y 都是 825，横向依次排开。
     */
    public static final float HEADER_LABEL_CENTER_Y = CONTENT_TOP + (CONTENT_HEIGHT - 825.0f);

    public static final float HEADER_ROOM_NUM_LEFT = 20.0f;
    public static final float HEADER_JUSHU_LEFT = 275.0f;
    public static final float HEADER_TIME_LEFT = 485.006f;

    /** {@code _KW_TEXT_GAME_RULE}：1870×75，anchor (0,0)，挂在内容区底部 {@code (20,16)}。 */
    public static final float GAME_RULE_LEFT = 20.0f;

    public static final float GAME_RULE_CENTER_Y =
            CONTENT_TOP + (CONTENT_HEIGHT - 16.0f) - 75.0f / 2.0f;

    /** {@code _KW_LIST_VIEW}：1920×785，anchor (0.5,1)，纵向缩放 0.9376。 */
    public static final float LIST_HEIGHT = 785.0f;

    public static final float LIST_SCALE_Y = 0.9376f;

    /** 列表顶边（顶部原点）。 */
    public static final float LIST_TOP = CONTENT_TOP + (CONTENT_HEIGHT - 790.0f);

    /** {@code _KW_BTN_CHECK_TABLE}：346×136 显示桌面。 */
    public static final Node BUTTON_CHECK_TABLE =
            bottom("_KW_BTN_CHECK_TABLE", 960.0f - 620.0f, 74.0f, 346.0f, 136.0f);

    /** {@code _KW_BTN_SHUFFLE_DIRECT}：387×132 洗牌。 */
    public static final Node BUTTON_SHUFFLE =
            bottom("_KW_BTN_SHUFFLE_DIRECT", 960.0f - 250.0f, 74.5f, 387.0f, 132.0f);

    /** {@code _KW_BTN_CUTCARD}：387×130 换牌。 */
    public static final Node BUTTON_CHANGE_CARD =
            bottom("_KW_BTN_CUTCARD", 960.0f, 74.0f, 387.0f, 130.0f);

    /** {@code _KW_BTN_NEXT_GAME}：346×136 下一局。 */
    public static final Node BUTTON_NEXT =
            bottom("_KW_BTN_NEXT_GAME", 960.0f + 620.0f, 74.0f, 346.0f, 136.0f);

    /** 末局唯一按钮 {@code _KW_BTN_CHECK_BILL}：原版 {@code updateBtnInEnd()} 居中显示。 */
    public static final Node BUTTON_CHECK_BILL =
            bottom("_KW_BTN_CHECK_BILL", 960.0f, 74.0f, 346.0f, 136.0f);

    // ---- SettleItem.csb ----

    /** 单行 {@code KW_PANEL_ROOT}：1920×196。 */
    public static final float ROW_WIDTH = 1920.0f;

    public static final float ROW_HEIGHT = 196.0f;

    /**
     * 相邻两行在设计空间的步长。ListView 的 {@code itemsMargin} 为 0，行高 196，整份列表再按
     * {@link #LIST_SCALE_Y} 纵向缩放，因此四行正好落在 {@code 785×0.9376} 的可视高度内。
     */
    public static final float LIST_ROW_STEP = ROW_HEIGHT * LIST_SCALE_Y;

    /**
     * {@code KW_ITEM_BG}：{@code settle_split_line.png} 1920×72，anchor (0.5,1) 挂在行内
     * Cocos {@code (960,196)}，也就是贴住行顶的一条分隔线——这是每一行唯一的常驻底图。
     * 不要拿 {@link #ROW_CAISHEN_BG} 或封顶角标 {@code mah_settle_df.png} 顶替：前者 CSB
     * 默认 {@code visible=False}，后者只有 100×55，铺满整行会被横向拉伸 19 倍。
     */
    public static final Node ROW_SPLIT_LINE =
            new Node("KW_ITEM_BG", 960.0f, 72.0f / 2.0f, 1920.0f, 72.0f);

    /** {@code _KW_ITEM_BG_CAISHEN}：1912×167，请财神道具生效的行专用底图，CSB 默认隐藏。 */
    public static final Node ROW_CAISHEN_BG = row("_KW_ITEM_BG_CAISHEN", 960.0f, 111.15f, 1912.0f, 167.0f);

    /** {@code _KW_IMG_HEAD_FRAME}：105×106，挂在 0.8 缩放的 {@code KW_PANEL_CONTENT} 下。 */
    public static final Node ROW_HEAD = rowContent("_KW_IMG_HEAD_FRAME", 0.0f, 0.0f, 105.0f, 106.0f);

    /** {@code _KW_IMG_WIND}：50×50 风位角标，相对头像 {@code (-40,-40)}。 */
    public static final Node ROW_WIND = rowContent("_KW_IMG_WIND", -40.0f, -40.0f, 50.0f, 50.0f);

    /** {@code _KW_TEXT_NICKNAME}：行内昵称。 */
    public static final Node ROW_NICKNAME = row("_KW_TEXT_NICKNAME", 90.0f, 67.5f, 104.0f, 26.0f);

    /** {@code _KW_TEXT_NUMBER_ID}：玩家公开 ID。 */
    public static final Node ROW_PLAYER_ID = row("_KW_TEXT_NUMBER_ID", 90.0f, 36.0f, 104.0f, 26.0f);

    /** {@code _KW_LABEL_DETAIL}：胡数/台数/总胡数 文本，anchor (0,0.5)。 */
    public static final float ROW_DETAIL_LEFT = 183.0f;

    public static final float ROW_DETAIL_CENTER_Y = ROW_HEIGHT - 166.0f;

    /** {@code _KW_PANEL_HAND_CARD}：手牌容器，anchor (0,0)，0.5 缩放。 */
    public static final float ROW_HAND_LEFT = 180.0f;

    public static final float ROW_HAND_BOTTOM_COCOS = 35.0f;

    /**
     * 手牌牌距公式（缺陷 #6 闭合证据，不再写「推算」）：
     * {@code UIMahHandArea.luac:192} 的
     * {@code mahPos.x = startPos.x + (index-1) * mah:getTopEdgeWidth() * AddDirection}
     * （手牌 {@code HandMahScale=1}）× {@code MahTopEdgeWidth[STAND_FACE_FORWARD]=135} ×
     * 本面板 0.5 缩放 = 67.5 设计单位。
     */
    public static final float ROW_HAND_SCALE = 0.5f;

    /**
     * {@code _KW_BTN_SHUFFLE_DIRECT} 的消耗行子节点（Settle.csb，按钮设计原点
     * (516.5,939.5)）：「（消耗」右对齐 + fk.png 图标（130×74 的 0.5 缩放）+「xN）」左对齐，
     * 文案规则见 {@code WinLost/View.luac:498}（{@code "x" .. count .. "）"}）。
     */
    public static final float SHUFFLE_COST_NAME_RIGHT = 746.5f;

    public static final Node SHUFFLE_COST_ICON =
            new Node("_KW_IMG_SHUFFLE_PROP_DIRECT", 771.5f, 1005.5f, 65.0f, 37.0f);

    public static final float SHUFFLE_COST_COUNT_LEFT = 796.5f;

    public static final float SHUFFLE_COST_CENTER_Y = 1005.5f;

    /** {@code _KW_PANEL_FLOWER}：花牌区，anchor (1,0)，0.35 缩放。 */
    public static final float ROW_FLOWER_RIGHT = 1200.0f;

    public static final float ROW_FLOWER_SCALE = 0.35f;

    /** {@code _KW_IMG_END_ICON}：90×90 结果标（胡/点炮/…）。 */
    public static final Node ROW_END_ICON = row("_KW_IMG_END_ICON", 1300.0f, 84.0f, 90.0f, 90.0f);

    /** {@code _KW_FNT_FAN}：107×60 番数。 */
    public static final Node ROW_FAN = row("_KW_FNT_FAN", 1437.0f, 84.4771f, 107.0f, 60.0f);

    /** {@code _KW_FNT_GANGFEN}：107×60 杠分，CSB 默认 {@code visible=False}，同 {@link #TITLE_GANG}。 */
    public static final Node ROW_GANG = row("_KW_FNT_GANGFEN", 1565.0f, 84.0001f, 107.0f, 60.0f);

    /**
     * {@code _KW_FNT_HEJI}：186×90，字体 {@code lose_number-export.fnt}，负分走这一格。
     *
     * <p>{@link #ROW_WIN} 用 {@code win_number-export.fnt} 且 CSB 默认 {@code visible=False}，
     * 两格几乎重叠：原版按得分正负只显示其中一个，不是「合计 + 大号得分」两列同时画。
     */
    public static final Node ROW_HEJI = row("_KW_FNT_HEJI", 1769.0f, 87.0f, 186.0f, 90.0f);

    /** {@code KW_FNT_WIN}：189×90 正分格，anchor (1,0.5)，右缘 1862。 */
    public static final Node ROW_WIN =
            new Node("KW_FNT_WIN", 1862.0f - 189.0f / 2.0f, ROW_HEIGHT - 87.0f, 189.0f, 90.0f);

    private TaizhouSettleLayout() {}

    /** {@code Panel_1} 局部 Cocos 坐标 → 顶部原点设计坐标。 */
    private static Node content(String name, float x, float cocosY, float width, float height) {
        return new Node(name, x, CONTENT_TOP + (CONTENT_HEIGHT - cocosY), width, height);
    }

    /** anchor (0.5,1) 的节点：Cocos Y 是顶边。 */
    private static Node contentTop(String name, float x, float cocosTopY, float width, float height) {
        float top = CONTENT_TOP + (CONTENT_HEIGHT - cocosTopY);
        return new Node(name, x, top + height / 2.0f, width, height);
    }

    /** {@code KW_PANEL_BOTTOM_CENTER} 下的按钮，Cocos Y 从屏幕底部起算。 */
    private static Node bottom(String name, float x, float cocosY, float width, float height) {
        return new Node(name, x, DESIGN_HEIGHT - cocosY, width, height);
    }

    /** 行内 Cocos 坐标 → 行内顶部原点坐标。 */
    private static Node row(String name, float x, float cocosY, float width, float height) {
        return new Node(name, x, ROW_HEIGHT - cocosY, width, height);
    }

    /** {@code KW_PANEL_CONTENT} 挂在行内 {@code (90,135)} 且 0.8 缩放。 */
    private static Node rowContent(
            String name, float relativeX, float relativeY, float width, float height) {
        float panelX = 90.0f;
        float panelCocosY = 135.0f;
        float scale = 0.8f;
        return new Node(
                name,
                panelX + relativeX * scale,
                ROW_HEIGHT - (panelCocosY + relativeY * scale),
                width * scale,
                height * scale);
    }
}
