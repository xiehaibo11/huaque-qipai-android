package com.huaque.ui.friend;

import java.util.Collections;
import java.util.List;

public final class FriendData {
    public enum Presence { ONLINE, GAMING, WAITING, OFFLINE }

    public static final class Entry {
        public final long publicPlayerId;
        public final String displayName;
        public final String avatarKey;
        public final Presence presence;
        public final long lastActiveEpochSeconds;
        public final boolean shielded;

        Entry(long publicPlayerId, String displayName, String avatarKey,
                Presence presence, long lastActiveEpochSeconds, boolean shielded) {
            this.publicPlayerId = publicPlayerId;
            this.displayName = displayName;
            this.avatarKey = avatarKey;
            this.presence = presence;
            this.lastActiveEpochSeconds = lastActiveEpochSeconds;
            this.shielded = shielded;
        }
    }

    public static final class Page {
        public final List<Entry> friends;
        public final boolean hasMore;

        Page(List<Entry> friends, boolean hasMore) {
            this.friends = Collections.unmodifiableList(friends);
            this.hasMore = hasMore;
        }
    }

    public static final class SearchResult {
        public final long publicPlayerId;
        public final String displayName;
        public final String avatarKey;
        public final String relation;

        SearchResult(long publicPlayerId, String displayName, String avatarKey, String relation) {
            this.publicPlayerId = publicPlayerId;
            this.displayName = displayName;
            this.avatarKey = avatarKey;
            this.relation = relation;
        }
    }

    public static final class Application {
        public final String id;
        public final long publicPlayerId;
        public final String displayName;
        public final String avatarKey;

        Application(String id, long publicPlayerId, String displayName, String avatarKey) {
            this.id = id;
            this.publicPlayerId = publicPlayerId;
            this.displayName = displayName;
            this.avatarKey = avatarKey;
        }
    }

    public static final class Notification {
        public final String id;
        public final long actorPublicPlayerId;
        public final String actorDisplayName;
        public final String type;

        Notification(String id, long actorPublicPlayerId, String actorDisplayName, String type) {
            this.id = id;
            this.actorPublicPlayerId = actorPublicPlayerId;
            this.actorDisplayName = actorDisplayName;
            this.type = type;
        }
    }

    public static final class InviteAllResult {
        public final int invitedCount;
        public final int cooldownSkippedCount;

        InviteAllResult(int invitedCount, int cooldownSkippedCount) {
            this.invitedCount = invitedCount;
            this.cooldownSkippedCount = cooldownSkippedCount;
        }
    }

    private FriendData() {}
}
