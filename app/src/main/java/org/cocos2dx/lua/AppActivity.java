package org.cocos2dx.lua;

import android.os.Bundle;

import com.huaque.ui.MainActivity;

import java.io.File;
import java.io.FileInputStream;

public class AppActivity extends MainActivity {
    public static AppActivity mactivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mactivity = this;
        super.onCreate(savedInstanceState);
    }

    public static void hideSplash() {
        // Kept for compatibility with the original Lua bridge:
        // luaj.callStaticMethod("org/cocos2dx/lua/AppActivity", "hideSplash", {}, "()V")
    }

    public static String readJsonFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        try {
            FileInputStream stream = new FileInputStream(file);
            byte[] data = new byte[stream.available()];
            int read = stream.read(data);
            stream.close();
            if (read <= 0) {
                return "";
            }
            return new String(data, "UTF-8");
        } catch (Exception ignored) {
            return null;
        }
    }
}
