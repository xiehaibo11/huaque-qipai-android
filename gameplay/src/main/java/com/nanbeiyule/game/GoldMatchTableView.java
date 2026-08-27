package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.BatteryManager;
import com.nanbeiyule.game.goldroom.GoldMatchLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongRoomInfoLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongWaitingLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Gold-room dispatch-queue waiting surface.
 *
 * <p>This is intentionally only the original MatchUI waiting overlay on the original 台州麻将 table
 * background. It never renders a wall, hand, discard, legal action, settlement, or START_ROUND
 * transition without authoritative gameplay events.
 */
@SuppressLint("ViewConstructor")
final class GoldMatchTableView extends AdaptiveCanvasView {
    private static final float DESIGN_WIDTH = GoldMatchLayout.DESIGN_WIDTH;
    private static final float DESIGN_HEIGHT = GoldMatchLayout.DESIGN_HEIGHT;
    private static final float TRUST_CENTER_X = 1698.04f;
    private static final float TRUST_CENTER_Y = 60.0f;
    private static final float TRUST_WIDTH = 101.0f;
    private static final float TRUST_HEIGHT = 101.0f;
    private static final float MATCH_TEXT_CENTER_X = GoldMatchLayout.MATCH_ANI_CENTER_X;
    private static final float MATCH_TEXT_CENTER_Y = GoldMatchLayout.MATCH_ANI_CENTER_Y;
    private static final float MATCH_TEXT_SIZE = 48.0f;
    private static final int MATCH_TEXT_FILL_COLOR = 0xffffe7a7;
    private static final int MATCH_TEXT_STROKE_COLOR = 0xff704c23;
    private static final int PLAYER_TEXT_COLOR = 0xffffe071;
    private static final float AVATAR_CONTENT_WIDTH = 98.0f;
    private static final float AVATAR_CONTENT_HEIGHT = 99.0f;
    private static final float AVATAR_CENTER_OFFSET_X = -0.5f;
    private static final float AVATAR_CENTER_OFFSET_Y = 0.3f;

    private final Bitmap backgroundBitmap;
    private final Bitmap defaultAvatar;
    private final Bitmap headFrameBitmap;
    private final Bitmap goldBitmap;
    private final Bitmap systemBackground;
    private final Bitmap wifi;
    private final Bitmap powerBackground;
    private final Bitmap powerBar;
    private final Bitmap roomBackground;
    private final Bitmap healthGame;
    private final Bitmap menu;
    private final Bitmap trust;
    private final Bitmap ting;
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint batteryPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Typeface typeface;
    private final BatteryManager batteryManager;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
    private final String nickname;
    private final long coins;
    private Bitmap avatarBitmap;

