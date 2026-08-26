#!/bin/zsh
set -euo pipefail

tool_dir=${0:A:h}
android_dir=${tool_dir:h}

python3 "$tool_dir/export_friend_panel_assets.py" \
  --output-dir "$android_dir/app/src/main/res/drawable-nodpi" \
  --verify
