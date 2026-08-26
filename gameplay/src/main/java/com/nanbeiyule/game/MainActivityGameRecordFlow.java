package com.nanbeiyule.game;

import android.app.AlertDialog;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.Toast;

final class MainActivityGameRecordFlow {
    private final MainActivityGameHomeDisplayFlow owner;
    private GameRecordApiClient client;
    private ZhejiangGameRecordDialog dialog;
    private GameRecordPage currentPage;
    private long requestGeneration;

    MainActivityGameRecordFlow(MainActivityGameHomeDisplayFlow owner) {
        this.owner = owner;
    }

    void show() {
        if (owner.isFinishing() || dialog != null || owner.authSessionCoordinator == null) return;
        if (client == null) client = new GameRecordApiClient();
        ZhejiangGameRecordDialog next = new ZhejiangGameRecordDialog(
                owner,
                new ZhejiangGameRecordDialog.Actions() {
                    @Override public void onClose() { dismissDialog(); }

                    @Override
                    public void onLoadRequested(String date, long gameId, boolean gold) {
                        load(date, gameId, gold);
                    }

                    @Override
                    public void onMembershipRequested() {
                        dismissDialog();
                        owner.showMembershipCenter();
                    }

                    @Override public void onReplayRequested() { showReplayInput(); }

                    @Override public void onTotalRequested(boolean gold) { showSummary(gold); }

                    @Override
                    public void onRecordRequested(GameRecordPage.Record record) {
                        showRecord(record);
                    }
                });
        dialog = next;
        if (owner.originalLobbyAudioController != null) {
            next.setButtonClickSound(owner.originalLobbyAudioController::playButtonClick);
        }
        next.setOnDismissListener(ignored -> {
            if (dialog == next) dialog = null;
            requestGeneration++;
            owner.applyImmersiveMode();
        });
        next.show();
    }

    void close() {
        if (dialog != null) dialog.dismiss();
        dialog = null;
        requestGeneration++;
        if (client != null) client.shutdown();
        client = null;
    }

    private void dismissDialog() {
        if (dialog != null) dialog.dismiss();
    }

    private void load(String date, long gameId, boolean gold) {
        ZhejiangGameRecordDialog showing = dialog;
        GameRecordApiClient api = client;
        if (showing == null || api == null || owner.authSessionCoordinator == null) return;
        long generation = ++requestGeneration;
        showing.setLoading(true);
        owner.authSessionCoordinator.execute(
                (accessToken, callback) -> api.load(
                        accessToken, date, gameId, gold, apiCallback(callback)),
                new AuthSessionCoordinator.Callback<GameRecordPage>() {
                    @Override
                    public void onSuccess(GameRecordPage page) {
                        if (generation != requestGeneration || dialog != showing) return;
                        currentPage = page;
                        showing.setPage(page);
                    }

                    @Override
                    public void onLoginRequired() {
                        if (generation != requestGeneration) return;
                        showing.dismiss();
                        owner.showLoginPage();
                    }

                    @Override
                    public void onError(String message) {
                        if (generation == requestGeneration && dialog == showing) {
                            showing.setError(message);
                        }
                    }
                });
    }

    private void showSummary(boolean gold) {
        if (currentPage == null) {
            toast("战绩正在加载");
            return;
        }
        GameRecordPage.Summary summary = currentPage.summary();
        String message = gold
                ? "当日场数：" + summary.roundCount()
                        + "\n当日金币胜负：" + signed(summary.score())
                : "冠军次数：" + summary.championCount()
                        + "\n优胜值：" + signed(summary.score())
                        + "\n对局数：" + summary.roundCount();
        new AlertDialog.Builder(owner)
                .setTitle(currentPage.date() + (gold ? " 金币战绩" : " 对局战绩"))
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }

    private void showReplayInput() {
        EditText input = new EditText(owner);
        input.setHint("请输入回放码");
        input.setSingleLine(true);
        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(32)});
        new AlertDialog.Builder(owner)
                .setTitle("查看回放")
                .setView(input)
                .setNegativeButton("取消", null)
                .setPositiveButton("查看", (ignored, which) -> {
                    if (input.getText().toString().trim().isEmpty()) {
                        toast("请输入回放码");
                    } else {
                        toast("当前版本暂不支持回放码查询");
                    }
                })
                .show();
    }

    private void showRecord(GameRecordPage.Record record) {
        StringBuilder message = new StringBuilder()
                .append("房间号：").append(record.roomNumber())
                .append("\n玩法：").append(record.gameName())
                .append("\n局数：").append(record.finishedRounds())
                .append('/').append(record.totalRounds()).append("\n\n");
        for (GameRecordPage.Player player : record.players()) {
            message.append(player.host() ? "[房主] " : "")
                    .append(player.displayName()).append("：")
                    .append(signed(player.score())).append('\n');
        }
        new AlertDialog.Builder(owner)
                .setTitle("对局详情")
                .setMessage(message.toString().trim())
                .setPositiveButton("确定", null)
                .show();
    }

    private void toast(String message) {
        Toast.makeText(owner, message, Toast.LENGTH_SHORT).show();
    }

    private static String signed(long score) {
        return score > 0 ? "+" + score : Long.toString(score);
    }

    private static GameRecordApiClient.Callback apiCallback(
            AuthSessionCoordinator.CallCallback<GameRecordPage> callback) {
        return new GameRecordApiClient.Callback() {
            @Override public void onSuccess(GameRecordPage page) { callback.onSuccess(page); }
            @Override public void onUnauthorized() { callback.onUnauthorized(); }
            @Override public void onError(String message) { callback.onError(message); }
        };
    }
}
