package com.huaque.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;
import com.nanbeiyule.game.GameRecordApiClient;
import com.nanbeiyule.game.GameRecordPage;
import com.nanbeiyule.game.ZhejiangGameRecordDialog;
import java.util.function.Supplier;

final class ZhejiangLobbyRecordController {
    private final Activity activity;
    private final Supplier<String> accessToken;
    private final Runnable openMembership;
    private final Runnable unauthorized;
    private final Runnable buttonClickSound;
    private final Runnable restoreImmersive;
    private GameRecordApiClient apiClient;
    private ZhejiangGameRecordDialog dialog;
    private GameRecordPage currentPage;

    ZhejiangLobbyRecordController(
            Activity activity,
            Supplier<String> accessToken,
            Runnable openMembership,
            Runnable unauthorized,
            Runnable buttonClickSound,
            Runnable restoreImmersive) {
        this.activity = activity;
        this.accessToken = accessToken;
        this.openMembership = openMembership;
        this.unauthorized = unauthorized == null ? () -> {} : unauthorized;
        this.buttonClickSound = buttonClickSound == null ? () -> {} : buttonClickSound;
        this.restoreImmersive = restoreImmersive == null ? () -> {} : restoreImmersive;
    }

    void show() {
        if (activity.isFinishing() || dialog != null) return;
        if (apiClient == null) apiClient = new GameRecordApiClient(BuildConfig.AUTH_BASE_URL);
        ZhejiangGameRecordDialog next = new ZhejiangGameRecordDialog(
                activity,
                new ZhejiangGameRecordDialog.Actions() {
                    @Override public void onClose() { nextDialogDismiss(); }

                    @Override
                    public void onLoadRequested(String date, long gameId, boolean gold) {
                        load(date, gameId, gold);
                    }

                    @Override
                    public void onMembershipRequested() {
                        nextDialogDismiss();
                        openMembership.run();
                    }

                    @Override public void onReplayRequested() { showReplayInput(); }

                    @Override public void onTotalRequested(boolean gold) { showSummary(gold); }

                    @Override
                    public void onRecordRequested(GameRecordPage.Record record) {
                        showRecordDetail(record);
                    }
                });
        dialog = next;
        next.setButtonClickSound(buttonClickSound);
        next.setOnDismissListener(ignored -> {
            if (dialog == next) dialog = null;
            restoreImmersive.run();
        });
        next.show();
    }

    void close() {
        if (dialog != null) dialog.dismiss();
        dialog = null;
        if (apiClient != null) apiClient.shutdown();
        apiClient = null;
    }

    private void load(String date, long gameId, boolean gold) {
        ZhejiangGameRecordDialog showing = dialog;
        GameRecordApiClient client = apiClient;
        if (showing == null || client == null) return;
        showing.setLoading(true);
        client.load(accessToken.get(), date, gameId, gold, new GameRecordApiClient.Callback() {
            @Override
            public void onSuccess(GameRecordPage page) {
                if (dialog != showing || activity.isFinishing()) return;
                currentPage = page;
                showing.setPage(page);
            }

            @Override
            public void onUnauthorized() {
                if (dialog == showing) {
                    showing.setError("登录状态已失效，请重新登录");
                }
                unauthorized.run();
            }

            @Override
            public void onError(String message) {
                if (dialog == showing) showing.setError(message);
            }
        });
    }

    private void nextDialogDismiss() {
        if (dialog != null) dialog.dismiss();
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
        new AlertDialog.Builder(activity)
                .setTitle(currentPage.date() + (gold ? " 金币战绩" : " 对局战绩"))
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }

    private void showReplayInput() {
        EditText input = new EditText(activity);
        input.setHint("请输入回放码");
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(32)});
        new AlertDialog.Builder(activity)
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

    private void showRecordDetail(GameRecordPage.Record record) {
        StringBuilder message = new StringBuilder()
                .append("房间号：").append(record.roomNumber())
                .append("\n玩法：").append(record.gameName())
                .append("\n局数：").append(record.finishedRounds())
                .append('/').append(record.totalRounds()).append("\n\n");
        for (GameRecordPage.Player player : record.players()) {
            message.append(player.host() ? "[房主] " : "")
                    .append(player.displayName())
                    .append("：")
                    .append(signed(player.score()))
                    .append('\n');
        }
        new AlertDialog.Builder(activity)
                .setTitle("对局详情")
                .setMessage(message.toString().trim())
                .setPositiveButton("确定", null)
                .show();
    }

    private void toast(String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    private static String signed(long score) {
        return score > 0 ? "+" + score : Long.toString(score);
    }
}
