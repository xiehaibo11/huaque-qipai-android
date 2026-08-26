package com.nanbeiyule.game;

interface TaizhouRoomToolsTransport {
    interface Callback<T> {
        void onSuccess(T result);
        void onUnauthorized();
        void onError(String message);
    }

    void loadState(String token, String roomNumber, Callback<TaizhouRoomToolsState> callback);

    void setReservation(
            String token,
            String roomNumber,
            String idempotencyKey,
            TaizhouRoomToolType type,
            boolean active,
            Callback<TaizhouRoomToolsState.Reservation> callback);

    void sendMessage(
            String token,
            String roomNumber,
            String idempotencyKey,
            String type,
            int contentIndex,
            Callback<TaizhouRoomToolsState.Message> callback);

    void sendVoice(
            String token,
            String roomNumber,
            String idempotencyKey,
            int durationMillis,
            byte[] data,
            Callback<TaizhouRoomToolsState.Message> callback);

    void loadVoice(
            String token,
            String roomNumber,
            String messageId,
            Callback<byte[]> callback);

    void cancelPending();
    void shutdown();
}
