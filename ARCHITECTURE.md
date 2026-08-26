# Android architecture migration

This Android workspace now follows the `浙江游戏大厅` architecture direction instead of a standalone native-only page project.

## Original architecture source

```text
/Users/mosc/Downloads/逆向/浙江游戏大厅
```

Observed startup chain:

```text
org.cocos2dx.lua.AppActivity
  -> assets/src/main.lua
  -> app.Launcher:start()
  -> hot update / package update
  -> app.MyApp:afterHotUpdateRun()
  -> XH.Controller:getInstance():enterLogin()
  -> login.Scene
  -> login.Modules.Login.View
  -> cocosStudio/hall/CSB/LoginScene.csb
```

Observed lobby chain:

```text
lobby.Scene
  -> lobby.Modules.Lobby.View
  -> cocosStudio/hall/CSB/MainScene.csb
```

## Current Android project mapping

```text
android/app/src/main/java/org/cocos2dx/lua/AppActivity.java
android/app/src/main/assets/src
android/app/src/main/assets/res
android/app/src/main/jniLibs
```

`AppActivity.java` keeps the original Java bridge name used by Lua:

```text
org/cocos2dx/lua/AppActivity.hideSplash()
org/cocos2dx/lua/AppActivity.readJsonFile(path)
```

`assets/src` contains the copied Lua startup/login/lobby code from:

```text
/Users/mosc/Downloads/逆向/浙江游戏大厅/lua-src-apk
```

`assets/res` contains selected Cocos Studio files and UI atlas files from:

```text
/Users/mosc/Downloads/逆向/浙江游戏大厅/hotfix-decrypted/res
```

`jniLibs` contains the original Cocos native libraries:

```text
libcocos2dlua.so
```

## UI replacement scope

The replacement is intentionally limited to startup, loading, and login visual assets.

Copied Cocos layout files:

```text
assets/res/cocosStudio/hall/CSB/Loading.csb
assets/res/cocosStudio/hall/CSB/LoginScene.csb
assets/res/cocosStudio/hall/CSB/MainScene.csb
```

Copied original UI atlases:

```text
assets/res/cocosStudio/hall/Image/img_login.png
assets/res/cocosStudio/hall/Image/img_login.plist
assets/res/cocosStudio/hall/Image/LoginMethod/LoginMethod.png
assets/res/cocosStudio/hall/Image/LoginMethod/LoginMethod.plist
assets/res/cocosStudio/hall/Image/lobby.png
assets/res/cocosStudio/hall/Image/lobby.plist
assets/res/cocosStudio/hall/Image/NewGoldHall/Main/_Plist.png
assets/res/cocosStudio/hall/Image/NewGoldHall/Main/_Plist.plist
assets/res/cocosStudio/hall/Image/hallBg/SpringLobby.png
assets/res/cocosStudio/hall/Image/hallBg/SpringLobby.plist
```

Replaced Huaque visual assets:

```text
assets/res/cocosStudio/hall/Image/login_background.jpg
assets/res/cocosStudio/hall/Image/huaque_loading_bg.jpg
app/src/main/res/drawable-nodpi/huaque_spring_bg.jpg
```

The replacement background is derived from:

```text
/Users/mosc/Downloads/逆向/UI-skill install/花雀棋牌/加载背景原图
```

## Runtime note

The original project used a full Cocos2d-x Android Java runtime plus RePlugin/YMN SDK smali classes. This workspace currently preserves the architecture and resources, and keeps the native Android startup/loading/login preview implemented in `com.huaque.ui.MainActivity`.

To run the copied Lua/Cocos resources directly, the next step is to wire the original Cocos2d-x Java runtime classes or rebuild through an apktool-based repack workspace. The UI replacement assets are already staged in the paths expected by the original Lua search paths.
