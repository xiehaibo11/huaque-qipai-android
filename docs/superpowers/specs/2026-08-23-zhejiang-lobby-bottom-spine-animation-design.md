# 浙江大厅底部商城与快速开始 Spine 动画设计

## 1. 目标

在当前 Android 原生首页中运行浙江游戏大厅原版 Spine 动画，替代商城和快速开始的纯静态表现：

1. 商城恢复原版购物车、金币翻转、金币位移、商城字牌和粒子闪光。
2. 快速开始恢复原版主标题呼吸缩放、按钮流光、光束和粒子。
3. 两套动画按原版时间轴持续循环，不使用人工模拟动效或预渲染 GIF/WebP。
4. 保留现有底栏、“更多”菜单、“金币场-台州麻将”副标题和点击行为。

## 2. 原版证据

### 2.1 商城

- 资源：`/浙江游戏大厅/原版可编辑源码/热更新工程/res/animation/Lobby/Base/NewGoldHall/StoreBtn/zzb_jbdt_sc.*`。
- `lobby/Modules/Lobby/Config.lua` 将商城定义为 Spine 入口：
  - `ArmatureName = "animation"`
  - `IconScaleSpine = 1`
  - `iconSize = 150×180`
  - `OffSetX = 7`
  - `OffSetY = -58`
- `GoldNew/Views/ResidentBtns/StoreBtn.lua` 通过 `initSpine(...)` 播放同一套骨骼。
- 骨骼动画 `animation` 时长约 `8.7s`，包含 `cc`、`jb*`、`coin_*`、`guangdian_0`、`zi` 和粒子插槽。

### 2.2 快速开始

- 资源：`/浙江游戏大厅/原版可编辑源码/热更新工程/res/animation/Lobby/Base/NewGoldHall/QuickStart/zzb_jbdt_ksks.*`。
- `lobby/Modules/Lobby/View.lua:updateQuickStart()` 调用 `SpineManager:playAni(..., "animation", true)`，最后一个参数明确为循环播放。
- 骨骼动画 `animation` 时长约 `8.1s`，包含：
  - `btn`：按钮底板。
  - `ksks`：“快速开始”标题，具有 `1.0 → 1.069 → 1.0` 的原版呼吸时间轴。
  - `btng/btng_00002..00028`：按钮流光序列。
  - `tx3_*`、`guanga` 和粒子插槽：光束与闪光。

## 3. 方案选择

### 方案 A：复用项目现有 Spine 3.7 运行时（采用）

`gameplay` 模块已包含：

- `Spine37JsonParser`
- `Spine37AtlasParser`
- `Spine37Runtime`
- `Spine37Animator`
- `Spine37MeshBuilder`
- `OriginalLobbyEffectView`
- EGL/OpenGL ES 2.0 透明渲染线程

同时，两套原版资源已存在于：

- `gameplay/src/main/assets/lobby_effects/zzb_jbdt_sc/`
- `gameplay/src/main/assets/lobby_effects/zzb_jbdt_ksks/`

因此可直接运行原版 JSON 骨骼、Atlas 旋转/裁切、Mesh、混合模式和时间轴，不增加第三方依赖。

### 方案 B：Android ValueAnimator 仿制（不采用）

只能近似实现整体缩放和位移，无法准确复原 Mesh 变形、插槽切换、加法混合和粒子节奏。

### 方案 C：预渲染动画图（不采用）

需要额外生成大量帧或动态 WebP，会增加包体、降低清晰度，且不再是原版骨骼时间轴实时播放。

## 4. 代码结构

### 4.1 `gameplay` 模块

新增一个仅负责本页坐标和骨骼列表的纯 Java 布局类 `ZhejiangLobbyBottomEffectLayout`：

- 定义商城与快速开始两个 `OriginalLobbyEffectSpec`。
- 将当前 `1920×1080` 虚拟坐标转换到现有渲染器使用的 `3200×1792` 页面坐标。
- 商城骨骼根节点对齐当前商城图标的原版位置：静态图位于 Android 虚拟坐标
  `(20, 840, 259, 196)`，骨骼静态包围盒为
  `x=-150.49..108.87, y=-17.52..177.73`，因此根节点为
  `(170.49, 1017.73)`。
- 快速开始骨骼根节点对齐 `GoldLayer.csd` 中 `(1436, 890, 430, 180)` 容器内的 `_KWA_QUICKSTART_POS = (215, 78)`。
- 快速开始的 Android 虚拟根节点为
  `(1436 + 215, 890 + 180 - 78) = (1651, 992)`；Y 轴公式明确处理
  Cocos 底部原点与 Android 顶部原点的差异。
- 两套动画均使用 `animation`，spec 比例为 `3200 / 1920`。锚点使用
  `x × 3200 / 1920`、`y × 1792 / 1080` 转到渲染页面坐标；继续遵循现有
  `OriginalLobbyEffectRenderer` 的 `3200×1792` 页面约定，不改动通用投影模型。
- 固定换算结果为：商城约 `(284.15, 1688.68)`，快速开始约
  `(2751.67, 1645.99)`。单元测试使用合理浮点误差比较，而不是字符串比较。

在现有公开 `OriginalLobbyEffectView` 上新增一个不暴露包内 `OriginalLobbyEffectSpec` 的公开工厂方法：

