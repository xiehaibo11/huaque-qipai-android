package com.nanbeiyule.game;

import java.time.Instant;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Value objects matching the authenticated score-ledger API. */
final class ScoreAssistantApiProtocol {
    enum Status {
        IN_PROGRESS,
        ENDED
    }

    record Player(
            UUID playerId,
            int position,
            String name,
            boolean ownerPlayer,
            long totalScore) {}

    record RoundScore(UUID playerId, String playerName, long scoreDelta, long totalAfter) {}

    record RoundResult(
            UUID roundId, int roundNumber, Instant recordedAt, List<RoundScore> scores) {
        RoundResult {
            scores = List.copyOf(scores);
        }
    }

    record LedgerSummary(
            UUID ledgerId,
            Status status,
            boolean favorite,
            int roundCount,
            Instant startedAt,
            Instant endedAt,
            List<Player> players) {
        LedgerSummary {
            players = List.copyOf(players);
        }
    }

    record LedgerDetail(
            UUID ledgerId,
            Status status,
            boolean favorite,
            int roundCount,
            Instant startedAt,
            Instant endedAt,
            List<Player> players,
            List<RoundResult> rounds) {
        LedgerDetail {
            players = List.copyOf(players);
            rounds = List.copyOf(rounds);
        }
    }

    record HistoryPage(
            int page,
            int pageSize,
            long totalCount,
            int totalPages,
            List<LedgerSummary> ledgers) {
        HistoryPage {
            ledgers = List.copyOf(ledgers);
        }
    }

    record MonthlyStatistics(
            YearMonth month,
            long totalPlay,
            long winPlay,
            long lossPlay,
            long totalScore,
            long winScore,
            long lossScore,
            String winMax,
            String lostMax) {}

    record LedgerState(
            UUID ledgerId,
            Status status,
            boolean favorite,
            int roundCount,
            Instant endedAt) {}

    record DeleteReceipt(UUID ledgerId, Instant deletedAt) {}

    private ScoreAssistantApiProtocol() {}

    static List<LedgerSummary> inProgressFromJson(String text) throws JSONException {
        JSONArray source = new JSONObject(text).getJSONArray("ledgers");
        List<LedgerSummary> result = new ArrayList<>(source.length());
        for (int index = 0; index < source.length(); index++) {
            result.add(summary(source.getJSONObject(index)));
        }
        return List.copyOf(result);
    }

    static LedgerDetail detailFromJson(String text) throws JSONException {
        JSONObject source = new JSONObject(text);
        return new LedgerDetail(
                uuid(source, "ledgerId"),
                status(source),
                source.getBoolean("favorite"),
                source.getInt("roundCount"),
                instant(source, "startedAt", false),
                instant(source, "endedAt", true),
                players(source.getJSONArray("players")),
                rounds(source.getJSONArray("rounds")));
    }

    static HistoryPage historyFromJson(String text) throws JSONException {
        JSONObject source = new JSONObject(text);
        JSONArray array = source.getJSONArray("ledgers");
        List<LedgerSummary> ledgers = new ArrayList<>(array.length());
        for (int index = 0; index < array.length(); index++) {
            ledgers.add(summary(array.getJSONObject(index)));
        }
        return new HistoryPage(
                source.getInt("page"),
                source.getInt("pageSize"),
                source.getLong("totalCount"),
                source.getInt("totalPages"),
                ledgers);
    }

    static MonthlyStatistics monthlyFromJson(String text) throws JSONException {
        JSONObject source = new JSONObject(text);
        try {
            return new MonthlyStatistics(
                    YearMonth.parse(source.getString("month")),
                    source.getLong("totalPlay"),
                    source.getLong("winPlay"),
                    source.getLong("lossPlay"),
                    source.getLong("totalScore"),
                    source.getLong("winScore"),
                    source.getLong("lossScore"),
                    text(source, "winMax"),
                    text(source, "lostMax"));
        } catch (DateTimeParseException exception) {
            throw new JSONException("month is not yyyy-MM");
        }
    }

    static RoundResult roundFromJson(String text) throws JSONException {
        return round(new JSONObject(text));
    }

    static LedgerState stateFromJson(String text) throws JSONException {
        JSONObject source = new JSONObject(text);
        return new LedgerState(
                uuid(source, "ledgerId"),
                status(source),
                source.getBoolean("favorite"),
                source.getInt("roundCount"),
                instant(source, "endedAt", true));
    }

    static DeleteReceipt deleteFromJson(String text) throws JSONException {
        JSONObject source = new JSONObject(text);
        return new DeleteReceipt(
                uuid(source, "ledgerId"), instant(source, "deletedAt", false));
    }

    private static LedgerSummary summary(JSONObject source) throws JSONException {
        return new LedgerSummary(
                uuid(source, "ledgerId"),
                status(source),
                source.getBoolean("favorite"),
                source.getInt("roundCount"),
                instant(source, "startedAt", false),
                instant(source, "endedAt", true),
                players(source.getJSONArray("players")));
    }

    private static List<Player> players(JSONArray source) throws JSONException {
        List<Player> result = new ArrayList<>(source.length());
        for (int index = 0; index < source.length(); index++) {
            JSONObject item = source.getJSONObject(index);
            result.add(
                    new Player(
                            uuid(item, "playerId"),
                            item.getInt("position"),
                            item.getString("name"),
                            item.getBoolean("ownerPlayer"),
                            item.getLong("totalScore")));
        }
        return result;
    }

    private static List<RoundResult> rounds(JSONArray source) throws JSONException {
        List<RoundResult> result = new ArrayList<>(source.length());
        for (int index = 0; index < source.length(); index++) {
            result.add(round(source.getJSONObject(index)));
        }
        return result;
    }

    private static RoundResult round(JSONObject source) throws JSONException {
        JSONArray array = source.getJSONArray("scores");
        List<RoundScore> scores = new ArrayList<>(array.length());
        for (int index = 0; index < array.length(); index++) {
            JSONObject item = array.getJSONObject(index);
            scores.add(
                    new RoundScore(
                            uuid(item, "playerId"),
                            text(item, "playerName"),
                            item.getLong("scoreDelta"),
                            item.getLong("totalAfter")));
        }
        return new RoundResult(
                uuid(source, "roundId"),
                source.getInt("roundNumber"),
                instant(source, "recordedAt", false),
                scores);
    }

    private static UUID uuid(JSONObject source, String key) throws JSONException {
        try {
            return UUID.fromString(source.getString(key));
        } catch (IllegalArgumentException exception) {
            throw new JSONException(key + " is not a UUID");
        }
    }

    private static Status status(JSONObject source) throws JSONException {
        try {
            return Status.valueOf(source.getString("status"));
        } catch (IllegalArgumentException exception) {
            throw new JSONException("unsupported ledger status");
        }
    }

    private static Instant instant(JSONObject source, String key, boolean optional)
            throws JSONException {
        if (optional && (!source.has(key) || source.isNull(key))) {
            return null;
        }
        try {
            return Instant.parse(source.getString(key));
        } catch (DateTimeParseException exception) {
            throw new JSONException(key + " is not an ISO-8601 instant");
        }
    }

    private static String text(JSONObject source, String key) {
        return !source.has(key) || source.isNull(key) ? "" : source.optString(key, "").trim();
    }
}
