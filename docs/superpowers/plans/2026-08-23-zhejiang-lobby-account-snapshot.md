# Zhejiang Lobby Account Snapshot Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the baked Zhejiang lobby identity and wallet values with authenticated `/api/v1/home` data and keep lobby, shop, and personal center consistent.

**Architecture:** Reuse the gameplay module's validated `GameHomeState` and `GameHomeApiClient` instead of introducing a second parser. Render only the lobby header through a focused view that follows the original Zhejiang header geometry and independent artwork; the existing lobby composition remains unchanged.

**Tech Stack:** Android Java 17, `HttpURLConnection`, `org.json`, Android Canvas, JUnit 4, Gradle.

## Global Constraints

- The lobby must not render the baked account name, player ID, or wallet values from `lobby_top_controls.png`.
- Player identity and wallet values must come from authenticated `GET /api/v1/home`.
- Preserve the Zhejiang lobby order: 欢乐豆、钻石、房卡.
- Do not synthesize screenshot values on request failure.
- Modify only lobby account-data rendering and the shared visible amount formatting needed for cross-screen consistency.

---

### Task 1: Public authenticated home snapshot boundary

**Files:**
- Modify: `gameplay/src/main/java/com/nanbeiyule/game/GameHomeState.java`
- Modify: `gameplay/src/main/java/com/nanbeiyule/game/GameHomeApiClient.java`
- Test: `gameplay/src/test/java/com/nanbeiyule/game/GameHomeStateTest.java`

**Interfaces:**
- Consumes: authenticated JSON from `GET /api/v1/home`.
- Produces: public `GameHomeState.fromJson(JSONObject)` and public `GameHomeApiClient.loadHome(String, Callback)`.

- [ ] Write a parsing test whose full response fixture asserts `displayName`, `publicPlayerId`, `roomCards`, `coins`, and `diamonds`.
- [ ] Run the focused test and confirm it fails because the app module cannot yet use the package-private boundary.
- [ ] Expose only the existing state and client API required by the app module.
- [ ] Run the focused test and gameplay unit tests.

### Task 2: Zhejiang lobby header presentation

**Files:**
- Create: `gameplay/src/main/java/com/nanbeiyule/game/ZhejiangLobbyAmountFormatter.java`
- Create: `gameplay/src/main/java/com/nanbeiyule/game/ZhejiangLobbyHeaderView.java`
- Test: `gameplay/src/test/java/com/nanbeiyule/game/ZhejiangLobbyAmountFormatterTest.java`

**Interfaces:**
- Consumes: `GameHomeState` and existing original Zhejiang header artwork.
- Produces: `ZhejiangLobbyHeaderView(Context, GameHomeState)` with dynamic identity and wallet rendering.

- [ ] Write literal formatting cases for `876900 -> 87.69万`, `1000300 -> 100.03万`, `20000 -> 2万`, and `6666 -> 6666`.
- [ ] Run the focused test and confirm the missing formatter failure.
- [ ] Implement minimal amount formatting.
- [ ] Implement the header view using original asset ordering and geometry, with no fallback account values.
- [ ] Run focused and module tests.

### Task 3: Lobby request wiring and MuMu verification

**Files:**
- Modify: `app/src/main/java/com/huaque/ui/MainActivity.java`

**Interfaces:**
- Consumes: authenticated access token and `GameHomeApiClient`.
- Produces: a live header added after a successful home response; no static `lobby_top_controls` layer.

- [ ] Remove the static top-controls layer from `showLobbyPage()`.
- [ ] Request `/api/v1/home` with the restored authenticated token and attach `ZhejiangLobbyHeaderView` only on success.
- [ ] Shut down the home client on page replacement and activity destruction.
- [ ] Run all app and gameplay unit tests.
- [ ] Build the debug APK, install it on MuMu, and capture lobby, shop, and personal-center screenshots.
- [ ] Compare all three screens against the same account identity and wallet snapshot.
