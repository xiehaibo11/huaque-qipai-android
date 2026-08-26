package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.FrameLayout;

@SuppressLint("ViewConstructor")
final class GameHomeSceneLayout extends FrameLayout {
    private final LoginRequestLoadingView loadingView;

    GameHomeSceneLayout(Context context, GameHomeView controls) {
        super(context);
        LayoutParams full =
                new LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT);
        GameHomeBackgroundView backgroundView =
                new GameHomeBackgroundView(context);
        addView(backgroundView, full);
        addView(controls, full);
        // QuickStartBtn.csb mounts zzb_jbdt_ksks on its _ani child. The transparent effect layer
        // follows the same GameHomeV3Layout quick-start tile as the static button and sits above
        // controls without introducing a second lobby backdrop.
        addView(
                new OriginalLobbyEffectView(
                        context, OriginalLobbyEffectLayout.frontLayerSpecs()),
                full);
        OriginalLobbyTapEffectView tapEffectView =
                new OriginalLobbyTapEffectView(context);
        controls.setTapEffectPlayer(tapEffectView::playAt);
        addView(tapEffectView, full);
        loadingView = new LoginRequestLoadingView(context);
        addView(loadingView, full);
    }

    LoginRequestLoadingView loadingView() {
        return loadingView;
    }
}
