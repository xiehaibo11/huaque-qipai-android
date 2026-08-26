#!/bin/zsh
set -euo pipefail

resource_dir="${0:A:h:h}/app/src/main/res/drawable-nodpi"

verify_size() {
  local file="$1" expected="$2" actual
  [[ -f "$file" ]] || { print -u2 "missing: $file"; return 1; }
  actual=$(magick identify -format '%wx%h' "$file")
  [[ "$actual" == "$expected" ]] || {
    print -u2 "size mismatch: $file expected=$expected actual=$actual"
    return 1
  }
}

verify_transparency() {
  local file="$1" opaque
  opaque=$(magick identify -format '%[opaque]' "$file")
  [[ "$opaque" == "False" ]] || {
    print -u2 "missing transparency: $file"
    return 1
  }
}

verify_size "$resource_dir/lobby_game_cards_static.png" 1183x613
verify_size "$resource_dir/lobby_icon_taizhou.png" 295x379
verify_size "$resource_dir/lobby_icon_wahua.png" 312x214
verify_size "$resource_dir/lobby_icon_shisanshui.png" 356x223
verify_transparency "$resource_dir/lobby_icon_taizhou.png"
verify_transparency "$resource_dir/lobby_icon_wahua.png"
verify_transparency "$resource_dir/lobby_icon_shisanshui.png"
