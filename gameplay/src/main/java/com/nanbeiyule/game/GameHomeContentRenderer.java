package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import java.util.List;

/** Independent asset rendering and hit-target registration for the final lobby. */

abstract class GameHomeContentRenderer extends GameHomeHeaderRenderer {
    /** 原版气泡渲染器；按服务端下发的 bubbleText 播放，没有配置就什么都不画。 */
    private LobbyBubbleRenderer bubbleRenderer;

    private long bubbleClockStartNanos;

    protected GameHomeContentRenderer(Context context, GameHomeState state) {
        super(context, state);
    }

    protected GameHomeContentRenderer(
            Context context,
            GameHomeState state,
            boolean drawBackgroundEnabled) {
        super(context, state, drawBackgroundEnabled);
    }

    protected void drawTopActions(Canvas canvas) {
        List<String> codes =
                List.of(
                        "MEMBERSHIP_CENTER",
                        "WELFARE_TASK",
                        "MAIL",
                        "CUSTOMER_SERVICE",
                        "SETTINGS");
        List<String> labels = List.of("会员", "福利任务", "邮件", "客服", "设置");
        for (int index = 0; index < v3Layout.topActions().size(); index++) {
            GameHomeV3Layout.Tile tile = v3Layout.topActions().get(index);
            drawTileBitmap(canvas, finalArtwork.topActions.get(index), tile);
            if ("MEMBERSHIP_CENTER".equals(codes.get(index))) {
                hitTargets.add(
                        new HitTarget(
                                tile.key(),
                                tile.hit(),
                                HitKind.MEMBERSHIP_CENTER,
                                null,
                                labels.get(index)));
            } else if ("WELFARE_TASK".equals(codes.get(index))) {
                bindDailyMissionAction(canvas, tile);
            } else if ("MAIL".equals(codes.get(index))) {
                bindMailAction(canvas, tile);
            } else {
                bindEntryAction(canvas, tile, codes.get(index), labels.get(index));
            }
        }
    }

    private void bindDailyMissionAction(Canvas canvas, GameHomeV3Layout.Tile tile) {
        drawInteractionState(canvas, tile, true);
        hitTargets.add(
                new HitTarget(
                        tile.key(),
                        tile.hit(),
                        HitKind.DAILY_MISSION,
                        null,
                        "每日任务"));
    }

    private void bindMailAction(Canvas canvas, GameHomeV3Layout.Tile tile) {
        drawInteractionState(canvas, tile, true);
        drawMailAttention(canvas, tile);
        hitTargets.add(
                new HitTarget(
                        tile.key(),
                        tile.hit(),
                        HitKind.MAIL,
                        null,
                        "邮件"));
    }

    protected void drawSideActions(Canvas canvas) {
        List<String> labels = List.of("福利任务", "幸运宝箱", "签到有礼");
        for (int index = 0; index < v3Layout.sideActions().size(); index++) {
            GameHomeV3Layout.Tile tile = v3Layout.sideActions().get(index);
            drawTransparentBitmap(canvas, finalArtwork.sideActions.get(index), tile.destination());
            drawInteractionState(canvas, tile, true);
            switch (tile.key()) {
                case "SIDE_WELFARE" ->
                        hitTargets.add(
                                new HitTarget(
                                        tile.key(),
                                        tile.hit(),
                                        HitKind.DAILY_MISSION,
                                        null,
                                        labels.get(index)));
                case "SIDE_CHEST" ->
                        hitTargets.add(
                                new HitTarget(
                                        tile.key(),
                                        tile.hit(),
                                        HitKind.UNAVAILABLE,
                                        null,
                                        labels.get(index)));
                case "SIDE_CHECKIN" ->
                        bindEntryAction(canvas, tile, "ACTIVITY", labels.get(index));
                default ->
                        hitTargets.add(
                                new HitTarget(
                                        tile.key(),
                                        tile.hit(),
                                        HitKind.UNAVAILABLE,
                                        null,
                                        labels.get(index)));
            }
        }
    }

