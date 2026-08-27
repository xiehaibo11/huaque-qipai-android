package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.MotionEvent;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.mahjong.MahjongSettingData;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayGesture;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayInteraction;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayPermission;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongVisibleRound;
import com.nanbeiyule.game.mahjong.TaizhouMahjongWaitingLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongWaitingProjection;
import com.nanbeiyule.game.mahjong.TaizhouMultipleState;
import java.util.List;

/**
 * Native Taizhou Mahjong table surface.
 *
 * <p>The scene bitmap is the byte-identical background referenced by the
 * original {@code MahjongNew/GameLayer/CSB/Layer.csb}. Tiles and controls are
 * intentionally absent until they can be rendered from an authoritative
 * {@link GameplayTableState}; this view never manufactures a hand, river, wall,
 * or legal action.
 */
@SuppressLint("ViewConstructor")
public final class TaizhouMahjongTableView extends AdaptiveCanvasView {
    public interface OnPlayRequestedListener {
        void onPlayRequested(int originalHandIndex, int tileValue, String actionToken);
    }

    public interface OnChromeActionRequestedListener {
        void onChromeActionRequested(TaizhouMahjongWaitingProjection.Action action);
    }

    public interface OnVoiceGestureListener {
        void onVoiceGesture(TaizhouMahjongVoiceGesture.Result result);
    }

    private static final float DESIGN_WIDTH = TaizhouMahjongTableLayout.DESIGN_WIDTH;
    private static final float DESIGN_HEIGHT = TaizhouMahjongTableLayout.DESIGN_HEIGHT;

    private final TaizhouMahjongTableRenderers renderers;
    private Bitmap backgroundBitmap;
    private final Bitmap readyBitmap;
    private final Bitmap inviteBitmap;
    private final Bitmap startBitmap;
    private final Bitmap copyBitmap;
    private final TaizhouMahjongRoomInfoRenderer roomInfoRenderer;
    private final TaizhouMahjongPlayerRenderer playerRenderer;
    private final TaizhouMahjongWaitingChromeRenderer waitingChromeRenderer;
    private OnPlayerHeadTappedListener onPlayerHeadTapped;
    private final TaizhouIconAnimationSelection iconAnimation;
    private final TaizhouIconEffectsDriver iconEffectsDriver;
    private final SxvipRecordBadgeStore recordBadgeStore;
    private final TaizhouCenterClockRenderer centerClockRenderer;
    private final TaizhouMahjongDiscardRenderer discardRenderer;
    private final TaizhouMahjongHandRenderer handRenderer;
    private final TaizhouSettleRenderer settleRenderer;
    private final TaizhouTotalResultRenderer totalResultRenderer;
    private final TaizhouMultipleRenderer multipleRenderer;
    private final TaizhouEarlyStartRenderer earlyStartRenderer;
    private final TaizhouMahjongPlayInteraction playInteraction =
            new TaizhouMahjongPlayInteraction();
    private final TaizhouMahjongVoiceGesture voiceGesture = new TaizhouMahjongVoiceGesture();
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final TaizhouRoomMessageRenderer roomMessageRenderer;
    private final TaizhouRoundOverlayController roundOverlays;
    private final TaizhouTableInfoRenderer tableInfoRenderer;
    private final TaizhouCanHuRenderer canHuRenderer;
    private final TaizhouCanHuTracker canHuTracker = new TaizhouCanHuTracker();
    private final TaizhouVoiceLoadOverlayRenderer voiceLoadOverlayRenderer;
    private final TaizhouVoiceLoadProgress voiceLoadProgress = new TaizhouVoiceLoadProgress();
    private GameplayTableState tableState;
    private TaizhouRoomToolsState roomToolsState;
    private String visibleMessageId = "";
    private long visibleMessageUntil;
    private TaizhouMahjongVisibleRound visibleRound;
    private TaizhouMahjongPlayPermission playPermission;
    private TaizhouMahjongPreferences preferences = TaizhouMahjongPreferences.defaults();
    private Runnable onInviteRequested;
    private Runnable onReadyRequested;
    private Runnable onCopyRequested;
    private Runnable onEarlyStartRequested;
    private TaizhouTableRoundActionListener roundActionListener;
    private OnPlayRequestedListener onPlayRequested;
    private OnChromeActionRequestedListener onChromeActionRequested;
    private OnVoiceGestureListener onVoiceGesture;
    private final TaizhouTableTouchController touchController;
    private boolean recordAccessKnown;
    private boolean recordAccessGranted;

