package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/** Byte-exact Xianyi Dou Dizhu QuicklyJoin controls packaged in drawable-nodpi. */
final class XianyiJoinRoomDrawableSet {
    final Bitmap title;
    final Bitmap slot;
    final Bitmap keyNormal;
    final Bitmap keyPressed;
    final Bitmap delete;
    final Bitmap redo;
    final Bitmap keypadFont;
    final Bitmap slotFont;

    XianyiJoinRoomDrawableSet(Resources resources) {
        title = load(resources, R.drawable.xianyi_join_room_title);
        slot = load(resources, R.drawable.xianyi_join_room_slot);
        keyNormal = load(resources, R.drawable.xianyi_join_room_key_normal);
        keyPressed = load(resources, R.drawable.xianyi_join_room_key_pressed);
        delete = load(resources, R.drawable.xianyi_join_room_delete);
        redo = load(resources, R.drawable.xianyi_join_room_redo);
        keypadFont = load(resources, R.drawable.xianyi_join_room_key_font);
        slotFont = load(resources, R.drawable.xianyi_join_room_slot_font);
    }

    private static Bitmap load(Resources resources, int resourceId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(resources, resourceId, options);
        if (bitmap == null) {
            throw new IllegalStateException("Unable to decode Xianyi join-room resource " + resourceId);
        }
        return bitmap;
    }
}
