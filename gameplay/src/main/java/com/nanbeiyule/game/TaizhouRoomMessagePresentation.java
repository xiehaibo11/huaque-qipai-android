package com.nanbeiyule.game;

/** Display-safe projection of one server-authoritative room message. */
record TaizhouRoomMessagePresentation(Kind kind, String text, int emojiIndex) {
    enum Kind { TEXT, EMOJI }

    static TaizhouRoomMessagePresentation from(TaizhouRoomToolsState.Message message) {
        if ("EMOJI".equals(message.type())) {
            return new TaizhouRoomMessagePresentation(Kind.EMOJI, "", message.contentIndex());
        }
        String text =
                "VOICE".equals(message.type())
                        ? "[语音 " + Math.max(1, message.durationMillis() / 1_000) + "秒]"
                        : safe(message.text());
        return new TaizhouRoomMessagePresentation(Kind.TEXT, text, -1);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
