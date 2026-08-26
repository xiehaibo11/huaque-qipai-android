# Startup Agreement Dialog Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the PSD-matched, version-aware startup agreement dialog to the Nanbei Android login flow.

**Architecture:** Keep the existing programmatic `BoxRoot` layout. Extend the existing agreement config/model for version handling, persist the accepted version with SharedPreferences, and add one modal overlay assembled from PSD layers plus native clickable text.

**Tech Stack:** Android Java 17, SharedPreferences, existing `BoxRoot`, JUnit 4, ImageMagick asset export, Gradle 8.13.

## Global Constraints

- Use only the operator name “南北娱乐”.
- Show only on first install, cleared data, or agreement version change.
- Agreement links must use the existing trusted HTTPS validation and system browser.
- Refusal requires a second confirmation and confirmed refusal exits the app.
- Do not alter the existing splash, loading, authentication, or lobby flows beyond the agreement gate.

---

### Task 1: Version-aware consent model

**Files:**
- Modify: `app/src/test/java/com/huaque/ui/LoginAgreementModelTest.java`
- Modify: `app/src/test/java/com/huaque/ui/LoginAgreementConfigTest.java`
- Modify: `app/src/main/java/com/huaque/ui/LoginAgreementModel.java`
- Modify: `app/src/main/java/com/huaque/ui/LoginAgreementConfig.java`

**Interfaces:**
- Produces: `LoginAgreementModel.requiresPrompt(String, String)` and `LoginAgreementConfig.version()`.

- [ ] Write failing tests for first install, matching version, updated version, default unchecked state, parsed remote version, and fallback version.
- [ ] Run the focused tests and confirm failures are caused by missing behavior.
- [ ] Implement the minimum model/config changes.
- [ ] Run the focused tests and confirm they pass.

### Task 2: PSD assets and persistent consent

**Files:**
- Create: `app/src/main/res/drawable-nodpi/agreement_dialog_panel.png`
- Create: `app/src/main/res/drawable-nodpi/agreement_dialog_header.png`
- Create: `app/src/main/res/drawable-nodpi/agreement_dialog_title.png`
- Create: `app/src/main/res/drawable-nodpi/agreement_dialog_reject.png`
- Create: `app/src/main/res/drawable-nodpi/agreement_dialog_accept.png`
- Create: `app/src/main/java/com/huaque/ui/LoginAgreementConsentStore.java`

**Interfaces:**
- Produces: `acceptedVersion()` and `accept(String)`.

- [ ] Export exact PSD layers with their original alpha and dimensions.
- [ ] Add the narrowly scoped SharedPreferences store.
- [ ] Verify every exported image dimension and alpha channel.

### Task 3: Startup modal integration

**Files:**
- Modify: `app/src/main/java/com/huaque/ui/MainActivity.java`

**Interfaces:**
- Consumes: version-aware config/model, consent store, five PSD drawables.

- [ ] Derive the initial login checkbox state from persisted consent.
- [ ] Add the non-dismissible PSD modal after the login root is attached.
- [ ] Wire browser links, accept persistence, reject confirmation, and back-button blocking.
- [ ] Re-evaluate consent when remote agreement configuration finishes loading.

### Task 4: Verification

**Files:**
- Output: `app/build/outputs/apk/debug/app-debug.apk`
- Output: simulator screenshots under `/tmp`.

- [ ] Run all Android unit tests.
- [ ] Build the APK with `AUTH_BASE_URL=https://api.nanbeiyule.com` and `LEGAL_BASE_URL=https://www.nanbeiyule.com`.
- [ ] Install on the emulator after clearing app data and capture the startup dialog.
- [ ] Verify agreement links, accept persistence, restart behavior, and first-run reset.
- [ ] Compare the screenshot framing and visible content against the PSD.
