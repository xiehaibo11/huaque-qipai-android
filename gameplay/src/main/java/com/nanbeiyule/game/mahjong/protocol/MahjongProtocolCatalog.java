package com.nanbeiyule.game.mahjong.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Registry of every message declared by the recovered 1.5.4 Taizhou mahjong
 * protocol chain: 72 entries in {@code BasicMahjong} (msgFanData carries no
 * XY_ID), 18 unique names in {@code BasicTaiZhouMahjong} (its msgRequestDismiss
 * is assigned twice at lines 124 and 244 with identical content and is registered
 * once) and 2 in {@code TaiZhouMahjong} — 92 layer-scoped entries over 82
 * distinct XY_ID values. The three cross-name id collisions (1045/1049/1053) are
 * original facts, documented in {@link MahjongMessageId}.
 *
 * <p>Pure semantic registry: XY_ID ↔ model record ↔ direction ↔ layer. No
 * transport or JSON mapping happens here (Wave 2 scope).
 */
public final class MahjongProtocolCatalog {
    /** Sentinel xyId for the only table without an {@code XY_ID} (msgFanData). */
    public static final int XY_ID_NONE = -1;

    /**
     * One layer-scoped message declaration.
     *
     * @param luaName Lua table name, e.g. {@code msgWallMah}
     * @param xyId {@code XY_ID} value or {@link #XY_ID_NONE}
     * @param layer declaring inheritance layer
     * @param direction wire direction from bistream/bostream presence
     * @param modelType semantic model record class
     * @param source recovered {@code file:line} relative to {@code $M}
     */
    public record Entry(String luaName, int xyId, MahjongProtocolLayer layer,
            MahjongMessageDirection direction, Class<?> modelType, String source) {
        public Entry {
            Objects.requireNonNull(luaName, "luaName");
            Objects.requireNonNull(layer, "layer");
            Objects.requireNonNull(direction, "direction");
            Objects.requireNonNull(modelType, "modelType");
            Objects.requireNonNull(source, "source");
        }
    }

    private static final List<Entry> ENTRIES = build();

    private MahjongProtocolCatalog() {}

    /** Returns all 92 layer-scoped entries in declaration order. */
    public static List<Entry> entries() {
        return ENTRIES;
    }

    /** Returns the entries declared by {@code layer}. */
    public static List<Entry> forLayer(MahjongProtocolLayer layer) {
        List<Entry> out = new ArrayList<>();
        for (Entry e : ENTRIES) {
            if (e.layer() == layer) {
                out.add(e);
            }
        }
        return out;
    }

    /** Returns the entry {@code layer/luaName}, throwing when absent. */
    public static Entry find(MahjongProtocolLayer layer, String luaName) {
        for (Entry e : ENTRIES) {
            if (e.layer() == layer && e.luaName().equals(luaName)) {
                return e;
            }
        }
        throw new IllegalArgumentException("no entry for " + layer + "/" + luaName);
    }

    private static void add(List<Entry> out, String luaName, int xyId,
            MahjongProtocolLayer layer, MahjongMessageDirection direction,
            Class<?> modelType, String sourceLine) {
        out.add(new Entry(luaName, xyId, layer, direction, modelType,
                layer.sourceFile() + ":" + sourceLine));
    }

    private static List<Entry> build() {
        List<Entry> out = new ArrayList<>(92);
        basic(out);
        basicTaizhou(out);
        taizhou(out);
        return Collections.unmodifiableList(out);
    }

