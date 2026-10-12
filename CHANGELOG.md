# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-10-13

### Added
- **Audio Engine**:
  - AndroidX Media3 `MediaSessionService` implementation with complete background playback.
  - 160kbps Opus stream extraction and LRU offline disk caching (`SimpleCache`).
  - Hardware AudioTrack offload and DSP effects pipeline with 5-band equalizer and virtualizer.
  - Sleep timer with smooth audio fade-out support.
  - System lock screen playback controls, notification media controls, and Bluetooth AVRCP support.
- **User Interface**:
  - 100% Jetpack Compose UI built on Unidirectional Data Flow (UDF / MVI).
  - Premium glassmorphism design system with custom frosted-glass surfaces and dynamic typography.
  - Floating MiniPlayer with progress indicators and seamless swipe-to-expand transition to Full Player.
  - Personalized dynamic greetings ("Good morning", "Good afternoon", "Good evening") with 4:00 AM reset logic.
  - Quick action chips for Quick Play, Top Charts, Offline, and Settings.
  - Comprehensive Legal & Open Source Hub screen.
- **Client & Networking**:
  - Lightweight, privacy-respecting YouTube Music InnerTube client using Ktor HTTP Client.
  - Zero Google account authentication requirements and zero telemetry/trackers.
  - Search suggestion engine and trending music discovery.
- **Persistence & Storage**:
  - Room database for local favorites, recent playback history, and cached playlists.
  - Fast search history with one-tap query re-execution.
- **Distribution & Security**:
  - Minimal Play Protect compliant permissions footprint (INTERNET, FOREGROUND_SERVICE, FOREGROUND_SERVICE_MEDIA_PLAYBACK, WAKE_LOCK, POST_NOTIFICATIONS).
  - Production release keystore signing configuration.
  - Comprehensive sideloading guide for Android 14+ and Android 15.
