package com.nanbeiyule.game;

enum LoginAgreementLink {
    SERVICE(
            "服务协议",
            "https://www.nanbeiyule.com/terms"),
    GUARDIANSHIP(
            "家长监护工程",
            "https://www.nanbeiyule.com/guardianship"),
    PRIVACY(
            "隐私政策",
            "https://www.nanbeiyule.com/privacy");

    private final String title;
    private final String url;

    LoginAgreementLink(String title, String url) {
        this.title = title;
        this.url = url;
    }

    String title() {
        return title;
    }

    String url() {
        return url;
    }
}
