package com.nanbeiyule.game.mahjong;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Projects one original Cocos tile composition into 1920x1080 Android draw commands. */
public final class OriginalMahjongTileDrawPlan {
    public enum Atlas {
        GROUND,
        FACE,
        FACE_TYPE_1,
        ICON
    }

    public static final class Command {
        public final String frameName;
        public final Atlas atlas;
        public final int sourceX;
        public final int sourceY;
        public final int storedWidth;
        public final int storedHeight;
        public final int uprightWidth;
        public final int uprightHeight;
        public final boolean atlasRotated;
        public final float visualLeft;
        public final float visualTop;
        public final float visualRight;
        public final float visualBottom;
        public final float drawLeft;
        public final float drawTop;
        public final float drawRight;
        public final float drawBottom;
        public final int rotationDegrees;
        public final int zOrder;
        public final boolean scale9;
        public final float fixedLeft;
        public final float fixedTop;
        public final float fixedRight;
        public final float fixedBottom;
        public final float nodeScale;

        private Command(
                String frameName,
                Atlas atlas,
                int sourceX,
                int sourceY,
                int uprightWidth,
                int uprightHeight,
                boolean atlasRotated,
                Bounds visual,
                Bounds draw,
                OriginalMahjongTileGeometry.Layer layer,
                float nodeScale) {
            this.frameName = frameName;
            this.atlas = atlas;
            this.sourceX = sourceX;
            this.sourceY = sourceY;
            this.uprightWidth = uprightWidth;
            this.uprightHeight = uprightHeight;
            this.atlasRotated = atlasRotated;
            storedWidth = atlasRotated ? uprightHeight : uprightWidth;
            storedHeight = atlasRotated ? uprightWidth : uprightHeight;
            visualLeft = visual.left;
            visualTop = visual.top;
            visualRight = visual.right;
            visualBottom = visual.bottom;
            drawLeft = draw.left;
            drawTop = draw.top;
            drawRight = draw.right;
            drawBottom = draw.bottom;
            rotationDegrees = layer.rotationDegrees;
            zOrder = layer.zOrder;
            scale9 = layer.scale9;
            fixedLeft = layer.capX;
            fixedTop = uprightHeight - layer.capY - layer.capHeight;
            fixedRight = uprightWidth - layer.capX - layer.capWidth;
            fixedBottom = layer.capY;
            this.nodeScale = nodeScale;
        }
    }

    private static final class Frame {
        final Atlas atlas;
        final int x;
        final int y;
        final int width;
        final int height;
        final boolean rotated;

        Frame(Atlas atlas, int x, int y, int width, int height, boolean rotated) {
            this.atlas = atlas;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.rotated = rotated;
        }
    }

    private static final class Bounds {
        final float left;
        final float top;
        final float right;
        final float bottom;

        Bounds(float left, float top, float right, float bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }

    private OriginalMahjongTileDrawPlan() {}

    public static List<Command> forHandTile(
            TaizhouMahjongHandLayout.TilePosition position, int tileValue) {
        return forHandTile(position, tileValue, false);
    }

    public static List<Command> forHandTile(
            TaizhouMahjongHandLayout.TilePosition position,
            int tileValue,
            boolean joker,
            MahjongTileAppearance appearance) {
        if (position == null) {
            throw new IllegalArgumentException("position is required");
        }
        return atAnchor(
                position.pose,
                tileValue,
                position.designX,
                position.cocosY,
                position.effectiveScale,
                position.anchorX,
                position.anchorY,
                joker,
                appearance);
    }

    public static List<Command> forHandTile(
            TaizhouMahjongHandLayout.TilePosition position, int tileValue, boolean joker) {
        if (position == null) {
            throw new IllegalArgumentException("position is required");
        }
        return atAnchor(
                position.pose,
                tileValue,
                position.designX,
                position.cocosY,
                position.effectiveScale,
                position.anchorX,
                position.anchorY,
                joker);
    }

    public static List<Command> atAnchor(
            int pose,
            int tileValue,
            float nodeX,
            float cocosY,
            float scale,
            float anchorX,
            float anchorY) {
        return atAnchor(pose, tileValue, nodeX, cocosY, scale, anchorX, anchorY, false);
    }

    public static List<Command> atAnchor(
            int pose,
            int tileValue,
            float nodeX,
            float cocosY,
            float scale,
            float anchorX,
            float anchorY,
            boolean joker) {
        return atAnchor(pose, tileValue, nodeX, cocosY, scale, anchorX, anchorY, joker,
                MahjongSettingData.appearance());
    }

    public static List<Command> atAnchor(
            int pose,
            int tileValue,
            float nodeX,
            float cocosY,
            float scale,
            float anchorX,
            float anchorY,
            boolean joker,
            MahjongTileAppearance appearance) {
        requireFinite(nodeX, "nodeX");
        requireFinite(cocosY, "cocosY");
        requireFinite(scale, "scale");
        requireFinite(anchorX, "anchorX");
        requireFinite(anchorY, "anchorY");
        if (scale <= 0.0f) {
            throw new IllegalArgumentException("scale must be positive");
        }

        OriginalMahjongTileGeometry.Composition composition =
                OriginalMahjongTileGeometry.tile(pose, tileValue, appearance);
        float contentLeft = nodeX - anchorX * composition.width * scale;
        float contentBottom = cocosY - anchorY * composition.height * scale;
        List<Command> commands = new ArrayList<>(joker ? 4 : 3);
        add(commands, composition.back, contentLeft, contentBottom, scale, tileValue);
        add(commands, composition.faceGround, contentLeft, contentBottom, scale, tileValue);
        if (composition.face != null) {
            add(commands, composition.face, contentLeft, contentBottom, scale, tileValue);
        }
        if (joker && composition.face != null) {
            add(
                    commands,
                    OriginalMahjongTileGeometry.jokerIcon(composition),
                    contentLeft,
                    contentBottom,
                    scale,
                    tileValue);
        }
        commands.sort(Comparator.comparingInt(command -> command.zOrder));
        return commands;
    }