    private static void basic(List<Entry> o) {
        MahjongProtocolLayer L = MahjongProtocolLayer.BASIC;
        add(o, "msgPlayerTimer", 9, L, MahjongMessageDirection.SERVER_TO_CLIENT, TableStateMessages.MsgPlayerTimer.class, "3");
        add(o, "msgBaseClientForwardEx", 22, L, MahjongMessageDirection.BIDIRECTIONAL, ForwardMessages.MsgBaseClientForwardEx.class, "32");
        add(o, "msgBaseScore", 1053, L, MahjongMessageDirection.SERVER_TO_CLIENT, TableStateMessages.MsgBaseScore.class, "88");
        add(o, "msgFanData", XY_ID_NONE, L, MahjongMessageDirection.STRUCTURE, ResultMessages.MsgFanData.class, "108");
        add(o, "msgStartGame", 513, L, MahjongMessageDirection.SERVER_TO_CLIENT, RoomMessages.MsgStartGame.class, "127");
        add(o, "msgEndGame", 514, L, MahjongMessageDirection.SERVER_TO_CLIENT, RoomMessages.MsgEndGame.class, "145");
        add(o, "msgRelinkEnter", 515, L, MahjongMessageDirection.SERVER_TO_CLIENT, TableStateMessages.MsgRelinkEnter.class, "163");
        add(o, "msgLookerEnter", 516, L, MahjongMessageDirection.SERVER_TO_CLIENT, TableStateMessages.MsgLookerEnter.class, "181");
        add(o, "msgTrust", 517, L, MahjongMessageDirection.BIDIRECTIONAL, ActionMessages.MsgTrust.class, "199");
        add(o, "msgPower", 518, L, MahjongMessageDirection.SERVER_TO_CLIENT, ActionMessages.MsgPower.class, "226");
        add(o, "msgGameStep", 519, L, MahjongMessageDirection.SERVER_TO_CLIENT, TableStateMessages.MsgGameStep.class, "250");
        add(o, "msgClock", 520, L, MahjongMessageDirection.BIDIRECTIONAL, TableStateMessages.MsgClock.class, "268");
        add(o, "msgEndWait", 521, L, MahjongMessageDirection.BIDIRECTIONAL, ActionMessages.MsgEndWait.class, "295");
        add(o, "msgSpecfReq", 522, L, MahjongMessageDirection.BIDIRECTIONAL, SpectateMessages.MsgSpecfReq.class, "322");
        add(o, "msgSpecfData", 523, L, MahjongMessageDirection.SERVER_TO_CLIENT, SpectateMessages.MsgSpecfData.class, "347");
        add(o, "msgSpecfPower", 524, L, MahjongMessageDirection.SERVER_TO_CLIENT, SpectateMessages.MsgSpecfPower.class, "370");
        add(o, "msgSpecfHand", 525, L, MahjongMessageDirection.BIDIRECTIONAL, SpectateMessages.MsgSpecfHand.class, "390");
        add(o, "msgSpecfDanFang", 526, L, MahjongMessageDirection.CLIENT_TO_SERVER, SpectateMessages.MsgSpecfDanFang.class, "425");
        add(o, "msgSpecfWall", 527, L, MahjongMessageDirection.CLIENT_TO_SERVER, SpectateMessages.MsgSpecfWall.class, "445");
        add(o, "msgSpecfEnd", 528, L, MahjongMessageDirection.BIDIRECTIONAL, SpectateMessages.MsgSpecfEnd.class, "467");
        add(o, "msgWallMah", 529, L, MahjongMessageDirection.SERVER_TO_CLIENT, WallMessages.MsgWallMah.class, "491");
        add(o, "msgOpenWall", 530, L, MahjongMessageDirection.SERVER_TO_CLIENT, WallMessages.MsgOpenWall.class, "519");
        add(o, "msgThrowChip", 532, L, MahjongMessageDirection.SERVER_TO_CLIENT, WallMessages.MsgThrowChip.class, "540");
        add(o, "msgTakeFirst", 533, L, MahjongMessageDirection.SERVER_TO_CLIENT, WallMessages.MsgTakeFirst.class, "565");
        add(o, "msgPlayerMah", 534, L, MahjongMessageDirection.SERVER_TO_CLIENT, HandMessages.MsgPlayerMah.class, "583");
        add(o, "msgPlayerBack", 535, L, MahjongMessageDirection.BIDIRECTIONAL, HandMessages.MsgPlayerBack.class, "650");
        add(o, "msgJoker", 536, L, MahjongMessageDirection.SERVER_TO_CLIENT, HandMessages.MsgJoker.class, "743");
        add(o, "msgReplace", 537, L, MahjongMessageDirection.BIDIRECTIONAL, HandMessages.MsgReplace.class, "775");
        add(o, "msgTake", 538, L, MahjongMessageDirection.BIDIRECTIONAL, HandMessages.MsgTake.class, "802");
        add(o, "msgPlay", 539, L, MahjongMessageDirection.BIDIRECTIONAL, PlayMessages.MsgPlay.class, "840");
        add(o, "msgCancel", 540, L, MahjongMessageDirection.CLIENT_TO_SERVER, PlayMessages.MsgCancel.class, "868");
        add(o, "msgHu", 541, L, MahjongMessageDirection.BIDIRECTIONAL, ActionMessages.MsgHu.class, "888");
        add(o, "msgHuEx", 542, L, MahjongMessageDirection.SERVER_TO_CLIENT, ActionMessages.MsgHuEx.class, "913");
        add(o, "msgAction", 543, L, MahjongMessageDirection.BIDIRECTIONAL, ActionMessages.MsgAction.class, "941");
        add(o, "msgPanData", 544, L, MahjongMessageDirection.SERVER_TO_CLIENT, TableStateMessages.MsgPanData.class, "999");
        add(o, "msgTurnData", 545, L, MahjongMessageDirection.SERVER_TO_CLIENT, TableStateMessages.MsgTurnData.class, "1031");
        add(o, "msgFlower", 546, L, MahjongMessageDirection.SERVER_TO_CLIENT, HandMessages.MsgFlower.class, "1068");
        add(o, "msgOutMah", 547, L, MahjongMessageDirection.SERVER_TO_CLIENT, PlayMessages.MsgOutMah.class, "1093");
        add(o, "msgFanCnt", 548, L, MahjongMessageDirection.SERVER_TO_CLIENT, ResultMessages.MsgFanCnt.class, "1120");
        add(o, "msgEndResult", 549, L, MahjongMessageDirection.SERVER_TO_CLIENT, ResultMessages.MsgEndResult.class, "1147");
        add(o, "msgTWait", 550, L, MahjongMessageDirection.BIDIRECTIONAL, ActionMessages.MsgTWait.class, "1165");
        add(o, "msgJustWaiting", 551, L, MahjongMessageDirection.SERVER_TO_CLIENT, ActionMessages.MsgJustWaiting.class, "1189");
        add(o, "msgWaiting", 552, L, MahjongMessageDirection.SERVER_TO_CLIENT, ActionMessages.MsgWaiting.class, "1207");
        add(o, "msgPlayLmts", 553, L, MahjongMessageDirection.SERVER_TO_CLIENT, PlayMessages.MsgPlayLmts.class, "1225");
        add(o, "msgObviousMahsData", 555, L, MahjongMessageDirection.SERVER_TO_CLIENT, TingMessages.MsgObviousMahsData.class, "1250");
        add(o, "msgOutMahRefresh", 556, L, MahjongMessageDirection.SERVER_TO_CLIENT, PlayMessages.MsgOutMahRefresh.class, "1284");
        add(o, "msgAllOutMahRefresh", 557, L, MahjongMessageDirection.SERVER_TO_CLIENT, PlayMessages.MsgAllOutMahRefresh.class, "1309");
        add(o, "msgTest", 1025, L, MahjongMessageDirection.SERVER_TO_CLIENT, SpectateMessages.MsgTest.class, "1333");
        add(o, "msgResult", 1026, L, MahjongMessageDirection.SERVER_TO_CLIENT, ResultMessages.MsgResult.class, "1351");
        add(o, "msgSpeak", 1028, L, MahjongMessageDirection.BIDIRECTIONAL, ForwardMessages.MsgSpeak.class, "1372");
        add(o, "msgServicePay", 1033, L, MahjongMessageDirection.SERVER_TO_CLIENT, TableStateMessages.MsgServicePay.class, "1404");
        add(o, "msgEndType", 1034, L, MahjongMessageDirection.SERVER_TO_CLIENT, ResultMessages.MsgEndType.class, "1422");
        add(o, "msgRoomHostSeat", 1035, L, MahjongMessageDirection.BIDIRECTIONAL, RoomMessages.MsgRoomHostSeat.class, "1442");
        add(o, "msgPlayCount", 1036, L, MahjongMessageDirection.SERVER_TO_CLIENT, RoomMessages.MsgPlayCount.class, "1466");
        add(o, "msgGameRule", 1037, L, MahjongMessageDirection.BIDIRECTIONAL, RoomMessages.MsgGameRule.class, "1486");
        add(o, "msgTotalResult", 1038, L, MahjongMessageDirection.SERVER_TO_CLIENT, ResultMessages.MsgTotalResult.class, "1510");
        add(o, "msgRequestDismiss", 1039, L, MahjongMessageDirection.BIDIRECTIONAL, RoomMessages.MsgRequestDismiss.class, "1549");
        add(o, "msgRespondDismiss", 1040, L, MahjongMessageDirection.BIDIRECTIONAL, RoomMessages.MsgRespondDismiss.class, "1573");
        add(o, "msgDismissFlag", 1041, L, MahjongMessageDirection.BIDIRECTIONAL, RoomMessages.MsgDismissFlag.class, "1599");
        add(o, "msgAvatarUrl", 1042, L, MahjongMessageDirection.BIDIRECTIONAL, ForwardMessages.MsgAvatarUrl.class, "1623");
        add(o, "msgClientForward", 1043, L, MahjongMessageDirection.BIDIRECTIONAL, ForwardMessages.MsgClientForward.class, "1651");
        add(o, "msgGameMaxFan", 1045, L, MahjongMessageDirection.SERVER_TO_CLIENT, ResultMessages.MsgGameMaxFan.class, "1694");
        add(o, "msgResultExtInfo", 1046, L, MahjongMessageDirection.SERVER_TO_CLIENT, ResultMessages.MsgResultExtInfo.class, "1714");
        add(o, "msgFollowMah", 1047, L, MahjongMessageDirection.SERVER_TO_CLIENT, PlayMessages.MsgFollowMah.class, "1735");
        add(o, "msgBetResult", 1049, L, MahjongMessageDirection.SERVER_TO_CLIENT, ResultMessages.MsgBetResult.class, "1762");
        add(o, "msgReqShuffle", 559, L, MahjongMessageDirection.BIDIRECTIONAL, ShuffleMessages.MsgReqShuffle.class, "1802");
        add(o, "msgShuffleSeats", 560, L, MahjongMessageDirection.BIDIRECTIONAL, ShuffleMessages.MsgShuffleSeats.class, "1826");
        add(o, "msgToTalShuffle", 563, L, MahjongMessageDirection.BIDIRECTIONAL, ShuffleMessages.MsgToTalShuffle.class, "1850");
        add(o, "msgShuffleFinish", 561, L, MahjongMessageDirection.BIDIRECTIONAL, ShuffleMessages.MsgShuffleFinish.class, "1883");
        add(o, "msgTingMahInfo", 562, L, MahjongMessageDirection.SERVER_TO_CLIENT, TingMessages.MsgTingMahInfo.class, "1907");
        add(o, "msgAllThrowChip", 564, L, MahjongMessageDirection.SERVER_TO_CLIENT, WallMessages.MsgAllThrowChip.class, "1946");
        add(o, "msgGameRuleUser", 1100, L, MahjongMessageDirection.BIDIRECTIONAL, RoomMessages.MsgGameRuleUser.class, "1973");
    }

