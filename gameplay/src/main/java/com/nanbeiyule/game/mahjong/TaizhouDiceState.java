package com.nanbeiyule.game.mahjong;

import com.nanbeiyule.game.mahjong.round.MahjongGameStep;
import java.util.List;
import java.util.Objects;

/** Original-shaped msgThrowChip state bridge: nSeat + nCount + nChips + game step. */
public record TaizhouDiceState(
        int seatNumber,
        List<Integer> values,
        MahjongGameStep gameStep,
        boolean showAnimation) {
    public TaizhouDiceState {
        values = List.copyOf(Objects.requireNonNull(values, "values"));
        gameStep = Objects.requireNonNull(gameStep, "gameStep");
        if (seatNumber <= 0 || seatNumber > 4) {
            throw new IllegalArgumentException("dice seat is outside chair count");
        }
        if (values.isEmpty() || values.size() > 3) {
            throw new IllegalArgumentException("dice values must contain one to three chips");
        }
        for (int value : values) {
            if (value < 1 || value > 6) {
                throw new IllegalArgumentException("dice value must be between 1 and 6");
            }
        }
        if (gameStep != MahjongGameStep.GAME_STEP_THROW_CHIP_1
                && gameStep != MahjongGameStep.GAME_STEP_THROW_CHIP_2
                && gameStep != MahjongGameStep.GAME_STEP_THROW_CHIP_3) {
            throw new IllegalArgumentException("dice game step must be a throw-chip step");
        }
    }
}
