package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;
import org.junit.Test;

public class ScoreAssistantInputValidatorTest {
    private static final UUID FIRST = UUID.fromString("40000000-0000-0000-0000-000000000004");
    private static final UUID SECOND = UUID.fromString("50000000-0000-0000-0000-000000000005");

    @Test
    public void normalizesTwoToSixUniqueNamesAndRequiresExactlyOneOwner() {
        ScoreAssistantInputValidator.Validation<List<ScoreAssistantInputValidator.PlayerDraft>> valid =
                ScoreAssistantInputValidator.validatePlayers(
                        List.of(
                                new ScoreAssistantInputValidator.PlayerDraft("  本人  ", true),
                                new ScoreAssistantInputValidator.PlayerDraft("牌友", false)));

        assertTrue(valid.valid());
        assertEquals("本人", valid.value().get(0).name());
        assertFalse(valid.value().get(1).ownerPlayer());

        assertFalse(ScoreAssistantInputValidator.validatePlayers(List.of(valid.value().get(0))).valid());
        assertFalse(
                ScoreAssistantInputValidator.validatePlayers(
                                List.of(
                                        new ScoreAssistantInputValidator.PlayerDraft("甲", false),
                                        new ScoreAssistantInputValidator.PlayerDraft("乙", false)))
                        .valid());
        assertFalse(
                ScoreAssistantInputValidator.validatePlayers(
                                List.of(
                                        new ScoreAssistantInputValidator.PlayerDraft("甲", true),
                                        new ScoreAssistantInputValidator.PlayerDraft("甲 ", false)))
                        .valid());
    }

    @Test
    public void roundRequiresEveryPlayerExactlyOnceAndAZeroLongSum() {
        List<ScoreAssistantApiProtocol.Player> players = players();
        ScoreAssistantInputValidator.Validation<List<ScoreAssistantInputValidator.ScoreDelta>> valid =
                ScoreAssistantInputValidator.validateRound(
                        players,
                        List.of(
                                new ScoreAssistantInputValidator.ScoreDraft(FIRST, "+18"),
                                new ScoreAssistantInputValidator.ScoreDraft(SECOND, "-18")));

        assertTrue(valid.valid());
        assertEquals(18L, valid.value().get(0).scoreDelta());
        assertEquals(-18L, valid.value().get(1).scoreDelta());
        assertFalse(
                ScoreAssistantInputValidator.validateRound(
                                players,
                                List.of(new ScoreAssistantInputValidator.ScoreDraft(FIRST, "0")))
                        .valid());
        assertFalse(
                ScoreAssistantInputValidator.validateRound(
                                players,
                                List.of(
                                        new ScoreAssistantInputValidator.ScoreDraft(FIRST, "10"),
                                        new ScoreAssistantInputValidator.ScoreDraft(SECOND, "-9")))
                        .valid());
        assertFalse(
                ScoreAssistantInputValidator.validateRound(
                                players,
                                List.of(
                                        new ScoreAssistantInputValidator.ScoreDraft(FIRST, "not-a-score"),
                                        new ScoreAssistantInputValidator.ScoreDraft(SECOND, "0")))
                        .valid());
    }

    private static List<ScoreAssistantApiProtocol.Player> players() {
        return List.of(
                new ScoreAssistantApiProtocol.Player(FIRST, 1, "本人", true, 0),
                new ScoreAssistantApiProtocol.Player(SECOND, 2, "牌友", false, 0));
    }
}
