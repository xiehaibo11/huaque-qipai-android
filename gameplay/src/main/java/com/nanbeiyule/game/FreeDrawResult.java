package com.nanbeiyule.game;

record FreeDrawResult(
        String sessionId,
        boolean replayed,
        FreeDrawState.Prize reward,
        int remainingDraws,
        Wallet wallet) {
    record Wallet(long roomCards, long boundRoomCards, long coins, long diamonds) {}
}
