package com.nanbeiyule.game.mahjong;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Server-projected 30400 gold-room AddMultipleLayer state.
 *
 * <p>This is UI state only. Production scoring and wall flow remain server authoritative and evidence gated.
 */
public record TaizhouMultipleState(
        boolean goldMode,
        boolean choiceActive,
        int baseScore,
        int currentMultiplier,
        int cardUseCount,
        int diamondUseCount,
        int mySeat,
        Set<Choice> allowedChoices,
        Map<Integer, Choice> seatChoices) {
    /**
     * 已作出的加倍选择。
     *
     * <p>对应原版 {@code GameProtocol.msgAddMulti.ADDMULTITYPE}（{@code XY_ID = 1478}）：
     *
     * <pre>
     * NONE    = 0   未操作加倍   ← 不是一个选项，用 seatChoices 里「没有该座位条目」表示
     * PASS    = 1   不加倍
     * DEFAULT = 2   普通加倍
     * SUPER   = 3   超级加倍
     * </pre>
     *
     * <p>原版 {@code GameBase/Modules/AddMultiple/View.lua:109-116} 收到 {@code NONE} 时只对自己
     * 打开按钮面板后 {@code return}，**不显示任何标牌**；只有 PASS/DEFAULT/SUPER 才
     * {@code loadTexture} 并 {@code setVisible(true)}。因此「未操作」绝不能和「不加倍」共用一个值，
     * 否则没人点选时四家标牌会全部亮出「不加倍」。
     */
    public enum Choice {
        PASS,
        DEFAULT,
        SUPER
    }

    public TaizhouMultipleState {
        if (baseScore < 0 || currentMultiplier < 0 || cardUseCount < 0 || diamondUseCount < 0) {
            throw new IllegalArgumentException("multiple numbers must not be negative");
        }
        if (mySeat <= 0 || mySeat > 4) {
            throw new IllegalArgumentException("mySeat is outside AddMultipleLayer seats");
        }
        Objects.requireNonNull(allowedChoices, "allowedChoices");
        Objects.requireNonNull(seatChoices, "seatChoices");
        allowedChoices = allowedChoices.isEmpty()
                ? Set.of()
                : Set.copyOf(EnumSet.copyOf(allowedChoices));
        Map<Integer, Choice> copied = new LinkedHashMap<>();
        for (Map.Entry<Integer, Choice> entry : seatChoices.entrySet()) {
            int seatNumber = entry.getKey();
            if (seatNumber <= 0 || seatNumber > 4) {
                throw new IllegalArgumentException("seat choice outside AddMultipleLayer seats");
            }
            if (entry.getValue() != null) {
                copied.put(seatNumber, entry.getValue());
            }
        }
        seatChoices = Map.copyOf(copied);
    }

    public boolean canChoose(Choice choice) {
        return choiceActive && allowedChoices.contains(choice);
    }

    public Optional<Choice> choiceForSeat(int seatNumber) {
        return Optional.ofNullable(seatChoices.get(seatNumber));
    }
}
