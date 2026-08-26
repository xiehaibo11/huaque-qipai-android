# Zhejiang Lobby Store and Quick Start Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 恢复浙江原版完整商城图标和右下角“快速开始 / 金币场-台州麻将”入口。

**Architecture:** 继续使用纯 Java `LobbyBottomBarModel` 保存可测试的虚拟坐标与文案，`MainActivity` 通过现有 `BoxRoot` 绘制。商城与快速开始都从浙江原版 Spine/Cocos 图集组合为透明静态 PNG，不引入新运行时。

**Tech Stack:** Java、Android View、JUnit 4、ImageMagick、Gradle 8.13。

## Global Constraints

- 快速开始虚拟坐标必须为 `(1436, 890, 430, 180)`。
- 副标题必须为“金币场-台州麻将”。
- 点击必须复用 `openTaizhouGoldGame()`。
- 不引入新依赖，不修改首页其他区域。
- 当前 `android` 目录不是 Git 工作树，本计划不执行提交步骤。

---

### Task 1: 锁定快速开始合同

**Files:**
- Modify: `app/src/test/java/com/huaque/ui/LobbyBottomBarModelTest.java`
- Modify: `app/src/main/java/com/huaque/ui/LobbyBottomBarModel.java`

**Interfaces:**
- Produces: `static Rect quickStartBounds()`
- Produces: `static String quickStartSubtitle()`

- [ ] **Step 1: Write the failing tests**

```java
@Test
public void quickStartUsesOriginalZhejiangBounds() {
    assertEquals(new LobbyBottomBarModel.Rect(1436, 890, 430, 180),
            LobbyBottomBarModel.quickStartBounds());
}

@Test
public void quickStartNamesTaizhouGoldRoom() {
    assertEquals("金币场-台州麻将", LobbyBottomBarModel.quickStartSubtitle());
}
```

- [ ] **Step 2: Verify RED**

Run:

```bash
/Users/mosc/.gradle/wrapper/dists/gradle-8.13-bin/5xuhj0ry160q40clulazy9h7d/gradle-8.13/bin/gradle :app:testDebugUnitTest --tests com.huaque.ui.LobbyBottomBarModelTest
```

Expected: test compilation fails because `quickStartBounds()` and `quickStartSubtitle()` do not exist.

- [ ] **Step 3: Implement the minimal model values**

```java
private static final Rect QUICK_START_BOUNDS = new Rect(1436, 890, 430, 180);
private static final String QUICK_START_SUBTITLE = "金币场-台州麻将";

static Rect quickStartBounds() { return QUICK_START_BOUNDS; }
static String quickStartSubtitle() { return QUICK_START_SUBTITLE; }
```

- [ ] **Step 4: Verify GREEN**

Run the Task 1 command again. Expected: all `LobbyBottomBarModelTest` tests pass.

### Task 2: 重组浙江原版静态素材

**Files:**
- Replace: `app/src/main/res/drawable-nodpi/zhejiang_lobby_store.png`
- Create: `app/src/main/res/drawable-nodpi/zhejiang_lobby_quick_start.png`

**Interfaces:**
- Consumes: Zhejiang `StoreBtn/zzb_jbdt_sc.*`, `QuickStart/zzb_jbdt_ksks.*`
- Produces: transparent PNG drawables referenced by `MainActivity`

- [ ] **Step 1: Extract all visible store regions**

Use atlas rectangles for `cc`, `jb`, `jb1`, `jb2`, `jb3`, `jb4`, and `zi`; restore rotated regions according to the atlas and preserve alpha.

- [ ] **Step 2: Compose the store default frame**

Place the cart, five coin layers, and title into one transparent canvas using the Spine setup-pose bone transforms. Compare the output against `/Users/mosc/Downloads/逆向/image.png`; the cart and coins must fill the first slot and extend above the bar.

- [ ] **Step 3: Compose the quick-start default frame**

Extract the `btn` and `ksks` regions from `zzb_jbdt_ksks.png`, restore atlas rotation, and place them on a transparent `430×150` canvas using the CSD/Spine default placement. The subtitle remains a native text view so it can use the exact approved copy.

- [ ] **Step 4: Inspect both PNGs**

Run `file` to confirm readable RGBA PNGs and visually inspect them before changing Java code.

### Task 3: 恢复快速开始渲染与点击

**Files:**
- Modify: `app/src/main/java/com/huaque/ui/MainActivity.java`

**Interfaces:**
- Consumes: `LobbyBottomBarModel.quickStartBounds()` and `quickStartSubtitle()`
- Consumes: `R.drawable.zhejiang_lobby_store`, `R.drawable.zhejiang_lobby_quick_start`
- Reuses: `openTaizhouGoldGame()`

- [ ] **Step 1: Replace the store render bounds**

Render the completed transparent store asset at the first slot with enough height to preserve the upward overflow. Bind the full visual bounds to the existing store toast action.

- [ ] **Step 2: Add the Zhejiang quick-start view**

Add the transparent quick-start drawable at `(1436, 890, 430, 150)` and a centered `FittedTextView` subtitle in the lower part of the `430×180` container, using `zhejiangLobbyTypeface()` and the original brown text color.

- [ ] **Step 3: Bind the complete quick-start hit area**

Add one focusable/clickable transparent hit view over `LobbyBottomBarModel.quickStartBounds()`. On click, close the more menu and invoke `openTaizhouGoldGame()`.

- [ ] **Step 4: Compile-check with the model test**

Run the Task 1 test command. Expected: PASS with no resource errors.

### Task 4: 回归、构建与模拟器验收

**Files:**
- Verify: `app/build/outputs/apk/debug/app-debug.apk`
- Capture: `/tmp/zhejiang-lobby-final.png`
- Capture: `/tmp/zhejiang-lobby-final-more.png`

- [ ] **Step 1: Run all app unit tests and build**

```bash
/Users/mosc/.gradle/wrapper/dists/gradle-8.13-bin/5xuhj0ry160q40clulazy9h7d/gradle-8.13/bin/gradle :app:testDebugUnitTest :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Install on emulator-5554**

```bash
adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5554 shell am force-stop com.huaque.ui
adb -s emulator-5554 shell am start -n com.huaque.ui/org.cocos2dx.lua.AppActivity
```

- [ ] **Step 3: Capture and inspect the lobby**

Confirm the store is complete, quick start appears at the original coordinates, the subtitle reads `金币场-台州麻将`, and no element overlaps the eight-item bottom bar.

- [ ] **Step 4: Verify interactions**

Open and close `更多`; click quick start and confirm the current Taizhou entry flow opens. If an external login/backend condition blocks entry, report that condition instead of claiming the interaction passed.
