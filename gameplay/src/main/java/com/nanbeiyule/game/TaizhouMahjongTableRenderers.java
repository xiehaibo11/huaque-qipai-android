package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.nanbeiyule.game.mahjong.MahjongTileSprite;
import com.nanbeiyule.game.mahjong.OriginalMahjongTilePainter;
import com.nanbeiyule.game.mahjong.TaizhouMahjongWaitingLayout;

/**
 * 台州麻将牌桌的位图与渲染器装配。
 *
 * <p>{@code TaizhouMahjongTableView} 只负责绘制、命中测试和生命周期；把图集解码、帧抽取和
 * 十来个渲染器的构造集中到这里，牌桌 View 才不用同时承担资源装配职责（AGENTS.md 第 25、28 章）。
 * 所有字段一次性构造后只读，图集位图只解码一次并在多个渲染器之间共享。
 */
final class TaizhouMahjongTableRenderers {
    /** {@code RoomInfo/CenterView.lua:TableBgRes}：桌布按 TABLE_STYLE 取图。 */
    private static final int[] TABLE_BACKGROUNDS = {
        R.drawable.taizhou_mahjong_table_bg_1,
        R.drawable.taizhou_mahjong_table_bg_2,
        R.drawable.taizhou_mahjong_table_bg_3,
        R.drawable.taizhou_mahjong_table_bg_4,
        R.drawable.taizhou_mahjong_table_bg_5,
        R.drawable.taizhou_mahjong_table_bg_6,
    };

    private final Context context;
    private final Bitmap[] tableBackgrounds = new Bitmap[TABLE_BACKGROUNDS.length];

    final Bitmap background;
    final Bitmap ready;
    final Bitmap invite;
    final Bitmap start;
    final Bitmap copy;
    final TaizhouMahjongRoomInfoRenderer roomInfo;
    final TaizhouMahjongPlayerRenderer player;
    final TaizhouMahjongWaitingChromeRenderer waitingChrome;
    final TaizhouIconAnimationSelection iconAnimation;
    final TaizhouWaitingIconEffects iconEffects;
    final SxvipRecordBadgeStore recordBadgeStore;
    final TaizhouMahjongDiscardRenderer discard;
    final TaizhouMahjongHandRenderer hand;
    final TaizhouCenterClockRenderer centerClock;
    final TaizhouSettleRenderer settle;
    final TaizhouTotalResultRenderer totalResult;
    final TaizhouMultipleRenderer multiple;
    final TaizhouEarlyStartRenderer earlyStart;
    final TaizhouRoomMessageRenderer roomMessage;
    final TaizhouRoundOverlayController roundOverlays;
    final TaizhouTableInfoRenderer tableInfo;
    final TaizhouCanHuRenderer canHu;
    final TaizhouVoiceLoadOverlayRenderer voiceLoadOverlay;

    TaizhouMahjongTableRenderers(Context context) {
        this.context = context.getApplicationContext();
        background = decode(context, R.drawable.taizhou_mahjong_scene_background);
        Bitmap gameLayerAtlas = decode(context, R.drawable.taizhou_mahjong_game_layer);
        Bitmap tableInfoAtlas = decode(context, R.drawable.taizhou_mahjong_table_info);
        ready = TaizhouMahjongGameLayerBitmap.extract(gameLayerAtlas, "mah_ready.png");
        invite =
                TaizhouMahjongGameLayerBitmap.extract(
                        gameLayerAtlas, TaizhouMahjongWaitingLayout.INVITE_BUTTON.frameName);
        start =
                TaizhouMahjongGameLayerBitmap.extract(
                        gameLayerAtlas, TaizhouMahjongWaitingLayout.START_BUTTON.frameName);
        copy =
                TaizhouMahjongGameLayerBitmap.extract(
                        gameLayerAtlas, TaizhouMahjongWaitingLayout.COPY_BUTTON.frameName);
        roomInfo = new TaizhouMahjongRoomInfoRenderer(context, gameLayerAtlas);
        player = new TaizhouMahjongPlayerRenderer(context, gameLayerAtlas, tableInfoAtlas);
        iconAnimation = new TaizhouIconAnimationSelection();
        iconEffects = new TaizhouWaitingIconEffects(context.getAssets());
        waitingChrome =
                new TaizhouMahjongWaitingChromeRenderer(
                        context, gameLayerAtlas, iconAnimation, iconEffects);
        recordBadgeStore = new SxvipRecordBadgeStore(context);
        Bitmap mahjongIconAtlas = decode(context, R.drawable.taizhou_mahjong_icon);
        OriginalMahjongTilePainter tilePainter =
                new OriginalMahjongTilePainter(
                        decode(context, R.drawable.taizhou_mahjong_ground),
                        decode(context, R.drawable.taizhou_mahjong_face_atlas),
                        mahjongIconAtlas,
                        decode(context, R.drawable.taizhou_mahjong_face_type_1));
        discard = new TaizhouMahjongDiscardRenderer(
                tilePainter,
                new TaizhouMahjongDiscardEffectRenderer(context),
                decode(context, R.drawable.taizhou_mahjong_show_out_bg));
        centerClock = new TaizhouCenterClockRenderer(context, tilePainter);
        hand =
                new TaizhouMahjongHandRenderer(
                        tilePainter,
                        TaizhouMahjongIconBitmap.extract(
                                mahjongIconAtlas,
                                MahjongTileSprite.TING_ICON_FRAME));
        Bitmap totalResultAtlas = TaizhouTotalResultBitmap.load(context.getAssets());
        settle = new TaizhouSettleRenderer(
                context, tilePainter, gameLayerAtlas, totalResultAtlas);
        totalResult = new TaizhouTotalResultRenderer(context, player, totalResultAtlas);
        multiple = new TaizhouMultipleRenderer(context);
        earlyStart = new TaizhouEarlyStartRenderer(context);
        roomMessage = new TaizhouRoomMessageRenderer(context);
        tableInfo = new TaizhouTableInfoRenderer(context, tableInfoAtlas);
        canHu = new TaizhouCanHuRenderer(context, tilePainter);
        voiceLoadOverlay = new TaizhouVoiceLoadOverlayRenderer(context);
        roundOverlays =
                new TaizhouRoundOverlayController(
                        new TaizhouJokerAreaRenderer(tilePainter),
                        new TaizhouMeldRenderer(tilePainter),
                        new TaizhouFlowerAreaRenderer(tilePainter),
                        new TaizhouDiceRenderer(context),
                        new TaizhouActionTipOverlay(
                                decode(context, R.drawable.taizhou_mahjong_action_tip)),
                        new TaizhouActionBarHost(),
                        new TaizhouActionBarRenderer(
                                decode(context, R.drawable.taizhou_mahjong_action_btn),
                                tilePainter),
                        new TaizhouMahjongActionEffectRenderer(context));
    }

    /**
     * 桌布：{@code TableBgRes[7]} 在原版是空串（霞光胜境由皮肤动画自带），这里回落到
     * 牌桌自带的场景底图，不让桌面变空。
     */
    Bitmap tableBackground(int tableStyle) {
        if (tableStyle < 1 || tableStyle > TABLE_BACKGROUNDS.length) {
            return background;
        }
        int index = tableStyle - 1;
        if (tableBackgrounds[index] == null) {
            tableBackgrounds[index] = decode(context, TABLE_BACKGROUNDS[index]);
        }
        return tableBackgrounds[index];
    }

    private static Bitmap decode(Context context, int resourceId) {
        return BitmapFactory.decodeResource(context.getResources(), resourceId);
    }
}
