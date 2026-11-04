# LastNap Agent Memory — Migration File (for new app)
# Luna moves the harness to a new app; the new agent should read THIS FILE FIRST.
# Written: 2026-11-04. Everything below is the complete working state.

## WHO
- User: **Luna** (do NOT call her Ryan). Casual tone, loves emojis/pookies. NON-NEGOTIABLE: talk to her in ENGLISH ONLY — she does not understand Chinese.
- App: **LastNap** — Luna's private-customized fork of Cocolalilal/LastChat (RikkaHub fork).
- This repo is cloned in Termux at ~/lastnap. The harness connects to Termux via SSH.

## HARD RULES (PINNED — obey always)
1. **ALL Termux work ONLY inside ~/lastnap.** Never cd/list/read/peek outside it over SSH, even if paths are visible. If a task seems to need outside access: STOP and ask Luna first.
2. **English only** in everything Luna reads (chat, commit messages, code comments she sees).

## CONNECTION (Termux bridge)
- SSH: host 127.0.0.1, port 8022, user u0_a433, workspace path /data/data/com.termux/files/home/lastnap
- Auth: KEY ONLY (password sessions dropped constantly). Keys live in ~/lastnap/.harness/: termux_ed25519 AND termux_rsa (PEM RSA-4096 — the harness's SSH client is old and chokes on new OpenSSH key ciphers: "cipher name is not available"). Both public keys are in Termux ~/.ssh/authorized_keys.
- GitHub: deploy key .harness/deploy_ed25519 (push-verified). The harness token auto-auths api.github.com via http_request; token lacks delete_repo scope (leftover repo Cutie-Cookie/LastChat-todelete awaits manual deletion by Luna).

## REPO / RELEASE PIPELINE
- Repo: github.com/Cutie-Cookie/LastChat, default branch **LastChat** (public — GitHub hard-blocks public forks from going private, 422; Luna accepted).
- Push to LastChat ⇒ GitHub Actions ".github/workflows/release.yml" auto-builds `./gradlew :app:assembleStableDebug --no-daemon -Plastchat.abi.splits=false` (~9 min) and attaches **LastNap_1.4.8_debug.apk** to Release "v1.4.8-lastnap". Watch: GET /repos/Cutie-Cookie/LastChat/actions/runs?per_page=1
- Build quirks: MUST use repo's own ./gradlew (runner Gradle too new); dummy app/google-services.json exists (applicationId dev.keepinitkrispy.lastnap + .dev/.debug flavor suffixes). APK is arm64-v8a only, ~210MB, DEBUG-signed (Play Protect may block install — Luna knows).
- Current HEAD at writing: ea21bc6 (build #16 SUCCESS).
- AGPL-3.0, Kotlin+Compose+Koin+Room, minSdk 28 / target 36. AGENTS.md in repo root has the full architecture map (GenerationHandler, transformers, tools, MCP...).

## DONE SO FAR (newest last)
1. Repo forked (full 767-commit history).
2. Launcher icon = Luna's image; app name = LastNap (both flavors); APK renamed LastNap_*.
3. applicationId → dev.keepinitkrispy.lastnap (installs fresh, doesn't touch old LastChat); gs.json matches.
4. Notification small icon = Luna's white kitten PNG (drawable-nodpi/ic_notification.png; old XML deleted). Later density-bucketed (mdpi24→xxxhdpi96) so it renders bigger.
5. Markdown GIFs animate (Coil GIF decoder registered, commit 8109761).
6. Global UI Scale slider 70–120% in Settings > UI Customization (density-based; commit bab8ba0). LAG FIXED: slider drafts during drag, commits on release.
7. arm64-only build (smaller APK, faf696b).
8. Per-provider Proxy: 3rd tab (🔑) in provider detail — toggle, HTTP/SOCKS5 chips, host/port/username/password (optional), Test Connection (GET api.github.com/zen through proxy). Plumbing: ProviderProxy.Socks5 + toUrl/fromUrl; PlatformHttpProxy.type; OkHttp Proxy.Type.SOCKS; all 6 toPlatformProxy() converters. Requests of that provider (incl /models) route via proxy.
9. FAB fixes: Providers pages (Models/Search/TTS) — Import/Settings FAB stacked above Plus FAB in bottom bar (ProvidersSecondaryActionSlot removed); models-page FABs raised 112dp (clear of pill bar + system nav).
10. Proxy tab insets fixed (contentPadding now applied; first card no longer under app bar).
11. **"Model Sees Generated Images"** (commit ea21bc6, build #16 green): DisplaySetting.modelSeesGeneratedImages (default false) + toggle in Settings > Advanced Settings ("Model Sees Generated Images"). generate_image tool is now a factory `imageGenerationTool(modelSupportsVision: Boolean)`; getTools takes modelSupportsVision and ChatService passes model.inputModalities.contains(Modality.IMAGE). Vision model ⇒ image injected via TOOL_RESULT_INJECT_USER_IMAGE_PARTS_KEY (private tool inspection, same as look_at_screen; counted by image limiter). Non-vision ⇒ silent OCR via OcrTransformer.performOcrWithMetadata (only if isOcrConfigured), returned as generated_image_ocr[] in tool result; failures silent.

## KNOWN GOTCHAS (learned the hard way)
- NEVER python-re.sub over multi-line Kotlin blindly — it ate 128 lines of ComfyUI helpers once (restored from git). Always verify braces balance + function-boundary scan after scripted edits.
- Big tool payloads get truncated ~4-5KB in chat echoes — don't echo base64/secrets; move via files.
- Private keys are REDACTED from tool output by the harness security filter — Luna must `cat ~/lastnap/.harness/termux_rsa` herself.
- GitHub raw file CDN caches aggressively — verify file changes via API contents (git truth), not raw.githubusercontent.
- Job logs: /actions/jobs/{id}/logs → 302 → fetch Location with fresh sig; use Range header for tail (failure section).
- objective_finish tool is strict: it wants a read-back (read_file/memory_read of the touched file) AFTER the last modification, else it rejects. Just do the read-back then retry.

## LUNA'S DEVICE
- itel P55, Unisoc T606, arm64, Android 13, 8GB RAM. Debug APKs install fine once Play Protect is off.

## NEXT IDEAS / PENDING
- Full optimization pass when all features Luna wants are in (she said later).
- Luna wants a proper release build someday (needs signing config in CI; debug-signed release possible).
- More inside-app customizations — ask Luna what's next.

## MIGRATION NOTE
- New app: read this file, read ~/lastnap/AGENTS.md, run `git -C ~/lastnap log --oneline -5` and `git -C ~/lastnap status`, then ask Luna what feature is next. Keep the two HARD RULES above sacred.
