package com.nanbeiyule.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.nanbeiyule.game.TaizhouSettingNewLayout.Box;
import com.nanbeiyule.game.TaizhouSettingNewLayout.Page;
import com.nanbeiyule.game.TaizhouSettingStyle.Choice;

/** 画 {@code _KW_PANAEL_SET_ROOT}：667 宽的右侧菜单。 */
final class TaizhouSettingNewMenuRenderer {
    private final TaizhouSettingNewSkin skin;

    TaizhouSettingNewMenuRenderer(TaizhouSettingNewSkin skin) {
        this.skin = skin;
    }

    void draw(
            Canvas canvas,
            TaizhouSettingView view,
            Page selected,
            PersonalCenterSystemSettings settings,
            TaizhouSettingStyle style,
            boolean goldRoom,
            TaizhouSettingNewViewport viewport) {
        // _KW_PANAEL_SET_ROOT 是 BothEdge，底图竖向铺满可视区。
        view.drawScale9(canvas, skin.background,
                new android.graphics.RectF(0.0f, viewport.top(),
                        TaizhouSettingNewLayout.MENU_WIDTH, viewport.bottom()),
                TaizhouSettingNewLayout.BACKGROUND_CAPS[0],
                TaizhouSettingNewLayout.BACKGROUND_CAPS[1],
                TaizhouSettingNewLayout.BACKGROUND_CAPS[2],
                TaizhouSettingNewLayout.BACKGROUND_CAPS[3]);
        int save = canvas.save();
        canvas.translate(0.0f, viewport.topOffset());
        for (Page page : Page.values()) {
            drawCard(canvas, view, page, page == selected, style);
        }
        drawBasicFunctions(canvas, view, settings);
        if (selected == null) {
            drawBitmap(canvas, view, skin.close, TaizhouSettingNewLayout.CLOSE_BUTTON);
        }
        canvas.restoreToCount(save);
        save = canvas.save();
        canvas.translate(0.0f, viewport.bottomOffset());
        drawBottomButton(canvas, view, selected, goldRoom);
        canvas.restoreToCount(save);
    }

    private void drawCard(
            Canvas canvas,
            TaizhouSettingView view,
            Page page,
            boolean selected,
            TaizhouSettingStyle style) {
        view.drawScale9(canvas, skin.card, page.card().rect(),
                TaizhouSettingNewLayout.CARD_CAPS[0], TaizhouSettingNewLayout.CARD_CAPS[1],
                TaizhouSettingNewLayout.CARD_CAPS[2], TaizhouSettingNewLayout.CARD_CAPS[3]);
        float[] frameCaps = page.frameCaps();
        view.drawScale9(canvas, skin.cardFrame, page.frame().rect(),
                frameCaps[0], frameCaps[1], frameCaps[2], frameCaps[3]);
        if (selected) {
            float[] selectedCaps = page.selectedCaps();
            view.drawScale9(canvas, skin.cardSelected, page.selectedFrame().rect(),
                    selectedCaps[0], selectedCaps[1], selectedCaps[2], selectedCaps[3]);
        }
        drawPreview(canvas, view, page, style);
        drawBitmap(canvas, view,
                page == Page.ADVANCED ? skin.plateWide : skin.plate, page.plate());
        view.drawLabel(canvas, page.label(), page.title().rect(), page.titleSize(),
                TaizhouSettingNewLayout.TEXT_COLOR, Paint.Align.CENTER);
    }

    /** {@code _KW_SCAN_MAH_IMG_BG} / {@code _KW_SCAN_TABLE_COLOR_IMG}：跟随当前外观。 */
    private void drawPreview(
            Canvas canvas, TaizhouSettingView view, Page page, TaizhouSettingStyle style) {
        Box preview = page.preview();
        if (preview == null) {
            return;
        }
        if (page == Page.TABLE) {
            drawBitmap(canvas, view,
                    skin.image(Choice.TABLE_STYLE, style.value(Choice.TABLE_STYLE)), preview);
            return;
        }
        TaizhouSettingNewTile.draw(canvas, view, skin, style, preview);
    }

    private void drawBasicFunctions(
            Canvas canvas, TaizhouSettingView view, PersonalCenterSystemSettings settings) {
        view.drawLabel(canvas, "出牌语音", TaizhouSettingNewLayout.VOICE_LABEL.rect(), 36.0f,
                TaizhouSettingNewLayout.LABEL_COLOR, Paint.Align.LEFT);
        drawBitmap(canvas, view, settings.maleVoice() ? skin.voiceMale : skin.voiceFemale,
                TaizhouSettingNewLayout.VOICE_SWITCH);
        view.drawLabel(canvas, "超时托管", TaizhouSettingNewLayout.TRUST_LABEL.rect(), 36.0f,
                TaizhouSettingNewLayout.LABEL_COLOR, Paint.Align.LEFT);
        drawBitmap(canvas, view, skin.trust, TaizhouSettingNewLayout.TRUST_BUTTON);
    }

    /**
     * {@code View.lua:updateBtnStatus}：包厢场画 {@code _KW_BTN_DISMISS}（退出房间），
     * 新金币场画 {@code _KW_BTN_BACK_NEW}（返回大厅）；进入详情页后两者都让位给保存。
     */
    private void drawBottomButton(
            Canvas canvas, TaizhouSettingView view, Page selected, boolean goldRoom) {
        if (selected != null) {
            if (selected != Page.ADVANCED) {
                view.drawScale9(canvas, skin.save, TaizhouSettingNewLayout.SAVE_BUTTON.rect(),
                        TaizhouSettingNewLayout.SAVE_BUTTON_CAPS[0],
                        TaizhouSettingNewLayout.SAVE_BUTTON_CAPS[1],
                        TaizhouSettingNewLayout.SAVE_BUTTON_CAPS[2],
                        TaizhouSettingNewLayout.SAVE_BUTTON_CAPS[3]);
            }
            return;
        }
        if (goldRoom) {
            drawBitmap(canvas, view, skin.back, TaizhouSettingNewLayout.BACK_BUTTON);
        } else {
            drawBitmap(canvas, view, skin.quit, TaizhouSettingNewLayout.DISMISS_BUTTON);
        }
    }

    private void drawBitmap(
            Canvas canvas, TaizhouSettingView view, android.graphics.Bitmap bitmap, Box box) {
        view.drawCentered(canvas, bitmap, box.centerX(), box.centerY(),
                box.width(), box.height());
    }
}
