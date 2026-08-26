package com.nanbeiyule.game;

import android.content.Context;

/** Full-screen host for the original GameRuleLayer and its official rule WebView. */
final class GameRuleDialog extends TaizhouFullscreenDialog {
    interface Actions extends GameRuleView.Actions {}

    private final GameRuleView content;

    GameRuleDialog(Context context, Actions actions) {
        this(context, actions, new DismissHolder());
    }

    private GameRuleDialog(Context context, Actions actions, DismissHolder holder) {
        this(context, new GameRuleView(context, forwarding(actions, holder)), holder);
    }

    private GameRuleDialog(Context context, GameRuleView content, DismissHolder holder) {
        super(context, content, false);
        this.content = content;
        holder.dismiss = this::dismiss;
    }

    void setClickSound(Runnable sound) {
        content.setClickSound(sound);
    }

    void release() {
        content.release();
    }

    private static GameRuleView.Actions forwarding(Actions actions, DismissHolder holder) {
        if (actions == null) throw new IllegalArgumentException("actions");
        return new GameRuleView.Actions() {
            @Override public void onDismissRequested() { holder.dismiss.run(); }
            @Override public void onGameSelected(GameRuleCatalog.Entry entry) {
                actions.onGameSelected(entry);
            }
            @Override public void onImageTutorialRequested(long gameId) {
                actions.onImageTutorialRequested(gameId);
            }
            @Override public void onTutorialStartGameRequested(long gameId) {
                actions.onTutorialStartGameRequested(gameId);
            }
        };
    }

    private static final class DismissHolder {
        private Runnable dismiss = () -> {};
    }
}
