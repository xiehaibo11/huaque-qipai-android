package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Canvas;
import com.nanbeiyule.game.TaizhouSettingNewLayout.Page;

/** 组合 SettingNew.csb 的菜单与详情两层。 */
final class TaizhouSettingNewRenderer {
    private final TaizhouSettingNewMenuRenderer menu;
    private final TaizhouSettingNewDetailRenderer detail;
    private final TaizhouSettingNewSkin skin;

    TaizhouSettingNewRenderer(Context context) {
        skin = new TaizhouSettingNewSkin(context);
        menu = new TaizhouSettingNewMenuRenderer(skin);
        detail = new TaizhouSettingNewDetailRenderer(
                skin, new TaizhouSettingNewAnimations(context.getAssets()));
    }

    void draw(
            Canvas canvas,
            TaizhouSettingView view,
            float rootX,
            Page page,
            PersonalCenterSystemSettings settings,
            TaizhouMahjongPreferences preferences,
            TaizhouSettingStyle style,
            float scrollY,
            float seconds,
            boolean goldRoom,
            TaizhouSettingNewViewport viewport) {
        canvas.save();
        canvas.translate(rootX, 0.0f);
        if (page != null) {
            detail.draw(canvas, view, page, settings, preferences, style, scrollY, seconds,
                    viewport);
        }
        menu.draw(canvas, view, page, settings, style, goldRoom, viewport);
        canvas.restore();
        if (page != null) {
            // 全屏详情态下关闭按钮贴屏幕右缘（View.lua:moveInDetailAnimation）。
            TaizhouSettingNewLayout.Box close =
                    TaizhouSettingNewLayout.detailCloseButton(viewport);
            view.drawCentered(canvas, skin.close, close.centerX(), close.centerY(),
                    close.width(), close.height());
        }
    }
}
