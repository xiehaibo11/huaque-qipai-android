package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.MahjongTileSprite;
import com.nanbeiyule.game.mahjong.OriginalMahjongTileDrawPlan;
import com.nanbeiyule.game.mahjong.OriginalMahjongTilePainter;
import com.nanbeiyule.game.mahjong.TaizhouCenterClockLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongSeatMapper;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableAtlas;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongVisibleRound;
import java.util.List;

/** Draws the original {@code TableClockLayer.csb} centre widget and wall count. */
final class TaizhouCenterClockRenderer {
    private static final String SURPLUS_FALLBACK_TEXT = "剩余:";
    private static final String COUNT_PREFIX = "x";
    private static final int OPENING_VISIBLE_WALL_COUNT = 136;
    private static final int WARNING_CLOCK_SECONDS = 2;

    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Bitmap background;
    private final Bitmap topDirection;
    private final Bitmap topLight;
    private final Bitmap topPanel;
    private final Bitmap topGlow;
    private final Bitmap bottomDirection;
    private final Bitmap bottomLight;
    private final Bitmap bottomPanel;
    private final Bitmap bottomGlow;
    private final Bitmap leftDirection;
    private final Bitmap leftLight;
    private final Bitmap leftPanel;
    private final Bitmap leftGlow;
    private final Bitmap rightDirection;
    private final Bitmap rightLight;
    private final Bitmap rightPanel;
    private final Bitmap rightGlow;
    private final Bitmap surplusLabel;
    private final OriginalMahjongTilePainter tilePainter;
    private final SxvipBitmapFont clockFont;
    private final SxvipBitmapFont warningClockFont;
    private final SxvipBitmapFont countFont;
    private long clockRevision = Long.MIN_VALUE;
    private int clockEventOrder = Integer.MIN_VALUE;
    private int clockActiveSeat;
    private Integer clockInitialSeconds;
    private long clockStartedElapsedMs;

    TaizhouCenterClockRenderer(Context context, OriginalMahjongTilePainter tilePainter) {
        Bitmap gameLayer = bitmap(context, R.drawable.taizhou_mahjong_game_layer);
        background = clockFrame(gameLayer, "mah_clock_bg.png");
        topDirection = clockFrame(gameLayer, "mah_clock_north_1.png");
        topLight = clockFrame(gameLayer, "mah_clock_north.png");
        topPanel = clockFrame(gameLayer, "mah_clock_north_2.png");
        topGlow = clockFrame(gameLayer, "mah_clock_north_3.png");
        bottomDirection = clockFrame(gameLayer, "mah_clock_south_1.png");
        bottomLight = clockFrame(gameLayer, "mah_clock_south.png");
        bottomPanel = clockFrame(gameLayer, "mah_clock_south_2.png");
        bottomGlow = clockFrame(gameLayer, "mah_clock_south_3.png");
        leftDirection = clockFrame(gameLayer, "mah_clock_west_1.png");
        leftLight = clockFrame(gameLayer, "mah_clock_west.png");
        leftPanel = clockFrame(gameLayer, "mah_clock_west_2.png");
        leftGlow = clockFrame(gameLayer, "mah_clock_west_3.png");
        rightDirection = clockFrame(gameLayer, "mah_clock_east_1.png");
        rightLight = clockFrame(gameLayer, "mah_clock_east.png");
        rightPanel = clockFrame(gameLayer, "mah_clock_east_2.png");
        rightGlow = clockFrame(gameLayer, "mah_clock_east_3.png");
        surplusLabel =
                extractTableInfoFrame(
                        bitmap(context, R.drawable.taizhou_mahjong_table_info),
                        "mah_img_surplus.png");
        this.tilePainter = tilePainter;
        clockFont =
                SxvipBitmapFont.loadRawResource(
                        context.getResources(),
                        R.raw.taizhou_mahjong_center_dis_fnt_maj,
                        R.drawable.taizhou_mahjong_center_dis_fnt_maj);
        warningClockFont =
                SxvipBitmapFont.loadRawResource(
                        context.getResources(),
                        R.raw.taizhou_mahjong_center_dis_fnt_maj2,
                        R.drawable.taizhou_mahjong_center_dis_fnt_maj2);
        countFont = clockFont;
    }