    public TaizhouMahjongTableView(Context context, String displayName) {
        super(context);
        setBackgroundColor(Color.rgb(3, 75, 63));
        setContentDescription(
                displayName == null || displayName.isBlank() ? "台州麻将" : displayName);
        renderers = new TaizhouMahjongTableRenderers(context);
        backgroundBitmap = renderers.background;
        readyBitmap = renderers.ready;
        inviteBitmap = renderers.invite;
        startBitmap = renderers.start;
        copyBitmap = renderers.copy;
        roomInfoRenderer = renderers.roomInfo;
        playerRenderer = renderers.player;
        iconAnimation = renderers.iconAnimation;
        iconEffectsDriver = new TaizhouIconEffectsDriver(renderers.iconEffects);
        waitingChromeRenderer = renderers.waitingChrome;
        recordBadgeStore = renderers.recordBadgeStore;
        centerClockRenderer = renderers.centerClock;
        discardRenderer = renderers.discard;
        handRenderer = renderers.hand;
        settleRenderer = renderers.settle;
        totalResultRenderer = renderers.totalResult;
        multipleRenderer = renderers.multiple;
        earlyStartRenderer = renderers.earlyStart;
        roomMessageRenderer = renderers.roomMessage;
        roundOverlays = renderers.roundOverlays;
        tableInfoRenderer = renderers.tableInfo;
        canHuRenderer = renderers.canHu;
        voiceLoadOverlayRenderer = renderers.voiceLoadOverlay;
        touchController =
                new TaizhouTableTouchController(
                        playInteraction,
                        voiceGesture,
                        roundOverlays,
                        canHuTracker,
                        new TaizhouTableTouchDispatch(this));
    }

    public void setOnReadyRequestedListener(Runnable listener) {
        onReadyRequested = listener;
    }

    public void setOnInviteRequestedListener(Runnable listener) {
        onInviteRequested = listener;
    }

    public void setOnCopyRequestedListener(Runnable listener) {
        onCopyRequested = listener;
    }

    /** 等待态「提前开局」按钮回调（TableInfo.csb 的 _KW_BTN_EARLY_START）。 */
    public void setOnEarlyStartRequestedListener(Runnable listener) {
        onEarlyStartRequested = listener;
    }

    /** 头像框点击回调（PlayerLayer.csb 的 HeadNode → PlayerInfoLayer.csb）。 */
    public void setOnPlayerHeadTappedListener(OnPlayerHeadTappedListener listener) {
        onPlayerHeadTapped = listener;
    }

    /** 头像框点击。 */
    public interface OnPlayerHeadTappedListener {
        void onPlayerHeadTapped(int seatNumber);
    }

    /** 结算页与加倍层的局级动作回调。 */
    public void setOnRoundActionRequestedListener(TaizhouTableRoundActionListener listener) {
        roundActionListener = listener;
    }

    public void setOnPlayRequestedListener(OnPlayRequestedListener listener) {
        onPlayRequested = listener;
    }

    public void setOnChromeActionRequestedListener(OnChromeActionRequestedListener listener) {
        onChromeActionRequested = listener;
    }

    public void setOnVoiceGestureListener(OnVoiceGestureListener listener) {
        onVoiceGesture = listener;
    }

    /** Wires the 吃碰杠胡 action commands dispatched by the action bar host. */
    public void setOnActionRequestedListener(TaizhouActionBarHost.Listener listener) {
        roundOverlays.setActionListener(listener);
    }

