package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import com.nanbeiyule.game.gameplay.GameplayTingInfo;
import com.nanbeiyule.game.mahjong.MahjongTileSprite;
import com.nanbeiyule.game.mahjong.OriginalMahjongTileDrawPlan;
import com.nanbeiyule.game.mahjong.OriginalMahjongTilePainter;
import com.nanbeiyule.game.mahjong.TaizhouMahjongExtraAtlas;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Draws the CanHuMahs 听牌可胡提示层: the stretched {@code can_hu_bg.png}
 * background, the {@code division_line.png} column, the {@code hu.png} logo and
 * the hu-target tiles as 0.7-scale {@code STAND_FACE_FORWARD} UIMah, exactly as
 * {@code CanHuMahsUI.luac:initUI} composes them. The selected-hand branch keeps
 * CSB {@code _bg} at (960,230), while the right-side 听 button branch moves it
 * to {@code _model} (1810,622) with anchor (1,0.5), matching the recovered Lua.
 */
final class TaizhouCanHuRenderer {
    /** Surrogate value whose back/face-ground layers stand in for the 255 any-tile. */
    private static final int ANY_TILE_PLACEHOLDER = 0x11;

    /** {@code can_hu_mah_info.fnt} 的 {@code face="FZCuYuan-M03S"}。 */
    private static final String ORIGINAL_FONT_ASSET = "fonts/fangzhengcuyuan.ttf";

