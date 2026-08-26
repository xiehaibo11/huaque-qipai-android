package com.nanbeiyule.game;

/** Owns launcher-level Zhejiang feature dialogs that reuse existing authenticated flows. */
abstract class MainActivityLobbyFeatureFlow extends MainActivityFreeDrawFlow {
    private LobbyActivityCenterDialog activityCenterDialog;
    private LobbyShareDialog shareDialog;
    private WechatPublicDialog wechatPublicDialog;
    private ZhejiangNewsDialog newsDialog;
    private HealthNoticeDialog healthNoticeDialog;
    private AnnouncementCenterController announcementCenterController;
    private ScoreAssistantController scoreAssistantController;
    private GameRuleController gameRuleController;
    private boolean switchingActivityAnnouncement;
    private MainActivityDestination activityAnnouncementOrigin;

    protected final void showActivityCenter() {
        if (isFinishing() || activityCenterDialog != null) return;
        LobbyActivityCenterDialog dialog =
                new LobbyActivityCenterDialog(
                        this,
                        new LobbyActivityCenterDialog.Actions() {
                            @Override
                            public void onActivityRequested(
                                    LobbyActivityCenterModel.Destination destination) {
                                switch (destination) {
                                    case FREE_DRAW -> startFreeDraw();
                                    case MEMBERSHIP_GIFT -> showShop(ShopCategory.GOLD_MEMBERSHIP);
                                    case TIME_LOGIN -> showTimeLoginAct();
                                    default -> android.widget.Toast.makeText(MainActivityLobbyFeatureFlow.this, "该活动当前未开放", android.widget.Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override public void onAnnouncementRequested() {
                                switchingActivityAnnouncement = true; showAnnouncements();
                            }
                            @Override public void onGoldMembershipClaim(String code) {
                                claimGoldMembershipCard(activityCenterDialog, code);
                            }
                        });
        activityCenterDialog = dialog;
        if (originalLobbyAudioController != null) {
            dialog.setButtonClickSound(originalLobbyAudioController::playButtonClick);
        }
        dialog.setOnDismissListener(
                ignored -> {
                    if (activityCenterDialog == dialog) {
                        activityCenterDialog = null;
                    }
                    applyImmersiveMode();
                    if (switchingActivityAnnouncement) {
                        switchingActivityAnnouncement = false;
                    } else {
                        finishActivityAnnouncementFlow();
                    }
                });
        dialog.show(); loadFreeDrawState(dialog::setFreeDrawState); loadGoldMembershipCards(dialog);
    }

    protected void onActivityCenterDismissed() {}

    @Override
    protected void showLobbyFeature(MainActivityDestination destination) {
        switch (destination) {
            case ACTIVITY_CENTER -> {
                activityAnnouncementOrigin = destination;
                showActivityCenter();
            }
            case SHARE -> showLobbyShare();
            case BAG -> showShopInventory();
            case WECHAT_PUBLIC -> showWechatPublic();
            case ZHEJIANG_NEWS -> showZhejiangNews();
            case PHONE_BINDING -> showPhoneBinding();
            case RULES -> showRules();
            case HEALTH_NOTICE -> showHealthNotice();
            case ANNOUNCEMENTS -> {
                activityAnnouncementOrigin = destination;
                showAnnouncements();
            }
            case SCORING_ASSISTANT -> showScoringAssistant();
            default -> throw new IllegalArgumentException("Unsupported lobby destination: " + destination);
        }
    }

    protected void onLobbyFeatureDismissed(MainActivityDestination destination) {}

    private void showLobbyShare() {
        if (isFinishing() || shareDialog != null || currentHomeState == null) return;
        LobbyShareDialog dialog =
                new LobbyShareDialog(this, currentHomeState.player(), wechatLoginManager);
        shareDialog = dialog;
        dialog.setOnDismissListener(
                ignored -> {
                    if (shareDialog == dialog) shareDialog = null;
                    applyImmersiveMode();
                    onLobbyFeatureDismissed(MainActivityDestination.SHARE);
                });
        dialog.show();
    }

    private void showWechatPublic() {
        if (isFinishing() || wechatPublicDialog != null) return;
        WechatPublicDialog dialog = new WechatPublicDialog(this);
        wechatPublicDialog = dialog;
        if (originalLobbyAudioController != null) {
            dialog.setButtonClickSound(originalLobbyAudioController::playButtonClick);
        }
        dialog.setOnDismissListener(
                ignored -> {
                    if (wechatPublicDialog == dialog) wechatPublicDialog = null;
                    applyImmersiveMode();
                    onLobbyFeatureDismissed(MainActivityDestination.WECHAT_PUBLIC);
                });
        dialog.show();
    }

    private void showZhejiangNews() {
        if (isFinishing() || newsDialog != null) return;
        ZhejiangNewsDialog dialog = new ZhejiangNewsDialog(this);
        newsDialog = dialog;
        dialog.setOnDismissListener(
                ignored -> {
                    if (newsDialog == dialog) newsDialog = null;
                    applyImmersiveMode();
                    onLobbyFeatureDismissed(MainActivityDestination.ZHEJIANG_NEWS);
                });
        dialog.show();
    }

    private void showHealthNotice() {
        if (isFinishing() || healthNoticeDialog != null) return;
        HealthNoticeDialog dialog = new HealthNoticeDialog(this);
        healthNoticeDialog = dialog;
        if (originalLobbyAudioController != null) {
            dialog.setButtonClickSound(originalLobbyAudioController::playButtonClick);
        }
        dialog.setOnDismissListener(
                ignored -> {
                    if (healthNoticeDialog == dialog) healthNoticeDialog = null;
                    applyImmersiveMode();
                    onLobbyFeatureDismissed(MainActivityDestination.HEALTH_NOTICE);
                });
        dialog.show();
    }

    private void showAnnouncements() {
        if (isFinishing()) return;
        if (announcementCenterController == null) {
            announcementCenterController =
                    new AnnouncementCenterController(
                            this,
                            BuildConfig.API_BASE_URL,
                            () -> loginSessionStore == null
                                    ? ""
                                    : loginSessionStore.snapshot().accessToken(),
                            new AnnouncementCenterController.Listener() {
                                @Override
                                public void onUnauthorized() {
                                    showLoginPage();
                                }

                                @Override
                                public void onMessage(String message) {
                                    android.widget.Toast.makeText(
                                                    MainActivityLobbyFeatureFlow.this,
                                                    message,
                                                    android.widget.Toast.LENGTH_LONG)
                                            .show();
                                }

                                @Override
                                public void onDismissed() {
                                    applyImmersiveMode();
                                    if (switchingActivityAnnouncement) {
                                        switchingActivityAnnouncement = false;
                                    } else {
                                        finishActivityAnnouncementFlow();
                                    }
                                }

                                @Override
                                public void onActivityRequested() {
                                    switchingActivityAnnouncement = true;
                                    showActivityCenter();
                                }
                            },
                            originalLobbyAudioController == null
                                    ? null
                                    : originalLobbyAudioController::playButtonClick);
        }
        announcementCenterController.show();
    }

    private void finishActivityAnnouncementFlow() {
        MainActivityDestination origin = activityAnnouncementOrigin;
        activityAnnouncementOrigin = null;
        if (origin == MainActivityDestination.ACTIVITY_CENTER) {
            onActivityCenterDismissed();
        } else {
            onLobbyFeatureDismissed(MainActivityDestination.ANNOUNCEMENTS);
        }
    }

    private void showScoringAssistant() {
        if (isFinishing()) return;
        if (scoreAssistantController == null) {
            scoreAssistantController =
                    new ScoreAssistantController(
                            this,
                            BuildConfig.API_BASE_URL,
                            () -> loginSessionStore == null
                                    ? ""
                                    : loginSessionStore.snapshot().accessToken(),
                            new ScoreAssistantController.Listener() {
                                @Override
                                public void onUnauthorized() {
                                    showLoginPage();
                                }

                                @Override
                                public void onMessage(String message) {
                                    android.widget.Toast.makeText(
                                                    MainActivityLobbyFeatureFlow.this,
                                                    message,
                                                    android.widget.Toast.LENGTH_LONG)
                                            .show();
                                }

                                @Override
                                public void onDismissed() {
                                    applyImmersiveMode();
                                    onLobbyFeatureDismissed(
                                            MainActivityDestination.SCORING_ASSISTANT);
                                }
                            },
                            originalLobbyAudioController == null
                                    ? null
                                    : originalLobbyAudioController::playButtonClick);
        }
        scoreAssistantController.show();
    }

    private void showRules() {
        if (isFinishing()) return;
        if (gameRuleController == null) {
            gameRuleController =
                    new GameRuleController(
                            this,
                            new GameRuleController.Listener() {
                                @Override public void onTutorialStartGameRequested(long gameId) {
                                    android.widget.Toast.makeText(MainActivityLobbyFeatureFlow.this, "暗斗双扣金币场暂未接入", android.widget.Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onDismissed() {
                                    applyImmersiveMode();
                                    onLobbyFeatureDismissed(MainActivityDestination.RULES);
                                }
                            },
                            originalLobbyAudioController == null
                                    ? null
                                    : originalLobbyAudioController::playButtonClick);
        }
        gameRuleController.show();
    }

    @Override
    protected void onDestroy() {
        if (activityCenterDialog != null) {
            activityCenterDialog.setOnDismissListener(null);
            activityCenterDialog.dismiss();
            activityCenterDialog = null;
        }
        dismissWithoutCallback(shareDialog);
        shareDialog = null;
        dismissWithoutCallback(wechatPublicDialog);
        wechatPublicDialog = null;
        dismissWithoutCallback(newsDialog);
        newsDialog = null;
        dismissWithoutCallback(healthNoticeDialog);
        healthNoticeDialog = null;
        if (announcementCenterController != null) {
            announcementCenterController.close();
            announcementCenterController = null;
        }
        if (scoreAssistantController != null) {
            scoreAssistantController.close();
            scoreAssistantController = null;
        }
        if (gameRuleController != null) {
            gameRuleController.close();
            gameRuleController = null;
        }
        super.onDestroy();
    }

    private static void dismissWithoutCallback(android.app.Dialog dialog) {
        if (dialog == null) return;
        dialog.setOnDismissListener(null);
        dialog.dismiss();
    }
}
