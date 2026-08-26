package com.nanbeiyule.game;

import android.widget.Toast;
import java.util.UUID;

/** Owns the authenticated native daily/weekly mission window. */
abstract class MainActivityDailyMissionFlow extends MainActivityAvatarFlow {
    private DailyMissionApiClient missionApiClient;
    private DailyMissionDialog dailyMissionDialog;
    private boolean claimInFlight;
    private long requestGeneration;

    @Override
    protected void showDailyMission() {
        if (isFinishing() || dailyMissionDialog != null || authSessionCoordinator == null) return;
        if (missionApiClient == null) missionApiClient = new DailyMissionApiClient();
        dailyMissionDialog = new DailyMissionDialog(this, new MissionActions());
        if (originalLobbyAudioController != null) {
            dailyMissionDialog.setButtonClickSound(
                    originalLobbyAudioController::playButtonClick);
        }
        dailyMissionDialog.setOnDismissListener(
                ignored -> {
                    dailyMissionDialog = null;
                    claimInFlight = false;
                    requestGeneration++;
                    applyImmersiveMode();
                });
        dailyMissionDialog.show();
        loadMissionPage(null);
    }

    /** pageCode 为空表示打开弹层，由服务端返回 pageList 的第一个页签。 */
    private void loadMissionPage(String pageCode) {
        DailyMissionDialog dialog = dailyMissionDialog;
        if (dialog == null || missionApiClient == null || authSessionCoordinator == null) return;
        long generation = ++requestGeneration;
        dialog.setLoading(true);
        boolean first = pageCode == null || pageCode.isBlank();
        authSessionCoordinator.execute(
                (accessToken, callback) -> {
                    if (first) {
                        missionApiClient.loadFirstPage(accessToken, missionCallback(callback));
                    } else {
                        missionApiClient.loadPage(
                                accessToken, pageCode, missionCallback(callback));
                    }
                },
                stateCallback(generation, false));
    }

    private void claimTask(String taskCode) {
        if (claimInFlight
                || dailyMissionDialog == null
                || missionApiClient == null
                || authSessionCoordinator == null) return;
        claimInFlight = true;
        long generation = ++requestGeneration;
        String idempotencyKey = "mission-task-" + UUID.randomUUID();
        dailyMissionDialog.setLoading(true);
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        missionApiClient.claimTask(
                                accessToken,
                                taskCode,
                                idempotencyKey,
                                missionCallback(callback)),
                stateCallback(generation, true));
    }

    private void claimMilestone(long target) {
        DailyMissionDialog dialog = dailyMissionDialog;
        String pageCode = dialog == null ? "" : dialogStatePageCode(dialog);
        if (claimInFlight
                || pageCode.isBlank()
                || missionApiClient == null
                || authSessionCoordinator == null) return;
        claimInFlight = true;
        long generation = ++requestGeneration;
        String idempotencyKey = "mission-milestone-" + UUID.randomUUID();
        dialog.setLoading(true);
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        missionApiClient.claimMilestone(
                                accessToken,
                                pageCode,
                                target,
                                idempotencyKey,
                                missionCallback(callback)),
                stateCallback(generation, true));
    }

    private AuthSessionCoordinator.Callback<DailyMissionState> stateCallback(
            long generation,
            boolean claimed) {
        return new AuthSessionCoordinator.Callback<>() {
            @Override
            public void onSuccess(DailyMissionState state) {
                if (generation != requestGeneration || dailyMissionDialog == null) return;
                claimInFlight = false;
                dailyMissionDialog.setState(state);
                if (claimed) {
                    Toast.makeText(
                                    MainActivityDailyMissionFlow.this,
                                    "奖励领取成功",
                                    Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onLoginRequired() {
                if (generation != requestGeneration) return;
                dismissMissionForLogin();
            }

            @Override
            public void onError(String message) {
                if (generation != requestGeneration || dailyMissionDialog == null) return;
                claimInFlight = false;
                dailyMissionDialog.setError(message);
            }
        };
    }

    private <T> DailyMissionApiClient.ResponseCallback<T> missionCallback(
            AuthSessionCoordinator.CallCallback<T> callback) {
        return new DailyMissionApiClient.ResponseCallback<>() {
            @Override
            public void onSuccess(T result) {
                callback.onSuccess(result);
            }

            @Override
            public void onUnauthorized() {
                callback.onUnauthorized();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        };
    }

    private static String dialogStatePageCode(DailyMissionDialog dialog) {
        DailyMissionState state = dialog.state();
        return state == null ? "" : state.page().pageCode();
    }

    private void goComplete(DailyMissionState.Task task) {
        if ("LOGIN".equals(task.jumpType())) {
            loadMissionPage(dialogStatePageCode(dailyMissionDialog));
            return;
        }
        if (dailyMissionDialog != null) dailyMissionDialog.dismiss();
        Toast.makeText(this, "请进入对应牌局完成任务", Toast.LENGTH_SHORT).show();
    }

    private void dismissMissionForLogin() {
        if (dailyMissionDialog != null) dailyMissionDialog.dismiss();
        showLoginPage();
    }

    @Override
    protected void onDestroy() {
        if (dailyMissionDialog != null) {
            dailyMissionDialog.dismiss();
            dailyMissionDialog = null;
        }
        if (missionApiClient != null) {
            missionApiClient.close();
            missionApiClient = null;
        }
        super.onDestroy();
    }

    private final class MissionActions implements DailyMissionDialog.Actions {
        @Override
        public void onClose() {
            if (dailyMissionDialog != null) dailyMissionDialog.dismiss();
        }

        @Override
        public void onPageSelected(String pageCode) {
            loadMissionPage(pageCode);
        }

        @Override
        public void onTaskClaim(String taskCode) {
            claimTask(taskCode);
        }

        @Override
        public void onTaskGo(DailyMissionState.Task task) {
            goComplete(task);
        }

        @Override
        public void onMilestoneClaim(long target) {
            claimMilestone(target);
        }
    }
}
