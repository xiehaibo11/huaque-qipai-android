package com.nanbeiyule.game;

import java.time.Instant;

record FreeDrawSession(
        String sessionId, String userCustomData, String adPlacementId, Instant expiresAt) {}
