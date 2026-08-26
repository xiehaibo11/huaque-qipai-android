#!/usr/bin/env python3
import argparse
import plistlib
import re
import shutil
from pathlib import Path

from PIL import Image


SPRITES = {
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
}

NINE_PATCH_SPRITES = {
    "friend_panel_ready_bg": (38, 38, 50, 50),
    "friend_panel_upcoming_filter_bg": (20, 20, 14, 14),
    "friend_panel_upcoming_filter_list_bg": (20, 20, 20, 20),
}


def to_nine_patch(image, left, right, top, bottom):
    width, height = image.size
    result = Image.new("RGBA", (width + 2, height + 2), (0, 0, 0, 0))
    result.paste(image, (1, 1), image)
    black = (0, 0, 0, 255)
    for x in range(1 + left, 1 + width - right):
        result.putpixel((x, 0), black)
    for y in range(1 + top, 1 + height - bottom):
        result.putpixel((0, y), black)
    for x in range(1, 1 + width):
        result.putpixel((x, height + 1), black)
    for y in range(1, 1 + height):
        result.putpixel((width + 1, y), black)
    return result


def _numbers(value):
    return [int(number) for number in re.findall(r"-?\d+", value)]


def export_atlas_frame(atlas, frame):
    x, y, logical_width, logical_height = _numbers(frame["frame"])
    rotated = bool(frame.get("rotated", False))
    packed_width = logical_height if rotated else logical_width
    packed_height = logical_width if rotated else logical_height
    image = atlas.crop((x, y, x + packed_width, y + packed_height))
    if rotated:
        image = image.transpose(Image.Transpose.ROTATE_90)

    source_width, source_height = _numbers(frame["sourceSize"])
    offset_x, offset_y = _numbers(frame.get("offset", "{0,0}"))
    left = (source_width - image.width) // 2 + offset_x
    top = (source_height - image.height) // 2 - offset_y
    restored = Image.new("RGBA", (source_width, source_height), (0, 0, 0, 0))
    restored.paste(image, (left, top), image)
    return restored


def _load_atlas(image_path, plist_path):
    atlas = Image.open(image_path).convert("RGBA")
    frames = plistlib.loads(plist_path.read_bytes())["frames"]
    return atlas, frames


def export_assets(reference_root, output_dir):
    friends_dir = reference_root / "Common/Image/Friends"
    hall_image_dir = reference_root / "hall/Image"
    atlases = {
        "friend": _load_atlas(friends_dir / "friends.png", friends_dir / "friends.plist"),
        "imteahouse": _load_atlas(
            hall_image_dir / "IMTeahouse.png", hall_image_dir / "IMTeahouse.plist"),
    }
    output_dir.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(friends_dir / "friend_list_bg.png", output_dir / "friend_panel_bg.png")
    for output_name, source_name in SPRITES.items():
        atlas_key = "imteahouse" if source_name.startswith("imteahouse_") else "friend"
        atlas, frames = atlases[atlas_key]
        if source_name not in frames:
            raise KeyError(f"Missing atlas frame: {source_name}")
        image = export_atlas_frame(atlas, frames[source_name])
        if output_name in NINE_PATCH_SPRITES:
            left, right, top, bottom = NINE_PATCH_SPRITES[output_name]
            (output_dir / f"{output_name}.png").unlink(missing_ok=True)
            to_nine_patch(image, left, right, top, bottom).save(
                output_dir / f"{output_name}.9.png")
        else:
            image.save(output_dir / f"{output_name}.png")


def verify_assets(output_dir):
    expected = ["friend_panel_bg.png"] + [
        f"{name}.9.png" if name in NINE_PATCH_SPRITES else f"{name}.png"
        for name in SPRITES]
    for filename in expected:
        path = output_dir / filename
        if not path.is_file():
            raise FileNotFoundError(path)
        image = Image.open(path).convert("RGBA")
        if image.getbbox() is None:
            raise ValueError(f"Drawable is fully transparent: {path}")
    if Image.open(output_dir / "friend_panel_bg.png").size != (695, 1080):
        raise ValueError("friend_panel_bg.png must remain 695x1080")
    for forbidden in ("friends.png", "IMTeahouse.png"):
        if (output_dir / forbidden).exists():
            raise ValueError(f"Full atlas must not be packaged: {forbidden}")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--reference-root", type=Path)
    parser.add_argument("--output-dir", required=True, type=Path)
    parser.add_argument("--verify", action="store_true")
    args = parser.parse_args()
    if args.verify:
        verify_assets(args.output_dir)
        return
    if args.reference_root is None:
        parser.error("--reference-root is required when exporting")
    export_assets(args.reference_root, args.output_dir)
    verify_assets(args.output_dir)


if __name__ == "__main__":
    main()
