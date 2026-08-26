package com.nanbeiyule.game.goldroom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 选场页右上活动入口组 {@code _menuBarTopAct} 的一次布局。
 *
 * <p>原版这一组不是定长的：{@code GoldNew/Manager} 按服务端下发的按钮列表用
 * {@code Views/BtnFactory.lua} 造按钮，每颗再跑 {@code checkShow()} 自我隐藏，
 * {@code LocalConfig.MENU_BAR_CFG} 的 {@code dtSize.x = -150} 让它从右边 1806 往左依次排开。
 * 因此组内位置只有在知道当前可见入口数量时才成立，本类负责这一层。
 *
 * <p>证据见 {@code android/docs/ORIGINAL-GOLD-HALL-ACT-ENTRIES-EVIDENCE.md}。
 */
public final class GoldHallActEntryGroup {
    private final List<GoldHallActEntry> visible;

    private GoldHallActEntryGroup(List<GoldHallActEntry> visible) {
        this.visible = Collections.unmodifiableList(visible);
    }

    /**
     * 没有任何真实活动状态时的组，即限时礼包、福利任务、财神月卡三颗，
     * 中心 {@code 1426/1576/1726}，与用户提供的 1.5.4 实机截图一致。
     */
    public static GoldHallActEntryGroup residentOnly() {
        return of(Set.of());
    }

    /**
     * 按真实活动状态组装。{@code activeServerActs} 里的开关型入口才进组，对应原版
     * 各按钮 {@code checkShow()} 里的 {@code isValid()}（例如
     * {@code TimeLoginActBtn.lua:38} → {@code TimeLoginActModule:isValid()} 即
     * {@code self._aid ~= 0}）。非开关型入口不看这个集合。
     */
    public static GoldHallActEntryGroup of(Set<GoldHallActEntry> activeServerActs) {
        List<GoldHallActEntry> entries = new ArrayList<>();
        for (GoldHallActEntry entry : GoldHallActEntry.values()) {
            if (!entry.serverGated() || activeServerActs.contains(entry)) {
                entries.add(entry);
            }
        }
        return new GoldHallActEntryGroup(entries);
    }

    /** 当前可见入口，按从左到右的绘制顺序。 */
    public List<GoldHallActEntry> visible() {
        return visible;
    }

    public boolean contains(GoldHallActEntry entry) {
        return visible.contains(entry);
    }

    /** 该入口在组内的中心 X；组右对齐，索引从左边数起。 */
    public float centerX(GoldHallActEntry entry) {
        int index = visible.indexOf(entry);
        if (index < 0) {
            return Float.NaN;
        }
        return GoldHallChromeLayout.activityButtonCenterX(index, visible.size());
    }

    /** 命中测试用 {@code _panel} 的 160x160 触摸盒。 */
    public boolean hits(GoldHallActEntry entry, float designX, float designY) {
        float centerX = centerX(entry);
        if (Float.isNaN(centerX)) {
            return false;
        }
        float half = GoldHallChromeLayout.ACT_BUTTON_SIZE / 2.0f;
        float centerY = GoldHallChromeLayout.ACT_BUTTON_CENTER_Y;
        return designX >= centerX - half
                && designX <= centerX + half
                && designY >= centerY - half
                && designY <= centerY + half;
    }

    /**
     * 命中的入口，没有命中返回 null。
     *
     * <p>按钮宽 160 而步距只有 150，相邻两个的触摸盒天然重叠 10；重叠带按离中心最近的入口判定，
     * 避免依赖 Cocos 的子节点绘制顺序去猜哪一个在上层。
     */
    public GoldHallActEntry at(float designX, float designY) {
        GoldHallActEntry nearest = null;
        float nearestDistance = Float.MAX_VALUE;
        for (GoldHallActEntry entry : visible) {
            if (!hits(entry, designX, designY)) {
                continue;
            }
            float distance = Math.abs(designX - centerX(entry));
            if (distance < nearestDistance) {
                nearest = entry;
                nearestDistance = distance;
            }
        }
        return nearest;
    }
}
