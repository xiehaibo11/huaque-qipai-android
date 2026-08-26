package com.nanbeiyule.game;

/** Original ImageTextTutorial.Config state for Dark Shuangkou game 30579. */
final class GameRuleTutorialModel {
    enum Next { PAGE_CHANGED, START_GAME }

    static final long GAME_ID = 30579L;
    static final int PAGE_COUNT = 4;
    private int pageIndex;

    int pageIndex() { return pageIndex; }
    boolean isLastPage() { return pageIndex == PAGE_COUNT - 1; }
    String pageResourceName() { return "game_rule_tutorial_30579_" + (pageIndex + 1); }

    Next next() {
        if (isLastPage()) return Next.START_GAME;
        pageIndex++;
        return Next.PAGE_CHANGED;
    }

    void previous() {
        pageIndex = Math.max(0, pageIndex - 1);
    }

    void select(int index) {
        pageIndex = Math.max(0, Math.min(index, PAGE_COUNT - 1));
    }

    void reset() {
        pageIndex = 0;
    }
}
