package com.huaque.ui.friend;

import java.util.List;

final class FriendPanelUpcomingState {
    enum Filter {
        ALL_ROOMS("所有房间"),
        MATCH_ARENA("比赛场");

        final String label;

        Filter(String label) {
            this.label = label;
        }
    }

    static final class Option {
        final Filter filter;
        final String label;

        Option(Filter filter) {
            this.filter = filter;
            label = filter.label;
        }
    }

    private static final List<Option> FILTER_OPTIONS = List.of(
            new Option(Filter.ALL_ROOMS), new Option(Filter.MATCH_ARENA));

    private boolean guideSeen;
    private boolean guideVisible;
    private boolean filterListVisible;
    private Filter selected = Filter.ALL_ROOMS;

    FriendPanelUpcomingState(boolean guideSeen) {
        this.guideSeen = guideSeen;
    }

    void enterUpcoming() {
        guideVisible = !guideSeen;
        filterListVisible = false;
    }

    void tapFilter() {
        guideSeen = true;
        guideVisible = false;
        filterListVisible = true;
    }

    void dismissFilter() {
        filterListVisible = false;
    }

    void select(Filter filter) {
        selected = filter;
        filterListVisible = false;
    }

    boolean hasSeenGuide() {
        return guideSeen;
    }

    boolean isGuideVisible() {
        return guideVisible;
    }

    boolean isFilterListVisible() {
        return filterListVisible;
    }

    String selectedName() {
        return selected.label;
    }

    Filter selectedFilter() {
        return selected;
    }

    List<Option> filterOptions() {
        return FILTER_OPTIONS;
    }
}
