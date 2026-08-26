package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import com.nanbeiyule.game.gameplay.GameplayTableState;

/** 直接按 MahjongNew/GameLayer/CSB/BigWinLost.csb 绘制台州麻将大结算。 */
final class TaizhouTotalResultRenderer {
    private static final int POSITIVE = Color.rgb(215, 78, 24);
    private static final int NEGATIVE = Color.rgb(3, 87, 146);
    private static final int NAME = Color.rgb(15, 43, 184);
    private static final int ID = Color.rgb(31, 86, 181);
    private static final int DESCRIPTION = Color.rgb(40, 99, 196);

    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint backgroundPaint = new Paint();
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final TaizhouMahjongPlayerRenderer playerRenderer;
    private final Bitmap title;
    private final Bitmap titleLeft;
    private final Bitmap titleRight;
    private final Bitmap topBack;
    private final Bitmap roomInfo;
    private final Bitmap card;
    private final Bitmap nicknameBackground;
    private final Bitmap host;
    private final Bitmap bigWinner;
    private final Bitmap backLobby;
    private final Bitmap share;

    TaizhouTotalResultRenderer(
            Context context, TaizhouMahjongPlayerRenderer playerRenderer, Bitmap atlas) {
        this.playerRenderer = playerRenderer;
        title = frame(atlas, "result_total_title.png");
        titleLeft = frame(atlas, "result_total_img.png");
        titleRight = frame(atlas, "result_total_img2.png");
        topBack = frame(atlas, "result_total_back.png");
        roomInfo = frame(atlas, "result_total_roominfo.png");
        card = frame(atlas, "result_total_item_bg.png");
        nicknameBackground = frame(atlas, "result_total_nick_name_bg.png");
        host = frame(atlas, "result_total_host.png");
        bigWinner = frame(atlas, "result_total_big_win.png");
        backLobby = frame(atlas, "result_total_back_lobby.png");
        share = frame(atlas, "result_total_share.png");
        textPaint.setTypeface(
                Typeface.createFromAsset(context.getAssets(), "fonts/fangzhengcuyuan.ttf"));
        // _KW_PANAEL_BG 的 Cocos Studio 渐变色：0xffc896ff -> 0xffffffff。
        backgroundPaint.setShader(new LinearGradient(
                0.0f,
                0.0f,
                0.0f,
                TaizhouTotalResultLayout.DESIGN_HEIGHT,
                Color.rgb(255, 200, 150),
                Color.WHITE,
                Shader.TileMode.CLAMP));
    }

    void draw(Canvas canvas, GameplayTableState state) {
        TaizhouTotalResultProjection.Result result =
                TaizhouTotalResultProjection.project(state);
        canvas.drawRect(
                0.0f,
                0.0f,
                TaizhouTotalResultLayout.DESIGN_WIDTH,
                TaizhouTotalResultLayout.DESIGN_HEIGHT,
                backgroundPaint);
        drawNode(canvas, titleLeft, TaizhouTotalResultLayout.TITLE_LEFT);
        drawNode(canvas, titleRight, TaizhouTotalResultLayout.TITLE_RIGHT);
        drawNode(canvas, title, TaizhouTotalResultLayout.TITLE);
        drawNode(canvas, topBack, TaizhouTotalResultLayout.TOP_BACK);
        drawNode(canvas, roomInfo, TaizhouTotalResultLayout.ROOM_INFO);
        drawRoomInfo(canvas, result);
        for (int index = 0; index < result.players().size() && index < 4; index++) {
            drawPlayer(canvas, result.players().get(index), index);
        }
        drawNode(canvas, backLobby, TaizhouTotalResultLayout.BUTTON_BACK_LOBBY);
        drawNode(canvas, share, TaizhouTotalResultLayout.BUTTON_SHARE);
    }

    private void drawRoomInfo(Canvas canvas, TaizhouTotalResultProjection.Result result) {
        drawText(canvas, result.roomLabel(), 25.0f, 91.0f, 28.0f, DESCRIPTION, Paint.Align.LEFT);
        drawText(canvas, result.playCountLabel(), 266.0f, 91.0f, 28.0f, DESCRIPTION, Paint.Align.LEFT);
        drawText(canvas, result.timeLabel(), 25.0f, 132.0f, 28.0f, DESCRIPTION, Paint.Align.LEFT);
    }

    private void drawPlayer(
            Canvas canvas, TaizhouTotalResultProjection.Player player, int index) {
        float centerX = TaizhouTotalResultLayout.playerCenterX(index);
        float top = TaizhouTotalResultLayout.PLAYER_CENTER_Y
                - TaizhouTotalResultLayout.PLAYER_HEIGHT / 2.0f;
        drawCentered(canvas, card, centerX, TaizhouTotalResultLayout.PLAYER_CENTER_Y,
                TaizhouTotalResultLayout.PLAYER_WIDTH, TaizhouTotalResultLayout.PLAYER_HEIGHT);
        if (player.bigWinner()) {
            drawCentered(canvas, bigWinner, centerX + 60.246f, top + 9.412f, 442.0f, 140.0f);
        }
        drawCentered(canvas, playerRenderer.avatarBitmap(player.avatarKey()),
                centerX - 138.0f, top + 113.698f, 130.0f, 130.0f);
        if (player.host()) {
            drawCentered(canvas, host, centerX - 79.0f, top + 169.764f, 68.0f, 70.0f);
        }
        drawCentered(canvas, nicknameBackground, centerX + 64.707f, top + 87.735f,
                261.0f, 50.0f);
        drawText(canvas, player.displayName(), centerX + 64.707f, top + 99.0f,
                36.0f, NAME, Paint.Align.CENTER);
        drawText(canvas, "ID:" + player.publicPlayerId(), centerX + 64.0f, top + 162.0f,
                34.0f, ID, Paint.Align.CENTER);
        float descriptionY = top + 254.0f;
        for (String description : player.scoreDescriptions()) {
            drawText(canvas, description, centerX, descriptionY, 38.0f,
                    DESCRIPTION, Paint.Align.CENTER);
            descriptionY += 68.0f;
        }
        drawText(canvas, player.totalScoreText(), centerX - 7.0f, top + 666.0f,
                72.0f, player.totalScore() >= 0 ? POSITIVE : NEGATIVE, Paint.Align.CENTER);
    }

    private void drawNode(
            Canvas canvas, Bitmap bitmap, TaizhouTotalResultLayout.Node node) {
        drawCentered(canvas, bitmap, node.centerX(), node.centerY(), node.width(), node.height());
    }

    private void drawCentered(
            Canvas canvas, Bitmap bitmap, float centerX, float centerY, float width, float height) {
        canvas.drawBitmap(bitmap, null, new RectF(
                centerX - width / 2.0f,
                centerY - height / 2.0f,
                centerX + width / 2.0f,
                centerY + height / 2.0f), bitmapPaint);
    }

    private void drawText(
            Canvas canvas, String value, float x, float baseline, float size, int color,
            Paint.Align align) {
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        textPaint.setTextAlign(align);
        canvas.drawText(value == null ? "" : value, x, baseline, textPaint);
    }

    private static Bitmap frame(Bitmap atlas, String name) {
        return TaizhouTotalResultBitmap.extract(atlas, name);
    }
}
