# 浙江游戏大厅首页底栏实施计划

> 后续修正计划：`2026-08-23-zhejiang-lobby-store-quick-start.md` 恢复浙江原版快速开始并重组完整商城图标，取代本计划中删除快速开始的早期步骤。

> 规格来源：`docs/superpowers/specs/2026-08-23-zhejiang-lobby-bottom-bar-design.md`

**目标：** 将 Android 首页当前的闲逸斗地主整图底栏替换成浙江游戏大厅最新普通大厅底栏，并保留现有首页其他区域和游戏入口。

**实现结构：** 用纯 Java `LobbyBottomBarModel` 固定浙江原版入口数据和 1920×1080 布局；`MainActivity` 继续使用项目现有 `BoxRoot` 渲染原生视图。底板、分隔线、红点、更多菜单底板、装扮新标记、商城静态图标和方正粗圆字体均从浙江原始资源提取或复制。

**技术栈：** Java、Android View、JUnit 4、Gradle；不增加第三方依赖。

---

## 任务 1：锁定底栏数据和几何布局

**文件：**

- 新增：`app/src/test/java/com/huaque/ui/LobbyBottomBarModelTest.java`
- 新增：`app/src/main/java/com/huaque/ui/LobbyBottomBarModel.java`

### 1.1 编写失败测试

测试覆盖：

- 默认入口数为 8。
- ID 和标题顺序为商城、装扮、战绩、活动、分享、背包、邮件、更多。
- ID 唯一。
- 底栏矩形为 `(80, 945, 1300, 95)`。
- 八个点击槽位从左向右等距排列且均在底栏内。
- 更多菜单默认关闭，连续两次切换后回到关闭状态。

### 1.2 运行目标测试并确认失败

运行：

```bash
/Users/mosc/.gradle/wrapper/dists/gradle-8.13-bin/5xuhj0ry160q40clulazy9h7d/gradle-8.13/bin/gradle :app:testDebugUnitTest --tests com.huaque.ui.LobbyBottomBarModelTest
```

预期：因为 `LobbyBottomBarModel` 尚不存在而编译失败。

### 1.3 添加最小模型实现

模型只包含：

- 入口 ID 常量和不可变入口列表。
- 简单 `Rect` 值对象。
- 底栏和点击槽位坐标。
- 更多菜单布尔状态切换函数。

不加入动态配置解析、Android View 依赖或未使用扩展点。

### 1.4 再次运行目标测试

预期：新增模型测试全部通过。

## 任务 2：提取并验证浙江原版资源

**来源：**

- `../浙江游戏大厅/原版可编辑源码/热更新工程/res/cocosStudio/hall/Image/lobbyNew/_Plist.png`
- `../浙江游戏大厅/原版可编辑源码/热更新工程/res/cocosStudio/hall/Image/lobbyNew/_Plist.plist`
- `../浙江游戏大厅/原版可编辑源码/热更新工程/res/animation/Lobby/Base/NewGoldHall/StoreBtn/zzb_jbdt_sc.png`
- `../浙江游戏大厅/原版可编辑源码/热更新工程/res/animation/Lobby/Base/NewGoldHall/StoreBtn/zzb_jbdt_sc.atlas`
- `../浙江游戏大厅/原版可编辑源码/热更新工程/res/cocosStudio/Common/Font/fangzhengcuyuan.TTF`

**目标文件：**

- `app/src/main/res/drawable-nodpi/zhejiang_lobby_bottom_bg.png`
- `app/src/main/res/drawable-nodpi/zhejiang_lobby_bottom_divider.png`
- `app/src/main/res/drawable-nodpi/zhejiang_lobby_red_point.png`
- `app/src/main/res/drawable-nodpi/zhejiang_lobby_more_bg.png`
- `app/src/main/res/drawable-nodpi/zhejiang_lobby_new_badge.png`
- `app/src/main/res/drawable-nodpi/zhejiang_lobby_store.png`
- `app/src/main/res/font/fangzhengcuyuan.ttf`

### 2.1 提取图集子图

严格使用 plist 已验证的矩形和旋转标记提取：

- `Img_dl`：`(1, 1, 150, 95)`，不旋转。
- `Img_fgx`：图集帧 `(486, 1, 1, 42)` 且 `rotated=true`，按源尺寸 `1×42` 恢复方向。
- `Img_redpoint`：`(435, 1, 33, 33)`。
- `Img_tip_di`：`(153, 1, 145, 87)`。
- `Img_x`：`(370, 1, 63, 47)`。

