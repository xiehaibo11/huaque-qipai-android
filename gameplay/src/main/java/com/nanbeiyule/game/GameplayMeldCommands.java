package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplayActionProtocol;
import com.nanbeiyule.game.gameplay.GameplayKongType;
import com.nanbeiyule.game.gameplay.GameplayTableState;

/**
 * 对同一个动作 offer 的应答（吃 / 碰 / 杠 / 胡 / 过）。
 *
 * <p>从 {@link GameplaySessionCoordinator} 拆出的纯校验+提交逻辑，语义不变：全部先确认当前
 * offer 的 actionToken 仍然匹配，再按 {@link GameplayActionProtocol} 的载荷形状提交。协调器保留
 * 同名薄门面，测试与调用方的接口不变。
 */
final class GameplayMeldCommands {
    private GameplayMeldCommands() {}

    static void chow(
            GameplaySessionCoordinator owner,
            int tileValue,
            int candidateIndex,
            String actionToken) {
        GameplayTableState current = owner.currentState();
        if (!owner.hasMatchingOffer(current, actionToken)) {
            return;
        }
        if (candidateIndex < 0
                || candidateIndex >= current.actionOffer().get().chowCandidates().size()) {
            return;
        }
        owner.submitAction(current, GameplayActionProtocol.CHOW,
                () -> GameplayActionProtocol.chowPayload(tileValue, candidateIndex, actionToken));
    }

    static void pung(GameplaySessionCoordinator owner, int tileValue, String actionToken) {
        GameplayTableState current = owner.currentState();
        if (!owner.hasMatchingOffer(current, actionToken)) {
            return;
        }
        owner.submitAction(current, GameplayActionProtocol.PUNG,
                () -> GameplayActionProtocol.pungPayload(tileValue, actionToken));
    }

    static void kong(
            GameplaySessionCoordinator owner,
            int tileValue,
            GameplayKongType kongType,
            String actionToken) {
        GameplayTableState current = owner.currentState();
        if (!owner.hasMatchingOffer(current, actionToken) || kongType == null) {
            return;
        }
        boolean offered =
                current.actionOffer().get().kongOptions().stream()
                        .anyMatch(
                                option ->
                                        option.kongType() == kongType
                                                && option.tileValue() == tileValue);
        if (!offered) {
            return;
        }
        owner.submitAction(current, GameplayActionProtocol.KONG,
                () -> GameplayActionProtocol.kongPayload(tileValue, kongType, actionToken));
    }

    static void hu(GameplaySessionCoordinator owner, String actionToken) {
        GameplayTableState current = owner.currentState();
        if (!owner.hasMatchingOffer(current, actionToken)) {
            return;
        }
        owner.submitAction(current, GameplayActionProtocol.HU,
                () -> GameplayActionProtocol.huPayload(actionToken));
    }

    static void pass(GameplaySessionCoordinator owner, String actionToken) {
        GameplayTableState current = owner.currentState();
        if (!owner.hasMatchingOffer(current, actionToken)) {
            return;
        }
        owner.submitAction(current, GameplayActionProtocol.PASS,
                () -> GameplayActionProtocol.passPayload(actionToken));
    }
}
