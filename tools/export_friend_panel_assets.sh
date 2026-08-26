#!/bin/zsh
set -euo pipefail

tool_dir=${0:A:h}
android_dir=${tool_dir:h}
workspace_dir=${android_dir:h}
reference_root="$workspace_dir/浙江游戏大厅/hotfix-decrypted/res/cocosStudio"
output_dir="$android_dir/app/src/main/res/drawable-nodpi"

python3 "$tool_dir/export_friend_panel_assets.py" \
  --reference-root "$reference_root" \
  --output-dir "$output_dir"
