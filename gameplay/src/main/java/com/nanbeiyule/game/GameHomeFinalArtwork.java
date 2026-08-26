package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.List;

/** Decoded independent artwork for the approved final lobby layout. */
final class GameHomeFinalArtwork {
    final Bitmap character;
    final Bitmap hostessPlatform;
    final Bitmap bottomBar;
    final Bitmap store;
    final Bitmap quickStartButton;
    final Bitmap quickStartLabel;
    final Bitmap resourceRoomCard;
    final Bitmap resourceCoin;
    final Bitmap resourceDiamond;
    final Bitmap walletTrack;
    final Bitmap walletAdd;
    final List<Bitmap> topActions;
    final List<Bitmap> sideActions;
    final List<Bitmap> primaryEntries;
    final Bitmap primaryBackRoom;
    final List<Bitmap> gameEntries;

    GameHomeFinalArtwork(Context context) {
        character = load(context, R.drawable.game_home_final_character);
        hostessPlatform = load(context, R.drawable.game_home_final_hostess_platform);
        bottomBar = load(context, R.drawable.game_home_final_bottom_bar);
        store = load(context, R.drawable.home_icon_store);
        quickStartButton = load(context, R.drawable.home_button_quick_start);
        quickStartLabel = load(context, R.drawable.home_label_quick_start);
        resourceRoomCard = load(context, R.drawable.game_home_final_resource_room_card);
        resourceCoin = load(context, R.drawable.game_home_final_resource_coin);
        resourceDiamond = load(context, R.drawable.game_home_final_resource_diamond);
        walletTrack = load(context, R.drawable.game_home_final_wallet_track);
        walletAdd = load(context, R.drawable.game_home_final_wallet_add);
        topActions =
                List.of(
                        load(context, R.drawable.game_home_final_top_member),
                        load(context, R.drawable.game_home_final_top_welfare),
                        load(context, R.drawable.game_home_final_top_mail),
                        load(context, R.drawable.game_home_final_top_customer),
                        load(context, R.drawable.game_home_final_top_settings));
        sideActions =
                List.of(
                        load(context, R.drawable.game_home_final_side_welfare),
                        load(context, R.drawable.game_home_final_side_chest),
                        load(context, R.drawable.game_home_final_side_checkin));
        primaryEntries =
                List.of(
                        load(context, R.drawable.game_home_play_create_room),
                        load(context, R.drawable.game_home_play_join),
                        load(context, R.drawable.game_home_play_match));
        // 原版 LobbyView:showBackBoom() 只给 MainScene.csb 的子节点
        // _KW_IMG_BOX_ROOM_TITLE(224x60) 换 lobby_title_back_box.png，按钮底图不变。
        // 本工程的卡片把文案烤进画面，只能整卡替换，这是被素材形态逼出的偏差。
        primaryBackRoom = load(context, R.drawable.game_home_play_back_room);
        gameEntries =
                List.of(
                        load(context, R.drawable.game_home_play_taizhou),
                        load(context, R.drawable.game_home_play_shi_san_shui),
                        load(context, R.drawable.game_home_play_wa_hua));
    }

    private static Bitmap load(Context context, int resourceId) {
        return BitmapFactory.decodeResource(context.getResources(), resourceId);
    }
}
