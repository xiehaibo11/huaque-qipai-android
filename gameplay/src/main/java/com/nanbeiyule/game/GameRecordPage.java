package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public record GameRecordPage(
        String date,
        boolean membershipActive,
        List<Long> gameIds,
        Summary summary,
        List<Record> records) {
    public GameRecordPage {
        gameIds = List.copyOf(gameIds);
        records = List.copyOf(records);
    }

    public static GameRecordPage fromJson(JSONObject json) throws JSONException {
        List<Long> gameIds = new ArrayList<>();
        JSONArray gameArray = json.getJSONArray("gameIds");
        for (int index = 0; index < gameArray.length(); index++) {
            gameIds.add(gameArray.getLong(index));
        }
        List<Record> records = new ArrayList<>();
        JSONArray recordArray = json.getJSONArray("records");
        for (int index = 0; index < recordArray.length(); index++) {
            records.add(parseRecord(recordArray.getJSONObject(index)));
        }
        JSONObject summary = json.getJSONObject("summary");
        return new GameRecordPage(
                json.getString("date"),
                json.getBoolean("membershipActive"),
                gameIds,
                new Summary(
                        summary.getInt("championCount"),
                        summary.getLong("score"),
                        summary.getInt("roundCount")),
                records);
    }

    private static Record parseRecord(JSONObject json) throws JSONException {
        List<Player> players = new ArrayList<>();
        JSONArray playerArray = json.getJSONArray("players");
        for (int index = 0; index < playerArray.length(); index++) {
            JSONObject player = playerArray.getJSONObject(index);
            players.add(new Player(
                    player.getLong("publicPlayerId"),
                    player.getString("displayName"),
                    player.getLong("score"),
                    player.getBoolean("host"),
                    player.getBoolean("self")));
        }
        return new Record(
                json.getString("sessionId"),
                json.getString("roomNumber"),
                json.getLong("gameId"),
                json.getString("gameName"),
                json.getBoolean("gold"),
                json.getInt("finishedRounds"),
                json.getInt("totalRounds"),
                json.getString("finishedAt"),
                players);
    }

    public record Summary(int championCount, long score, int roundCount) {}

    public record Record(
            String sessionId,
            String roomNumber,
            long gameId,
            String gameName,
            boolean gold,
            int finishedRounds,
            int totalRounds,
            String finishedAt,
            List<Player> players) {
        public Record {
            players = List.copyOf(players);
        }
    }

    public record Player(
            long publicPlayerId,
            String displayName,
            long score,
            boolean host,
            boolean self) {}
}