```java
public static OriginalLobbyEffectView createZhejiangBottomControls(
        Context context,
        Runnable onFirstFrameRendered)
```

回调在构造时注入，防止异步资源加载或首帧渲染先于监听器注册。现有构造函数保持
兼容并委托给内部构造逻辑；页面层只调用这个工厂，不复制 Spine 加载或 EGL 生命周期代码。

### 4.2 `app` 模块

`MainActivity.addZhejiangLobbyBottomBar()` 保存商城和快速开始两个静态 `ImageView`
的局部引用，并在静态兜底图之上、“更多”弹层与点击区之下增加一层全画布透明
`OriginalLobbyEffectView`。快速开始副标题是独立 `TextView`，首帧切换时始终保留。

层级顺序：

1. 首页背景和游戏卡片。
2. 底栏背景、商城和快速开始静态兜底。
3. 商城与快速开始 Spine 透明动画层。
4. “更多”二级菜单。
5. 主底栏、商城完整图标和快速开始的透明点击区。
6. 好友面板。

Spine `TextureView` 必须设为非点击、非聚焦且透明，不改变现有交互。

## 5. 静态兜底与首帧切换

资源加载和 EGL 初始化异步执行。为避免首页刚进入时空白：

- 保留现有完整商城静态 PNG 作为兜底。
- 保留现有完整快速开始静态 PNG 作为兜底。
- `OriginalLobbyEffectRenderThread` 仅在第一次 `EGL14.eglSwapBuffers(...)`
  返回成功后调用首帧通知；“完成 draw 调用”不视为首帧已展示。
- `OriginalLobbyEffectView` 将通知通过 `View.post(...)` 送回主线程，并隐藏两张
  兜底图，避免重复叠加导致变亮或重影；快速开始副标题不隐藏。
- 如 Spine 资源解析、EGL 或首帧渲染失败，兜底图保持可见，页面仍可交互。
- 渲染线程使用局部布尔状态保证首帧通知最多触发一次，不为此引入新的状态类或公共抽象。
- 如果视图已经 detach，则不再执行页面可见性回调。

## 6. 动画生命周期和性能

- 渲染线程使用现有 `OriginalLobbyEffectRenderThread`，目标 `30 FPS`。
- `Spine37Animator.wrapTime()` 按骨骼原始时长循环时间轴。
- 首页被新 `setContentView()` 替换后，`TextureView.onDetachedFromWindow()` 停止渲染线程、回收 GL Texture 和未上传 Bitmap。
- 重新进入首页时创建新动画视图，不复用已销毁的 EGL 上下文。
- 不新增第三方依赖，不新增运行时网络请求。

## 7. 交互与错误处理

- 商城完整溢出图标点击区保持不变，当前无商城业务页时继续显示“商城”提示。
- 快速开始点击区保持 `(1436, 890, 430, 180)`，关闭“更多”后调用 `openTaizhouGoldGame()`。
- 未认证会话继续使用现有“请先完成登录”保护。
- Spine 加载失败只记录错误并保留静态兜底，不阻断首页、点击和游戏入口。

## 8. 测试与验收

### 8.1 测试驱动

在 `gameplay/src/test/java/com/nanbeiyule/game/ZhejiangLobbyBottomEffectLayoutTest.java` 先写失败测试，覆盖：

1. 刚好返回商城和快速开始两个 spec。
2. 两者的资源目录、基名和动画名均与原版一致。
3. 虚拟坐标到渲染页面坐标的锚点转换为手工计算的固定值。
4. 比例为 `3200 / 1920`，确保 Spine 像素与当前虚拟像素一致。

遵循 RED → GREEN：新类/新方法不存在时先确认测试失败，再添加最小实现。
首帧回调位于 Android EGL 线程边界，不为单测可测性拆出无业务价值的包装类；通过
设备验收确认首帧后兜底消失且没有双影。

### 8.2 构建验证

```bash
/Users/mosc/.gradle/wrapper/dists/gradle-8.13-bin/5xuhj0ry160q40clulazy9h7d/gradle-8.13/bin/gradle \
  :gameplay:testDebugUnitTest \
  :app:testDebugUnitTest \
  :app:assembleDebug
```

### 8.3 动画验收

1. 安装到 `emulator-5554` 并进入首页。
2. 录制至少 `10s` 屏幕视频，覆盖商城 `8.7s` 和快速开始 `8.1s` 的完整循环。
3. 抽取不同时间点帧并确认像素发生变化：
   - 商城金币位置/旋转/粒子变化。
   - 快速开始标题缩放和流光帧变化。
4. 检查不存在静态兜底与 Spine 重叠导致的双影、变亮或错位。
5. 展开“更多”，确认弹层位于动画之上。
6. 点击商城和快速开始，确认动画层不拦截交互。

## 9. 非目标

- 不修改 Spine 骨骼 JSON、Atlas 或纹理。
- 不改变商城和快速开始的文案、业务路由或点击矩形。
- 不向 `app` 复制 `gameplay` 已有 Spine 运行时。
- 不引入 Spine 官方 SDK 或其他新依赖。
- 不顺手重构首页、底栏或游戏卡片其他代码。
