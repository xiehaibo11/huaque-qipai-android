package com.nanbeiyule.game;

import android.annotation.SuppressLint;
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
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Interactive data layer for the final independent-asset game-home composition. */

@SuppressWarnings("deprecation")
abstract class GameHomeViewState extends AdaptiveCanvasView {
    public interface OnHomeActionListener {
        void onPersonalCenterRequested();

        void onMembershipCenterRequested();

        void onShopRequested();

        void onShopRequested(ShopCategory initialCategory);

        void onBagRequested();

        void onActivityCenterRequested();

        void onShareRequested();

        void onDailyMissionRequested();

        void onMailRequested();

        void onGameRecordsRequested();

        void onMoreRequested();

        void onSettingsRequested();

        void onChangeRegionRequested();

        void onLogoutRequested();

        void onEntryRequested(GameHomeState.Entry entry);

        void onUnavailableFeatureRequested(String featureName);

        void onLobbyStatusRequested(String status);

        void onRetryRequested();
    }

    protected enum HitKind {
        PERSONAL_CENTER,
        MEMBERSHIP_CENTER,
        SHOP,
        SHOP_DECORATION,
        SHOP_ROOM_CARD,
        SHOP_COIN,
        SHOP_DIAMOND,
        SHOP_INVENTORY,
        ACTIVITY_CENTER,
        SHARE,
        DAILY_MISSION,
        MAIL,
        GAME_RECORDS,
        MORE_MENU,
        PERSONAL_CENTER_SETTINGS,
        CHANGE_REGION,
        LOGOUT,
        ENTRY,
        LOBBY_STATUS,
        UNAVAILABLE
    }

    protected record HitTarget(
            String key,
            GameHomeV3Layout.Box bounds,
            HitKind kind,
            GameHomeState.Entry entry,
            String label) {}

    protected static final float PAGE_WIDTH = GameHomeViewportLayout.PAGE_WIDTH;
    protected static final float PAGE_HEIGHT = GameHomeViewportLayout.PAGE_HEIGHT;

    protected static final List<String> PRIMARY_CODES =
            List.of("CREATE_ROOM", "JOIN_ROOM", "MATCH");
    protected static final List<String> PRIMARY_LABELS =
            List.of("创建房间", "加入房间", "比赛场");
    /**
     * 承担「创建房间 / 返回房间」两态的主入口槽位序号。
     *
     * <p>原版是同一个按钮 {@code _KWA_BTND_CREATE_BACK_BOX_ROOM}
     * （{@code MainScene.csb} pos=(203,440) size=354x254），加入房间按钮
     * {@code _KWA_GOLD_BTND_JOIN_ROOM} 从不参与切换。
     */
    protected static final int CREATE_ROOM_SLOT = 0;

    /** 身上挂着未结束房间时该槽位的文案，对应原版 {@code lobby_title_back_box.png} 那一态。 */
    protected static final String BACK_ROOM_LABEL = "返回房间";

    protected static final List<String> GAME_CODES =
            List.of(
                    "TAIZHOU_MAHJONG",
                    "SHI_SAN_SHUI",
                    "WA_HUA");
    protected static final List<String> GAME_FALLBACK_LABELS =
            List.of(
                    "台州麻将",
                    "十三水",
                    "挖花");
    protected static final List<String> BOTTOM_CODES =
            List.of(
                    "DRESS_UP",
                    "RECORDS",
                    "ACTIVITIES",
                    "SHARE",
                    "BAG",
                    "MAIL",
                    "MORE");
    protected static final List<String> BOTTOM_LABELS =
            List.of("装扮", "战绩", "活动", "分享", "背包", "邮件", "更多");

    protected final GameHomeState state;
    protected final GameHomeV3Layout v3Layout = new GameHomeV3Layout();
    protected final GameHomeOriginalHeaderLayout headerLayout =
            new GameHomeOriginalHeaderLayout();
    protected final Map<String, GameHomeState.Entry> entriesByCode = new HashMap<>();
    protected final List<HitTarget> hitTargets = new ArrayList<>();
    protected final boolean drawBackgroundEnabled;

    protected Bitmap avatarBitmap;
    protected final Bitmap springBackgroundBitmap;
    protected final Bitmap originalHeaderBackgroundBitmap;
    protected final Bitmap copyButtonBitmap;
    protected final Bitmap locationIconBitmap;
    protected final Bitmap roomCardIconBitmap;
    protected final Bitmap coinIconBitmap;
    protected final Bitmap diamondIconBitmap;
    protected final Bitmap addIconBitmap;
    protected final Bitmap resourceTrackBitmap;
    protected final Bitmap mailAttentionBitmap;
    protected final AvatarFrameRenderer avatarFrameRenderer;
    protected final GameHomeFinalArtwork finalArtwork;

    protected final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    protected final Paint springBackgroundPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    protected final Paint titlePaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    protected final Paint valuePaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    protected final Paint interactionPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

    /**
     * 玩家是否还挂着未结束的房间。
     *
     * <p>原版 {@code LobbyView:showBackBoom()} 按 {@code position.gameID ~= 0} 切换同一入口的贴图
     * （{@code lobby/Modules/Lobby/View.lua:725}）；这里由 {@code GET /api/v1/rooms/current} 驱动
     * 同一语义。命中区与路由不变，仍走 {@code CREATE_ROOM}，由路由层按服务端位置决定建房还是返场。
     */
    private boolean inRoom;
    private boolean mailAttention;

