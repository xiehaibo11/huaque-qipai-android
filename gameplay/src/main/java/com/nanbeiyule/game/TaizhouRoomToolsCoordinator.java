package com.nanbeiyule.game;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

final class TaizhouRoomToolsCoordinator {
    interface Listener {
        void onState(TaizhouRoomToolsState state);
        void onLoginRequired();
        void onError(String message);
    }

    interface VoiceCallback {
        void onVoice(byte[] data);
        void onError(String message);
    }

    private final AuthSessionCoordinator auth;
    private final TaizhouRoomToolsTransport transport;
    private final Listener listener;
    private final Supplier<String> keyFactory;
    private long generation;
    private String roomNumber;
    private boolean closed = true;
    private boolean stateLoading;

    TaizhouRoomToolsCoordinator(AuthSessionCoordinator auth, Listener listener) {
        this(auth, new TaizhouRoomToolsApiClient(), listener, () -> UUID.randomUUID().toString());
    }

    TaizhouRoomToolsCoordinator(
            AuthSessionCoordinator auth,
            TaizhouRoomToolsTransport transport,
            Listener listener,
            Supplier<String> keyFactory) {
        this.auth = Objects.requireNonNull(auth, "auth");
        this.transport = Objects.requireNonNull(transport, "transport");
        this.listener = Objects.requireNonNull(listener, "listener");
        this.keyFactory = Objects.requireNonNull(keyFactory, "keyFactory");
    }

    void open(String nextRoomNumber) {
        requireRoom(nextRoomNumber);
        generation++;
        roomNumber = nextRoomNumber;
        closed = false;
        stateLoading = false;
        transport.cancelPending();
        refreshState();
    }

    void refreshState() {
        if (closed || stateLoading) return;
        stateLoading = true;
        long requestGeneration = generation;
        auth.<TaizhouRoomToolsState>execute(
                (token, callback) -> transport.loadState(token, roomNumber, forwarding(callback)),
                new AuthSessionCoordinator.Callback<TaizhouRoomToolsState>() {
                    @Override public void onSuccess(TaizhouRoomToolsState state) {
                        if (!current(requestGeneration)) return;
                        stateLoading = false;
                        listener.onState(state);
                    }
                    @Override public void onLoginRequired() {
                        if (!current(requestGeneration)) return;
                        stateLoading = false;
                        listener.onLoginRequired();
                    }
                    @Override public void onError(String message) {
                        if (!current(requestGeneration)) return;
                        stateLoading = false;
                        listener.onError(message);
                    }
                });
    }

    void setReservation(TaizhouRoomToolType type, boolean active) {
        if (closed) return;
        long requestGeneration = generation;
        String key = keyFactory.get();
        auth.<TaizhouRoomToolsState.Reservation>execute(
                (token, callback) -> transport.setReservation(
                        token, roomNumber, key, type, active, forwarding(callback)),
                writeCallback(requestGeneration));
    }

    void sendMessage(String type, int contentIndex) {
        if (closed) return;
        long requestGeneration = generation;
        String key = keyFactory.get();
        auth.<TaizhouRoomToolsState.Message>execute(
                (token, callback) -> transport.sendMessage(
                        token, roomNumber, key, type, contentIndex, forwarding(callback)),
                writeCallback(requestGeneration));
    }

    void sendVoice(int durationMillis, byte[] data) {
        if (closed) return;
        long requestGeneration = generation;
        String key = keyFactory.get();
        auth.<TaizhouRoomToolsState.Message>execute(
                (token, callback) -> transport.sendVoice(
                        token, roomNumber, key, durationMillis, data, forwarding(callback)),
                writeCallback(requestGeneration));
    }

    void loadVoice(String messageId, VoiceCallback callback) {
        if (closed) return;
        long requestGeneration = generation;
        auth.<byte[]>execute(
                (token, authCallback) -> transport.loadVoice(
                        token, roomNumber, messageId, forwarding(authCallback)),
                new AuthSessionCoordinator.Callback<byte[]>() {
                    @Override public void onSuccess(byte[] data) {
                        if (current(requestGeneration)) callback.onVoice(data);
                    }
                    @Override public void onLoginRequired() {
                        if (current(requestGeneration)) listener.onLoginRequired();
                    }
                    @Override public void onError(String message) {
                        if (current(requestGeneration)) callback.onError(message);
                    }
                });
    }

    void close() {
        generation++;
        closed = true;
        stateLoading = false;
        roomNumber = null;
        transport.cancelPending();
    }

    void destroy() {
        close();
        transport.shutdown();
    }

    private <T> AuthSessionCoordinator.Callback<T> writeCallback(long requestGeneration) {
        return new AuthSessionCoordinator.Callback<>() {
            @Override public void onSuccess(T ignored) {
                if (current(requestGeneration)) refreshState();
            }
            @Override public void onLoginRequired() {
                if (current(requestGeneration)) listener.onLoginRequired();
            }
            @Override public void onError(String message) {
                if (current(requestGeneration)) listener.onError(message);
            }
        };
    }

    private boolean current(long requestGeneration) {
        return !closed && generation == requestGeneration;
    }

    private static void requireRoom(String value) {
        if (value == null || !value.matches("\\d{6}")) {
            throw new IllegalArgumentException("roomNumber must contain six digits");
        }
    }

    private static <T> TaizhouRoomToolsTransport.Callback<T> forwarding(
            AuthSessionCoordinator.CallCallback<T> callback) {
        return new TaizhouRoomToolsTransport.Callback<>() {
            @Override public void onSuccess(T result) { callback.onSuccess(result); }
            @Override public void onUnauthorized() { callback.onUnauthorized(); }
            @Override public void onError(String message) { callback.onError(message); }
        };
    }
}
