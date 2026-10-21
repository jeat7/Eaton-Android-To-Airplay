# Eaton AirLink

**v1.0 beta development:** [feature guide and compatibility matrix](EATON_AIRLINK_V1.md). The v0.2 README below remains valid for the original mirroring tools.

**Simple Android screen mirroring and external displays.**

Eaton AirLink is a custom Android mirroring application in the Eaton family. It uses the same **navy / teal** design language as Eaton Vital, but remains a completely separate app and codebase.

## Single-app AirPlay sharing (experimental)

Open **AirPlay > Choose an app to share**, turn it on, then reconnect to the TV. On supported Android 14+ devices, the system dialog lets you pick one app or the entire screen. Sharing one app hides other phone app screens, although Android can pause the shared app in the background, and audio may include other phone apps. The existing Shizuku + Extend display path is for running an app separately on the TV.

## Features

- **Mirror to TV (AirPlay):** find and connect to compatible TVs and AirPlay receivers, with quick reconnect to your last successfully paired receiver
- **Stream to Moonlight:** use the integrated Sunshine host with compatible Moonlight clients
- **USB display:** supported DisplayLink devices
- **Phone remote:** touchscreen control of compatible active virtual displays
- **Guided setup:** optional first-run onboarding, permission explanations and troubleshooting guidance
- **Advanced tools:** complete original Mirror display settings, Shizuku, companion Extend integration (subject to app-ID compatibility), logs and diagnostics

No app-specific Stremio shortcut is included: mirror any application permitted by Android screen-capture rules.

## Getting started

1. Download a completed **Eaton AirLink - Debug APK** workflow artifact from [Actions](https://github.com/jeat7/Eaton-Android-To-Airplay/actions/workflows/eaton-airlink-debug.yml).
2. Install the APK on Android 8 or newer. The application ID is `io.github.jeat7.eatonairlink` and can coexist with the upstream Mirror app.
3. Follow the first-run setup guide or skip it. Shizuku is not required for basic AirPlay mirroring.
4. For a TV, connect both devices to the same network, select **Connect → Mirror to TV**, scan for receivers and approve the Android screen-capture prompt.

### Audio / Sky Glass volume

The included AirPlay library previously sent `SET_PARAMETER volume: 0`, which can set some TVs to maximum volume. Eaton AirLink's build removes this unsolicited request on the disposable native-source build copy; the TV should retain its own volume level, although actual Sky Glass testing is needed.

The optional **Follow phone media volume** option only changes the audio samples that are transmitted and does not control the TV's speaker volume. Some Android apps prohibit audio or video capture.

## Development

This is a native Android/Gradle project including Go and C++ dependencies, not a standard Capacitor build. The original AirPlay engine is retained.

```bash
git submodule update --init --recursive
./build.sh
./gradlew assembleDebug
```

The GitHub Actions debug workflow uses Java 21, Go and Android SDK/NDK and provides an APK artifact after a successful run.

Review [the draft pull request](https://github.com/jeat7/Eaton-Android-To-Airplay/pull/1) before merging v0.2 changes into `main`.

## Upstream and licence

Eaton AirLink is based on [jqssun/android-display-mirror](https://github.com/jqssun/android-display-mirror), distributed under **GNU GPL version 3**. Copyright notices, the LICENSE file, and source-sharing obligations remain in force. The product is unaffiliated with Sky or Apple.

The Eaton Vital repository was inspected solely for palette consistency and has not been modified by this project.
