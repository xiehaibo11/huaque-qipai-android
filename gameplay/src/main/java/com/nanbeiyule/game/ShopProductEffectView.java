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

/**
 * Transparent card-interior layer for NewGoldHall Shop.csb's {@code _nodeAniLight}.
 *
 * <p>The recovered ShopView.lua mounts {@code zzb_ty_xingguang} only for diamond and gold
 * products, using {@code animation} for items 1-6 and {@code animation2} afterwards. The
 * price button is a separate CSB sibling, so each spec masks its price-bar rectangle.
 */
final class ShopProductEffectView extends TextureView
        implements TextureView.SurfaceTextureListener {
    private static final String TAG = "ShopProductFx";
    private static final String EFFECT_DIRECTORY =
            "shop_product_effects/zzb_ty_xingguang";
    private static final String EFFECT_NAME = "zzb_ty_xingguang";
    private static final int ORIGINAL_EFFECT_ITEM_COUNT = 8;
    private static final int PRIMARY_ANIMATION_ITEM_COUNT = 6;
    private static final float ICON_ANCHOR_Y_OFFSET = 245f;
    private static final float REGULAR_PRICE_BAR_HEIGHT = 78f;
    private static final List<OriginalLobbyEffectSpec> SPECS = buildSpecs();

    private final ExecutorService assetExecutor = Executors.newSingleThreadExecutor();
    private final Object stateLock = new Object();
    private final long animationStartedNanos;
    private SurfaceTexture availableSurface;
    private MembershipPrivilegeEffectRenderThread renderThread;
    private List<OriginalLobbyEffectInstance> loadedInstances;
    private int surfaceWidth;
    private int surfaceHeight;
    private boolean detached;
    private boolean assetsLoading;
    private float verticalScrollOffset;

    ShopProductEffectView(Context context, long animationStartedNanos) {
        super(context);
        this.animationStartedNanos = animationStartedNanos;
        setOpaque(false);
        setClickable(false);
        setFocusable(false);
        setSurfaceTextureListener(this);
        synchronized (stateLock) {
            requestAssetLoadLocked();
        }
    }

    static boolean supportsCategory(ShopCategory category) {
        return category == ShopCategory.DIAMOND_RECHARGE || category == ShopCategory.COIN;
    }

    static List<OriginalLobbyEffectSpec> specs() {
        return SPECS;
    }

    void setVerticalScrollOffset(float verticalScrollOffset) {
        synchronized (stateLock) {
            this.verticalScrollOffset = Math.max(0f, verticalScrollOffset);
            if (renderThread != null) {
                renderThread.setContentOffsetY(this.verticalScrollOffset);
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        synchronized (stateLock) {
            availableSurface = surface;
            surfaceWidth = width;
            surfaceHeight = height;
            if (loadedInstances == null && !assetsLoading && !detached) {
                requestAssetLoadLocked();
            }
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
        // The EGL render thread presents frames directly.
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

    private void requestAssetLoadLocked() {
        assetsLoading = true;
        assetExecutor.execute(this::loadAssets);
    }

    private void loadAssets() {
        List<OriginalLobbyEffectInstance> instances = new ArrayList<>();
        try {
            for (OriginalLobbyEffectSpec spec : SPECS) {
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
            Log.e(TAG, "Unable to load recovered original shop product effects", exception);
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
        renderThread.setContentOffsetY(verticalScrollOffset);
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
            Log.w(TAG, "Interrupted while stopping shop product effect renderer", exception);
        }
    }

    private static List<OriginalLobbyEffectSpec> buildSpecs() {
        List<OriginalLobbyEffectSpec> specs = new ArrayList<>();
        AdaptiveViewport.Rect viewport =
                new AdaptiveViewport.Rect(
                        ShopRuntimeLayout.CONTENT_VIEWPORT.left(),
                        ShopRuntimeLayout.CONTENT_VIEWPORT.top(),
                        ShopRuntimeLayout.CONTENT_VIEWPORT.right(),
                        ShopRuntimeLayout.CONTENT_VIEWPORT.bottom());
        for (int index = 0; index < ORIGINAL_EFFECT_ITEM_COUNT; index++) {
            ShopLayout.Rect card =
                    ShopRuntimeLayout.productCard(
                            ShopCategory.DIAMOND_RECHARGE, index, 0f);
            AdaptiveViewport.Rect priceMask =
                    new AdaptiveViewport.Rect(
                            card.left(),
                            card.bottom() - REGULAR_PRICE_BAR_HEIGHT,
                            card.right(),
                            card.bottom());
            specs.add(
                    new OriginalLobbyEffectSpec(
                                    EFFECT_DIRECTORY,
                                    EFFECT_NAME,
                                    index < PRIMARY_ANIMATION_ITEM_COUNT
                                            ? "animation"
                                            : "animation2",
                                    card.centerX(),
                                    card.top() + ICON_ANCHOR_Y_OFFSET,
                                    1f,
                                    Set.of(),
                                    viewport)
                            .withMaskContentRects(List.of(priceMask)));
        }
        return List.copyOf(specs);
    }
}
