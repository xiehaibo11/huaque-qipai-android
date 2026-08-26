package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import com.nanbeiyule.game.mahjong.MahjongTile;
import com.nanbeiyule.game.mahjong.MahjongTileSprite;
import com.nanbeiyule.game.mahjong.OriginalMahjongTileDrawPlan;
import com.nanbeiyule.game.mahjong.OriginalMahjongTilePainter;
import com.nanbeiyule.game.mahjong.TaizhouMahjongMeldLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongSettlement;
import com.nanbeiyule.game.mahjong.TaizhouSettleLayout;
import com.nanbeiyule.game.mahjong.TaizhouSettleState;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Draws MahjongNew/GameLayer/CSB/Settle/Settle.csb from server result state. */
final class TaizhouSettleRenderer {
    /** {@code _KW_LIST_VIEW} 的顶边；行按 {@code 196×0.9376} 的步长向下排。 */
    private static final float ROW_FIRST_TOP = TaizhouSettleLayout.LIST_TOP;
    private static final float ROW_STEP = TaizhouSettleLayout.LIST_ROW_STEP;

    /**
     * {@code KW_COVER_LAYER} 的遮罩色。CSB 只记成无贴图 Panel，解析工具读不到底色，
     * 按实机截图取近似值，属推断而非已确认的原版数值。
     */
    private static final int COVER_SCRIM = Color.argb(140, 0, 0, 0);

    private static final int TEXT_LIGHT = Color.rgb(234, 252, 242);
    private static final int TEXT_GOLD = Color.rgb(255, 232, 106);
    private static final int TEXT_POSITIVE = Color.rgb(255, 193, 61);
    private static final int TEXT_NEGATIVE = Color.rgb(136, 234, 244);

    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Paint scrimPaint = new Paint();
    private final Bitmap background;
    private final Bitmap titleBar;
    private final Bitmap titleFan;
    private final Bitmap titleGang;
    private final Bitmap titleHeji;
    private final Bitmap checkTable;
    private final Bitmap checkBill;
    private final Bitmap shuffle;
    private final Bitmap next;
    private final Bitmap rowSplitLine;
    private final Bitmap rowCaishenBackground;
    private final Bitmap roomCard;
    private final Bitmap headFrame;
    private TaizhouRoomToolsState.Tool shuffleCost;
    private final Bitmap[] winds = new Bitmap[4];
    private final Bitmap[] endIcons = new Bitmap[10];
    private final Map<TaizhouSettleState.Result, Bitmap> resultBanners =
            new EnumMap<>(TaizhouSettleState.Result.class);
    private final OriginalMahjongTilePainter tilePainter;

