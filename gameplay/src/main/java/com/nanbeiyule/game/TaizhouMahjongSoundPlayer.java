package com.nanbeiyule.game;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.SystemClock;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 台州麻将 30109 牌局音效播放器：按资源名播放 {@link TaizhouRoundAudioDirector}
 * 输出的报牌语音与操作音。
 *
 * <p>短音效走 SoundPool，与大厅 {@link OriginalLobbyAudioController} 的短音效先例一致
 * （聚宝盆 MediaPlayer 只服务单个长抽奖音，不适合 91 个秒级短音）。资源按需懒加载，
 * 加载完成后补播；同名音效 300ms 内不叠加（{@link TaizhouMahjongSoundDebounce}）。
 *
 * <p>所有失败静默降级：资源缺失（getIdentifier 返回 0）、加载失败、已 release 均不抛异常。
 * 主线程无文件 IO——SoundPool 的解码在其内部工作线程完成。Activity/牌桌销毁必须调
 * {@link #release()}（批 2 装配挂钩点）。
 */
final class TaizhouMahjongSoundPlayer {
    interface PreloadListener {
        void onPreloadProgress(int loaded, int total);
    }

    private final Context applicationContext;
    private final SoundPool soundPool;
    private final TaizhouMahjongSoundDebounce debounce = new TaizhouMahjongSoundDebounce();
    private final Map<String, Integer> resourceIds = new HashMap<>();
    private final Map<String, Integer> soundIdsByName = new HashMap<>();
    private final Map<Integer, String> namesBySoundId = new HashMap<>();
    private final Map<String, Boolean> loadReady = new HashMap<>();
    private final Map<String, Boolean> pendingPlay = new HashMap<>();
    private final Set<String> preloadPending = new LinkedHashSet<>();
    private float soundVolume = 1.0f;
    private float voiceVolume = 0.5f;
    private PreloadListener preloadListener;
    private int preloadTotal;
    private int preloadFinished;
    private boolean released;

    TaizhouMahjongSoundPlayer(Context context) {
        applicationContext = context.getApplicationContext();
        soundPool =
                new SoundPool.Builder()
                        .setMaxStreams(8)
                        .setAudioAttributes(
                                new AudioAttributes.Builder()
                                        .setUsage(AudioAttributes.USAGE_GAME)
                                        .setContentType(
                                                AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                        .build())
                        .build();
        soundPool.setOnLoadCompleteListener(this::onLoadComplete);
    }

    void preload(Collection<String> resourceNames, PreloadListener listener) {
        preloadPending.clear();
        preloadListener = listener;
        preloadFinished = 0;
        Set<String> names = new LinkedHashSet<>();
        if (resourceNames != null) {
            for (String resourceName : resourceNames) {
                if (resourceName != null && !resourceName.isBlank()) {
                    names.add(resourceName);
                }
            }
        }
        preloadTotal = names.size();
        if (released || preloadTotal == 0) {
            notifyPreload();
            return;
        }
        for (String resourceName : names) {
            int resourceId = resourceId(resourceName);
            if (resourceId == 0 || Boolean.TRUE.equals(loadReady.get(resourceName))) {
                preloadFinished++;
                continue;
            }
            preloadPending.add(resourceName);
            if (!soundIdsByName.containsKey(resourceName)) {
                int soundId = soundPool.load(applicationContext, resourceId, 1);
                if (soundId == 0) {
                    preloadPending.remove(resourceName);
                    preloadFinished++;
                    continue;
                }
                soundIdsByName.put(resourceName, soundId);
                namesBySoundId.put(soundId, resourceName);
            }
        }
        notifyPreload();
    }

    /** 播放 director 输出的整份清单；空清单直接返回。 */
    void playAll(List<String> resourceNames) {
        if (resourceNames == null) {
            return;
        }
        for (String resourceName : resourceNames) {
            play(resourceName);
        }
    }

    /** 播放单个资源；缺失、加载失败或 300ms 防抖窗口内静默跳过。 */
    void play(String resourceName) {
        if (released || resourceName == null || volumeFor(resourceName) <= 0f) {
            return;
        }
        if (!debounce.shouldPlay(resourceName, SystemClock.elapsedRealtime())) {
            return;
        }
        int resourceId = resourceId(resourceName);
        if (resourceId == 0) {
            return;
        }
        if (Boolean.TRUE.equals(loadReady.get(resourceName))) {
            Integer soundId = soundIdsByName.get(resourceName);
            if (soundId != null) {
                float volume = volumeFor(resourceName);
                soundPool.play(soundId, volume, volume, 1, 0, 1.0f);
            }
            return;
        }
        pendingPlay.put(resourceName, Boolean.TRUE);
        if (!soundIdsByName.containsKey(resourceName)) {
            int soundId = soundPool.load(applicationContext, resourceId, 1);
            soundIdsByName.put(resourceName, soundId);
            namesBySoundId.put(soundId, resourceName);
        }
    }

    void applySettings(PersonalCenterSystemSettings settings) {
        if (settings == null) return;
        soundVolume = settings.soundEnabled() ? settings.soundVolume() / 100f : 0f;
        voiceVolume = settings.voiceEnabled() ? settings.voiceVolume() / 100f : 0f;
    }

    boolean hasRawResource(String resourceName) {
        return resourceName != null && resourceId(resourceName) != 0;
    }

    /** 停止全部在播音效并清空防抖窗口；开局/离桌切换时由装配方调用。 */
    void stopAll() {
        if (released) {
            return;
        }
        soundPool.autoPause();
        pendingPlay.clear();
        debounce.reset();
    }

    /** Activity 或牌桌销毁时调用，释放 SoundPool；之后所有播放调用静默失效。 */
    void release() {
        if (released) {
            return;
        }
        released = true;
        pendingPlay.clear();
        preloadPending.clear();
        soundPool.release();
    }

    private int resourceId(String resourceName) {
        Integer cached = resourceIds.get(resourceName);
        if (cached != null) {
            return cached;
        }
        Resources resources = applicationContext.getResources();
        int id = resources.getIdentifier(resourceName, "raw", applicationContext.getPackageName());
        resourceIds.put(resourceName, id);
        return id;
    }

    private void onLoadComplete(SoundPool pool, int sampleId, int status) {
        String name = namesBySoundId.get(sampleId);
        if (name == null) {
            return;
        }
        if (status != 0) {
            // 加载失败：移除记录允许下次重试，保持静默。
            soundIdsByName.remove(name);
            namesBySoundId.remove(sampleId);
            pendingPlay.remove(name);
            onPreloadLoaded(name);
            return;
        }
        loadReady.put(name, Boolean.TRUE);
        onPreloadLoaded(name);
        if (pendingPlay.remove(name) != null && !released) {
            float volume = volumeFor(name);
            if (volume > 0f) {
                pool.play(sampleId, volume, volume, 1, 0, 1.0f);
            }
        }
    }

    private float volumeFor(String resourceName) {
        return resourceName.startsWith("taizhou_mahjong_voice_")
                        || resourceName.startsWith("taizhou_mahjong_action_")
                ? voiceVolume
                : soundVolume;
    }

    private void onPreloadLoaded(String resourceName) {
        if (preloadPending.remove(resourceName)) {
            preloadFinished++;
            notifyPreload();
        }
    }

    private void notifyPreload() {
        PreloadListener listener = preloadListener;
        if (listener != null) {
            listener.onPreloadProgress(preloadFinished, preloadTotal);
        }
        if (preloadTotal == 0 || preloadFinished >= preloadTotal) {
            preloadPending.clear();
            preloadListener = null;
        }
    }
}
