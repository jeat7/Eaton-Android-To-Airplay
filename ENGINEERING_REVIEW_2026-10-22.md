# Eaton AirLink — engineering and product review
**Reviewed:** 22 October 2026  
**Review base:** `feature/airlink-single-app-apk` (the debug APK used for the user test); proposed TV Apps work: `feature/airlink-independent-tv-apps`.  
**Scope:** Android Java, manifest, Gradle/CI, AirPlay sender integration, Sunshine, DisplayLink, Shizuku, Tools and network remote. Static analysis plus GitHub Actions build/tests when completed. **Not** a penetration test, DRM bypass assessment or real-device Sky Glass acceptance test.

## Executive assessment
AirLink already includes a working AirPlay sender, pairing/reconnect, a MediaCodec video encoder, Sunshine/Moonlight, DisplayLink, a trusted virtual-display path through Shizuku, remote-input tooling, and a substantial guided UI. Its biggest gap for the user's use case is the distinction between Android *task capture* and *independent app rendering*: sharing one task can freeze when that task loses visibility. A true Android secondary-display activity is the right experiment, but some manufacturers and applications restrict it. TV Apps mode is being built directly into AirPlay; the companion Extend app is not necessary for this basic activity-launch workflow.

### Mode definitions
| Mode | Source | Phone usable simultaneously? | Dependencies and limits |
|---|---|---|---|
| Mirror screen | Android default display via MediaProjection | Phone screen duplicated | Android consent; shares phone notifications |
| Single app capture | Android task/window via MediaProjection | Other apps hidden; captured app may pause/freeze | Android 14 QPR2+; source task remains on phone |
| TV Apps (new, experimental) | Independent, trusted Android virtual display with own-content activity | Designed to allow unrelated phone browsing | Shizuku + device multi-display support; app may refuse second display, DRM or background execution |
| Sunshine / DisplayLink | Their existing virtual-display and encoding paths | Depends on their settings | Keep behaviour regression-tested |

## High priority findings

1. **P0 — Playback-on-TV while phone is used:** the Android task-capture path is intentionally tied to app lifecycle, and the user observed frozen video. The old Extend button required navigating a separate application, while the Shizuku-backed own-content display already existed in `job/CreateVirtualDisplay.java`. New TV Apps mode is intended to launch selected apps on the independent virtual display via `ActivityOptions.setLaunchDisplayId`; blocked apps are reported. No promise is made that all apps or protected videos will work.

2. **P1 — Package-name drift in privileged permission logic:** `State.java` still issues `appops set io.github.jqssun.displaymirror PROJECT_MEDIA allow` and `SYSTEM_ALERT_WINDOW allow`, but the current package is `io.github.jeat7.eatonairlink` in `app/build.gradle`. These commands target the wrong app. Beyond fixing the identifier, **do not silently grant broad screen-capture and overlay permissions**; replace this behaviour with explicit permission flows and precise user-facing explanations. Audit Shizuku UserService methods and all cross-package IPC before release.

3. **P1 — Connection state and lifecycle:** `AirPlayService.onConnected` sets `connected=true` when the network handshake completes, before Android projection consent and before the encoder/virtual display starts. UI can therefore say “connected” with nothing streaming. Model handshake, capture consent, display created, encoder running, interrupted and disconnected separately; improve decline/retry/timeout handling and stop/cleanup race tests.

4. **P1 — Phone audio isolation:** unscoped `CaptureAudio.startPlaybackCapture` records every capture-eligible app and the optional Shizuku `REMOTE_SUBMIX` path can capture/redirect all audio. In new TV Apps mode the selected application's UID is filtered with `AudioPlaybackCaptureConfiguration.Builder.addMatchingUid()` and global submix bypassed. Test shared UID, split processes, multiple audio players and stream switches. Audio protected by Android capture policies will still be silent; don't work around those policies.

5. **P1 — Receiver discovery thread safety:** `AirPlayService.discover` calls `devices.clear()` and populates an ordinary `ArrayList` from the JmDNS listener thread, while UI iterates it on the main thread. This can race or throw `ConcurrentModificationException`. Put all device-list updates on the main thread (or synchronize snapshots); add stable receiver identity, deduplication and better network-interface selection.

6. **P1 — Display and capture teardown:** audit `AirPlayEncoder.start/stop`, `ProjectionService.onDestroy`, `State.mediaProjectionInUse`, `CreateVirtualDisplay.changeAspectRatio` and delayed placeholder startup. Network disconnect, permission revoke, Android process kill, screen lock, Wi-Fi handover and repeated reconnect can race. Ensure virtual display, encoder, MediaProjection and audio release in a known order, and never leave forced phone display dimensions after crashes.

## Medium priority findings

7. **P2 — UI complexity and old instructions:** AirPlay currently exposes a network receiver list, manual IP, single-app sharing, legacy AirPlay mode, external-display management, picture, audio and advanced settings. Provide a top-level segmented choice **Mirror / Share an app / TV Apps**, each with a 2-line explanation, guided prerequisites, and an always-visible streaming status. Mark the old Extend path “advanced” instead of presenting it as the only app launcher. Explain that TV Apps needs Shizuku.

8. **P2 — Background and battery behaviour:** `MainActivity.onCreate` adds `FLAG_KEEP_SCREEN_ON` for all app activity time. That keeps the phone awake outside active casting. Move wake policies into active sessions, respect battery saver, expose a “keep casting with screen off” setting where supported, and measure device/thermal impact.

