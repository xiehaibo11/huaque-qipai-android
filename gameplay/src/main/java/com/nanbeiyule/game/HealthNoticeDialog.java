package com.nanbeiyule.game;

import android.content.Context;

/** Public native entry point for the recovered Zhejiang health notice. */
public final class HealthNoticeDialog extends TaizhouFullscreenDialog {
    private final HealthNoticeView noticeView;

    public HealthNoticeDialog(Context context) {
        this(context, new HealthNoticeView(context));
    }

    private HealthNoticeDialog(Context context, HealthNoticeView noticeView) {
        super(context, noticeView, false);
        this.noticeView = noticeView;
        noticeView.setOnDismissRequested(this::dismiss);
    }

    public void setButtonClickSound(Runnable sound) {
        noticeView.setButtonClickSound(sound);
    }
}
