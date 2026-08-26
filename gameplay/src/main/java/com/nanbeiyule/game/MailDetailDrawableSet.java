package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

final class MailDetailDrawableSet {
    final Bitmap close;
    final Bitmap blueButton;
    final Bitmap yellowButton;
    final Bitmap separator;
    final Bitmap awardBackground;
    final Bitmap claimedStamp;
    final Bitmap claimedMask;
    final Bitmap coin;
    final Bitmap diamond;
    final Bitmap roomCard;

    MailDetailDrawableSet(Resources resources) {
        close = load(resources, R.drawable.btn_mail_detail_close);
        blueButton = load(resources, R.drawable.btn_mail_detail_blue);
        yellowButton = load(resources, R.drawable.btn_mail_detail_yellow);
        separator = load(resources, R.drawable.img_mail_hw1);
        awardBackground = load(resources, R.drawable.img_mail_detail_award_bg);
        claimedStamp = load(resources, R.drawable.img_ylq);
        claimedMask = load(resources, R.drawable.img_mail_mask);
        coin = load(resources, R.drawable.home_icon_coin);
        diamond = load(resources, R.drawable.home_icon_diamond);
        roomCard = load(resources, R.drawable.home_icon_room_card);
    }

    Bitmap rewardIcon(String type) {
        return switch (type) {
            case "COIN" -> coin;
            case "DIAMOND" -> diamond;
            case "ROOM_CARD" -> roomCard;
            default -> null;
        };
    }

    private static Bitmap load(Resources resources, int id) {
        Bitmap bitmap = BitmapFactory.decodeResource(resources, id);
        if (bitmap == null) throw new IllegalStateException("Unable to decode mail detail drawable");
        return bitmap;
    }
}
