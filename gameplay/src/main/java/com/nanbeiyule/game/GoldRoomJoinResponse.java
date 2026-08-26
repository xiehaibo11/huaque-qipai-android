package com.nanbeiyule.game;

/** Response from the first-party gold-room join endpoint. */
record GoldRoomJoinResponse(
        String code,
        String status,
        String roomMode,
        long lobbyId,
        long gameId,
        long boxGameId,
        int roomNameFlag,
        int sessionId,
        int chairCount,
        long baseScore,
        boolean dynamicCost,
        long minRich,
        long maxRich,
        String matchingTicketId,
        String message,
        String roomNumber,
        boolean autoGameplay,
        boolean replay) {}
