package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import com.nanbeiyule.game.mahjong.TaizhouMahjongWaitingLayout;

/** Draws the original fixed controls surrounding the Taizhou Mahjong table. */
final class TaizhouMahjongWaitingChromeRenderer {
    private static final float CHANGE_CARD_PRICE_CENTER_X = 1698.04f;
    private static final float CHANGE_CARD_PRICE_CENTER_Y = 216.94f;
    private static final float SHUFFLE_PRICE_CENTER_X = 1855.0f;
    private static final float SHUFFLE_PRICE_CENTER_Y = 218.0f;
    private static final int COPY_CAP_LEFT = 71;
    private static final int COPY_CAP_TOP = 36;
    private static final int COPY_CAP_RIGHT = 18;
    private static final int COPY_CAP_BOTTOM = 48;

    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint recordPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final ColorMatrixColorFilter lockedRecordFilter;
    private final Typeface typeface;
    private final Bitmap rule;
    private final Bitmap record;
    private final Bitmap recordRedPoint;
    private final Bitmap friends;
    private final Bitmap menu;
    private final Bitmap trust;
    private final Bitmap changeCard;
    private final Bitmap shuffle;
    private final Bitmap priceBackground;
    private final Bitmap roomCard;
    private final Bitmap fortune;
    private final Bitmap ting;
    private final Bitmap chat;
    private final Bitmap voice;
    private final Bitmap luckyMission;
    private final Bitmap treasurePot;
    private final Bitmap inviteCaishen;
    private final Bitmap copyRecommendation;
    private TaizhouRoomToolsState roomToolsState;
    private final TaizhouIconAnimationSelection iconAnimation;
    private final TaizhouWaitingIconEffects iconEffects;

    TaizhouMahjongWaitingChromeRenderer(
            Context context,
            Bitmap gameLayerAtlas,
            TaizhouIconAnimationSelection iconAnimation,
            TaizhouWaitingIconEffects iconEffects) {
        this.iconAnimation = iconAnimation;
        this.iconEffects = iconEffects;
        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/fangzhengcuyuan.ttf");
        rule = TaizhouMahjongGameLayerBitmap.extract(gameLayerAtlas, "mah_btn_rule.png");
        menu = TaizhouMahjongGameLayerBitmap.extract(gameLayerAtlas, "mah_set_btn.png");
        trust = TaizhouMahjongGameLayerBitmap.extract(gameLayerAtlas, "mah_tuoguan_btn.png");
        chat = TaizhouMahjongGameLayerBitmap.extract(gameLayerAtlas, "mah_msg_btn.png");
        voice = TaizhouMahjongGameLayerBitmap.extract(gameLayerAtlas, "mah_speak_btn.png");
        record = bitmap(context, R.drawable.taizhou_mahjong_battle_record);
        recordRedPoint =
                bitmap(context, R.drawable.taizhou_mahjong_battle_record_red_point);
        friends = bitmap(context, R.drawable.taizhou_mahjong_friend_tab);
        changeCard = bitmap(context, R.drawable.taizhou_mahjong_change_card);
        shuffle = bitmap(context, R.drawable.taizhou_mahjong_shuffle);
        priceBackground = bitmap(context, R.drawable.taizhou_mahjong_price_background);
        roomCard = bitmap(context, R.drawable.taizhou_mahjong_room_card_small);
        fortune = bitmap(context, R.drawable.taizhou_mahjong_fortune);
        ting = TaizhouMahjongGameLayerBitmap.extract(gameLayerAtlas, "mah_ting_btn.png");
        luckyMission = bitmap(context, R.drawable.taizhou_mahjong_lucky_mission);
        treasurePot = bitmap(context, R.drawable.taizhou_mahjong_treasure_pot);
        inviteCaishen = bitmap(context, R.drawable.taizhou_mahjong_invite_caishen);
        copyRecommendation = bitmap(context, R.drawable.taizhou_mahjong_copy_tip);
        ColorMatrix grayscale = new ColorMatrix();
        grayscale.setSaturation(0.0f);
        lockedRecordFilter = new ColorMatrixColorFilter(grayscale);
    }