9. **P2 — Network transport/security:** manifest sets `android:usesCleartextTraffic="true"` for the entire application; Roku HTTP and LG/Samsung WebSocket remotes are experimental. Restrict plaintext to the required local-network protocols where possible, keep permissions scoped, maintain pairing/credential privacy, and do not imply Sky Glass is universally remote-controllable. Validate URLs from network-discovered data before any request. Review installed Git submodules, Go, OpenSSL and native dependencies.

10. **P2 — Build and app ownership:** `main` still contains the upstream Mirror baseline while Eaton-branded work sits in several open draft PRs. Establish a canonical integration branch/release policy to avoid shipping stale builds and confusing users. Keep an annotated version/build number in the app and persist a **stable signing key** in a protected CI secret; ephemeral debug-key builds frequently cannot install over one another. Make a release test matrix a merge gate.

11. **P2 — Diagnostics are not actual stream-health telemetry:** `AirLinkHealthFragment` shows Android Wi-Fi signal/connection but not sent frames, dropped frames, streaming RTT or audio underruns. Add encoder frame counters, stalls, virtual-display state, Wi-Fi transport, last reconnect reason and a redacted export report. Avoid promising measured packet loss without tracking it.

12. **P2 — Media showcase is phone-centric:** `AirLinkShowcaseFragment` plays videos in a regular phone `VideoView`, so it is bound to the phone rather than the new display. A purpose-built, AirLink-owned TV media player activity launched on the virtual display could make **local, non-DRM videos** a predictable first-class use case. Use a SAF document picker, URI grants, pause/resume and a phone-side media controller.

13. **P2 — Accessibility, navigation and performance:** the app mixes legacy dense settings with many programmatically built Tool pages. Check content descriptions, large font scaling, 60 Hz animations, TalkBack, empty-state focus, orientation changes, stable layout and small touch targets. Provide a simple “what is on TV now?” indicator and stop-casting action.

14. **P2 — Audio level/volume safety:** existing native sender has a patch to avoid unsolicited AirPlay `volume: 0` resets, but verify on Sky Glass. Show separately “TV speaker volume (TV remote)” vs “Stream gain (phone)” and avoid blasting sound on reconnect; keep muted defaults for unknown receivers optional.

## Security and privacy checklist
- Screen capture is explicitly consented to by Android; do not bypass permission prompts or auto-grant via appops. Review existing `State.java` elevated helper commands.
- TV Apps **must never** silently downgrade to whole-phone mirroring if Shizuku disappears. Treat missing permission/display failure as a visible session error.
- Capture audio only from the chosen app in TV Apps; don't route global submix. The Android system may still require per-app opt-in and will refuse protected streams.
- Minimise personal data in logs: receiver IPs, package names, pairing status, credentials and stack traces. No credentials in analytics, exported logs or PRs.
- Confirm the app's signature-protected `TouchscreenActivity`/`MirrorTouchscreenProvider` exports and any inbound intents are not broadened unintentionally.
- Installable development builds are unsigned-for-release / debug-signed; never treat them as production-grade security or automatic-update-ready.

## Proposed roadmap
**Milestone 1 — Functional dual-screen video (current):** built-in AirPlay TV Apps switch and chooser; Shizuku setup assistance; independent own-content display; safe no-mirror fallback; selected-UID audio; an Android 14+ single-app option retained. Test at least a local video player, Chrome and an unrestricted media app on actual phone + Sky Glass.

**Milestone 2 — Reliable TV playback:** dedicated TV media player for local videos, playback control buttons on phone, per-app audio switching without drops, easy stop/relaunch controls, app chooser icons and favourites, resume after receiver drops.

**Milestone 3 — Stream quality:** adaptive bit rate/frame rate and aspect ratio; 720p/1080p presets; FPS, dropped-frame and audio-underflow telemetry; robust reconnect with permission handling; network transition safeguards; power/thermal profiles.

**Milestone 4 — Production safety:** background lifecycle tests, scoped network security, stable release signing, user-consent audit, app-store permission declarations, CI test suite and automated release notes.

**Milestone 5 — Advanced compatibility:** optional touchpad or virtual remote input on TV display, multi-display app focus tests, accessibility, Chromecast/Google Cast integrations where official APIs allow them, and AirPlay receiver compatibility matrix.

## Hardware acceptance checks (must pass before describing TV Apps as reliable)
- Shizuku stopped, denied, killed and restarted: TV Apps refuses to mirror and displays an actionable error.
- Before connection choose app A, connect, verify app A starts on Sky Glass; launch app B on phone; A video continues and B is not shown.
- Switch TV app while connected; verify previous stream audio stops; only target app audio reaches receiver.
- Disconnect while playing; ensure audio encoder, virtual display, placeholder and MediaProjection release; repeat 10+ times.
- Try a local non-DRM MP4, VLC, browser and several commercial subscription apps. Record protected-video limitations without attempting bypass.
- Check lock screen, home gesture, portrait/landscape, incoming notification, audio focus and permission revoke.
- Verify debug APK upgrades from the previous build; if signature mismatch, save settings before uninstalling. Verify app version and signing fingerprint.

## Limits of this review
This is a source-level assessment of the linked development branch. No live phone/TV instrumentation, static-analyser suite, dynamic security scanner, codec interoperability suite or dependency vulnerability report was completed. Assertions about compatibility are risks to test, not guaranteed product defects.
