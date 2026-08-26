package com.nanbeiyule.game;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import java.io.IOException;

/**
 * 播放俏皮话配音，对应原版 {@code SoundManager:playSoundWisecrack} 末尾的 {@code playEffect(path)}。
 *
 * <p>路径解析见 {@link TaizhouQuickPhraseVoicePath}。原版用
 * {@code cc.FileUtils:isFileExist} 在方言与标准话之间回退，这里等价地按候选顺序尝试打开 asset。
 *
 * <p><b>资源边界</b>：30109 的 {@code M_Speak1..9.mp3} / {@code W_Speak1..9.mp3} 不在仓库归档内
 * （{@code res/audio/Speak/} 下只有 30308 一个玩法），原版是按需从热更 CDN 下载的。因此本类当前
 * 在真机上总是走「两个候选都不存在」的分支并静默返回，不会发声，也不得用任何替代音频冒充原版
 * 配音。链路本身是完整的：一旦官方音频包落到 {@code assets/audio/Speak/30109/} 即可发声。
 */
final class TaizhouQuickPhraseVoicePlayer {
    /** 台州麻将牌局 ID。 */
    static final int TAIZHOU_GAME_ID = 30109;

    private final AssetManager assets;
    private MediaPlayer player;

    TaizhouQuickPhraseVoicePlayer(AssetManager assets) {
        this.assets = assets;
    }

    /**
     * 播放某条俏皮话的配音。
     *
     * @param contentIndex 服务端目录下标，0 起；原版配置 index 从 1 起，这里加一还原
     * @param male 说话人是否男性
     * @param dialect 设置页「方言」开关
     * @return 找到并开始播放返回 true；音频缺失或音效关闭返回 false
     */
    boolean play(int contentIndex, boolean male, boolean dialect, float volume) {
        if (volume <= 0f || contentIndex < 0) {
            return false;
        }
        String[] candidates =
                TaizhouQuickPhraseVoicePath.candidates(
                        TAIZHOU_GAME_ID,
                        contentIndex + 1,
                        male,
                        dialect,
                        TaizhouQuickPhraseVoicePath.DIALECT_TYPE_DEFAULT);
        for (String candidate : candidates) {
            if (start(candidate, volume)) {
                return true;
            }
        }
        return false;
    }

    private boolean start(String assetPath, float volume) {
        try (AssetFileDescriptor descriptor = assets.openFd(assetPath)) {
            release();
            MediaPlayer next = new MediaPlayer();
            next.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build());
            next.setDataSource(
                    descriptor.getFileDescriptor(),
                    descriptor.getStartOffset(),
                    descriptor.getLength());
            float clampedVolume = Math.max(0f, Math.min(1f, volume));
            next.setVolume(clampedVolume, clampedVolume);
            next.setOnCompletionListener(finished -> release());
            next.prepare();
            next.start();
            player = next;
            return true;
        } catch (IOException | IllegalStateException | IllegalArgumentException exception) {
            // 候选不存在或无法解码：与原版 isFileExist 判否等价，继续尝试下一个候选。
            return false;
        }
    }

    /** Activity 或牌桌销毁时必须调用，避免 MediaPlayer 泄漏。 */
    void release() {
        MediaPlayer current = player;
        player = null;
        if (current != null) {
            try {
                current.stop();
            } catch (IllegalStateException ignored) {
                // 已经停止，忽略。
            }
            current.release();
        }
    }
}