    private static void basicTaizhou(List<Entry> o) {
        MahjongProtocolLayer L = MahjongProtocolLayer.BASIC_TAIZHOU;
        add(o, "msgResult", 1026, L, MahjongMessageDirection.SERVER_TO_CLIENT, TaizhouResultMessages.MsgResultTZ.class, "3");
        add(o, "msgTotalResult", 1038, L, MahjongMessageDirection.SERVER_TO_CLIENT, TaizhouResultMessages.MsgTotalResultTZ.class, "53");
        add(o, "msgRequestDismiss", 1039, L, MahjongMessageDirection.BIDIRECTIONAL, TaizhouRoomMessages.MsgRequestDismissTZ.class, "124");
        add(o, "msgTaiName", 1045, L, MahjongMessageDirection.SERVER_TO_CLIENT, TaizhouResultMessages.MsgTaiName.class, "157");
        add(o, "msgWallCnt", 1048, L, MahjongMessageDirection.SERVER_TO_CLIENT, TaizhouFlowMessages.MsgWallCnt.class, "181");
        add(o, "msgShengPaiCnt", 1049, L, MahjongMessageDirection.SERVER_TO_CLIENT, TaizhouFlowMessages.MsgShengPaiCnt.class, "198");
        add(o, "msgSpeak", 1028, L, MahjongMessageDirection.SERVER_TO_CLIENT, TaizhouResultMessages.MsgSpeakTZ.class, "218");
        add(o, "msgLeftBanker", 1050, L, MahjongMessageDirection.SERVER_TO_CLIENT, TaizhouFlowMessages.MsgLeftBanker.class, "277");
        add(o, "msgShuffleSeatsTZ", 1052, L, MahjongMessageDirection.BIDIRECTIONAL, TaizhouRoomMessages.MsgShuffleSeatsTZ.class, "296");
        add(o, "msgTingMahInfo", 562, L, MahjongMessageDirection.BIDIRECTIONAL, TaizhouTingMessages.MsgTingMahInfoTZ.class, "326");
        add(o, "msgTestSingleTingMah", 1053, L, MahjongMessageDirection.SERVER_TO_CLIENT, TaizhouTingMessages.MsgTestSingleTingMah.class, "370");
        add(o, "msgAdvanceStart", 1200, L, MahjongMessageDirection.SERVER_TO_CLIENT, TaizhouFlowMessages.MsgAdvanceStart.class, "400");
        add(o, "msgReqAdvanceStart", 1201, L, MahjongMessageDirection.BIDIRECTIONAL, TaizhouFlowMessages.MsgReqAdvanceStart.class, "421");
        add(o, "msgAdvanceStartFlag", 1202, L, MahjongMessageDirection.SERVER_TO_CLIENT, TaizhouFlowMessages.MsgAdvanceStartFlag.class, "448");
        add(o, "msgReqAdPlayerAgree", 1203, L, MahjongMessageDirection.BIDIRECTIONAL, TaizhouFlowMessages.MsgReqAdPlayerAgree.class, "467");
        add(o, "msgDynamicTableChangeSeat", 1204, L, MahjongMessageDirection.SERVER_TO_CLIENT, TaizhouRoomMessages.MsgDynamicTableChangeSeat.class, "496");
        add(o, "msgAllWaitInfo", 1500, L, MahjongMessageDirection.SERVER_TO_CLIENT, TaizhouTingMessages.MsgAllWaitInfo.class, "520");
        add(o, "msgBaseClientForwardEx", 22, L, MahjongMessageDirection.BIDIRECTIONAL, TaizhouRoomMessages.MsgBaseClientForwardExTZ.class, "567");
    }

    private static void taizhou(List<Entry> o) {
        MahjongProtocolLayer L = MahjongProtocolLayer.TAIZHOU;
        add(o, "msgChengBaoFlag", 1055, L, MahjongMessageDirection.SERVER_TO_CLIENT, TaizhouFlowMessages.MsgChengBaoFlag.class, "3");
        add(o, "msgPreBaoPaiMah", 1501, L, MahjongMessageDirection.SERVER_TO_CLIENT, TaizhouFlowMessages.MsgPreBaoPaiMah.class, "23");
    }
}
