# Eaton AirLink v0.2.0

## Delivered
- Eaton family design language, adaptive vector icon, matching navy and teal Material theme
- Card-based Home, Connect, Help and Settings entry points; original advanced screens retained
- Skippable first-run setup wizard with guidance for screen capture, optional audio and overlay permissions
- AirPlay receiver choices as labelled cards, paired-receiver memory and manual reconnect
- Expanded in-app help and diagnostics links
- Build patch preventing AirPlay from unilaterally setting a receiver to full (0 dB) at connection
- Optional outbound PCM attenuation by Android phone media volume (off by default on clean installs)
- Original upstream Mirror features and GPLv3 licence retained, no Stremio-specific button

## Validation
GitHub Actions must complete before distributing the APK. Confirm Sky Glass AirPlay, audio, connection volume retention, and remote features on an actual phone/TV. The original Extend companion may need separate app-ID integration testing. A passing compilation does not prove every native feature works with your exact hardware.

## Safety and source boundaries
The separate Eaton Vital fitness repository was used **read-only** as a theme reference. No files were modified in that repository. The `main` branch of Eaton-Android-To-Airplay remains unchanged until the draft PR is explicitly merged.

## Licence
Based on [Mirror](https://github.com/jqssun/android-display-mirror) under GNU GPLv3. Provide source with distributed modified builds, preserve upstream attribution, and identify modified code. No official association with Sky or Apple.
