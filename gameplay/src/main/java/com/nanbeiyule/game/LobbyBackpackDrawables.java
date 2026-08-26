package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

final class LobbyBackpackDrawables {
    final ShopDrawableSet shop;
    final Bitmap detailPanel;
    final Bitmap itemBackground;
    final Bitmap itemNameBackground;
    final Bitmap itemSelected;
    final Bitmap itemWatermark;
    final Bitmap leftStrip;
    final Bitmap pedestal;
    final Bitmap remainingBackground;
    final Bitmap shopButton;
    final Bitmap subStrip;
    final Bitmap tabSelected;
    final Bitmap title;
    final Bitmap useButton;

    LobbyBackpackDrawables(Resources resources) {
        shop = ShopDrawableSet.obtain(resources);
        detailPanel = load(resources, R.drawable.lobby_backpack_detail_panel);
        itemBackground = load(resources, R.drawable.lobby_backpack_item_bg);
        itemNameBackground = load(resources, R.drawable.lobby_backpack_item_name_bg);
        itemSelected = load(resources, R.drawable.lobby_backpack_item_selected);
        itemWatermark = load(resources, R.drawable.lobby_backpack_item_watermark);
        leftStrip = load(resources, R.drawable.lobby_backpack_left_strip);
        pedestal = load(resources, R.drawable.lobby_backpack_pedestal);
        remainingBackground = load(resources, R.drawable.lobby_backpack_remaining_bg);
        shopButton = load(resources, R.drawable.lobby_backpack_shop_button);
        subStrip = load(resources, R.drawable.lobby_backpack_sub_strip);
        tabSelected = load(resources, R.drawable.lobby_backpack_tab_selected);
        title = load(resources, R.drawable.lobby_backpack_title);
        useButton = load(resources, R.drawable.lobby_backpack_use_button);
    }

    private static Bitmap load(Resources resources, int resourceId) {
        Bitmap result = BitmapFactory.decodeResource(resources, resourceId);
        if (result == null) throw new IllegalStateException("Unable to decode " + resourceId);
        return result;
    }
}
