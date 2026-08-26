package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

final class GameRecordDrawableSet {
    final Bitmap background;
    final Bitmap top;
    final Bitmap titleBackground;
    final Bitmap title;
    final Bitmap back;
    final Bitmap goldOff;
    final Bitmap goldOn;
    final Bitmap battleOff;
    final Bitmap battleOn;
    final Bitmap inputBackground;
    final Bitmap dropDown;
    final Bitmap dropUp;
    final Bitmap refresh;
    final Bitmap totalGold;
    final Bitmap totalBattle;
    final Bitmap openMember;
    final Bitmap replay;
    final Bitmap popup;
    final Bitmap itemBackground;
    final Bitmap gameBackground;
    final Bitmap detail;
    final Bitmap host;

    GameRecordDrawableSet(Resources resources) {
        background = load(resources, R.drawable.game_record_background);
        top = load(resources, R.drawable.game_record_top);
        titleBackground = load(resources, R.drawable.game_record_title_bg);
        title = load(resources, R.drawable.game_record_title);
        back = load(resources, R.drawable.game_record_back);
        goldOff = load(resources, R.drawable.game_record_gold_off);
        goldOn = load(resources, R.drawable.game_record_gold_on);
        battleOff = load(resources, R.drawable.game_record_battle_off);
        battleOn = load(resources, R.drawable.game_record_battle_on);
        inputBackground = load(resources, R.drawable.game_record_input_bg);
        dropDown = load(resources, R.drawable.game_record_drop_down);
        dropUp = load(resources, R.drawable.game_record_drop_up);
        refresh = load(resources, R.drawable.game_record_refresh);
        totalGold = load(resources, R.drawable.game_record_total_gold);
        totalBattle = load(resources, R.drawable.game_record_total_battle);
        openMember = load(resources, R.drawable.game_record_open_member);
        replay = load(resources, R.drawable.game_record_replay);
        popup = load(resources, R.drawable.game_record_popup_bg);
        itemBackground = load(resources, R.drawable.game_record_item_bg);
        gameBackground = load(resources, R.drawable.game_record_game_bg);
        detail = load(resources, R.drawable.game_record_detail);
        host = load(resources, R.drawable.game_record_host);
    }

    private static Bitmap load(Resources resources, int id) {
        return BitmapFactory.decodeResource(resources, id);
    }
}
