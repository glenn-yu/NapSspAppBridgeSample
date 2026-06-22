# CHANGELOG

## Unreleased

### Added
- Beginner quickstart with exact macOS, Linux, and Windows commands plus troubleshooting.
- Git LFS migration plan for `ios/Vendor/`.
- Release notes draft for the next tag.

### Changed
- Upgraded AdMixer SDK dependencies to version 2.0.0 (replacing legacy classes with new `AMM*` classes).
- Refactored Android reflection bridges to concrete type-safe calls to support v2.0.0 SDK's abstract AdListener class.
- Updated native ad layout XML view IDs to use the `nap_mx_` prefix.
- Android CI now sets up JDK 21/17 with Gradle caching and runs from the real `android/` working directory.
- iOS CI now caches Xcode/SwiftPM build products and writes derived data into a predictable path.
- Docs now point new contributors to the shorter quickstart and the new vendor/release notes pages.

### Fixed
- Android and iOS sample wiring now covers rewarded and interstitial flows end to end.
- The iOS build story remains reproducible through the vendored local binary targets in `ios/Vendor/`.

### Notes
- Android still requires a valid JDK 17 locally; this repo does not bundle a Java runtime.
- If the team later migrates `ios/Vendor/` to Git LFS, update the quickstart and CI docs together.
