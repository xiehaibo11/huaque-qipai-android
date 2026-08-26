package com.nanbeiyule.game;

import java.nio.charset.StandardCharsets;

/**
 * 原版大厅气泡 BubbleItem 的几何与文字度量。
 *
 * <p>几何取自 {@code hall/main/BubbleItem.csb} 与 {@code prime/hall/style/BubbleItem.lua}：
 * bub 底图 102×43，九宫格 {@code capInsets(51, 22, 10, 43)}，宽度
 * {@code 20 + (#text/3) * 20}（{@code #text} 是 UTF-8 字节数），文字 X 为 {@code 宽/2 - 2}。
 *
 * <p>字体是方正粗圆：闲逸 APK 内唯一 TTF 为 {@code Common/Font/fangzhengcuyuan.TTF}，
 * 另外两个字体资源是只含数字的 BMFont。字号与行距来自实机截图测量，
 * 截图缩放比由人物头部模板匹配定为 1.54，属于截图校准推断而非 CSB 读数。
 */
final class LobbyBubbleLayout {

    /** BubbleItem.csb 里 bub 的原始尺寸。 */
    static final float ORIGINAL_BASE_WIDTH = 102.0f;

    static final float ORIGINAL_BASE_HEIGHT = 43.0f;

    /** capInsets(51, 22, 10, 43) 的左保护宽与中心可拉伸宽。 */
    static final float ORIGINAL_CAP_LEFT = 51.0f;

    static final float ORIGINAL_CAP_CENTER_WIDTH = 10.0f;

    /** BubbleItem.csb 里 text 节点的 fontSize（1334×750 设计空间）。 */
    static final float ORIGINAL_FONT_SIZE = 18.0f;

    /** BubbleItem.csb 里 text 节点的 WidgetOptions 颜色为纯白。 */
    static final int TEXT_COLOR = 0xFFFFFFFF;

    /**
     * BubbleItem.csb 的 fontName 为空且资源串表里是 {@code font/sysfont.ttf}，
     * 即系统默认字体，不是方正粗圆；方正粗圆是大厅其它控件用的。
     */
    static final boolean USES_SYSTEM_FONT = true;

    /** 原版 1334 宽设计空间换算到南北 3200 宽设计空间。 */
    static final float DESIGN_SCALE = 3200.0f / 1334.0f;

    /** BubbleItem.lua 的 {@code getBubbleInterval() or 30}。 */
    private static final float DEFAULT_INTERVAL_SECONDS = 30.0f;

    private LobbyBubbleLayout() {}

    static float originalCapRight() {
        return ORIGINAL_BASE_WIDTH - ORIGINAL_CAP_LEFT - ORIGINAL_CAP_CENTER_WIDTH;
    }

    /** 原版 {@code local charCount = #text/3}，按 UTF-8 字节数除以 3。 */
    static int originalCharCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.getBytes(StandardCharsets.UTF_8).length / 3;
    }

    /** 原版只在 {@code charCount > 0} 时改尺寸，否则保持 csb 原始宽度。 */
    static float originalWidth(String text) {
        int charCount = originalCharCount(text);
        if (charCount <= 0) {
            return ORIGINAL_BASE_WIDTH;
        }
        return 20.0f + charCount * 20.0f;
    }

    static float originalTextCentreX(String text) {
        return originalWidth(text) / 2.0f - 2.0f;
    }

    static float designWidth(String text) {
        return originalWidth(text) * DESIGN_SCALE;
    }

    static float designHeight() {
        return ORIGINAL_BASE_HEIGHT * DESIGN_SCALE;
    }

    static float designFontSize() {
        return ORIGINAL_FONT_SIZE * DESIGN_SCALE;
    }

    static float designCapLeft() {
        return ORIGINAL_CAP_LEFT * DESIGN_SCALE;
    }

    static float designCapRight() {
        return originalCapRight() * DESIGN_SCALE;
    }

    /** 原版 showBuble 只处理 2、3、4，其余分支直接隐藏。 */
    static boolean isPlayableType(Integer type) {
        return type != null && (type == 2 || type == 3 || type == 4);
    }

    static float intervalSeconds(Integer configured) {
        if (configured == null || configured <= 0) {
            return DEFAULT_INTERVAL_SECONDS;
        }
        return configured;
    }
}
