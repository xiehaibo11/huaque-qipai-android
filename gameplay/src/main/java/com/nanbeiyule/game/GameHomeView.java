package com.nanbeiyule.game;

import android.content.Context;

/** Public game-home view facade backed by focused rendering and interaction layers. */
public final class GameHomeView extends GameHomeInteractionView {
    public GameHomeView(Context context, GameHomeState state) {
        super(context, state);
    }

    public GameHomeView(
            Context context,
            GameHomeState state,
            boolean drawBackgroundEnabled) {
        super(context, state, drawBackgroundEnabled);
    }
}
