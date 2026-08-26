package com.nanbeiyule.game;

import java.util.List;
import java.util.Set;

/**
 * Declarative placement of one recovered original lobby spine effect.
 *
 * <p>Anchors use the 3200 x 1792 page space shared with the baked
 * background, the lobby controls and their hit testing; the renderer maps
 * that space through the same adaptive viewport transform, so an anchored
 * effect lands exactly on its baked counterpart on every screen.
 */
record OriginalLobbyEffectSpec(
        String assetDirectory,
        String baseName,
        String animationName,
        float anchorX,
        float anchorY,
        float scale,
        Set<String> excludedAttachments,
        AdaptiveViewport.Rect clipDesignRect,
        List<AdaptiveViewport.Rect> maskContentRects) {
    OriginalLobbyEffectSpec(
            String assetDirectory,
            String baseName,
            String animationName,
            float anchorX,
            float anchorY,
            float scale,
            Set<String> excludedAttachments,
            AdaptiveViewport.Rect clipDesignRect) {
        this(
                assetDirectory,
                baseName,
                animationName,
                anchorX,
                anchorY,
                scale,
                excludedAttachments,
                clipDesignRect,
                List.of());
    }

    OriginalLobbyEffectSpec(
            String assetDirectory,
            String baseName,
            String animationName,
            float anchorX,
            float anchorY,
            float scale,
            Set<String> excludedAttachments) {
        this(
                assetDirectory,
                baseName,
                animationName,
                anchorX,
                anchorY,
                scale,
                excludedAttachments,
                null);
    }

    /**
     * Returns a copy whose animation is hidden behind the given content-space rectangles,
     * reproducing the original draw order where the effect node sits below every card
     * panel, so all opaque card bitmaps cover the effect center. The rectangles scroll
     * with the content, so the renderer subtracts the content offset before carving them
     * out of the clip rectangle.
     */
    OriginalLobbyEffectSpec withMaskContentRects(
            List<AdaptiveViewport.Rect> maskContentRects) {
        return new OriginalLobbyEffectSpec(
                assetDirectory,
                baseName,
                animationName,
                anchorX,
                anchorY,
                scale,
                excludedAttachments,
                clipDesignRect,
                maskContentRects);
    }

    OriginalLobbyEffectSpec {
        if (assetDirectory == null || assetDirectory.isEmpty()) {
            throw new IllegalArgumentException("assetDirectory required");
        }
        if (baseName == null || baseName.isEmpty()) {
            throw new IllegalArgumentException("baseName required");
        }
        if (animationName == null || animationName.isEmpty()) {
            throw new IllegalArgumentException("animationName required");
        }
        if (scale <= 0.0f) {
            throw new IllegalArgumentException("scale must be positive");
        }
        if (excludedAttachments == null) {
            throw new IllegalArgumentException("excludedAttachments required");
        }
        excludedAttachments = Set.copyOf(excludedAttachments);
        maskContentRects =
                maskContentRects == null ? List.of() : List.copyOf(maskContentRects);
    }

    boolean drawsAttachment(String attachmentName) {
        return !excludedAttachments.contains(attachmentName);
    }

    static OriginalLobbyEffectSpec scene(String animationName) {
        // The scene spine is authored in the original 2340 x 1080 design
        // space; on a 16:9 page it is cropped equally on both sides, so it
        // is anchored at the page center with the page scale factor.
        return new OriginalLobbyEffectSpec(
                "lobby_effects/zzb_jbdt_cj",
                "zzb_jbdt_cj",
                animationName,
                1600.0f,
                896.0f,
                3200.0f / 1920.0f,
                Set.of());
    }

    static OriginalLobbyEffectSpec overlay(
            String baseName,
            String animationName,
            float anchorX,
            float anchorY) {
        return overlay(baseName, animationName, anchorX, anchorY, 1.0f);
    }

    static OriginalLobbyEffectSpec overlay(
            String baseName,
            String animationName,
            float anchorX,
            float anchorY,
            float scale) {
        return new OriginalLobbyEffectSpec(
                "lobby_effects/" + baseName,
                baseName,
                animationName,
                anchorX,
                anchorY,
                scale,
                Set.of());
    }

    static OriginalLobbyEffectSpec overlayExcluding(
            String baseName,
            String animationName,
            float anchorX,
            float anchorY,
            float scale,
            Set<String> excludedAttachments) {
        return new OriginalLobbyEffectSpec(
                "lobby_effects/" + baseName,
                baseName,
                animationName,
                anchorX,
                anchorY,
                scale,
                excludedAttachments);
    }
}
