package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

final class ShopDrawableSet {
    private static final String TAG = "ShopDrawableSet";
    private static final Object CACHE_LOCK = new Object();
    private static volatile ShopDrawableSet cached;
    private static boolean preloadStarted;

    private final Resources resources;
    final Bitmap sceneBackground;
    final Bitmap topBackground;
    final Bitmap leftPanel;
    final Bitmap leftScenery;
    final Bitmap selectedCategory;
    final Bitmap categoryDivider;
    final Bitmap contentTile;
    final Bitmap titleBackground;
    final Bitmap headerDecoration;
    final Bitmap backPlate;
    final Bitmap backArrow;
    final Bitmap headerTitle;
    final Bitmap headerPanelBackground;
    final Bitmap walletBackground;
    final Bitmap headerCouponIcon;
    final Bitmap headerRoomCardIcon;
    final Bitmap headerCoinIcon;
    final Bitmap headerDiamondIcon;
    final Bitmap headerAddIcon;
    final Bitmap bag;
    final Bitmap customerService;
    final Bitmap productCard;
    final Bitmap productIconGlow;
    final Bitmap buyButton;
    final Bitmap diamond;
    final Bitmap diamondChest;
    final Bitmap coin;
    final Bitmap membershipNoticeHelp;
    final Bitmap membershipNoticeLink;

    private final Map<ShopCategory, Bitmap> categoryLabels = new EnumMap<>(ShopCategory.class);
    private final Map<String, Bitmap> productIcons = new HashMap<>();

    ShopDrawableSet(Resources resources) {
        this.resources = resources;
        sceneBackground = load(resources, R.drawable.shop_new_gold_hall_bg);
        topBackground = load(resources, R.drawable.shop_original_bgdi);
        leftPanel = load(resources, R.drawable.shop_original_leftdi);
        leftScenery = load(resources, R.drawable.shop_original_leftbgdi);
        selectedCategory = load(resources, R.drawable.shop_original_chose);
        categoryDivider = load(resources, R.drawable.shop_original_cut);
        contentTile = load(resources, R.drawable.shop_original_itemsbg);
        titleBackground = load(resources, R.drawable.shop_original_titlebg);
        headerDecoration = load(resources, R.drawable.shop_original_rightupdi);
        backPlate = load(resources, R.drawable.shop_original_backdi);
        backArrow = load(resources, R.drawable.shop_header_back);
        headerTitle = load(resources, R.drawable.shop_header_title);
        headerPanelBackground = load(resources, R.drawable.shop_header_panel_bg);
        walletBackground = load(resources, R.drawable.shop_original_gold);
        headerCouponIcon = load(resources, R.drawable.shop_header_coupon);
        headerRoomCardIcon = load(resources, R.drawable.shop_header_room_card);
        headerCoinIcon = load(resources, R.drawable.shop_header_coin);
        headerDiamondIcon = load(resources, R.drawable.shop_header_diamond);
        headerAddIcon = load(resources, R.drawable.shop_header_add);
        bag = load(resources, R.drawable.shop_header_bag);
        customerService = load(resources, R.drawable.shop_header_customer_service);
        productCard = load(resources, R.drawable.shop_original_itemsbg);
        productIconGlow = load(resources, R.drawable.shop_original_icon_bg);
        buyButton = load(resources, R.drawable.shop_original_buydi);
        diamond = load(resources, R.drawable.shop_product_diamond);
        diamondChest =
                load(resources, R.drawable.shop_product_diamond_chest_generated);
        coin = load(resources, R.drawable.shop_product_coin_stack);
        membershipNoticeHelp = load(resources, R.drawable.membership_notice_help);
        membershipNoticeLink = load(resources, R.drawable.membership_notice_link);

        categoryLabels.put(ShopCategory.HOT_RECOMMENDATION,
                load(resources, R.drawable.shop_original_tab_hot));
        categoryLabels.put(ShopCategory.DIAMOND_RECHARGE,
                load(resources, R.drawable.shop_original_tab_diamond));
        categoryLabels.put(ShopCategory.ROOM_CARD,
                load(resources, R.drawable.shop_original_tab_room_card));
        categoryLabels.put(ShopCategory.COIN,
                load(resources, R.drawable.shop_original_tab_gold));
        categoryLabels.put(ShopCategory.COUPON_STORE,
                load(resources, R.drawable.shop_original_tab_coupon));

        icon(resources, "diamond", R.drawable.shop_product_diamond);
        icon(resources, "coin_stack", R.drawable.shop_product_coin_stack);
        icon(resources, "coin_bag", R.drawable.shop_product_coin_bag);
        icon(resources, "coin_chest", R.drawable.shop_product_coin_chest);
        icon(resources, "coin_gift", R.drawable.shop_product_coin_gift);
        icon(resources, "vip_gift", R.drawable.shop_product_vip_gift);
        icon(resources, "daily_gift", R.drawable.shop_product_daily_gift);
        icon(resources, "rose", R.drawable.shop_product_rose);
        icon(resources, "wash_card", R.drawable.shop_product_wash_card);
        icon(resources, "luck_bead", R.drawable.shop_product_luck_bead);
        icon(resources, "recorder", R.drawable.shop_product_recorder_gold);
        icon(resources, "room_card", R.drawable.shop_product_room_card);
        icon(resources, "room_card_bound", R.drawable.shop_product_room_card_bound);
        icon(resources, "coupon_gold", R.drawable.shop_product_coupon_gold);
        icon(resources, "coupon_black", R.drawable.shop_product_coupon_black);
        icon(resources, "tablecloth", R.drawable.shop_product_tablecloth);
        icon(resources, "card_back", R.drawable.shop_product_card_back);
        icon(resources, "avatar_frame", R.drawable.shop_product_avatar_frame);
        icon(resources, "press_bull", R.drawable.shop_product_press_bull);
        icon(resources, "vehicle_150801", R.drawable.shop_product_vehicle_150801);
        icon(resources, "vehicle_150802", R.drawable.shop_product_vehicle_150802);
        icon(resources, "vehicle_150803", R.drawable.shop_product_vehicle_150803);
        icon(resources, "vehicle_150804", R.drawable.shop_product_vehicle_150804);
        icon(resources, "vehicle_150805", R.drawable.shop_product_vehicle_150805);
        icon(resources, "vehicle_150806", R.drawable.shop_product_vehicle_150806);
        icon(resources, "vehicle_150807", R.drawable.shop_product_vehicle_150807);
        icon(resources, "vehicle_150808", R.drawable.shop_product_vehicle_150808);
        icon(resources, "vehicle_150816", R.drawable.shop_product_vehicle_150816);
        icon(resources, "face", R.drawable.shop_product_face);
        icon(resources, "slipper", R.drawable.shop_product_slipper);
        icon(resources, "thumb", R.drawable.shop_product_thumb);
        icon(resources, "voice", R.drawable.shop_product_voice);
        icon(resources, "treasure_pot", R.drawable.shop_product_treasure_pot);
        icon(resources, "GOLD_MEMBER_WEEK", R.drawable.shop_original_membership_week);
        icon(resources, "GOLD_MEMBER_MONTH", R.drawable.shop_original_membership_month);
        icon(resources, "GOLD_MEMBER_VALUE_MONTH", R.drawable.shop_original_membership_value_month);
    }