    private final OriginalMahjongTilePainter tilePainter;
    private final Bitmap background;
    private final Bitmap divisionLine;
    private final Bitmap huLogo;
    private final Bitmap anyTileIcon;
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    TaizhouCanHuRenderer(Context context, OriginalMahjongTilePainter tilePainter) {
        this.tilePainter = tilePainter;
        Bitmap canHuAtlas =
                BitmapFactory.decodeResource(
                        context.getResources(), R.drawable.taizhou_mahjong_can_hu_tip_atlas);
        background = extract(canHuAtlas, TaizhouCanHuLayout.BG_FRAME);
        divisionLine = extract(canHuAtlas, TaizhouCanHuLayout.LINE_FRAME);
        huLogo = extract(canHuAtlas, TaizhouCanHuLayout.HU_LOGO_FRAME);
        anyTileIcon =
                TaizhouMahjongIconBitmap.extract(
                        BitmapFactory.decodeResource(
                                context.getResources(), R.drawable.taizhou_mahjong_icon),
                        TaizhouCanHuLayout.ANY_TILE_FRAME);
        textPaint.setTypeface(
                Typeface.createFromAsset(context.getAssets(), ORIGINAL_FONT_ASSET));
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    /**
     * @param jokerTiles 本局财神物理牌值，来自 {@code TaizhouMahjongVisibleRound.jokerTiles()}。
     *     与手牌、牌河、副露和结算共用同一个判定，可胡目标里的财神因此拿到同一层「财」角标
     *     （规格「覆盖范围」明列胡牌提示）。替牌白板不在此列，不会误标。
     */
    void draw(Canvas canvas, TaizhouCanHuState state, List<Integer> jokerTiles) {
        if (state == null || !state.visible()) {
            return;
        }
        List<Integer> jokers = jokerTiles == null ? List.of() : jokerTiles;
        float scale = state.nodeScale();
        float width = state.backgroundWidth() * scale;
        float height = state.backgroundHeight() * scale;
        float bgLeft = anchorX(state) - width * anchorPointX(state);
        float bgRight = bgLeft + width;
        float bgBottomCocos = anchorCocosY(state) - height * anchorPointY(state);
        float bgTopCocos = bgBottomCocos + height;
        float bgTop = TaizhouMahjongTableLayout.designY(bgTopCocos);
        float bgBottom = TaizhouMahjongTableLayout.designY(bgBottomCocos);
        TaizhouNineSlice.draw(
                canvas,
                background,
                bitmapPaint,
                new RectF(bgLeft, bgTop, bgRight, bgBottom),
                TaizhouCanHuLayout.BG_CAP_X,
                TaizhouCanHuLayout.BG_CAP_Y,
                TaizhouCanHuLayout.BG_CAP_WIDTH,
                TaizhouCanHuLayout.BG_CAP_HEIGHT,
                scale);

        // 分割线：bg 本地 x=190 起 2 宽、通高（Lua setPosition/setContentSize）。
        float lineLeft = bgLeft + TaizhouCanHuLayout.LINE_LOCAL_X * scale;
        TaizhouNineSlice.draw(
                canvas,
                divisionLine,
                bitmapPaint,
                new RectF(lineLeft, bgTop, lineLeft + TaizhouCanHuLayout.LINE_WIDTH * scale, bgBottom),
                TaizhouCanHuLayout.LINE_CAP_X,
                TaizhouCanHuLayout.LINE_CAP_Y,
                TaizhouCanHuLayout.LINE_CAP_WIDTH,
                TaizhouCanHuLayout.LINE_CAP_HEIGHT,
                scale);

        // 胡字 logo：anchor (0,0.5) 于 bg 本地 (-35, h/2)，0.7 缩放随节点缩放。
        float logoWidth = TaizhouCanHuLayout.HU_LOGO_WIDTH * TaizhouCanHuLayout.HU_LOGO_SCALE * scale;
        float logoHeight = TaizhouCanHuLayout.HU_LOGO_HEIGHT * TaizhouCanHuLayout.HU_LOGO_SCALE * scale;
        float logoLeft = bgLeft + TaizhouCanHuLayout.HU_LOGO_LOCAL_X * scale;
        float logoCenterY =
                TaizhouMahjongTableLayout.designY(
                        bgBottomCocos + state.backgroundHeight() / 2.0f * scale);
        canvas.drawBitmap(
                huLogo,
                null,
                new RectF(
                        logoLeft,
                        logoCenterY - logoHeight / 2.0f,
                        logoLeft + logoWidth,
                        logoCenterY + logoHeight / 2.0f),
                bitmapPaint);

        float tileScale = TaizhouCanHuLayout.TILE_SCALE * scale;
        for (int index = 0; index < state.huTargets().size(); index++) {
            float worldX = bgLeft + state.tileLocalX(index) * scale;
            float worldCocosY = bgBottomCocos + state.tileLocalY(index) * scale;
            int target = state.huTargets().get(index);
            if (target == GameplayTingInfo.ANY_TILE) {
                drawAnyTile(canvas, worldX, worldCocosY, tileScale);
            } else {
                tilePainter.draw(
                        canvas,
                        OriginalMahjongTileDrawPlan.atAnchor(
                                MahjongTileSprite.STAND_FACE_FORWARD,
                                target,
                                worldX,
                                worldCocosY,
                                tileScale,
                                0.5f,
                                0.5f,
                                jokers.contains(target)));
            }
            drawInfo(canvas, state, index, worldX, worldCocosY, scale);
        }
    }

    /**
     * 每格的「N台」「N胡」「N张」：原版 {@code setHuInfo} 把数字放在牌位右侧 {@code x+50}，
     * 行基线取 {@code huInfoPositionY[段数][序号]}，单位紧接数字右缘。数字用
     * {@code can_hu_mah_info} 的红，单位用 {@code can_hu_mah_info_2} 的棕，两套字模
     * 都是 FZCuYuan-M03S 46 号。
     */
    private void drawInfo(
            Canvas canvas,
            TaizhouCanHuState state,
            int index,
            float worldX,
            float worldCocosY,
            float scale) {
        if (index >= state.infoRows().size()) {
            return;
        }
        List<TaizhouCanHuState.InfoSegment> segments = state.infoRows().get(index);
        if (segments.isEmpty()) {
            return;
        }
        float[] rowOffsets = TaizhouCanHuLayout.INFO_ROW_OFFSETS[segments.size() - 1];
        float textLeft = worldX + TaizhouCanHuLayout.INFO_TEXT_LOCAL_X * scale;
        textPaint.setTextSize(TaizhouCanHuLayout.INFO_FONT_SIZE * scale);
        for (int segment = 0; segment < segments.size(); segment++) {
            float baselineCocosY =
                    worldCocosY
                            + (rowOffsets[segment]
                                            + TaizhouCanHuLayout.INFO_LINE_HEIGHT
                                            - TaizhouCanHuLayout.INFO_BASE_LINE)
                                    * scale;
            float baseline = TaizhouMahjongTableLayout.designY(baselineCocosY);
            String number = Integer.toString(segments.get(segment).number());
            textPaint.setColor(TaizhouCanHuLayout.INFO_NUMBER_COLOR);
            canvas.drawText(number, textLeft, baseline, textPaint);
            textPaint.setColor(TaizhouCanHuLayout.INFO_UNIT_COLOR);
            canvas.drawText(
                    segments.get(segment).unit(),
                    textLeft + textPaint.measureText(number),
                    baseline,
                    textPaint);
        }
    }

    private static float anchorX(TaizhouCanHuState state) {
        return state.source() == TaizhouCanHuState.Source.TING_BUTTON
                ? TaizhouCanHuLayout.TING_BUTTON_BG_ANCHOR_X
                : TaizhouCanHuLayout.BG_ANCHOR_X;
    }

    private static float anchorCocosY(TaizhouCanHuState state) {
        return state.source() == TaizhouCanHuState.Source.TING_BUTTON
                ? TaizhouCanHuLayout.TING_BUTTON_BG_ANCHOR_COCOS_Y
                : TaizhouCanHuLayout.BG_ANCHOR_COCOS_Y;
    }

    private static float anchorPointX(TaizhouCanHuState state) {
        return state.source() == TaizhouCanHuState.Source.TING_BUTTON
                ? TaizhouCanHuLayout.TING_BUTTON_BG_ANCHOR_POINT_X
                : TaizhouCanHuLayout.SELECTED_BG_ANCHOR_POINT_X;
    }

    private static float anchorPointY(TaizhouCanHuState state) {
        return state.source() == TaizhouCanHuState.Source.TING_BUTTON
                ? TaizhouCanHuLayout.TING_BUTTON_BG_ANCHOR_POINT_Y
                : TaizhouCanHuLayout.SELECTED_BG_ANCHOR_POINT_Y;
    }

    /**
     * 胡任意牌：原版把 face ImageView 换成 {@code mahlayer_mah_any.png}，牌身与
     * 牌面底不变；此处用合法牌值生成牌身后剔除 face 层，再把任意牌帧居中画上
     * （推断：原版 face 锚点在牌身内的精确偏移未被归档证据锁定）。
     */
    private void drawAnyTile(Canvas canvas, float worldX, float worldCocosY, float tileScale) {
        List<OriginalMahjongTileDrawPlan.Command> body = new ArrayList<>();
        for (OriginalMahjongTileDrawPlan.Command command :
                OriginalMahjongTileDrawPlan.atAnchor(
                        MahjongTileSprite.STAND_FACE_FORWARD,
                        ANY_TILE_PLACEHOLDER,
                        worldX,
                        worldCocosY,
                        tileScale,
                        0.5f,
                        0.5f)) {
            if (command.zOrder != MahjongTileSprite.Z_FACE) {
                body.add(command);
            }
        }
        tilePainter.draw(canvas, body);
        float iconWidth = TaizhouCanHuLayout.ANY_TILE_WIDTH * tileScale;
        float iconHeight = TaizhouCanHuLayout.ANY_TILE_HEIGHT * tileScale;
        float centerY = TaizhouMahjongTableLayout.designY(worldCocosY);
        canvas.drawBitmap(
                anyTileIcon,
                null,
                new RectF(
                        worldX - iconWidth / 2.0f,
                        centerY - iconHeight / 2.0f,
                        worldX + iconWidth / 2.0f,
                        centerY + iconHeight / 2.0f),
                bitmapPaint);
    }

    /** Extracts one frame from the recovered {@code tip_can_hu_mah} atlas. */
    private static Bitmap extract(Bitmap atlas, String frameName) {
        if (atlas == null
                || atlas.isRecycled()
                || atlas.getWidth() != TaizhouMahjongExtraAtlas.TAIZHOU_MAHJONG_CAN_HU_TIP_WIDTH
                || atlas.getHeight() != TaizhouMahjongExtraAtlas.TAIZHOU_MAHJONG_CAN_HU_TIP_HEIGHT) {
            throw new IllegalArgumentException("Invalid original can-hu tip atlas");
        }
        int index =
                TaizhouMahjongExtraAtlas.indexOf(
                        TaizhouMahjongExtraAtlas.TAIZHOU_MAHJONG_CAN_HU_TIP_NAMES, frameName);
        if (index < 0) {
            throw new IllegalArgumentException("Missing original can-hu tip frame " + frameName);
        }
        int[] frame = TaizhouMahjongExtraAtlas.TAIZHOU_MAHJONG_CAN_HU_TIP_FRAMES[index];
        if (frame[4] != 0) {
            throw new IllegalStateException("unexpected rotated can-hu tip frame " + frameName);
        }
        return Bitmap.createBitmap(atlas, frame[0], frame[1], frame[2], frame[3]);
    }
}