    boolean draw(
            Canvas canvas,
            GameplayTableState tableState,
            TaizhouMahjongVisibleRound visibleRound) {
        if (tableState == null || !visibleIn(tableState.phase())) {
            return false;
        }
        drawCentered(
                canvas,
                background,
                TaizhouCenterClockLayout.TABLE_BG.centerX(),
                TaizhouCenterClockLayout.TABLE_BG.centerY(),
                TaizhouCenterClockLayout.TABLE_BG.width(),
                TaizhouCenterClockLayout.TABLE_BG.height());
        int bankerLocalSeat = visibleRound == null ? 0 : visibleRound.bankerLocalSeat();
        drawDirections(canvas, activeLocalSeat(tableState), bankerLocalSeat);
        boolean clockNeedsTick = drawClock(canvas, tableState);
        drawSurplus(canvas, tableState.remainingWallCount(), shengPaiLift(tableState));
        return clockNeedsTick;
    }

    private boolean drawClock(Canvas canvas, GameplayTableState tableState) {
        Integer seconds = TaizhouCenterClockTimer.secondsFor(tableState);
        if (seconds == null) {
            clockInitialSeconds = null;
            return false;
        }
        int displaySeconds = displayClockSeconds(tableState, seconds);
        SxvipBitmapFont font =
                displaySeconds <= WARNING_CLOCK_SECONDS ? warningClockFont : clockFont;
        font.drawCentered(
                canvas,
                Integer.toString(displaySeconds),
                TaizhouCenterClockLayout.TABLE_BG.centerX(),
                TaizhouCenterClockLayout.TABLE_BG.centerY(),
                1.0f);
        return displaySeconds > 0;
    }

    private int displayClockSeconds(GameplayTableState tableState, int seconds) {
        long now = SystemClock.elapsedRealtime();
        int activeSeat = tableState.activeSeat() == null ? 0 : tableState.activeSeat();
        if (clockInitialSeconds == null
                || clockRevision != tableState.revision()
                || clockEventOrder != tableState.eventOrder()
                || clockActiveSeat != activeSeat
                || !clockInitialSeconds.equals(seconds)) {
            clockRevision = tableState.revision();
            clockEventOrder = tableState.eventOrder();
            clockActiveSeat = activeSeat;
            clockInitialSeconds = seconds;
            clockStartedElapsedMs = now;
        }
        long elapsedSeconds = Math.max(0L, (now - clockStartedElapsedMs) / 1000L);
        return (int) Math.max(0L, seconds - elapsedSeconds);
    }

    /**
     * 中心转向盘：按原版对 {@code _KW_PANAEL_CLOCK} 整块旋转，并按庄家换算高亮方位。
     *
     * <p>原版 {@code GameModule:rotateWindPos}（{@code BasicMahjong/.../GameLayer/Module.luac:870-875}）
     * 算出 {@code (4 - bankerLocalSeat - 1) * 90}，由
     * {@code TableClockView:onRotateWindSeat}（{@code TableClockView.luac:287-289}）执行
     * {@code _clockRoot:setRotation}。台州未覆写 {@code getClockWindSeatType()}，取基类默认
     * {@code rotateByBanker}（{@code Module.luac:399-401}）。
     *
     * <p>高亮索引来自 {@code TableClockView:getSeatByBanker}（{@code TableClockView.luac:202-211}）：
     * {@code (localBankerSeat + index + 3) % 4 + 1}，节点顺序为 SOUTH/WEST/NORTH/EAST
     * （{@code dirNodeName} 表，{@code TableClockView.luac:226}）。庄在 RIGHT 时角度为 0，
     * 该映射退化成固定的「上南/左西/下北/右东」——这正是此前被硬写死的那一种特例。
     *
     * <p>倒计时字 {@code _KW_FNT_CLOCK_TIME} 挂在 {@code _KW_IMG_TABLEBG} 之下、不在旋转面板内，
     * 因此数字始终正立；牌墙剩余同理。图集帧名与字形本身是错位的
     * （{@code mah_clock_north_1.png} 画的是「南」），所以只能整块旋转，不能改帧名绑定。
     */
    private void drawDirections(Canvas canvas, int activeLocalSeat, int bankerLocalSeat) {
        int saved = canvas.save();
        if (bankerLocalSeat >= 1 && bankerLocalSeat <= 4) {
            canvas.rotate(
                    (4 - bankerLocalSeat - 1) * 90.0f,
                    TaizhouCenterClockLayout.ROOT.centerX(),
                    TaizhouCenterClockLayout.ROOT.centerY());
        }
        drawRotatedDirections(canvas, activeLocalSeat, bankerLocalSeat);
        canvas.restoreToCount(saved);
    }

