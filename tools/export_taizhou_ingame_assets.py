#!/usr/bin/env python3
"""Export the original in-round (局内) Taizhou mahjong sprites and armatures.

Source of truth is the decrypted Zhejiang lobby 1.5.4 hotfix resource tree; the
frames below are exactly the ones referenced by the recovered CSB layouts
(TrustLayer.csb, DismissLayer.csb, WatchLayer.csb) and their Lua modules.
"""
import argparse
import json
import plistlib
import re
import shutil
from pathlib import Path

from PIL import Image

# drawable name -> (atlas key, original frame name)
SPRITES = {
    # Trust 托管 —— Common/CSB/GameBase/TrustLayer.csb
    "taizhou_trust_tip_bg": ("@file", "cocosStudio/Common/Image/trust_tip_bg.png"),
    # Dismiss 解散 —— Common/CSB/GameBase/DismissLayer.csb
    "taizhou_dismiss_title": ("common_layer", "img_title_sqjs.png"),
    "taizhou_dismiss_close": ("common_layer", "common_btn_closed.png"),
    "taizhou_dismiss_agree": ("dismiss", "mah_btn_agree.png"),
    "taizhou_dismiss_refuse": ("dismiss", "mah_btn_refuse.png"),
    "taizhou_dismiss_clock": ("dismiss", "mah_img_clock.png"),
    "taizhou_dismiss_default_head": ("dismiss", "mah_img_default_head.png"),
    # WatchGame 观战 —— Common/CSB/GameBase/WatchLayer.csb
    "taizhou_watch_speed_bg": ("watch_game", "img_watch_multibg.png"),
    "taizhou_watch_real_normal": ("watch_game", "btn_watch_real_1.png"),
    "taizhou_watch_real_pressed": ("watch_game", "btn_watch_real_2.png"),
    "taizhou_watch_delay_normal": ("watch_game", "btn_watch_delay_1.png"),
    "taizhou_watch_delay_pressed": ("watch_game", "btn_watch_delay_2.png"),
    "taizhou_watch_exit": ("@file", "cocosStudio/Common/Image/btn_exit_watch.png"),
    # PlayerInfo 玩家信息 —— Common/CSB/GameBase/PlayerInfoLayer.csb
    "taizhou_player_info_panel": ("player_info", "PlayerInfoNew_Img_di.png"),
    "taizhou_player_info_ornament_left": ("player_info", "PlayerInfoNew_Img_hw2.png"),
    "taizhou_player_info_ornament_right": ("player_info", "PlayerInfoNew_Img_hw1.png"),
    "taizhou_player_info_divider_thick": ("player_info", "PlayerInfoNew_Img_xian1.png"),
    "taizhou_player_info_divider_thin": ("player_info", "PlayerInfoNew_Img_xian2.png"),
    "taizhou_player_info_vip_panel": ("player_info", "PlayerInfoNew_Img_di2.png"),
    "taizhou_player_info_section_title": ("player_info", "PlayerInfoNew_Img_bt.png"),
    "taizhou_player_info_range_icon": ("player_info", "PlayerInfoNew_Btn_cj.png"),
    "taizhou_player_info_kick": ("player_info", "PlayerInfoNew_Btn_qcfj.png"),
    "taizhou_player_info_blur_small": ("player_info", "PlayerInfoNew_Img_mh1.png"),
    "taizhou_player_info_blur_wide": ("player_info", "PlayerInfoNew_Img_mh2.png"),
    "taizhou_player_info_blur_flat": ("player_info", "PlayerInfoNew_Img_mh3.png"),
    "taizhou_player_info_buy_vip": ("player_info", "PlayerInfoNew_Btn_huang_1.png"),
    "taizhou_player_info_close": ("player_info", "PlayerInfoNew_BtnGuanbi.png"),
    "taizhou_player_info_checkbox_off": ("player_info", "PlayerInfoNew_Btn_off.png"),
    "taizhou_player_info_checkbox_on": ("player_info", "PlayerInfoNew_Btn_on.png"),
    "taizhou_player_info_diamond": ("player_info", "PlayerInfoNew_icon_diamond.png"),
    "taizhou_player_info_room_card": ("player_info", "PlayerInfoNew_icon_card.png"),
    "taizhou_player_info_speed_normal": ("user_info", "common_userinfo_Img_zc.png"),
    "taizhou_player_info_help": ("user_info", "common_userinfo_Btn_yw.png"),
}

