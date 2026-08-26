package com.nanbeiyule.game;

/**
 * 玩家信息面板要展示的一位玩家，取自牌桌快照与本地屏蔽开关。
 *
 * <p>{@code PlayerInfo/View.luac:662-676 initKickUser} 的四条隐藏规则全部落在
 * {@link #kickVisible}：开过局、自己不是房主、目标是房主、非包厢房，任一成立都不显示
 * 「请出房间」。
 */
record TaizhouPlayerInfoState(
        int seat,
        String nickname,
        long playerId,
        boolean self,
        boolean host,
        boolean kickVisible,
        boolean membershipActive,
        boolean blockedVoice,
        boolean blockedChat,
        boolean blockedEmojis,
        long diamondBalance,
        long roomCardBalance) {

    /**
     * {@code initKickUser}：{@code playCount>0}、自己非房主、目标是房主、
     * {@code getRoomMode2() ~= BOX_ROOM} 任一成立即隐藏。
     */
    static boolean kickVisible(
            boolean viewerIsHost,
            boolean targetIsHost,
            boolean targetIsSelf,
            int roundNumber,
            boolean boxRoom) {
        return viewerIsHost
                && !targetIsHost
                && !targetIsSelf
                && roundNumber == 0
                && boxRoom;
    }
}
