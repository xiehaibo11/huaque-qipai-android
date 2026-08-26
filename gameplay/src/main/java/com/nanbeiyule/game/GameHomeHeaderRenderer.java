package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

/** Header and hostess layer for the final independent-asset game-home composition. */

abstract class GameHomeHeaderRenderer extends GameHomeViewState {

    /**
     * 原版按 {@code m_presenter:getIsMan()} 选择男女骨架，当前大厅 API 尚未下发玩家性别，
     * 因此固定使用实机证据里出现的女性骨架；性别字段接入后改为按服务端数据选择。
     */
    private final LobbyCharacterController lobbyCharacter;

    protected GameHomeHeaderRenderer(Context context, GameHomeState state) {
        super(context, state);
        lobbyCharacter = createLobbyCharacter(context);
    }

    protected GameHomeHeaderRenderer(
            Context context,
            GameHomeState state,
            boolean drawBackgroundEnabled) {
        super(context, state, drawBackgroundEnabled);
        lobbyCharacter = createLobbyCharacter(context);
    }

    private static LobbyCharacterController createLobbyCharacter(Context context) {
        return new LobbyCharacterController(
                context, LobbyCharacterStateMachine.Gender.FEMALE);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        lobbyCharacter.attach(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        lobbyCharacter.release();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        GameHomeViewportLayout layout =
                GameHomeViewportLayout.calculate(
                        getWidth(),
                        getHeight(),
                        adaptiveSafeInsets());
        hitTargets.clear();

        int pageSave =
                AdaptiveCanvasDrawing.apply(
                        canvas,
                        layout.pageTransform());
        drawOriginalHeaderData(canvas);
        drawFinalHostess(canvas);
        drawTopActions(canvas);
        drawSideActions(canvas);
        drawPrimaryEntries(canvas);
        drawGameGrid(canvas);
        drawStore(canvas);
        drawBottomNavigation(canvas);
        drawQuickStart(canvas);
        drawEntryBubbles(canvas);
        canvas.restoreToCount(pageSave);
    }

    /** 入口气泡由内容层实现，这里只保证它画在所有槽位之上。 */
    protected abstract void drawEntryBubbles(Canvas canvas);

    private void drawFinalHostess(Canvas canvas) {
        drawHostessPlatform(canvas);
        GameHomeV3Layout.Box character = v3Layout.character();

        // 原版人物是 Spine 骨架实时动画（renwunv/renwunan，3.3.07），骨架未装载完成前
        // 先画静态位图占位，装载完成后由 LobbyCharacterController 接管，不再叠加位图。
        if (!lobbyCharacter.drawIfReady(canvas, getWidth(), getHeight())) {
            drawTransparentBitmap(canvas, finalArtwork.character, character);
        }
    }

    /**
     * 底座按顶面椭圆中心对齐人物站立点，不按位图边框对齐。
     *
     * <p>几何在 {@link HostessPlatformLayout} 中求解并单测，这里只负责把结果画出来。
     */
    private void drawHostessPlatform(Canvas canvas) {
        Bitmap platform = finalArtwork.hostessPlatform;
        if (platform == null || platform.isRecycled()) {
            return;
        }
        canvas.drawBitmap(platform, null, rect(HostessPlatformLayout.place()), bitmapPaint);
    }

    protected void drawOriginalHeaderData(Canvas canvas) {
        drawOriginalHeaderBackdrop(canvas);
        drawTransparentHeaderTracks(canvas);
        drawTransparentHeaderAssets(canvas);

        avatarFrameRenderer.draw(
                canvas,
                avatarBitmap,
                rect(headerLayout.avatarAssembly()),
                state.player().membershipLevel(),
                0L);

        titlePaint.setTextSize(64.0f);
        titlePaint.setColor(Color.WHITE);
        drawFittedText(
                canvas,
                state.player().displayName(),
                headerLayout.nickname().left(),
                92.0f,
                headerLayout.nickname().width(),
                titlePaint,
                38.0f);

        valuePaint.setTextSize(50.0f);
        valuePaint.setColor(Color.WHITE);
        drawFittedText(
                canvas,
                "ID:" + state.player().publicPlayerId(),
                headerLayout.playerId().left(),
                158.0f,
                headerLayout.playerId().width(),
                valuePaint,
                32.0f);

        drawHeaderValue(
                canvas,
                state.region().areaName(),
                rect(headerLayout.regionValue()),
                45.0f,
                Color.WHITE);
        // 闲逸原版胶囊内只有数值，没有货币名文本；房卡图标本身已含“房卡”标签。
        drawHeaderValue(
                canvas,
                Long.toString(state.wallet().roomCards()),
                rect(headerLayout.roomCardValue()),
                68.0f,
                Color.WHITE);
        drawHeaderValue(
                canvas,
                Long.toString(state.wallet().coins()),
                rect(headerLayout.coinValue()),
                68.0f,
                Color.WHITE);
        drawHeaderValue(
                canvas,
                Long.toString(state.wallet().diamonds()),
                rect(headerLayout.diamondValue()),
                68.0f,
                Color.WHITE);

        hitTargets.add(
                new HitTarget(
                        "PERSONAL_CENTER",
                        v3Layout.playerPanel(),
                        HitKind.PERSONAL_CENTER,
                        null,
                        "个人中心"));
        hitTargets.add(
                new HitTarget(
                        "CHANGE_REGION",
                        headerLayout.regionGroup(),
                        HitKind.CHANGE_REGION,
                        null,
                        state.region().areaName()));
        hitTargets.add(
                new HitTarget(
                        "SHOP_ROOM_CARD",
                        headerLayout.roomCardPlusHit(),
                        HitKind.SHOP_ROOM_CARD,
                        null,
                        "房卡"));
        hitTargets.add(
                new HitTarget(
                        "SHOP_COIN",
                        headerLayout.coinPlusHit(),
                        HitKind.SHOP_COIN,
                        null,
                        "金币"));
        hitTargets.add(
                new HitTarget(
                        "SHOP_DIAMOND",
                        headerLayout.diamondPlusHit(),
                        HitKind.SHOP_DIAMOND,
                        null,
                        "钻石"));
    }

    protected void drawHeaderValue(
            Canvas canvas,
            String value,
            RectF bounds,
            float textSize,
            int color) {
        valuePaint.setTextSize(textSize);
        valuePaint.setColor(color);
        drawCenteredFittedText(
                canvas,
                value,
                bounds,
                valuePaint,
                32.0f);
    }

    protected void drawOriginalHeaderBackdrop(Canvas canvas) {
        // 闲逸原版顶栏没有玩家信息衬底，头像、昵称与 ID 直接贴合大厅背景；
        // 保留此方法以维持渲染契约，但不再绘制蓝色面板与描边。
    }

    protected void drawTransparentHeaderTracks(Canvas canvas) {
        drawHeaderTrack(canvas, headerLayout.regionGroup());
        // 房卡/金币/钻石胶囊使用闲逸原版 hall_icon_bg.png（140x35 半透明深色，
        // 左缘直角被图标压住、右端圆角），九宫格左右各保护 17px，中间拉伸。
        drawHorizontalScale9Bitmap(
                canvas, finalArtwork.walletTrack, headerLayout.roomCardTrack(), 17, 17);
        drawHorizontalScale9Bitmap(
                canvas, finalArtwork.walletTrack, headerLayout.coinTrack(), 17, 17);
        drawHorizontalScale9Bitmap(
                canvas, finalArtwork.walletTrack, headerLayout.diamondTrack(), 17, 17);
    }

    protected void drawTransparentHeaderAssets(Canvas canvas) {
        drawTransparentBitmap(canvas, locationIconBitmap, headerLayout.locationIcon());
        drawTransparentBitmap(canvas, finalArtwork.resourceRoomCard, headerLayout.roomCardIcon());
        drawTransparentBitmap(canvas, finalArtwork.resourceCoin, headerLayout.coinIcon());
        drawTransparentBitmap(canvas, finalArtwork.resourceDiamond, headerLayout.diamondIcon());
        // 白色加号徽章为闲逸原版 hall_add_btn.png，中心落在胶囊左缘。
        drawTransparentBitmap(canvas, finalArtwork.walletAdd, headerLayout.roomCardPlus());
        drawTransparentBitmap(canvas, finalArtwork.walletAdd, headerLayout.coinPlus());
        drawTransparentBitmap(canvas, finalArtwork.walletAdd, headerLayout.diamondPlus());
    }

    private void drawHeaderTrack(Canvas canvas, GameHomeV3Layout.Box box) {
        // 地区胶囊为南北娱乐自有入口，沿用闲逸 money_bg 的深藏青半透明质感。
        RectF bounds = rect(box);
        interactionPaint.setStyle(Paint.Style.FILL);
        interactionPaint.setColor(Color.argb(102, 37, 38, 45));
        canvas.drawRoundRect(bounds, 41.0f, 41.0f, interactionPaint);
    }

    protected void drawTransparentBitmap(
            Canvas canvas,
            Bitmap bitmap,
            GameHomeV3Layout.Box destination) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        canvas.drawBitmap(bitmap, null, rect(destination), bitmapPaint);
    }