    GoldMatchTableView(
            Context context,
            GameHomeState.Player player,
            GameHomeState.Wallet wallet,
            GoldRoomJoinResponse response,
            Bitmap currentAvatarBitmap) {
        super(context);
        String displayName = player == null ? null : player.displayName();
        this.nickname = displayName == null || displayName.isBlank() ? "玩家" : displayName;
        this.coins = wallet == null ? 0L : wallet.coins();
        setBackgroundColor(Color.rgb(3, 75, 63));
        setContentDescription("台州麻将金币场正在匹配");
        backgroundBitmap =
                BitmapFactory.decodeResource(
                        getResources(), R.drawable.taizhou_mahjong_scene_background);
        Bitmap gameLayerAtlas =
                BitmapFactory.decodeResource(getResources(), R.drawable.taizhou_mahjong_game_layer);
        Bitmap commonAtlas =
                BitmapFactory.decodeResource(
                        getResources(), R.drawable.taizhou_mahjong_common_game_layer);
        defaultAvatar = AvatarFrameRenderer.loadDefaultAvatar(getResources());
        avatarBitmap = usable(currentAvatarBitmap) ? currentAvatarBitmap : defaultAvatar;
        headFrameBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gold_match_img_head);
        goldBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gold_match_img_gold);
        systemBackground = extract(commonAtlas, TaizhouMahjongRoomInfoLayout.SYSTEM_BACKGROUND);
        wifi = extract(commonAtlas, TaizhouMahjongRoomInfoLayout.WIFI);
        powerBackground = extract(commonAtlas, TaizhouMahjongRoomInfoLayout.POWER_BACKGROUND);
        powerBar = extract(commonAtlas, TaizhouMahjongRoomInfoLayout.POWER_BAR);
        roomBackground = extract(commonAtlas, TaizhouMahjongRoomInfoLayout.ROOM_BACKGROUND);
        healthGame =
                TaizhouMahjongGameLayerBitmap.extract(
                        gameLayerAtlas, TaizhouMahjongRoomInfoLayout.HEALTH_GAME.frameName);
        menu =
                TaizhouMahjongGameLayerBitmap.extract(
                        gameLayerAtlas, TaizhouMahjongWaitingLayout.MENU_BUTTON.frameName);
        trust =
                BitmapFactory.decodeResource(
                        getResources(), R.drawable.taizhou_mahjong_game_layer2_head_trust);
        ting = TaizhouMahjongGameLayerBitmap.extract(gameLayerAtlas, "mah_ting_btn.png");
        batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/fangzhengcuyuan.ttf");
        textPaint.setTypeface(typeface);
    }

    void setAvatarBitmap(Bitmap bitmap) {
        if (!usable(bitmap)) {
            return;
        }
        avatarBitmap = bitmap;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        AdaptiveViewport viewport = adaptiveViewport(DESIGN_WIDTH, DESIGN_HEIGHT);
        AdaptiveCanvasDrawing.drawTransformedBitmap(
                canvas,
                backgroundBitmap,
                bitmapPaint,
                viewport.designTransform(),
                viewport.viewportWidth(),
                viewport.viewportHeight(),
                DESIGN_WIDTH,
                DESIGN_HEIGHT);
        int save = AdaptiveCanvasDrawing.apply(canvas, viewport.designTransform());
        drawFixedChrome(canvas);
        drawMatchText(canvas);
        drawContent(canvas);
        canvas.restoreToCount(save);
        postInvalidateDelayed(30_000L);
    }

    private void drawFixedChrome(Canvas canvas) {
        drawSprite(canvas, systemBackground, TaizhouMahjongRoomInfoLayout.SYSTEM_BACKGROUND);
        drawCenteredText(
                canvas,
                timeFormat.format(new Date()),
                TaizhouMahjongRoomInfoLayout.TIME_CENTER_X,
                TaizhouMahjongRoomInfoLayout.TIME_CENTER_Y,
                TaizhouMahjongRoomInfoLayout.LEFT_TEXT_SIZE,
                TaizhouMahjongRoomInfoLayout.LEFT_TEXT_COLOR,
                Paint.Align.CENTER);
        drawSprite(canvas, wifi, TaizhouMahjongRoomInfoLayout.WIFI);
        drawSprite(canvas, powerBackground, TaizhouMahjongRoomInfoLayout.POWER_BACKGROUND);
        drawBattery(canvas, currentBatteryPercent());
        drawSprite(canvas, healthGame, TaizhouMahjongRoomInfoLayout.HEALTH_GAME);
        drawScoreChrome(canvas);
        drawCentered(canvas, trust, TRUST_CENTER_X, TRUST_CENTER_Y, TRUST_WIDTH, TRUST_HEIGHT);
        drawNode(canvas, menu, TaizhouMahjongWaitingLayout.MENU_BUTTON);
        drawNode(canvas, ting, TaizhouMahjongWaitingLayout.TING_BUTTON);
    }

    /**
     * 金币场房间信息层：{@code 底    分} 与 {@code 倍    数} 两行。
     *
     * <p>原版 {@code RoomInfoView:getInfoNodeConfig}
     * （{@code BasicMahjong/Modules/RoomInfo/View.luac:19-29}）按 {@code CF.roomData:isGoldRoom()}
     * 分支：金币场是 {@code baseScore("底    分")} + {@code addMultiple("倍    数")}，房卡场才是
     * {@code roomID("房间号")} + {@code playCount("局    数")}。进场/清桌时
     * {@code RoomInfoView:onClearTable}（同文件 {@code :31-36}）在金币场把两行置为
     * {@code baseScore = "--"}、{@code addMultiple = "x1"}——与原版匹配态实机截图逐字一致。
     *
     * <p>行几何来自基类 {@code GameBase/Modules/RoomInfo/View.lua:303-321}：
     * {@code NODE_HEIGHT=35}、{@code NODE_DIS=8}，底图高 {@code 43n+18}，第 i 行
     * {@code cocosY=(n-i+0.5)*35+(n-i+1)*8+5}。n=2 时两行 cocosY 为 73.5 / 30.5，换算到本仓库的
     * Android 顶左坐标即 {@link TaizhouMahjongRoomInfoLayout#ROOM_ROW_CENTER_Y}=95.5 与
     * {@link TaizhouMahjongRoomInfoLayout#REMAINING_ROW_CENTER_Y}=138.5，与既有底图高 104 自洽。
     */
    private void drawScoreChrome(Canvas canvas) {
        drawSprite(canvas, roomBackground, TaizhouMahjongRoomInfoLayout.ROOM_BACKGROUND);
        drawInfoRow(canvas, "底    分", "--", TaizhouMahjongRoomInfoLayout.ROOM_ROW_CENTER_Y);
        drawInfoRow(canvas, "倍    数", "x1", TaizhouMahjongRoomInfoLayout.REMAINING_ROW_CENTER_Y);
    }

    /** 一行 {@code 键 : 值}，键右对齐、冒号居中、值左对齐（原版 createOneInfoNode 的三段式）。 */
    private void drawInfoRow(Canvas canvas, String key, String value, float centerY) {
        drawCenteredText(
                canvas,
                key,
                TaizhouMahjongRoomInfoLayout.KEY_RIGHT_X,
                centerY,
                TaizhouMahjongRoomInfoLayout.LEFT_TEXT_SIZE,
                TaizhouMahjongRoomInfoLayout.LEFT_TEXT_COLOR,
                Paint.Align.RIGHT);
        drawCenteredText(
                canvas,
                ":",
                TaizhouMahjongRoomInfoLayout.COLON_CENTER_X,
                centerY,
                TaizhouMahjongRoomInfoLayout.LEFT_TEXT_SIZE,
                TaizhouMahjongRoomInfoLayout.LEFT_TEXT_COLOR,
                Paint.Align.CENTER);
        drawCenteredText(
                canvas,
                value,
                TaizhouMahjongRoomInfoLayout.VALUE_LEFT_X,
                centerY,
                TaizhouMahjongRoomInfoLayout.LEFT_TEXT_SIZE,
                TaizhouMahjongRoomInfoLayout.LEFT_TEXT_COLOR,
                Paint.Align.LEFT);
    }

    private void drawMatchText(Canvas canvas) {
        drawOutlinedCenteredText(
                canvas,
                "正在为您寻找合适的牌友...",
                MATCH_TEXT_CENTER_X,
                MATCH_TEXT_CENTER_Y,
                MATCH_TEXT_SIZE,
                MATCH_TEXT_FILL_COLOR,
                MATCH_TEXT_STROKE_COLOR);
    }

    private void drawContent(Canvas canvas) {
        float cx = GoldMatchLayout.CONTENT_CENTER_X;
        float cy = GoldMatchLayout.androidY(GoldMatchLayout.CONTENT_CENTER_Y);
        RectF headRect =
                new RectF(
                        cx - GoldMatchLayout.HEAD_FRAME_WIDTH / 2.0f,
                        cy - GoldMatchLayout.HEAD_FRAME_HEIGHT / 2.0f,
                        cx + GoldMatchLayout.HEAD_FRAME_WIDTH / 2.0f,
                        cy + GoldMatchLayout.HEAD_FRAME_HEIGHT / 2.0f);
        canvas.drawBitmap(headFrameBitmap, null, headRect, bitmapPaint);
        Bitmap avatar = usable(avatarBitmap) ? avatarBitmap : defaultAvatar;
        RectF avatarRect =
                new RectF(
                        headRect.centerX() + AVATAR_CENTER_OFFSET_X - AVATAR_CONTENT_WIDTH / 2.0f,
                        headRect.centerY() + AVATAR_CENTER_OFFSET_Y - AVATAR_CONTENT_HEIGHT / 2.0f,
                        headRect.centerX() + AVATAR_CENTER_OFFSET_X + AVATAR_CONTENT_WIDTH / 2.0f,
                        headRect.centerY() + AVATAR_CENTER_OFFSET_Y + AVATAR_CONTENT_HEIGHT / 2.0f);
        AvatarBitmapRenderer.drawCenterCrop(canvas, avatar, avatarRect, bitmapPaint);

        // MatchUI.lua only shows _KW_NAME_AND_SCORE_BG when a table-skin prop is active.
        // This matching state has no such prop, so the original default is no black backing panel.
        drawCenteredText(
                canvas,
                nickname,
                cx,
                cy + 77.0f,
                fittedSize(nickname, 104.0f, 30.0f),
                PLAYER_TEXT_COLOR,
                Paint.Align.CENTER);
        drawCentered(canvas, goldBitmap, cx - 39.448f, cy + 115.0f, 34.5f, 40.5f);
        drawCenteredText(
                canvas,
                String.valueOf(coins),
                cx - 15.0f,
                cy + 110.0f,
                30.0f,
                PLAYER_TEXT_COLOR,
                Paint.Align.LEFT);
    }

    private void drawBattery(Canvas canvas, int percentage) {
        if (percentage < 0) {
            return;
        }
        TaizhouMahjongRoomInfoLayout.Sprite sprite = TaizhouMahjongRoomInfoLayout.POWER_BAR;
        RectF destination = bounds(sprite);
        float visibleRight = destination.left + destination.width() * percentage / 100.0f;
        int save = canvas.save();
        canvas.clipRect(destination.left, destination.top, visibleRight, destination.bottom);
        batteryPaint.setColorFilter(
                new PorterDuffColorFilter(
                        TaizhouMahjongRoomInfoRenderer.batteryColor(percentage),
                        PorterDuff.Mode.MULTIPLY));
        canvas.drawBitmap(powerBar, null, destination, batteryPaint);
        batteryPaint.setColorFilter(null);
        canvas.restoreToCount(save);
    }

    private int currentBatteryPercent() {
        if (batteryManager == null) {
            return -1;
        }
        int value = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        return value < 0 ? -1 : Math.min(100, value);
    }

    private void drawOutlinedCenteredText(
            Canvas canvas,
            String text,
            float centerX,
            float centerY,
            float size,
            int fillColor,
            int strokeColor) {
        textPaint.setTypeface(Typeface.create(typeface, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(size);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseline = centerY - (metrics.ascent + metrics.descent) * 0.5f;
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(Math.max(2.0f, size * 0.075f));
        textPaint.setColor(strokeColor);
        canvas.drawText(text, centerX, baseline, textPaint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(fillColor);
        canvas.drawText(text, centerX, baseline, textPaint);
    }

    private void drawCenteredText(
            Canvas canvas,
            String value,
            float x,
            float centerY,
            float size,
            int color,
            Paint.Align align) {
        textPaint.setTypeface(typeface);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(align);
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(value, x, centerY - (metrics.ascent + metrics.descent) * 0.5f, textPaint);
        textPaint.setFakeBoldText(false);
    }

    private float fittedSize(String text, float maxWidth, float originalSize) {
        textPaint.setTypeface(typeface);
        textPaint.setTextSize(originalSize);
        float measuredWidth = textPaint.measureText(text);
        if (measuredWidth <= maxWidth || measuredWidth <= 0.0f) {
            return originalSize;
        }
        return originalSize * maxWidth / measuredWidth;
    }

    private void drawSprite(
            Canvas canvas, Bitmap bitmap, TaizhouMahjongRoomInfoLayout.Sprite sprite) {
        drawCentered(canvas, bitmap, sprite.centerX, sprite.centerY, sprite.width, sprite.height);
    }

    private void drawNode(
            Canvas canvas, Bitmap bitmap, TaizhouMahjongWaitingLayout.CenterButton node) {
        drawCentered(canvas, bitmap, node.centerX, node.centerY, node.width, node.height);
    }

    private void drawCentered(
            Canvas canvas,
            Bitmap bitmap,
            float centerX,
            float centerY,
            float width,
            float height) {
        if (bitmap == null) {
            return;
        }
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

    private static Bitmap extract(
            Bitmap atlas, TaizhouMahjongRoomInfoLayout.Sprite sprite) {
        return TaizhouMahjongCommonBitmap.extract(atlas, sprite.frameName);
    }

    private static RectF bounds(TaizhouMahjongRoomInfoLayout.Sprite sprite) {
        return new RectF(
                sprite.centerX - sprite.width / 2.0f,
                sprite.centerY - sprite.height / 2.0f,
                sprite.centerX + sprite.width / 2.0f,
                sprite.centerY + sprite.height / 2.0f);
    }

    private static boolean usable(Bitmap bitmap) {
        return bitmap != null && !bitmap.isRecycled();
    }
}
