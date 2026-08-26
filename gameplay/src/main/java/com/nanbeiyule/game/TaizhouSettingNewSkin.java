package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.nanbeiyule.game.TaizhouSettingStyle.Choice;

/** {@code settingNew.plist} / {@code settingNew2.plist} 导出的位图集合。 */
final class TaizhouSettingNewSkin {
    final Bitmap background;
    final Bitmap detailBackground;
    final Bitmap card;
    final Bitmap cardFrame;
    final Bitmap cardSelected;
    final Bitmap plate;
    final Bitmap plateWide;
    final Bitmap itemBackground;
    final Bitmap itemSelected;
    final Bitmap close;
    final Bitmap quit;
    final Bitmap back;
    final Bitmap save;
    final Bitmap planBar;
    final Bitmap planNormal;
    final Bitmap planSelected;
    final Bitmap switchOn;
    final Bitmap switchOff;
    final Bitmap voiceMale;
    final Bitmap voiceFemale;
    final Bitmap voiceButton;
    final Bitmap voiceListBackground;
    final Bitmap trust;
    final Bitmap line;
    final Bitmap sliderTrackNarrow;
    final Bitmap sliderTrackWide;
    final Bitmap sliderFillNarrow;
    final Bitmap sliderFillWide;
    final Bitmap sliderThumb;
    final Bitmap previewBackground;
    final Bitmap handArrow;

    private final Bitmap[] words;
    private final Bitmap[] backs;
    private final Bitmap[] bodies;
    private final Bitmap[] faces;
    private final Bitmap[] tables;
    private final Bitmap[] outStyles;
    private final Bitmap[] effects;
    private final Bitmap[] handDirections;
    private final Bitmap[] handSorts;
    private final Bitmap[] outTables;

