package com.nanbeiyule.game;

import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.RectF;
import com.nanbeiyule.game.TaizhouSettingStyle.Choice;
import com.nanbeiyule.game.dragonbones.DragonBonesArmature;
import java.util.HashMap;
import java.util.Map;

/**
 * 动画页两组选项的 DragonBones 预览。
 *
 * <p>与 {@code View.lua:initAnimationDetailLayer / initInsertAnimationDetailLayer} 同一批
 * 骨骼：出牌轨迹用 {@code cpgj_ani_arc/line}，插牌用 {@code Ios_shezhi_charu/bucha}，
 * 都是 {@code playDargonBonesAnimByTimes(params, 0)} 无限循环。
 */
final class TaizhouSettingNewAnimations {
    private static final String DIRECTORY = "taizhou_setting_effects";
    private static final String[] MOVE_STYLES = {"cpgj_ani_arc", "cpgj_ani_line"};
    private static final String[] INSERT_STYLES = {"Ios_shezhi_charu", "Ios_shezhi_bucha"};

    private final AssetManager assets;
    private final Map<String, DragonBonesArmature> armatures = new HashMap<>();

    TaizhouSettingNewAnimations(AssetManager assets) {
        this.assets = assets;
    }

    static boolean handles(Choice choice) {
        return choice == Choice.OUT_MOVE_STYLE || choice == Choice.INSERT_STYLE;
    }

    void draw(Canvas canvas, Choice choice, int index, RectF destination, float seconds) {
        String name = (choice == Choice.OUT_MOVE_STYLE ? MOVE_STYLES : INSERT_STYLES)[index];
        DragonBonesArmature armature = armatures.get(name);
        if (armature == null) {
            armature = DragonBonesArmature.load(assets, DIRECTORY + "/" + name, name);
            armatures.put(name, armature);
        }
        armature.draw(canvas, destination, seconds);
    }
}
