package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/** Original frames referenced by JoinBoxRoom.csb. */
final class JoinRoomDrawableSet {
    static final String[] REQUIRED_DRAWABLES = {
        "original_join_room_panel",
        "original_join_room_close",
        "original_join_room_key_normal",
        "original_join_room_key_pressed",
        "original_join_room_reset",
        "original_join_room_delete",
        "original_join_room_title",
        "original_join_room_number_bg",
        "original_join_room_dot",
        "original_join_room_area_bg",
        "original_join_room_area_line",
        "original_join_room_tip_bg",
        "original_join_room_number_0",
        "original_join_room_number_1",
        "original_join_room_number_2",
        "original_join_room_number_3",
        "original_join_room_number_4",
        "original_join_room_number_5",
        "original_join_room_number_6",
        "original_join_room_number_7",
        "original_join_room_number_8",
        "original_join_room_number_9"
    };

    final Bitmap panel;
    final Bitmap close;
    final Bitmap keyNormal;
    final Bitmap keyPressed;
    final Bitmap reset;
    final Bitmap delete;
    final Bitmap title;
    final Bitmap roomNumberBackground;
    final Bitmap dot;
    final Bitmap areaBackground;
    final Bitmap areaLine;
    final Bitmap tipBackground;
    final Bitmap[] numbers;

    JoinRoomDrawableSet(Resources resources) {
        panel = load(resources, R.drawable.original_join_room_panel);
        close = load(resources, R.drawable.original_join_room_close);
        keyNormal = load(resources, R.drawable.original_join_room_key_normal);
        keyPressed = load(resources, R.drawable.original_join_room_key_pressed);
        reset = load(resources, R.drawable.original_join_room_reset);
        delete = load(resources, R.drawable.original_join_room_delete);
        title = load(resources, R.drawable.original_join_room_title);
        roomNumberBackground = load(resources, R.drawable.original_join_room_number_bg);
        dot = load(resources, R.drawable.original_join_room_dot);
        areaBackground = load(resources, R.drawable.original_join_room_area_bg);
        areaLine = load(resources, R.drawable.original_join_room_area_line);
        tipBackground = load(resources, R.drawable.original_join_room_tip_bg);
        numbers =
                new Bitmap[] {
                    load(resources, R.drawable.original_join_room_number_0),
                    load(resources, R.drawable.original_join_room_number_1),
                    load(resources, R.drawable.original_join_room_number_2),
                    load(resources, R.drawable.original_join_room_number_3),
                    load(resources, R.drawable.original_join_room_number_4),
                    load(resources, R.drawable.original_join_room_number_5),
                    load(resources, R.drawable.original_join_room_number_6),
                    load(resources, R.drawable.original_join_room_number_7),
                    load(resources, R.drawable.original_join_room_number_8),
                    load(resources, R.drawable.original_join_room_number_9)
                };
    }

    private static Bitmap load(Resources resources, int resourceId) {
        Bitmap bitmap = BitmapFactory.decodeResource(resources, resourceId);
        if (bitmap == null) {
            throw new IllegalStateException("Unable to decode join-room resource " + resourceId);
        }
        return bitmap;
    }
}
