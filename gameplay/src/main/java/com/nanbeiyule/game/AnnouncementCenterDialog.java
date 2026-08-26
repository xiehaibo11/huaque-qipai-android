package com.nanbeiyule.game;

import android.content.Context;

/** Public native host for the authenticated Zhejiang announcement center. */
public final class AnnouncementCenterDialog extends TaizhouFullscreenDialog {
    public interface Actions {
        void onRetryRequested();

        void onAnnouncementRequested(long announcementId);

        void onPageRequested(String pageUrl);

        default void onActivityRequested() {}

        default void onAwardCenterRequested() {}
    }

    private final AnnouncementCenterView content;

    public AnnouncementCenterDialog(Context context, Actions actions) {
        this(context, actions, new DismissHolder());
    }

    private AnnouncementCenterDialog(Context context, Actions actions, DismissHolder holder) {
        this(context, new AnnouncementCenterView(context, forwarding(actions, holder)), holder);
    }

    private AnnouncementCenterDialog(
            Context context, AnnouncementCenterView content, DismissHolder holder) {
        super(context, content, false);
        this.content = content;
        holder.action = this::dismiss;
    }

    public void setButtonClickSound(Runnable sound) {
        content.setButtonClickSound(sound);
    }

    AnnouncementCenterState state() {
        return content.state();
    }

    void beginPageLoad() {
        content.beginPageLoad();
    }

    void showPage(AnnouncementApiProtocol.AnnouncementPage page) {
        content.showPage(page);
    }

    void showPageError(String message) {
        content.showPageError(message);
    }

    boolean beginDetailLoad(long announcementId) {
        return content.beginDetailLoad(announcementId);
    }

    void showDetail(AnnouncementApiProtocol.AnnouncementDetail detail) {
        content.showDetail(detail);
    }

    void showDetailError(long announcementId, String message) {
        content.showDetailError(announcementId, message);
    }

    private static AnnouncementCenterView.Actions forwarding(
            Actions actions, DismissHolder holder) {
        if (actions == null) {
            throw new IllegalArgumentException("actions must not be null");
        }
        return new AnnouncementCenterView.Actions() {
            @Override
            public void onDismissRequested() {
                holder.action.run();
            }

            @Override
            public void onRetryRequested() {
                actions.onRetryRequested();
            }

            @Override
            public void onAnnouncementRequested(long announcementId) {
                actions.onAnnouncementRequested(announcementId);
            }

            @Override
            public void onPageRequested(String pageUrl) {
                actions.onPageRequested(pageUrl);
            }

            @Override
            public void onActivityRequested() {
                holder.action.run();
                actions.onActivityRequested();
            }

            @Override
            public void onAwardCenterRequested() {
                actions.onAwardCenterRequested();
            }
        };
    }

    private static final class DismissHolder {
        private Runnable action = () -> {};
    }
}
