package com.nanbeiyule.game;

import java.util.Map;

/**
 * 加入房间弹层几何，1920x1080 左上角坐标系。
 *
 * <p>版式按用户提供的闲逸斗地主 QuicklyJoin 参考图映射：银灰玻璃外框 + 奶白本体，顶部宽幅卷纹
 * 标题冠压住外框上沿，右上角挂件式关闭键，六个独立圆槽一排，键盘 4 列 3 行，右列自上而下为
 * 退格、0、重输。
 *
 * <p>行为证据 CONFIRMED：{@code artifacts/xianyi-doudizhu/local-mac-extract/decoded-src/prime/
 * hall/joingame/QuiclyJoinGame.lua}（节点 {@code panel_number.number_bg_1..6}、
 * {@code panel_btn.btn_1..9/btn_0/btn_del/btn_redo}，满六位自动提交）。
 *
 * <p>坐标为推断值：该 CSB 不在归档内，下列数值由用户提供的闲逸实机设计图（1146x880）按 1:1
 * 平移到 1920x1080 得到，平移量 {@code (+392, +92.5)}，弹层中心落在 {@code (960,540)}。
 * 不得描述为闲逸 CSB 坐标的恢复。加入流程、六位号码与满位自动提交沿用浙江原版
 * {@code JoinBoxRoom/View.lua} 的语义（{@code checkReqJoinBoxRoom}），后端为南北娱乐自建。
 */
final class JoinRoomLayout {
    static final float DESIGN_WIDTH = 1920f;
    static final float DESIGN_HEIGHT = 1080f;

    /** 银灰外框。参考图 (50,39)-(1086,856)。 */
    static final Rect FRAME = new Rect(442f, 131.5f, 1478f, 948.5f);
    /** 奶白本体。参考图 (71,139)-(1066,830)。 */
    static final Rect BODY = new Rect(463f, 231.5f, 1458f, 922.5f);
    /**
     * 顶部半透明玻璃层。没有恢复到原始贴图；几何按用户最新提供的闲逸截图推断。
     *
     * <p>它覆盖外框上沿至奶白本体之间的整条带，不能退化成一条不透光的深灰矩形。
     */
    static final Rect HEADER =
            new Rect(FRAME.left() + 8f, FRAME.top() + 4f, FRAME.right() - 8f, BODY.top() + 3f);

    /**
     * 标题的宽幅卷纹冠。它比原先的文字牌宽约两倍，轻微越过外框顶边并与本体上沿相接。
     *
     * <p>闲逸归档没有该贴图或 CSB；仅标题文字 {@code joing_title.png} 是已恢复的原始像素。
     */
    static final Rect TITLE_CROWN = new Rect(650f, 124f, 1270f, 222f);
    static final float TITLE_TEXT_CENTER_X = 960f;
    static final float TITLE_TEXT_CENTER_Y = 178f;

    /** 右上角黑桃形关闭键，压住外框右上角。参考图中心 (1039,80)、约 68x75。 */
    static final Rect CLOSE = centered(1431f, 177f, 76f, 84f);

    /** 六个圆槽的中心，对弹层中心 960 对称，间距 143.3、直径 111（参考图实测）。 */
    static final float[] DIGIT_CENTERS_X = {
        601.75f, 745.05f, 888.35f, 1031.65f, 1174.95f, 1318.25f
    };
    static final float DIGIT_CENTER_Y = 346.5f;
    static final float DIGIT_SLOT_DIAMETER = 111f;

    /** 键盘整块外框，四周只留极窄内缩，键与键之间是细缝。 */
    static final Rect KEYPAD = new Rect(488f, 447.5f, 1433f, 887.5f);

    private static final float KEY_WIDTH = 234f;
    private static final float KEY_HEIGHT = 145f;
    private static final float COL_1 = 605.55f;
    private static final float COL_2 = 841.85f;
    private static final float COL_3 = 1078.15f;
    private static final float COL_4 = 1314.45f;
    private static final float ROW_1 = 520.5f;
    private static final float ROW_2 = 667.5f;
    private static final float ROW_3 = 815.5f;

    /** 右列自上而下：退格、0、重输，与 QuiclyJoinGame.lua 的 btn_del / btn_0 / btn_redo 同序。 */
    static final Rect DELETE = keyRect(COL_4, ROW_1);
    static final Rect CLEAR = keyRect(COL_4, ROW_3);

    private static final Map<Integer, Rect> KEYS =
            Map.ofEntries(
                    Map.entry(1, keyRect(COL_1, ROW_1)),
                    Map.entry(2, keyRect(COL_2, ROW_1)),
                    Map.entry(3, keyRect(COL_3, ROW_1)),
                    Map.entry(4, keyRect(COL_1, ROW_2)),
                    Map.entry(5, keyRect(COL_2, ROW_2)),
                    Map.entry(6, keyRect(COL_3, ROW_2)),
                    Map.entry(7, keyRect(COL_1, ROW_3)),
                    Map.entry(8, keyRect(COL_2, ROW_3)),
                    Map.entry(9, keyRect(COL_3, ROW_3)),
                    Map.entry(0, keyRect(COL_4, ROW_2)));

    private JoinRoomLayout() {}

    static Rect key(int digit) {
        Rect rect = KEYS.get(digit);
        if (rect == null) {
            throw new IllegalArgumentException("digit must be between 0 and 9");
        }
        return rect;
    }

    static int digitAt(float x, float y) {
        for (Map.Entry<Integer, Rect> entry : KEYS.entrySet()) {
            if (entry.getValue().contains(x, y)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    static Viewport contain(int width, int height) {
        if (width <= 0 || height <= 0) {
            return new Viewport(1f, 0f, 0f);
        }
        float scale = Math.min(width / DESIGN_WIDTH, height / DESIGN_HEIGHT);
        return new Viewport(
                scale,
                (width - DESIGN_WIDTH * scale) * 0.5f,
                (height - DESIGN_HEIGHT * scale) * 0.5f);
    }

    private static Rect keyRect(float centerX, float centerY) {
        return centered(centerX, centerY, KEY_WIDTH, KEY_HEIGHT);
    }

    private static Rect centered(float centerX, float centerY, float width, float height) {
        return new Rect(
                centerX - width * 0.5f,
                centerY - height * 0.5f,
                centerX + width * 0.5f,
                centerY + height * 0.5f);
    }

    record Rect(float left, float top, float right, float bottom) {
        boolean contains(float x, float y) {
            return x >= left && x <= right && y >= top && y <= bottom;
        }

        float centerX() {
            return (left + right) * 0.5f;
        }

        float centerY() {
            return (top + bottom) * 0.5f;
        }

        float width() {
            return right - left;
        }

        float height() {
            return bottom - top;
        }
    }

    record Viewport(float scale, float offsetX, float offsetY) {
        float designX(float screenX) {
            return (screenX - offsetX) / scale;
        }

        float designY(float screenY) {
            return (screenY - offsetY) / scale;
        }
    }
}