    void setRecordAccessGranted(boolean membershipAccessGranted) {
        recordAccessKnown = true;
        recordAccessGranted = membershipAccessGranted;
        invalidate();
    }

    List<String> missingAvatarKeys() {
        return tableState == null ? List.of() : playerRenderer.missingAvatarKeys(tableState);
    }

    void setAvatarBitmap(String avatarKey, Bitmap bitmap) {
        playerRenderer.setAvatarBitmap(avatarKey, bitmap);
        invalidate();
    }

    /** 生成与当前 BigWinLost 页面同源的 1920×1080 分享图，不截取系统栏或其他应用。 */
    Bitmap totalResultShareBitmap() {
        if (!touchController.totalResultInteraction().showing(tableState)) {
            return null;
        }
        Bitmap result = Bitmap.createBitmap(
                (int) DESIGN_WIDTH, (int) DESIGN_HEIGHT, Bitmap.Config.ARGB_8888);
        totalResultRenderer.draw(new Canvas(result), tableState);
        return result;
    }

    void setRoomToolsState(TaizhouRoomToolsState state) {
        roomToolsState = state;
        waitingChromeRenderer.setRoomToolsState(state);
        // 结算页「（消耗 fk xN）」按服务端权威定价刷新（WinLost/View.luac:498）。
        settleRenderer.setShuffleCost(
                state == null ? null : state.tool(TaizhouRoomToolType.SHUFFLE));
        if (state != null && !state.messages().isEmpty()) {
            TaizhouRoomToolsState.Message latest = state.messages().get(state.messages().size() - 1);
            if (!latest.messageId().equals(visibleMessageId)) {
                visibleMessageId = latest.messageId();
                visibleMessageUntil =
                        TaizhouRoomMessageLayout.visibleUntil(SystemClock.elapsedRealtime());
            }
        }
        invalidate();
    }

    /**
     * {@code onBtnSave} 之后整桌刷新：牌面外观走 {@link MahjongSettingData}（每张牌与命中
     * 测试都读它），桌布走 {@code TableBgRes}。
     */
    void applyStyle(TaizhouSettingStyle style) {
        if (style == null) {
            return;
        }
        MahjongSettingData.setAppearance(style.appearance());
        discardRenderer.setShowBigOutMah(style.value(TaizhouSettingStyle.Choice.OUT_STYLE) == 2);
        backgroundBitmap = renderers.tableBackground(
                style.value(TaizhouSettingStyle.Choice.TABLE_STYLE));
        invalidate();
    }

    void applyPreferences(TaizhouMahjongPreferences nextPreferences) {
        preferences = nextPreferences == null
                ? TaizhouMahjongPreferences.defaults()
                : nextPreferences;
        playInteraction.replace(
                visibleRound,
                effectivePermission(playPermission),
                TaizhouMahjongHandRenderer.renderedLocalMeldCount(tableState, visibleRound, false),
                preferences.playMode());
        canHuTracker.update(tableState, preferences, playInteraction);
        invalidate();
    }

    void startVoiceLoadProgress() {
        voiceLoadProgress.start();
        invalidate();
    }

    void setVoiceLoadProgress(int loaded, int total) {
        voiceLoadProgress.onProgress(loaded, total);
        invalidate();
    }

    public void render(GameplayTableState nextState) {
        render(
                nextState,
                nextState == null ? null : nextState.visibleRound().orElse(null),
                nextState == null ? null : nextState.playPermission().orElse(null));
    }

    /** Renders only a server-projected private round; callers must never synthesize this data. */
    public void render(
            GameplayTableState nextState, TaizhouMahjongVisibleRound nextVisibleRound) {
        render(nextState, nextVisibleRound, null);
    }

