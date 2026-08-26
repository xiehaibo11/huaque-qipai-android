package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.nanbeiyule.game.TaizhouSettingNewDetailLayout.Option;
import com.nanbeiyule.game.TaizhouSettingNewLayout.Box;
import com.nanbeiyule.game.TaizhouSettingNewLayout.Page;

/** 画 {@code _KW_PANAEL_SET_DETAIL}：2000 宽的详情区与底部方案条。 */
final class TaizhouSettingNewDetailRenderer {
    private final TaizhouSettingNewSkin skin;
    private final TaizhouSettingNewPages pages;
    private final TaizhouSettingArea7109Catalog catalog =
            TaizhouSettingArea7109Catalog.original();

    TaizhouSettingNewDetailRenderer(
            TaizhouSettingNewSkin skin, TaizhouSettingNewAnimations animations) {
        this.skin = skin;
        pages = new TaizhouSettingNewPages(skin, this, animations);
    }

    void draw(
            Canvas canvas,
            TaizhouSettingView view,
            Page page,
            PersonalCenterSystemSettings settings,
            TaizhouMahjongPreferences preferences,
            TaizhouSettingStyle style,
            float scrollY,
            float seconds,
            TaizhouSettingNewViewport viewport) {
        canvas.save();
        canvas.translate(TaizhouSettingNewLayout.DETAIL_LOCAL_X, 0.0f);
        view.drawScale9(canvas, skin.detailBackground,
                new android.graphics.RectF(0.0f, viewport.top(),
                        TaizhouSettingNewLayout.DETAIL_WIDTH, viewport.bottom()),
                TaizhouSettingNewDetailLayout.BACKGROUND_CAPS[0],
                TaizhouSettingNewDetailLayout.BACKGROUND_CAPS[1],
                TaizhouSettingNewDetailLayout.BACKGROUND_CAPS[2],
                TaizhouSettingNewDetailLayout.BACKGROUND_CAPS[3]);
        int save = canvas.save();
        canvas.translate(0.0f, TaizhouSettingNewOptions.anchorOffset(page, viewport));
        switch (page) {
            case MAH -> pages.drawMahjong(canvas, view, style);
            case TABLE -> pages.drawTables(canvas, view, style, scrollY);
            case ANIMATION -> pages.drawAnimation(canvas, view, style, scrollY, seconds);
            case EFFECTS -> pages.drawEffects(canvas, view, style);
            case HAND -> pages.drawHand(canvas, view, style);
            case ADVANCED -> pages.drawAdvanced(canvas, view, settings, preferences, viewport);
        }
        canvas.restoreToCount(save);
        save = canvas.save();
        // _KW_BOTTOM_NODE / _KW_BOTTOM_NODE_0 都是 BottomEdge。
        canvas.translate(0.0f, viewport.bottomOffset());
        if (page == Page.ADVANCED) {
            drawBar(canvas, view, TaizhouSettingNewAdvancedLayout.BOTTOM_BAR);
        } else {
            drawPlans(canvas, view, style);
        }
        canvas.restoreToCount(save);
        canvas.restore();
    }

    /** {@code _KW_BOTTOM_NODE}：运营方案 + 自定义。 */
    private void drawPlans(Canvas canvas, TaizhouSettingView view, TaizhouSettingStyle style) {
        drawBar(canvas, view, TaizhouSettingNewDetailLayout.BOTTOM_BAR);
        String[] labels = catalog.planLabels();
        int custom = catalog.customPlanIndex();
        int selected = style.playerType() == 0 ? custom : style.playerType() - 1;
        for (int index = 0; index <= custom; index++) {
            if (index == custom && style.playerType() != 0) {
                continue;
            }
            Box box = TaizhouSettingNewDetailLayout.plan(index);
            boolean active = index == selected;
            view.drawCentered(canvas, active ? skin.planSelected : skin.planNormal,
                    box.centerX(), active ? box.centerY() + 1.0f : box.centerY(),
                    box.width(), box.height());
            view.drawLabel(canvas, index == custom ? "自定义" : labels[index], box.rect(),
                    36.0f,
                    active
                            ? TaizhouSettingNewLayout.SELECTED_TEXT_COLOR
                            : TaizhouSettingNewLayout.TEXT_COLOR,
                    Paint.Align.CENTER);
        }
    }

