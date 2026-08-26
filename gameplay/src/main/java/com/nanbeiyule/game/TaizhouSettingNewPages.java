package com.nanbeiyule.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.nanbeiyule.game.TaizhouSettingNewAdvancedLayout.Toggle;
import com.nanbeiyule.game.TaizhouSettingNewLayout.Box;
import com.nanbeiyule.game.TaizhouSettingStyle.Choice;
import com.nanbeiyule.game.TaizhouSettingStyle.Slider;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayGesture;

/** 六个详情页的页面内容。 */
final class TaizhouSettingNewPages {
    private static final float LABEL_SIZE = 43.0f;
    private static final int LABEL_COLOR = TaizhouSettingNewLayout.LABEL_COLOR;

    private final TaizhouSettingNewSkin skin;
    private final TaizhouSettingNewDetailRenderer renderer;
    private final TaizhouSettingNewAnimations animations;

    TaizhouSettingNewPages(
            TaizhouSettingNewSkin skin,
            TaizhouSettingNewDetailRenderer renderer,
            TaizhouSettingNewAnimations animations) {
        this.skin = skin;
        this.renderer = renderer;
        this.animations = animations;
    }

    void drawMahjong(Canvas canvas, TaizhouSettingView view, TaizhouSettingStyle style) {
        Box preview = TaizhouSettingNewDetailLayout.MAH_PREVIEW;
        view.drawCentered(canvas, skin.previewBackground, preview.centerX(), preview.centerY(),
                preview.width(), preview.height());
        TaizhouSettingNewTile.draw(canvas, view, skin, style,
                new Box(preview.centerX(), preview.centerY(), 101.0f * 0.85f, 144.0f * 0.85f));

        drawSliderRow(canvas, view, TaizhouSettingNewDetailLayout.MAH_HEIGHT,
                "高度:", "低", "高", 99.0f, 45.0f, 44.0f, 191.0f, 542.0f, false,
                style.value(Slider.CARD_HEIGHT));
        drawSliderRow(canvas, view, TaizhouSettingNewDetailLayout.MAH_WIDTH,
                "厚度:", "薄", "厚", 99.0f, 45.0f, 44.0f, 191.0f, 542.0f, false,
                style.value(Slider.CARD_WIDTH));
        renderer.drawLine(canvas, view, TaizhouSettingNewDetailLayout.MAH_LINE);

        label(canvas, view, TaizhouSettingNewDetailLayout.MAH_WORD
                .boxLeft(0.0f, 0.0f, 99.0f, 51.0f), "牌花:");
        drawChoiceRow(canvas, view, style, Choice.WORD_TYPE);

        drawSliderRow(canvas, view, TaizhouSettingNewDetailLayout.MAH_WORD_SIZE,
                "牌花大小:", "小", "大", 185.0f, 45.0f, 46.0f, 310.0f, 732.0f, true,
                style.value(Slider.CARD_WORD_SIZE));

        label(canvas, view, TaizhouSettingNewDetailLayout.MAH_BACK
                .boxLeft(0.0f, 0.0f, 99.0f, 51.0f), "牌背:");
        drawChoiceRow(canvas, view, style, Choice.BACK_TYPE);

        label(canvas, view, TaizhouSettingNewDetailLayout.MAH_BODY
                .boxLeft(0.0f, 0.0f, 99.0f, 51.0f), "牌型:");
        drawChoiceRow(canvas, view, style, Choice.BODY_TYPE);

        label(canvas, view, TaizhouSettingNewDetailLayout.MAH_FACE
                .boxLeft(0.0f, 0.0f, 99.0f, 51.0f), "牌面:");
        drawChoiceRow(canvas, view, style, Choice.FACE_TYPE);
    }

    void drawTables(
            Canvas canvas, TaizhouSettingView view, TaizhouSettingStyle style, float scrollY) {
        canvas.save();
        canvas.clipRect(TaizhouSettingNewDetailLayout.TABLE_VIEWPORT.rect());
        canvas.translate(0.0f, -scrollY);
        drawChoiceRow(canvas, view, style, Choice.TABLE_STYLE);
        canvas.restore();
    }

