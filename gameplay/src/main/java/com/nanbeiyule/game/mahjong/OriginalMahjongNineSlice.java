package com.nanbeiyule.game.mahjong;

import java.util.ArrayList;
import java.util.List;

/** Nine-slice rectangles derived from the original Cocos capInsets. */
public final class OriginalMahjongNineSlice {
    public static final class Patch {
        public final int sourceLeft;
        public final int sourceTop;
        public final int sourceRight;
        public final int sourceBottom;
        public final float destinationLeft;
        public final float destinationTop;
        public final float destinationRight;
        public final float destinationBottom;

        private Patch(
                int sourceLeft,
                int sourceTop,
                int sourceRight,
                int sourceBottom,
                float destinationLeft,
                float destinationTop,
                float destinationRight,
                float destinationBottom) {
            this.sourceLeft = sourceLeft;
            this.sourceTop = sourceTop;
            this.sourceRight = sourceRight;
            this.sourceBottom = sourceBottom;
            this.destinationLeft = destinationLeft;
            this.destinationTop = destinationTop;
            this.destinationRight = destinationRight;
            this.destinationBottom = destinationBottom;
        }
    }

    private OriginalMahjongNineSlice() {}

    public static List<Patch> forCommand(OriginalMahjongTileDrawPlan.Command command) {
        if (command == null || !command.scale9) {
            throw new IllegalArgumentException("scale9 command is required");
        }

        int sourceLeftSplit = fixedPixels(command.fixedLeft, command.uprightWidth);
        int sourceTopSplit = fixedPixels(command.fixedTop, command.uprightHeight);
        int sourceRightSplit =
                command.uprightWidth
                        - fixedPixels(command.fixedRight, command.uprightWidth);
        int sourceBottomSplit =
                command.uprightHeight
                        - fixedPixels(command.fixedBottom, command.uprightHeight);
        requireOrdered(sourceLeftSplit, sourceRightSplit, "horizontal source capInsets");
        requireOrdered(sourceTopSplit, sourceBottomSplit, "vertical source capInsets");

        float destinationLeftSplit =
                command.drawLeft + command.fixedLeft * command.nodeScale;
        float destinationTopSplit =
                command.drawTop + command.fixedTop * command.nodeScale;
        float destinationRightSplit =
                command.drawRight - command.fixedRight * command.nodeScale;
        float destinationBottomSplit =
                command.drawBottom - command.fixedBottom * command.nodeScale;
        requireOrdered(
                destinationLeftSplit,
                destinationRightSplit,
                "horizontal destination capInsets");
        requireOrdered(
                destinationTopSplit,
                destinationBottomSplit,
                "vertical destination capInsets");

        int[] sourceX = {0, sourceLeftSplit, sourceRightSplit, command.uprightWidth};
        int[] sourceY = {0, sourceTopSplit, sourceBottomSplit, command.uprightHeight};
        float[] destinationX = {
            command.drawLeft,
            destinationLeftSplit,
            destinationRightSplit,
            command.drawRight,
        };
        float[] destinationY = {
            command.drawTop,
            destinationTopSplit,
            destinationBottomSplit,
            command.drawBottom,
        };

        List<Patch> patches = new ArrayList<>(9);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                patches.add(
                        new Patch(
                                sourceX[column],
                                sourceY[row],
                                sourceX[column + 1],
                                sourceY[row + 1],
                                destinationX[column],
                                destinationY[row],
                                destinationX[column + 1],
                                destinationY[row + 1]));
            }
        }
        return patches;
    }

    private static int fixedPixels(float fixed, int dimension) {
        if (!Float.isFinite(fixed) || fixed < 0.0f || fixed > dimension) {
            throw new IllegalArgumentException("invalid fixed edge " + fixed);
        }
        return Math.min(dimension, (int) Math.floor(fixed));
    }

    private static void requireOrdered(float first, float second, String name) {
        if (first > second) {
            throw new IllegalArgumentException("overlapping " + name);
        }
    }
}
