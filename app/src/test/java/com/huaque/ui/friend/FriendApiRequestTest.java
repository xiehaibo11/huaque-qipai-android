package com.huaque.ui.friend;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class FriendApiRequestTest {
    @Test
    public void buildsEveryFriendEndpointExactly() {
        assertRequest("GET", "/api/v1/friends?page=0&size=20", "", FriendApiRequest.list());
        assertRequest("GET", "/api/v1/friends/search?query=138%20001", "",
                FriendApiRequest.search("138 001"));
        assertRequest("POST", "/api/v1/friends/applications", "{\"publicPlayerId\":42}",
                FriendApiRequest.apply(42));
        assertRequest("GET", "/api/v1/friends/applications", "", FriendApiRequest.applications());
        assertRequest("POST", "/api/v1/friends/applications/a-1/accept", "{}",
                FriendApiRequest.accept("a-1"));
        assertRequest("POST", "/api/v1/friends/applications/a-1/reject", "{}",
                FriendApiRequest.reject("a-1"));
        assertRequest("DELETE", "/api/v1/friends/42", "", FriendApiRequest.remove(42));
        assertRequest("PUT", "/api/v1/friends/42/shield", "{\"shielded\":true}",
                FriendApiRequest.shield(42, true));
        assertRequest("POST", "/api/v1/friends/42/invite", "{\"type\":\"INVITE\"}",
                FriendApiRequest.invite(42));
        assertRequest("POST", "/api/v1/friends/invite-all", "{}", FriendApiRequest.inviteAll());
        assertRequest("GET", "/api/v1/friends/notifications?unread=true", "",
                FriendApiRequest.notifications());
        assertRequest("POST", "/api/v1/friends/notifications/read", "{}",
                FriendApiRequest.readNotifications());
    }

    private static void assertRequest(String method, String path, String body,
            FriendApiRequest request) {
        assertEquals(method, request.method);
        assertEquals(path, request.path);
        assertEquals(body, request.body);
    }
}
