package com.nanbeiyule.game;

record CreateRoomResult(
        String roomNumber,
        String status,
        long gameId,
        String gameRule,
        String roomRule,
        int roomMode,
        int playerCount,
        int playCount,
        String payType,
        long roomFeeCenti) {
    boolean hasSixDigitRoomNumber() {
        return roomNumber != null && roomNumber.matches("\\d{6}");
    }
}
