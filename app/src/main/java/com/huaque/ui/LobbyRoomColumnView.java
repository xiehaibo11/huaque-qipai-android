package com.huaque.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

/** Right lobby column rebuilt to fit friend, create-room, and join-room cards. */
final class LobbyRoomColumnView extends View {
    static final int PSD_X = LobbyRoomColumnLayoutModel.PSD_X;
    static final int PSD_Y = LobbyRoomColumnLayoutModel.PSD_Y;
    static final int PSD_WIDTH = LobbyRoomColumnLayoutModel.PSD_WIDTH;
    static final int PSD_HEIGHT = LobbyRoomColumnLayoutModel.PSD_HEIGHT;
    static final int FRIEND_TOP = LobbyRoomColumnLayoutModel.FRIEND.top();
    static final int FRIEND_HEIGHT = LobbyRoomColumnLayoutModel.FRIEND.height();
    static final int CREATE_TOP = LobbyRoomColumnLayoutModel.CREATE.top();
    static final int CREATE_HEIGHT = LobbyRoomColumnLayoutModel.CREATE.height();
    static final int JOIN_TOP = LobbyRoomColumnLayoutModel.JOIN.top();
    static final int JOIN_HEIGHT = LobbyRoomColumnLayoutModel.JOIN.height();

    private static final Rect BACKGROUND_SOURCE = new Rect(1947, 235, 2340, 848);
    private static final Rect FRIEND_SOURCE = new Rect(790, 0, 1183, 363);
    private static final Rect CREATE_SOURCE = new Rect(790, 391, 1183, 613);

    private final Bitmap background;
    private final Bitmap existingCards;
    private final Bitmap joinRoom;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    LobbyRoomColumnView(Context context) {
        super(context);
        background = bitmap(R.drawable.lobby_bg);
        existingCards = bitmap(R.drawable.lobby_game_cards_static);
        joinRoom = bitmap(R.drawable.lobby_join_room_wide);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        canvas.drawBitmap(
                background,
                BACKGROUND_SOURCE,
                new RectF(0f, 0f, width, getHeight()),
                paint);
        canvas.drawBitmap(
                existingCards,
                FRIEND_SOURCE,
                destination(LobbyRoomColumnLayoutModel.FRIEND, FRIEND_SOURCE),
                paint);
        canvas.drawBitmap(
                existingCards,
                CREATE_SOURCE,
                destination(LobbyRoomColumnLayoutModel.CREATE, CREATE_SOURCE),
                paint);
        canvas.drawBitmap(
                joinRoom,
                null,
                destination(
                        LobbyRoomColumnLayoutModel.JOIN,
                        joinRoom.getWidth(),
                        joinRoom.getHeight()),
                paint);
    }

    private RectF destination(LobbyRoomColumnLayoutModel.CardSpec spec, Rect source) {
        return destination(spec, source.width(), source.height());
    }

    private RectF destination(
            LobbyRoomColumnLayoutModel.CardSpec spec,
            int sourceWidth,
            int sourceHeight) {
        float slotTop = scaledY(spec.top());
        float slotHeight = scaledY(spec.height());
        LobbyRoomColumnLayoutModel.FittedSize fitted =
                LobbyRoomColumnLayoutModel.fitCenter(
                        getWidth(), slotHeight, sourceWidth, sourceHeight);
        float left = (getWidth() - fitted.width()) / 2f;
        float top = slotTop + (slotHeight - fitted.height()) / 2f;
        return new RectF(left, top, left + fitted.width(), top + fitted.height());
    }

    private float scaledY(float designY) {
        return designY * getHeight() / PSD_HEIGHT;
    }

    private Bitmap bitmap(int resourceId) {
        Bitmap value = BitmapFactory.decodeResource(getResources(), resourceId);
        if (value == null) {
            throw new IllegalStateException("Unable to decode lobby room card " + resourceId);
        }
        return value;
    }
}