    TaizhouSettleRenderer(
            Context context,
            OriginalMahjongTilePainter tilePainter,
            Bitmap gameLayerAtlas,
            Bitmap totalResultAtlas) {
        this.tilePainter = tilePainter;
        headFrame = TaizhouMahjongGameLayerBitmap.extract(gameLayerAtlas, "mah_head_bg.png");
        background = bitmap(context, R.drawable.taizhou_settle_background);
        titleBar = bitmap(context, R.drawable.taizhou_settle_title_bar);
        titleFan = bitmap(context, R.drawable.taizhou_settle_title_fan);
        titleGang = bitmap(context, R.drawable.taizhou_settle_title_gang);
        titleHeji = bitmap(context, R.drawable.taizhou_settle_title_heji);
        checkTable = bitmap(context, R.drawable.taizhou_settle_check_table);
        checkBill = TaizhouTotalResultBitmap.extract(
                totalResultAtlas, "result_checkbill_btn.png");
        shuffle = bitmap(context, R.drawable.taizhou_settle_shuffle);
        next = bitmap(context, R.drawable.taizhou_settle_next);
        rowSplitLine = bitmap(context, R.drawable.taizhou_settle_split_line);
        rowCaishenBackground = bitmap(context, R.drawable.taizhou_settle_row_caishen_bg);
        roomCard = bitmap(context, R.drawable.taizhou_settle_room_card);
        winds[0] = bitmap(context, R.drawable.taizhou_settle_wind_0);
        winds[1] = bitmap(context, R.drawable.taizhou_settle_wind_1);
        winds[2] = bitmap(context, R.drawable.taizhou_settle_wind_2);
        winds[3] = bitmap(context, R.drawable.taizhou_settle_wind_3);
        Bitmap tableInfoAtlas = bitmap(context, R.drawable.taizhou_mahjong_table_info);
        endIcons[TaizhouMahjongSettlement.STATE_HU] =
                TaizhouTableInfoRenderer.extract(tableInfoAtlas, "tz_settle_icon_1.png");
        endIcons[TaizhouMahjongSettlement.STATE_DISCARD] =
                TaizhouTableInfoRenderer.extract(tableInfoAtlas, "tz_settle_icon_2.png");
        endIcons[TaizhouMahjongSettlement.STATE_ROB_KONG] =
                TaizhouTableInfoRenderer.extract(tableInfoAtlas, "tz_settle_icon_3.png");
        endIcons[TaizhouMahjongSettlement.STATE_KONG_BLOOM] =
                TaizhouTableInfoRenderer.extract(tableInfoAtlas, "tz_settle_icon_4.png");
        endIcons[TaizhouMahjongSettlement.STATE_CHENG_BAO] =
                TaizhouTableInfoRenderer.extract(tableInfoAtlas, "tz_settle_icon_5.png");
        endIcons[TaizhouMahjongSettlement.STATE_DRAWN] =
                TaizhouTableInfoRenderer.extract(tableInfoAtlas, "tz_settle_icon_9.png");
        resultBanners.put(
                TaizhouSettleState.Result.ZIMO,
                bitmap(context, R.drawable.taizhou_settle_result_zimo));
        resultBanners.put(
                TaizhouSettleState.Result.DIANPAO,
                bitmap(context, R.drawable.taizhou_settle_result_dianpao));
        resultBanners.put(
                TaizhouSettleState.Result.LIUJU,
                bitmap(context, R.drawable.taizhou_settle_result_liuju));
        resultBanners.put(
                TaizhouSettleState.Result.DRAWN,
                bitmap(context, R.drawable.taizhou_settle_result_liuju));
        resultBanners.put(
                TaizhouSettleState.Result.QIANGGANG,
                bitmap(context, R.drawable.taizhou_settle_result_qianggang));
        resultBanners.put(
                TaizhouSettleState.Result.DISMISS,
                bitmap(context, R.drawable.taizhou_settle_result_dismiss));
        textPaint.setTypeface(
                Typeface.createFromAsset(context.getAssets(), "fonts/fangzhengcuyuan.ttf"));
        scrimPaint.setColor(COVER_SCRIM);
    }

    void draw(
            Canvas canvas,
            TaizhouSettleState state,
            List<Integer> jokerTiles,
            boolean finalRound) {
        if (state == null) {
            return;
        }
        // KW_COVER_LAYER：先铺满整屏，否则牌桌层的房间号面板、玩家头像与座位分数会从内容区
        // 上下两条露白里透出来，压在表头和结果横幅上。
        canvas.drawRect(
                0.0f,
                0.0f,
                TaizhouSettleLayout.COVER_WIDTH,
                TaizhouSettleLayout.COVER_HEIGHT,
                scrimPaint);
        drawBitmap(canvas, background, 0.0f, TaizhouSettleLayout.CONTENT_TOP, 1920.0f, 867.0f);
        drawNode(canvas, resultBanners.get(state.result()), TaizhouSettleLayout.RESULT_BANNER);
        drawNode(canvas, titleBar, TaizhouSettleLayout.TITLE_BAR);
        drawNode(canvas, titleFan, TaizhouSettleLayout.TITLE_FAN);
        // _KW_IMG_GANG 在 CSB 里 visible=False，台州把杠分并进明细行，这里不画。
        drawNode(canvas, titleHeji, TaizhouSettleLayout.TITLE_HEJI);
        drawHeader(canvas, state);
        for (int index = 0; index < state.seats().size(); index++) {
            drawSeat(
                    canvas,
                    state.seats().get(index),
                    ROW_FIRST_TOP + index * ROW_STEP,
                    jokerTiles == null ? List.of() : jokerTiles);
        }
        if (finalRound) {
            // BasicMahjong/WinLost/View.luac:updateBtnInEnd：末局仅显示居中的查看账单。
            drawNode(canvas, checkBill, TaizhouSettleLayout.BUTTON_CHECK_BILL);
        } else {
            drawNode(canvas, checkTable, TaizhouSettleLayout.BUTTON_CHECK_TABLE);
            drawNode(canvas, shuffle, TaizhouSettleLayout.BUTTON_SHUFFLE);
            drawNode(canvas, next, TaizhouSettleLayout.BUTTON_NEXT);
            drawShuffleCost(canvas);
        }
    }

