package com.nanbeiyule.game;

record MatchArenaSummary(
        String id,
        String arenaNumber,
        long lobbyId,
        String areaName,
        String remark,
        String level,
        String mode,
        String costType,
        String role,
        long ownerPublicPlayerId,
        String ownerNickname,
        String ownerAvatarKey,
        long roomCards,
        long dailyRoomCardLimit,
        String status,
        int memberCount,
        int onlineCount,
        boolean duplicate) {}
