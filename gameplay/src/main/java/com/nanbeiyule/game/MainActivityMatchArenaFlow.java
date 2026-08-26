package com.nanbeiyule.game;

import android.widget.Toast;
import java.util.List;
import java.util.UUID;

/** Composed persistent "我的比赛场" list/create flow owned by MainActivity. */
final class MainActivityMatchArenaFlow {
    private final MainActivityRealNameFlow owner;
    private final MatchArenaApiClient apiClient = new MatchArenaApiClient();
    private MatchArenaListDialog listDialog;
    private MatchArenaCreateDialog createDialog;
    private long lobbyId;
    private long generation;

    MainActivityMatchArenaFlow(MainActivityRealNameFlow owner) {
        this.owner = owner;
    }

    void show(long selectedLobbyId) {
        if (owner.isFinishing()
                || listDialog != null
                || selectedLobbyId <= 0
                || owner.authSessionCoordinator == null) {
            return;
        }
        lobbyId = selectedLobbyId;
        MatchArenaListDialog source =
                new MatchArenaListDialog(
                        owner,
                        new MatchArenaListView.Actions() {
                            @Override public void onBackRequested() { dismiss(); }
                            @Override public void onCreateRequested() { showCreate(); }
                        });
        listDialog = source;
        if (owner.originalLobbyAudioController != null) {
            source.setButtonClickSound(owner.originalLobbyAudioController::playButtonClick);
        }
        source.setOnDismissListener(
                ignored -> {
                    generation++;
                    apiClient.cancelPending();
                    dismissCreate();
                    listDialog = null;
                    owner.applyImmersiveMode();
                });
        source.show();
        loadList(source);
    }

    void dismiss() {
        generation++;
        apiClient.cancelPending();
        dismissCreate();
        if (listDialog != null) {
            MatchArenaListDialog source = listDialog;
            listDialog = null;
            source.setOnDismissListener(null);
            source.dismiss();
            owner.applyImmersiveMode();
        }
    }

    void destroy() {
        dismiss();
        apiClient.shutdown();
    }

    private void loadList(MatchArenaListDialog source) {
        long request = ++generation;
        source.setLoading(true);
        owner.authSessionCoordinator.execute(
                (accessToken, callback) -> apiClient.list(accessToken, forwarding(callback)),
                new AuthSessionCoordinator.Callback<List<MatchArenaSummary>>() {
                    @Override public void onSuccess(List<MatchArenaSummary> items) {
                        if (isCurrent(source, request)) {
                            source.setItems(items);
                            loadAvatars(source, items);
                        }
                    }

                    @Override public void onLoginRequired() { loginIfCurrent(source, request); }

                    @Override public void onError(String message) {
                        if (isCurrent(source, request)) {
                            dismiss();
                            Toast.makeText(owner, message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showCreate() {
        if (createDialog != null || listDialog == null || owner.currentHomeState == null) {
            return;
        }
        MatchArenaCreateDialog source =
                new MatchArenaCreateDialog(
                        owner,
                        owner.currentHomeState.wallet().roomCards(),
                        state -> create(sourceOrCurrent(), state));
        createDialog = source;
        if (owner.originalLobbyAudioController != null) {
            source.setButtonClickSound(owner.originalLobbyAudioController::playButtonClick);
        }
        source.setOnDismissListener(
                ignored -> {
                    if (createDialog == source) {
                        generation++;
                        apiClient.cancelPending();
                        createDialog = null;
                        owner.applyImmersiveMode();
                    }
                });
        source.show();
    }

    private MatchArenaCreateDialog sourceOrCurrent() {
        return createDialog;
    }

    private void create(MatchArenaCreateDialog source, MatchArenaCreateState state) {
        if (source == null || source != createDialog || state == null) {
            return;
        }
        source.setSubmitting(true);
        long request = ++generation;
        String idempotencyKey = "match-arena-android-" + UUID.randomUUID();
        owner.authSessionCoordinator.execute(
                (accessToken, callback) ->
                        apiClient.create(
                                accessToken,
                                lobbyId,
                                state,
                                idempotencyKey,
                                forwarding(callback)),
                new AuthSessionCoordinator.Callback<MatchArenaSummary>() {
                    @Override public void onSuccess(MatchArenaSummary result) {
                        if (!isCurrent(source, request)) {
                            return;
                        }
                        closeCreateAfterSuccess(source);
                        if (listDialog != null) {
                            listDialog.prepend(result);
                            loadAvatars(listDialog, List.of(result));
                        }
                    }

                    @Override public void onLoginRequired() {
                        if (isCurrent(source, request)) {
                            owner.showLoginPage();
                        }
                    }

                    @Override public void onError(String message) {
                        if (isCurrent(source, request)) {
                            source.showError(message);
                        }
                    }
                });
    }

    private void closeCreateAfterSuccess(MatchArenaCreateDialog source) {
        createDialog = null;
        source.setOnDismissListener(null);
        source.dismiss();
        owner.applyImmersiveMode();
    }

    private void dismissCreate() {
        if (createDialog != null) {
            MatchArenaCreateDialog source = createDialog;
            createDialog = null;
            source.setOnDismissListener(null);
            source.dismiss();
        }
    }

    private void loadAvatars(MatchArenaListDialog source, List<MatchArenaSummary> items) {
        for (MatchArenaSummary item : items) {
            String avatarKey = item.ownerAvatarKey();
            if (avatarKey == null || avatarKey.isBlank()) continue;
            owner.loadAvatarBitmap(
                    avatarKey,
                    bitmap -> {
                        if (source == listDialog && source.isShowing()) {
                            source.setAvatarBitmap(avatarKey, bitmap);
                        }
                    });
        }
    }

    private boolean isCurrent(MatchArenaListDialog source, long request) {
        return !owner.isFinishing() && listDialog == source && generation == request;
    }

    private boolean isCurrent(MatchArenaCreateDialog source, long request) {
        return !owner.isFinishing() && createDialog == source && generation == request;
    }

    private void loginIfCurrent(MatchArenaListDialog source, long request) {
        if (isCurrent(source, request)) {
            owner.showLoginPage();
        }
    }

    private static <T> MatchArenaApiClient.Callback<T> forwarding(
            AuthSessionCoordinator.CallCallback<T> callback) {
        return new MatchArenaApiClient.Callback<>() {
            @Override public void onSuccess(T result) { callback.onSuccess(result); }
            @Override public void onUnauthorized() { callback.onUnauthorized(); }
            @Override public void onError(String message) { callback.onError(message); }
        };
    }
}
