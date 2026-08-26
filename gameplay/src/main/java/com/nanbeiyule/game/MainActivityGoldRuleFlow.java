package com.nanbeiyule.game;

import android.widget.Toast;
import com.nanbeiyule.game.goldroom.GoldHallGameRuleDocument;
import java.util.Collections;

/**
 * Authenticated owner for the original gold-hall rule dialog.
 *
 * <p>对应原版 {@code GoldNew/View.lua:242 onClickGameRule → openView("GoldHallGameRuleView", nil,
 * roomInfo.LeisureID)}（{@code ViewsConfig.lua:170} 指向
 * {@code lobby.Modules.GoldNew.SubModules.Rule.RuleView}）。
 *
 * <p>原版 {@code RuleView:updateRuleWebView} 用 WebView 加载浙江服务器的
 * {@code GAME_RULE_HTML_ADDR/<渠道>/7128/<GameID>.html}。本项目不请求原版服务，也不使用
 * 浏览器或 WebView：弹层外框严格按 {@code GameRuleLayer.csb} 原生绘制，正文由自建后端
 * {@code GET /api/v1/game-rules/{gameId}} 下发后原生排版。
 */
final class MainActivityGoldRuleFlow {
    private final MainActivityRealNameFlow owner;
    private final Runnable onLoginRequired;
    private final Runnable onDismissed;
    private final GameRuleApiClient apiClient = new GameRuleApiClient();
    private GoldHallGameRuleDialog dialog;

    MainActivityGoldRuleFlow(MainActivityRealNameFlow owner, Runnable onLoginRequired) {
        this(owner, onLoginRequired, () -> {});
    }

    MainActivityGoldRuleFlow(
            MainActivityRealNameFlow owner,
            Runnable onLoginRequired,
            Runnable onDismissed) {
        this.owner = owner;
        this.onLoginRequired = onLoginRequired;
        this.onDismissed = onDismissed;
    }

    /**
     * 打开规则弹层。原版 {@code RuleView:initGameBtnsList} 在带 leisureID 打开时只建当前玩法
     * 一个页签，所以这里同样只放一项；页签文案取已鉴权目录的玩法名。
     */
    void show(long gameId, String displayName) {
        if (owner.isFinishing() || dialog != null || owner.authSessionCoordinator == null) {
            return;
        }
        GoldHallGameRuleDialog source = new GoldHallGameRuleDialog(owner, ignored -> {});
        dialog = source;
        source.setOnDismissListener(
                ignored -> {
                    if (dialog == source) {
                        dialog = null;
                    }
                    owner.applyImmersiveMode();
                    onDismissed.run();
                });
        source.show();
        // 先摆好页签与 _panelLoading 等待态；正文到达前不画任何规则文字。
        source.setDocuments(
                Collections.singletonList(
                        new GoldHallGameRuleDocument(
                                gameId,
                                displayName == null ? "" : displayName,
                                Collections.emptyList())),
                gameId);
        load(source, gameId, displayName);
    }

    void dismiss() {
        if (dialog == null) {
            return;
        }
        GoldHallGameRuleDialog source = dialog;
        dialog = null;
        source.setOnDismissListener(null);
        source.dismiss();
        owner.applyImmersiveMode();
    }

    boolean handleBack() {
        if (dialog == null) {
            return false;
        }
        dismiss();
        return true;
    }

    void shutdown() {
        apiClient.shutdown();
        dismiss();
    }

    private void load(GoldHallGameRuleDialog source, long gameId, String displayName) {
        owner.authSessionCoordinator.execute(
                (accessToken, callback) ->
                        apiClient.loadDocument(accessToken, gameId, forwarding(callback)),
                new AuthSessionCoordinator.Callback<GoldHallGameRuleDocument>() {
                    @Override
                    public void onSuccess(GoldHallGameRuleDocument document) {
                        if (dialog != source) {
                            return;
                        }
                        String title =
                                document.title().isBlank() ? displayName : document.title();
                        source.setDocuments(
                                Collections.singletonList(
                                        new GoldHallGameRuleDocument(
                                                document.gameId(),
                                                title == null ? "" : title,
                                                document.blocks())),
                                document.gameId());
                    }

                    @Override
                    public void onLoginRequired() {
                        if (dialog != source) {
                            return;
                        }
                        dismiss();
                        onLoginRequired.run();
                        owner.showLoginPage();
                    }

                    @Override
                    public void onError(String message) {
                        if (dialog != source) {
                            return;
                        }
                        // 原版加载失败时 WebView 不显示、_panelLoading 保持可见；这里同样停在
                        // 等待态并如实提示，不用本地文案冒充服务端规则。
                        source.setLoading(true);
                        Toast.makeText(owner, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private static GameRuleApiClient.ResponseCallback forwarding(
            AuthSessionCoordinator.CallCallback<GoldHallGameRuleDocument> callback) {
        return new GameRuleApiClient.ResponseCallback() {
            @Override
            public void onSuccess(GoldHallGameRuleDocument document) {
                callback.onSuccess(document);
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
}
