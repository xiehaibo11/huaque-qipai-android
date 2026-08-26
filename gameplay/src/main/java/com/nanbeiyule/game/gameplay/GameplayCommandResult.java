package com.nanbeiyule.game.gameplay;

import java.util.List;

/**
 * 牌局命令应答。
 *
 * <p>{@code events} 是服务端在同一事务内算好、对本座位可见的权威事件。客户端拿到应答即可经
 * {@code GameplayEventPlaybackGate} 推进画面，不必等下一次轮询；随后轮询到的同一批事件由
 * {@code GameplayReducer} 按 {@code (revision, eventOrder)} 判重丢弃。
 *
 * <p>这不是乐观更新：客户端仍然只应用服务端下发的权威事件，不本地移牌、不伪造事件。
 * 随应答下发事件属**南北娱乐自建**协议扩展。
 */
public record GameplayCommandResult(
        long revision,
        String eventType,
        int seatNumber,
        boolean ready,
        boolean replayed,
        List<GameplayEvent> events) {
    public GameplayCommandResult {
        events = events == null ? List.of() : List.copyOf(events);
    }

    /** 服务端未随应答下发事件时的应答形状（旧版本后端或不产生可见事件的命令）。 */
    public GameplayCommandResult(
            long revision, String eventType, int seatNumber, boolean ready, boolean replayed) {
        this(revision, eventType, seatNumber, ready, replayed, List.of());
    }
}
