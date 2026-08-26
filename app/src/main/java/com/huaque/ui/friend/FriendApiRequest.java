package com.huaque.ui.friend;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class FriendApiRequest {
    public final String method;
    public final String path;
    public final String body;

    private FriendApiRequest(String method, String path, String body) {
        this.method = method;
        this.path = path;
        this.body = body;
    }

    public static FriendApiRequest list() {
        return new FriendApiRequest("GET", "/api/v1/friends?page=0&size=20", "");
    }

    public static FriendApiRequest search(String query) {
        String encoded = URLEncoder.encode(query == null ? "" : query, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return new FriendApiRequest("GET", "/api/v1/friends/search?query=" + encoded, "");
    }

    public static FriendApiRequest apply(long publicPlayerId) {
        return new FriendApiRequest("POST", "/api/v1/friends/applications",
                "{\"publicPlayerId\":" + publicPlayerId + "}");
    }

    public static FriendApiRequest applications() {
        return new FriendApiRequest("GET", "/api/v1/friends/applications", "");
    }

    public static FriendApiRequest accept(String id) {
        return new FriendApiRequest("POST", "/api/v1/friends/applications/" + id + "/accept", "{}");
    }

    public static FriendApiRequest reject(String id) {
        return new FriendApiRequest("POST", "/api/v1/friends/applications/" + id + "/reject", "{}");
    }

    public static FriendApiRequest remove(long publicPlayerId) {
        return new FriendApiRequest("DELETE", "/api/v1/friends/" + publicPlayerId, "");
    }

    public static FriendApiRequest shield(long publicPlayerId, boolean shielded) {
        return new FriendApiRequest("PUT", "/api/v1/friends/" + publicPlayerId + "/shield",
                "{\"shielded\":" + shielded + "}");
    }

    public static FriendApiRequest invite(long publicPlayerId) {
        return new FriendApiRequest("POST", "/api/v1/friends/" + publicPlayerId + "/invite",
                "{\"type\":\"INVITE\"}");
    }

    public static FriendApiRequest inviteAll() {
        return new FriendApiRequest("POST", "/api/v1/friends/invite-all", "{}");
    }

    public static FriendApiRequest notifications() {
        return new FriendApiRequest("GET", "/api/v1/friends/notifications?unread=true", "");
    }

    public static FriendApiRequest readNotifications() {
        return new FriendApiRequest("POST", "/api/v1/friends/notifications/read", "{}");
    }
}
