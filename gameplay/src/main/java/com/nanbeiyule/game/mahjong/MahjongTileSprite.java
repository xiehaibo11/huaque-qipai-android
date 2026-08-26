package com.nanbeiyule.game.mahjong;

/**
 * Resolves the original atlas frames and nine-patch insets for one drawn tile.
 *
 * <p>Ported from the recovered original
 * {@code BasicMahjong/Modules/GameLayer/Config/UIMahConfig2D.lua}. A tile is
 * drawn as a back sprite plus a face-ground sprite, both taken from
 * {@code mahlayer_mah_ground.plist}, with the tile face itself taken from the
 * per-area {@code mah_face} atlas.
 *
 * <p>Frame names are built from the same string tables the original uses:
 * backs are {@code mahlayer_mahback_<shape>_<color>_<pose>.png} and face grounds
 * are {@code mahlayer_mahface_<shape>_<light>_<pose>.png}.
 */
public final class MahjongTileSprite {
    /** {@code MAH_TYPE.STAND_FACE_FORWARD}: upright, face towards the viewer. */
    public static final int STAND_FACE_FORWARD = 1;

    /** {@code MAH_TYPE.STAND_FACE_BACKWARD}: upright, back towards the viewer. */
    public static final int STAND_FACE_BACKWARD = 2;

    /** {@code MAH_TYPE.STAND_FACE_TOLEFT}. */
    public static final int STAND_FACE_TO_LEFT = 3;

    /** {@code MAH_TYPE.STAND_FACE_TORIGHT}. */
    public static final int STAND_FACE_TO_RIGHT = 4;

    /** {@code MAH_TYPE.LIE_FACE_UP_VERTICAL_TOUP}. */
    public static final int LIE_UP_VERTICAL_UP = 5;

    /** {@code MAH_TYPE.LIE_FACE_UP_VERTICAL_TODOWN}. */
    public static final int LIE_UP_VERTICAL_DOWN = 6;

    /** {@code MAH_TYPE.LIE_FACE_UP_HORIZONTAL_TOLEFT}. */
    public static final int LIE_UP_HORIZONTAL_LEFT = 7;

    /** {@code MAH_TYPE.LIE_FACE_UP_HORIZONTAL_TORIGHT}. */
    public static final int LIE_UP_HORIZONTAL_RIGHT = 8;

    /** {@code MAH_TYPE.LIE_FACE_DOWN_VERTICAL}. */
    public static final int LIE_DOWN_VERTICAL = 9;

    /** {@code MAH_TYPE.LIE_FACE_DOWN_HORIZONTAL}. */
    public static final int LIE_DOWN_HORIZONTAL = 10;

    /** {@code MahGroundShapeStr}. */
    public static final String SHAPE_CIRCLE = "circle";

    public static final String SHAPE_SQUARE = "square";

    /** {@code MahGroundColorStr}. */
    public static final String COLOR_ORANGE = "orange";

    public static final String COLOR_YELLOW = "yellow";

    public static final String COLOR_GREEN = "green";

    public static final String COLOR_BLUE = "blue";

    public static final String COLOR_CHANGE_CARD = "change";

    public static final String COLOR_XGSJ = "xg";

    /** {@code MahGroundLightStr}. */
    public static final String LIGHT_ON = "light";

    public static final String LIGHT_OFF = "dark";

    /** {@code MahSettingDefault}: the engine's out-of-the-box tile appearance. */
    public static final String DEFAULT_SHAPE = SHAPE_CIRCLE;

    public static final String DEFAULT_COLOR = COLOR_GREEN;

    public static final String DEFAULT_LIGHT = LIGHT_ON;

    /**
     * {@code MahSettingDefault} face type is FACE_1, but Taizhou's GameSub entry
     * sets {@code GameFace = 2}, which is why the area package ships
     * {@code mahlayer_mah_face_2}.
     */
    public static final int TAIZHOU_FACE_TYPE = 2;

    /** {@code MahFaceMinScale} / {@code MahFaceMaxScale}. */
    public static final float FACE_MIN_SCALE = 0.85f;

    public static final float FACE_MAX_SCALE = 1.0f;

    /** {@code HandMahMinHeight} / {@code HandMahMaxHeight}. */
    public static final int HAND_MIN_HEIGHT = 170;

    public static final int HAND_MAX_HEIGHT = 190;

    /** {@code MahMaxAddThick}. */
    public static final int MAX_ADD_THICKNESS = 15;

