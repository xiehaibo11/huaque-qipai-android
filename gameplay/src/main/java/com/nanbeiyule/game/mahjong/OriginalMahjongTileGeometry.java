package com.nanbeiyule.game.mahjong;

/** Exact tile composition ported from the original 2D Mahjong Lua renderer. */
public final class OriginalMahjongTileGeometry {

    public static final class Layer {
        public final String frameName;
        public final float sourceWidth;
        public final float sourceHeight;
        public final float left;
        public final float bottom;
        public final float right;
        public final float top;
        public final int rotationDegrees;
        public final int zOrder;
        public final float bitmapScale;
        public final boolean scale9;
        public final float capX;
        public final float capY;
        public final float capWidth;
        public final float capHeight;

        private Layer(
                String frameName,
                float sourceWidth,
                float sourceHeight,
                float left,
                float bottom,
                float right,
                float top,
                int rotationDegrees,
                int zOrder,
                float bitmapScale,
                CapInsets capInsets) {
            this.frameName = frameName;
            this.sourceWidth = sourceWidth;
            this.sourceHeight = sourceHeight;
            this.left = left;
            this.bottom = bottom;
            this.right = right;
            this.top = top;
            this.rotationDegrees = rotationDegrees;
            this.zOrder = zOrder;
            this.bitmapScale = bitmapScale;
            scale9 = capInsets != null;
            capX = capInsets == null ? 0.0f : capInsets.x;
            capY = capInsets == null ? 0.0f : capInsets.y;
            capWidth = capInsets == null ? 0.0f : capInsets.width;
            capHeight = capInsets == null ? 0.0f : capInsets.height;
        }
    }

    public static final class Composition {
        public final int pose;
        public final float width;
        public final float height;
        public final float rootX;
        public final float rootY;
        public final float thickness;
        public final Layer back;
        public final Layer faceGround;
        public final Layer face;

        private Composition(
                int pose,
                float width,
                float height,
                float rootX,
                float rootY,
                float thickness,
                Layer back,
                Layer faceGround,
                Layer face) {
            this.pose = pose;
            this.width = width;
            this.height = height;
            this.rootX = rootX;
            this.rootY = rootY;
            this.thickness = thickness;
            this.back = back;
            this.faceGround = faceGround;
            this.face = face;
        }
    }

    private static final class CapInsets {
        final float x;
        final float y;
        final float width;
        final float height;

