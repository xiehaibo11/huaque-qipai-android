package com.nanbeiyule.game;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import java.util.Objects;

/** Public controller used by either lobby host without duplicating announcement behavior. */
public final class AnnouncementCenterController implements AutoCloseable {
    public interface TokenProvider {
        String accessToken();
    }

    public interface Listener {
        void onUnauthorized();

        void onMessage(String message);

        void onDismissed();

        default void onActivityRequested() {}

        default void onAwardCenterRequested() {}
    }

    private final Activity activity;
    private final TokenProvider tokenProvider;
    private final Listener listener;
    private final Runnable buttonClickSound;
    private final AnnouncementApiClient client;
    private AnnouncementCenterDialog dialog;
    private boolean closed;

    public AnnouncementCenterController(
            Activity activity,
            String apiBaseUrl,
            TokenProvider tokenProvider,
            Listener listener,
            Runnable buttonClickSound) {
        this.activity = Objects.requireNonNull(activity, "activity");
        this.tokenProvider = Objects.requireNonNull(tokenProvider, "tokenProvider");
        this.listener = Objects.requireNonNull(listener, "listener");
        this.buttonClickSound = buttonClickSound == null ? () -> {} : buttonClickSound;
        client = new AnnouncementApiClient(apiBaseUrl);
    }

    public void show() {
        if (closed || activity.isFinishing() || dialog != null) {
            return;
        }
        AnnouncementCenterDialog opened =
                new AnnouncementCenterDialog(activity, new DialogActions());
        dialog = opened;
        opened.setButtonClickSound(buttonClickSound);
        opened.setOnDismissListener(
                ignored -> {
                    if (dialog == opened) {
                        dialog = null;
                        listener.onDismissed();
                    }
                });
        opened.show();
        loadList(opened);
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void close() {
        closed = true;
        dismiss();
        client.shutdown();
    }

    private void loadList(AnnouncementCenterDialog expected) {
        expected.beginPageLoad();
        client.loadList(
                token(),
                new Callback<>(expected) {
                    @Override
                    public void onSuccess(AnnouncementApiProtocol.AnnouncementPage result) {
                        if (active()) {
                            expected.showPage(result);
                        }
                    }

                    @Override
                    void requestFailed(String message) {
                        if (active()) {
                            expected.showPageError(message);
                        }
                    }
                });
    }

    private void loadDetail(long announcementId) {
        AnnouncementCenterDialog expected = dialog;
        if (expected == null || !expected.beginDetailLoad(announcementId)) {
            return;
        }
        client.loadDetail(
                token(),
                announcementId,
                new Callback<>(expected) {
                    @Override
                    public void onSuccess(AnnouncementApiProtocol.AnnouncementDetail result) {
                        if (active()) {
                            expected.showDetail(result);
                        }
                    }

                    @Override
                    void requestFailed(String message) {
                        if (active()) {
                            expected.showDetailError(announcementId, message);
                        }
                    }
                });
    }

    private void openPage(String pageUrl) {
        if (!AnnouncementPageUrlPolicy.isSafe(pageUrl)) {
            listener.onMessage("公告链接不安全，已拒绝打开");
            return;
        }
        try {
            activity.startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse(pageUrl))
                            .addCategory(Intent.CATEGORY_BROWSABLE));
        } catch (ActivityNotFoundException exception) {
            listener.onMessage("未找到可打开公告网页的应用");
        }
    }

    private String token() {
        String value = tokenProvider.accessToken();
        return value == null ? "" : value;
    }

    private final class DialogActions implements AnnouncementCenterDialog.Actions {
        @Override
        public void onRetryRequested() {
            if (dialog != null) {
                loadList(dialog);
            }
        }

        @Override
        public void onAnnouncementRequested(long announcementId) {
            loadDetail(announcementId);
        }

        @Override
        public void onPageRequested(String pageUrl) {
            openPage(pageUrl);
        }

        @Override
        public void onActivityRequested() {
            dismiss();
            listener.onActivityRequested();
        }

        @Override
        public void onAwardCenterRequested() {
            listener.onAwardCenterRequested();
        }
    }

    private abstract class Callback<T> implements AnnouncementApiClient.Callback<T> {
        private final AnnouncementCenterDialog expected;

        Callback(AnnouncementCenterDialog expected) {
            this.expected = expected;
        }

        final boolean active() {
            return !closed && dialog == expected;
        }

        @Override
        public void onUnauthorized() {
            if (!active()) {
                return;
            }
            dismiss();
            listener.onUnauthorized();
        }

        @Override
        public void onError(String message) {
            if (active()) {
                requestFailed(message);
            }
        }

        void requestFailed(String message) {
            listener.onMessage(message);
        }
    }
}
