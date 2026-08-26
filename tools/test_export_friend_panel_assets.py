import importlib.util
import unittest
from pathlib import Path

from PIL import Image


MODULE_PATH = Path(__file__).with_name("export_friend_panel_assets.py")


def load_exporter():
    spec = importlib.util.spec_from_file_location("friend_exporter", MODULE_PATH)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class FriendPanelAssetExporterTest(unittest.TestCase):
    def test_restores_normal_frame_to_declared_source_size_and_offset(self):
        exporter = load_exporter()
        atlas = Image.new("RGBA", (4, 4), (0, 0, 0, 0))
        atlas.putpixel((1, 1), (255, 0, 0, 255))
        atlas.putpixel((2, 1), (0, 255, 0, 255))

        restored = exporter.export_atlas_frame(atlas, {
            "frame": "{{1,1},{2,1}}",
            "offset": "{1,-1}",
            "rotated": False,
            "sourceSize": "{6,5}",
        })

        self.assertEqual((6, 5), restored.size)
        self.assertEqual((255, 0, 0, 255), restored.getpixel((3, 3)))
        self.assertEqual((0, 255, 0, 255), restored.getpixel((4, 3)))

    def test_unrotates_texturepacker_frame_before_restoring_it(self):
        exporter = load_exporter()
        logical = Image.new("RGBA", (2, 3))
        logical.putdata([
            (255, 0, 0, 255), (0, 255, 0, 255),
            (0, 0, 255, 255), (255, 255, 0, 255),
            (255, 0, 255, 255), (0, 255, 255, 255),
        ])
        packed = logical.transpose(Image.Transpose.ROTATE_270)
        atlas = Image.new("RGBA", (5, 5), (0, 0, 0, 0))
        atlas.paste(packed, (1, 1))

        restored = exporter.export_atlas_frame(atlas, {
            "frame": "{{1,1},{2,3}}",
            "offset": "{0,0}",
            "rotated": True,
            "sourceSize": "{2,3}",
        })

        self.assertEqual(
            list(logical.get_flattened_data()),
            list(restored.get_flattened_data()))

    def test_manifest_contains_only_the_approved_panel_sprites(self):
        exporter = load_exporter()

        self.assertEqual({
            "friend_panel_open": "friend_btn_open_2.png",
            "friend_panel_close": "friend_btn_open_1.png",
            "friend_panel_tab_friends_on": "friend_lable_list_2.png",
            "friend_panel_tab_friends_off": "friend_lable_list_1.png",
            "friend_panel_tab_upcoming_on": "friend_lable_follow_2.png",
            "friend_panel_tab_upcoming_off": "friend_lable_follow_1.png",
            "friend_panel_invite_all": "friend_invite_all.png",
            "friend_panel_state_online": "friend_state_on_line.png",
            "friend_panel_state_offline": "friend_state_off_line.png",
            "friend_panel_state_gaming": "friend_state_gaming.png",
            "friend_panel_search": "imteahouse_search_img.png",
            "friend_panel_search_cancel": "imteahouse_search_cancel.png",
            "friend_panel_add_friend": "imteahouse_add_paiyou_small.png",
            "friend_panel_apply": "imteahouse_paiyou_apply_btn.png",
            "friend_panel_my_friends": "imteahouse_left_page_1.png",
            "friend_panel_my_groups": "imteahouse_left_page_2.png",
            "friend_panel_ready_bg": "friend_ready_bg.png",
            "friend_panel_ready_title": "friend_ready_title.png",
            "friend_panel_upcoming_filter_bg": "imteahouse_search.png",
            "friend_panel_upcoming_filter_button": "imteahouse_filter.png",
            "friend_panel_upcoming_filter_icon": "imteahouse_filter_icon.png",
            "friend_panel_upcoming_filter_list_bg": "imteahouse_box_bg.png",
            "friend_panel_upcoming_filter_line": "imteahouse_filter_line.png",
            "friend_panel_upcoming_filter_selected": "imteahouse_select.png",
            "friend_panel_upcoming_guide_bg": "friend_tips_bg.png",
            "friend_panel_upcoming_refresh": "imteahouse_refresh_btn.png",
        }, exporter.SPRITES)

        self.assertEqual({
            "friend_panel_ready_bg": (38, 38, 50, 50),
            "friend_panel_upcoming_filter_bg": (20, 20, 14, 14),
            "friend_panel_upcoming_filter_list_bg": (20, 20, 20, 20),
        }, exporter.NINE_PATCH_SPRITES)

    def test_creates_android_nine_patch_markers_around_original_pixels(self):
        exporter = load_exporter()
        source = Image.new("RGBA", (10, 12), (255, 240, 200, 255))

        result = exporter.to_nine_patch(source, left=3, right=3, top=4, bottom=4)

        self.assertEqual((12, 14), result.size)
        self.assertEqual((0, 0, 0, 0), result.getpixel((0, 0)))
        self.assertEqual((0, 0, 0, 255), result.getpixel((4, 0)))
        self.assertEqual((0, 0, 0, 255), result.getpixel((0, 5)))
        self.assertEqual((255, 240, 200, 255), result.getpixel((1, 1)))


if __name__ == "__main__":
    unittest.main()
