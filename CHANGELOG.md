# Changelog

All notable changes to the **Charge Alert (تنبيه الشحن)** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-10-17

### Changed
- Clarified the charge indicator: one current-charge arc, a separate white target tick and an active-charging pulse. Explained paused motion and removed duplicate progress graphics.
- Replaced the launcher artwork with a simpler battery-and-bolt mark, including legacy, adaptive and monochrome icons.
- Prepared a portable GitHub project with one current APK, a logo-led README and developer contact details.
- Removed unused Firebase/AI generator configuration and a machine-specific Java path.
- Rebuilt all ten charging designs, including Prism Glass and Circuit Studio, while retaining saved theme IDs.
- Replaced the text-only theme picker with a thumbnail gallery, collections and a full-screen preview.
- Matched the charging screen background and accents to the selected theme.
- Preserved the chosen design in reduced-motion and Battery Saver modes. Static designs do not run an animation clock.

### Added
- Ten designs: Lunar Eclipse, Ocean Depth, Rose Silk, Golden Dial, Mono Studio, Crystal Facets, Light Bloom, Contour Lines, Comet Trail and Sunset Horizon.
- Visual coverage for all twenty designs, gallery selection and compatibility with previously saved selections.

## [1.0.0] - 2026-07-25
<!-- Original release notes below describe the previous implementation. -->

### Added
- **Battery Charge Monitoring**: Monitor battery percentage in real-time and sound an alert when reaching user-selected target thresholds (50% - 100%).
- **Smart Alert System**: Custom audio channel configuration allowing alarm, ringtone, or notification sounds to bypass silent/Do-Not-Disturb modes for critical charge alerts.
- **10 Premium Animated Lockscreen Themes**: High-performance, Canvas-driven charging screens featuring particle effects, neon glows, sweep gradients, wave ripples, and futuristic HUD displays.
- **Full-Screen Notification Intent**: Lockscreen overlay activity for Android 10+ (API 29+) ensuring immediate alert display over locked screens.
- **Foreground Service Integration**: Persistent background monitoring (`BatteryMonitoringService`) ensuring reliable charge tracking even when the app is closed.
- **Bilingual Interface**: Full localization support for Arabic (RTL native support) and English (LTR).
- **Comprehensive Battery Health Metrics**: Real-time display of battery temperature (°C / °F), voltage (mV), health condition, and battery chemistry technology.
- **Smart WakeLock Management**: Automatically wakes up the screen upon charger plugin/unplug events.
- **Battery Optimization Exemption Request**: Built-in prompts to request battery optimization exclusion for uninterrupted background service execution across manufacturer OS skins.
- **Dynamic App Status Card**: Instant visual dashboard indicating service connection status, monitoring state, target percentage, and active charging source.
