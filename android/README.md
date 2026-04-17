# Android Sample Notes

## Build

- Gradle toolchain auto-download is enabled in the project, so AGP/Kotlin tasks can request Java 17 when Gradle can launch.
- You still need a local Java launcher on the machine to start `./gradlew`. If you see "Unable to locate a Java Runtime", install or point `JAVA_HOME` at any JDK 17 first.
- From this directory:

```bash
./gradlew assembleDebug
./gradlew assembleDebug -PvendorSdkEnabled=true
```

## Beginner-friendly sample flow

- `Configure Keys` opens the key editor.
- `Fill sample keys` populates the screen with the default sample media key and ad unit IDs.
- `Test presets` lets you quickly load banner/native/video/reward/interstitial test combinations.
- `Sample settings` opens the helper menu.
- `View recent logs` shows the latest 50 in-app log lines.

## Runtime config

The SDK bridge reads runtime values through `NapSspConfig.mediaKey(context)` and `NapSspConfig.adUnitIds(context)` so saved settings always win over defaults.
