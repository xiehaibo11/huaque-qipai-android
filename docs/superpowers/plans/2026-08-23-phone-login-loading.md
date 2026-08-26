# 手机登录加载遮罩 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 手机验证码提交后显示复用现有光环动画且文案为“正在登录”的顶层加载遮罩。

**Architecture:** Lua 继续作为登录状态唯一来源；Java 根据 `VERIFYING` 和 `authenticated` 渲染遮罩。`XianyiLoadingView` 增加可配置文案，新的纯 Java 状态模型负责可见性和循环进度，便于单元测试。

**Tech Stack:** Android Java、LuaJ、JUnit 4、现有自绘 Canvas 加载组件。

## Global Constraints

- 不修改后端、短信接口和验证码协议。
- 不新增图片、字体或第三方依赖。
- 登录请求仍只连接 `https://api.nanbeiyule.com`。
- 启动加载文案保持“加载中”，登录加载文案必须为“正在登录”。

---

### Task 1: 登录加载状态模型

**Files:**
- Create: `app/src/main/java/com/huaque/ui/LoginLoadingModel.java`
- Create: `app/src/test/java/com/huaque/ui/LoginLoadingModelTest.java`

**Interfaces:**
- Produces: `LoginLoadingModel.isVisible(String phase, boolean authenticated)` 与 `nextProgress(int progress)`。

- [ ] **Step 1: 写失败测试**

覆盖 `VERIFYING=true`、`AUTHENTICATED+authenticated=true`、`ERROR=false`，以及进度 `99 -> 0`。

- [ ] **Step 2: 运行测试确认 RED**

Run: `gradle :app:testDebugUnitTest --tests com.huaque.ui.LoginLoadingModelTest`
Expected: FAIL，`LoginLoadingModel` 尚不存在。

- [ ] **Step 3: 实现最小模型并运行 GREEN**

仅实现状态判断、`LOGIN_LABEL = "正在登录"` 和 0-99 循环进度。

### Task 2: 复用加载组件并绑定登录状态

**Files:**
- Modify: `app/src/main/java/com/huaque/ui/MainActivity.java`
- Test: `app/src/test/java/com/huaque/ui/LoginLoadingModelTest.java`

**Interfaces:**
- Consumes: `LoginLoadingModel.isVisible(...)`、`LOGIN_LABEL`、`nextProgress(...)`。
- Produces: `showLoginLoadingOverlay()`、`removeLoginLoadingOverlay()`。

- [ ] **Step 1: 让 `XianyiLoadingView` 接受显示文案**

启动页传入“加载中”，登录遮罩传入 `LoginLoadingModel.LOGIN_LABEL`。

- [ ] **Step 2: 在 `renderAuthState` 中绑定加载层生命周期**

`VERIFYING` 显示；失败移除；成功保持到 `showLobbyPage`；弹窗关闭和 Activity 销毁时清理回调。

- [ ] **Step 3: 运行聚焦、全量测试与构建**

Run: `gradle :app:testDebugUnitTest :app:assembleDebug`
Expected: BUILD SUCCESSFUL。

### Task 3: 模拟器真实流程验证

**Files:**
- Verify: `app/build/outputs/apk/debug/app-debug.apk`

- [ ] **Step 1: 安装 APK 并核对生产域名**

从设备拉取安装 APK，确认 SHA-256 与本地产物一致，且 `AUTH_BASE_URL=https://api.nanbeiyule.com`。

- [ ] **Step 2: 输入真实验证码并点击登录**

截图确认半透明遮罩、光环动画和“正在登录”文案显示完整且不与登录弹窗内容重叠。

- [ ] **Step 3: 验证完成态**

失败时遮罩消失并允许重试；成功时进入大厅且不存在残留加载层。

