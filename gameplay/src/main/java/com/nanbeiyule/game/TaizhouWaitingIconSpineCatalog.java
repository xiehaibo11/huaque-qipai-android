package com.nanbeiyule.game;

import android.content.res.AssetManager;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 等待桌三图标骨架的逐骨架装载目录。
 *
 * <p>原版三个图标是三个独立视图（{@code GamePropView}、{@code JuBaoPenIconView}、
 * {@code LuckyMission/IconView}），各自在自己的 {@code ctor} 里装载自己的骨架；因此本目录也按
 * 骨架逐个尝试，单个骨架被运行时拒绝或资源缺失只记录到降级清单，不中断其余骨架的装载。
 * 是否整体回退静态位图由 {@link TaizhouWaitingIconEffects#available()} 决定，保证现有合成
 * 管线的回退输出不变。
 */
final class TaizhouWaitingIconSpineCatalog {
    /** 骨架不可动画的降级原因，供降级清单与诊断使用。 */
    enum DegradeReason {
        /** 三件套任一缺失或位图解码失败。 */
        ASSET_MISSING,
        /** 骨架版本被 {@code Spine37JsonParser} 拒绝（非 3.3/3.7 系）。 */
        VERSION_UNSUPPORTED,
        /** 其余解析期错误。 */
        PARSE_ERROR
    }

    /** 单骨架装载来源；生产接 {@link AssetManager}，单测接真实文件解析。 */
    interface Loader {
        OriginalLobbyEffectAssets.Loaded load(String skeleton) throws IOException;
    }

    private final Map<String, OriginalLobbyEffectAssets.Loaded> loaded = new LinkedHashMap<>();
    private final Map<String, DegradeReason> degraded = new LinkedHashMap<>();

    static TaizhouWaitingIconSpineCatalog load(
            AssetManager assets, String assetRoot, List<String> skeletons) {
        return load(
                skeletons,
                skeleton ->
                        OriginalLobbyEffectAssets.load(assets, assetRoot + "/" + skeleton, skeleton));
    }

    static TaizhouWaitingIconSpineCatalog load(List<String> skeletons, Loader loader) {
        TaizhouWaitingIconSpineCatalog catalog = new TaizhouWaitingIconSpineCatalog();
        for (String skeleton : skeletons) {
            try {
                catalog.loaded.put(skeleton, loader.load(skeleton));
            } catch (IOException | RuntimeException exception) {
                catalog.degraded.put(skeleton, classify(exception));
            }
        }
        return catalog;
    }

    /** 降级原因分类：{@code Spine37JsonParser} 的版本拒绝报文以 {@code "Expected Spine"} 开头。 */
    static DegradeReason classify(Throwable exception) {
        if (exception instanceof IOException) {
            return DegradeReason.ASSET_MISSING;
        }
        if (String.valueOf(exception.getMessage()).startsWith("Expected Spine")) {
            return DegradeReason.VERSION_UNSUPPORTED;
        }
        return DegradeReason.PARSE_ERROR;
    }

    boolean available(String skeleton) {
        return loaded.containsKey(skeleton);
    }

    boolean allAvailable(List<String> skeletons) {
        for (String skeleton : skeletons) {
            if (!loaded.containsKey(skeleton)) {
                return false;
            }
        }
        return true;
    }

    OriginalLobbyEffectAssets.Loaded loaded(String skeleton) {
        return loaded.get(skeleton);
    }

    DegradeReason degradeReason(String skeleton) {
        return degraded.get(skeleton);
    }

    /** 降级清单：按声明顺序记录不可动画的骨架名。 */
    List<String> degradedSkeletons() {
        return List.copyOf(degraded.keySet());
    }

    void clear() {
        loaded.clear();
        degraded.clear();
    }
}
