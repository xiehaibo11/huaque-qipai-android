package com.nanbeiyule.game;

/**
 * Pure rendering policy derived from the real membership level returned by the
 * game-home API.
 */
record AvatarMembershipStyle(
        boolean grayscaleMembershipAssets,
        boolean showActiveEffects) {

    static AvatarMembershipStyle forLevel(int membershipLevel) {
        if (membershipLevel < 0) {
            throw new IllegalArgumentException("membershipLevel must be non-negative");
        }
        boolean active = membershipLevel > 0;
        return new AvatarMembershipStyle(!active, active);
    }
}