### 2.2 组合商城静态图标

根据 Spine atlas 中的区域名和商城节点纹理，将购物车和“商城”牌组合成透明 PNG。只使用浙江商城纹理，不混入现有闲逸素材。

### 2.3 复制字体并检查资源

复制原版 TTF 到 Android `res/font`，然后使用 `file` 和图像尺寸检查确认 PNG/TTF 可读取、PNG 保留 alpha。

## 任务 3：替换首页底栏渲染

**文件：**

- 修改：`app/src/main/java/com/huaque/ui/MainActivity.java`

### 3.1 删除旧页面引用

从 `showLobbyPage()` 移除：

- `lobby_bottom_controls`
- `lobby_quick_start`
- `addLobbyMorePopup(root)`
- 旧快速开始点击区
- 旧商城点击区
- 旧更多点击区

不删除旧资源文件，避免无关资源清理。

### 3.2 添加浙江底栏

新增局部辅助方法：

- `addZhejiangLobbyBottomBar(BoxRoot root)`
- `addZhejiangLobbyBottomItem(...)`
- `addZhejiangLobbyMoreMenu(BoxRoot root)`
- `setZhejiangLobbyMoreMenuVisible(boolean visible)`

渲染内容：

- 九宫拉伸深棕底板。
- 商城静态图标。
- 七个文字入口。
- 七条分隔线。
- 独立透明点击层和中文内容描述。
- 默认隐藏的更多菜单。

文字使用 `R.font.fangzhengcuyuan`、`#E8C8B5`、原版 40 虚拟像素基准，并关闭 Android 默认全大写/额外内边距等无关样式。

### 3.3 绑定原型行为

- 商城、装扮、战绩、活动、分享、背包、邮件：关闭更多菜单后显示同名 toast。
- 更多：切换更多菜单。
- 更多二级入口依次为计分助手、公众号、浙江新闻、绑定手机、设置、规则、健康须知、公告；点击后关闭菜单并显示同名 toast。
- 所有入口具有内容描述。

### 3.4 编译检查

运行目标单元测试，修复编译或资源引用问题。

## 任务 4：回归测试和构建

### 4.1 运行全部 Android 单元测试

```bash
/Users/mosc/.gradle/wrapper/dists/gradle-8.13-bin/5xuhj0ry160q40clulazy9h7d/gradle-8.13/bin/gradle :app:testDebugUnitTest
```

预期：全部通过。

### 4.2 构建宿主 APK

```bash
/Users/mosc/.gradle/wrapper/dists/gradle-8.13-bin/5xuhj0ry160q40clulazy9h7d/gradle-8.13/bin/gradle :app:assembleDebug
```

预期：构建成功并生成 Debug APK。

## 任务 5：模拟器视觉和交互验收

### 5.1 安装并进入首页

将新 APK 安装到可用模拟器，按现有登录路径进入首页。

### 5.2 截图检查

检查：

- 闲逸旧底栏和快速开始完全消失。
- 浙江底栏位于 `(80, 945, 1300, 95)` 对应区域。
- 八入口顺序、字体、颜色、圆角底板、分隔线正确。
- 商城图标透明且未裁切。
- 更多菜单尖角指向更多入口。
- 游戏卡片和好友面板仍可用。

### 5.3 交互检查

依次点击八个主入口；确认普通入口显示正确提示，“更多”可开关，二级入口点击后菜单关闭。再次点击台州麻将确认原游戏进入逻辑未被底栏遮挡。

## 最终验收命令

```bash
/Users/mosc/.gradle/wrapper/dists/gradle-8.13-bin/5xuhj0ry160q40clulazy9h7d/gradle-8.13/bin/gradle :app:testDebugUnitTest
/Users/mosc/.gradle/wrapper/dists/gradle-8.13-bin/5xuhj0ry160q40clulazy9h7d/gradle-8.13/bin/gradle :app:assembleDebug
```

最终交付说明必须列出：修改文件、资源来源、测试结果、APK 路径和视觉验收结果；若模拟器登录或外部状态阻止视觉检查，需明确说明已完成到哪一步，不得将未验证内容宣称为通过。