ATLASES = {
    "common_layer": "cocosStudio/Common/Image/common_layer",
    "dismiss": "cocosStudio/Common/Image/dismiss",
    "watch_game": "cocosStudio/Common/Image/watchGame",
    "player_info": "cocosStudio/Common/Image/PlayerInfoNew",
    "user_info": "cocosStudio/Common/Image/common_user_info",
}

# asset directory name -> original armature directory
ARMATURES = {
    "taizhou_trust_effects/tuoguan_ani": "animation/GameCommon/tuoguan_ani",
}


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


def _load_atlas(root, stem):
    atlas = Image.open(root / f"{stem}.png").convert("RGBA")
    frames = plistlib.loads((root / f"{stem}.plist").read_bytes())["frames"]
    return atlas, frames


def export_drawables(reference_root, output_dir):
    output_dir.mkdir(parents=True, exist_ok=True)
    loaded = {}
    for output_name, (atlas_key, frame_name) in SPRITES.items():
        target = output_dir / f"{output_name}.png"
        if atlas_key == "@file":
            shutil.copyfile(reference_root / frame_name, target)
            continue
        if atlas_key not in loaded:
            loaded[atlas_key] = _load_atlas(reference_root, ATLASES[atlas_key])
        atlas, frames = loaded[atlas_key]
        if frame_name not in frames:
            raise KeyError(f"Missing atlas frame {frame_name} in {atlas_key}")
        export_atlas_frame(atlas, frames[frame_name]).save(target)


def export_armatures(reference_root, assets_dir):
    for asset_name, source_name in ARMATURES.items():
        source = reference_root / source_name
        target = assets_dir / asset_name
        target.mkdir(parents=True, exist_ok=True)
        for path in sorted(source.iterdir()):
            if path.suffix == ".plist":
                # Armature plists are property lists of plain frame rectangles;
                # re-emit them as JSON so the runtime needs no plist parser.
                frames = plistlib.loads(path.read_bytes())["frames"]
                (target / f"{path.stem}.json").write_text(
                    json.dumps(frames, ensure_ascii=False, sort_keys=True),
                    encoding="utf-8")
            else:
                shutil.copyfile(path, target / path.name)


def verify(output_dir, assets_dir):
    for output_name in SPRITES:
        path = output_dir / f"{output_name}.png"
        if not path.is_file():
            raise FileNotFoundError(path)
        if Image.open(path).convert("RGBA").getbbox() is None:
            raise ValueError(f"Drawable is fully transparent: {path}")
    for asset_name in ARMATURES:
        directory = assets_dir / asset_name
        if not any(directory.glob("*.ExportJson")):
            raise FileNotFoundError(f"Missing armature ExportJson in {directory}")
        if not any(directory.glob("*.json")):
            raise FileNotFoundError(f"Missing armature frame table in {directory}")
        if not any(directory.glob("*.png")):
            raise FileNotFoundError(f"Missing armature texture in {directory}")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--reference-root",
        type=Path,
        default=Path("/Users/mosc/Downloads/逆向/浙江游戏大厅/hotfix-decrypted/res"))
    parser.add_argument("--output-dir", required=True, type=Path)
    parser.add_argument("--assets-dir", required=True, type=Path)
    parser.add_argument("--verify", action="store_true")
    args = parser.parse_args()
    if not args.verify:
        export_drawables(args.reference_root, args.output_dir)
        export_armatures(args.reference_root, args.assets_dir)
    verify(args.output_dir, args.assets_dir)
    print(f"{len(SPRITES)} drawables, {len(ARMATURES)} armatures OK")


if __name__ == "__main__":
    main()
