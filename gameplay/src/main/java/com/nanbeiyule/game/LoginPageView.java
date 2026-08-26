package com.nanbeiyule.game;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/** Composes the login page, agreement hint, and topmost request-loading layer. */
public final class LoginPageView extends FrameLayout {
    private final LoginView loginView;
    private final LoginAgreementHintView hintView;
    private final LoginRequestLoadingView loadingView;

    public LoginPageView(Context context) {
        super(context);
        loginView = new LoginView(context);
        hintView = new LoginAgreementHintView(context);
        loadingView = new LoginRequestLoadingView(context);

        LayoutParams fullScreen =
                new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        LayoutParams hintFullScreen =
                new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        addView(loginView, fullScreen);
        addView(hintView, hintFullScreen);
        addView(loadingView, fullScreen);

        loginView.setOnAgreementCheckedChangeListener(
                new LoginView.OnAgreementCheckedChangeListener() {
                    @Override
                    public void onAgreementCheckedChanged(boolean checked) {
                        hintView.setHintVisible(!checked);
                    }
                });
        hintView.setHintVisible(!loginView.isAgreementChecked());
    }

    LoginView loginView() {
        return loginView;
    }

    LoginRequestLoadingView loadingView() {
        return loadingView;
    }
}