    /**
     * 结算页洗牌按钮的消耗行（缺陷 #2 修复）：30109 是房卡 DIRECT 分支
     * （{@code WinLost/View.luac:222-250}），画「（消耗」+ fk.png +「xN）」（:498），N 为
     * 服务端下发的洗牌房卡数；此前误植的「500」来自 30400 金币场售价分支，已删除。
     * 非房卡定价或定价未到达时不画（免费分支未接入，已标注）。
     */
    void setShuffleCost(TaizhouRoomToolsState.Tool tool) {
        shuffleCost = tool;
    }

    private void drawShuffleCost(Canvas canvas) {
        TaizhouRoomToolsState.Tool cost = shuffleCost;
        if (cost == null
                || cost.priceAmount() <= 0
                || !"ROOM_CARD".equals(cost.priceCurrency())) {
            return;
        }
        // 推断: 两个文本节点高 41，字号取 36；基线按节点中线 +13。
        float baseline = TaizhouSettleLayout.SHUFFLE_COST_CENTER_Y + 13.0f;
        drawText(canvas, "（消耗", TaizhouSettleLayout.SHUFFLE_COST_NAME_RIGHT,
                baseline, 36.0f, Color.WHITE, Paint.Align.RIGHT);
        drawNode(canvas, roomCard, TaizhouSettleLayout.SHUFFLE_COST_ICON);
        drawText(canvas, "x" + cost.priceLabel() + "）",
                TaizhouSettleLayout.SHUFFLE_COST_COUNT_LEFT,
                baseline, 36.0f, Color.WHITE, Paint.Align.LEFT);
    }

    /**
     * {@code _KW_LABEL_ROOM_NUM} / {@code _KW_LABEL_JUSHU} / {@code _KW_LABEL_TIME} 是标题栏
     * 同一行上 anchor (0,0.5) 的三个左对齐文本，不是左上角的两行堆叠——排成两行会直接压在
     * 牌桌层的房间号面板上。
     */
    private void drawHeader(Canvas canvas, TaizhouSettleState state) {
        float baseline = TaizhouSettleLayout.HEADER_LABEL_CENTER_Y + 14.0f;
        if (!state.roomNumber().isBlank()) {
            drawText(canvas, "房间号:" + state.roomNumber(),
                    TaizhouSettleLayout.HEADER_ROOM_NUM_LEFT, baseline, 34.0f,
                    TEXT_LIGHT, Paint.Align.LEFT);
        }
        if (!state.roundLabel().isBlank()) {
            drawText(canvas, state.roundLabel(), TaizhouSettleLayout.HEADER_JUSHU_LEFT,
                    baseline, 34.0f, TEXT_LIGHT, Paint.Align.LEFT);
        }
        drawText(canvas, state.time(), TaizhouSettleLayout.HEADER_TIME_LEFT, baseline, 34.0f,
                TEXT_GOLD, Paint.Align.LEFT);
        if (!state.gameRule().isBlank()) {
            drawText(canvas, state.gameRule(), TaizhouSettleLayout.GAME_RULE_LEFT,
                    TaizhouSettleLayout.GAME_RULE_CENTER_Y + 10.0f, 26.0f, TEXT_LIGHT,
                    Paint.Align.LEFT);
        }
    }

    private void drawSeat(
            Canvas canvas,
            TaizhouSettleState.Seat seat,
            float top,
            List<Integer> jokerTiles) {
        canvas.save();
        canvas.translate(0.0f, top);
        canvas.scale(1.0f, TaizhouSettleLayout.LIST_SCALE_Y);
        drawSeatRow(canvas, seat, jokerTiles);
        canvas.restore();
    }