        CapInsets(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private static final class GroundSpec {
        final float anchorX;
        final float anchorY;
        final float positionX;
        final float positionY;
        final float initialThickness;
        final int zOrder;
        final CapInsets capInsets;

        GroundSpec(
                float anchorX,
                float anchorY,
                float positionX,
                float positionY,
                float initialThickness,
                int zOrder,
                CapInsets capInsets) {
            this.anchorX = anchorX;
            this.anchorY = anchorY;
            this.positionX = positionX;
            this.positionY = positionY;
            this.initialThickness = initialThickness;
            this.zOrder = zOrder;
            this.capInsets = capInsets;
        }
    }

    private OriginalMahjongTileGeometry() {}

    /** 用当前生效的外观（{@link MahjongSettingData}）组一张牌。 */
    public static Composition defaultTile(int pose, int tileValue) {
        return tile(pose, tileValue, MahjongSettingData.appearance());
    }

    public static Composition tile(int pose, int tileValue, MahjongTileAppearance appearance) {
        GroundSpec backSpec = backSpec(pose);
        GroundSpec faceGroundSpec = faceGroundSpec(pose);
        String backName =
                MahjongTileSprite.backFrame(
                        pose, appearance.shapeName(), appearance.colorName());
        String faceGroundName =
                MahjongTileSprite.faceGroundFrame(
                        pose, appearance.shapeName(), appearance.lightName());
        int[] backFrame = groundFrame(backName);
        int[] faceGroundFrame = groundFrame(faceGroundName);
        float backWidth = backFrame[2];
        // _updateThick：可变厚度的牌把厚度加在牌背的 contentSize 上。
        float backHeight = backFrame[3] + (canChangeThickness(pose)
                ? appearance.addedThickness() : 0.0f);
        float faceGroundWidth = faceGroundFrame[2];
        // _updateFaceGroundHeight：只有正面立牌可以改高度。
        float faceGroundHeight =
                pose == MahjongTileSprite.STAND_FACE_FORWARD
                        ? appearance.faceGroundHeight()
                        : faceGroundFrame[3];

        float width;
        float height;
        float rootX;
        float rootY;
        if (isSideStanding(pose)) {
            width = backWidth + faceGroundWidth;
            height = backHeight;
            rootX = backSpec.anchorX == 1.0f ? backWidth : faceGroundWidth;
            rootY = backHeight / 2.0f;
        } else {
            GroundSpec topSpec = backSpec.zOrder > faceGroundSpec.zOrder ? backSpec : faceGroundSpec;
            GroundSpec bottomSpec = topSpec == backSpec ? faceGroundSpec : backSpec;
            float topHeight = topSpec == backSpec ? backHeight : faceGroundHeight;
            float bottomHeight = bottomSpec == backSpec ? backHeight : faceGroundHeight;
            if (sameAnchor(backSpec, faceGroundSpec)) {
                height = topHeight + topSpec.positionY + Math.abs(bottomSpec.positionY);
                rootY = Math.abs(bottomSpec.positionY);
            } else {
                height = bottomHeight - Math.abs(bottomSpec.positionY) + topHeight;
                rootY =
                        isStanding(pose)
                                ? height - bottomHeight + Math.abs(bottomSpec.positionY)
                                : bottomHeight - Math.abs(bottomSpec.positionY);
            }
            width = backWidth;
            rootX = backWidth / 2.0f;
        }

        Layer back =
                groundLayer(
                        backName, backFrame, backWidth, backHeight, rootX, rootY, backSpec);
        Layer faceGround =
                groundLayer(
                        faceGroundName,
                        faceGroundFrame,
                        faceGroundWidth,
                        faceGroundHeight,
                        rootX,
                        rootY,
                        faceGroundSpec);
        Layer face =
                showsFace(pose)
                        ? faceLayer(
                                pose,
                                tileValue,
                                rootX,
                                rootY,
                                faceGroundHeight,
                                faceGroundSpec.initialThickness,
                                appearance)
                        : null;
        return new Composition(
                pose,
                width,
                height,
                rootX,
                rootY,
                faceGroundSpec.initialThickness,
                back,
                faceGround,
                face);
    }

    /** {@code UIMah:getThick}：牌背与牌面底的初始厚度，加上当前外观的附加厚度。 */
    public static float thickness(int pose) {
        return thickness(pose, MahjongSettingData.appearance());
    }

    public static float thickness(int pose, MahjongTileAppearance appearance) {
        return MahjongTileSprite.defaultThickness(pose)
                + (canChangeThickness(pose) ? appearance.addedThickness() : 0.0f);
    }

    static Layer jokerIcon(Composition composition) {
        if (composition == null || composition.face == null) {
            return null;
        }
        float iconWidth = 81.0f;
        float iconHeight = 85.0f;
        float centerX;
        float centerY;
        float visualWidth = iconWidth;
        float visualHeight = iconHeight;
        int rotation;
        switch (composition.pose) {
            case MahjongTileSprite.STAND_FACE_FORWARD:
                centerX = iconWidth / 2.0f;
                centerY = composition.height - composition.thickness - iconHeight / 2.0f;
                rotation = 0;
                break;
            case MahjongTileSprite.LIE_UP_VERTICAL_UP:
                centerX = iconWidth / 2.0f;
                centerY = composition.height - iconHeight / 2.0f;
                rotation = 0;
                break;
            case MahjongTileSprite.LIE_UP_VERTICAL_DOWN:
                centerX = composition.width - iconWidth / 2.0f;
                centerY = composition.thickness + iconHeight / 2.0f;
                rotation = 180;
                break;
            case MahjongTileSprite.LIE_UP_HORIZONTAL_LEFT:
                centerX = iconHeight / 2.0f;
                centerY = composition.thickness + iconWidth / 2.0f;
                visualWidth = iconHeight;
                visualHeight = iconWidth;
                rotation = -90;
                break;
            case MahjongTileSprite.LIE_UP_HORIZONTAL_RIGHT:
                centerX = composition.width - iconHeight / 2.0f;
                centerY = composition.height - iconWidth / 2.0f;
                visualWidth = iconHeight;
                visualHeight = iconWidth;
                rotation = 90;
                break;
            default:
                return null;
        }
        return new Layer(
                MahjongTileSprite.JOKER_ICON_CIRCLE_FRAME,
                iconWidth,
                iconHeight,
                centerX - visualWidth / 2.0f,
                centerY - visualHeight / 2.0f,
                centerX + visualWidth / 2.0f,
                centerY + visualHeight / 2.0f,
                rotation,
                MahjongTileSprite.Z_JOKER_ICON,
                1.0f,
                null);
    }

    private static Layer groundLayer(
            String name,
            int[] frame,
            float width,
            float height,
            float rootX,
            float rootY,
            GroundSpec spec) {
        float left = rootX + spec.positionX - spec.anchorX * width;
        float bottom = rootY + spec.positionY - spec.anchorY * height;
        return new Layer(
                name,
                frame[2],
                frame[3],
                left,
                bottom,
                left + width,
                bottom + height,
                0,
                spec.zOrder,
                1.0f,
                spec.capInsets);
    }

    private static Layer faceLayer(
            int pose,
            int tileValue,
            float rootX,
            float rootY,
            float faceGroundHeight,
            float initialThickness,
            MahjongTileAppearance appearance) {
        if (TaizhouMahjongFaceAtlas.frameOf(tileValue) == null) {
            throw new IllegalArgumentException("missing Taizhou face for tile " + tileValue);
        }
        int rotation = faceRotation(pose);
        float scale = appearance.faceScale(isHorizontalFaceUp(pose));
        float centerY =
                rootY
                        + (isStanding(pose)
                                ? -initialThickness
                                        - (faceGroundHeight - initialThickness) / 2.0f
                                : initialThickness
                                        + (faceGroundHeight - initialThickness) / 2.0f);
        float width =
                Math.abs(rotation) == 90
                        ? TaizhouMahjongFaceAtlas.FACE_HEIGHT * scale
                        : TaizhouMahjongFaceAtlas.FACE_WIDTH * scale;
        float height =
                Math.abs(rotation) == 90
                        ? TaizhouMahjongFaceAtlas.FACE_WIDTH * scale
                        : TaizhouMahjongFaceAtlas.FACE_HEIGHT * scale;
        return new Layer(
                MahjongTileSprite.faceFrame(appearance.faceType(), tileValue),
                TaizhouMahjongFaceAtlas.FACE_WIDTH,
                TaizhouMahjongFaceAtlas.FACE_HEIGHT,
                rootX - width / 2.0f,
                centerY - height / 2.0f,
                rootX + width / 2.0f,
                centerY + height / 2.0f,
                rotation,
                MahjongTileSprite.Z_FACE,
                scale,
                null);
    }

    private static int[] groundFrame(String name) {
        int index =
                TaizhouMahjongGroundAtlas.indexOf(
                        TaizhouMahjongGroundAtlas.GROUND_NAMES, name);
        if (index < 0) {
            throw new IllegalStateException("missing original ground frame " + name);
        }
        return TaizhouMahjongGroundAtlas.GROUND_FRAMES[index];
    }

    /** {@code MahLogic.isCanChangeThick}：左右立牌之外都能加厚度。 */
    private static boolean canChangeThickness(int pose) {
        return pose != MahjongTileSprite.STAND_FACE_TO_LEFT
                && pose != MahjongTileSprite.STAND_FACE_TO_RIGHT;
    }

    private static boolean showsFace(int pose) {
        return pose == MahjongTileSprite.STAND_FACE_FORWARD
                || pose == MahjongTileSprite.LIE_UP_VERTICAL_UP
                || pose == MahjongTileSprite.LIE_UP_VERTICAL_DOWN
                || isHorizontalFaceUp(pose);
    }

    private static boolean isStanding(int pose) {
        return pose >= MahjongTileSprite.STAND_FACE_FORWARD
                && pose <= MahjongTileSprite.STAND_FACE_TO_RIGHT;
    }

    private static boolean isSideStanding(int pose) {
        return pose == MahjongTileSprite.STAND_FACE_TO_LEFT
                || pose == MahjongTileSprite.STAND_FACE_TO_RIGHT;
    }

    private static boolean isHorizontalFaceUp(int pose) {
        return pose == MahjongTileSprite.LIE_UP_HORIZONTAL_LEFT
                || pose == MahjongTileSprite.LIE_UP_HORIZONTAL_RIGHT;
    }

    private static boolean sameAnchor(GroundSpec first, GroundSpec second) {
        return first.anchorX == second.anchorX && first.anchorY == second.anchorY;
    }

    private static int faceRotation(int pose) {
        switch (pose) {
            case MahjongTileSprite.LIE_UP_VERTICAL_DOWN:
                return 180;
            case MahjongTileSprite.LIE_UP_HORIZONTAL_LEFT:
                return -90;
            case MahjongTileSprite.LIE_UP_HORIZONTAL_RIGHT:
                return 90;
            default:
                return 0;
        }
    }

    private static GroundSpec backSpec(int pose) {
        switch (pose) {
            case MahjongTileSprite.STAND_FACE_FORWARD:
                return spec(0.5f, 0.0f, 0.0f, -48.0f, 12.0f, 0, cap(45, 47, 45, 1));
            case MahjongTileSprite.STAND_FACE_BACKWARD:
                return spec(0.5f, 1.0f, 0.0f, 0.0f, 12.0f, 1, cap(50, 12, 38, 0.01f));
            case MahjongTileSprite.STAND_FACE_TO_LEFT:
                return spec(0.0f, 0.5f, 0.0f, 0.0f, 39.0f, 1, null);
            case MahjongTileSprite.STAND_FACE_TO_RIGHT:
                return spec(1.0f, 0.5f, 0.0f, 0.0f, 39.0f, 1, null);
            case MahjongTileSprite.LIE_UP_VERTICAL_UP:
            case MahjongTileSprite.LIE_UP_VERTICAL_DOWN:
                return spec(0.5f, 1.0f, 0.0f, 49.0f, 12.0f, 0, cap(45, 20, 50, 28));
            case MahjongTileSprite.LIE_UP_HORIZONTAL_LEFT:
            case MahjongTileSprite.LIE_UP_HORIZONTAL_RIGHT:
                return spec(0.5f, 1.0f, -1.0f, 48.0f, 12.0f, 0, cap(45, 20, 50, 28));
            case MahjongTileSprite.LIE_DOWN_VERTICAL:
                return spec(0.5f, 0.0f, 0.0f, 0.0f, 12.0f, 1, cap(50, 158, 38, 0.01f));
            case MahjongTileSprite.LIE_DOWN_HORIZONTAL:
                return spec(0.5f, 0.0f, 0.0f, 0.0f, 12.0f, 1, cap(50, 108, 77, 0.01f));
            default:
                throw new IllegalArgumentException("unknown tile pose " + pose);
        }
    }

    private static GroundSpec faceGroundSpec(int pose) {
        switch (pose) {
            case MahjongTileSprite.STAND_FACE_FORWARD:
                return spec(0.5f, 1.0f, 0.0f, 0.0f, 15.0f, 1, cap(45, 20, 50, 28));
            case MahjongTileSprite.STAND_FACE_BACKWARD:
                return spec(0.5f, 0.0f, 0.0f, -48.0f, 12.0f, 0, null);
            case MahjongTileSprite.STAND_FACE_TO_LEFT:
                return spec(1.0f, 0.5f, 0.0f, 0.0f, 38.0f, 0, null);
            case MahjongTileSprite.STAND_FACE_TO_RIGHT:
                return spec(0.0f, 0.5f, 0.0f, 0.0f, 38.0f, 0, null);
            case MahjongTileSprite.LIE_UP_VERTICAL_UP:
            case MahjongTileSprite.LIE_UP_VERTICAL_DOWN:
            case MahjongTileSprite.LIE_UP_HORIZONTAL_LEFT:
            case MahjongTileSprite.LIE_UP_HORIZONTAL_RIGHT:
                return spec(0.5f, 0.0f, 0.0f, 0.0f, 15.0f, 1, null);
            case MahjongTileSprite.LIE_DOWN_VERTICAL:
            case MahjongTileSprite.LIE_DOWN_HORIZONTAL:
                return spec(0.5f, 0.0f, 0.0f, -12.0f, 12.0f, 0, null);
            default:
                throw new IllegalArgumentException("unknown tile pose " + pose);
        }
    }

    private static GroundSpec spec(
            float anchorX,
            float anchorY,
            float positionX,
            float positionY,
            float initialThickness,
            int zOrder,
            CapInsets capInsets) {
        return new GroundSpec(
                anchorX,
                anchorY,
                positionX,
                positionY,
                initialThickness,
                zOrder,
                capInsets);
    }

    private static CapInsets cap(float x, float y, float width, float height) {
        return new CapInsets(x, y, width, height);
    }
}