    /** 无庄家信息时退回原来的固定方位（等价于庄在 RIGHT、角度 0）。 */
    private static int litSeatFor(int bankerLocalSeat, int index, int fallback) {
        return bankerLocalSeat >= 1 && bankerLocalSeat <= 4
                ? (bankerLocalSeat + index + 3) % 4 + 1
                : fallback;
    }

    private void drawRotatedDirections(
            Canvas canvas, int activeLocalSeat, int bankerLocalSeat) {
        drawDirection(
                canvas,
                bottomDirection,
                bottomLight,
                bottomPanel,
                bottomGlow,
                TaizhouCenterClockLayout.NORTH,
                TaizhouCenterClockLayout.NORTH_LIGHT,
                activeLocalSeat
                        == litSeatFor(
                                bankerLocalSeat, 3, TaizhouMahjongTableLayout.SEAT_BOTTOM));
        drawDirection(
                canvas,
                topDirection,
                topLight,
                topPanel,
                topGlow,
                TaizhouCenterClockLayout.SOUTH,
                TaizhouCenterClockLayout.SOUTH_LIGHT,
                activeLocalSeat
                        == litSeatFor(
                                bankerLocalSeat, 1, TaizhouMahjongTableLayout.SEAT_TOP));
        drawDirection(
                canvas,
                leftDirection,
                leftLight,
                leftPanel,
                leftGlow,
                TaizhouCenterClockLayout.WEST,
                TaizhouCenterClockLayout.WEST_LIGHT,
                activeLocalSeat
                        == litSeatFor(
                                bankerLocalSeat, 2, TaizhouMahjongTableLayout.SEAT_LEFT));
        drawDirection(
                canvas,
                rightDirection,
                rightLight,
                rightPanel,
                rightGlow,
                TaizhouCenterClockLayout.EAST,
                TaizhouCenterClockLayout.EAST_LIGHT,
                activeLocalSeat
                        == litSeatFor(
                                bankerLocalSeat, 4, TaizhouMahjongTableLayout.SEAT_RIGHT));
    }

    private void drawDirection(
            Canvas canvas,
            Bitmap dim,
            Bitmap light,
            Bitmap panel,
            Bitmap glow,
            TaizhouCenterClockLayout.Node dimNode,
            TaizhouCenterClockLayout.Node lightNode,
            boolean active) {
        if (active) {
            drawCentered(canvas, panel, lightNode.centerX(), lightNode.centerY(),
                    lightNode.width(), lightNode.height());
            drawCentered(canvas, glow, lightNode.centerX(), lightNode.centerY(),
                    lightNode.width(), lightNode.height());
        }
        drawCentered(canvas, active ? light : dim, dimNode.centerX(), dimNode.centerY(),
                dimNode.width(), dimNode.height());
    }

    private int activeLocalSeat(GameplayTableState tableState) {
        Integer activeSeat = tableState.activeSeat();
        if (activeSeat == null) {
            return 0;
        }
        return TaizhouMahjongSeatMapper.toLocalSeat(
                activeSeat, tableState.mySeat(), tableState.chairCount());
    }

    /**
     * 生牌态把牌墙剩余整体上移，避开生牌块。
     *
     * <p>原版 {@code UIMahLayer:onEventShengPaiAni} 上移 40，{@code onEventClearTable} 复位；
     * 这里按同一语义按帧根据生牌可见性求值，等价于原版的置位/复位两个事件。
     */
    private static float shengPaiLift(GameplayTableState tableState) {
        TaizhouTableInfoState info = TaizhouTableInfoState.from(tableState);
        return info.shengPaiVisible()
                ? TaizhouCenterClockLayout.SHENG_PAI_SURPLUS_LIFT_COCOS_Y
                : 0.0f;
    }

