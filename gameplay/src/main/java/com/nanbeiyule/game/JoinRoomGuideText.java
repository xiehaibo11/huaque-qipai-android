package com.nanbeiyule.game;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** FourToTwoActConfig.lua lobby text for area 900023. */
final class JoinRoomGuideText {
    static final List<String> TAIZHOU_TIPS =
            List.of(
                    "2人麻将，组局更快，90%玩家都在玩",
                    "玩2人房间，去福利任务领奖哦~",
                    "据说2人玩法可以防作弊，亲测有效！",
                    "2人房间对局，奖励多多快来试试~",
                    "身边小伙伴都在2人房间，你也试试呗~",
                    "玩2人麻将，再也不怕三缺一");

    private JoinRoomGuideText() {}

    static String randomTaizhouTip() {
        int index = ThreadLocalRandom.current().nextInt(TAIZHOU_TIPS.size());
        return "【小提示】" + TAIZHOU_TIPS.get(index);
    }
}
