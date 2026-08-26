package com.nanbeiyule.game;

import android.graphics.Canvas;
import com.nanbeiyule.game.TaizhouSettingNewLayout.Box;
import com.nanbeiyule.game.TaizhouSettingStyle.Choice;

/**
 * 设置里的立牌预览（{@code _KW_SCAN_MAH_IMG_BG} 挂的 UIMah）。
 *
 * <p>原版按 {@code UIMah:setConfig} 叠 牌型 / 牌面 / 牌花 三层，尺寸取自 CSB 里
 * 牌型 102×144、牌面 95×127、牌花 81×108 的选项图。
 */
final class TaizhouSettingNewTile {
    private static final float BODY_WIDTH = 101.0f;
    private static final float BODY_HEIGHT = 144.0f;

    static void draw(
            Canvas canvas,
            TaizhouToolView view,
            TaizhouSettingNewSkin skin,
            TaizhouSettingStyle style,
            Box box) {
        float scale = box.width() / BODY_WIDTH;
        view.drawCentered(canvas, skin.image(Choice.BODY_TYPE, style.value(Choice.BODY_TYPE)),
                box.centerX(), box.centerY(), BODY_WIDTH * scale, BODY_HEIGHT * scale);
        view.drawCentered(canvas, skin.image(Choice.FACE_TYPE, style.value(Choice.FACE_TYPE)),
                box.centerX(), box.centerY(), 95.0f * scale, 127.0f * scale);
        view.drawCentered(canvas, skin.image(Choice.WORD_TYPE, style.value(Choice.WORD_TYPE)),
                box.centerX(), box.centerY(), 81.0f * scale, 108.0f * scale);
    }

    private TaizhouSettingNewTile() {}
}
