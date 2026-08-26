package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Converts room polling snapshots into one-time playback requests.
 *
 * <p>两类回放来源不同：语音是别人发的才回放（自己录的不回放自己），俏皮话配音则对本轮新出现的
 * 每条都播，包含自己发的——原版 {@code Chat/Module.lua} 收到广播消息后统一调
 * {@code playSoundWisecrack}，不区分发送者。
 */
final class TaizhouIncomingVoiceTracker {
    /** 一次快照带来的新增回放请求。 */
    record Incoming(List<String> voiceMessageIds, List<QuickPhrase> quickPhrases) {
        Incoming {
            voiceMessageIds = List.copyOf(voiceMessageIds);
            quickPhrases = List.copyOf(quickPhrases);
        }

        static Incoming empty() {
            return new Incoming(List.of(), List.of());
        }
    }

    /** 一条待播的俏皮话：目录下标决定音频文件序号。 */
    record QuickPhrase(String messageId, int contentIndex) {}

    private final Set<String> seenMessageIds = new HashSet<>();
    private boolean initialized;

    Incoming accept(List<TaizhouRoomToolsState.Message> messages, String localUserId) {
        List<TaizhouRoomToolsState.Message> safeMessages =
                messages == null ? List.of() : messages;
        if (!initialized) {
            initialized = true;
            for (TaizhouRoomToolsState.Message message : safeMessages) {
                seenMessageIds.add(message.messageId());
            }
            return Incoming.empty();
        }
        List<String> voices = new ArrayList<>();
        List<QuickPhrase> phrases = new ArrayList<>();
        for (TaizhouRoomToolsState.Message message : safeMessages) {
            if (!seenMessageIds.add(message.messageId())) {
                continue;
            }
            if ("VOICE".equals(message.type())) {
                if (!message.senderUserId().equals(localUserId)) {
                    voices.add(message.messageId());
                }
            } else if ("QUICK_PHRASE".equals(message.type())) {
                phrases.add(new QuickPhrase(message.messageId(), message.contentIndex()));
            }
        }
        return new Incoming(voices, phrases);
    }

    void reset() {
        seenMessageIds.clear();
        initialized = false;
    }
}