    /** Enables input only when the private round and one-use server permission arrive together. */
    public void render(
            GameplayTableState nextState,
            TaizhouMahjongVisibleRound nextVisibleRound,
            TaizhouMahjongPlayPermission nextPlayPermission) {
        if (nextVisibleRound != null
                && (nextState == null
                        || nextVisibleRound.chairCount() != nextState.chairCount()
                        || nextVisibleRound.mySeat() != nextState.mySeat())) {
            throw new IllegalArgumentException("visible round does not match gameplay state");
        }
        if (nextPlayPermission != null && nextVisibleRound == null) {
            throw new IllegalArgumentException("play permission requires a visible round");
        }
        tableState = nextState;
        visibleRound = nextVisibleRound;
        playPermission = nextPlayPermission;
        playInteraction.replace(
                nextVisibleRound,
                effectivePermission(nextPlayPermission),
                TaizhouMahjongHandRenderer.renderedLocalMeldCount(nextState, nextVisibleRound, false),
                preferences.playMode());
        touchController.reset(nextState);
        long nowElapsed = SystemClock.elapsedRealtime();
        discardRenderer.update(nextVisibleRound, nowElapsed);
        roundOverlays.update(nextState, nowElapsed);
        canHuTracker.update(tableState, preferences, playInteraction);
        if (nextState != null) {
            setContentDescription("台州麻将，房间" + nextState.roomNumber());
        }
        invalidate();
    }

