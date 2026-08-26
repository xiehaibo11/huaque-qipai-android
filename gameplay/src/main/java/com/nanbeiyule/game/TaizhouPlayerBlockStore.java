package com.nanbeiyule.game;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * 「屏蔽TA」的本地开关，还原 {@code GameBase/Modules/PlayerInfo/Module.luac:251-279}。
 *
 * <p>原版用 {@code cc.UserDefault} 存三个布尔键：{@code blockedVoice_%d_%d}、
 * {@code blockedPhrase_%d_%d}、{@code blockedEmojis_%d_%d}，两个 {@code %d} 依次是
 * 自己的 numberID 与对方的 numberID；读取时再与会员是否过期取与（:255-257）——
 * 会员过期则一律不生效。这里保持同一套键名与同一条与会员状态的与运算。
 */
final class TaizhouPlayerBlockStore {
    /** {@code BlockType}。 */
    enum Type {
        VOICE("blockedVoice_%d_%d"),
        CHAT("blockedPhrase_%d_%d"),
        EMOJIS("blockedEmojis_%d_%d");

        private final String keyFormat;

        Type(String keyFormat) {
            this.keyFormat = keyFormat;
        }

        String key(long selfPlayerId, long targetPlayerId) {
            return String.format(Locale.ROOT, keyFormat, selfPlayerId, targetPlayerId);
        }
    }

    private static final String PREFERENCES = "taizhou_player_block";

    private final Supplier<SharedPreferences> preferencesSource;
    private SharedPreferences preferences;

    TaizhouPlayerBlockStore(Context context) {
        this(() -> context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE));
    }

    TaizhouPlayerBlockStore(Supplier<SharedPreferences> preferencesSource) {
        this.preferencesSource = preferencesSource;
    }

    /** {@code getBlockedVoice/Chat/Emojis}：存储值与「会员未过期」的与运算。 */
    boolean isBlocked(Type type, long selfPlayerId, long targetPlayerId, boolean membershipActive) {
        return membershipActive && stored(type, selfPlayerId, targetPlayerId);
    }

    /** 复选框回显用的原始存储值，不带会员判定。 */
    boolean stored(Type type, long selfPlayerId, long targetPlayerId) {
        return preferences().getBoolean(type.key(selfPlayerId, targetPlayerId), false);
    }

    /** {@code setBlocked}(:267-271)。 */
    void setBlocked(Type type, long selfPlayerId, long targetPlayerId, boolean blocked) {
        preferences()
                .edit()
                .putBoolean(type.key(selfPlayerId, targetPlayerId), blocked)
                .apply();
    }

    private SharedPreferences preferences() {
        if (preferences == null) {
            preferences = preferencesSource.get();
        }
        return preferences;
    }
}