    protected void drawPrimaryEntries(Canvas canvas) {
        for (int index = 0; index < v3Layout.primaryEntries().size(); index++) {
            GameHomeV3Layout.Tile tile = v3Layout.primaryEntries().get(index);
            // 创建房间是第一格底层；返回房间是同坐标的第二层。原版
            // LobbyView:showBackBoom() 换的是按钮子节点 _KW_IMG_BOX_ROOM_TITLE 的贴图，
            // 按钮、命中区、entry 和点击路由都不变，这里保持同一语义。
            drawTileBitmap(canvas, finalArtwork.primaryEntries.get(index), tile);
            boolean backRoom = index == CREATE_ROOM_SLOT && isInRoom();
            if (backRoom) {
                drawTileBitmap(
                        canvas,
                        finalArtwork.primaryBackRoom,
                        v3Layout.primaryBackRoom());
            }
            bindEntryAction(
                    canvas,
                    tile,
                    PRIMARY_CODES.get(index),
                    backRoom ? BACK_ROOM_LABEL : PRIMARY_LABELS.get(index));
        }
    }

    protected void drawGameGrid(Canvas canvas) {
        for (int index = 0; index < v3Layout.gameEntries().size(); index++) {
            drawTileBitmap(
                    canvas,
                    finalArtwork.gameEntries.get(index),
                    v3Layout.gameEntries().get(index));
            bindEntryAction(
                    canvas,
                    v3Layout.gameEntries().get(index),
                    GAME_CODES.get(index),
                    GAME_FALLBACK_LABELS.get(index));
        }
    }

    /** 在所有槽位之上叠加入口气泡，让气泡不被相邻卡片压住。 */
    protected void drawEntryBubbles(Canvas canvas) {
        if (bubbleRenderer == null) {
            bubbleRenderer = new LobbyBubbleRenderer(getContext());
        }
        if (!bubbleRenderer.hasArtwork()) {
            return;
        }
        if (bubbleClockStartNanos == 0L) {
            bubbleClockStartNanos = System.nanoTime();
        }
        float elapsedSeconds =
                (System.nanoTime() - bubbleClockStartNanos) / 1_000_000_000.0f;
        boolean playing = false;
        for (int index = 0; index < v3Layout.primaryEntries().size(); index++) {
            playing |=
                    bubbleRenderer.draw(
                            canvas,
                            v3Layout.primaryEntries().get(index),
                            entriesByCode.get(PRIMARY_CODES.get(index)),
                            elapsedSeconds);
        }
        for (int index = 0; index < v3Layout.gameEntries().size(); index++) {
            playing |=
                    bubbleRenderer.draw(
                            canvas,
                            v3Layout.gameEntries().get(index),
                            entriesByCode.get(GAME_CODES.get(index)),
                            elapsedSeconds);
        }
        if (playing) {
            postInvalidateOnAnimation();
        }
    }

    protected void drawStore(Canvas canvas) {
        bindZhejiangLobbyAction(
                canvas,
                v3Layout.store(),
                ZhejiangLobbyAction.bottom(v3Layout.store().key()));
    }

    protected void drawBottomNavigation(Canvas canvas) {
        drawTransparentBitmap(canvas, finalArtwork.bottomBar, v3Layout.bottomBar());
        drawTransparentBitmap(canvas, finalArtwork.store, v3Layout.storeArtwork());
        drawBottomLabel(canvas, v3Layout.store(), "商城");
        drawInteractionState(canvas, v3Layout.store(), true);
        for (int index = 0; index < v3Layout.bottomActions().size(); index++) {
            GameHomeV3Layout.Tile tile = v3Layout.bottomActions().get(index);
            drawBottomLabel(canvas, tile, BOTTOM_LABELS.get(index));
            bindZhejiangLobbyAction(
                    canvas, tile, ZhejiangLobbyAction.bottom(BOTTOM_CODES.get(index)));
        }
    }

    private void bindZhejiangLobbyAction(
            Canvas canvas,
            GameHomeV3Layout.Tile tile,
            ZhejiangLobbyAction.Route route) {
        boolean enabled = route.unavailableMessage() == null;
        drawInteractionState(canvas, tile, enabled);
        if (route.destination() == ZhejiangLobbyAction.Destination.MAIL) {
            drawMailAttention(canvas, tile);
        }
        hitTargets.add(
                new HitTarget(
                        tile.key(),
                        tile.hit(),
                        hitKind(route.destination()),
                        null,
                        route.unavailableMessage()));
    }

    private void drawMailAttention(Canvas canvas, GameHomeV3Layout.Tile tile) {
        if (!hasMailAttention()) return;
        GameHomeV3Layout.Box box = tile.destination();
        float size = Math.min(54f, Math.min(box.width(), box.height()) * 0.3f);
        drawTransparentBitmap(
                canvas,
                mailAttentionBitmap,
                new GameHomeV3Layout.Box(
                        box.right() - size * 0.65f,
                        box.top() - size * 0.15f,
                        box.right() + size * 0.35f,
                        box.top() + size * 0.85f));
    }

