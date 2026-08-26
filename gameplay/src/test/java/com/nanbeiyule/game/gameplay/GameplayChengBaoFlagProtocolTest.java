package com.nanbeiyule.game.gameplay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayerLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayerProjection;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public final class GameplayChengBaoFlagProtocolTest {
    @Test
    public void parsesAuthoritativeBooleanAndBinarySeatFlags() throws Exception {
        JSONObject body = snapshotBody(4);
        body.put(
                "chengBaoFlagsBySeat",
                new JSONObject().put("1", false).put("2", true).put("3", 0).put("4", 1));

        GameplaySnapshot snapshot = GameplayApiProtocol.parseSnapshot(body.toString());

        assertEquals(
                Map.of(1, false, 2, true, 3, false, 4, true),
                snapshot.chengBaoFlagsBySeat());
    }

    @Test
    public void missingSeatFlagsDefaultToEmpty() throws Exception {
        GameplaySnapshot snapshot =
                GameplayApiProtocol.parseSnapshot(snapshotBody(4).toString());

        assertTrue(snapshot.chengBaoFlagsBySeat().isEmpty());
    }

    @Test
    public void fourPlayerProjectionShowsOnlyAuthoritativeFlaggedSeat() throws Exception {
        JSONObject body = snapshotBody(4);
        body.put(
                "chengBaoFlagsBySeat",
                new JSONObject().put("1", false).put("2", true).put("3", false).put("4", false));

        List<Integer> visibleSeats =
                TaizhouMahjongPlayerProjection.players(
                                GameplayReducer.fromSnapshot(
                                        GameplayApiProtocol.parseSnapshot(body.toString())))
                        .stream()
                        .filter(TaizhouMahjongPlayerProjection.Player::chengBaoVisible)
                        .map(player -> player.seat().seatNumber())
                        .toList();

        assertEquals(List.of(2), visibleSeats);
    }

    @Test
    public void twoPlayerProjectionNeverShowsChengBaoFlag() throws Exception {
        JSONObject body = snapshotBody(2);
        body.put("chengBaoFlagsBySeat", new JSONObject().put("1", true).put("2", true));

        assertTrue(
                TaizhouMahjongPlayerProjection.players(
                                GameplayReducer.fromSnapshot(
                                        GameplayApiProtocol.parseSnapshot(body.toString())))
                        .stream()
                        .noneMatch(TaizhouMahjongPlayerProjection.Player::chengBaoVisible));
    }

    @Test
    public void chengBaoOverlayKeepsOriginalHeadNodeGeometry() {
        assertEquals(43.2f, TaizhouMahjongPlayerLayout.CHENG_BAO_SIZE, 0.001f);
        assertEquals(43.0f, TaizhouMahjongPlayerLayout.CHENG_BAO_CENTER_OFFSET_Y, 0.001f);
        assertEquals(
                -42.5f,
                TaizhouMahjongPlayerLayout.chengBaoCenterOffsetX(
                        TaizhouMahjongTableLayout.SEAT_LEFT),
                0.001f);
        assertEquals(
                37.5f,
                TaizhouMahjongPlayerLayout.chengBaoCenterOffsetX(
                        TaizhouMahjongTableLayout.SEAT_RIGHT),
                0.001f);
    }

    static JSONObject snapshotBody(int chairCount) throws Exception {
        JSONArray seats = new JSONArray();
        for (int seat = 1; seat <= chairCount; seat++) {
            seats.put(
                    new JSONObject()
                            .put("seatNumber", seat)
                            .put("userId", "user-" + seat)
                            .put("publicPlayerId", 1000 + seat)
                            .put("displayName", "player-" + seat)
                            .put("avatarKey", "avatar_default")
                            .put("score", 10000)
                            .put("host", seat == 1)
                            .put("ready", true)
                            .put("connected", true));
        }
        return new JSONObject()
                .put("sessionId", "session")
                .put("roomNumber", "123456")
                .put("gameId", 30109)
                .put("phase", "PLAYING")
                .put("roundNumber", 1)
                .put("revision", 1)
                .put("chairCount", chairCount)
                .put("maxPlayCount", 8)
                .put("gameRuleDisplay", "")
                .put("autoReady", false)
                .put("mySeat", 1)
                .put("seats", seats)
                .put("remainingWallCount", 60)
                .put("updatedAt", "2026-08-25T00:00:00Z");
    }
}
