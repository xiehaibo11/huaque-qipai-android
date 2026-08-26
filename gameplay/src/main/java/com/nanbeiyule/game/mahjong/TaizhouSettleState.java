package com.nanbeiyule.game.mahjong;

import com.nanbeiyule.game.gameplay.GameplayMeld;
import java.util.ArrayList;
import java.util.List;

/**
 * 一局结算的展示数据，字段对应原版 {@code msgResult}（XY_ID 1026）下发给
 * {@code SettleItem.csb} 的各节点。
 *
 * <p>实机对照见 {@code android/docs/evidence/taizhou-live-round-20260811/}。所有数值都由服务端
 * 权威下发，客户端只做展示与文本拼装；在牌墙证据阻断解除前牌局走不到结算，本模型属于提前
 * 就位的展示层。
 */
public record TaizhouSettleState(
        Result result, String roomNumber, String roundLabel, String time, String gameRule,
        List<Seat> seats) {

    public TaizhouSettleState {
        seats = List.copyOf(seats);
    }

    /** {@code _KW_IMG_RESULT} 的横幅，帧名见 {@code settle.plist}；DRAWN 复用原版流局图。 */
    public enum Result {
        ZIMO("settle_text_zimo"),
        DIANPAO("settle_text_dianpao"),
        LIUJU("settle_text_liuju"),
        DRAWN("settle_text_liuju"),
        QIANGGANG("settle_text_qianggang"),
        DISMISS("settle_text_dismiss");

        private final String frameName;

        Result(String frameName) {
            this.frameName = frameName;
        }

        public String frameName() {
            return frameName;
        }
    }

    /**
     * 结算页一行。
     *
     * @param wind 座位风，0..3 对应 {@code settle_feng_0..3}
     * @param handHu {@code nCountHu}，牌面胡数
     * @param tai {@code nCountTai}，台数
     * @param totalHu {@code nToTalCountHu}，总胡数
     * @param playerState {@code nPlayerState}，取值见 {@link TaizhouMahjongSettlement}
     * @param fan 番列数值
     * @param gangScore 杠分列数值
     * @param total 合计列数值
     * @param delta 右侧大号得分
     * @param hasCaishen 计分口径的「手牌含财神」信息位，仅作数据透传
     * @param caishenPropActive 请财神道具是否生效中；原版
     *     {@code WinLost/ItemNode.luac:updateBG} 只在此时显示
     *     {@code _KW_ITEM_BG_CAISHEN}（{@code CaiYunProp:isShowCaiYun} 剩余时间大于 0），
     *     与手牌是否含财神无关
     * @param huTile 胡牌张（原版 {@code dfMahID}），从手牌抽出单放；0 表示无
     *     （{@code MAH_VALUE.NONE}）
     * @param handTiles 结算展示的手牌（不含 {@code huTile} 与副露），牌值编码与
     *     {@link MahjongTile} 一致
     * @param melds 副露（原版 {@code combData}），画在手牌左侧
     */
    public record Seat(
            int seatNumber,
            String displayName,
            String publicPlayerId,
            int wind,
            boolean banker,
            int handHu,
            int tai,
            int totalHu,
            int playerState,
            int fan,
            int gangScore,
            int total,
            long delta,
            boolean hasCaishen,
            boolean caishenPropActive,
            int huTile,
            List<Integer> handTiles,
            List<GameplayMeld> melds) {

        public Seat {
            handTiles = List.copyOf(handTiles);
            melds = List.copyOf(melds);
        }

        /** {@code _KW_LABEL_DETAIL} 的文本，复用已由实机验证的拼装规则。 */
        public String detailText() {
            return TaizhouMahjongSettlement.describeSeat(handHu, tai, totalHu, playerState);
        }

        /** 该行是否为赢家，供胡数拆分和赢家样式使用。 */
        public boolean winner() {
            return TaizhouMahjongSettlement.isWinningState(playerState);
        }

        /** {@code _KW_IMG_END_ICON} 在台州 tableInfo 图集中的原版帧名。 */
        public String endIconFrameName() {
            return TaizhouMahjongSettlement.endIconFrameName(playerState);
        }

        /** 风位角标帧名，{@code settle_feng_0..3}。 */
        public String windFrameName() {
            int safe = Math.floorMod(wind, 4);
            return "settle_feng_" + safe;
        }

        /** 大号得分文本，正数带加号，与实机 {@code +2160} / {@code -1800} 一致。 */
        public String deltaText() {
            return delta > 0 ? "+" + delta : String.valueOf(delta);
        }
    }

    /**
     * 结算行手牌排序，对应原版 {@code MahLogic.sortMahValues}：财神（万能牌）整体排在
     * 最前，其余按牌值（花色×16+点数）升序；财神之间同样按牌值升序。
     */
    public static List<Integer> sortResultHand(List<Integer> handTiles, List<Integer> jokerTiles) {
        List<Integer> sorted = new ArrayList<>(handTiles);
        sorted.sort(
                (left, right) -> {
                    boolean leftJoker = jokerTiles.contains(left);
                    boolean rightJoker = jokerTiles.contains(right);
                    if (leftJoker != rightJoker) {
                        return leftJoker ? -1 : 1;
                    }
                    return Integer.compare(left, right);
                });
        return List.copyOf(sorted);
    }
}