    private void drawBar(Canvas canvas, TaizhouSettingView view, Box box) {
        view.drawScale9(canvas, skin.planBar, box.rect(),
                TaizhouSettingNewDetailLayout.BOTTOM_CAPS[0],
                TaizhouSettingNewDetailLayout.BOTTOM_CAPS[1],
                TaizhouSettingNewDetailLayout.BOTTOM_CAPS[2],
                TaizhouSettingNewDetailLayout.BOTTOM_CAPS[3]);
    }

    /** 选项内容不是静态图时（DragonBones 预览）由调用方画进 {@code _KW_STYEL_IMG} 的位置。 */
    interface Content {
        void draw(Canvas canvas, Box box);
    }

    void drawOption(
            Canvas canvas,
            TaizhouSettingView view,
            Option option,
            Bitmap image,
            boolean selected,
            float[] selectCaps,
            float textSize) {
        drawOption(canvas, view, option, image, selected, selectCaps, textSize, null);
    }

    /**
     * 一个选项：选中时才画底图与角标（{@code setDetailTagSelected} 只切
     * {@code _KW_TYPE_BG} 与 {@code _KW_TYPE_SELECT_IMG} 两个节点）。
     */
    void drawOption(
            Canvas canvas,
            TaizhouSettingView view,
            Option option,
            Bitmap image,
            boolean selected,
            float[] selectCaps,
            float textSize,
            Content content) {
        if (selected) {
            view.drawScale9(canvas, skin.itemBackground, option.background().rect(),
                    TaizhouSettingNewDetailLayout.ITEM_CAPS[0],
                    TaizhouSettingNewDetailLayout.ITEM_CAPS[1],
                    TaizhouSettingNewDetailLayout.ITEM_CAPS[2],
                    TaizhouSettingNewDetailLayout.ITEM_CAPS[3]);
        }
        if (image != null) {
            Box box = option.image();
            view.drawCentered(canvas, image, box.centerX(), box.centerY(),
                    box.width(), box.height());
        }
        if (content != null) {
            content.draw(canvas, option.image());
        }
        if (selected) {
            view.drawScale9(canvas, skin.itemSelected, option.selected().rect(),
                    selectCaps[0], selectCaps[1], selectCaps[2], selectCaps[3]);
        }
        if (option.text() != null) {
            view.drawLabel(canvas, option.text(), option.label().rect(), textSize,
                    TaizhouSettingNewLayout.LABEL_COLOR, Paint.Align.CENTER);
        }
    }

    /** Cocos Slider：底槽 + 进度条 + 滑块，百分比标签在滑块下方。 */
    void drawSlider(
            Canvas canvas,
            TaizhouSettingView view,
            Box track,
            Box percentLabel,
            boolean wide,
            float percent) {
        view.drawCentered(canvas, wide ? skin.sliderTrackWide : skin.sliderTrackNarrow,
                track.centerX(), track.centerY(), track.width(), track.height());
        float fillWidth = (wide ? 728.0f : 538.0f) * percent;
        if (fillWidth > 1.0f) {
            float left = track.centerX() - track.width() / 2.0f + (wide ? 2.0f : 2.0f);
            view.drawBitmap(canvas, wide ? skin.sliderFillWide : skin.sliderFillNarrow,
                    new android.graphics.RectF(left, track.centerY() - 23.5f,
                            left + fillWidth, track.centerY() + 23.5f));
        }
        float thumbX = track.centerX() - track.width() / 2.0f + track.width() * percent;
        view.drawCentered(canvas, skin.sliderThumb, thumbX, track.centerY(), 74.0f, 77.0f);
        view.drawLabel(canvas, Math.round(percent * 100.0f) + "%", percentLabel.rect(), 36.0f,
                TaizhouSettingNewLayout.LABEL_COLOR, Paint.Align.CENTER);
    }

    void drawLine(Canvas canvas, TaizhouSettingView view, Box box) {
        view.drawCentered(canvas, skin.line, box.centerX(), box.centerY(),
                box.width(), box.height());
    }

    TaizhouSettingArea7109Catalog catalog() {
        return catalog;
    }
}
