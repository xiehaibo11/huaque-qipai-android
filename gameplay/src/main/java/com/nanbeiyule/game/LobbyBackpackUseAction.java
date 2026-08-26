package com.nanbeiyule.game;

/** Real inventory operation supplied by the host only when a backend capability exists. */
public interface LobbyBackpackUseAction {
    boolean canUse(LobbyBackpackEntry entry);

    void use(LobbyBackpackEntry entry);
}
