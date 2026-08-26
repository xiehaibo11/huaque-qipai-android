package com.nanbeiyule.game.mahjong;

/** One frame from the original mah_game_layer.plist. */
public record TaizhouMahjongGameLayerFrame(
        int atlasX, int atlasY, int width, int height, boolean rotated) {
    public static TaizhouMahjongGameLayerFrame require(String frameName) {
        int index =
                TaizhouMahjongGameLayerAtlas.indexOf(
                        TaizhouMahjongGameLayerAtlas.GAME_LAYER_NAMES, frameName);
        if (index < 0) {
            throw new IllegalArgumentException("Missing original GameLayer frame " + frameName);
        }
        int[] frame = TaizhouMahjongGameLayerAtlas.GAME_LAYER_FRAMES[index];
        return new TaizhouMahjongGameLayerFrame(
                frame[0], frame[1], frame[2], frame[3], frame[4] != 0);
    }

    public int storedWidth() {
        return rotated ? height : width;
    }

    public int storedHeight() {
        return rotated ? width : height;
    }
}
