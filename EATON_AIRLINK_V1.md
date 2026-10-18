# Eaton AirLink v1.0 beta — feature and compatibility record

This is a **development build** on `feature/eaton-airlink-v1-smart-hub`, not a
guarantee of universal TV support. `main` and the separate Eaton Vital
fitness project remain unchanged.

## 12 feature areas

| Area | Implementation | Limits |
| --- | --- | --- |
| Favourite TVs | Save and remove profiles; preset resolution and reconnect | PIN and screen capture prompts still required |
| Quick Connect | Home-screen widget and Android Quick Settings tile | Opens AirPlay page; no automatic screen capture |
| Floating controls | User-invoked overlay with mute and stop buttons | Optional permission; may disappear if Android kills the process |
| Privacy mode | Stop sending further captured frames immediately by disconnecting | Receiver may briefly retain last image; not a secure black screen |
| Picture quality | Smooth/Balanced/Best Picture output presets | Apply to next connection; TV or codec may negotiate a different format |
| Audio controls | Stream-only mute and optional follow-phone gain | Does not modify TV hardware volume; precise audio-delay calibration pending |
| Screen fitting | Existing rotate, crop and mirror settings surfaced clearly | Some features require Shizuku |
| Phone remote | Original Mirror touch/virtual display controls retained | Does not remotely control arbitrary TV menus |
| Media showcase | Private local photo/video picker and photo slideshow | Standard codecs and capture restrictions apply |
| Connection health | Wi-Fi connection, RSSI and preferred output format | Not actual latency, dropped frames or end-to-end bandwidth |
| Troubleshooting | Permission and Wi-Fi checks, AirPlay status, setup advice | No intrusive scans or cloud data collection |
| Receiver expansion | Android system Cast entry + read-only DLNA/SSDP discovery | Full Google Cast SDK, DLNA media push and general Miracast sender not implemented |

## TV remote providers

| Device | How it works | Status |
| --- | --- | --- |
| Roku / Roku TV | Roku ECP, keypress over LAN port 8060, user selected IP | Implemented; test hardware and mobile-control setting |
| LG webOS | Local SSAP WebSocket and TV-side pairing/pointer socket | Experimental; requires TV-side acceptance and may reject connection |
| Samsung Tizen | Local remote WebSocket on port 8001 and TV-side pairing | Experimental; secure-only models are not supported by this provider |
| Infrared TV | Programmable 32-bit NEC codes using Android's ConsumerIrManager | Requires IR phone, compatible NEC TV and known codes; no universal code database |
| Google / Android TV | Official TV remote guidance | No built-in authenticated Android TV remote yet |
| Amazon Fire TV | Official Fire TV remote guidance | No direct control yet |
| Sky Glass | Use Sky's remote; can still mirror through AirPlay | No verified Sky Glass network remote API; TV hardware volume not controlled |
| Other TV | Manufacturer remote app or compatible IR hardware | Depends on manufacturer/model |

**No TV should receive commands without a deliberate user action.**
The IP remote allows private IPv4 LAN hosts only. Some providers use unencrypted
local WebSockets; do not use across untrusted networks. Never disable TLS
certificate verification to force LG pointer access.

## Build and testing

- GitHub Actions assembles the unchanged native Mirror engine (with the v0.2 AirPlay volume safety patch).
- Android app version `1.0.0-beta1`.
- Run unit tests plus Gradle APK compilation, then test on actual TV hardware.
- Verify AirPlay volume preservation, no pairing regressions, Roku keys,
  LG/Samsung consent, HDMI/DisplayLink, the existing Shizuku functions, and
  media projection permissions.
- A successful compilation is **not** evidence of TV-brand compatibility.
- Keep the PR in draft until verified.
- Preserve all upstream Mirror GPLv3 notices and provide corresponding source with distributed builds.

## What is intentionally not done

This beta does not silently bypass Android capture permissions, does not
install apps onto Sky Glass, does not guarantee remote access to all TVs,
does not implement automatic safe audio synchronization or cross-protocol
transcoding, and does not circumvent protected media playback.
