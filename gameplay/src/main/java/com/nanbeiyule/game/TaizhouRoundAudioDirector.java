package com.nanbeiyule.game;

import com.nanbeiyule.game.TaizhouMahjongVoiceCatalog.TableSound;
import com.nanbeiyule.game.TaizhouMahjongVoiceCatalog.WinKind;
import com.nanbeiyule.game.mahjong.round.MahjongCombType;
import java.util.ArrayList;
import java.util.List;

/**
 * 台州麻将 30109 回合音频指挥：把牌局事件翻译成应播的 {@code res/raw} 资源名清单。
 * 纯状态机、无 Android 依赖，播放由 {@link TaizhouMahjongSoundPlayer} 执行。
 *
 * <p>男女声选择按原版：{@code bMan = playerData:getSex() == 1}，playerData 缺失时
 * 恒为 false 即女声目录（GameLayer/Module.luac:633-637、737-741、WinLost/Module.luac:272-276）。
 * 当前大厅 API 无玩家性别字段，装配方暂传 {@link SeatGender#UNKNOWN}（=女声）。
 *
 * <p>快进房间（{@code roomData:getIsFastPlay()}）时原版报牌、动作语音、骰子、出牌、
 * 时钟音全部早退静默（SoundManager.luac:148-150、179-181、306-308、314-316、322-324），
 * 而 playSoundStart/playSoundEnd/playButtonClick 无该检查；{@link #setFastPlay} 复刻该矩阵。
 *
 * <p>牌音和动作音固定使用 MuMu 原版应用导出的 30109 方言目录
 * （MahLayer/30109/Mah 与 MahAction）；俏皮话 Speak/30109 是另一条语音链路，本类不涉及。
 */
public final class TaizhouRoundAudioDirector {
    /** 座位性别；服务端暂无性别字段时的取值见 {@link #UNKNOWN}。 */
    public enum SeatGender {
        MALE,
        FEMALE,
        /** 原版 playerData 缺失等价分支：bMan=false，使用女声目录。 */
        UNKNOWN;

        boolean maleVoice() {
            return this == MALE;
        }
    }

    private boolean fastPlay;

    /** 快进房间开关，对应原版 {@code roomData:getIsFastPlay()} 的静默矩阵。 */
    public void setFastPlay(boolean fastPlay) {
        this.fastPlay = fastPlay;
    }

    /** 开局：playSoundStart，挂点 GameLayer/Module.luac:230。 */
    public List<String> onRoundStarted() {
        return List.of(TableSound.DEAL_START.resourceName());
    }

    /** 骰子动画：playSoundChip，挂点 AnimationLayer.luac:137/177/183。 */
    public List<String> onDiceRolled() {
        return fastPlay ? List.of() : List.of(TableSound.DICE_ROLL.resourceName());
    }

    /** 小结束：playSoundEnd，挂点 TaiZhou WinLost/Module.luac:8。 */
    public List<String> onRoundEnded() {
        return List.of(TableSound.ROUND_END.resourceName());
    }

    /** 牌局按钮：playButtonClick，挂点 GoldView.luac:458。 */
    public List<String> onTableButtonClick() {
        return List.of(TableSound.BUTTON_CLICK.resourceName());
    }

    /** 倒计时 ≤2 秒每秒：playSoundClock，挂点 TableClockView.luac:259/354。 */
    public List<String> onClockWarningTick() {
        return fastPlay ? List.of() : List.of(TableSound.CLOCK_TICK.resourceName());
    }

    /** 服务端 {@code bFirst=true} 时的生牌入场音，快进房与原版动画一样静默。 */
    public List<String> onShengPaiStarted() {
        return fastPlay ? List.of() : List.of(TableSound.SHENG_PAI.resourceName());
    }

    /**
     * 出牌：先报牌语音（挂点 GameLayer/Module.luac:743，msg 处理即播），
     * 再出牌嗖声（挂点 UIMahLayer.luac:272/310，原版在飞牌动画落点；
     * 当前事件流没有动画落点回调，装配方在出牌事件一次播放——标注为推断时序）。
     */
    public List<String> onTileDiscarded(int tileValue, SeatGender seatGender) {
        if (fastPlay) {
            return List.of();
        }
        List<String> sounds = new ArrayList<>(2);
        String voice =
                TaizhouMahjongVoiceCatalog.discardVoiceResource(
                        seatGender.maleVoice(), tileValue);
        if (voice != null) {
            sounds.add(voice);
        }
        sounds.add(TableSound.DISCARD_WHOOSH.resourceName());
        return sounds;
    }

    /**
     * 吃碰杠副露落地：playSoundMahAction(convertFlag, …)，挂点 GameLayer/Module.luac:794-811；
     * convertFlag 即 {@code COMB_FLAG_TO_TYPE} 映射后的 COMB_TYPE（GameDefine.luac:159-168）。
     */
    public List<String> onMeldApplied(MahjongCombType combType, SeatGender seatGender) {
        if (fastPlay) {
            return List.of();
        }
        String voice =
                TaizhouMahjongVoiceCatalog.meldVoiceResource(seatGender.maleVoice(), combType);
        return voice == null ? List.of() : List.of(voice);
    }

    /** 胡牌：hu_1 自摸 / hu_2 点炮抢杠，挂点 WinLost/Module.luac:263-281。 */
    public List<String> onWinDeclared(WinKind winKind, SeatGender seatGender) {
        if (fastPlay) {
            return List.of();
        }
        return List.of(
                TaizhouMahjongVoiceCatalog.winVoiceResource(seatGender.maleVoice(), winKind));
    }

    /**
     * 补花：原版 actionIndex="flower"（GameLayer/Module.luac:631-647），
     * 原版 30109 的 MahLayer/MahAction 下有 flower.mp3，跟随动作语音目录播放。
     */
    public List<String> onFlowerReplaced(SeatGender seatGender) {
        if (fastPlay) {
            return List.of();
        }
        return List.of(
                TaizhouMahjongVoiceCatalog.flowerVoiceResource(seatGender.maleVoice()));
    }
}
