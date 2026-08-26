package com.nanbeiyule.game.mahjong;

/**
 * 一张牌的外观配置，对应原版 {@code UIMahConfig2D.MahSettingKey} 里影响 2D 牌面的七项。
 *
 * <p>{@code UIMah:setConfig} 收到的就是这些值：牌型（圆/直角）、牌面（亮/暗）、牌背颜色、
 * 牌花类型，以及高度 / 厚度 / 牌花大小三个 0~1 的比例。
 */
public record MahjongTileAppearance(
        int shapeType,
        int lightType,
        int backColorType,
        int faceType,
        float heightRatio,
        float thickRatio,
        float faceSizeRatio) {

    /** {@code MahSettingDefault}：引擎出厂值。 */
    public static MahjongTileAppearance engineDefaults() {
        return new MahjongTileAppearance(1, 1, 3, 1, 0.5f, 0.0f, 0.5f);
    }

    /** {@code MahSettingConfig.lua tab[7109].CUSTOM_STYLE[1]}：台州的运营方案一。 */
    public static MahjongTileAppearance area7109Defaults() {
        return new MahjongTileAppearance(1, 1, 3, 2, 0.0f, 0.2f, 1.0f);
    }

    /** {@code MahGroundShapeStr}。 */
    public String shapeName() {
        return shapeType == 2 ? MahjongTileSprite.SHAPE_SQUARE : MahjongTileSprite.SHAPE_CIRCLE;
    }

    /** {@code MahGroundLightStr}。 */
    public String lightName() {
        return lightType == 2 ? MahjongTileSprite.LIGHT_OFF : MahjongTileSprite.LIGHT_ON;
    }

    /** {@code MahGroundColorStr}。 */
    public String colorName() {
        return switch (backColorType) {
            case 1 -> MahjongTileSprite.COLOR_ORANGE;
            case 2 -> MahjongTileSprite.COLOR_YELLOW;
            case 4 -> MahjongTileSprite.COLOR_BLUE;
            case 5 -> MahjongTileSprite.COLOR_CHANGE_CARD;
            case 6 -> MahjongTileSprite.COLOR_XGSJ;
            default -> MahjongTileSprite.COLOR_GREEN;
        };
    }

    /** {@code HandMahMinHeight + (Max-Min) * HeightRatio}，只有立牌可变。 */
    public float faceGroundHeight() {
        return MahjongTileSprite.HAND_MIN_HEIGHT
                + (MahjongTileSprite.HAND_MAX_HEIGHT - MahjongTileSprite.HAND_MIN_HEIGHT)
                        * clamp(heightRatio);
    }

    /** {@code MahMaxAddThick * ThickRatio}。 */
    public float addedThickness() {
        return MahjongTileSprite.MAX_ADD_THICKNESS * clamp(thickRatio);
    }

    /** {@code MahFaceMinScale + (Max-Min) * FaceSizeRatio}，横躺牌再乘 0.9。 */
    public float faceScale(boolean horizontal) {
        float scale = MahjongTileSprite.FACE_MIN_SCALE
                + (MahjongTileSprite.FACE_MAX_SCALE - MahjongTileSprite.FACE_MIN_SCALE)
                        * clamp(faceSizeRatio);
        if (horizontal) {
            scale = scale * 0.9f;
        }
        return Math.max(MahjongTileSprite.FACE_MIN_SCALE, scale);
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
