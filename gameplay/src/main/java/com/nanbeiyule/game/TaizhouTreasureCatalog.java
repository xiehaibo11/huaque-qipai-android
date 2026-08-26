package com.nanbeiyule.game;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Exact client-side presentation metadata recovered from JuBaoPen/Config.lua. */
final class TaizhouTreasureCatalog {
    private static final int[] FORTUNE_BY_QUALITY = {60, 80, 168, 666};
    private static final String[] ANIMATION_BY_QUALITY = {"lv", "lan", "zi", "cheng"};
    private static final int[] COLOR_BY_QUALITY = {
        0xFF7CE29E, 0xFF69BAFF, 0xFFFF65F9, 0xFFFF9B34
    };
    private static final String[] QUALITY_NAMES = {"普通", "精品", "极品", "绝品"};
    private static final Set<Integer> RIGHT_TOOLTIP_ITEMS = Set.of(6, 2, 14, 10, 8, 4, 16, 12);

    private static final List<Item> ITEMS =
            List.of(
                    item(
                            1,
                            "手串",
                            "凝珠成串，清雅致韵",
                            "珍珠镶嵌金纹，缀绿玉珠，配黑珠白珠吊坠，精致典雅，似能汇聚财气、带来福气满满。"),
                    item(
                            2,
                            "宝瓶",
                            "金翠交织，玉质莹润",
                            "瓶身呈渐变翠绿，饰有金色卷草纹，瓶口、瓶底镶金，通透雅致，似能收纳财气，为周遭带来吉祥福气。"),
                    item(
                            3,
                            "金元宝",
                            "元宝生辉，财运亨通",
                            "纯金打造元宝，通体金黄，底部刻招财进宝，象征财富积累，助事业兴旺。"),
                    item(
                            4,
                            "玉佩",
                            "玉翠灵动，聚财纳福",
                            "翠绿玉饰，造型优美带涡旋纹，红绳流苏点缀，温润有光泽，仿若能聚财气、送福气。"),
                    item(
                            5,
                            "宝石戒指",
                            "红宝金戒，财福俱收",
                            "金色戒身镶嵌大红宝石，两侧缀蓝绿宝石，花纹精致，似能汇聚财气、带来吉祥好运。"),
                    item(
                            6,
                            "聚宝葫芦",
                            "聚宝葫芦，财福双收",
                            "葫芦谐音“福禄”，既聚财又招福，寓意财运亨通的同时，还有健康、平安等福气环绕，吉祥美满。"),
                    item(
                            7,
                            "金算盘",
                            "算珠灵动，财吉满盈",
                            "通体纯金算盘，算珠圆润饱满，框架雕祥云纹，精致贵气，似能算八方财，聚纳富贵吉祥。"),
                    item(
                            8,
                            "金猪拱财",
                            "金猪献瑞，拱财纳吉",
                            "通体金黄的小猪，体态圆润，衔着铜钱，神态活泼，似能引财上门、带来满满福气。"),
                    item(
                            9,
                            "铜钱串",
                            "铜钱成串，家缠万贯",
                            "用红绳串联的古铜钱，代表“万贯家财”寓意财富积累如同串子般越变越长，运气好到挡不住。"),
                    item(
                            10,
                            "阴阳宝镜",
                            "阴阳调和，财气满盈",
                            "蓝绿色象征生机与财富，中央的阴阳鱼清晰灵动，似在不断流转，寓意阴阳平衡，能调和气场汇聚财气。"),
                    item(
                            11,
                            "转运珠",
                            "金玉珠连，财吉双至",
                            "蓝黄珠粒相间，色泽鲜亮，搭配红流苏，灵动雅致，似能聚拢财气，带来吉祥好运。"),
                    item(
                            12,
                            "玉如意",
                            "如意如意，随我心意",
                            "翠绿玉石温润透亮，鎏金祥云纹饰环绕，贵气十足，能招引四方财气，带来顺遂吉祥。"),
                    item(
                            13,
                            "招财金猫",
                            "金猫捧宝，招财纳福",
                            "通体金黄的猫咪，捧金元宝，挂“发”字牌，神态喜庆，似能招揽财富、送来福气。"),
                    item(
                            14,
                            "金钱树",
                            "金叶满树，聚宝呈祥",
                            "满树金黄铜钱，枝干挺拔，底座翠绿带花纹，散落金币点缀，似能摇落财富、汇聚福气。"),
                    item(
                            15,
                            "聚宝盆",
                            "盆聚万宝，吉祥满溢",
                            "红鼎金纹，双耳衔链，满盛元宝铜钱，缀绿珠，富贵庄重，似能广纳财气，助财富充盈、福气降临。"),
                    item(
                            16,
                            "金蟾吐宝",
                            "金蟾吐宝，财源滚滚",
                            "金色蟾蜍，口含铜钱，身绕红绳铜钱饰，蹲坐铜钱堆上，模样富态，似能吸纳财气、带来吉祥。"));

    private static final Map<String, Item> BY_CODE = byCode();

    private TaizhouTreasureCatalog() {}

    static List<Item> items() {
        return ITEMS;
    }

    static Item itemForCode(String code) {
        return code == null ? null : BY_CODE.get(code);
    }

    static Item item(int index) {
        return index >= 1 && index <= ITEMS.size() ? ITEMS.get(index - 1) : null;
    }

    static String qualityName(int quality) {
        return quality >= 1 && quality <= QUALITY_NAMES.length
                ? QUALITY_NAMES[quality - 1]
                : "";
    }

    static boolean flipsTooltip(int index) {
        return RIGHT_TOOLTIP_ITEMS.contains(index);
    }

    private static Item item(int index, String name, String title, String description) {
        int quality = (index - 1) / 4 + 1;
        return new Item(
                index,
                String.format("TREASURE_%02d", index),
                name,
                quality,
                FORTUNE_BY_QUALITY[quality - 1],
                ANIMATION_BY_QUALITY[quality - 1],
                title,
                COLOR_BY_QUALITY[quality - 1],
                description);
    }

    private static Map<String, Item> byCode() {
        Map<String, Item> result = new LinkedHashMap<>();
        for (Item item : ITEMS) result.put(item.code(), item);
        return Map.copyOf(result);
    }

    record Item(
            int index,
            String code,
            String name,
            int quality,
            int fortunePerLevel,
            String animationName,
            String title,
            int titleColor,
            String description) {}
}
