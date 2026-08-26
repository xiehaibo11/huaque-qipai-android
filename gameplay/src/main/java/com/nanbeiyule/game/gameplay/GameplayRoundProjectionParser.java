package com.nanbeiyule.game.gameplay;

import com.nanbeiyule.game.mahjong.TaizhouMahjongVisibleRound;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Parses full seat-private and public-only Mahjong round projections. */
final class GameplayRoundProjectionParser {
    private GameplayRoundProjectionParser() {}

    static Optional<TaizhouMahjongVisibleRound> parseOptionalVisibleRound(JSONObject body)
            throws JSONException {
        if (body == null) {
            return Optional.empty();
        }
        JSONArray sourceHands = body.getJSONArray("hands");
        List<TaizhouMahjongVisibleRound.SeatHand> hands =
                new ArrayList<>(sourceHands.length());
        for (int index = 0; index < sourceHands.length(); index++) {
            JSONObject hand = sourceHands.getJSONObject(index);
            JSONArray concealed = hand.getJSONArray("concealedTiles");
            hands.add(
                    new TaizhouMahjongVisibleRound.SeatHand(
                            hand.getInt("seatNumber"),
                            intList(concealed),
                            hand.has("drawnTile") && !hand.isNull("drawnTile")
                                    ? hand.getInt("drawnTile")
                                    : null,
                            hand.optInt("meldCount", 0)));
        }
        JSONArray sourceRivers = body.optJSONArray("rivers");
        List<TaizhouMahjongVisibleRound.SeatRiver> rivers =
                sourceRivers == null ? null : parseRivers(sourceRivers);
        JSONObject last = body.optJSONObject("lastDiscard");
        TaizhouMahjongVisibleRound.LastDiscard lastDiscard =
                last == null
                        ? null
                        : new TaizhouMahjongVisibleRound.LastDiscard(
                                last.getInt("seatNumber"), last.getInt("tileIndex"));
        Integer dealerSeat =
                body.has("dealerSeat") && !body.isNull("dealerSeat")
                        ? body.getInt("dealerSeat")
                        : null;
        TaizhouMahjongVisibleRound round =
                rivers == null
                        ? new TaizhouMahjongVisibleRound(
                                body.getInt("chairCount"),
                                body.getInt("mySeat"),
                                hands,
                                intList(body.optJSONArray("jokerTiles")),
                                intList(body.optJSONArray("insteadTiles")))
                        : new TaizhouMahjongVisibleRound(
                                body.getInt("chairCount"),
                                body.getInt("mySeat"),
                                hands,
                                intList(body.optJSONArray("jokerTiles")),
                                intList(body.optJSONArray("insteadTiles")),
                                rivers,
                                lastDiscard);
        round = round.withDealerSeat(dealerSeat);
        return Optional.of(round);
    }

    static Optional<TaizhouMahjongVisibleRound> parseOptionalPublicRound(
            JSONObject body,
            int chairCount,
            int mySeat,
            Optional<TaizhouMahjongVisibleRound> previous)
            throws JSONException {
        if (body == null) {
            return Optional.empty();
        }
        int sourceChairCount = body.optInt("chairCount", chairCount);
        if (sourceChairCount != chairCount) {
            throw new JSONException("publicRound chairCount does not match gameplay state");
        }
        List<TaizhouMahjongVisibleRound.SeatHand> hands =
                previous.isPresent() && previous.get().chairCount() == chairCount
                        ? previous.get().hands()
                        : emptyHands(chairCount);
        List<Integer> jokerTiles =
                previous.isPresent() ? previous.get().jokerTiles() : List.of();
        List<Integer> insteadTiles =
                previous.isPresent() ? previous.get().insteadTiles() : List.of();
        JSONArray sourceRivers = body.optJSONArray("rivers");
        List<TaizhouMahjongVisibleRound.SeatRiver> rivers =
                sourceRivers == null
                        ? previous.map(TaizhouMahjongVisibleRound::rivers).orElseGet(
                                () -> emptyRivers(chairCount))
                        : parseRivers(sourceRivers);
        JSONObject last = body.optJSONObject("lastDiscard");
        TaizhouMahjongVisibleRound.LastDiscard lastDiscard =
                last == null
                        ? null
                        : new TaizhouMahjongVisibleRound.LastDiscard(
                                last.getInt("seatNumber"), last.getInt("tileIndex"));
        return Optional.of(
                new TaizhouMahjongVisibleRound(
                        chairCount,
                        mySeat,
                        hands,
                        jokerTiles,
                        insteadTiles,
                        rivers,
                        lastDiscard));
    }

    private static List<TaizhouMahjongVisibleRound.SeatHand> emptyHands(int chairCount) {
        List<TaizhouMahjongVisibleRound.SeatHand> hands = new ArrayList<>(chairCount);
        for (int seat = 1; seat <= chairCount; seat++) {
            hands.add(new TaizhouMahjongVisibleRound.SeatHand(seat, List.of(), null, 0));
        }
        return List.copyOf(hands);
    }

    private static List<TaizhouMahjongVisibleRound.SeatRiver> emptyRivers(int chairCount) {
        List<TaizhouMahjongVisibleRound.SeatRiver> rivers = new ArrayList<>(chairCount);
        int maxLineCount = chairCount == 2 ? 2 : 3;
        for (int seat = 1; seat <= chairCount; seat++) {
            rivers.add(
                    new TaizhouMahjongVisibleRound.SeatRiver(
                            seat, List.of(), maxLineCount));
        }
        return List.copyOf(rivers);
    }

    private static List<TaizhouMahjongVisibleRound.SeatRiver> parseRivers(JSONArray source)
            throws JSONException {
        List<TaizhouMahjongVisibleRound.SeatRiver> rivers = new ArrayList<>(source.length());
        for (int index = 0; index < source.length(); index++) {
            JSONObject river = source.getJSONObject(index);
            rivers.add(
                    new TaizhouMahjongVisibleRound.SeatRiver(
                            river.getInt("seatNumber"),
                            intList(river.getJSONArray("tiles")),
                            river.getInt("maxLineCount")));
        }
        return List.copyOf(rivers);
    }

    private static List<Integer> intList(JSONArray source) throws JSONException {
        if (source == null) {
            return List.of();
        }
        List<Integer> values = new ArrayList<>(source.length());
        for (int index = 0; index < source.length(); index++) {
            values.add(source.getInt(index));
        }
        return List.copyOf(values);
    }
}
