package com.nanbeiyule.game;

import android.widget.Toast;

/** Authenticated owner for the original JoinBoxRoom native dialog. */
final class MainActivityJoinRoomFlow {
    private final MainActivityRealNameFlow owner;
    private final CreateRoomApiClient apiClient = new CreateRoomApiClient();
    private JoinRoomDialog dialog;
    private long requestGeneration;

    MainActivityJoinRoomFlow(MainActivityRealNameFlow owner) {
        this.owner = owner;
    }

    void show(String initialRoomNumber) {
        show(initialRoomNumber, () -> {});
    }

    void show(String initialRoomNumber, Runnable onDismiss) {
        if (owner.isFinishing()
                || dialog != null
                || owner.currentHomeState == null
                || owner.authSessionCoordinator == null) {
            return;
        }
        JoinRoomDialog source =
                new JoinRoomDialog(
                        owner,
                        owner.currentHomeState.region().areaName(),
                        roomNumber -> join(sourceDialog(), roomNumber));
        dialog = source;
        if (owner.originalLobbyAudioController != null) {
            source.setButtonClickSound(owner.originalLobbyAudioController::playButtonClick);
        }
        source.setOnDismissListener(
                ignored -> {
                    requestGeneration++;
                    apiClient.cancelPending();
                    if (dialog == source) {
                        dialog = null;
                    }
                    owner.applyImmersiveMode();
                    onDismiss.run();
                });
        if (initialRoomNumber != null) {
            source.setInitialRoomNumber(initialRoomNumber);
        }
        source.show();
        if (initialRoomNumber != null) {
            source.submitInitialRoomNumber();
        }
    }

    void dismiss() {
        requestGeneration++;
        apiClient.cancelPending();
        if (dialog != null) {
            JoinRoomDialog source = dialog;
            dialog = null;
            source.setOnDismissListener(null);
            source.dismiss();
            owner.applyImmersiveMode();
        }
    }

    void destroy() {
        dismiss();
        apiClient.shutdown();
    }

    private JoinRoomDialog sourceDialog() {
        return dialog;
    }

    private void join(JoinRoomDialog source, String roomNumber) {
        if (source == null || source != dialog) {
            return;
        }
        long generation = ++requestGeneration;
        apiClient.cancelPending();
        source.setSubmitting(true);
        owner.authSessionCoordinator.execute(
                (accessToken, callback) ->
                        apiClient.join(accessToken, roomNumber, forwarding(callback)),
                new AuthSessionCoordinator.Callback<CreateRoomResult>() {
                    @Override
                    public void onSuccess(CreateRoomResult result) {
                        if (!isCurrent(source, generation)) {
                            return;
                        }
                        if (result.gameId() == 30109L) {
                            dismiss();
                            owner.openTaizhouMahjongSession(result.roomNumber());
                            return;
                        }
                        if (result.gameId() == 30588L) {
                            dismiss();
                            owner.openWuLongSession(result.roomNumber());
                            return;
                        }
                        source.setSubmitting(false);
                        Toast.makeText(owner, "该房间玩法尚未完成原版恢复", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onLoginRequired() {
                        if (isCurrent(source, generation)) {
                            owner.pendingJoinRoomNumber = roomNumber;
                            dismiss();
                            owner.showLoginPage();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (isCurrent(source, generation)) {
                            source.setSubmitting(false);
                            Toast.makeText(owner, message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private boolean isCurrent(JoinRoomDialog source, long generation) {
        return !owner.isFinishing() && dialog == source && requestGeneration == generation;
    }

    private static CreateRoomApiClient.ResponseCallback<CreateRoomResult> forwarding(
            AuthSessionCoordinator.CallCallback<CreateRoomResult> callback) {
        return new CreateRoomApiClient.ResponseCallback<>() {
            @Override
            public void onSuccess(CreateRoomResult value) {
                callback.onSuccess(value);
            }

            @Override
            public void onUnauthorized() {
                callback.onUnauthorized();
            }

            @Override
            public void onError(String message, CreateRoomApiClient.FailureKind failureKind) {
                callback.onError(message);
            }
        };
    }
}
