package com.huaque.ui.friend;

import java.util.ArrayList;
import java.util.List;

public final class FriendPanelController implements AutoCloseable {
    public interface View {
        void setLoading(boolean loading);
        void showAuthenticationRequired();
        void showFriends(List<FriendData.Entry> friends);
        void showUpcoming(List<FriendData.Application> applications,
                List<FriendData.Notification> notifications);
        void showSearchResult(FriendData.SearchResult result);
        void showMessage(String message);
    }

    private final String accessToken;
    private final FriendGateway gateway;
    private final View view;
    private final Runnable unauthorized;
    private List<FriendData.Application> pendingApplications = new ArrayList<>();

    public FriendPanelController(String accessToken, FriendGateway gateway, View view) {
        this(accessToken, gateway, view, () -> {});
    }

    public FriendPanelController(
            String accessToken,
            FriendGateway gateway,
            View view,
            Runnable unauthorized) {
        this.accessToken = accessToken == null ? "" : accessToken;
        this.gateway = gateway;
        this.view = view;
        this.unauthorized = unauthorized == null ? () -> {} : unauthorized;
    }

    public void open() {
        if (!authenticated()) {
            view.showAuthenticationRequired();
            return;
        }
        loadFriends();
    }

    public void loadFriends() {
        execute(FriendApiRequest.list(), response ->
                view.showFriends(FriendJson.parsePage(response.body).friends));
    }

    public void loadUpcoming() {
        execute(FriendApiRequest.applications(), response -> {
            pendingApplications = FriendJson.parseApplications(response.body);
            execute(FriendApiRequest.notifications(), notificationResponse ->
                    view.showUpcoming(pendingApplications,
                            FriendJson.parseNotifications(notificationResponse.body)));
        });
    }

    public void search(String query) {
        if (query == null || query.trim().isEmpty()) {
            view.showMessage("请输入玩家序号");
            return;
        }
        execute(FriendApiRequest.search(query.trim()), response ->
                view.showSearchResult(FriendJson.parseSearch(response.body)));
    }

    public void apply(long publicPlayerId) {
        execute(FriendApiRequest.apply(publicPlayerId), response ->
                view.showMessage("牌友申请已发送"));
    }

    public void accept(String applicationId) {
        execute(FriendApiRequest.accept(applicationId), response -> loadUpcoming());
    }

    public void reject(String applicationId) {
        execute(FriendApiRequest.reject(applicationId), response -> loadUpcoming());
    }

    public void remove(long publicPlayerId) {
        execute(FriendApiRequest.remove(publicPlayerId), response -> loadFriends());
    }

    public void shield(long publicPlayerId, boolean shielded) {
        execute(FriendApiRequest.shield(publicPlayerId, shielded), response -> loadFriends());
    }

    public void invite(long publicPlayerId) {
        execute(FriendApiRequest.invite(publicPlayerId), response ->
                view.showMessage("邀请已发送"));
    }

    public void inviteAll() {
        execute(FriendApiRequest.inviteAll(), response -> {
            FriendData.InviteAllResult result = FriendJson.parseInviteAll(response.body);
            view.showMessage("已邀请 " + result.invitedCount + " 位在线牌友"
                    + (result.cooldownSkippedCount > 0
                    ? "，" + result.cooldownSkippedCount + " 位仍在冷却中" : ""));
        });
    }

    public void markNotificationsRead() {
        execute(FriendApiRequest.readNotifications(), response -> loadUpcoming());
    }

    private void execute(FriendApiRequest request, Success success) {
        if (!authenticated()) {
            view.showAuthenticationRequired();
            return;
        }
        view.setLoading(true);
        gateway.execute(request, response -> {
            view.setLoading(false);
            if (!response.isSuccessful()) {
                if (response.status == 401) {
                    unauthorized.run();
                    return;
                }
                view.showMessage(errorMessage(response));
                return;
            }
            try {
                success.accept(response);
            } catch (IllegalArgumentException error) {
                view.showMessage("牌友数据格式错误");
            }
        });
    }

    private boolean authenticated() {
        return !accessToken.isEmpty();
    }

    private static String errorMessage(FriendApiClient.Response response) {
        if (response.networkError != null) return "网络连接失败，请稍后重试";
        switch (response.status) {
            case 404: return "未找到该玩家";
            case 409: return "牌友申请已发送，请勿重复操作";
            case 429: return "操作太频繁，请稍后再试";
            default: return "牌友服务暂时不可用（" + response.status + "）";
        }
    }

    @Override
    public void close() {
        gateway.close();
    }

    private interface Success {
        void accept(FriendApiClient.Response response);
    }
}