    protected OnHomeActionListener actionListener;
    protected Runnable buttonClickSound = () -> {};
    protected String pressedTargetKey;
    /** @return true 表示这次调用真的改变了状态，需要重绘。 */
    public boolean setInRoom(boolean value) {
        if (inRoom == value) {
            return false;
        }
        inRoom = value;
        invalidate();
        return true;
    }

    protected boolean isInRoom() {
        return inRoom;
    }

    public void setMailAttention(boolean visible) {
        if (mailAttention != visible) {
            mailAttention = visible;
            invalidate();
        }
    }

    protected boolean hasMailAttention() {
        return mailAttention;
    }

    public GameHomeViewState(Context context, GameHomeState state) {
        this(context, state, true);
    }

    public GameHomeViewState(
            Context context,
            GameHomeState state,
            boolean drawBackgroundEnabled) {
        super(context);
        this.state = state;
        this.drawBackgroundEnabled = drawBackgroundEnabled;
        springBackgroundBitmap = loadBitmap(R.drawable.home_background_spring);
        originalHeaderBackgroundBitmap =
                loadBitmap(R.drawable.home_header_original_bg);
        copyButtonBitmap = loadBitmap(R.drawable.home_button_copy);
        // 归一化后的 100x100 定位图标，与房卡/金币/钻石保持同尺寸无变形。
        locationIconBitmap = loadBitmap(R.drawable.game_home_final_resource_location);
        roomCardIconBitmap = loadBitmap(R.drawable.home_icon_room_card);
        coinIconBitmap = loadBitmap(R.drawable.home_icon_coin);
        diamondIconBitmap = loadBitmap(R.drawable.home_icon_diamond);
        addIconBitmap = loadBitmap(R.drawable.home_icon_add);
        resourceTrackBitmap = loadBitmap(R.drawable.home_resource_track);
        mailAttentionBitmap = loadBitmap(R.drawable.img_mail_red);
        avatarFrameRenderer = new AvatarFrameRenderer(getResources());
        finalArtwork = new GameHomeFinalArtwork(context);
        avatarBitmap = AvatarFrameRenderer.loadDefaultAvatar(getResources());
        for (GameHomeState.Entry entry : state.entries()) {
            entriesByCode.put(entry.code(), entry);
        }
        configurePaints();
        setContentDescription(
                getResources().getString(
                        R.string.game_home_accessibility,
                        state.player().displayName(),
                        state.region().areaName()));
        setFocusable(true);
        setClickable(true);
    }

    public abstract void setOnHomeActionListener(OnHomeActionListener listener);

    public abstract void setButtonClickSound(Runnable buttonClickSound);

    public abstract void setAvatarBitmap(Bitmap bitmap);

    protected abstract void drawOriginalHeaderData(Canvas canvas);

    protected abstract void drawHeaderValue(
            Canvas canvas,
            String value,
            RectF bounds,
            float textSize,
            int color);

    protected abstract void drawOriginalHeaderBackdrop(Canvas canvas);

    protected abstract void drawTransparentHeaderTracks(Canvas canvas);

    protected abstract void drawTransparentHeaderAssets(Canvas canvas);

    protected abstract void drawTransparentBitmap(
            Canvas canvas,
            Bitmap bitmap,
            GameHomeV3Layout.Box destination);

    protected abstract void drawHorizontalScale9Bitmap(
            Canvas canvas,
            Bitmap bitmap,
            GameHomeV3Layout.Box destination,
            int leftCapPixels,
            int rightCapPixels);

    protected abstract void drawTopActions(Canvas canvas);

    protected abstract void drawSideActions(Canvas canvas);

    protected abstract void drawPrimaryEntries(Canvas canvas);

    protected abstract void drawGameGrid(Canvas canvas);

    protected abstract void drawStore(Canvas canvas);

    protected abstract void drawBottomNavigation(Canvas canvas);

    protected abstract void drawQuickStart(Canvas canvas);

    protected abstract void bindEntryAction(
            Canvas canvas,
            GameHomeV3Layout.Tile tile,
            String entryCode,
            String fallbackLabel);

    protected abstract void drawInteractionState(
            Canvas canvas,
            GameHomeV3Layout.Tile tile,
            boolean enabled);

    public abstract boolean onTouchEvent(MotionEvent event);

    protected abstract void dispatch(HitTarget target);

    protected abstract HitTarget findHit(float x, float y);

    protected abstract void configurePaints();

    protected abstract void drawFittedText(
            Canvas canvas,
            String value,
            float x,
            float baseline,
            float maximumWidth,
            Paint paint,
            float minimumSize);

    protected abstract void drawCenteredFittedText(
            Canvas canvas,
            String value,
            RectF bounds,
            Paint paint,
            float minimumSize);

    protected abstract void drawCenterCrop(
            Canvas canvas,
            Bitmap bitmap,
            RectF destination);

    protected abstract Bitmap loadBitmap(int resourceId);

    protected static RectF rect(GameHomeV3Layout.Box box) {
        return new RectF(box.left(), box.top(), box.right(), box.bottom());
    }

    protected static Rect sourceRect(GameHomeV3Layout.Box box) {
        return new Rect(
                Math.round(box.left()),
                Math.round(box.top()),
                Math.round(box.right()),
                Math.round(box.bottom()));
    }
}
