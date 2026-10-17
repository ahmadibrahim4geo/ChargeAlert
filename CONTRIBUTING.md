# Contributing to Charge Alert

Report bugs and propose changes through [GitHub Issues](https://github.com/ahmadibrahim4geo/ChargeAlert/issues). Include the device model, Android version, app version, steps to reproduce and expected behavior.

Use JDK 17 and Android SDK 36. Set your own SDK location in an ignored local.properties file; never add signing keys or credentials.

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

For visual changes, record screenshots and inspect them:

```bash
./gradlew testDebugUnitTest -Proborazzi.test.record=true
```

Keep Arabic RTL and English LTR usable on small screens. Readings must come from Android; animation must not invent battery progress. Test dismissal, reconnecting and background recovery when changing monitoring behavior.

Open a pull request against main describing the change and validation. Keep generated build directories and personal SDK settings out of Git. The root ChargeAlert.apk is the single current test build; update it only after the checks pass.

Developer contact: [Ahmad Ibrahim](mailto:ahmadibrahim.geo@gmail.com).
