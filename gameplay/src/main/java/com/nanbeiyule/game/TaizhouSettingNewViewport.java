package com.nanbeiyule.game;

/**
 * 把 Cocos 的边缘吸附（{@code HorizontalEdge / VerticalEdge}）搬到 1920×1080 设计坐标上。
 *
 * <p>{@code _KW_PANAEL_BG} 是 BothEdge，运行期铺满可视区；{@code _KW_PANAEL_SET_ROOT} 是
 * RightEdge + BothEdge，所以面板贴可视区右缘、竖向拉满。面板内的子节点分两类：标了
 * {@code VerticalEdge=TopEdge} 的保持距顶距离，没标或 {@code BottomEdge} 的按 Cocos
 * 左下原点走，随面板底边下移——原版在非 16:9 窗口下音效/音乐比语音/纯净模式低，就是这条规则。
 */
record TaizhouSettingNewViewport(float top, float bottom, float right) {
    static final TaizhouSettingNewViewport DESIGN =
            new TaizhouSettingNewViewport(
                    0.0f, TaizhouSettingNewLayout.DESIGN_HEIGHT,
                    TaizhouSettingNewLayout.DESIGN_WIDTH);

    static TaizhouSettingNewViewport of(AdaptiveViewport viewport) {
        AdaptiveViewportTypes.Rect visible = viewport.visibleDesignRect();
        return new TaizhouSettingNewViewport(visible.top(), visible.bottom(), visible.right());
    }

    /** TopEdge 子节点的 Y 偏移。 */
    float topOffset() {
        return top;
    }

    /** BottomEdge 与无 VerticalEdge 子节点的 Y 偏移。 */
    float bottomOffset() {
        return bottom - TaizhouSettingNewLayout.DESIGN_HEIGHT;
    }

    /** 菜单面板展开后的左边（贴可视区右缘）。 */
    float menuOpenX() {
        return right - TaizhouSettingNewLayout.MENU_WIDTH;
    }

    /** 菜单面板收起后的左边。 */
    float menuClosedX() {
        return right;
    }
}
