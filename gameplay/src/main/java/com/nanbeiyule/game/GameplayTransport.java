package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplayCommandResult;
import com.nanbeiyule.game.gameplay.GameplayEvent;
import com.nanbeiyule.game.gameplay.GameplaySnapshot;
import java.util.List;
import org.json.JSONObject;

interface GameplayTransport {
    interface Callback<T> {
        void onSuccess(T result);

        void onUnauthorized();

        void onError(String message);
    }

    void open(String token, String roomNumber, Callback<GameplaySnapshot> callback);

    void loadSnapshot(String token, String roomNumber, Callback<GameplaySnapshot> callback);

    void submitCommand(
            String token,
            String roomNumber,
            String idempotencyKey,
            String type,
            long expectedRevision,
            JSONObject payload,
            Callback<GameplayCommandResult> callback);

    void loadEvents(
            String token,
            String roomNumber,
            long afterRevision,
            Callback<List<GameplayEvent>> callback);

    void cancelPending();

    void shutdown();
}
