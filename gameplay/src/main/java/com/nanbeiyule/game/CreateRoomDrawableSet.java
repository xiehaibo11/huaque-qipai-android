package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/** Only recovered original create-room bitmaps; no generated substitutes. */
final class CreateRoomDrawableSet {
    final Bitmap background;
    final Bitmap topBackground;
    final Bitmap titleBackground;
    final Bitmap title;
    final Bitmap back;
    final Bitmap splitLine;
    final Bitmap tabNormal;
    final Bitmap tabSelected;
    final Bitmap gameBadge;
    final Bitmap externalGameBadge;
    final Bitmap rowLine;
    final Bitmap radioBackground;
    final Bitmap radioChecked;
    final Bitmap checkboxBackground;
    final Bitmap checkboxChecked;
    final Bitmap tip;
    final Bitmap tipBubble;
    final Bitmap create;
    final Bitmap roomCard;
    final Bitmap feedback;

    CreateRoomDrawableSet(Resources resources) {
        background = load(resources, R.drawable.tea_house_rank_bg);
        topBackground = load(resources, R.drawable.tea_house_rank_top_bg);
        titleBackground = load(resources, R.drawable.tea_house_rank_title_bg);
        title = load(resources, R.drawable.form_title_create_box_room);
        back = load(resources, R.drawable.com_btn_back);
        splitLine = load(resources, R.drawable.com_ui_line_2);
        tabNormal = load(resources, R.drawable.com_btn_label_normal);
        tabSelected = load(resources, R.drawable.com_btn_label_select);
        gameBadge = load(resources, R.drawable.create_box_room_mark);
        externalGameBadge = load(resources, R.drawable.promote_hall_createroom_act_new);
        rowLine = load(resources, R.drawable.com_ui_line_1);
        radioBackground = load(resources, R.drawable.com_btn_check_box1_bg);
        radioChecked = load(resources, R.drawable.com_btn_check_box1);
        checkboxBackground = load(resources, R.drawable.com_btn_check_box2_bg);
        checkboxChecked = load(resources, R.drawable.com_btn_check_box2);
        tip = load(resources, R.drawable.com_btn_tips);
        tipBubble = load(resources, R.drawable.com_ui_tip_paopao);
        create = load(resources, R.drawable.com_btn_create_box_room);
        roomCard = load(resources, R.drawable.ico_card);
        feedback = load(resources, R.drawable.feedbackicon);
    }

    private static Bitmap load(Resources resources, int id) {
        Bitmap bitmap = BitmapFactory.decodeResource(resources, id);
        if (bitmap == null) {
            throw new IllegalStateException("Unable to decode create-room drawable " + id);
        }
        return bitmap;
    }
}