    /** 行内绘制，坐标原点在行左上角，纵向已按 {@code _KW_LIST_VIEW} 的 0.9376 缩放。 */
    private void drawSeatRow(
            Canvas canvas, TaizhouSettleState.Seat seat, List<Integer> jokerTiles) {
        float top = 0.0f;
        // 原版 ItemNode.luac:updateBG：行底图 settle_caishen_bg 只在请财神道具生效时显示。
        if (seat.caishenPropActive()) {
            drawNodeAt(canvas, rowCaishenBackground, TaizhouSettleLayout.ROW_CAISHEN_BG, top);
        }
        drawNodeAt(canvas, rowSplitLine, TaizhouSettleLayout.ROW_SPLIT_LINE, top);
        drawNodeAt(canvas, headFrame, TaizhouSettleLayout.ROW_HEAD, top);
        drawNodeAt(canvas, winds[Math.floorMod(seat.wind(), 4)], TaizhouSettleLayout.ROW_WIND, top);
        drawText(canvas, seat.displayName(), TaizhouSettleLayout.ROW_NICKNAME.centerX(),
                top + TaizhouSettleLayout.ROW_NICKNAME.centerY() + 8.0f,
                24.0f, TEXT_LIGHT, Paint.Align.CENTER);
        drawText(canvas, seat.publicPlayerId(), TaizhouSettleLayout.ROW_PLAYER_ID.centerX(),
                top + TaizhouSettleLayout.ROW_PLAYER_ID.centerY() + 8.0f,
                22.0f, TEXT_GOLD, Paint.Align.CENTER);
        drawText(canvas, seat.detailText(), TaizhouSettleLayout.ROW_DETAIL_LEFT,
                top + TaizhouSettleLayout.ROW_DETAIL_CENTER_Y + 8.0f,
                28.0f, TEXT_LIGHT, Paint.Align.LEFT);
        drawHand(canvas, seat, top, jokerTiles);
        Bitmap endIcon = endIcon(seat);
        if (endIcon != null) {
            drawNodeAt(canvas, endIcon, TaizhouSettleLayout.ROW_END_ICON, top);
        }
        drawText(canvas, Integer.toString(seat.fan()), TaizhouSettleLayout.ROW_FAN.centerX(),
                top + TaizhouSettleLayout.ROW_FAN.centerY() + 14.0f,
                42.0f, Color.WHITE, Paint.Align.CENTER);
        // _KW_FNT_GANGFEN 与 _KW_IMG_GANG 一样 CSB 默认隐藏，不再单列杠分。
        drawScore(canvas, seat, top);
    }

    private Bitmap endIcon(TaizhouSettleState.Seat seat) {
        if (seat.endIconFrameName().isEmpty()) {
            return null;
        }
        int state = seat.playerState();
        return state >= 0 && state < endIcons.length ? endIcons[state] : null;
    }

    /**
     * 原版右侧只有一格数字：负分走 {@code _KW_FNT_HEJI}（lose 字体），正分走 {@code KW_FNT_WIN}
     * （win 字体、anchor (1,0.5)）。两格几乎重叠，同时绘制会让两个数字叠在一起。
     */
    private void drawScore(Canvas canvas, TaizhouSettleState.Seat seat, float top) {
        boolean positive = seat.delta() >= 0;
        if (positive) {
            drawText(canvas, seat.deltaText(), TaizhouSettleLayout.ROW_WIN.right(),
                    top + TaizhouSettleLayout.ROW_WIN.centerY() + 21.0f,
                    70.0f, TEXT_POSITIVE, Paint.Align.RIGHT);
        } else {
            drawText(canvas, seat.deltaText(), TaizhouSettleLayout.ROW_HEJI.centerX(),
                    top + TaizhouSettleLayout.ROW_HEJI.centerY() + 21.0f,
                    70.0f, TEXT_NEGATIVE, Paint.Align.CENTER);
        }
    }