    private static void add(
            List<Command> commands,
            OriginalMahjongTileGeometry.Layer layer,
            float contentLeft,
            float contentBottom,
            float scale,
            int tileValue) {
        Frame frame = frame(layer, tileValue);
        requireLayerSize(layer, frame);
        Bounds visual = project(layer, contentLeft, contentBottom, scale);
        Bounds draw = unrotatedBounds(visual, layer.rotationDegrees);
        commands.add(
                new Command(
                        layer.frameName,
                        frame.atlas,
                        frame.x,
                        frame.y,
                        frame.width,
                        frame.height,
                        frame.rotated,
                        visual,
                        draw,
                        layer,
                        scale));
    }

    private static Frame frame(OriginalMahjongTileGeometry.Layer layer, int tileValue) {
        if (layer.zOrder == MahjongTileSprite.Z_JOKER_ICON) {
            int index =
                    TaizhouMahjongTableAtlas.indexOf(
                            TaizhouMahjongTableAtlas.ICON_NAMES, layer.frameName);
            if (index < 0) {
                throw new IllegalStateException("missing original icon frame " + layer.frameName);
            }
            int[] icon = TaizhouMahjongTableAtlas.ICON_FRAMES[index];
            return new Frame(
                    Atlas.ICON,
                    icon[0],
                    icon[1],
                    icon[2],
                    icon[3],
                    icon[4] != 0);
        }
        if (layer.zOrder == MahjongTileSprite.Z_FACE) {
            if (layer.frameName.startsWith("mj_mah_face_1_")) {
                int[] face = MahjongFaceType1Atlas.frameOf(tileValue);
                if (face == null) {
                    throw new IllegalArgumentException("missing face_1 for tile " + tileValue);
                }
                return new Frame(
                        Atlas.FACE_TYPE_1,
                        face[0],
                        face[1],
                        TaizhouMahjongFaceAtlas.FACE_WIDTH,
                        TaizhouMahjongFaceAtlas.FACE_HEIGHT,
                        false);
            }
            int[] face = TaizhouMahjongFaceAtlas.frameOf(tileValue);
            if (face == null) {
                throw new IllegalArgumentException("missing Taizhou face for tile " + tileValue);
            }
            return new Frame(
                    Atlas.FACE,
                    face[0],
                    face[1],
                    TaizhouMahjongFaceAtlas.FACE_WIDTH,
                    TaizhouMahjongFaceAtlas.FACE_HEIGHT,
                    true);
        }
        int index =
                TaizhouMahjongGroundAtlas.indexOf(
                        TaizhouMahjongGroundAtlas.GROUND_NAMES, layer.frameName);
        if (index < 0) {
            throw new IllegalStateException("missing original ground frame " + layer.frameName);
        }
        int[] ground = TaizhouMahjongGroundAtlas.GROUND_FRAMES[index];
        return new Frame(
                Atlas.GROUND,
                ground[0],
                ground[1],
                ground[2],
                ground[3],
                ground[4] != 0);
    }

    private static Bounds project(
            OriginalMahjongTileGeometry.Layer layer,
            float contentLeft,
            float contentBottom,
            float scale) {
        float worldLeft = contentLeft + layer.left * scale;
        float worldRight = contentLeft + layer.right * scale;
        float worldBottom = contentBottom + layer.bottom * scale;
        float worldTop = contentBottom + layer.top * scale;
        return new Bounds(
                worldLeft,
                TaizhouMahjongTableLayout.DESIGN_HEIGHT - worldTop,
                worldRight,
                TaizhouMahjongTableLayout.DESIGN_HEIGHT - worldBottom);
    }

    private static Bounds unrotatedBounds(Bounds visual, int rotationDegrees) {
        if (Math.abs(rotationDegrees) != 90) {
            return visual;
        }
        float centerX = (visual.left + visual.right) / 2.0f;
        float centerY = (visual.top + visual.bottom) / 2.0f;
        float width = visual.bottom - visual.top;
        float height = visual.right - visual.left;
        return new Bounds(
                centerX - width / 2.0f,
                centerY - height / 2.0f,
                centerX + width / 2.0f,
                centerY + height / 2.0f);
    }

    private static void requireLayerSize(OriginalMahjongTileGeometry.Layer layer, Frame frame) {
        if (Math.abs(layer.sourceWidth - frame.width) > 0.001f
                || Math.abs(layer.sourceHeight - frame.height) > 0.001f) {
            throw new IllegalStateException("frame geometry mismatch for " + layer.frameName);
        }
    }

    private static void requireFinite(float value, String name) {
        if (!Float.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}
