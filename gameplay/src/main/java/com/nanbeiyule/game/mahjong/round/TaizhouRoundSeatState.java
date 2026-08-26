package com.nanbeiyule.game.mahjong.round;

import com.nanbeiyule.game.mahjong.MahjongTile;
import java.util.List;
import java.util.Objects;

/**
 * Per-seat round state of the original {@code GameData} state bus, recovered
 * from {@code src/game/Mahjong/BasicMahjong/Modules/GameLayer/GameData.luac}.
 *
 * <p>Component mapping:
 *
 * <ul>
 *   <li>{@code handTiles} — {@code _handMahData[seat]} (:89, get/set :449-455,:464-481)
 *   <li>{@code drawnTile} — {@code _handDfData[seat]} 单放 (:90, get :661-663),
 *       {@code null} 对应 Lua 的 nil
 *   <li>{@code flowerTiles} — 补花区 {@code _flowerMahData[seat]} (:93, get/set :901-914)
 *   <li>{@code melds} — 副露 {@code _combMahData[seat]} (:92, get/set :767-784)；
 *       每组只保留 Lua comb 表里的 {@code nMahs}
 *       （见 GameData.luac:1114-1128、UIMahLayerBase.luac:462-466）
 *   <li>{@code outTiles} — 四方出牌区 {@code _outCardDataSquare[seat]} (:101, get/set :802-855)
 *   <li>{@code tingTiles} — 听牌 {@code _tingMahs[seat]} (:108, get/set :969-978)
 *   <li>{@code preBaoTiles} — 可能承包牌 {@code _preBaoMahData[seat]} (:111-112, get/set :695-705)
 * </ul>
 *
 * 推断: {@code score} 由服务端结算消息下发，客户端只透传不计算
 * （{@code BasicTaiZhouMahjong/Modules/GameLayer/GameData.luac:15,:70-77} 的
 * {@code _scoreInfo} 是不透明透传表）；本模型只承载数值，不含计分规则。
 */
public record TaizhouRoundSeatState(
        int seatNumber,
        List<Integer> handTiles,
        Integer drawnTile,
        List<Integer> flowerTiles,
        List<Meld> melds,
        List<Integer> outTiles,
        List<Integer> tingTiles,
        List<Integer> preBaoTiles,
        long score) {
    /** Lua comb 表的 {@code nMahs} 成员（GameData.luac:1114-1128）。 */
    public record Meld(List<Integer> tiles) {
        public Meld {
            tiles = copyTiles(tiles, "meld tiles");
        }
    }

    public TaizhouRoundSeatState {
        if (seatNumber <= 0) {
            throw new IllegalArgumentException("seatNumber must be positive");
        }
        handTiles = copyTiles(handTiles, "handTiles");
        if (drawnTile != null) {
            requireValidTile(drawnTile);
        }
        flowerTiles = copyTiles(flowerTiles, "flowerTiles");
        melds = List.copyOf(Objects.requireNonNull(melds, "melds"));
        for (Meld meld : melds) {
            Objects.requireNonNull(meld, "meld");
        }
        outTiles = copyTiles(outTiles, "outTiles");
        tingTiles = copyTiles(tingTiles, "tingTiles");
        preBaoTiles = copyTiles(preBaoTiles, "preBaoTiles");
    }

    public TaizhouRoundSeatState withHandTiles(List<Integer> nextHandTiles) {
        return new TaizhouRoundSeatState(
                seatNumber, nextHandTiles, drawnTile, flowerTiles, melds, outTiles,
                tingTiles, preBaoTiles, score);
    }

    public TaizhouRoundSeatState withDrawnTile(Integer nextDrawnTile) {
        return new TaizhouRoundSeatState(
                seatNumber, handTiles, nextDrawnTile, flowerTiles, melds, outTiles,
                tingTiles, preBaoTiles, score);
    }

    public TaizhouRoundSeatState withFlowerTiles(List<Integer> nextFlowerTiles) {
        return new TaizhouRoundSeatState(
                seatNumber, handTiles, drawnTile, nextFlowerTiles, melds, outTiles,
                tingTiles, preBaoTiles, score);
    }

    public TaizhouRoundSeatState withMelds(List<Meld> nextMelds) {
        return new TaizhouRoundSeatState(
                seatNumber, handTiles, drawnTile, flowerTiles, nextMelds, outTiles,
                tingTiles, preBaoTiles, score);
    }

    public TaizhouRoundSeatState withOutTiles(List<Integer> nextOutTiles) {
        return new TaizhouRoundSeatState(
                seatNumber, handTiles, drawnTile, flowerTiles, melds, nextOutTiles,
                tingTiles, preBaoTiles, score);
    }

    public TaizhouRoundSeatState withTingTiles(List<Integer> nextTingTiles) {
        return new TaizhouRoundSeatState(
                seatNumber, handTiles, drawnTile, flowerTiles, melds, outTiles,
                nextTingTiles, preBaoTiles, score);
    }

    public TaizhouRoundSeatState withPreBaoTiles(List<Integer> nextPreBaoTiles) {
        return new TaizhouRoundSeatState(
                seatNumber, handTiles, drawnTile, flowerTiles, melds, outTiles,
                tingTiles, nextPreBaoTiles, score);
    }

    public TaizhouRoundSeatState withScore(long nextScore) {
        return new TaizhouRoundSeatState(
                seatNumber, handTiles, drawnTile, flowerTiles, melds, outTiles,
                tingTiles, preBaoTiles, nextScore);
    }

    static List<Integer> copyTiles(List<Integer> source, String name) {
        List<Integer> copy = List.copyOf(Objects.requireNonNull(source, name));
        for (Integer tile : copy) {
            requireValidTile(tile);
        }
        return copy;
    }

    private static void requireValidTile(Integer tile) {
        if (tile == null || !MahjongTile.isValid(tile)) {
            throw new IllegalArgumentException("undefined mahjong tile");
        }
    }
}
