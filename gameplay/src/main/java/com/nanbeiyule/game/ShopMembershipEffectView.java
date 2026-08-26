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
 * Transparent overlay that restores the original SxvipShopView collection-mode effects on
 * the shop time-membership page. The original keeps _KW_ITEM_ANI_1/2/3 visible when the
 * purchase layer is embedded at 0.9 scale, so the first continuous-month card keeps its
 * zzb_hy_xfhy border sparkle, badge and button effects here as well, with every purchase
 * anchor mapped through the collection transform at the runtime layout origin. The border
 * sparkle is masked by every card interior, matching the original z-order where the effect
 * node sits below all card panels so each opaque card bitmap covers the effect center.
 */
final class ShopMembershipEffectView extends TextureView
        implements TextureView.SurfaceTextureListener {
    private static final String TAG = "ShopMembershipFx";
    private static final String EFFECT_DIRECTORY = "membership_purchase_effects/zzb_hy_xfhy";
    private static final String EFFECT_NAME = "zzb_hy_xfhy";
    static final AdaptiveViewport.Rect SHOP_LIST_CLIP_RECT =
            shopClip(MembershipPurchaseEffectView.SHOP_SCROLL_CLIP_RECT);
    private static final AdaptiveViewport.Rect SHOP_REWARD_LIST_CLIP_RECT =
            shopClip(MembershipPurchaseEffectView.FIRST_REWARD_LIST_CLIP_RECT);
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
    private float scrollOffset;

    ShopMembershipEffectView(Context context, long animationStartedNanos) {
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

    static List<OriginalLobbyEffectSpec> specs() {
        return SPECS;
    }

    void setScrollOffset(float scrollOffset) {
        synchronized (stateLock) {
            this.scrollOffset = Math.max(0.0f, scrollOffset);
            if (renderThread != null) {
                renderThread.setContentOffsetX(this.scrollOffset);
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
                // Category switches toggle this overlay with View visibility, so every new
                // surface needs fresh textures after the previous ones were released.
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
            Log.e(TAG, "Unable to load recovered original shop membership effects", exception);
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
            Log.w(TAG, "Interrupted while stopping shop membership effect renderer", exception);
        }
    }

    private static List<OriginalLobbyEffectSpec> buildSpecs() {
        List<OriginalLobbyEffectSpec> specs = new ArrayList<>();
        specs.add(
                shopSpec(
                                "gx",
                                MembershipPurchaseEffectView.FIRST_THIRTY_DAY_BORDER_SPARKLE_X,
                                MembershipPurchaseEffectView.FIRST_THIRTY_DAY_BORDER_SPARKLE_Y,
                                MembershipPurchaseEffectView.SHOP_SCROLL_CLIP_RECT)
                        .withMaskContentRects(cardContentRects()));
        specs.add(
                shopSpec(
                        "btn",
                        MembershipPurchaseEffectView.FIRST_THIRTY_DAY_BUTTON_EFFECT_X,
                        MembershipPurchaseEffectView.FIRST_THIRTY_DAY_BUTTON_EFFECT_Y,
                        MembershipPurchaseEffectView.SHOP_SCROLL_CLIP_RECT));
        float[] sparkleXs = {168.0f, 313.0f, 458.0f};
        float[] sparkleYs = {563.3863f, 708.3863f};
        for (float sparkleY : sparkleYs) {
            for (float sparkleX : sparkleXs) {
                specs.add(
                        shopSpec(
                                "ss",
                                sparkleX,
                                sparkleY,
                                MembershipPurchaseEffectView.FIRST_REWARD_LIST_CLIP_RECT));
            }
        }
        specs.add(
                shopSpec(
                        "hy",
                        MembershipPurchaseEffectView.FIRST_THIRTY_DAY_BADGE_EFFECT_X,
                        MembershipPurchaseEffectView.FIRST_THIRTY_DAY_BADGE_EFFECT_Y,
                        MembershipPurchaseEffectView.SHOP_SCROLL_CLIP_RECT));
        return List.copyOf(specs);
    }

    private static OriginalLobbyEffectSpec shopSpec(
            String animationName,
            float purchaseX,
            float purchaseY,
            AdaptiveViewport.Rect purchaseClip) {
        return new OriginalLobbyEffectSpec(
                EFFECT_DIRECTORY,
                EFFECT_NAME,
                animationName,
                shopCoordinate(purchaseX, ShopRuntimeLayout.MEMBERSHIP_LAYER_ORIGIN_X),
                shopCoordinate(purchaseY, ShopRuntimeLayout.MEMBERSHIP_LAYER_ORIGIN_Y),
                ShopRuntimeLayout.MEMBERSHIP_CARD_SCALE,
                Set.of(),
                shopClip(purchaseClip));
    }

    private static AdaptiveViewport.Rect shopClip(AdaptiveViewport.Rect purchaseClip) {
        return new AdaptiveViewport.Rect(
                shopCoordinate(purchaseClip.left(), ShopRuntimeLayout.MEMBERSHIP_LAYER_ORIGIN_X),
                shopCoordinate(purchaseClip.top(), ShopRuntimeLayout.MEMBERSHIP_LAYER_ORIGIN_Y),
                shopCoordinate(purchaseClip.right(), ShopRuntimeLayout.MEMBERSHIP_LAYER_ORIGIN_X),
                shopCoordinate(purchaseClip.bottom(), ShopRuntimeLayout.MEMBERSHIP_LAYER_ORIGIN_Y));
    }

    private static float shopCoordinate(float purchaseCoordinate, float origin) {
        return origin + purchaseCoordinate * ShopRuntimeLayout.MEMBERSHIP_CARD_SCALE;
    }

    private static List<AdaptiveViewport.Rect> cardContentRects() {
        List<AdaptiveViewport.Rect> rects = new ArrayList<>();
        for (int index = 0; index < MembershipPurchasePlan.originalPlans().size(); index++) {
            ShopLayout.Rect card = ShopRuntimeLayout.membershipCard(index, 0.0f);
            rects.add(
                    new AdaptiveViewport.Rect(
                            card.left(), card.top(), card.right(), card.bottom()));
        }
        return rects;
    }
}