    /** {@code MahZorderConf}. */
    public static final int Z_FACE = 3;

    public static final int Z_JOKER_ICON = 4;

    public static final int Z_ARROW_ICON = 5;

    public static final int Z_TING_ICON = 6;

    public static final int Z_LAST_OUT_FLAG_ICON = 7;

    /** Icon frame names inside {@code mahlayer_mah_icon.plist}. */
    public static final String TING_ICON_FRAME = "mahlayer_mah_img_sign.png";

    public static final String BAO_ICON_FRAME = "mahlayer_mah_img_bao.png";

    public static final String JOKER_ICON_CIRCLE_FRAME = "mahlayer_mah_face_joker_circle.png";

    public static final String JOKER_ICON_SQUARE_FRAME = "mahlayer_mah_face_joker_square.png";

    public static final String FLOWER_ICON_FRAME = "mahlayer_mah_face_flower.png";

    public static final String ANY_TILE_FRAME = "mahlayer_mah_any.png";

    // Pose suffix per MAH_TYPE, as spelled by MahFrameName.
    private static final String[] POSE_SUFFIX = {
        "1_1", // STAND_FACE_FORWARD
        "1_2", // STAND_FACE_BACKWARD
        "1_3", // STAND_FACE_TOLEFT
        "1_4", // STAND_FACE_TORIGHT
        "2_1_1", // LIE_FACE_UP_VERTICAL_TOUP
        "2_1_1", // LIE_FACE_UP_VERTICAL_TODOWN
        "2_1_2", // LIE_FACE_UP_HORIZONTAL_TOLEFT
        "2_1_2", // LIE_FACE_UP_HORIZONTAL_TORIGHT
        "2_2_1", // LIE_FACE_DOWN_VERTICAL
        "2_2_2", // LIE_FACE_DOWN_HORIZONTAL
    };

    // MahTopEdgeWidth
    private static final int[] TOP_EDGE_WIDTH = {
        135, 135, 116, 116, 135, 135, 108, 108, 137, 108,
    };

    // MahLeftRightEdgeWidth
    private static final int[] LEFT_RIGHT_EDGE_WIDTH = {
        135, 169, 75, 75, 169, 169, 174, 174, 158, 177,
    };

    // Back.InitialThick + FaceGround.InitialThick for the default 2D layout.
    private static final int[] DEFAULT_THICKNESS = {
        27, 24, 77, 77, 27, 27, 27, 27, 24, 24,
    };

    private MahjongTileSprite() {}

    /** Returns the back frame for {@code pose} in {@code shape} and {@code color}. */
    public static String backFrame(int pose, String shape, String color) {
        return "mahlayer_mahback_" + shape + "_" + color + "_" + poseSuffix(pose) + ".png";
    }

    /** Returns the face-ground frame for {@code pose} in {@code shape} and {@code light}. */
    public static String faceGroundFrame(int pose, String shape, String light) {
        return "mahlayer_mahface_" + shape + "_" + light + "_" + poseSuffix(pose) + ".png";
    }

    /**
     * Returns the tile-face frame, matching the original {@code MahFrameNameStr}
     * pattern {@code mj_mah_face_%d_%d.png}.
     */
    public static String faceFrame(int faceType, int tileValue) {
        return "mj_mah_face_" + faceType + "_" + tileValue + ".png";
    }

    /** Returns {@code MahTopEdgeWidth} for {@code pose}. */
    public static int topEdgeWidth(int pose) {
        return TOP_EDGE_WIDTH[poseIndex(pose)];
    }

    /** Returns {@code MahLeftRightEdgeWidth} for {@code pose}. */
    public static int leftRightEdgeWidth(int pose) {
        return LEFT_RIGHT_EDGE_WIDTH[poseIndex(pose)];
    }

    /** Returns the unscaled default thickness used for river layer offsets. */
    public static int defaultThickness(int pose) {
        return DEFAULT_THICKNESS[poseIndex(pose)];
    }

    /** Returns the atlas suffix the original uses for {@code pose}. */
    public static String poseSuffix(int pose) {
        return POSE_SUFFIX[poseIndex(pose)];
    }

    private static int poseIndex(int pose) {
        if (pose < STAND_FACE_FORWARD || pose > LIE_DOWN_HORIZONTAL) {
            throw new IllegalArgumentException("unknown tile pose " + pose);
        }
        return pose - 1;
    }
}
