package com.nanbeiyule.game.mahjong;

/** Selected frames from the original Common/Image/doublekou_gamelayer.plist. */
public record TaizhouMahjongCommonFrame(
        int atlasX, int atlasY, int width, int height, boolean rotated) {
    public static TaizhouMahjongCommonFrame require(String frameName) {
        return switch (frameName) {
            case "doublekou_sys_bg.png" -> frame(1756, 566, 253, 58, false);
            case "doublekou_roomnum_bg.png" -> frame(1939, 109, 253, 103, true);
            case "doublekou_btn_rule.png" -> frame(821, 976, 70, 67, false);
            case "doublekou_mdd_info_bg.png" -> frame(327, 503, 130, 46, true);
            case "doublekouwifi_icon_3.png" -> frame(998, 243, 29, 21, true);
            case "doublekou_power_bg.png" -> frame(378, 370, 53, 28, true);
            case "doublekou_power_bar.png" -> frame(401, 435, 37, 18, false);
            default ->
                    throw new IllegalArgumentException(
                            "Missing original common game-layer frame " + frameName);
        };
    }

    public int storedWidth() {
        return rotated ? height : width;
    }

    public int storedHeight() {
        return rotated ? width : height;
    }

    private static TaizhouMahjongCommonFrame frame(
            int x, int y, int width, int height, boolean rotated) {
        return new TaizhouMahjongCommonFrame(x, y, width, height, rotated);
    }
}
