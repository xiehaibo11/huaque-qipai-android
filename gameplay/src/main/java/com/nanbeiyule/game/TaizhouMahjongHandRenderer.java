package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import com.nanbeiyule.game.gameplay.GameplayMeld;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.gameplay.GameplayActionOffer;
import com.nanbeiyule.game.mahjong.MahjongTileSprite;
import com.nanbeiyule.game.mahjong.OriginalMahjongTileDrawPlan;
import com.nanbeiyule.game.mahjong.OriginalMahjongTileGeometry;
import com.nanbeiyule.game.mahjong.OriginalMahjongTilePainter;
import com.nanbeiyule.game.mahjong.TaizhouMahjongHandLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongHandProjection;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayGesture;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayInteraction;
import com.nanbeiyule.game.mahjong.TaizhouMahjongSeatMapper;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongVisibleRound;
import com.nanbeiyule.game.mahjong.round.MahjongPower;
import java.util.List;
import java.util.Objects;

/** Draws private/public hands plus the recovered selection and drag visuals. */
final class TaizhouMahjongHandRenderer {
    private static final int NORMAL = 0xffffffff;
    private static final int SELECTED = 0xffffc9aa;
    private static final int DRAG = 0xff9e9e9e;
    private static final int LIMIT = 0xff77797d;
    private static final int ACTION_MASK = 0xfffff6ae;
    private static final int PRE_BAO = 0xff88ddfd;

    private final OriginalMahjongTilePainter tilePainter;
    private final Bitmap tingIcon;
    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    TaizhouMahjongHandRenderer(
            OriginalMahjongTilePainter tilePainter, Bitmap tingIcon) {
        this.tilePainter = Objects.requireNonNull(tilePainter, "tilePainter");
        this.tingIcon = Objects.requireNonNull(tingIcon, "tingIcon");
        if (tingIcon.getWidth() != 52 || tingIcon.getHeight() != 39) {
            throw new IllegalArgumentException("original ting icon size mismatch");
        }
    }

    void draw(
            Canvas canvas,
            GameplayTableState state,
            TaizhouMahjongVisibleRound round,
            TaizhouMahjongPlayInteraction interaction) {
        draw(canvas, state, round, interaction, false);
    }

    void draw(
            Canvas canvas,
            TaizhouMahjongVisibleRound round,
            TaizhouMahjongPlayInteraction interaction) {
        draw(canvas, null, round, interaction, true);
    }

    private void draw(
            Canvas canvas,
            GameplayTableState state,
            TaizhouMahjongVisibleRound round,
            TaizhouMahjongPlayInteraction interaction,
            boolean useVisibleMeldCount) {
        if (round == null) {
            return;
        }
        Integer actionMaskValue = actionMaskTileValue(state);
        for (int serverSeat = 1; serverSeat <= round.chairCount(); serverSeat++) {
            int renderedMeldCount =
                    renderedMeldCount(state, round, serverSeat, useVisibleMeldCount);
            List<TaizhouMahjongHandProjection.Tile> hand =
                    TaizhouMahjongHandProjection.forSeat(round, serverSeat, renderedMeldCount);
            boolean local = serverSeat == round.mySeat();
            for (TaizhouMahjongHandProjection.Tile tile : hand) {
                boolean masked =
                        local
                                && actionMaskValue != null
                                && actionMaskValue == tile.tileValue();
                drawTile(
                        canvas,
                        round,
                        tile,
                        renderedMeldCount,
                        local ? interaction : null,
                        masked);
            }
        }
        drawDragPreview(canvas, state, round, interaction, useVisibleMeldCount);
    }

    /**
     * 「能碰不碰 / 能杠不杠」的高亮牌值（{@code MahColor.ActionShader}）。
     *
     * <p>原版是**客户端**逻辑：{@code GameModule:analysePower}
     * （{@code BasicMahjong/Modules/GameLayer/Module.luac:295-335}）只在权限位含 {@code PUNG}(61)
     * 或 {@code MKONG}(59，明杠) 时把 {@code showActionColor} 置真——吃、胡、暗杠、补杠都不触发；
     * 随后 {@code lightActionMahs}（{@code :372-378}）取 {@code getLastPlayMah()}，
     * {@code UIMahTouchHandArea:lightActionMahs}（{@code View2D/UIMahTouchHandArea.luac:247-253}）
     * 对每张立牌做 {@code setMahActionMask(getMahValue() == mahValue)}。
     *
     * <p>该窗口没有出牌权（正在等你答碰/杠），所以不能挂在出牌权限上；这里用同一份
     * {@code ACTION_OFFERED} 的 powerMask 与 contextTile 本地计算，与原版同源。
     */
    private static Integer actionMaskTileValue(GameplayTableState state) {
        if (state == null || state.actionOffer().isEmpty()) {
            return null;
        }
        GameplayActionOffer offer = state.actionOffer().get();
        boolean showActionColor =
                (offer.powerMask() & (MahjongPower.PUNG | MahjongPower.MKONG)) != 0;
        return showActionColor && offer.contextTile() > 0 ? offer.contextTile() : null;
    }

    private void drawTile(
            Canvas canvas,
            TaizhouMahjongVisibleRound round,
            TaizhouMahjongHandProjection.Tile tile,
            int renderedMeldCount,
            TaizhouMahjongPlayInteraction interaction,
            boolean actionMasked) {
        TaizhouMahjongPlayGesture.Tile input =
                interaction == null ? null : inputAt(interaction.tiles(), originalIndex(tile));
        TaizhouMahjongPlayInteraction.VisualState visual =
                interaction == null ? null : interaction.visualState();
        boolean selected =
                input != null && Objects.equals(visual.selectedIndex(), input.index());
        TaizhouMahjongHandLayout.TilePosition position =
                position(round, tile, renderedMeldCount, selected);
        tilePainter.drawHandTile(
                canvas,
                position,
                tile.tileValue(),
                color(input, visual, selected, actionMasked),
                round.jokerTiles().contains(tile.tileValue()));
        if (input != null && input.ting()) {
            drawTingIcon(canvas, position, tile.tileValue());
        }
    }

