package com.nanbeiyule.game.gameplay;

import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayGesture;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayPermission;
import com.nanbeiyule.game.mahjong.TaizhouMahjongVisibleRound;
import com.nanbeiyule.game.mahjong.TaizhouSettleState;
import com.nanbeiyule.game.mahjong.TaizhouDiceState;
import com.nanbeiyule.game.mahjong.TaizhouWallState;
import com.nanbeiyule.game.mahjong.TaizhouMultipleState;
import com.nanbeiyule.game.mahjong.TaizhouTotalResultState;
import com.nanbeiyule.game.mahjong.round.MahjongCombType;
import com.nanbeiyule.game.mahjong.round.MahjongGameStep;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class GameplayRoundProtocol {
    private GameplayRoundProtocol() {}

    static Optional<TaizhouTotalResultState> parseOptionalTotalResult(JSONObject body)
            throws JSONException {
        if (body == null) {
            return Optional.empty();
        }
        JSONArray sourceSeats = body.optJSONArray("seats");
        List<TaizhouTotalResultState.SeatTotal> seats = new ArrayList<>();
        if (sourceSeats != null) {
            for (int index = 0; index < sourceSeats.length(); index++) {
                JSONObject seat = sourceSeats.getJSONObject(index);
                List<Long> rounds = new ArrayList<>();
                JSONArray sourceRounds = seat.optJSONArray("roundWinLost");
                if (sourceRounds != null) {
                    for (int round = 0; round < sourceRounds.length(); round++) {
                        rounds.add(sourceRounds.getLong(round));
                    }
                }
                List<String> fanNames = new ArrayList<>();
                JSONArray sourceNames = seat.optJSONArray("maxFanNames");
                if (sourceNames != null) {
                    for (int name = 0; name < sourceNames.length(); name++) {
                        fanNames.add(sourceNames.getString(name));
                    }
                }
                seats.add(
                        new TaizhouTotalResultState.SeatTotal(
                                seat.getInt("seatNumber"),
                                rounds,
                                seat.optInt("maxHuCount", 0),
                                seat.optInt("maxFanNum", 0),
                                seat.optInt("maxFanCount", 0),
                                fanNames,
                                seat.optInt("winByOwn", 0),
                                seat.optInt("winScoreNum", 0),
                                seat.optInt("jiePaoNum", 0),
                                seat.optInt("discardNum", 0),
                                seat.optLong("maxScore", 0L),
                                seat.optInt("laZiNum", 0),
                                seat.optInt("chengBaoNum", 0)));
            }
        }
        return Optional.of(
                new TaizhouTotalResultState(
                        body.optInt("playCount", 0), body.optBoolean("show", true), seats));
    }

    static Optional<TaizhouMahjongVisibleRound> parseOptionalVisibleRound(JSONObject body)
            throws JSONException {
        return GameplayRoundProjectionParser.parseOptionalVisibleRound(body);
    }

    static Optional<TaizhouMahjongVisibleRound> parseOptionalPublicRound(
            JSONObject body,
            int chairCount,
            int mySeat,
            Optional<TaizhouMahjongVisibleRound> previous)
            throws JSONException {
        return GameplayRoundProjectionParser.parseOptionalPublicRound(
                body, chairCount, mySeat, previous);
    }

    static Optional<TaizhouMahjongPlayPermission> parseOptionalPlayPermission(JSONObject body)
            throws JSONException {
        if (body == null) {
            return Optional.empty();
        }
        return Optional.of(
                new TaizhouMahjongPlayPermission(
                        body.getString("actionToken"),
                        body.has("mode")
                                ? TaizhouMahjongPlayGesture.Mode.valueOf(body.getString("mode"))
                                : TaizhouMahjongPlayGesture.Mode.SINGLE_CLICK,
                        intSet(body.getJSONArray("playableOriginalIndexes")),
                        intSet(body.optJSONArray("tingOriginalIndexes")),
                        intSet(body.optJSONArray("actionMaskOriginalIndexes")),
                        intSet(body.optJSONArray("preBaoOriginalIndexes"))));
    }


    static Optional<TaizhouMultipleState> parseOptionalMultipleChoice(JSONObject body)
            throws JSONException {
        if (body == null) {
            return Optional.empty();
        }
        Set<TaizhouMultipleState.Choice> allowed = EnumSet.noneOf(TaizhouMultipleState.Choice.class);
        JSONArray sourceAllowed = body.optJSONArray("allowedChoices");
        if (sourceAllowed != null) {
            for (int index = 0; index < sourceAllowed.length(); index++) {
                TaizhouMultipleState.Choice allowedChoice =
                        parseChoice(sourceAllowed.getString(index));
                if (allowedChoice != null) {
                    allowed.add(allowedChoice);
                }
            }
        }
        Map<Integer, TaizhouMultipleState.Choice> seatChoices = new LinkedHashMap<>();
        JSONArray sourceSeatChoices = body.optJSONArray("seatChoices");
        if (sourceSeatChoices != null) {
            for (int index = 0; index < sourceSeatChoices.length(); index++) {
                JSONObject seat = sourceSeatChoices.getJSONObject(index);
                if (!seat.has("choice") || seat.isNull("choice")) {
                    continue;
                }
                // 未知/未来的取值直接跳过：等价于原版的 NONE=0「未操作」，不画标牌。
                TaizhouMultipleState.Choice parsed = parseChoice(seat.getString("choice"));
                if (parsed != null) {
                    seatChoices.put(seat.getInt("seatNumber"), parsed);
                }
            }
        }
        return Optional.of(
                new TaizhouMultipleState(
                        body.optBoolean("goldMode", true),
                        body.optBoolean("choiceActive", false),
                        body.optInt("baseScore", 0),
                        body.optInt("currentMultiplier", 1),
                        body.optInt("cardUseCount", 0),
                        body.optInt("diamondUseCount", 0),
                        body.optInt("mySeat", 1),
                        allowed,
                        seatChoices));
    }

    /**
     * 解析加倍选择。
     *
     * <p>原版 {@code msgAddMulti.ADDMULTITYPE} 的 {@code NONE = 0} 是「未操作加倍」，不是一个
     * 可显示的选择；服务端不应下发，真下发了也按「没选」处理，不画标牌。未知取值同样跳过，
     * 避免版本错位时 {@code valueOf} 直接抛异常打断整帧解析。
     */
    private static TaizhouMultipleState.Choice parseChoice(String value) {
        if (value == null) {
            return null;
        }
        try {
            return TaizhouMultipleState.Choice.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    static Optional<TaizhouDiceState> parseOptionalDiceRoll(JSONObject body)
            throws JSONException {
        if (body == null) {
            return Optional.empty();
        }
        List<Integer> values = intList(body.getJSONArray("nChips"));
        int expectedCount = body.getInt("nCount");
        if (expectedCount != values.size()) {
            throw new JSONException("dice nCount does not match nChips");
        }
        try {
            return Optional.of(
                    new TaizhouDiceState(
                            body.getInt("nSeat"),
                            values,
                            MahjongGameStep.fromValue(body.getInt("gameStep")),
                            body.optBoolean("showAni", false)));
        } catch (IllegalArgumentException exception) {
            throw new JSONException(exception.getMessage());
        }
    }

    static Optional<TaizhouWallState> parseOptionalWallState(JSONObject body)
            throws JSONException {
        if (body == null) {
            return Optional.empty();
        }
        JSONObject wall = body.optJSONObject("wallState");
        if (wall == null) {
            return Optional.empty();
        }
        JSONObject opened = body.optJSONObject("openWall");
        int openIndex = opened == null
                ? wall.optInt("nOpenIndex", -1)
                : opened.getInt("nIndex");
        int openTile = opened == null ? -1 : opened.getInt("nMah");
        try {
            return Optional.of(
                    new TaizhouWallState(
                            wall.getInt("nWallCnt"),
                            wall.getInt("nAsc"),
                            wall.getInt("nDesc"),
                            wall.getInt("nFirstAsc"),
                            wall.getInt("nFirstDesc"),
                            wall.optInt("bShow", 0) != 0 || wall.optBoolean("bShow", false),
                            openIndex,
                            openTile));
        } catch (IllegalArgumentException exception) {
            throw new JSONException(exception.getMessage());
        }
    }

    static Optional<TaizhouSettleState> parseOptionalSettlement(JSONObject body)
            throws JSONException {
        if (body == null) {
            return Optional.empty();
        }
        JSONArray sourceSeats = body.optJSONArray("seats");
        List<TaizhouSettleState.Seat> seats =
                sourceSeats == null ? List.of() : settleSeats(sourceSeats);
        return Optional.of(
                new TaizhouSettleState(
                        TaizhouSettleState.Result.valueOf(body.getString("result")),
                        body.optString("roomNumber", ""),
                        body.optString("roundLabel", ""),
                        body.optString("time", ""),
                        body.optString("gameRule", ""),
                        seats));
    }

    private static List<TaizhouSettleState.Seat> settleSeats(JSONArray source)
            throws JSONException {
        List<TaizhouSettleState.Seat> seats = new ArrayList<>(source.length());
        for (int index = 0; index < source.length(); index++) {
            JSONObject seat = source.getJSONObject(index);
            seats.add(
                    new TaizhouSettleState.Seat(
                            seat.getInt("seatNumber"),
                            seat.optString("displayName", ""),
                            seat.optString("publicPlayerId", ""),
                            seat.optInt("wind", 0),
                            seat.optBoolean("banker", false),
                            seat.optInt("handHu", 0),
                            seat.optInt("tai", 0),
                            seat.optInt("totalHu", 0),
                            seat.optInt("playerState", 0),
                            seat.optInt("fan", 0),
                            seat.optInt("gangScore", 0),
                            seat.optInt("total", 0),
                            seat.optLong("delta", 0L),
                            seat.optBoolean("hasCaishen", false),
                            seat.optBoolean("caishenPropActive", false),
                            seat.optInt("huTile", 0),
                            intList(seat.optJSONArray("handTiles")),
                            parseOptionalMelds(seat.optJSONArray("melds"))));
        }
        return List.copyOf(seats);
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

    private static Set<Integer> intSet(JSONArray source) throws JSONException {
        if (source == null) {
            return Set.of();
        }
        Set<Integer> values = new HashSet<>();
        for (int index = 0; index < source.length(); index++) {
            values.add(source.getInt(index));
        }
        return Set.copyOf(values);
    }

    static Integer optionalActiveSeat(JSONObject body) throws JSONException {
        if (!body.has("activeSeat") || body.isNull("activeSeat")) {
            return null;
        }
        return body.getInt("activeSeat");
    }

    /**
     * Parses the TING_INFO shape
     * {@code {"seat":n,"showFanNum":b,"showHuNum":b,"tingMahs":[...]}} into the
     * discard→huTargets map; a null body means the field is absent. Mirrors the
     * original {@code msgAllWaitInfo} field-for-field.
     */
    static Optional<GameplayTingInfo> parseOptionalTingInfo(JSONObject body)
            throws JSONException {
        if (body == null) {
            return Optional.empty();
        }
        JSONArray sourceTingMahs = body.getJSONArray("tingMahs");
        Map<Integer, List<GameplayTingInfo.HuTarget>> huTargets = new LinkedHashMap<>();
        for (int index = 0; index < sourceTingMahs.length(); index++) {
            JSONObject entry = sourceTingMahs.getJSONObject(index);
            huTargets.put(entry.getInt("discard"), huTargetList(entry));
        }
        return Optional.of(
                new GameplayTingInfo(
                        body.getInt("seat"),
                        body.optBoolean("showFanNum"),
                        body.optBoolean("showHuNum"),
                        huTargets));
    }

    /** {@code nWaitMahs}/{@code nFanPoint}/{@code nHuPoint} 三条平行数组合成一列目标。 */
    private static List<GameplayTingInfo.HuTarget> huTargetList(JSONObject entry)
            throws JSONException {
        List<Integer> tiles = intList(entry.getJSONArray("huTargets"));
        JSONArray fanPoints = entry.optJSONArray("fanPoints");
        JSONArray huPoints = entry.optJSONArray("huPoints");
        List<GameplayTingInfo.HuTarget> targets = new ArrayList<>(tiles.size());
        for (int index = 0; index < tiles.size(); index++) {
            targets.add(
                    new GameplayTingInfo.HuTarget(
                            tiles.get(index),
                            point(fanPoints, index),
                            point(huPoints, index)));
        }
        return targets;
    }

    private static int point(JSONArray points, int index) throws JSONException {
        return points == null || index >= points.length() ? 0 : points.getInt(index);
    }

    /** Reads an optional non-negative count field; absent or JSON null maps to null. */
    static Integer optionalNonNegativeCount(JSONObject body, String name) throws JSONException {
        if (body == null || !body.has(name) || body.isNull(name)) {
            return null;
        }
        int value = body.getInt(name);
        if (value < 0) {
            throw new JSONException(name + " must be non-negative");
        }
        return value;
    }

    static Optional<GameplayActionOffer> parseOptionalActionOffer(JSONObject body)
            throws JSONException {
        if (body == null) {
            return Optional.empty();
        }
        JSONArray sourcePairs = body.optJSONArray("chowCandidates");
        List<int[]> pairs = new ArrayList<>(sourcePairs == null ? 0 : sourcePairs.length());
        if (sourcePairs != null) {
            for (int index = 0; index < sourcePairs.length(); index++) {
                JSONArray pair = sourcePairs.getJSONArray(index);
                if (pair.length() != 3) {
                    throw new JSONException("a chow candidate is a three-tile comb");
                }
                pairs.add(new int[] {pair.getInt(0), pair.getInt(1), pair.getInt(2)});
            }
        }
        JSONArray sourceOptions = body.optJSONArray("kongOptions");
        List<GameplayActionOffer.KongOption> options =
                new ArrayList<>(sourceOptions == null ? 0 : sourceOptions.length());
        if (sourceOptions != null) {
            for (int index = 0; index < sourceOptions.length(); index++) {
                JSONObject option = sourceOptions.getJSONObject(index);
                options.add(
                        new GameplayActionOffer.KongOption(
                                kongTypeOf(option.getString("kongType")),
                                option.getInt("tileValue")));
            }
        }
        return Optional.of(
                new GameplayActionOffer(
                        body.getInt("seat"),
                        body.getInt("powerMask"),
                        body.getString("actionToken"),
                        body.optInt("contextTile", 0),
                        pairs,
                        options,
                        body.getInt("offerId")));
    }

    static List<GameplayMeld> parseOptionalMelds(JSONArray source) throws JSONException {
        if (source == null) {
            return List.of();
        }
        List<GameplayMeld> melds = new ArrayList<>(source.length());
        for (int index = 0; index < source.length(); index++) {
            melds.add(parseMeld(source.getJSONObject(index)));
        }
        return List.copyOf(melds);
    }

    static GameplayMeld parseMeld(JSONObject meld) throws JSONException {
        return new GameplayMeld(
                meld.getInt("seat"),
                combTypeOf(meld.getString("combType")),
                intList(meld.getJSONArray("tiles")),
                meld.getInt("fromSeat"));
    }

    static List<GameplaySeatFlowers> parseOptionalFlowers(JSONArray source) throws JSONException {
        if (source == null) {
            return List.of();
        }
        List<GameplaySeatFlowers> flowers = new ArrayList<>(source.length());
        for (int index = 0; index < source.length(); index++) {
            JSONObject seatFlowers = source.getJSONObject(index);
            flowers.add(
                    new GameplaySeatFlowers(
                            seatFlowers.getInt("seat"),
                            intList(seatFlowers.getJSONArray("tiles"))));
        }
        return List.copyOf(flowers);
    }

    private static GameplayKongType kongTypeOf(String name) throws JSONException {
        try {
            return GameplayKongType.valueOf(name);
        } catch (IllegalArgumentException exception) {
            throw new JSONException("unknown kong type " + name);
        }
    }

    /**
     * The MELD_APPLIED contract only allows the five applied-meld kinds; the
     * DOUBLE/NONE members of the original enum are not valid event payloads.
     */
    private static MahjongCombType combTypeOf(String name) throws JSONException {
        return switch (name) {
            case "CHOW" -> MahjongCombType.CHOW;
            case "PONG" -> MahjongCombType.PONG;
            case "EXPOSED_KONG" -> MahjongCombType.EXPOSED_KONG;
            case "CONCEALED_KONG" -> MahjongCombType.CONCEALED_KONG;
            case "FILL_KONG" -> MahjongCombType.FILL_KONG;
            default -> throw new JSONException("unknown meld comb type " + name);
        };
    }
}
