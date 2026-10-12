# OpenMusic

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Target API](https://img.shields.io/badge/API-34%2B%20(Android%2014)-brightgreen.svg)](https://developer.android.com/about/versions/14)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.10.00-4285F4.svg)](https://developer.android.com/jetpack/compose)

A lightweight, private, and high-performance streaming music player for Android. OpenMusic interfaces directly with the YouTube Music InnerTube endpoint without requiring Google Play Services or account logins, delivering high-fidelity audio with zero trackers and zero ads.

---

## About

OpenMusic is engineered as a privacy-respecting client for YouTube Music streams. By querying InnerTube directly through a lightweight HTTP client, OpenMusic bypasses heavy web wrappers and client-side telemetry. The app pairs a modern Jetpack Compose interface with AndroidX Media3 for rock-solid background playback and caching.

---

## Features

* **High-Fidelity Opus Playback**: Streams and extracts clean 160kbps Opus streams with automatic fallback.
* **Intelligent LRU Disk Caching**: Caches recently played tracks to disk using ExoPlayer's `SimpleCache` for instant re-listening without re-downloading.
* **Background & Lock Screen Playback**: Full `MediaSessionService` implementation with rich lock-screen controls, media notifications, and Bluetooth AVRCP support.
* **DSP Audio Suite**: Hardware-accelerated 5-band equalizer, bass boost, and audio virtualizer with preset management.
* **Modern Glassmorphic UI**: 100% Jetpack Compose with custom frosted-glass surfaces, blur backdrops, and fluid transitions.
* **Unidirectional Data Flow (UDF)**: Strict MVI/UDF state architecture ensuring responsive and predictable UI state handling.
* **Privacy by Design**: Zero analytics, zero ad SDKs, zero telemetry, and minimal permission footprint.
* **Offline Playback & Library**: Save favorite tracks, browse playback history, and play cached tracks offline.

---

## Tech Stack & Architecture

* **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) + Material 3
* **Audio Engine**: [AndroidX Media3](https://developer.android.com/media/media3) (`MediaSessionService`, `ExoPlayer`, `AudioTrack` offload)
* **Networking**: [Ktor Client](https://ktor.io/) + Kotlinx Serialization
* **Database**: [Room](https://developer.android.com/training/data-storage/room) SQLite with Kotlin Coroutines Flow
* **Image Loading**: [Coil Compose](https://coil-kt.github.io/coil/)
* **Architecture**: Clean Architecture with MVI / Unidirectional Data Flow (UDF)

---

## Security & Permission Model

OpenMusic adheres to a minimal-permission security posture to comply with Play Protect and modern Android privacy standards:

| Permission | Purpose |
| ---------- | ------- |
| `android.permission.INTERNET` | Stream audio tracks and metadata from InnerTube endpoints |
| `android.permission.FOREGROUND_SERVICE` | Maintain audio playback while the app is in the background |
| `android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Required on Android 14+ for foreground audio playback services |
| `android.permission.WAKE_LOCK` | Prevent CPU sleep during screen-off audio streaming |
| `android.permission.POST_NOTIFICATIONS` | Display playback controls in the system notification shade |

---

## Installation & Sideloading

Release APKs are available on the [Releases](../../releases) page.

If you encounter Play Protect warnings or are installing on Android 14+ devices, please refer to our detailed setup guide:

> **[Read the Sideloading & Android Security Guide](SIDELOADING_GUIDE.md)**
> *Includes instructions for Play Protect appeals, Android 14 Developer Options setup, and instant ADB installation.*

---

## Building from Source

### Prerequisites
* JDK 17 or higher
* Android SDK Platform 34 & 35
* Android SDK Build-Tools 34.0.0+
* Gradle 8.11+ (or use the included `./gradlew` wrapper)

### Build Commands

1. **Clone the repository:**
   ```bash
   git clone https://github.com/OpenMusic.git
   cd OpenMusic
   ```

2. **Build Debug APK:**
   ```bash
   ./gradlew assembleDebug
   ```
   Output: `app/build/outputs/apk/debug/app-debug.apk`

3. **Build Release APK:**
   Ensure release signing configuration is present in `app/build.gradle.kts` or `keystore.jks`:
   ```bash
   ./gradlew assembleRelease
   ```
   Output: `app/build/outputs/apk/release/app-release.apk`

4. **Install to Connected Device via ADB:**
   ```bash
   adb install -r app/build/outputs/apk/release/app-release.apk
   ```

---

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on code style, branch guidelines, and testing requirements.

---

## Security

To report security vulnerabilities responsibly, please review our [Security Policy](SECURITY.md).

---

## License

OpenMusic is licensed under the **GNU General Public License v3.0**. See the [LICENSE](LICENSE) file for details.
