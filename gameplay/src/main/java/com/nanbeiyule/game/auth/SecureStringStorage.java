package com.nanbeiyule.game.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Shared encrypted storage used by both the launcher and gameplay authentication stacks. */
public final class SecureStringStorage {
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "huaque.lua.platform.storage";
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final String PREFERENCES = "lua_platform_secure";

    private final SharedPreferences preferences;

    public SecureStringStorage(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public String get(String key) {
        String encoded = preferences.getString(key, null);
        if (encoded == null) {
            return null;
        }
        try {
            String[] parts = encoded.split("\\.", 2);
            if (parts.length != 2) {
                throw new GeneralSecurityException("invalid encrypted value");
            }
            byte[] iv = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] ciphertext = Base64.decode(parts[1], Base64.NO_WRAP);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    getOrCreateKey(),
                    new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException error) {
            preferences.edit().remove(key).commit();
            return null;
        }
    }

    public void set(String key, String value) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
            byte[] ciphertext =
                    cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            String encoded =
                    Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP)
                            + "."
                            + Base64.encodeToString(ciphertext, Base64.NO_WRAP);
            if (!preferences.edit().putString(key, encoded).commit()) {
                throw new IllegalStateException("secure platform storage write failed");
            }
        } catch (GeneralSecurityException error) {
            throw new IllegalStateException(
                    "secure platform storage is unavailable", error);
        }
    }

    public void remove(String key) {
        if (!preferences.edit().remove(key).commit()) {
            throw new IllegalStateException("secure platform storage removal failed");
        }
    }

    private static SecretKey getOrCreateKey() throws GeneralSecurityException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        try {
            keyStore.load(null);
        } catch (java.io.IOException error) {
            throw new GeneralSecurityException("cannot load Android KeyStore", error);
        }
        java.security.Key existing = keyStore.getKey(KEY_ALIAS, null);
        if (existing instanceof SecretKey secretKey) {
            return secretKey;
        }

        KeyGenerator generator =
                KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
        generator.init(
                new KeyGenParameterSpec.Builder(
                                KEY_ALIAS,
                                KeyProperties.PURPOSE_ENCRYPT
                                        | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(true)
                        .build());
        return generator.generateKey();
    }
}
