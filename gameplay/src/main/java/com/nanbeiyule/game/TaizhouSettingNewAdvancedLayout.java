package com.nanbeiyule.game;

import com.nanbeiyule.game.TaizhouSettingNewLayout.Box;
import com.nanbeiyule.game.TaizhouSettingNewLayout.Frame;

/**
 * 高级设置页（{@code _KW_SET_DETAIL_6}）的节点几何。
 *
 * <p>出牌轨迹 {@code _KW_IMG_GJ_6} 与 3 维模式在 1.5.4 的 CSB 里是 HIDDEN，方言开关
 * {@code _KW_IMG_FY_4} 摆在 x=2144 的可视区外，三者都不画；语音改用
 * {@code _KW_LISTVIEW_YUYIN_INFO} 的按内容排布列表（{@code View.lua:initYunyinBtns}）。
 */
final class TaizhouSettingNewAdvancedLayout {
    private static final Frame DETAIL = TaizhouSettingNewDetailLayout.DETAIL;

    static final Box BOTTOM_BAR = DETAIL.boxCorner(0.0f, 0.0f, 2000.0f, 102.0f);

    /** 语音列表左端与垂直中心（{@code _KW_LISTVIEW_YUYIN_INFO} anchor(0,0.5)）。 */
    static final float VOICE_LIST_LEFT = 255.25f;
    static final float VOICE_LIST_COCOS_Y = 786.045f;
    static final float VOICE_BUTTON_HEIGHT = 80.0f;
    static final float VOICE_BUTTON_PADDING = 30.0f;
    static final float VOICE_TEXT_SIZE = 40.0f;

    static final Box VOICE_LABEL = DETAIL.boxLeft(93.0836f, 794.546f, 89.0f, 52.0f);

    static Box voiceButton(float centerX, float width) {
        return DETAIL.box(centerX, VOICE_LIST_COCOS_Y, width, VOICE_BUTTON_HEIGHT);
    }

    /** {@code _KW_LISTVIEW_YUYIN_INFO} 自身 79 高的底，宽度等于按钮总宽。 */
    static Box voiceList(float width) {
        return DETAIL.box(VOICE_LIST_LEFT + width / 2.0f, VOICE_LIST_COCOS_Y, width, 79.0f);
    }

    /** 双段开关的一项：行标题、开关底、两段文字。 */
    enum Toggle {
        TING_HINT("听牌提示", false, 55.71f, 945.63f, 177.0f, 430.0f, 945.63f,
                88.2352f, 45.5724f, 251.18f, 45.4875f, "开启", "关闭"),
        PLAY_MODE("出牌方式", false, 645.93f, 950.71f, 178.0f, 1007.61f, 950.71f,
                87.0719f, 43.3316f, 250.018f, 43.2466f, "单击", "双击"),
        SOUND("音效", true, -142.93f, 44.6295f, 102.0f, 1016.2f, 789.69f,
                87.63f, 45.3605f, 250.576f, 45.2756f, "开启", "关闭"),
        PURE_MODE("纯净模式", false, 55.0f, 649.4f, 178.0f, 430.0f, 649.4f,
                84.4453f, 43.739f, 247.388f, 43.6532f, "开启", "关闭"),
        MUSIC("音乐", true, -141.861f, 46.7246f, 102.0f, 1013.0f, 645.09f,
                87.0219f, 40.2549f, 249.97f, 40.1702f, "开启", "关闭");

        static final float WIDTH = 332.0f;
        static final float HEIGHT = 84.0f;
        static final float LABEL_SIZE = 44.0f;
        static final float SEGMENT_SIZE = 40.0f;

        private final String label;
        private final Box labelBox;
        private final Box box;
        private final Box onBox;
        private final Box offBox;
        private final String onText;
        private final String offText;

        Toggle(
                String label,
                boolean labelInsideSwitch,
                float labelX,
                float labelY,
                float labelWidth,
                float centerX,
                float cocosCenterY,
                float onX,
                float onY,
                float offX,
                float offY,
                String onText,
                String offText) {
            this.label = label;
            this.onText = onText;
            this.offText = offText;
            box = DETAIL.box(centerX, cocosCenterY, WIDTH, HEIGHT);
            Frame local = DETAIL.at(centerX - WIDTH / 2.0f, cocosCenterY - HEIGHT / 2.0f);
            onBox = local.box(onX, onY, 81.0f, 47.0f);
            offBox = local.box(offX, offY, 81.0f, 47.0f);
            // 音效/音乐的行标题是开关的子节点（Text_3 / Text_3_0），坐标相对开关左下角。
            labelBox = labelInsideSwitch
                    ? local.boxLeft(labelX, labelY, labelWidth, 52.0f)
                    : DETAIL.boxLeft(labelX, labelY, labelWidth, 52.0f);
        }

        String label() {
            return label;
        }

        Box labelBox() {
            return labelBox;
        }

        Box box() {
            return box;
        }

        /** 左半段（开启/单击）的文字盒。 */
        Box onBox() {
            return onBox;
        }

        /** 右半段（关闭/双击）的文字盒。 */
        Box offBox() {
            return offBox;
        }

        String onText() {
            return onText;
        }

        String offText() {
            return offText;
        }

        /** CSB 里没有 VerticalEdge 的两项（音效/音乐），跟随面板底边。 */
        boolean bottomAnchored() {
            return this == SOUND || this == MUSIC;
        }

        /** 命中左半段返回 true，右半段返回 false，未命中返回 null。 */
        Boolean segmentAt(float x, float y) {
            if (!box.contains(x, y)) {
                return null;
            }
            return x < box.centerX();
        }
    }

    private TaizhouSettingNewAdvancedLayout() {}
}
