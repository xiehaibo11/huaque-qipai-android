package com.nanbeiyule.game;

import java.util.HashMap;
import java.util.Map;

/**
 * 同名音效 300ms 防抖：同一资源名在窗口期内的重复触发只播第一次。
 *
 * <p>原版 SoundManager 没有任何防抖规则（SoundManager.luac 全文无时间窗逻辑），
 * 该窗口是南北娱乐推断值：防止服务端事件去重前的瞬间重复（如出牌确认与动画回调
 * 同帧到达）造成同一音效叠音。纯 JVM 实现，便于契约测试。
 */
final class TaizhouMahjongSoundDebounce {
    static final long WINDOW_MS = 300;

    private final Map<String, Long> lastPlayMsByName = new HashMap<>();

    boolean shouldPlay(String resourceName, long nowMs) {
        Long lastPlayMs = lastPlayMsByName.get(resourceName);
        if (lastPlayMs != null && nowMs - lastPlayMs < WINDOW_MS) {
            return false;
        }
        lastPlayMsByName.put(resourceName, nowMs);
        return true;
    }

    void reset() {
        lastPlayMsByName.clear();
    }
}
