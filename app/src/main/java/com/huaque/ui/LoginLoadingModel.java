package com.huaque.ui;

final class LoginLoadingModel {
    static final String LOGIN_LABEL = "正在登录";

    private LoginLoadingModel() {
    }

    static boolean isVisible(String phase, boolean authenticated) {
        return "VERIFYING".equals(phase) || authenticated;
    }

    static int nextProgress(int progress) {
        return progress >= 99 ? 0 : Math.max(0, progress + 1);
    }
}
