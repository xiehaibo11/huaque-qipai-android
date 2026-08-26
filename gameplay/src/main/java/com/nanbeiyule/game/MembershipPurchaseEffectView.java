package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Transparent overlay for the original SxvipShopLayer zzb_hy_xfhy purchase effects. */
final class MembershipPurchaseEffectView extends TextureView
        implements TextureView.SurfaceTextureListener {
    enum Layer {
        BACKGROUND,
        FOREGROUND
    }

    private static final String TAG = "MembershipPurchaseFx";
    private static final String EFFECT_DIRECTORY = "membership_purchase_effects/zzb_hy_xfhy";
    private static final String EFFECT_NAME = "zzb_hy_xfhy";

    // Original SxvipShopLayer.csb _KW_ITEM_ANI_1: first red 30-day card border sparkle.
    static final float FIRST_THIRTY_DAY_EFFECT_X = 314.0f;
    static final float FIRST_THIRTY_DAY_EFFECT_Y = 514.02f;
    static final float FIRST_THIRTY_DAY_BORDER_SPARKLE_X = 314.0f;
    static final float FIRST_THIRTY_DAY_BORDER_SPARKLE_Y = 514.02f;
    static final float FIRST_THIRTY_DAY_BADGE_EFFECT_X = 173.6f;
    static final float FIRST_THIRTY_DAY_BADGE_EFFECT_Y = 338.24f;
    static final float FIRST_THIRTY_DAY_BUTTON_EFFECT_X = 314.0f;
    static final float FIRST_THIRTY_DAY_BUTTON_EFFECT_Y = 893.5f;
    static final AdaptiveViewport.Rect SHOP_SCROLL_CLIP_RECT =
            new AdaptiveViewport.Rect(80.0f, 125.0f, 1840.0f, 955.0f);
    static final AdaptiveViewport.Rect FIRST_REWARD_LIST_CLIP_RECT =
            new AdaptiveViewport.Rect(93.0f, 485.8863f, 533.0f, 785.8863f);

    private static final List<OriginalLobbyEffectSpec> BACKGROUND_SPECS =
            List.of(
                    purchaseSpec(
                            "gx",
                            FIRST_THIRTY_DAY_BORDER_SPARKLE_X,
                            FIRST_THIRTY_DAY_BORDER_SPARKLE_Y,
                            SHOP_SCROLL_CLIP_RECT));

    private static final List<OriginalLobbyEffectSpec> FOREGROUND_SPECS =
            List.of(
                    purchaseSpec(
                            "btn",
                            FIRST_THIRTY_DAY_BUTTON_EFFECT_X,
                            FIRST_THIRTY_DAY_BUTTON_EFFECT_Y,
                            SHOP_SCROLL_CLIP_RECT),
                    purchaseSpec(
                            "ss",
                            168.0f, 563.3863f,
                            FIRST_REWARD_LIST_CLIP_RECT),
                    purchaseSpec(
                            "ss",
                            313.0f, 563.3863f,
                            FIRST_REWARD_LIST_CLIP_RECT),
                    purchaseSpec(
                            "ss",
                            458.0f, 563.3863f,
                            FIRST_REWARD_LIST_CLIP_RECT),
                    purchaseSpec(
                            "ss",
                            168.0f, 708.3863f,
                            FIRST_REWARD_LIST_CLIP_RECT),
                    purchaseSpec(
                            "ss",
                            313.0f, 708.3863f,
                            FIRST_REWARD_LIST_CLIP_RECT),
                    purchaseSpec(
                            "ss",
                            458.0f, 708.3863f,
                            FIRST_REWARD_LIST_CLIP_RECT),
                    purchaseSpec(
                            "hy",
                            FIRST_THIRTY_DAY_BADGE_EFFECT_X,
                            FIRST_THIRTY_DAY_BADGE_EFFECT_Y,
                            SHOP_SCROLL_CLIP_RECT));

    private final ExecutorService assetExecutor = Executors.newSingleThreadExecutor();
    private final Object stateLock = new Object();
    private final List<OriginalLobbyEffectSpec> specs;
    private final long animationStartedNanos;
    private SurfaceTexture availableSurface;
    private MembershipPrivilegeEffectRenderThread renderThread;
    private List<OriginalLobbyEffectInstance> loadedInstances;
    private int surfaceWidth;
    private int surfaceHeight;
    private boolean detached;
    private boolean assetsLoading = true;
    private float scrollOffset;

    MembershipPurchaseEffectView(Context context) {
        this(context, Layer.BACKGROUND, System.nanoTime());
    }

    MembershipPurchaseEffectView(
            Context context, Layer layer, long animationStartedNanos) {
        super(context);
        specs = layer == Layer.BACKGROUND ? BACKGROUND_SPECS : FOREGROUND_SPECS;
        this.animationStartedNanos = animationStartedNanos;
        setOpaque(false);
        setClickable(false);
        setFocusable(false);
        setSurfaceTextureListener(this);
        assetExecutor.execute(this::loadAssets);
    }

    void setScrollOffset(float scrollOffset) {
        synchronized (stateLock) {
            this.scrollOffset = Math.max(0.0f, scrollOffset);
            if (renderThread != null) {
                renderThread.setContentOffsetX(scrollOffset);
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        synchronized (stateLock) {
            availableSurface = surface;
            surfaceWidth = width;
            surfaceHeight = height;
            startRendererIfReadyLocked();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        synchronized (stateLock) {
            surfaceWidth = width;
            surfaceHeight = height;
            if (renderThread != null) {
                renderThread.setViewport(width, height);
            }
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        MembershipPrivilegeEffectRenderThread thread;
        synchronized (stateLock) {
            availableSurface = null;
            thread = renderThread;
            renderThread = null;
        }
        stopRenderer(thread);
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // EGL render thread drives presentation.
    }

    @Override
    protected void onDetachedFromWindow() {
        MembershipPrivilegeEffectRenderThread thread;
        List<OriginalLobbyEffectInstance> abandoned;
        synchronized (stateLock) {
            detached = true;
            thread = renderThread;
            renderThread = null;
            abandoned = loadedInstances;
            loadedInstances = null;
        }
        stopRenderer(thread);
        OriginalLobbyEffectView.recycleInstances(abandoned);
        assetExecutor.shutdownNow();
        super.onDetachedFromWindow();
    }

    private void loadAssets() {
        List<OriginalLobbyEffectInstance> instances = new ArrayList<>();
        try {
            for (OriginalLobbyEffectSpec spec : specs) {
                instances.add(
                        new OriginalLobbyEffectInstance(
                                OriginalLobbyEffectAssets.load(getContext().getAssets(), spec),
                                spec));
            }
            synchronized (stateLock) {
                assetsLoading = false;
                if (detached) {
                    OriginalLobbyEffectView.recycleInstances(instances);
                    return;
                }
                loadedInstances = instances;
                startRendererIfReadyLocked();
            }
        } catch (Exception exception) {
            OriginalLobbyEffectView.recycleInstances(instances);
            synchronized (stateLock) {
                assetsLoading = false;
            }
            Log.e(TAG, "Unable to load recovered original purchase fireworks", exception);
        }
    }

    private void startRendererIfReadyLocked() {
        if (detached
                || assetsLoading
                || renderThread != null
                || availableSurface == null
                || loadedInstances == null
                || surfaceWidth <= 0
                || surfaceHeight <= 0) {
            return;
        }
        List<OriginalLobbyEffectInstance> instances = loadedInstances;
        loadedInstances = null;
        renderThread =
                new MembershipPrivilegeEffectRenderThread(
                        availableSurface,
                        surfaceWidth,
                        surfaceHeight,
                        instances,
                        animationStartedNanos);
        renderThread.setContentOffsetX(scrollOffset);
        renderThread.start();
    }

    private void stopRenderer(MembershipPrivilegeEffectRenderThread thread) {
        if (thread == null) {
            return;
        }
        thread.requestStop();
        try {
            thread.join(1500L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            Log.w(TAG, "Interrupted while stopping purchase effect renderer", exception);
        }
    }

    private static OriginalLobbyEffectSpec purchaseSpec(
            String animationName,
            float anchorX,
            float anchorY,
            AdaptiveViewport.Rect clipDesignRect) {
        return new OriginalLobbyEffectSpec(
                EFFECT_DIRECTORY,
                EFFECT_NAME,
                animationName,
                anchorX,
                anchorY,
                1.0f,
                Set.of(),
                clipDesignRect);
    }
}
