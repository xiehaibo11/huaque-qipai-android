package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 驱动原版大厅人物：装载骨架、推进状态机、按帧请求重绘。
 *
 * <p>对应原版 {@code prime/hall/style/HallStyleBaseLayer.lua} 的
 * {@code refreshPeopleAtlas} 与 {@code update(dt)}。骨架解码在后台线程完成，
 * 主线程只做采样与绘制。
 */
final class LobbyCharacterController {
    private static final String TAG = "LobbyCharacter";
    private static final String ASSET_ROOT = "lobby_character";

    private final ExecutorService loader = Executors.newSingleThreadExecutor();
    private final Context context;
    private final LobbyCharacterStateMachine stateMachine;

    private View host;
    private LobbyCharacterRenderer renderer;
    private boolean loading;
    private boolean failed;
    private long lastFrameNanos;

    LobbyCharacterController(Context context, LobbyCharacterStateMachine.Gender gender) {
        this.context = context.getApplicationContext();
        this.stateMachine = new LobbyCharacterStateMachine(gender);
    }

    void attach(View host) {
        this.host = host;
        // 回到前台时重新计时，避免把整段后台时间当成一帧的 dt 直接跳过多个状态。
        lastFrameNanos = 0L;
    }

    void detach() {
        host = null;
        lastFrameNanos = 0L;
    }

    void release() {
        detach();
        loader.shutdownNow();
        if (renderer != null) {
            renderer.recycle();
            renderer = null;
        }
    }

    /**
     * 在大厅主 Canvas 的页面变换内绘制人物。
     *
     * @param viewportWidthPixels 实际窗口宽度，用于原版显示门槛判定
     * @param viewportHeightPixels 实际窗口高度
     */
    boolean drawIfReady(Canvas canvas, int viewportWidthPixels, int viewportHeightPixels) {
        if (!LobbyCharacterStateMachine.shouldRender(viewportWidthPixels, viewportHeightPixels)) {
            // 原版在窄屏直接不加载人物，这里也不回退静态位图。
            return true;
        }
        if (renderer == null) {
            requestLoad();
            return false;
        }
        advanceClock();
        renderer.draw(canvas, stateMachine.animationName(), stateMachine.animationTime());
        if (host != null) {
            host.postInvalidateOnAnimation();
        }
        return true;
    }

    private void advanceClock() {
        long now = System.nanoTime();
        if (lastFrameNanos != 0L) {
            stateMachine.advance((now - lastFrameNanos) / 1_000_000_000.0f);
        }
        lastFrameNanos = now;
    }

    private void requestLoad() {
        if (loading || failed || loader.isShutdown()) {
            return;
        }
        loading = true;
        String skeleton = stateMachine.skeletonName();
        loader.execute(
                () -> {
                    LobbyCharacterRenderer loaded = null;
                    try {
                        OriginalLobbyEffectAssets.Loaded assets =
                                OriginalLobbyEffectAssets.load(
                                        context.getAssets(),
                                        ASSET_ROOT + "/" + skeleton,
                                        skeleton);
                        loaded =
                                new LobbyCharacterRenderer(
                                        assets.runtime(),
                                        assets.pages().values().iterator().next(),
                                        "animation");
                    } catch (Exception exception) {
                        Log.w(TAG, "Unable to load original lobby character " + skeleton);
                    }
                    publish(loaded);
                });
    }

    private void publish(LobbyCharacterRenderer loaded) {
        View target = host;
        if (target == null) {
            if (loaded != null) {
                loaded.recycle();
            }
            loading = false;
            return;
        }
        LobbyCharacterRenderer result = loaded;
        target.post(
                () -> {
                    loading = false;
                    if (result == null) {
                        failed = true;
                        return;
                    }
                    if (host == null) {
                        result.recycle();
                        return;
                    }
                    renderer = result;
                    lastFrameNanos = 0L;
                    host.invalidate();
                });
    }
}
