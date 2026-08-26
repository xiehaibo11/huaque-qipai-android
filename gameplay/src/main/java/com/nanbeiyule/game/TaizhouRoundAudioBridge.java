package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplayEvent;
import com.nanbeiyule.game.mahjong.round.MahjongCombType;
import java.util.List;
import org.json.JSONObject;

/**
 * 台州麻将 30109 牌局音频装配桥：把 {@link GameplaySessionCoordinator} 恢复出的
 * 牌局事件翻译成 {@link TaizhouRoundAudioDirector} 的声音清单并交给声音出口播放。
 * 纯逻辑、无 Android 依赖，单测用假 {@link SoundSink} 锁定映射。
 *
 * <p>事件→声音映射与原版挂点：
 * <ul>
 *   <li>DEALT → playSoundStart（开局，GameLayer/Module.luac:230）；
 *   <li>DISCARDED → 报牌语音 + Out.mp3（GameLayer/Module.luac:743 与
 *       UIMahLayer.luac:272/310；事件流无飞牌动画落点回调，一次性播放为标注推断）；
 *   <li>MELD_APPLIED → playSoundMahAction 吃碰杠语音（GameLayer/Module.luac:794-811）；
 *   <li>WIN_DECLARED → hu_1 自摸 / hu_2 点炮（WinLost/Module.luac:263-281），
 *       QA 事件 winType 取值 ZIMO/DIANPAO（南北自建 QA 协议）；
 *   <li>FLOWER_REPLACED → MahLayer/30109/MahAction/{Man|Women}/flower.mp3；
 *   <li>ROUND_RESULT_READY → playSoundEnd 小结束（TaiZhou WinLost/Module.luac:8）。
 * </ul>
 *
 * <p>QA 事件流没有骰子与倒计时事件，{@code onDiceRolled}/{@code onClockWarningTick}
 * 保持未接线；补花接入原版 30109 方言动作音。
 * 出牌/发牌的公共与座位双发事件会各到一次，同名 300ms 防抖由
 * {@link TaizhouMahjongSoundPlayer} 负责，本桥不去重。牌面与动作配音按大厅设置中的
 * 男声/女声选项选择；快进房间标记 QA 协议不下发，保持默认关闭。
 * 单个事件解析失败只跳过该事件，不得影响牌局主流程。
 */
final class TaizhouRoundAudioBridge {
    /** 声音出口：生产实现是 {@link TaizhouMahjongSoundPlayer}，测试用假实现。 */
    interface SoundSink {
        void playAll(List<String> resourceNames);
    }

    private final TaizhouRoundAudioDirector director;
    private final SoundSink sink;
    private final TaizhouRoundAudioDirector.SeatGender voiceGender;

    TaizhouRoundAudioBridge(SoundSink sink) {
        this(new TaizhouRoundAudioDirector(), sink,
                TaizhouRoundAudioDirector.SeatGender.UNKNOWN);
    }

    TaizhouRoundAudioBridge(SoundSink sink, boolean maleVoice) {
        this(new TaizhouRoundAudioDirector(), sink,
                maleVoice
                        ? TaizhouRoundAudioDirector.SeatGender.MALE
                        : TaizhouRoundAudioDirector.SeatGender.FEMALE);
    }

    TaizhouRoundAudioBridge(TaizhouRoundAudioDirector director, SoundSink sink) {
        this(director, sink, TaizhouRoundAudioDirector.SeatGender.UNKNOWN);
    }

    private TaizhouRoundAudioBridge(
            TaizhouRoundAudioDirector director,
            SoundSink sink,
            TaizhouRoundAudioDirector.SeatGender voiceGender) {
        this.director = director;
        this.sink = sink;
        this.voiceGender = voiceGender;
    }

    /** 处理一批已接受的事件；任何单事件异常只跳过自身。 */
    void onEvents(List<GameplayEvent> events) {
        if (events == null) {
            return;
        }
        for (GameplayEvent event : events) {
            try {
                onEvent(event);
            } catch (RuntimeException exception) {
                // 音频永远不得阻断牌局事件流。
            }
        }
    }

    private void onEvent(GameplayEvent event) {
        switch (event.type()) {
            case "DEALT" -> sink.playAll(director.onRoundStarted());
            case "DISCARDED" -> {
                JSONObject lastDiscard = event.payload().optJSONObject("lastDiscard");
                if (lastDiscard == null || !lastDiscard.has("tile")) {
                    return;
                }
                sink.playAll(director.onTileDiscarded(lastDiscard.optInt("tile"), voiceGender));
            }
            case "MELD_APPLIED" -> {
                MahjongCombType combType =
                        MahjongCombType.valueOf(event.payload().optString("combType", ""));
                sink.playAll(director.onMeldApplied(combType, voiceGender));
            }
            case "WIN_DECLARED" -> {
                String winType = event.payload().optString("winType", "");
                TaizhouMahjongVoiceCatalog.WinKind winKind =
                        switch (winType) {
                            case "ZIMO" -> TaizhouMahjongVoiceCatalog.WinKind.SELF_DRAWN;
                            case "DIANPAO" -> TaizhouMahjongVoiceCatalog.WinKind.DISCARD;
                            default -> null;
                        };
                if (winKind == null) {
                    return;
                }
                sink.playAll(director.onWinDeclared(winKind, voiceGender));
            }
            case "FLOWER_REPLACED" -> sink.playAll(director.onFlowerReplaced(voiceGender));
            case "SHENG_PAI_COUNT" -> {
                if (event.payload().optBoolean("bFirst", false)) {
                    sink.playAll(director.onShengPaiStarted());
                }
            }
            case "ROUND_RESULT_READY" -> sink.playAll(director.onRoundEnded());
            default -> {
                // 其余事件无声音映射。
            }
        }
    }
}
