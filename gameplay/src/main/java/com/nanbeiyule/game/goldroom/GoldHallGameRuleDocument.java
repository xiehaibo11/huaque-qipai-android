package com.nanbeiyule.game.goldroom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 规则正文文档：一个页签对应一份，由南北娱乐后端下发。
 *
 * <p>原版这块是 {@code RuleView:updateRuleWebView} 里的 WebView，加载
 * {@code UrlConf.GAME_RULE_HTML_ADDR/<渠道>/7128/<GameID>.html}。该页面在浙江服务器上，
 * 归档里没有副本，本项目也不允许请求原版服务，因此正文改由自建后端提供结构化段落，
 * 客户端按 {@link GoldHallGameRuleLayout} 的排版常量原生绘制。弹层外框仍严格按
 * {@code GameRuleLayer.csb} 还原。
 */
public final class GoldHallGameRuleDocument {
    /** 段落类型，对应原版 HTML 里的小标题与正文行。 */
    public enum BlockType {
        HEADING,
        BODY
    }

    /** 一段正文；{@code text} 已是可直接换行排版的纯文本。 */
    public static final class Block {
        private final BlockType type;
        private final String text;

        public Block(BlockType type, String text) {
            this.type = Objects.requireNonNull(type, "type");
            this.text = Objects.requireNonNull(text, "text");
        }

        public BlockType type() {
            return type;
        }

        public String text() {
            return text;
        }
    }

    private final long gameId;
    private final String title;
    private final List<Block> blocks;

    public GoldHallGameRuleDocument(long gameId, String title, List<Block> blocks) {
        this.gameId = gameId;
        this.title = Objects.requireNonNull(title, "title");
        this.blocks = Collections.unmodifiableList(new ArrayList<>(blocks));
    }

    public long gameId() {
        return gameId;
    }

    /** 页签文案；原版取 {@code gameInfo.GameTitle} 写进 {@code _txtName}。 */
    public String title() {
        return title;
    }

    public List<Block> blocks() {
        return blocks;
    }

    public boolean isEmpty() {
        return blocks.isEmpty();
    }
}
