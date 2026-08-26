package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Anchor geometry for the recovered original lobby overlay effects.
 *
 * <p>The original Cocos lobby attaches every entry spine at scale 1 inside
 * a card child node whose size is only known from the compiled UI layout.
 * To reproduce the same visual result, each effect is fitted to its tile:
 * the scale makes the measured animation bounds match the tile size and the
 * anchor compensates the bounds center offset, so the artwork lands exactly
 * where the original frozen frame sits in the reference screenshot.
 *
 * <p>All anchors and scales use the 3200 x 1792 page space shared with the
 * baked background and the interactive controls, so the renderer maps them
 * through the identical viewport transform on every screen.
 *
 * <p>Animation bounds below were measured with the in-app Spine 3.7 runtime
 * by sampling each animation over a full loop (width/height/center in spine
 * pixel units).
 */
final class OriginalLobbyEffectLayout {
    private OriginalLobbyEffectLayout() {}

    static List<OriginalLobbyEffectSpec> backLayerSpecs() {
        return List.of(OriginalLobbyEffectSpec.scene("bg"));
    }

    static List<OriginalLobbyEffectSpec> frontLayerSpecs() {
        GameHomeV3Layout v3 = new GameHomeV3Layout();
        List<OriginalLobbyEffectSpec> specs = new ArrayList<>();
        specs.add(OriginalLobbyEffectSpec.scene("qj"));

        addStoreEffect(specs, v3.store());
        addQuickStartEffect(specs, v3.quickStart());
        return List.copyOf(specs);
    }

    private static void addQuickStartEffect(
            List<OriginalLobbyEffectSpec> specs,
            GameHomeV3Layout.Tile tile) {
        // Original QuickStartBtn.csb mounts zzb_jbdt_ksks on _ani at (215,75), scale=1,
        // while _imgBg is hidden. GameHomeContentRenderer now draws recovered Btn_anniu,
        // Img_KS and the truthful _txtGameName subtitle itself, so exclude static/full-plate
        // attachments and broad sweeps that would duplicate those pixels. The anchor is derived
        // from the shared quick-start tile, so correcting that tile retargets the effect too.
        float scale = 1.55f;
        specs.add(
                OriginalLobbyEffectSpec.overlayExcluding(
                        "zzb_jbdt_ksks",
                        "animation",
                        tile.destination().centerX() + 1.9f * scale,
                        tile.destination().centerY() - 22.0f + 2.3f * scale,
                        scale,
                        quickStartExcludedAttachments()));
    }

    private static Set<String> quickStartExcludedAttachments() {
        Set<String> excluded =
                new java.util.LinkedHashSet<>(List.of("btn", "ksks"));
        for (int index = 2; index <= 28; index++) {
            excluded.add(String.format("btng/btng_%05d", index));
        }
        for (int index = 0; index <= 14; index++) {
            excluded.add(String.format("tx3_%05d", index));
        }
        excluded.add("guanga");
        return Set.copyOf(excluded);
    }

    private static void addStoreEffect(
            List<OriginalLobbyEffectSpec> specs,
            GameHomeV3Layout.Tile tile) {
        // Keep recovered home_icon_store plus the Canvas 商城 label as the single static base,
        // then restore the original animated gold/particle layer on top. Redrawing cc/zi would
        // duplicate that cart body and label; jb*/coin/guangdian/libao retain visible movement.
        float scale = 1.45f;
        specs.add(
                OriginalLobbyEffectSpec.overlayExcluding(
                        "zzb_jbdt_sc",
                        "animation",
                        185.0f + 29.2f * scale,
                        1585.0f + 103.5f * scale,
                        scale,
                        Set.of("cc", "zi")));
    }

}
