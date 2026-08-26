package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Mirrors score-ledger input constraints before a request reaches the server. */
final class ScoreAssistantInputValidator {
    record PlayerDraft(String name, boolean ownerPlayer) {}

    record ScoreDraft(UUID playerId, String scoreText) {}

    record ScoreDelta(UUID playerId, long scoreDelta) {}

    record Validation<T>(T value, String error) {
        boolean valid() {
            return error == null || error.isEmpty();
        }
    }

    private ScoreAssistantInputValidator() {}

    static Validation<List<PlayerDraft>> validatePlayers(List<PlayerDraft> drafts) {
        if (drafts == null || drafts.size() < 2 || drafts.size() > 6) {
            return invalid("玩家人数必须为 2 至 6 人");
        }
        List<PlayerDraft> normalized = new ArrayList<>(drafts.size());
        Set<String> names = new HashSet<>();
        int owners = 0;
        for (PlayerDraft draft : drafts) {
            String name = draft == null || draft.name() == null ? "" : draft.name().strip();
            if (name.isEmpty()) {
                return invalid("玩家名称不能为空");
            }
            if (name.length() > 40) {
                return invalid("玩家名称不能超过 40 个字符");
            }
            if (!names.add(name)) {
                return invalid("玩家名称不能重复");
            }
            boolean owner = draft.ownerPlayer();
            owners += owner ? 1 : 0;
            normalized.add(new PlayerDraft(name, owner));
        }
        return owners == 1
                ? valid(List.copyOf(normalized))
                : invalid("请选择且只能选择一名本人");
    }

    static Validation<List<ScoreDelta>> validateRound(
            List<ScoreAssistantApiProtocol.Player> players, List<ScoreDraft> drafts) {
        if (players == null || drafts == null || drafts.size() != players.size()) {
            return invalid("必须填写全部玩家的分数");
        }
        Map<UUID, ScoreDraft> byPlayer = new HashMap<>();
        for (ScoreDraft draft : drafts) {
            if (draft == null
                    || draft.playerId() == null
                    || byPlayer.put(draft.playerId(), draft) != null) {
                return invalid("每名玩家只能填写一次分数");
            }
        }
        List<ScoreDelta> result = new ArrayList<>(players.size());
        long sum = 0L;
        try {
            for (ScoreAssistantApiProtocol.Player player : players) {
                ScoreDraft draft = byPlayer.get(player.playerId());
                String text = draft == null || draft.scoreText() == null
                        ? ""
                        : draft.scoreText().strip();
                if (text.isEmpty()) {
                    return invalid("必须填写全部玩家的分数");
                }
                long delta = Long.parseLong(text);
                sum = Math.addExact(sum, delta);
                result.add(new ScoreDelta(player.playerId(), delta));
            }
        } catch (NumberFormatException | ArithmeticException exception) {
            return invalid("分数必须是有效整数且不能溢出");
        }
        if (byPlayer.size() != result.size()) {
            return invalid("分数中包含不属于本账本的玩家");
        }
        return sum == 0L ? valid(List.copyOf(result)) : invalid("本局所有玩家分数之和必须为 0");
    }

    private static <T> Validation<T> valid(T value) {
        return new Validation<>(value, "");
    }

    private static <T> Validation<T> invalid(String error) {
        return new Validation<>(null, error);
    }
}
