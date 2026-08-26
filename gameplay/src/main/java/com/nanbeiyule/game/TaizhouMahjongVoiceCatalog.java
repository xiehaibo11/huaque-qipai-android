package com.nanbeiyule.game;

import com.nanbeiyule.game.mahjong.round.MahjongCombType;
import java.util.Arrays;

/**
 * 台州麻将 30109 牌局音频目录：牌值/动作 → {@code res/raw} 资源名映射。
 *
 * <p>报牌语音按原版 {@code SoundManager:playSoundMah}
 * （BasicMahjong/Manager/SoundManager.luac:119-146）：有 gameID 时从
 * {@code res/audio/Mahjong/MahLayer/<gameID>/Mah/<Man|Women>/<mahID>.mp3} 取牌音；
 * 30109 运行时实际资源由 MuMu 中已登录的原版应用导出，文件名仍是牌值十进制
 * （0x11=17 一万 … 0x53=83 白板）。本客户端固定使用原版 30109 方言资源，避免回退到
 * {@code Base/Mah} 的普通话目录。花牌等无对应文件的牌值经 {@code playEffect} 的
 * isFileExist 检查静默跳过（GameBase/Manager/SoundManager.lua:86-94），本目录同样映射为 null。
 *
 * <p>动作语音按 {@code SoundManager:playSoundMahAction}（SoundManager.luac:147-173）：
 * 动作序号即 {@code GameDefine.COMB_TYPE}（BasicMahjong/Define/GameDefine.luac:149-157），
 * 文件为 {@code MahLayer/30109/MahAction/<Man|Women>/<1..5>.mp3}；补花使用
 * {@code flower.mp3}；胡牌音效 hu_1/hu_2 按
 * WinLost/Module.luac:263-270 的 sEndType 映射；hu_3.mp3 在归档内但全部恢复 Lua 均无引用。
 *
 * <p>操作音按 SoundManager.luac:290-327 的七个函数恢复。
 */
public final class TaizhouMahjongVoiceCatalog {
    /** 原版 30109 MahLayer/Mah 目录存在方言语音的 34 个牌值（十进制），升序。 */
    private static final int[] DISCARD_VOICE_TILES = {
        17, 18, 19, 20, 21, 22, 23, 24, 25,
        33, 34, 35, 36, 37, 38, 39, 40, 41,
        49, 50, 51, 52, 53, 54, 55, 56, 57,
        65, 66, 67, 68,
        81, 82, 83,
    };

    /** 胡牌结局类型，对应 WinLost/Module.luac:263-270 的 sEndType 分支。 */
    public enum WinKind {
        /** {@code END_TYPE.ET_SELF} 自摸 → hu_1.mp3。 */
        SELF_DRAWN,
        /** {@code END_TYPE.ET_DISCARD / ET_ROBKONG} 点炮、抢杠 → hu_2.mp3。 */
        DISCARD,
    }

    /**
     * 原版 {@code res/audio/Mahjong/Sound/} 七个操作音（SoundManager.luac:290-327）。
     * piaocai.mp3 在归档内但全部恢复 Lua 均无挂点，仅保留目录映射。
     */
    public enum TableSound {
        /** start.mp3 — playSoundStart :295-298，开局 GameLayer/Module.luac:230。 */
        DEAL_START("taizhou_mahjong_sound_start"),
        /** end.mp3 — playSoundEnd :300-303，小结束 TaiZhou WinLost/Module.luac:8。 */
        ROUND_END("taizhou_mahjong_sound_end"),
        /** Button.mp3 — playButtonClick :290-293，牌局按钮 GoldView.luac:458。 */
        BUTTON_CLICK("taizhou_mahjong_sound_button"),
        /** Chip.mp3 — playSoundChip :305-311，骰子动画 AnimationLayer.luac:137/177/183。 */
        DICE_ROLL("taizhou_mahjong_sound_chip"),
        /** Out.mp3 — playSoundOut :313-319，出牌落点 UIMahLayer.luac:272/310。 */
        DISCARD_WHOOSH("taizhou_mahjong_sound_out"),
        /** Clock.mp3 — playSoundClock :321-327，倒计时 ≤2 秒 TableClockView.luac:259/354。 */
        CLOCK_TICK("taizhou_mahjong_sound_clock"),
        /** shengpai.mp3 — 首次进入生牌阶段，TaiZhou GameLayer/Module.lua:35-39。 */
        SHENG_PAI("taizhou_mahjong_sound_shengpai"),
        /** piaocai.mp3 — 归档存在但无 Lua 挂点证据。 */
        PIAOCAI("taizhou_mahjong_sound_piaocai");

        private final String resourceName;

        TableSound(String resourceName) {
            this.resourceName = resourceName;
        }

        /** Returns the {@code res/raw} stem of this effect. */
        public String resourceName() {
            return resourceName;
        }
    }

    private TaizhouMahjongVoiceCatalog() {}

    /** Returns whether the original archive ships a discard voice for this tile value. */
    public static boolean hasDiscardVoice(int tileValue) {
        return Arrays.binarySearch(DISCARD_VOICE_TILES, tileValue) >= 0;
    }

    /** Returns a defensive copy of the complete original 30109 tile voice set. */
    public static int[] discardVoiceTileValues() {
        return DISCARD_VOICE_TILES.clone();
    }

    /**
     * 30109 方言报牌语音资源名；无语音牌值返回 null（原版 playEffect 缺失静默）。
     * 挂点：出牌消息处理 GameLayer/Module.luac:743。
     */
    public static String discardVoiceResource(boolean male, int tileValue) {
        if (!hasDiscardVoice(tileValue)) {
            return null;
        }
        return "taizhou_mahjong_voice_dialect_" + (male ? "man_" : "women_") + tileValue;
    }

    /**
     * Standard Mandarin resource name retained for evidence and explicit fallback tooling.
     * Production Taizhou playback uses {@link #discardVoiceResource(boolean, int)} instead.
     */
    public static String standardDiscardVoiceResource(boolean male, int tileValue) {
        if (!hasDiscardVoice(tileValue)) {
            return null;
        }
        return "taizhou_mahjong_voice_" + (male ? "man_" : "women_") + tileValue;
    }

    /**
     * 30109 方言吃碰杠动作语音资源名；{@code COMB_TYPE.NONE}/{@code DOUBLE} 无对应文件返回 null。
     * 挂点：onMsgAction 的 playSoundMahAction(convertFlag, …) GameLayer/Module.luac:794-811。
     */
    public static String meldVoiceResource(boolean male, MahjongCombType combType) {
        int value = combType.value();
        if (value < 1 || value > 5) {
            return null;
        }
        return "taizhou_mahjong_action_dialect_" + (male ? "man_" : "women_") + value;
    }

    /** 30109 方言补花动作音，原版资源为 MahAction/{Man|Women}/flower.mp3。 */
    public static String flowerVoiceResource(boolean male) {
        return "taizhou_mahjong_action_dialect_" + (male ? "man" : "women") + "_flower";
    }

    /** 30109 方言胡牌语音资源名，挂点 WinLost/Module.luac:263-281。 */
    public static String winVoiceResource(boolean male, WinKind winKind) {
        String key = winKind == WinKind.SELF_DRAWN ? "hu_1" : "hu_2";
        return "taizhou_mahjong_action_dialect_" + (male ? "man_" : "women_") + key;
    }
}
