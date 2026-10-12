# OpenMusic Sideloading & Android Security Guide

This guide details how to install the OpenMusic release APK on Android devices and resolve installation warnings caused by Google Play Protect and developer verification checks.

---

## Why Does Play Protect Show a Warning?

OpenMusic is an independent open-source application distributed directly via GitHub releases rather than the Google Play Store. When installing an APK signed with a private release keystore for the first time, Google Play Protect may display a warning dialog (such as *"Unrecognized app"* or *"Blocked by Play Protect"*). This occurs simply because the cryptographic signature is self-published and not indexed in Google's proprietary Play Console database.

OpenMusic contains zero telemetry, zero trackers, and zero proprietary ad SDKs. Its source code is fully open and auditable under the GPL-3.0 license.

---

## Installation Methods

### Method 1: The Play Protect Appeal Whitelist (Standard Flow)

The release signature for OpenMusic v1.0.0 has been submitted to Google's official Play Protect Appeals portal for global hash whitelisting. 

* **Timeline:** Whitelisting verification typically completes within **24 to 48 hours** following each signed release.
* **How to proceed if prompted immediately:**
  1. When opening the downloaded `app-release.apk`, if a Play Protect dialog appears, tap **More details**.
  2. Tap **Install anyway**.
  3. Once the 48-hour global certificate whitelisting propagates across Google Play Services, this prompt will disappear automatically for all users.

---

### Method 2: The "Advanced Flow" (Android 14 & Android 15)

On modern Android versions with strict package verification rules enabled, Android may require explicit confirmation for packages from non-registered developer certificates:

1. Open **Settings** on your Android device.
2. Navigate to **System** > **Developer options**. *(If Developer Options is not visible, go to Settings > About phone, and tap "Build number" 7 times).*
3. Locate and toggle **Apps from unverified developers** (or **Allow installation from unknown sources** in application settings).
4. Review and acknowledge the system confirmation prompt.
5. If your device enforces a security observation interval, wait the mandatory **24-hour security period**.
6. Return to Settings to set the permission to **Allow indefinitely**.
7. Open your file manager or browser downloads and install `app-release.apk`.

---

### Method 3: Instant Bypass via ADB (Recommended for PC Users)

If you have a computer with the Android SDK Platform Tools installed, you can bypass all on-device verification prompts instantly using ADB. This installs the APK directly to the device package manager.

1. Enable **USB Debugging** on your Android device:
   * Go to **Settings** > **Developer options** > Toggle **USB Debugging** to **ON**.
2. Connect your phone to your computer via USB and allow USB debugging when prompted.
3. Open a terminal / command prompt on your computer and navigate to where `app-release.apk` is saved.
4. Execute:
   ```bash
   adb install -r app-release.apk
   ```
5. The package manager will output `Success`. OpenMusic is now installed and ready to launch without any security prompts.

---

### Method 4: Limited Distribution Accounts (Internal Device Whitelisting)

For trusted testing rings and close circles, Android allows distributing unsigned or privately signed builds via a free **Limited Distribution Account** registered with Google:

* Up to **20 specific devices** can be registered using their hardware Android IDs or Google accounts.
* Devices enrolled in this tier completely bypass Google Play Protect warnings and verification locks for authorized builds.
* This distribution tier does not require commercial developer registration fees or government identity verification.
* To request device enrollment for beta builds, contact the maintainer with your device identifier.

---

## Verifying Package Integrity

Before installing, you can verify the SHA-256 fingerprint of the release APK to ensure the binary matches the official build:

```bash
# Verify APK signing certificate
apksigner verify --print-certs app-release.apk
```
