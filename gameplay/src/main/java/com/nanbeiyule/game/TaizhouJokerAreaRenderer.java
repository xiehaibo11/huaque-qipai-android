package com.nanbeiyule.game;

import android.graphics.Canvas;
import com.nanbeiyule.game.mahjong.OriginalMahjongTileDrawPlan;
import com.nanbeiyule.game.mahjong.OriginalMahjongTilePainter;
import com.nanbeiyule.game.mahjong.TaizhouJokerAreaLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongVisibleRound;
import java.util.Objects;

/**
 * 画牌桌中心的财神指示牌（原版 {@code KW_JOKER_MAH} / {@code UIMahJokerArea}）。
 *
 * <p>原版在 {@code UIMahLayer.lua:114-128} 用本局翻出的财神值调用
 * {@code setJokerMahs}，每张都是 {@code LIE_FACE_UP_VERTICAL_TOUP} 的正面牌并
 * {@code showJokerIcon(true)}。这里几何全部来自 {@link TaizhouJokerAreaLayout}，
 * 牌身与「财」角标全部来自共享的 {@link OriginalMahjongTileDrawPlan}，因此指示牌
 * 与手牌、牌河、副露、结算用的是同一套姿态公式和同一张原版角标帧。
 *
 * <p>未翻得（{@code jokerTiles} 为空）时什么都不画，旧快照缺字段同样按空列表处理。
 */
final class TaizhouJokerAreaRenderer {
    private final OriginalMahjongTilePainter tilePainter;

    TaizhouJokerAreaRenderer(OriginalMahjongTilePainter tilePainter) {
        this.tilePainter = Objects.requireNonNull(tilePainter, "tilePainter");
    }

    void draw(Canvas canvas, TaizhouMahjongVisibleRound round) {
        if (round == null) {
            return;
        }
        for (TaizhouJokerAreaLayout.TilePosition tile :
                TaizhouJokerAreaLayout.tiles(round.jokerTiles())) {
            tilePainter.draw(
                    canvas,
                    OriginalMahjongTileDrawPlan.atAnchor(
                            tile.pose(),
                            tile.tileValue(),
                            tile.designX(),
                            tile.cocosY(),
                            tile.scale(),
                            0.5f,
                            0.5f,
                            true));
        }
    }
}
