package com.nanbeiyule.game;

import java.util.List;

/** Text transcribed from the recovered jkxz_text.png so it can use the licensed app font. */
final class HealthNoticeContent {
    record Block(String text, boolean heading) {}

    private static final List<Block> BLOCKS =
            List.of(
                    new Block("健康游戏须知", true),
                    new Block(
                            "1、公司始终为用户提供公平公正、健康绿色的游戏环境。游戏中所有道具不具任何财产性功能，仅限用户本人使用。",
                            false),
                    new Block(
                            "2、游戏中使用的房卡、钻石均为游戏道具，公司将不提供任何形式的官方回购，并禁止直接或变相兑换现金等服务。",
                            false),
                    new Block(
                            "3、禁止利用游戏产品从事赌博、非法交易等违法违规活动。一旦发现，封号处理，保留问责，决不姑息。请用户文明游戏，远离赌博。",
                            false),
                    new Block(
                            "4、公司致力于为用户打造优质的线上游戏及线下好友互动体验的平台，不参与、不组织、不指导任何线下游戏，请用户提高警惕，以防上当！",
                            false),
                    new Block("不良信息举报公告内容", true),
                    new Block("1、禁止任何利用本游戏进行赌博的行为。", false),
                    new Block(
                            "2、游戏中禁止传播危害国家安全、荣誉和利益，以及色情淫秽、暴力的信息。",
                            false),
                    new Block(
                            "3、游戏中禁止散布违反国家法律、扰乱社会秩序以及破坏国家领土和主权完整的谣言。",
                            false),
                    new Block(
                            "如果发现以上行为请及时在（在线客服）进行反馈，让我们一起净化游戏环境。",
                            false));

    private HealthNoticeContent() {}

    static List<Block> blocks() {
        return BLOCKS;
    }

    static String accessibilityText() {
        StringBuilder text = new StringBuilder();
        for (Block block : BLOCKS) {
            if (text.length() > 0) {
                text.append('\n');
            }
            text.append(block.text());
        }
        return text.toString();
    }
}
