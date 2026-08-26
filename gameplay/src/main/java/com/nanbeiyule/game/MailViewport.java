package com.nanbeiyule.game;

/** Responsive viewport: the scenery fills the window; interactive artwork stays proportional. */
final class MailViewport {
    private final int backgroundWidth;
    private final int backgroundHeight;
    private final MailLayout.Transform content;

    private MailViewport(
            int backgroundWidth,
            int backgroundHeight,
            MailLayout.Transform content) {
        this.backgroundWidth = backgroundWidth;
        this.backgroundHeight = backgroundHeight;
        this.content = content;
    }

    static MailViewport fullBleed(int width, int height) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        return new MailViewport(
                safeWidth,
                safeHeight,
                MailLayout.Transform.contain(safeWidth, safeHeight));
    }

    int backgroundWidth() {
        return backgroundWidth;
    }

    int backgroundHeight() {
        return backgroundHeight;
    }

    MailLayout.Transform content() {
        return content;
    }
}
