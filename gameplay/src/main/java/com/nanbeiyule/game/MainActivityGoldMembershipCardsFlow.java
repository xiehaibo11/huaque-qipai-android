package com.nanbeiyule.game;

import android.widget.Toast;

/** Coordinates the embedded gold-card panel with authenticated membership APIs. */
abstract class MainActivityGoldMembershipCardsFlow extends MainActivityRealNameFlow {
    protected final void loadGoldMembershipCards(LobbyActivityCenterDialog dialog) {
        if (dialog == null || membershipApiClient == null || authSessionCoordinator == null) return;
        dialog.setGoldMembershipCardsLoading(true);
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        GoldMembershipCardsApi.load(
                                membershipApiClient,
                                accessToken,
                                MembershipCallbackAdapter.from(callback)),
                new AuthSessionCoordinator.Callback<GoldMembershipCardsState>() {
                    @Override
                    public void onSuccess(GoldMembershipCardsState state) {
                        if (dialog.isShowing()) dialog.setGoldMembershipCardsState(state);
                    }

                    @Override
                    public void onLoginRequired() {
                        if (dialog.isShowing()) dialog.dismiss();
                        showLoginPage();
                    }

                    @Override
                    public void onError(String message) {
                        if (dialog.isShowing()) dialog.setGoldMembershipCardsError(message);
                    }
                });
    }

    protected final void claimGoldMembershipCard(
            LobbyActivityCenterDialog dialog, String productCode) {
        if (dialog == null || membershipApiClient == null || authSessionCoordinator == null) return;
        dialog.setGoldMembershipCardsLoading(true);
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        GoldMembershipCardsApi.claim(
                                membershipApiClient,
                                accessToken,
                                productCode,
                                MembershipCallbackAdapter.from(callback)),
                new AuthSessionCoordinator.Callback<GoldMembershipCardsState.Card>() {
                    @Override
                    public void onSuccess(GoldMembershipCardsState.Card card) {
                        if (!dialog.isShowing()) return;
                        dialog.updateGoldMembershipCard(card);
                        Toast.makeText(
                                        MainActivityGoldMembershipCardsFlow.this,
                                        "会员金币领取成功",
                                        Toast.LENGTH_SHORT)
                                .show();
                        loadGameHome();
                        loadGoldMembershipCards(dialog);
                    }

                    @Override
                    public void onLoginRequired() {
                        if (dialog.isShowing()) dialog.dismiss();
                        showLoginPage();
                    }

                    @Override
                    public void onError(String message) {
                        if (dialog.isShowing()) dialog.setGoldMembershipCardsError(message);
                    }
                });
    }
}