    static void preload(Resources resources) {
        synchronized (CACHE_LOCK) {
            if (cached != null || preloadStarted) {
                return;
            }
            preloadStarted = true;
        }
        Thread thread =
                new Thread(
                        () -> {
                            android.os.Process.setThreadPriority(
                                    android.os.Process.THREAD_PRIORITY_BACKGROUND);
                            synchronized (CACHE_LOCK) {
                                try {
                                    if (cached == null) {
                                        cached = new ShopDrawableSet(resources);
                                    }
                                } catch (RuntimeException error) {
                                    Log.w(TAG, "Unable to preload shop drawables", error);
                                } finally {
                                    preloadStarted = false;
                                }
                            }
                        },
                        "shop-drawable-preload");
        thread.start();
    }

    static ShopDrawableSet obtain(Resources resources) {
        ShopDrawableSet available = cached;
        if (available != null) {
            return available;
        }
        synchronized (CACHE_LOCK) {
            if (cached == null) {
                cached = new ShopDrawableSet(resources);
            }
            return cached;
        }
    }

    Bitmap categoryLabel(ShopCategory category) {
        return categoryLabels.get(category);
    }

    Resources resources() {
        return resources;
    }

    Bitmap productIcon(String iconKey) {
        Bitmap bitmap = productIcons.get(iconKey);
        return bitmap == null ? productIcons.get("daily_gift") : bitmap;
    }

    Bitmap productIcon(ShopProduct product) {
        Bitmap productSpecific = productIcons.get(product.productCode());
        return productSpecific == null ? productIcon(product.iconKey()) : productSpecific;
    }

    private void icon(Resources resources, String key, int resourceId) {
        productIcons.put(key, load(resources, resourceId));
    }

    private static Bitmap load(Resources resources, int resourceId) {
        Bitmap bitmap = BitmapFactory.decodeResource(resources, resourceId);
        if (bitmap == null) {
            throw new IllegalStateException("Unable to decode shop drawable " + resourceId);
        }
        return bitmap;
    }
}
