package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplayTableState;

/** Original 30109 private-room share text adapted to Nanbei's first-party download URL. */
record TaizhouMahjongShareInfo(String title, String description, String webpageUrl) {
    private static final long GAME_ID = 30109L;
    private static final String DOWNLOAD_URL = "https://www.nanbeiyule.com/download";

    static TaizhouMahjongShareInfo from(GameplayTableState state) {
        requireTaizhou(state);
        int currentPlayers = Math.max(1, state.seats().size());
        String title = "台州游戏," + waitingTitle(currentPlayers, state.chairCount());
        String description =
                state.roomNumber()
                        + "/"
                        + state.maxPlayCount()
                        + "局/"
                        + state.chairCount()
                        + "人/"
                        + replaceWechatSensitiveWords(state.gameRuleDisplay());
        return new TaizhouMahjongShareInfo(
                title,
                description,
                DOWNLOAD_URL + "?key=" + state.roomNumber());
    }

    static String copyText(GameplayTableState state, String appName) {
        requireTaizhou(state);
        String rule = filterCopySensitiveWords(state.gameRuleDisplay());
        return "【"
                + state.roomNumber()
                + "】"
                + appName
                + "台州麻将，"
                + state.chairCount()
                + "人/"
                + state.maxPlayCount()
                + "局/"
                + rule
                + "(长按复制消息打开游戏直接进房)\n"
                + "【下载地址】"
                + DOWNLOAD_URL;
    }

    private static String waitingTitle(int currentPlayers, int maxPlayers) {
        int missingPlayers = maxPlayers - currentPlayers;
        if (currentPlayers == 1 && missingPlayers == 1) {
            return "二人对杀速度来！";
        }
        if (currentPlayers == 1 && missingPlayers == 3) {
            return "一等三牌搭子呢?";
        }
        if (currentPlayers == 2 && missingPlayers == 2) {
            return "二拖二好着急呀!";
        }
        if (currentPlayers == 3 && missingPlayers == 1) {
            return "三缺一速来速来!";
        }
        if (maxPlayers == 2 && currentPlayers >= 2) {
            return "二人对杀速度来！";
        }
        return "正在火拼速来组局!";
    }

    private static String filterCopySensitiveWords(String value) {
        return value.replace("支付", "").replace("麻将", "").replace("扑克", "");
    }

    private static String replaceWechatSensitiveWords(String value) {
        return value.replace("麻将", "游戏")
                .replace("斗地主", "抖地主")
                .replace("跑得快", "足包得快")
                .replace("双扣", "双筘")
                .replace("支付", "")
                .replace("防作弊", "强制定位");
    }

    private static void requireTaizhou(GameplayTableState state) {
        if (state == null || state.gameId() != GAME_ID) {
            throw new IllegalArgumentException("Taizhou Mahjong share state is required");
        }
    }
}
