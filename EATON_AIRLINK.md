# Eaton AirLink v0.1.0

Eaton AirLink builds on the GPL-3.0-licensed [Mirror](https://github.com/jqssun/android-display-mirror) project.

## Preserved
AirPlay 1/2, Moonlight/Sunshine streaming and control, DisplayLink USB, Shizuku optional tools, virtual display options, logs and settings. **No Stremio launcher** is included.

The Android application ID changes to `io.github.jeat7.eatonairlink` to allow side-by-side install. Its Java namespace stays `io.github.jqssun.displaymirror` to preserve native bindings. Verify integrations relying on the original package ID, especially the separate Extend companion app.

## Eaton-family appearance
Eaton Vital was inspected in read-only mode only; no files in the fitness repository were edited. AirLink uses its navy background #07111F, panel #0D1A2B, elevated panel #12243A, border #213853, foreground #EEF6FB, muted #91A8BC, teal #2ED3B7, and light teal #7AF0DA. The adaptive launcher icon uses Eaton's pulse motif.

## AirPlay audio level
The new **Follow phone media volume** option is enabled by default. It attenuates 16-bit audio frames sent over AirPlay based on the phone's media volume. It does NOT change Sky Glass's actual TV speaker volume. If Android has already scaled playback capture, the sound may be reduced twice; disable the option if that happens. TV volume protocol control still needs receiver-specific investigation and testing.

## Build
The new GitHub Actions **Eaton AirLink - Debug APK** workflow assembles the upstream native engine, then generates a debug-signed APK as a build artifact. Successful CI and on-device testing are required before considering this production-ready. Release distribution requires a unique signing key.

## GPL notices
Continue preserving the original GPLv3 copyright, licence and corresponding source when sharing builds. Eaton AirLink is unofficial and not affiliated with Apple, Sky or the upstream developers.
