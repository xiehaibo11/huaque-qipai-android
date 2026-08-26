package com.nanbeiyule.game;

/** Items in the Zhejiang lobby bottom "更多" expanded bar. */
enum MoreMenuItem {
    ANNOUNCEMENT("公告"),
    HEALTH_NOTICE("健康须知"),
    RULES("规则"),
    SETTINGS("设置"),
    ZHEJIANG_NEWS("浙江新闻"),
    WECHAT_PUBLIC("公众号"),
    SCORE_BOX("麻将计分器");

    private final String label;

    MoreMenuItem(String label) {
        this.label = label;
    }

    String label() {
        return label;
    }
}
