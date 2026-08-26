package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import com.nanbeiyule.game.gameplay.GameplaySeat;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayerLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayerProjection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Draws PlayerHeadBase.csb from real gameplay-seat profiles. */
final class TaizhouMahjongPlayerRenderer {
    private static final String ELLIPSIS = "...";

    private final Bitmap headFrame;
    private final Bitmap hostFlag;
    private final Bitmap chengBaoFlag;
    private final Bitmap defaultAvatar;
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint nicknamePaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Paint scorePaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Map<String, Bitmap> avatarBitmaps = new HashMap<>();

    TaizhouMahjongPlayerRenderer(
            Context context, Bitmap gameLayerAtlas, Bitmap tableInfoAtlas) {
        headFrame = TaizhouMahjongGameLayerBitmap.extract(gameLayerAtlas, "mah_head_bg.png");
        hostFlag = TaizhouMahjongGameLayerBitmap.extract(gameLayerAtlas, "mah_host_flag.png");
        chengBaoFlag = TaizhouTableInfoRenderer.extract(tableInfoAtlas, "tz_chengbao.png");
        defaultAvatar = AvatarFrameRenderer.loadDefaultAvatar(context.getResources());
        nicknamePaint.setColor(
                Color.rgb(
                        TaizhouMahjongPlayerLayout.NICKNAME_RED,
                        TaizhouMahjongPlayerLayout.NICKNAME_GREEN,
                        TaizhouMahjongPlayerLayout.NICKNAME_BLUE));
        nicknamePaint.setTextSize(TaizhouMahjongPlayerLayout.NICKNAME_FONT_SIZE);
        nicknamePaint.setTextAlign(Paint.Align.CENTER);
        scorePaint.setColor(
                Color.rgb(
                        TaizhouMahjongPlayerLayout.SCORE_RED,
                        TaizhouMahjongPlayerLayout.SCORE_GREEN,
                        TaizhouMahjongPlayerLayout.SCORE_BLUE));
        scorePaint.setTextSize(TaizhouMahjongPlayerLayout.SCORE_FONT_SIZE);
        scorePaint.setTextAlign(Paint.Align.LEFT);
        // PlayerHeadBase.csb leaves both fontName and fontResource empty.
    }

    void draw(Canvas canvas, GameplayTableState state) {
        for (TaizhouMahjongPlayerProjection.Player player :
                TaizhouMahjongPlayerProjection.players(state)) {
            drawPlayer(canvas, player);
        }
    }

    List<String> missingAvatarKeys(GameplayTableState state) {
        List<String> missing = new ArrayList<>();
        for (String avatarKey : TaizhouMahjongPlayerProjection.remoteAvatarKeys(state)) {
            Bitmap bitmap = avatarBitmaps.get(avatarKey);
            if (bitmap == null || bitmap.isRecycled()) {
                missing.add(avatarKey);
            }
        }
        return List.copyOf(missing);
    }

    void setAvatarBitmap(String avatarKey, Bitmap bitmap) {
        if (avatarKey == null
                || avatarKey.isBlank()
                || bitmap == null
                || bitmap.isRecycled()) {
            return;
        }
        avatarBitmaps.put(avatarKey, bitmap);
    }

    /** BigWinLost 与牌桌共用已鉴权下载的真实头像，缺图时沿用同一原版默认头像。 */
    Bitmap avatarBitmap(String avatarKey) {
        Bitmap bitmap = avatarBitmaps.get(avatarKey);
        return bitmap == null || bitmap.isRecycled() ? defaultAvatar : bitmap;
    }