    void drawAnimation(
            Canvas canvas,
            TaizhouSettingView view,
            TaizhouSettingStyle style,
            float scrollY,
            float seconds) {
        canvas.save();
        canvas.clipRect(TaizhouSettingNewDetailLayout.ANIMATION_VIEWPORT.rect());
        canvas.translate(0.0f, -scrollY);
        renderer.drawLine(canvas, view, TaizhouSettingNewDetailLayout.ANIMATION_LINE_1);
        renderer.drawLine(canvas, view, TaizhouSettingNewDetailLayout.ANIMATION_LINE_2);
        drawChoiceRow(canvas, view, style, Choice.OUT_MOVE_STYLE, seconds);
        drawChoiceRow(canvas, view, style, Choice.INSERT_STYLE, seconds);
        drawChoiceRow(canvas, view, style, Choice.OUT_STYLE);
        canvas.restore();
    }

    void drawEffects(Canvas canvas, TaizhouSettingView view, TaizhouSettingStyle style) {
        drawChoiceRow(canvas, view, style, Choice.OUT_EFFECTS);
        renderer.drawLine(canvas, view, TaizhouSettingNewDetailLayout.EFFECTS_LINE);
    }

    void drawHand(Canvas canvas, TaizhouSettingView view, TaizhouSettingStyle style) {
        label(canvas, view, TaizhouSettingNewDetailLayout.HAND_STYLE_LABEL, "倒牌样式:");
        drawChoiceRow(canvas, view, style, Choice.HAND_STYLE);
        Box arrow = TaizhouSettingNewDetailLayout.handStyleArrow();
        view.drawCentered(canvas, skin.handArrow, arrow.centerX(), arrow.centerY(),
                arrow.width(), arrow.height());

        label(canvas, view, TaizhouSettingNewDetailLayout.HAND_SORT_LABEL, "倒牌方向:");
        drawChoiceRow(canvas, view, style, Choice.HAND_SORT_STYLE);

        label(canvas, view, TaizhouSettingNewDetailLayout.OUT_TABLE_LABEL, "摆牌方式:");
        drawChoiceRow(canvas, view, style, Choice.OUT_TABLE_CARD_STYLE);
    }

    void drawAdvanced(
            Canvas canvas,
            TaizhouSettingView view,
            PersonalCenterSystemSettings settings,
            TaizhouMahjongPreferences preferences,
            TaizhouSettingNewViewport viewport) {
        for (Toggle toggle : Toggle.values()) {
            boolean on = switch (toggle) {
                case TING_HINT -> preferences.tingHintEnabled();
                case PLAY_MODE -> preferences.playMode()
                        == TaizhouMahjongPlayGesture.Mode.SINGLE_CLICK;
                case SOUND -> settings.soundEnabled();
                case PURE_MODE -> preferences.pureModeEnabled();
                case MUSIC -> settings.musicEnabled();
            };
            int save = canvas.save();
            // 音效/音乐在 CSB 里没有 VerticalEdge，跟随面板底边（Toggle.bottomAnchored）。
            canvas.translate(0.0f, toggle.bottomAnchored()
                    ? viewport.bottomOffset() - viewport.topOffset() : 0.0f);
            Box box = toggle.box();
            view.drawScale9(canvas, on ? skin.switchOn : skin.switchOff, box.rect(),
                    TaizhouSettingNewDetailLayout.SWITCH_CAPS[0],
                    TaizhouSettingNewDetailLayout.SWITCH_CAPS[1],
                    TaizhouSettingNewDetailLayout.SWITCH_CAPS[2],
                    TaizhouSettingNewDetailLayout.SWITCH_CAPS[3]);
            view.drawLabel(canvas, toggle.label(), toggle.labelBox().rect(),
                    Toggle.LABEL_SIZE, LABEL_COLOR, Paint.Align.LEFT);
            view.drawLabel(canvas, toggle.onText(), toggle.onBox().rect(),
                    Toggle.SEGMENT_SIZE, TaizhouSettingNewLayout.TEXT_COLOR, Paint.Align.CENTER);
            view.drawLabel(canvas, toggle.offText(), toggle.offBox().rect(),
                    Toggle.SEGMENT_SIZE, TaizhouSettingNewLayout.TEXT_COLOR, Paint.Align.CENTER);
            canvas.restoreToCount(save);
        }
        drawVoiceList(canvas, view, preferences);
    }

