package com.huaque.ui.friend;

public interface FriendGateway extends AutoCloseable {
    void execute(FriendApiRequest request, FriendApiClient.Callback callback);

    @Override
    void close();
}