    private TaizhouMahjongPlayPermission effectivePermission(TaizhouMahjongPlayPermission permission) {
        return permission == null ? null : permission.withMode(preferences.playMode());
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
        // State-backed seats, tiles, actions, and counters are added only when
        // their server events and original CSB/Lua evidence are both complete.
        if (tableState == null) {
            int save = AdaptiveCanvasDrawing.apply(canvas, viewport.designTransform());
            voiceLoadOverlayRenderer.draw(canvas, voiceLoadProgress);
            canvas.restoreToCount(save);
            return;
        }
        int save = AdaptiveCanvasDrawing.apply(canvas, viewport.designTransform());
        roomInfoRenderer.draw(canvas, tableState);
        playerRenderer.draw(canvas, tableState);
        if (touchController.totalResultInteraction().showing(tableState)) {
            totalResultRenderer.draw(canvas, tableState);
            voiceLoadOverlayRenderer.draw(canvas, voiceLoadProgress);
            canvas.restoreToCount(save);
            return;
        }
        // 结算页是模态整页；「查看牌桌」回看态时跳过本层，让牌桌正常绘制。
        if (tableState.phase() == GameplayPhase.ROUND_RESULT
                && tableState.settlement().isPresent()
                && !touchController.settleInteraction().reviewingTable()) {
            settleRenderer.draw(
                    canvas,
                    tableState.settlement().get(),
                    TaizhouMahjongVisibleRound.jokerTilesOf(visibleRound),
                    TaizhouSettleInteraction.hasVisibleTotalResult(tableState));
            waitingChromeRenderer.drawSettlementTopControls(
                    canvas, TaizhouMahjongWaitingProjection.showTrustButton(tableState));
            voiceLoadOverlayRenderer.draw(canvas, voiceLoadProgress);
            canvas.restoreToCount(save);
            postInvalidateDelayed(10_000L);
            return;
        }
        multipleRenderer.draw(canvas, tableState);
        long nowElapsed = SystemClock.elapsedRealtime();
        discardRenderer.draw(canvas, visibleRound, playInteraction, nowElapsed);
        handRenderer.draw(canvas, tableState, visibleRound, playInteraction);
        discardRenderer.drawShowOutMah(canvas, visibleRound, nowElapsed);
        boolean showTableActivityIcons = TaizhouMahjongWaitingProjection.showTableActivityIcons(tableState);
        waitingChromeRenderer.draw(
                canvas,
                TaizhouMahjongWaitingProjection.showInviteAndCopy(tableState),
                recordAccessKnown && !recordAccessGranted,
                recordAccessKnown && recordBadgeStore.shouldShow(recordAccessGranted),
                showTableActivityIcons,
                TaizhouMahjongWaitingProjection.showTrustButton(tableState),
                TaizhouMahjongWaitingProjection.showRuleButton(tableState),
                canHuTracker.tingButtonVisible(),
                iconEffectsDriver.elapsedSeconds());
        if (showTableActivityIcons) { iconEffectsDriver.scheduleFrame(this); }
        // 生牌信息层浮在牌面与等待桌控件之上（原版 TableInfoLayer 是独立 UI 层）。
        tableInfoRenderer.draw(canvas, tableState);
        boolean centerClockNeedsTick = centerClockRenderer.draw(canvas, tableState, visibleRound);
        if (centerClockNeedsTick) {
            postInvalidateDelayed(1_000L);
        }
        // 提前开局按钮同属 TableInfo.csb，画在等待桌控件之上、房间消息之下。
        earlyStartRenderer.draw(canvas, tableState);
        roundOverlays.draw(canvas, tableState, visibleRound, nowElapsed);
        long frameDelay =
                Math.max(
                        discardRenderer.nextRepaintDelayMillis(nowElapsed),
                        roundOverlays.nextRepaintDelayMillis(tableState, nowElapsed));
        if (frameDelay > 0L) {
            postInvalidateDelayed(frameDelay);
        }
        drawRoomMessage(canvas);
        // 听牌可胡提示是 WINDOW 级弹层（原版 CanHuMahsUI:showSelf），画在最上。
        canHuRenderer.draw(canvas, canHuTracker.current(), TaizhouMahjongVisibleRound.jokerTilesOf(visibleRound));
        for (TaizhouMahjongWaitingLayout.ReadyIndicator indicator :
                TaizhouMahjongWaitingProjection.readyIndicators(tableState)) {
            canvas.drawBitmap(
                    readyBitmap,
                    null,
                    new RectF(
                            indicator.centerX - indicator.width / 2.0f,
                            indicator.centerY - indicator.height / 2.0f,
                            indicator.centerX + indicator.width / 2.0f,
                            indicator.centerY + indicator.height / 2.0f),
                    bitmapPaint);
        }
        if (TaizhouMahjongWaitingProjection.showInviteAndCopy(tableState)) {
            drawCenterButton(
                    canvas, inviteBitmap, TaizhouMahjongWaitingLayout.INVITE_BUTTON);
            drawCenterButton(canvas, copyBitmap, TaizhouMahjongWaitingLayout.COPY_BUTTON);
        }
        if (TaizhouMahjongWaitingProjection.showStartButton(tableState)) {
            drawCenterButton(canvas, startBitmap, TaizhouMahjongWaitingLayout.START_BUTTON);
        }
        voiceLoadOverlayRenderer.draw(canvas, voiceLoadProgress);
        canvas.restoreToCount(save);
        postInvalidateDelayed(10_000L);
    }

    private void drawCenterButton(Canvas canvas, Bitmap bitmap, TaizhouMahjongWaitingLayout.CenterButton button) {
        canvas.drawBitmap(
                bitmap,
                null,
                new RectF(button.left(), button.top(), button.right(), button.bottom()),
                bitmapPaint);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        iconEffectsDriver.onWindowVisibilityChanged(visibility);
    }

    @Override
    protected void onDetachedFromWindow() {
        discardRenderer.release();
        roundOverlays.release();
        iconEffectsDriver.release();
        super.onDetachedFromWindow();
    }