    void draw(
            Canvas canvas,
            boolean showCopyRecommendation,
            boolean recordLocked,
            boolean showRecordRedPoint,
            boolean showTableActivityIcons,
            boolean showTingButton,
            boolean showTrustButton,
            float elapsedSeconds) {
        drawNode(canvas, rule, TaizhouMahjongWaitingLayout.RULE_BUTTON);
        drawRecord(canvas, recordLocked);
        if (showRecordRedPoint) {
            drawNode(
                    canvas,
                    recordRedPoint,
                    TaizhouMahjongWaitingLayout.RECORD_RED_POINT);
        }
        drawNode(canvas, friends, TaizhouMahjongWaitingLayout.FRIEND_BUTTON);
        drawNode(canvas, menu, TaizhouMahjongWaitingLayout.MENU_BUTTON);
        if (showTrustButton) {
            drawNode(canvas, trust, TaizhouMahjongWaitingLayout.TRUST_BUTTON);
        }

        drawCentered(canvas, changeCard, CHANGE_CARD_PRICE_CENTER_X, 176.94f, 100.0f, 85.0f);
        drawPropPrice(
                canvas,
                CHANGE_CARD_PRICE_CENTER_X,
                CHANGE_CARD_PRICE_CENTER_Y,
                priceLabel(TaizhouRoomToolType.CHANGE_CARD));
        drawCentered(canvas, shuffle, SHUFFLE_PRICE_CENTER_X, 178.0f, 100.0f, 84.0f);
        drawPropPrice(
                canvas,
                SHUFFLE_PRICE_CENTER_X,
                SHUFFLE_PRICE_CENTER_Y,
                priceLabel(TaizhouRoomToolType.SHUFFLE));

        if (showTingButton) {
            drawNode(canvas, ting, TaizhouMahjongWaitingLayout.TING_BUTTON);
        } else {
            drawNode(canvas, fortune, TaizhouMahjongWaitingLayout.FORTUNE_BUTTON);
        }
        drawNode(canvas, chat, TaizhouMahjongWaitingLayout.CHAT_BUTTON);
        drawNode(canvas, voice, TaizhouMahjongWaitingLayout.VOICE_BUTTON);
        if (showTableActivityIcons) {
            drawBottomIcons(canvas, elapsedSeconds);
        }
        if (showCopyRecommendation) {
            drawCopyRecommendation(canvas);
        }
    }

    /**
     * 底部三个图标：本局被 {@link TaizhouIconAnimationSelection} 抽中的那个播主骨骼动画，其余
     * 退回原版的未命中分支。
     *
     * <p>请财神和聚宝盆的未命中分支是静态位图（{@code GamePropView.lua:64-69}、
     * {@code JuBaoPenIconView.lua:11-15} 都是显隐一张 ImageView）；福利任务没有静态分支，
     * 未命中时改播同一骨架的 {@code animation2}（{@code LuckyMission/IconView.lua:12}）。
     */
    private void drawBottomIcons(Canvas canvas, float elapsedSeconds) {
        boolean effects = iconEffects != null && iconEffects.available();

        if (effects && iconAnimation.caishenAnimated()) {
            drawIconEffect(
                    canvas,
                    TaizhouWaitingIconEffects.CAISHEN,
                    TaizhouWaitingIconEffects.CAISHEN_ANIMATION,
                    elapsedSeconds,
                    TaizhouMahjongWaitingLayout.CAISHEN_BUTTON);
        } else {
            drawNode(canvas, inviteCaishen, TaizhouMahjongWaitingLayout.CAISHEN_BUTTON);
        }

        if (effects && iconAnimation.treasurePotAnimated()) {
            drawIconEffect(
                    canvas,
                    TaizhouWaitingIconEffects.TREASURE_POT,
                    TaizhouWaitingIconEffects.TREASURE_POT_ANIMATION,
                    elapsedSeconds,
                    TaizhouMahjongWaitingLayout.TREASURE_POT_BUTTON);
        } else {
            drawNode(canvas, treasurePot, TaizhouMahjongWaitingLayout.TREASURE_POT_BUTTON);
        }

        if (effects) {
            drawIconEffect(
                    canvas,
                    TaizhouWaitingIconEffects.LUCKY_MISSION,
                    iconAnimation.luckyMissionAnimation(),
                    elapsedSeconds,
                    TaizhouMahjongWaitingLayout.LUCKY_MISSION_BUTTON);
        } else {
            drawNode(canvas, luckyMission, TaizhouMahjongWaitingLayout.LUCKY_MISSION_BUTTON);
        }
    }

    private void drawIconEffect(
            Canvas canvas,
            String skeleton,
            String animation,
            float elapsedSeconds,
            TaizhouMahjongWaitingLayout.CenterButton node) {
        iconEffects.draw(
                canvas, skeleton, animation, elapsedSeconds, node.centerX, node.centerY, 1.0f);
    }

