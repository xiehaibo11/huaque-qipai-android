# 浙江大厅商城与快速开始修正设计

## 目标

修正首页浙江底栏的两个缺口：商城入口恢复原版大购物车、金币与“商城”字牌的完整静态默认帧；右下角恢复浙江原版“快速开始”按钮，副标题固定为“金币场-台州麻将”。

## 视觉基准与原版证据

- 用户确认的视觉基准：`/Users/mosc/Downloads/逆向/image.png`。
- 商城来源：`res/animation/Lobby/Base/NewGoldHall/StoreBtn/zzb_jbdt_sc.png/.atlas/.json`。
  - `cc`：购物车主体。
  - `jb`、`jb1`、`jb2`、`jb3`、`jb4`：金币主体与散落金币。
  - `zi`：原版“商城”字牌。
- 快速开始来源：
  - `res/cocosStudio/hall/CSB/MainScene.csd` 中 `_KWA_BOTTOM_QUICKSTAR`：`x=1436`、`y=890`、`430×180`。
  - `res/cocosStudio/hall/CSB/NewGoldHall/QuickStartBtn.csd`：按钮内部尺寸、标题和副标题字体参数。
  - `res/animation/Lobby/Base/NewGoldHall/QuickStart/zzb_jbdt_ksks.png/.atlas/.json`：按钮底板与“快速开始”主标题。

## 实现方案

### 商城

不引入 Spine 运行时。从原版 Spine 图集提取全部可见商城构件，根据原骨骼初始变换和用户提供的原版截图组合为透明 PNG。组合图保留大购物车、金币、散落金币和商城字牌，在第一个底栏槽位向上溢出；不改动装扮及后续七项的位置。

### 快速开始

在 `1920×1080` 虚拟画布中按原版放置于 `(1436, 890, 430, 180)`。按钮背景和“快速开始”主标题使用浙江原图集组成静态默认帧；副标题使用方正粗圆字体，内容为“金币场-台州麻将”。点击调用现有 `openTaizhouGoldGame()`，复用现有登录态检查和 Token 传递。

## 层级与交互

- 浙江底栏仍位于 `(80, 945, 1300, 95)`。
- 快速开始位于底栏右侧，不遮挡“更多”入口。
- “更多”二级菜单打开时保持现有层级；点击快速开始前先关闭二级菜单，再进入台州麻将。
- 商城点击区覆盖向上溢出的完整图标，不只限于 `160×95` 底栏槽位。

## 测试与验收

1. 先在 `LobbyBottomBarModelTest` 新增失败测试，验证快速开始矩形为 `(1436, 890, 430, 180)`、副标题为“金币场-台州麻将”。
2. 模型实现后确认目标测试转绿，再接入 Android View。
3. 运行 `:app:testDebugUnitTest` 与 `:app:assembleDebug`。
4. 在 `emulator-5554` 安装后截图，与用户原版截图检查：商城主体尺寸、金币完整性、快速开始位置、主副标题和与底栏的间距。
5. 点击快速开始，确认进入现有台州麻将流程。

## 非目标

- 不引入 Cocos、Lua 或 Spine Android 运行时。
- 不改动其他游戏卡片、登录、好友、台州麻将内部流程或后端。
- 不使用截图裁剪替代透明原始素材。
