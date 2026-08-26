package com.huaque.ui.friend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class FriendJsonTest {
    @Test
    public void parsesCanonicalFriendPage() {
        String json = "{\"page\":0,\"size\":20,\"hasMore\":true,\"friends\":[{"
                + "\"publicPlayerId\":123,\"displayName\":\"阿青\",\"avatarKey\":\"a.png\","
                + "\"state\":\"ONLINE\",\"lastActiveAt\":\"2026-08-23T12:00:00Z\","
                + "\"shielded\":false}]}";

        FriendData.Page page = FriendJson.parsePage(json);

        assertTrue(page.hasMore);
        assertEquals(1, page.friends.size());
        FriendData.Entry friend = page.friends.get(0);
        assertEquals(123L, friend.publicPlayerId);
        assertEquals("阿青", friend.displayName);
        assertEquals(FriendData.Presence.ONLINE, friend.presence);
        assertFalse(friend.shielded);
    }

    @Test
    public void parsesOriginalProtocolAliases() {
        String json = "{\"cur_package\":0,\"total_package\":1,\"friendInfo\":[{"
                + "\"numid\":456,\"nickname\":\"牌友\",\"headurl\":\"head.png\","
                + "\"player_state\":2,\"last_login_time\":99,\"shieldState\":1}]}";

        FriendData.Entry friend = FriendJson.parsePage(json).friends.get(0);

        assertEquals(456L, friend.publicPlayerId);
        assertEquals("牌友", friend.displayName);
        assertEquals(FriendData.Presence.GAMING, friend.presence);
        assertEquals(99L, friend.lastActiveEpochSeconds);
        assertTrue(friend.shielded);
    }

    @Test
    public void parsesSearchApplicationsNotificationsAndInviteAll() {
        FriendData.SearchResult search = FriendJson.parseSearch(
                "{\"publicPlayerId\":7,\"displayName\":\"小七\",\"relation\":\"NONE\"}");
        FriendData.Application application = FriendJson.parseApplications(
                "{\"applications\":[{\"id\":\"app-1\",\"publicPlayerId\":8,"
                        + "\"displayName\":\"小八\"}]}").get(0);
        FriendData.Notification notification = FriendJson.parseNotifications(
                "{\"notifications\":[{\"id\":\"notice-1\",\"type\":\"INVITE\","
                        + "\"actorPublicPlayerId\":9,\"actorDisplayName\":\"小九\"}]}").get(0);
        FriendData.InviteAllResult result = FriendJson.parseInviteAll(
                "{\"invitedCount\":2,\"cooldownSkippedCount\":1}");

        assertEquals(7L, search.publicPlayerId);
        assertEquals("NONE", search.relation);
        assertEquals("app-1", application.id);
        assertEquals(8L, application.publicPlayerId);
        assertEquals("notice-1", notification.id);
        assertEquals("INVITE", notification.type);
        assertEquals(2, result.invitedCount);
        assertEquals(1, result.cooldownSkippedCount);
    }
}