    /**
     * {@code _KW_PANEL_HAND_CARD} 挂在行内 {@code (180,35)} 且整体 0.5 缩放。原版
     * {@code WinLost/ItemMahsArea.luac:showResultMahs} 的行内容顺序：副露（combData）
     * 在最左，手牌（{@code UIMahHandArea.luac:_sortHandMahs}，财神在前再按牌值升序）
     * 跟在末组副露 +20 后，胡牌张（dfMahID）再与手牌隔 {@code DanFangDistance=15} 单放。
     * 手牌牌距公式证据：{@code UIMahHandArea.luac:192}
     * （{@code topEdgeWidth × AddDirection}，HandMahScale=1）
     * × MahTopEdgeWidth 135 × 0.5 = 67.5，见 {@link TaizhouSettleLayout#ROW_HAND_SCALE}。
     */
    private void drawHand(
            Canvas canvas,
            TaizhouSettleState.Seat seat,
            float rowTop,
            List<Integer> jokerTiles) {
        float x = TaizhouSettleLayout.ROW_HAND_LEFT;
        // 画布已平移到行顶，牌绘制器按 1080 的 Cocos 基线换算，故把行内底边折算回该基线。
        float cocosY = TaizhouSettleLayout.DESIGN_HEIGHT
                - (rowTop + TaizhouSettleLayout.ROW_HEIGHT - TaizhouSettleLayout.ROW_HAND_BOTTOM_COCOS);
        if (!seat.melds().isEmpty()) {
            TaizhouMahjongMeldLayout.SettleMeldRow meldRow =
                    TaizhouMahjongMeldLayout.settleRowMelds(seat.melds());
            for (TaizhouMahjongMeldLayout.TilePlacement placement : meldRow.placements()) {
                drawMeldTile(canvas, placement, x, cocosY, jokerTiles);
            }
            x += meldRow.handStartX() * TaizhouSettleLayout.ROW_HAND_SCALE;
        }
        float step =
                MahjongTileSprite.topEdgeWidth(MahjongTileSprite.STAND_FACE_FORWARD)
                        * TaizhouSettleLayout.ROW_HAND_SCALE;
        for (int tile : TaizhouSettleState.sortResultHand(seat.handTiles(), jokerTiles)) {
            tilePainter.draw(
                    canvas,
                    OriginalMahjongTileDrawPlan.atAnchor(
                            MahjongTileSprite.STAND_FACE_FORWARD,
                            tile,
                            x,
                            cocosY,
                            TaizhouSettleLayout.ROW_HAND_SCALE,
                            0.0f,
                            0.0f,
                            jokerTiles.contains(tile)));
            x += step;
        }
        if (MahjongTile.isValid(seat.huTile())) {
            // 单放间隙：DanFangDistance 15 × 面板 0.5 缩放 = 7.5 设计单位。
            x += 15.0f * TaizhouSettleLayout.ROW_HAND_SCALE;
            tilePainter.draw(
                    canvas,
                    OriginalMahjongTileDrawPlan.atAnchor(
                            MahjongTileSprite.STAND_FACE_FORWARD,
                            seat.huTile(),
                            x,
                            cocosY,
                            TaizhouSettleLayout.ROW_HAND_SCALE,
                            0.0f,
                            0.0f,
                            jokerTiles.contains(seat.huTile())));
        }
    }

    /** 副露牌位从面板单位换算到设计单位：位置与缩放同乘 {@code ROW_HAND_SCALE}。 */
    private void drawMeldTile(
            Canvas canvas,
            TaizhouMahjongMeldLayout.TilePlacement placement,
            float areaLeft,
            float areaBottomCocosY,
            List<Integer> jokerTiles) {
        if (placement.tileValue != MahjongTile.BACK
                && !MahjongTile.hasTaizhouFace(placement.tileValue)) {
            return;
        }
        tilePainter.draw(
                canvas,
                OriginalMahjongTileDrawPlan.atAnchor(
                        placement.pose,
                        placement.tileValue,
                        areaLeft + placement.designX * TaizhouSettleLayout.ROW_HAND_SCALE,
                        areaBottomCocosY + placement.cocosY * TaizhouSettleLayout.ROW_HAND_SCALE,
                        placement.scale * TaizhouSettleLayout.ROW_HAND_SCALE,
                        0.5f,
                        0.5f,
                        jokerTiles.contains(placement.tileValue)));
    }

    private void drawNode(Canvas canvas, Bitmap bitmap, TaizhouSettleLayout.Node node) {
        drawBitmap(canvas, bitmap, node.left(), node.top(), node.width(), node.height());
    }

    private void drawNodeAt(Canvas canvas, Bitmap bitmap, TaizhouSettleLayout.Node node, float top) {
        drawBitmap(canvas, bitmap, node.left(), top + node.top(), node.width(), node.height());
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, float left, float top, float width, float height) {
        if (bitmap != null && !bitmap.isRecycled()) {
            canvas.drawBitmap(bitmap, null, new RectF(left, top, left + width, top + height), bitmapPaint);
        }
    }

    private void drawText(
            Canvas canvas,
            String text,
            float x,
            float baseline,
            float size,
            int color,
            Paint.Align align) {
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        textPaint.setTextAlign(align);
        textPaint.setShadowLayer(3.0f, 0.0f, 2.0f, Color.argb(180, 25, 20, 15));
        canvas.drawText(text == null ? "" : text, x, baseline, textPaint);
        textPaint.clearShadowLayer();
    }

    private static Bitmap bitmap(Context context, int resourceId) {
        return BitmapFactory.decodeResource(context.getResources(), resourceId);
    }
}