    private void drawPlayer(Canvas canvas, TaizhouMahjongPlayerProjection.Player player) {
        GameplaySeat seat = player.seat();
        TaizhouMahjongPlayerLayout.PlayerSlot slot = player.slot();
        Bitmap avatar = avatarBitmaps.get(seat.avatarKey());
        if (avatar == null || avatar.isRecycled()) {
            avatar = defaultAvatar;
        }
        drawCentered(
                canvas,
                headFrame,
                slot.centerX(),
                slot.centerY(),
                TaizhouMahjongPlayerLayout.HEAD_WIDTH,
                TaizhouMahjongPlayerLayout.HEAD_HEIGHT);
        drawCentered(
                canvas,
                avatar,
                slot.centerX() + TaizhouMahjongPlayerLayout.AVATAR_CENTER_OFFSET_X,
                slot.centerY() + TaizhouMahjongPlayerLayout.AVATAR_CENTER_OFFSET_Y,
                TaizhouMahjongPlayerLayout.AVATAR_WIDTH,
                TaizhouMahjongPlayerLayout.AVATAR_HEIGHT);
        if (player.chengBaoVisible()) {
            drawCentered(
                    canvas,
                    chengBaoFlag,
                    slot.centerX()
                            + TaizhouMahjongPlayerLayout.chengBaoCenterOffsetX(
                                    player.localSeat()),
                    slot.centerY() + TaizhouMahjongPlayerLayout.CHENG_BAO_CENTER_OFFSET_Y,
                    TaizhouMahjongPlayerLayout.CHENG_BAO_SIZE,
                    TaizhouMahjongPlayerLayout.CHENG_BAO_SIZE);
        }
        drawNickname(canvas, seat.displayName(), slot);
        drawScore(canvas, seat.score(), slot);
        if (seat.host()) {
            drawCentered(
                    canvas,
                    hostFlag,
                    slot.centerX() + TaizhouMahjongPlayerLayout.HOST_CENTER_OFFSET_X,
                    slot.centerY() + TaizhouMahjongPlayerLayout.HOST_CENTER_OFFSET_Y,
                    TaizhouMahjongPlayerLayout.HOST_WIDTH,
                    TaizhouMahjongPlayerLayout.HOST_HEIGHT);
        }
    }

    private void drawScore(
            Canvas canvas,
            long score,
            TaizhouMahjongPlayerLayout.PlayerSlot slot) {
        float left = slot.centerX() + TaizhouMahjongPlayerLayout.SCORE_LEFT_OFFSET_X;
        float centerY = slot.centerY() + TaizhouMahjongPlayerLayout.SCORE_CENTER_OFFSET_Y;
        RectF bounds =
                new RectF(
                        left,
                        centerY - TaizhouMahjongPlayerLayout.SCORE_HEIGHT / 2.0f,
                        left + TaizhouMahjongPlayerLayout.SCORE_WIDTH,
                        centerY + TaizhouMahjongPlayerLayout.SCORE_HEIGHT / 2.0f);
        int save = canvas.save();
        canvas.clipRect(bounds);
        Paint.FontMetrics metrics = scorePaint.getFontMetrics();
        float baseline = centerY - (metrics.ascent + metrics.descent) / 2.0f;
        canvas.drawText(Long.toString(score), left, baseline, scorePaint);
        canvas.restoreToCount(save);
    }

    private void drawNickname(
            Canvas canvas,
            String displayName,
            TaizhouMahjongPlayerLayout.PlayerSlot slot) {
        float centerX =
                slot.centerX() + TaizhouMahjongPlayerLayout.NICKNAME_CENTER_OFFSET_X;
        float centerY =
                slot.centerY() + TaizhouMahjongPlayerLayout.NICKNAME_CENTER_OFFSET_Y;
        RectF bounds =
                centered(
                        centerX,
                        centerY,
                        TaizhouMahjongPlayerLayout.NICKNAME_WIDTH,
                        TaizhouMahjongPlayerLayout.NICKNAME_HEIGHT);
        int save = canvas.save();
        canvas.clipRect(bounds);
        Paint.FontMetrics metrics = nicknamePaint.getFontMetrics();
        float baseline = centerY - (metrics.ascent + metrics.descent) / 2.0f;
        canvas.drawText(fitNickname(displayName), centerX, baseline, nicknamePaint);
        canvas.restoreToCount(save);
    }

    private String fitNickname(String displayName) {
        String nickname = displayName.replace("\n", "");
        float maxWidth = TaizhouMahjongPlayerLayout.NICKNAME_WIDTH;
        if (nicknamePaint.measureText(nickname) <= maxWidth) {
            return nickname;
        }
        float textWidth = maxWidth - nicknamePaint.measureText(ELLIPSIS);
        if (textWidth <= 0.0f) {
            return "";
        }
        int count = nicknamePaint.breakText(nickname, true, textWidth, null);
        if (count <= 0) {
            return ELLIPSIS;
        }
        if (count < nickname.length() && Character.isLowSurrogate(nickname.charAt(count))) {
            count--;
        }
        return nickname.substring(0, count) + ELLIPSIS;
    }

    private void drawCentered(
            Canvas canvas, Bitmap bitmap, float centerX, float centerY, float width, float height) {
        canvas.drawBitmap(bitmap, null, centered(centerX, centerY, width, height), bitmapPaint);
    }

    private static RectF centered(
            float centerX, float centerY, float width, float height) {
        return new RectF(
                centerX - width / 2.0f,
                centerY - height / 2.0f,
                centerX + width / 2.0f,
                centerY + height / 2.0f);
    }
}
