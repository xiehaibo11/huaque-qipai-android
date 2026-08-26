package com.nanbeiyule.game;

/**
 * Original Emoji/xiaoduantuier/expression_xiaoduantuier.plist frames in server index order.
 *
 * <p>30109 picks its emoji set through GameFace: GameSub.lua gives the game GameFace = 2, and
 * Chat/View.lua passes that value to GameExpressionConfiger as the ConfID, selecting
 * GameExpression.lua ConfID 2 ("小短腿"). The order below is that entry's ExpressionSelectIcon
 * list verbatim, which is also the on-screen order.
 */
final class TaizhouChatEmojiResources {
    private static final int[] DRAWABLES = {
        R.drawable.taizhou_tool_chat_emoji_1,
        R.drawable.taizhou_tool_chat_emoji_2,
        R.drawable.taizhou_tool_chat_emoji_3,
        R.drawable.taizhou_tool_chat_emoji_4,
        R.drawable.taizhou_tool_chat_emoji_5,
        R.drawable.taizhou_tool_chat_emoji_6,
        R.drawable.taizhou_tool_chat_emoji_7,
        R.drawable.taizhou_tool_chat_emoji_8,
        R.drawable.taizhou_tool_chat_emoji_9,
        R.drawable.taizhou_tool_chat_emoji_10,
        R.drawable.taizhou_tool_chat_emoji_11,
        R.drawable.taizhou_tool_chat_emoji_12,
        R.drawable.taizhou_tool_chat_emoji_13,
        R.drawable.taizhou_tool_chat_emoji_14,
        R.drawable.taizhou_tool_chat_emoji_15,
        R.drawable.taizhou_tool_chat_emoji_16,
        R.drawable.taizhou_tool_chat_emoji_17,
        R.drawable.taizhou_tool_chat_emoji_18,
        R.drawable.taizhou_tool_chat_emoji_19,
        R.drawable.taizhou_tool_chat_emoji_20,
    };

    private TaizhouChatEmojiResources() {}

    static int count() {
        return DRAWABLES.length;
    }

    static int drawableAt(int index) {
        return DRAWABLES[index];
    }
}
