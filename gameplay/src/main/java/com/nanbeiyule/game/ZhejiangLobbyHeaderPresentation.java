package com.nanbeiyule.game;

/** Visible account values for the Zhejiang lobby header, projected from one home snapshot. */
record ZhejiangLobbyHeaderPresentation(
        String displayName,
        String playerId,
        String coins,
        String diamonds,
        String roomCards) {
    static ZhejiangLobbyHeaderPresentation from(GameHomeState state) {
        return new ZhejiangLobbyHeaderPresentation(
                state.player().displayName(),
                "ID:" + state.player().publicPlayerId(),
                ZhejiangLobbyAmountFormatter.format(state.wallet().coins()),
                ZhejiangLobbyAmountFormatter.format(state.wallet().diamonds()),
                ZhejiangLobbyAmountFormatter.format(state.wallet().roomCards()));
    }
}
