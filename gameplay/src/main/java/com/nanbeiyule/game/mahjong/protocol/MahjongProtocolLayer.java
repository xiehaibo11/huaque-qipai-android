package com.nanbeiyule.game.mahjong.protocol;

/**
 * The three protocol inheritance layers of the original 1.5.4 Taizhou mahjong
 * client: {@code BasicMahjong} is the shared engine layer,
 * {@code BasicTaiZhouMahjong} overrides and extends it for the Taizhou family,
 * and {@code TaiZhouMahjong} (gameid 30109) is the leaf variant. A message name
 * defined again in a deeper layer replaces the shallower definition.
 *
 * <p>{@code sourceFile} is relative to
 * {@code artifacts/zhejiang_game_lobby_1.5.4/recovered/mahjong-1.0.0.687-plain/src/game/Mahjong}.
 */
public enum MahjongProtocolLayer {
    /** {@code game.Mahjong.BasicMahjong.Protocols.GameProtocol}. */
    BASIC("BasicMahjong/Protocols/GameProtocol.luac"),

    /** {@code game.Mahjong.TaiZhou.BasicTaiZhouMahjong.Protocols.GameProtocol}. */
    BASIC_TAIZHOU("TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac"),

    /** {@code game.Mahjong.TaiZhou.TaiZhouMahjong.Protocols.GameProtocol}. */
    TAIZHOU("TaiZhou/TaiZhouMahjong/Protocols/GameProtocol.luac");

    private final String sourceFile;

    MahjongProtocolLayer(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    /** Returns the recovered Lua file (relative to {@code $M}) of this layer. */
    public String sourceFile() {
        return sourceFile;
    }
}
