package com.nanbeiyule.game.mahjong.protocol;

/**
 * Wire direction of an original protocol message, derived from which stream
 * functions the recovered Lua table implements: {@code bistream} parses
 * server-to-client payloads, {@code bostream} serializes client-to-server
 * payloads. A table with both is bidirectional.
 */
public enum MahjongMessageDirection {
    /** Only {@code bistream}: the client parses it, the server sends it. */
    SERVER_TO_CLIENT,

    /** Only {@code bostream}: the client sends it. */
    CLIENT_TO_SERVER,

    /** Both {@code bistream} and {@code bostream}. */
    BIDIRECTIONAL,

    /**
     * No {@code XY_ID}: a pure sub-structure embedded in other messages
     * (only {@code msgFanData} in the recovered chain).
     */
    STRUCTURE
}
