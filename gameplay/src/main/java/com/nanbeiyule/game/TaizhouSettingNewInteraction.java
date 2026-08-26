package com.nanbeiyule.game;

import com.nanbeiyule.game.TaizhouSettingNewAdvancedLayout.Toggle;
import com.nanbeiyule.game.TaizhouSettingNewLayout.Box;
import com.nanbeiyule.game.TaizhouSettingNewLayout.Page;
import com.nanbeiyule.game.TaizhouSettingStyle.Choice;
import com.nanbeiyule.game.TaizhouSettingStyle.Slider;

/** SettingNew.csb 的命中测试，坐标与 {@link TaizhouSettingNewLayout} 同一套。 */
final class TaizhouSettingNewInteraction {
    /** {@code GameDialectCfg[0]}：30109 没有独立配置，回落到普通话/本地话两项。 */
    static final String[] VOICE_NAMES = {"普通话", "本地话"};

    record Selection(Choice choice, int index) {}

    record SliderHit(Slider slider, float percent) {}

    record ToggleHit(Toggle toggle, boolean on) {}

    static Page menuPageAt(float localX, float y) {
        for (Page page : Page.values()) {
            if (page.card().contains(localX, y)) {
                return page;
            }
        }
        return null;
    }

    static boolean closeContains(float localX, float y) {
        return TaizhouSettingNewLayout.CLOSE_BUTTON.contains(localX, y);
    }

    static boolean roomButtonContains(float localX, float y, boolean goldRoom) {
        Box box = goldRoom
                ? TaizhouSettingNewLayout.BACK_BUTTON
                : TaizhouSettingNewLayout.DISMISS_BUTTON;
        return box.contains(localX, y);
    }

    static boolean saveButtonContains(float localX, float y) {
        return TaizhouSettingNewLayout.SAVE_BUTTON.contains(localX, y);
    }

    static boolean voiceSwitchContains(float localX, float y) {
        return TaizhouSettingNewLayout.VOICE_SWITCH.contains(localX, y);
    }

    static boolean trustButtonContains(float localX, float y) {
        return TaizhouSettingNewLayout.TRUST_BUTTON.contains(localX, y);
    }

    /** 底部方案条：返回按钮下标，未命中返回 -1。 */
    static int planAt(float detailX, float y, int planCount) {
        for (int index = 0; index <= planCount; index++) {
            if (TaizhouSettingNewDetailLayout.plan(index).contains(detailX, y)) {
                return index;
            }
        }
        return -1;
    }

    static Selection optionAt(Page page, float detailX, float y, float scrollY) {
        TaizhouSettingArea7109Catalog catalog = TaizhouSettingArea7109Catalog.original();
        float localY = y + scrollY;
        for (Choice choice : TaizhouSettingNewOptions.choices(page)) {
            int count = catalog.options(choice).length;
            for (int index = 0; index < count; index++) {
                if (TaizhouSettingNewOptions.option(choice, index).contains(detailX, localY)) {
                    return new Selection(choice, index);
                }
            }
        }
        return null;
    }

    static ToggleHit toggleAt(float detailX, float topY, float bottomY) {
        for (Toggle toggle : Toggle.values()) {
            Boolean segment =
                    toggle.segmentAt(detailX, toggle.bottomAnchored() ? bottomY : topY);
            if (segment != null) {
                return new ToggleHit(toggle, segment);
            }
        }
        return null;
    }

    /** 语音列表按钮：宽度按文字实测，与 {@code initYunyinBtns} 同算法。 */
    static int voiceIndexAt(TaizhouToolView view, float detailX, float y) {
        float cursor = TaizhouSettingNewAdvancedLayout.VOICE_LIST_LEFT;
        for (int index = 0; index < VOICE_NAMES.length; index++) {
            float width = view.measureText(VOICE_NAMES[index],
                    TaizhouSettingNewAdvancedLayout.VOICE_TEXT_SIZE)
                    + TaizhouSettingNewAdvancedLayout.VOICE_BUTTON_PADDING * 2.0f;
            if (TaizhouSettingNewAdvancedLayout.voiceButton(cursor + width / 2.0f, width)
                    .contains(detailX, y)) {
                return index;
            }
            cursor += width;
        }
        return -1;
    }

    /** 麻将页的三条滑条：命中后按 X 折算百分比。 */
    static SliderHit sliderAt(float detailX, float y) {
        SliderHit height = hit(Slider.CARD_HEIGHT,
                TaizhouSettingNewDetailLayout.MAH_HEIGHT.boxLeft(191.0f, 0.0f, 542.0f, 53.0f),
                detailX, y);
        if (height != null) {
            return height;
        }
        SliderHit width = hit(Slider.CARD_WIDTH,
                TaizhouSettingNewDetailLayout.MAH_WIDTH.boxLeft(191.0f, 0.0f, 542.0f, 53.0f),
                detailX, y);
        if (width != null) {
            return width;
        }
        return hit(Slider.CARD_WORD_SIZE,
                TaizhouSettingNewDetailLayout.MAH_WORD_SIZE.boxLeft(310.0f, 0.0f, 732.0f, 53.0f),
                detailX, y);
    }

    private static SliderHit hit(Slider slider, Box track, float x, float y) {
        Box touch = new Box(track.centerX(), track.centerY(), track.width() + 74.0f, 90.0f);
        if (!touch.contains(x, y)) {
            return null;
        }
        float percent = (x - track.left()) / track.width();
        return new SliderHit(slider, Math.max(0.0f, Math.min(1.0f, percent)));
    }

    private TaizhouSettingNewInteraction() {}
}
