package com.huaque.ui.friend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public final class FriendPanelControllerTest {
    @Test
    public void unauthenticatedOpenExplainsHowToContinue() {
        FakeView view = new FakeView();
        FriendPanelController controller = new FriendPanelController("", new FakeGateway(), view);

        controller.open();

        assertTrue(view.authRequired);
    }

    @Test
    public void friendOperationsRefreshTheCorrectContent() {
        FakeGateway gateway = new FakeGateway();
        FakeView view = new FakeView();
        FriendPanelController controller = new FriendPanelController("token", gateway, view);

        controller.open();
        assertEquals("/api/v1/friends?page=0&size=20", gateway.last.path);
        gateway.complete(200, "{\"friends\":[{\"publicPlayerId\":7,\"displayName\":\"小七\","
                + "\"state\":\"ONLINE\",\"shielded\":false}]}");
        assertEquals(1, view.friends.size());

        controller.search("88");
        gateway.complete(200, "{\"publicPlayerId\":8,\"displayName\":\"小八\",\"relation\":\"NONE\"}");
        assertEquals(8L, view.search.publicPlayerId);

        controller.apply(8);
        gateway.complete(202, "");
        assertEquals("牌友申请已发送", view.message);

        controller.invite(7);
        gateway.complete(429, "{}");
        assertEquals("操作太频繁，请稍后再试", view.message);
    }

    @Test
    public void upcomingLoadsApplicationsThenUnreadNotifications() {
        FakeGateway gateway = new FakeGateway();
        FakeView view = new FakeView();
        FriendPanelController controller = new FriendPanelController("token", gateway, view);

        controller.loadUpcoming();
        gateway.complete(200, "{\"applications\":[{\"id\":\"a1\",\"publicPlayerId\":9,"
                + "\"displayName\":\"小九\"}]}");
        assertEquals("/api/v1/friends/notifications?unread=true", gateway.last.path);
        gateway.complete(200, "{\"notifications\":[{\"id\":\"n1\",\"type\":\"INVITE\","
                + "\"actorPublicPlayerId\":10,\"actorDisplayName\":\"小十\"}]}");

        assertEquals(1, view.applications.size());
        assertEquals(1, view.notifications.size());
    }

    @Test
    public void unauthorizedResponseEntersSharedSessionRecovery() {
        FakeGateway gateway = new FakeGateway();
        FakeView view = new FakeView();
        boolean[] unauthorized = {false};
        FriendPanelController controller = new FriendPanelController(
                "token", gateway, view, () -> unauthorized[0] = true);

        controller.open();
        gateway.complete(401, "{}");

        assertTrue(unauthorized[0]);
    }

    private static final class FakeGateway implements FriendGateway {
        FriendApiRequest last;
        FriendApiClient.Callback callback;

        @Override public void execute(FriendApiRequest request, FriendApiClient.Callback callback) {
            this.last = request;
            this.callback = callback;
        }

        void complete(int status, String body) {
            FriendApiClient.Callback current = callback;
            callback = null;
            current.onComplete(new FriendApiClient.Response(status, body, null));
        }

        @Override public void close() {}
    }

    private static final class FakeView implements FriendPanelController.View {
        boolean authRequired;
        String message = "";
        List<FriendData.Entry> friends = new ArrayList<>();
        List<FriendData.Application> applications = new ArrayList<>();
        List<FriendData.Notification> notifications = new ArrayList<>();
        FriendData.SearchResult search;

        @Override public void setLoading(boolean loading) {}
        @Override public void showAuthenticationRequired() { authRequired = true; }
        @Override public void showFriends(List<FriendData.Entry> friends) { this.friends = friends; }
        @Override public void showUpcoming(List<FriendData.Application> applications,
                List<FriendData.Notification> notifications) {
            this.applications = applications;
            this.notifications = notifications;
        }
        @Override public void showSearchResult(FriendData.SearchResult result) { search = result; }
        @Override public void showMessage(String message) { this.message = message; }
    }
}