    private void drawDragPreview(
            Canvas canvas,
            GameplayTableState state,
            TaizhouMahjongVisibleRound round,
            TaizhouMahjongPlayInteraction interaction,
            boolean useVisibleMeldCount) {
        if (interaction == null || !interaction.visualState().dragging()) {
            return;
        }
        TaizhouMahjongPlayInteraction.VisualState visual = interaction.visualState();
        int renderedMeldCount =
                renderedMeldCount(state, round, round.mySeat(), useVisibleMeldCount);
        for (TaizhouMahjongHandProjection.Tile tile :
                TaizhouMahjongHandProjection.forSeat(round, round.mySeat(), renderedMeldCount)) {
            if (originalIndex(tile) != visual.draggedIndex()) {
                continue;
            }
            TaizhouMahjongHandLayout.TilePosition position =
                    position(round, tile, renderedMeldCount, false);
            tilePainter.draw(
                    canvas,
                    OriginalMahjongTileDrawPlan.atAnchor(
                            position.pose,
                            tile.tileValue(),
                            visual.dragNodeX(),
                            visual.dragNodeY(),
                            position.effectiveScale,
                            position.anchorX,
                            position.anchorY,
                            round.jokerTiles().contains(tile.tileValue())));
            return;
        }
    }

    private void drawTingIcon(
            Canvas canvas,
            TaizhouMahjongHandLayout.TilePosition position,
            int tileValue) {
        OriginalMahjongTileGeometry.Composition composition =
                OriginalMahjongTileGeometry.defaultTile(position.pose, tileValue);
        float scale = position.effectiveScale;
        float contentLeft = position.designX - position.anchorX * composition.width * scale;
        float contentBottom = position.cocosY - position.anchorY * composition.height * scale;
        float centerX = contentLeft + composition.width * scale / 2.0f;
        float bottom = contentBottom + composition.height * scale;
        canvas.drawBitmap(
                tingIcon,
                null,
                new RectF(
                        centerX - tingIcon.getWidth() * scale / 2.0f,
                        TaizhouMahjongTableLayout.designY(
                                bottom + tingIcon.getHeight() * scale),
                        centerX + tingIcon.getWidth() * scale / 2.0f,
                        TaizhouMahjongTableLayout.designY(bottom)),
                bitmapPaint);
    }

    private static TaizhouMahjongHandLayout.TilePosition position(
            TaizhouMahjongVisibleRound round,
            TaizhouMahjongHandProjection.Tile tile,
            int renderedMeldCount,
            boolean selected) {
        TaizhouMahjongVisibleRound.SeatHand hand = round.handAt(tile.serverSeat());
        return tile.drawn()
                ? TaizhouMahjongHandLayout.drawnTile(
                        tile.localSeat(),
                        hand.concealedTiles().size(),
                        renderedMeldCount,
                        selected)
                : TaizhouMahjongHandLayout.handTile(
                        tile.localSeat(), tile.handIndex(), renderedMeldCount, selected);
    }

    static int renderedLocalMeldCount(
            GameplayTableState state,
            TaizhouMahjongVisibleRound round,
            boolean useVisibleMeldCount) {
        return round == null
                ? 0
                : renderedMeldCount(state, round, round.mySeat(), useVisibleMeldCount);
    }

    static int renderedMeldCount(
            GameplayTableState state,
            TaizhouMahjongVisibleRound round,
            int serverSeat,
            boolean useVisibleMeldCount) {
        int visibleCount = round.handAt(serverSeat).meldCount();
        int localSeat =
                TaizhouMahjongSeatMapper.toLocalSeat(
                        serverSeat, round.mySeat(), round.chairCount());
        if (localSeat != TaizhouMahjongTableLayout.SEAT_BOTTOM) {
            return 0;
        }
        if (useVisibleMeldCount || state == null) {
            return visibleCount;
        }
        int rendered = 0;
        for (GameplayMeld meld : state.melds()) {
            if (meld.seat() == serverSeat) {
                rendered++;
            }
        }
        return Math.min(rendered, visibleCount);
    }

    private static int color(
            TaizhouMahjongPlayGesture.Tile tile,
            TaizhouMahjongPlayInteraction.VisualState visual,
            boolean selected,
            boolean actionMasked) {
        if (tile == null) {
            // 碰/明杠待答窗口没有出牌权，手牌不产生手势 Tile，但原版仍要上 ActionShader。
            return actionMasked ? ACTION_MASK : NORMAL;
        }
        if (selected && !tile.preBao()) {
            return SELECTED;
        }
        if (visual.dragging()
                && Objects.equals(visual.draggedIndex(), tile.index())
                && visual.dragTinted()) {
            return DRAG;
        }
        if (actionMasked || tile.actionMask()) {
            return ACTION_MASK;
        }
        if (!tile.touchEnabled()) {
            return LIMIT;
        }
        return tile.preBao() ? PRE_BAO : NORMAL;
    }

    private static TaizhouMahjongPlayGesture.Tile inputAt(
            List<TaizhouMahjongPlayGesture.Tile> tiles, int originalIndex) {
        for (TaizhouMahjongPlayGesture.Tile tile : tiles) {
            if (tile.index() == originalIndex) {
                return tile;
            }
        }
        return null;
    }

    private static int originalIndex(TaizhouMahjongHandProjection.Tile tile) {
        return tile.drawn() ? 0 : tile.handIndex() + 1;
    }
}
