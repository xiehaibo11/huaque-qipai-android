package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/** Bitmaps recovered from the original NewGoldHall mail atlases. */
final class MailDrawableSet {
    final Bitmap background;
    final Bitmap backArrow;
    final Bitmap title;
    final Bitmap youxiang;
    final Bitmap tips;
    final Bitmap ziHave;
    final Bitmap ziEmpty;
    final Bitmap paperPanel;
    final Bitmap iconOne;
    final Bitmap iconTwo;
    final Bitmap emptyBox;
    final Bitmap rowBackground;
    final Bitmap buttonBlue;
    final Bitmap buttonYellow;
    final Bitmap checkboxOff;
    final Bitmap checkboxOn;
    final Bitmap redPoint;
    final Bitmap timeIcon;
    final Bitmap awardBadge;
    final Bitmap claimedStamp;
    final Bitmap coinIcon;
    final Bitmap diamondIcon;
    final Bitmap roomCardIcon;

    MailDrawableSet(Resources resources) {
        background = load(resources, R.drawable.img_mail_bg);
        backArrow = load(resources, R.drawable.btn_fanhui);
        title = load(resources, R.drawable.img_mail_title);
        youxiang = load(resources, R.drawable.img_mail_youxiang);
        tips = load(resources, R.drawable.img_mail_tips);
        ziHave = load(resources, R.drawable.img_mail_zi1);
        ziEmpty = load(resources, R.drawable.img_mail_zi2);
        paperPanel = load(resources, R.drawable.img_mail_xf_di);
        iconOne = load(resources, R.drawable.img_mail_yj_1);
        iconTwo = load(resources, R.drawable.img_mail_yj_2);
        emptyBox = load(resources, R.drawable.img_mail_kzt);
        rowBackground = load(resources, R.drawable.img_zj_tiao);
        buttonBlue = load(resources, R.drawable.btn_mail_lv);
        buttonYellow = load(resources, R.drawable.btn_mail_yjlq);
        checkboxOff = load(resources, R.drawable.btn_mail_off);
        checkboxOn = load(resources, R.drawable.btn_mail_on);
        redPoint = load(resources, R.drawable.img_mail_red);
        timeIcon = load(resources, R.drawable.img_mail_time);
        awardBadge = load(resources, R.drawable.img_mail_ts);
        claimedStamp = load(resources, R.drawable.img_ylq);
        coinIcon = load(resources, R.drawable.home_icon_coin);
        diamondIcon = load(resources, R.drawable.home_icon_diamond);
        roomCardIcon = load(resources, R.drawable.home_icon_room_card);
    }

    /** 附件图标按服务端 rewardType 复用大厅货币图标；未知类型返回 null 只绘文本。 */
    Bitmap attachmentIcon(String rewardType) {
        if ("COIN".equals(rewardType)) {
            return coinIcon;
        }
        if ("DIAMOND".equals(rewardType)) {
            return diamondIcon;
        }
        if ("ROOM_CARD".equals(rewardType)) {
            return roomCardIcon;
        }
        return null;
    }

    private static Bitmap load(Resources resources, int resourceId) {
        Bitmap result = BitmapFactory.decodeResource(resources, resourceId);
        if (result == null) {
            throw new IllegalStateException("Unable to decode mail drawable " + resourceId);
        }
        return result;
    }
}
