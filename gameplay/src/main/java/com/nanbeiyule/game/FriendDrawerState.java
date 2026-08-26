package com.nanbeiyule.game;

import android.graphics.Bitmap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Mutable data state backing the friend drawer view. */
final class FriendDrawerState {
    static final int PAGE_SIZE = 20;
    static final long RECALL_OFFLINE_THRESHOLD_MS = 7L * 24 * 3600 * 1000;

    /** Drawer tabs matching the original lobby friend panel. */
    enum Tab {
        LIST,
        STARTING,
        RECALL
    }

    private final List<FriendEntry> friends = new ArrayList<>();
    private final Map<String, Bitmap> avatars = new HashMap<>();
    private Tab tab = Tab.LIST;
    private boolean hasMore;
    private int nextPage;
    private boolean loadingInitial;
    private boolean loadingMore;
    private int unreadApplications;
    private float scrollOffset;
    private java.util.function.LongPredicate inviteCooldownChecker;

    List<FriendEntry> friends() {
        return friends;
    }

    Tab tab() {
        return tab;
    }

    void setTab(Tab value) {
        tab = value == null ? Tab.LIST : value;
    }

    /** Friends offline long enough to be recall candidates. */
    List<FriendEntry> recallCandidates(long nowMillis) {
        List<FriendEntry> candidates = new ArrayList<>();
        for (FriendEntry friend : friends) {
            if (isRecallCandidate(friend, nowMillis)) {
                candidates.add(friend);
            }
        }
        return candidates;
    }

    static boolean isRecallCandidate(FriendEntry friend, long nowMillis) {
        if (friend.state() != FriendEntry.State.OFFLINE) {
            return false;
        }
        Long at = FriendEntry.lastActiveAtMillis(friend.lastActiveAt());
        return at == null || nowMillis - at >= RECALL_OFFLINE_THRESHOLD_MS;
    }

    /** Currently online friends, used by the invite-all shortcut. */
    List<FriendEntry> onlineFriends() {
        List<FriendEntry> online = new ArrayList<>();
        for (FriendEntry friend : friends) {
            if (friend.state() == FriendEntry.State.ONLINE
                    || friend.state() == FriendEntry.State.WAITING) {
                online.add(friend);
            }
        }
        return online;
    }

    Bitmap avatar(String avatarKey) {
        return avatarKey == null ? null : avatars.get(avatarKey);
    }

    void putAvatar(String avatarKey, Bitmap bitmap) {
        if (avatarKey != null
                && !avatarKey.isBlank()
                && bitmap != null
                && !bitmap.isRecycled()) {
            avatars.put(avatarKey, bitmap);
        }
    }

    boolean hasMore() {
        return hasMore;
    }

    boolean loadingInitial() {
        return loadingInitial;
    }

    boolean loadingMore() {
        return loadingMore;
    }

    int unreadApplications() {
        return unreadApplications;
    }

    void setUnreadApplications(int count) {
        unreadApplications = Math.max(0, count);
    }

    float scrollOffset() {
        return scrollOffset;
    }

    void beginInitialLoad() {
        resetPages();
        loadingInitial = true;
    }

    void loadFailed() {
        loadingInitial = false;
        loadingMore = false;
    }

    void resetPages() {
        friends.clear();
        hasMore = false;
        nextPage = 0;
        loadingInitial = false;
        loadingMore = false;
        scrollOffset = 0.0f;
    }

    void applyPage(FriendListPage page) {
        if (page.page() == 0) {
            friends.clear();
            scrollOffset = 0.0f;
        }
        friends.addAll(page.friends());
        hasMore = page.hasMore();
        // A reload fetches every loaded page in one request, so the next
        // page derives from the response size, not just the page index.
        nextPage =
                page.page()
                        + Math.max(
                                1,
                                (page.friends().size() + PAGE_SIZE - 1)
                                        / PAGE_SIZE);
        loadingInitial = false;
        loadingMore = false;
    }

    /** Avatar keys of loaded friends that are not cached yet. */
    List<String> missingAvatarKeys() {
        List<String> keys = new ArrayList<>();
        for (FriendEntry friend : friends) {
            String key = friend.avatarKey();
            if (key != null
                    && !key.isBlank()
                    && !avatars.containsKey(key)
                    && !keys.contains(key)) {
                keys.add(key);
            }
        }
        return keys;
    }

    int nextPage() {
        return nextPage;
    }

    /** Number of already loaded items, used when reloading every page. */
    int loadedCount() {
        return friends.size();
    }

    void markLoadingMore() {
        loadingMore = true;
    }

    void scrollBy(
            float delta, float viewportHeight, float itemHeight) {
        scrollOffset += delta;
        clampScroll(viewportHeight, itemHeight);
    }

    void clampScroll(float viewportHeight, float itemHeight) {
        scrollOffset =
                Math.min(
                        Math.max(0.0f, scrollOffset),
                        maxScrollOffset(viewportHeight, itemHeight));
    }

    float maxScrollOffset(float viewportHeight, float itemHeight) {
        return Math.max(
                0.0f, friends.size() * itemHeight - viewportHeight);
    }

    boolean shouldLoadMore(float viewportHeight, float itemHeight) {
        // Trigger once the second-to-last loaded item enters the
        // viewport, so the next page arrives before the user reaches
        // the end of the list.
        return hasMore
                && !loadingMore
                && !loadingInitial
                && scrollOffset
                        >= maxScrollOffset(viewportHeight, itemHeight)
                                - 2.0f * itemHeight;
    }

    /** Optional per-friend invite cooldown check wired by the host. */
    void setInviteCooldownChecker(
            java.util.function.LongPredicate checker) {
        inviteCooldownChecker = checker;
    }

    boolean inviteCoolingDown(long publicPlayerId) {
        return inviteCooldownChecker != null
                && inviteCooldownChecker.test(publicPlayerId);
    }
}
