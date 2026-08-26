#!/bin/zsh
set -e

project_dir="/Users/mosc/Downloads/逆向/android"
drawable_dir="$project_dir/app/src/main/res/drawable-nodpi"
jsx_file="$project_dir/tools/export_lobby_motion_assets.jsx"
temp_dir="${TMPDIR%/}/huaque-lobby-motion"

osascript -e "tell application \"Adobe Photoshop 2026\" to do javascript file POSIX file \"$jsx_file\""

crop_and_make_outside_transparent() {
    local image_path="$1"
    local crop_geometry="$2"
    local cleaned_path="${image_path%.png}.clean.png"
    magick "$image_path" \
        -crop "$crop_geometry" +repage \
        -bordercolor white -border 1 \
        -alpha on -fuzz 2% -fill none -draw 'alpha 0,0 floodfill' \
        -shave 1x1 \
        "$cleaned_path"
    mv "$cleaned_path" "$image_path"
}

crop_and_make_outside_transparent \
    "$drawable_dir/lobby_icon_taizhou.png" "295x379+1188+302"
crop_and_make_outside_transparent \
    "$drawable_dir/lobby_icon_wahua.png" "312x214+1597+239"
crop_and_make_outside_transparent \
    "$drawable_dir/lobby_icon_shisanshui.png" "356x223+1555+532"
crop_and_make_outside_transparent \
    "$temp_dir/taizhou-static.png" "330x613+1159+235"
crop_and_make_outside_transparent \
    "$temp_dir/wahua-static.png" "393x243+1530+235"
crop_and_make_outside_transparent \
    "$temp_dir/shisanshui-static.png" "393x343+1530+506"

magick "$drawable_dir/lobby_game_cards.png" \
    "$temp_dir/taizhou-static.png" -geometry +0+0 -composite \
    "$temp_dir/wahua-static.png" -geometry +371+0 -composite \
    "$temp_dir/shisanshui-static.png" -geometry +371+271 -composite \
    "$drawable_dir/lobby_game_cards_static.png"
