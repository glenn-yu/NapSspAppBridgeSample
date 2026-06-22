# CHANGELOG

## Unreleased

## [0.2.0] - 2026-06-22

### Added
- Hybrid WebView bridge now supports an optional `requestId`: it is echoed on the matching response and on the SDK lifecycle events that follow a `loadAd`, so the web layer can correlate async loaded/clicked/closed events with their request (Android + iOS, with HTML samples and docs).
- Explicit `busy` response (carrying the `requestId`) when a request is dropped by the 500ms throttle, instead of being silently ignored.
- Beginner quickstart with exact macOS, Linux, and Windows commands plus troubleshooting.
- Git LFS migration plan for `ios/Vendor/`.
- Release notes draft for the next tag.
- Added MIT LICENSE file and expanded `.gitignore` patterns.
- Bilingual (English/Korean) support for main documentation files (README, guide, quickstart).

### Changed
- Upgraded AdMixer SDK dependencies to version 2.0.0 (replacing legacy classes with new `AMM*` classes).
- Refactored Android reflection bridges to concrete type-safe calls to support v2.0.0 SDK's abstract AdListener class.
- Updated native ad layout XML view IDs to use the `nap_mx_` prefix.
- Android CI now sets up JDK 21/17 with Gradle caching and runs from the real `android/` working directory.
- iOS CI now caches Xcode/SwiftPM build products and writes derived data into a predictable path.
- Docs now point new contributors to the shorter quickstart and the new vendor/release notes pages.
- Updated all guide links to point to the new official guide website (https://napmx.github.io).

### Fixed
- Android and iOS sample wiring now covers rewarded and interstitial flows end to end.
- The iOS build story remains reproducible through the vendored local binary targets in `ios/Vendor/`.

### Removed
- Internal build/debug artifacts (`android/artifacts/`, smoke report, build logs) and dangling internal-only documentation references, for the public release.

### Notes
- Android still requires a valid JDK 17 locally; this repo does not bundle a Java runtime.
- If the team later migrates `ios/Vendor/` to Git LFS, update the quickstart and CI docs together.
