package com.nanbeiyule.game;

import java.net.URLEncoder;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Authenticated first-party client for the friend (牌友) API. Only
 * endpoint shapes live here; transport concerns sit in
 * {@link FriendApiTransport}.
 */
final class FriendApiClient {
    private final FriendApiTransport transport;

    FriendApiClient(FriendApiTransport.MessageResolver messages) {
        this(BuildConfig.API_BASE_URL, messages);
    }

    FriendApiClient(
            String baseUrl, FriendApiTransport.MessageResolver messages) {
        transport = new FriendApiTransport(baseUrl, messages);
    }

    void shutdown() {
        transport.shutdown();
    }

    void listFriends(
            String accessToken,
            int page,
            int size,
            FriendApiTransport.ResultCallback<FriendListPage> callback) {
        transport.request(
                "GET",
                "/api/v1/friends?page=" + page + "&size=" + size,
                accessToken,
                null,
                FriendApiProtocol::parseListPage,
                callback);
    }

    void search(
            String accessToken,
            String query,
            FriendApiTransport.ResultCallback<FriendSearchResult>
                    callback) {
        String encoded;
        try {
            encoded =
                    URLEncoder.encode(
                            query == null ? "" : query.trim(), "UTF-8");
        } catch (java.io.UnsupportedEncodingException exception) {
            encoded = "";
        }
        transport.request(
                "GET",
                "/api/v1/friends/search?query=" + encoded,
                accessToken,
                null,
                FriendApiProtocol::parseSearchResult,
                callback);
    }

    void apply(
            String accessToken,
            long publicPlayerId,
            FriendApiTransport.ResultCallback<Void> callback) {
        post(
                "/api/v1/friends/applications",
                accessToken,
                body(() -> FriendApiProtocol.applyBody(publicPlayerId)),
                callback);
    }

    void applications(
            String accessToken,
            FriendApiTransport.ResultCallback<FriendApplicationsPage>
                    callback) {
        transport.request(
                "GET",
                "/api/v1/friends/applications",
                accessToken,
                null,
                FriendApiProtocol::parseApplications,
                callback);
    }

    void accept(
            String accessToken,
            long publicPlayerId,
            FriendApiTransport.ResultCallback<Void> callback) {
        post(
                "/api/v1/friends/applications/by-player/"
                        + publicPlayerId
                        + "/accept",
                accessToken,
                null,
                callback);
    }

    void reject(
            String accessToken,
            long publicPlayerId,
            FriendApiTransport.ResultCallback<Void> callback) {
        post(
                "/api/v1/friends/applications/by-player/"
                        + publicPlayerId
                        + "/reject",
                accessToken,
                null,
                callback);
    }

    void deleteFriend(
            String accessToken,
            long publicPlayerId,
            FriendApiTransport.ResultCallback<Void> callback) {
        transport.request(
                "DELETE",
                "/api/v1/friends/" + publicPlayerId,
                accessToken,
                null,
                FriendApiTransport.VOID_PARSER,
                callback);
    }

    void shield(
            String accessToken,
            long publicPlayerId,
            boolean shielded,
            FriendApiTransport.ResultCallback<Void> callback) {
        transport.request(
                "PUT",
                "/api/v1/friends/" + publicPlayerId + "/shield",
                accessToken,
                body(() -> FriendApiProtocol.shieldBody(shielded)),
                FriendApiTransport.VOID_PARSER,
                callback);
    }

    void invite(
            String accessToken,
            long publicPlayerId,
            String type,
            FriendApiTransport.ResultCallback<Void> callback) {
        post(
                "/api/v1/friends/" + publicPlayerId + "/invite",
                accessToken,
                body(() -> FriendApiProtocol.inviteBody(type)),
                callback);
    }

    void inviteAll(
            String accessToken,
            FriendApiTransport.ResultCallback<FriendInviteAllResult>
                    callback) {
        transport.request(
                "POST",
                "/api/v1/friends/invite-all",
                accessToken,
                null,
                FriendApiProtocol::parseInviteAllResult,
                callback);
    }

    void notifications(
            String accessToken,
            boolean unreadOnly,
            FriendApiTransport.ResultCallback<FriendNotificationsPage>
                    callback) {
        transport.request(
                "GET",
                "/api/v1/friends/notifications"
                        + (unreadOnly ? "?unread=true" : ""),
                accessToken,
                null,
                FriendApiProtocol::parseNotifications,
                callback);
    }

    void markAllRead(
            String accessToken,
            FriendApiTransport.ResultCallback<Void> callback) {
        post(
                "/api/v1/friends/notifications/read",
                accessToken,
                null,
                callback);
    }

    private void post(
            String path,
            String accessToken,
            JSONObject requestBody,
            FriendApiTransport.ResultCallback<Void> callback) {
        transport.request(
                "POST",
                path,
                accessToken,
                requestBody,
                FriendApiTransport.VOID_PARSER,
                callback);
    }

    private static JSONObject body(BodyBuilder builder) {
        try {
            return builder.build();
        } catch (JSONException exception) {
            return null;
        }
    }

    @FunctionalInterface
    private interface BodyBuilder {
        JSONObject build() throws JSONException;
    }
}