    /** 价格一律来自服务端定价，客户端不再内置数字。 */
    private String priceLabel(TaizhouRoomToolType type) {
        TaizhouRoomToolsState state = roomToolsState;
        return state == null ? "" : state.tool(type).priceLabel();
    }

    void setRoomToolsState(TaizhouRoomToolsState state) {
        roomToolsState = state;
    }

    private void drawRecord(Canvas canvas, boolean locked) {
        TaizhouMahjongWaitingLayout.CenterButton node =
                TaizhouMahjongWaitingLayout.RECORD_BUTTON;
        recordPaint.setColorFilter(locked ? lockedRecordFilter : null);
        canvas.drawBitmap(
                record,
                null,
                new RectF(node.left(), node.top(), node.right(), node.bottom()),
                recordPaint);
        recordPaint.setColorFilter(null);
    }

    private void drawCopyRecommendation(Canvas canvas) {
        TaizhouMahjongWaitingLayout.CenterButton node =
                TaizhouMahjongWaitingLayout.COPY_RECOMMENDATION;
        RectF destination = new RectF(node.left(), node.top(), node.right(), node.bottom());
        drawNineSlice(
                canvas,
                copyRecommendation,
                destination,
                COPY_CAP_LEFT,
                COPY_CAP_TOP,
                COPY_CAP_RIGHT,
                COPY_CAP_BOTTOM);
        drawOutlinedCenteredText(canvas, "【推荐使用】", node.centerX, 566.0f, 30.0f);
        drawOutlinedCenteredText(canvas, "入桌安全高效", node.centerX, 602.0f, 30.0f);
    }

    private void drawPropPrice(Canvas canvas, float centerX, float centerY, String cost) {
        drawCentered(canvas, priceBackground, centerX, centerY, 80.0f, 24.0f);
        drawCentered(canvas, roomCard, centerX - 25.0f, centerY, 39.0f, 22.2f);
        drawOutlinedCenteredText(canvas, cost, centerX - 2.0f, centerY, 24.0f);
    }

    private void drawOutlinedCenteredText(
            Canvas canvas, String text, float centerX, float centerY, float size) {
        textPaint.setTypeface(Typeface.create(typeface, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(size);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseline = centerY - (metrics.ascent + metrics.descent) * 0.5f;
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(Math.max(1.5f, size * 0.075f));
        textPaint.setColor(Color.rgb(151, 61, 24));
        canvas.drawText(text, centerX, baseline, textPaint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.rgb(255, 239, 177));
        canvas.drawText(text, centerX, baseline, textPaint);
    }

    private void drawNode(
            Canvas canvas,
            Bitmap bitmap,
            TaizhouMahjongWaitingLayout.CenterButton node) {
        drawCentered(canvas, bitmap, node.centerX, node.centerY, node.width, node.height);
    }

    private void drawCentered(
            Canvas canvas,
            Bitmap bitmap,
            float centerX,
            float centerY,
            float width,
            float height) {
        canvas.drawBitmap(
                bitmap,
                null,
                new RectF(
                        centerX - width * 0.5f,
                        centerY - height * 0.5f,
                        centerX + width * 0.5f,
                        centerY + height * 0.5f),
                bitmapPaint);
    }

    private void drawNineSlice(
            Canvas canvas,
            Bitmap bitmap,
            RectF destination,
            int left,
            int top,
            int right,
            int bottom) {
        int[] sourceX = {0, left, bitmap.getWidth() - right, bitmap.getWidth()};
        int[] sourceY = {0, top, bitmap.getHeight() - bottom, bitmap.getHeight()};
        float[] destinationX = {
            destination.left,
            destination.left + left,
            destination.right - right,
            destination.right
        };
        float[] destinationY = {
            destination.top,
            destination.top + top,
            destination.bottom - bottom,
            destination.bottom
        };
        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 3; row++) {
                canvas.drawBitmap(
                        bitmap,
                        new Rect(
                                sourceX[column],
                                sourceY[row],
                                sourceX[column + 1],
                                sourceY[row + 1]),
                        new RectF(
                                destinationX[column],
                                destinationY[row],
                                destinationX[column + 1],
                                destinationY[row + 1]),
                        bitmapPaint);
            }
        }
    }

    private static Bitmap bitmap(Context context, int resourceId) {
        return BitmapFactory.decodeResource(context.getResources(), resourceId);
    }
}