    TaizhouSettingNewSkin(Context context) {
        background = load(context, R.drawable.taizhou_setting_new_bg);
        detailBackground = load(context, R.drawable.taizhou_setting_new_bg2);
        card = load(context, R.drawable.taizhou_setting_new_bg3);
        cardFrame = load(context, R.drawable.taizhou_setting_new_menu_select_2);
        cardSelected = load(context, R.drawable.taizhou_setting_new_menu_select_1);
        plate = load(context, R.drawable.taizhou_setting_new_slider_text);
        plateWide = load(context, R.drawable.taizhou_setting_new_slider_text_max);
        itemBackground = load(context, R.drawable.taizhou_setting_new_bg_detail);
        itemSelected = load(context, R.drawable.taizhou_setting_new_item_select);
        quit = load(context, R.drawable.taizhou_setting_new_quit);
        back = load(context, R.drawable.taizhou_setting_new_back_button);
        save = load(context, R.drawable.taizhou_setting_new_save_disabled);
        planBar = load(context, R.drawable.taizhou_setting_new_plan_bg);
        planNormal = load(context, R.drawable.taizhou_setting_new_plan_1);
        planSelected = load(context, R.drawable.taizhou_setting_new_plan_2);
        switchOn = load(context, R.drawable.taizhou_setting_new_switch_on_2);
        switchOff = load(context, R.drawable.taizhou_setting_new_switch_off_2);
        voiceMale = load(context, R.drawable.taizhou_setting_new_switch_male);
        voiceFemale = load(context, R.drawable.taizhou_setting_new_switch_female);
        voiceButton = load(context, R.drawable.taizhou_setting_new_voice_on);
        voiceListBackground = load(context, R.drawable.taizhou_setting_new_voice_bg);
        trust = load(context, R.drawable.taizhou_setting_new_trust);
        line = load(context, R.drawable.taizhou_setting_new_line);
        sliderTrackNarrow = load(context, R.drawable.taizhou_setting_new_slider_bg_1);
        sliderTrackWide = load(context, R.drawable.taizhou_setting_new_slider_bg_2);
        sliderFillNarrow = load(context, R.drawable.taizhou_setting_new_slider_1);
        sliderFillWide = load(context, R.drawable.taizhou_setting_new_slider_2);
        sliderThumb = load(context, R.drawable.taizhou_setting_new_check);
        previewBackground = load(context, R.drawable.taizhou_setting_new_preview_bg);
        handArrow = load(context, R.drawable.taizhou_setting_new_hand_arrow);

        words = load(context, R.drawable.taizhou_setting_new_word_1,
                R.drawable.taizhou_setting_new_word_2);
        backs = load(context, R.drawable.taizhou_setting_new_back_1,
                R.drawable.taizhou_setting_new_back_2,
                R.drawable.taizhou_setting_new_back_3,
                R.drawable.taizhou_setting_new_back_4,
                R.drawable.taizhou_setting_new_back_4,
                R.drawable.taizhou_setting_new_back_6);
        bodies = load(context, R.drawable.taizhou_setting_new_body_1,
                R.drawable.taizhou_setting_new_body_2);
        faces = load(context, R.drawable.taizhou_setting_new_face_1,
                R.drawable.taizhou_setting_new_face_2);
        tables = load(context, R.drawable.taizhou_setting_new_table_1,
                R.drawable.taizhou_setting_new_table_2,
                R.drawable.taizhou_setting_new_table_3,
                R.drawable.taizhou_setting_new_table_4,
                R.drawable.taizhou_setting_new_table_5,
                R.drawable.taizhou_setting_new_table_6,
                R.drawable.taizhou_setting_new_table_7);
        outStyles = load(context, R.drawable.taizhou_setting_new_out_style_1,
                R.drawable.taizhou_setting_new_out_style_2);
        effects = load(context, R.drawable.taizhou_setting_new_effects_1,
                R.drawable.taizhou_setting_new_effects_2);
        handDirections = load(context, R.drawable.taizhou_setting_new_hand_direction_1,
                R.drawable.taizhou_setting_new_hand_direction_2);
        handSorts = load(context, R.drawable.taizhou_setting_new_hand_sort_1,
                R.drawable.taizhou_setting_new_hand_sort_2);
        outTables = load(context, R.drawable.taizhou_setting_new_out_table_1,
                R.drawable.taizhou_setting_new_out_table_2);

        Bitmap atlas = load(context, R.drawable.taizhou_mahjong_game_layer);
        close = TaizhouMahjongGameLayerBitmap.extract(atlas, "mah_btn_close.png");
        atlas.recycle();
    }

    /**
     * 按真实枚举值取图，与 {@code KW_TEXTUTRE_LIST} 的「前缀 + 真实值」一致。
     * 出牌轨迹与插牌没有静态图，原版挂的是 DragonBones，见
     * {@link TaizhouSettingNewAnimations}。
     */
    Bitmap image(Choice choice, int realValue) {
        if (TaizhouSettingNewAnimations.handles(choice)) {
            return null;
        }
        Bitmap[] group = switch (choice) {
            case WORD_TYPE -> words;
            case BACK_TYPE -> backs;
            case BODY_TYPE -> bodies;
            case FACE_TYPE -> faces;
            case TABLE_STYLE -> tables;
            case OUT_MOVE_STYLE, INSERT_STYLE -> null;
            case OUT_STYLE -> outStyles;
            case OUT_EFFECTS -> effects;
            case HAND_STYLE -> handDirections;
            case HAND_SORT_STYLE -> handSorts;
            case OUT_TABLE_CARD_STYLE -> outTables;
        };
        int index = Math.max(1, Math.min(group.length, realValue)) - 1;
        return group[index];
    }

    private static Bitmap load(Context context, int resourceId) {
        return BitmapFactory.decodeResource(context.getResources(), resourceId);
    }

    private static Bitmap[] load(Context context, int... resourceIds) {
        Bitmap[] result = new Bitmap[resourceIds.length];
        for (int index = 0; index < resourceIds.length; index++) {
            result[index] = load(context, resourceIds[index]);
        }
        return result;
    }
}
