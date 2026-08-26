package com.nanbeiyule.game;

import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.Toast;
import com.nanbeiyule.game.gameplay.GameplaySnapshot;
import com.nanbeiyule.game.wulong.WuLongTableState;
import com.nanbeiyule.game.wulong.WuLongTableView;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Authenticated native 30588 table owner; it never routes through the Taizhou Cocos launcher. */
final class MainActivityWuLongFlow {
    private final MainActivityGameHomeDisplayFlow owner;
    private GameplayApiClient api;
    private WuLongTableView table;
    private WuLongSnapshotSync snapshotSync;
    private String roomNumber;
    private GameplaySnapshot snapshot;
    private final Handler snapshotHandler = new Handler(Looper.getMainLooper());

    MainActivityWuLongFlow(MainActivityGameHomeDisplayFlow owner) { this.owner = owner; }

    void open(String roomNumber) {
        if (owner.isFinishing() || owner.authSessionCoordinator == null || roomNumber == null) return;
        close();
        this.roomNumber = roomNumber;
        api = new GameplayApiClient();
        table = new WuLongTableView(owner);
        snapshotSync = createSnapshotSync();
        table.setListener(new WuLongTableView.Listener() {
            @Override public void onReady() { command("READY", null); }
            @Override public void onStart() { command("START_ROUND", null); }
            @Override public void onPlay(List<Integer> cards) { command("PLAY_CARDS", cards); }
            @Override public void onPass() { command("PASS", null); }
            @Override public void onNextRound() { command("NEXT_ROUND", null); }
        });
        owner.loadingView = table;
        owner.setContentView(table, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        requestInitialSnapshot();
    }

    boolean handleBack() {
        if (table == null) return false;
        close();
        if (owner.currentHomeState != null) owner.displayGameHome(owner.currentHomeState);
        else owner.loadGameHome();
        return true;
    }

    void close() {
        if (snapshotSync != null) {
            snapshotSync.close();
            snapshotSync = null;
        }
        snapshot = null;
        table = null;
        roomNumber = null;
        if (api != null) {
            api.shutdown();
            api = null;
        }
    }

    void onHostPaused() {
        if (snapshotSync != null) snapshotSync.pause();
    }

    void onHostResumed() {
        if (snapshotSync != null) snapshotSync.resume();
    }

    private WuLongSnapshotSync createSnapshotSync() {
        return new WuLongSnapshotSync(
                new WuLongSnapshotSync.Requester() {
                    @Override public void load(GameplayTransport.Callback<GameplaySnapshot> callback) {
                        loadAuthoritativeSnapshot(callback);
                    }

                    @Override public void cancelPending() {
                        if (api != null) api.cancelPending();
                    }
                },
                (delayMillis, runnable) -> {
                    snapshotHandler.postDelayed(runnable, delayMillis);
                    return () -> snapshotHandler.removeCallbacks(runnable);
                },
                new WuLongSnapshotSync.Listener() {
                    @Override public void onSnapshot(GameplaySnapshot value) {
                        owner.runOnUiThread(() -> show(value));
                    }

                    @Override public void onLoginRequired() {
                        owner.runOnUiThread(() -> { close(); owner.showLoginPage(); });
                    }

                    @Override public void onError(String message) {
                        owner.runOnUiThread(() -> error(message));
                    }
                });
    }

    private void requestInitialSnapshot() {
        GameplayApiClient client = api;
        String room = roomNumber;
        if (client == null || room == null) return;
        owner.authSessionCoordinator.<GameplaySnapshot>execute(
                (token, callback) -> {
                    GameplayTransport.Callback<GameplaySnapshot> response = transport(callback);
                    client.open(token, room, response);
                }, new AuthSessionCoordinator.Callback<GameplaySnapshot>() {
                    @Override public void onSuccess(GameplaySnapshot value) {
                        if (snapshotSync != null) snapshotSync.start(value);
                    }
                    @Override public void onLoginRequired() { close(); owner.showLoginPage(); }
                    @Override public void onError(String message) { error(message); }
                });
    }

    private void loadAuthoritativeSnapshot(GameplayTransport.Callback<GameplaySnapshot> result) {
        GameplayApiClient client = api;
        String room = roomNumber;
        if (client == null || room == null) return;
        owner.authSessionCoordinator.<GameplaySnapshot>execute(
                (token, callback) -> client.loadSnapshot(token, room, transport(callback)),
                new AuthSessionCoordinator.Callback<GameplaySnapshot>() {
                    @Override public void onSuccess(GameplaySnapshot value) { result.onSuccess(value); }
                    @Override public void onLoginRequired() { result.onUnauthorized(); }
                    @Override public void onError(String message) { result.onError(message); }
                });
    }

    private void command(String type, List<Integer> cards) {
        if (snapshot == null || api == null || roomNumber == null) return;
        JSONObject payload = null;
        if (cards != null) {
            try {
                payload = new JSONObject();
                payload.put("cards", new JSONArray(cards));
            } catch (JSONException exception) {
                error("出牌参数无法编码");
                return;
            }
        }
        GameplayApiClient client = api;
        String room = roomNumber;
        long revision = snapshot.revision();
        final JSONObject commandPayload = payload;
        owner.authSessionCoordinator.<com.nanbeiyule.game.gameplay.GameplayCommandResult>execute(
                (token, callback) -> client.submitCommand(token, room, UUID.randomUUID().toString(), type,
                        revision, commandPayload, transport(callback)), new AuthSessionCoordinator.Callback<com.nanbeiyule.game.gameplay.GameplayCommandResult>() {
                    @Override public void onSuccess(com.nanbeiyule.game.gameplay.GameplayCommandResult ignored) {
                        if (snapshotSync != null) snapshotSync.requestNow();
                    }
                    @Override public void onLoginRequired() { close(); owner.showLoginPage(); }
                    @Override public void onError(String message) {
                        error(message);
                        if (snapshotSync != null) snapshotSync.requestNow();
                    }
                });
    }

    private <T> GameplayTransport.Callback<T> transport(AuthSessionCoordinator.CallCallback<T> callback) {
        return new GameplayTransport.Callback<>() {
            @Override public void onSuccess(T value) { callback.onSuccess(value); }
            @Override public void onUnauthorized() { callback.onUnauthorized(); }
            @Override public void onError(String message) { callback.onError(message); }
        };
    }

    private void show(GameplaySnapshot value) {
        if (value.gameId() != 30588L) { error("服务器返回的不是乌龙 30588 会话"); return; }
        snapshot = value;
        if (table != null) table.setState(WuLongTableState.from(value));
    }

    private void error(String message) {
        if (!owner.isFinishing()) Toast.makeText(owner, message, Toast.LENGTH_LONG).show();
    }
}