    protected void drawQuickStart(Canvas canvas) {
        GameHomeV3Layout.Tile tile = v3Layout.quickStart();
        ZhejiangQuickStart.Target target = ZhejiangQuickStart.resolve(state.entries());
        drawTileBitmap(canvas, finalArtwork.quickStartButton, tile);
        drawTransparentBitmap(canvas, finalArtwork.quickStartLabel, v3Layout.quickStartLabel());
        drawQuickStartSubtitle(canvas, target.subtitle());
        drawInteractionState(canvas, tile, target.entry() != null);
        hitTargets.add(
                target.entry() == null
                        ? new HitTarget(
                                tile.key(),
                                tile.hit(),
                                HitKind.LOBBY_STATUS,
                                null,
                                target.unavailableMessage())
                        : new HitTarget(
                                tile.key(),
                                tile.hit(),
                                HitKind.ENTRY,
                                target.entry(),
                                target.subtitle()));
    }

    private static HitKind hitKind(ZhejiangLobbyAction.Destination destination) {
        return switch (destination) {
            case SHOP -> HitKind.SHOP;
            case SHOP_DECORATION -> HitKind.SHOP_DECORATION;
            case ACTIVITY_CENTER -> HitKind.ACTIVITY_CENTER;
            case SHARE -> HitKind.SHARE;
            case DAILY_MISSION -> HitKind.DAILY_MISSION;
            case SHOP_INVENTORY -> HitKind.SHOP_INVENTORY;
            case MAIL -> HitKind.MAIL;
            case GAME_RECORDS -> HitKind.GAME_RECORDS;
            case MORE_MENU -> HitKind.MORE_MENU;
            case SETTINGS -> HitKind.PERSONAL_CENTER_SETTINGS;
            default -> HitKind.LOBBY_STATUS;
        };
    }

    private void drawBottomLabel(
            Canvas canvas,
            GameHomeV3Layout.Tile tile,
            String label) {
        titlePaint.setColor(Color.rgb(244, 226, 207));
        titlePaint.setTextSize("STORE".equals(tile.key()) ? 68.0f : 60.0f);
        titlePaint.setShadowLayer(5.0f, 0.0f, 3.0f, Color.rgb(62, 33, 26));
        drawCenteredFittedText(
                canvas,
                label,
                rect(v3Layout.bottomLabelBounds(tile)),
                titlePaint,
                38.0f);
        titlePaint.clearShadowLayer();
    }

    private void drawQuickStartSubtitle(Canvas canvas, String subtitle) {
        valuePaint.setColor(Color.rgb(114, 57, 17));
        valuePaint.setTextSize(48.0f);
        valuePaint.setShadowLayer(2.0f, 0.0f, 1.0f, Color.argb(96, 255, 244, 205));
        drawCenteredFittedText(
                canvas,
                subtitle,
                rect(v3Layout.quickStartSubtitle()),
                valuePaint,
                32.0f);
        valuePaint.clearShadowLayer();
    }

    protected void bindEntryAction(
            Canvas canvas,
            GameHomeV3Layout.Tile tile,
            String entryCode,
            String fallbackLabel) {
        GameHomeState.Entry entry = entriesByCode.get(entryCode);
        boolean enabled = entry != null && entry.enabled();
        drawInteractionState(canvas, tile, enabled);
        hitTargets.add(
                entry == null
                        ? new HitTarget(
                                tile.key(),
                                tile.hit(),
                                HitKind.UNAVAILABLE,
                                null,
                                fallbackLabel)
                        : new HitTarget(
                                tile.key(),
                                tile.hit(),
                                HitKind.ENTRY,
                                entry,
                                entry.displayName()));
    }

    protected void drawInteractionState(
            Canvas canvas,
            GameHomeV3Layout.Tile tile,
            boolean enabled) {
        boolean pressed = tile.key().equals(pressedTargetKey);
        if (!pressed) {
            return;
        }
        interactionPaint.setColor(Color.argb(92, 12, 22, 55));
        RectF bounds = rect(tile.destination());
        canvas.drawRoundRect(bounds, 28.0f, 28.0f, interactionPaint);
    }

    protected void drawTileBitmap(
            Canvas canvas,
            Bitmap bitmap,
            GameHomeV3Layout.Tile tile) {
        drawTransparentBitmap(canvas, bitmap, tile.destination());
    }
}
