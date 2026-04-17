# CHANGELOG

## Unreleased

### Changed
- Make NapSspAppBridgeSample beginner-friendly and buildable for Android and iOS.
- iOS: Vendored AdMixer and AdMixerMediation xcframeworks under `ios/Vendor/` to ensure reproducible builds.
- Runtime config: `Configure Keys` UI (Android/iOS) now persists and feeds `NapSspConfig` at runtime — no hardcoded keys required for testing.
- Docs: README and docs/* updated to reflect current build flow and iOS vendored SDK note.

### Notes
- Consider migrating large binary files in `ios/Vendor/` to Git LFS or an artifact repository for long-term maintenance.
- Recommended next steps: add iOS CI job on macOS runner; create PR checklist and release notes before merging.
