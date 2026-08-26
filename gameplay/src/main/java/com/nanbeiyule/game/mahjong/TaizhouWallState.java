package com.nanbeiyule.game.mahjong;

/** 原版 msgWallMah(529) 与 msgOpenWall(530) 合并后的不可变客户端状态。 */
public record TaizhouWallState(
        int remainingCount,
        int asc,
        int desc,
        int firstAsc,
        int firstDesc,
        boolean showImmediately,
        int openIndex,
        int openTile) {
    public TaizhouWallState {
        if (remainingCount < 0 || remainingCount > 136) {
            throw new IllegalArgumentException("wall remaining count is outside 0..136");
        }
        requireWallIndex(asc, "asc");
        requireWallIndex(desc, "desc");
        requireWallIndex(firstAsc, "firstAsc");
        requireWallIndex(firstDesc, "firstDesc");
        if (openIndex < -1 || openIndex >= 136) {
            throw new IllegalArgumentException("open wall index is outside -1..135");
        }
        if (openTile < -1 || openTile > 0xff) {
            throw new IllegalArgumentException("open wall tile is outside protocol byte range");
        }
    }

    private static void requireWallIndex(int value, String name) {
        if (value < 0 || value >= 136) {
            throw new IllegalArgumentException(name + " is outside 0..135");
        }
    }
}