    protected void drawHorizontalScale9Bitmap(
            Canvas canvas,
            Bitmap bitmap,
            GameHomeV3Layout.Box destination,
            int leftCapPixels,
            int rightCapPixels) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        int sourceWidth = bitmap.getWidth();
        int sourceHeight = bitmap.getHeight();
        int centerRight = sourceWidth - rightCapPixels;
        float scale = destination.height() / sourceHeight;
        float leftWidth = leftCapPixels * scale;
        float rightWidth = rightCapPixels * scale;
        float centerLeft = destination.left() + leftWidth;
        float centerRightDestination = destination.right() - rightWidth;

        canvas.drawBitmap(
                bitmap,
                new Rect(0, 0, leftCapPixels, sourceHeight),
                new RectF(
                        destination.left(),
                        destination.top(),
                        centerLeft,
                        destination.bottom()),
                bitmapPaint);
        canvas.drawBitmap(
                bitmap,
                new Rect(leftCapPixels, 0, centerRight, sourceHeight),
                new RectF(
                        centerLeft,
                        destination.top(),
                        centerRightDestination,
                        destination.bottom()),
                bitmapPaint);
        canvas.drawBitmap(
                bitmap,
                new Rect(centerRight, 0, sourceWidth, sourceHeight),
                new RectF(
                        centerRightDestination,
                        destination.top(),
                        destination.right(),
                        destination.bottom()),
                bitmapPaint);
    }
}