    private void drawRoomMessage(Canvas canvas) {
        TaizhouRoomToolsState state = roomToolsState;
        if (state == null
                || state.messages().isEmpty()
                || SystemClock.elapsedRealtime() >= visibleMessageUntil) {
            return;
        }
        TaizhouRoomToolsState.Message message = state.messages().get(state.messages().size() - 1);
        roomMessageRenderer.draw(canvas, message, tableState);
        postInvalidateDelayed(Math.max(50L, visibleMessageUntil - SystemClock.elapsedRealtime()));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (tableState == null || getWidth() <= 0 || getHeight() <= 0) {
            return false;
        }
        AdaptiveViewport.Transform transform =
                adaptiveViewport(DESIGN_WIDTH, DESIGN_HEIGHT).designTransform();
        float designX = transform.unmapX(event.getX());
        float designY = transform.unmapY(event.getY());
        float cocosY = DESIGN_HEIGHT - designY;
        return switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN ->
                    touchController.onDown(tableState, designX, designY, cocosY);
            case MotionEvent.ACTION_MOVE ->
                    touchController.onMove(designX, designY, cocosY);
            case MotionEvent.ACTION_UP ->
                    touchController.onUp(tableState, designX, designY, cocosY);
            case MotionEvent.ACTION_CANCEL ->
                    touchController.onCancel(designX, designY, cocosY);
            default -> touchController.onOther();
        };
    }

    void releasePlayPermission(String actionToken) {
        playInteraction.releasePlayPermission(actionToken);
        invalidate();
    }

    void applyPlayResult(TaizhouMahjongPlayGesture.Result result) {
        if (result == null) {
            return;
        }
        invalidate();
        if (result.playIntent != null) {
            canHuTracker.onSelfDiscardRequested(result.playIntent.tileValue);
            if (onPlayRequested != null) {
                onPlayRequested.onPlayRequested(
                        result.playIntent.tileIndex, result.playIntent.tileValue, result.playIntent.actionToken);
            }
        }
    }

    void dispatchVoiceGesture(TaizhouMahjongVoiceGesture.Result result) {
        if (result != null && onVoiceGesture != null) {
            onVoiceGesture.onVoiceGesture(result);
        }
    }

    void dispatchWaitingAction(TaizhouMahjongWaitingProjection.Action action) {
        if (action == TaizhouMahjongWaitingProjection.Action.TING) {
            canHuTracker.onTingButtonClicked();
            invalidate();
            return;
        }
        if (action == TaizhouMahjongWaitingProjection.Action.RECORD
                && recordAccessKnown
                && !recordAccessGranted) {
            recordBadgeStore.markSeen();
            invalidate();
        }
        Runnable listener = null;
        if (action == TaizhouMahjongWaitingProjection.Action.INVITE) {
            listener = onInviteRequested;
        } else if (action == TaizhouMahjongWaitingProjection.Action.READY) {
            listener = onReadyRequested;
        } else if (action == TaizhouMahjongWaitingProjection.Action.COPY) {
            listener = onCopyRequested;
        }
        if (listener != null) {
            listener.run();
        } else if (action != TaizhouMahjongWaitingProjection.Action.NONE
                && onChromeActionRequested != null) {
            onChromeActionRequested.onChromeActionRequested(action);
        }
    }

    void dispatchSettleAction(TaizhouSettleInteraction.Action action) {
        if (action == TaizhouSettleInteraction.Action.TOTAL_RESULT) {
            if (touchController.totalResultInteraction().enter(tableState)) invalidate();
        } else if (action == TaizhouSettleInteraction.Action.CHECK_TABLE) {
            touchController.settleInteraction().enterReview();
            invalidate();
        } else if (roundActionListener != null) {
            roundActionListener.onSettleActionRequested(action);
        }
    }

    void dispatchTotalResultAction(TaizhouTotalResultInteraction.Action action) {
        if (roundActionListener != null) roundActionListener.onTotalResultActionRequested(action);
    }

    void dispatchEarlyStart() {
        if (onEarlyStartRequested != null) onEarlyStartRequested.run();
    }

    void dispatchPlayerHeadTapped(int seatNumber) {
        if (onPlayerHeadTapped != null) onPlayerHeadTapped.onPlayerHeadTapped(seatNumber);
    }

    void dispatchMultipleChoice(TaizhouMultipleState.Choice choice) {
        if (roundActionListener != null) roundActionListener.onMultipleChoiceRequested(choice);
    }
    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

}
