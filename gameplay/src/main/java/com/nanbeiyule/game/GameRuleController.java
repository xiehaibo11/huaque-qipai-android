package com.nanbeiyule.game;

import android.app.Activity;
import java.util.Objects;

/** Public lobby boundary for the recovered 18-game Zhejiang rule browser. */
public final class GameRuleController implements AutoCloseable {
    public interface Listener {
        default void onGameSelected(long gameId) {}
        default void onImageTutorialRequested(long gameId) {}
        default void onTutorialStartGameRequested(long gameId) {}
        void onDismissed();
    }

    private final Activity activity;
    private final Listener listener;
    private final Runnable clickSound;
    private GameRuleDialog dialog;
    private boolean closed;

    public GameRuleController(Activity activity, Listener listener, Runnable clickSound) {
        this.activity = Objects.requireNonNull(activity, "activity");
        this.listener = Objects.requireNonNull(listener, "listener");
        this.clickSound = clickSound == null ? () -> {} : clickSound;
    }

    public void show() {
        if (closed || dialog != null || activity.isFinishing() || activity.isDestroyed()) return;
        GameRuleDialog opened = new GameRuleDialog(activity, new GameRuleDialog.Actions() {
            @Override public void onDismissRequested() { openedDismiss(); }
            @Override public void onGameSelected(GameRuleCatalog.Entry entry) {
                listener.onGameSelected(entry.gameId());
            }
            @Override public void onImageTutorialRequested(long gameId) {
                listener.onImageTutorialRequested(gameId);
            }
            @Override public void onTutorialStartGameRequested(long gameId) {
                listener.onTutorialStartGameRequested(gameId);
            }
        });
        dialog = opened;
        opened.setClickSound(clickSound);
        opened.setOnDismissListener(ignored -> {
            if (dialog != opened) return;
            dialog = null;
            opened.release();
            listener.onDismissed();
        });
        opened.show();
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public void dismiss() {
        if (dialog != null) dialog.dismiss();
    }

    @Override
    public void close() {
        closed = true;
        dismiss();
    }

    private void openedDismiss() {
        if (dialog != null) dialog.dismiss();
    }
}