    /** {@code View.lua:initYunyinBtns}：按文字宽度左右各留 30 排布方言按钮。 */
    private void drawVoiceList(
            Canvas canvas, TaizhouSettingView view, TaizhouMahjongPreferences preferences) {
        view.drawLabel(canvas, "语音", TaizhouSettingNewAdvancedLayout.VOICE_LABEL.rect(),
                Toggle.LABEL_SIZE, LABEL_COLOR, Paint.Align.LEFT);
        String[] names = TaizhouSettingNewInteraction.VOICE_NAMES;
        float total = 0.0f;
        for (String name : names) {
            total += view.measureText(name, TaizhouSettingNewAdvancedLayout.VOICE_TEXT_SIZE)
                    + TaizhouSettingNewAdvancedLayout.VOICE_BUTTON_PADDING * 2.0f;
        }
        view.drawScale9(canvas, skin.voiceListBackground,
                TaizhouSettingNewAdvancedLayout.voiceList(total).rect(), 21.0f, 34.0f, 20.0f, 26.0f);
        float cursor = TaizhouSettingNewAdvancedLayout.VOICE_LIST_LEFT;
        for (int index = 0; index < names.length; index++) {
            float width = view.measureText(names[index],
                    TaizhouSettingNewAdvancedLayout.VOICE_TEXT_SIZE)
                    + TaizhouSettingNewAdvancedLayout.VOICE_BUTTON_PADDING * 2.0f;
            Box box = TaizhouSettingNewAdvancedLayout.voiceButton(cursor + width / 2.0f, width);
            boolean selected = index == (preferences.dialectEnabled() ? 1 : 0);
            if (selected) {
                view.drawScale9(canvas, skin.voiceButton, box.rect(),
                        TaizhouSettingNewDetailLayout.VOICE_CAPS[0],
                        TaizhouSettingNewDetailLayout.VOICE_CAPS[1],
                        TaizhouSettingNewDetailLayout.VOICE_CAPS[2],
                        TaizhouSettingNewDetailLayout.VOICE_CAPS[3]);
            }
            view.drawLabel(canvas, names[index], box.rect(),
                    TaizhouSettingNewAdvancedLayout.VOICE_TEXT_SIZE,
                    TaizhouSettingNewLayout.TEXT_COLOR, Paint.Align.CENTER);
            cursor += width;
        }
    }

    private void drawSliderRow(
            Canvas canvas,
            TaizhouSettingView view,
            TaizhouSettingNewLayout.Frame group,
            String title,
            String low,
            String high,
            float titleWidth,
            float lowWidth,
            float highWidth,
            float trackX,
            float trackWidth,
            boolean wide,
            float percent) {
        label(canvas, view, group.boxLeft(0.0f, 0.0f, titleWidth, 51.0f), title);
        float lowX = wide ? 225.0f : 110.0f;
        float highX = wide ? 1080.0f : 773.0f;
        label(canvas, view, group.boxLeft(lowX, 0.0f, lowWidth, 51.0f), low);
        label(canvas, view, group.boxLeft(highX, 0.0f, highWidth, 51.0f), high);
        renderer.drawSlider(canvas, view,
                group.boxLeft(trackX, 0.0f, trackWidth, 53.0f),
                group.box(trackX + trackWidth / 2.0f, -30.0f, 70.0f, 42.0f),
                wide, percent);
    }

    private void label(Canvas canvas, TaizhouSettingView view, Box box, String text) {
        view.drawLabel(canvas, text, box.rect(), LABEL_SIZE, LABEL_COLOR, Paint.Align.LEFT);
    }

    private void drawChoiceRow(
            Canvas canvas, TaizhouSettingView view, TaizhouSettingStyle style, Choice choice) {
        drawChoiceRow(canvas, view, style, choice, Float.NaN);
    }

    private void drawChoiceRow(
            Canvas canvas,
            TaizhouSettingView view,
            TaizhouSettingStyle style,
            Choice choice,
            float seconds) {
        TaizhouSettingArea7109Catalog catalog = renderer.catalog();
        int selected = catalog.localIndex(choice, style.value(choice));
        int count = catalog.options(choice).length;
        for (int index = 0; index < count; index++) {
            int slot = index;
            renderer.drawOption(canvas, view,
                    TaizhouSettingNewOptions.option(choice, index),
                    skin.image(choice, catalog.realValue(choice, index)),
                    index == selected,
                    TaizhouSettingNewOptions.selectCaps(choice),
                    TaizhouSettingNewOptions.textSize(choice),
                    TaizhouSettingNewAnimations.handles(choice)
                            ? (target, box) ->
                                    animations.draw(target, choice, slot, box.rect(), seconds)
                            : null);
        }
    }
}
