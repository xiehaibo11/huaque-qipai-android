package com.nanbeiyule.game.gameplay;

import java.util.Objects;

public record GameplaySeat(
        int seatNumber,
        String userId,
        long publicPlayerId,
        String displayName,
        String avatarKey,
        long score,
        boolean host,
        boolean ready,
        boolean connected) {
    public GameplaySeat {
        if (seatNumber <= 0 || publicPlayerId <= 0) {
            throw new IllegalArgumentException("invalid gameplay seat identity");
        }
        userId = Objects.requireNonNull(userId, "userId");
        displayName = Objects.requireNonNull(displayName, "displayName");
        avatarKey = Objects.requireNonNull(avatarKey, "avatarKey");
        if (userId.isBlank() || displayName.isBlank() || avatarKey.isBlank()) {
            throw new IllegalArgumentException("incomplete gameplay seat profile");
        }
    }

    public GameplaySeat(
            int seatNumber,
            String userId,
            long publicPlayerId,
            String displayName,
            String avatarKey,
            boolean host,
            boolean ready,
            boolean connected) {
        this(
                seatNumber,
                userId,
                publicPlayerId,
                displayName,
                avatarKey,
                1000L,
                host,
                ready,
                connected);
    }


    GameplaySeat withScore(long nextScore) {
        return new GameplaySeat(
                seatNumber,
                userId,
                publicPlayerId,
                displayName,
                avatarKey,
                nextScore,
                host,
                ready,
                connected);
    }

    GameplaySeat withReady(boolean nextReady) {
        return new GameplaySeat(
                seatNumber,
                userId,
                publicPlayerId,
                displayName,
                avatarKey,
                score,
                host,
                nextReady,
                connected);
    }
}