    private void drawSurplus(Canvas canvas, int remainingWallCount, float liftCocosY) {
        if (remainingWallCount < 0) {
            return;
        }
        drawCentered(
                canvas,
                surplusLabel,
                TaizhouCenterClockLayout.SURPLUS_LABEL.centerX(),
                TaizhouCenterClockLayout.SURPLUS_LABEL.centerY() - liftCocosY,
                TaizhouCenterClockLayout.SURPLUS_LABEL.width(),
                TaizhouCenterClockLayout.SURPLUS_LABEL.height());
        if (surplusLabel == null || surplusLabel.isRecycled()) {
            drawFallbackLabel(canvas);
        }
        List<OriginalMahjongTileDrawPlan.Command> tile =
                OriginalMahjongTileDrawPlan.atAnchor(
                        MahjongTileSprite.LIE_DOWN_VERTICAL,
                        0,
                        TaizhouCenterClockLayout.SURPLUS_TILE_X,
                        TaizhouCenterClockLayout.SURPLUS_TILE_COCOS_Y + liftCocosY,
                        TaizhouCenterClockLayout.SURPLUS_TILE_SCALE,
                        0.5f,
                        1.0f);
        tilePainter.draw(canvas, tile);
        int displayCount = Math.min(remainingWallCount, OPENING_VISIBLE_WALL_COUNT);
        countFont.drawLeft(
                canvas,
                COUNT_PREFIX + displayCount,
                TaizhouCenterClockLayout.SURPLUS_COUNT.centerX(),
                TaizhouCenterClockLayout.SURPLUS_COUNT.centerY() - liftCocosY,
                0.9f);
    }

    private void drawFallbackLabel(Canvas canvas) {
        textPaint.setColor(Color.rgb(255, 231, 151));
        textPaint.setTextSize(28.0f);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setFakeBoldText(true);
        canvas.drawText(
                SURPLUS_FALLBACK_TEXT,
                TaizhouCenterClockLayout.REMAINING_LABEL_X,
                TaizhouCenterClockLayout.REMAINING_LABEL_BASELINE,
                textPaint);
        textPaint.setFakeBoldText(false);
    }

    private void drawCentered(
            Canvas canvas, Bitmap bitmap, float centerX, float centerY, float width, float height) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        canvas.drawBitmap(
                bitmap,
                null,
                new RectF(
                        centerX - width / 2.0f,
                        centerY - height / 2.0f,
                        centerX + width / 2.0f,
                        centerY + height / 2.0f),
                bitmapPaint);
    }

    private static boolean visibleIn(GameplayPhase phase) {
        return phase == GameplayPhase.DEALING
                || phase == GameplayPhase.PLAYING
                || phase == GameplayPhase.ROUND_RESULT;
    }

    private static Bitmap bitmap(Context context, int resourceId) {
        return BitmapFactory.decodeResource(context.getResources(), resourceId);
    }

    private static Bitmap clockFrame(Bitmap atlas, String frameName) {
        return TaizhouMahjongGameLayerBitmap.extract(atlas, frameName);
    }

    private static Bitmap extractTableInfoFrame(Bitmap atlas, String frameName) {
        if (atlas == null
                || atlas.isRecycled()
                || atlas.getWidth() != TaizhouMahjongTableAtlas.TABLE_INFO_WIDTH
                || atlas.getHeight() != TaizhouMahjongTableAtlas.TABLE_INFO_HEIGHT) {
            return null;
        }
        int index =
                TaizhouMahjongTableAtlas.indexOf(
                        TaizhouMahjongTableAtlas.TABLE_INFO_NAMES, frameName);
        if (index < 0) {
            return null;
        }
        int[] frame = TaizhouMahjongTableAtlas.TABLE_INFO_FRAMES[index];
        int storedWidth = frame[4] == 0 ? frame[2] : frame[3];
        int storedHeight = frame[4] == 0 ? frame[3] : frame[2];
        Bitmap stored =
                Bitmap.createBitmap(atlas, frame[0], frame[1], storedWidth, storedHeight);
        if (frame[4] == 0) {
            return stored;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(-90.0f);
        Bitmap upright =
                Bitmap.createBitmap(stored, 0, 0, storedWidth, storedHeight, matrix, true);
        if (upright != stored) {
            stored.recycle();
        }
        return upright;
    }
}
