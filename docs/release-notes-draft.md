# Release Notes Draft — Unreleased

This draft is based on the current commit history and is ready to be trimmed into a tag-specific release note.

## Highlights
- Android and iOS samples now cover the full beginner flow: banner, native, rewarded, and interstitial ads.
- The iOS sample uses local vendored `AdMixer` / `AdMixerMediation` binaries so builds are reproducible without remote binary fetches.
- The docs now include a shorter beginner quickstart and a migration plan for `ios/Vendor/`.
- CI coverage was tightened for Android and iOS, with better caching and clearer Java/Xcode expectations.

## What changed
- Android:
  - Added reflection-based interstitial and rewarded modules.
  - Kept the sample beginner-friendly while preserving the vendor toggle path.
  - CI now documents the real Gradle wrapper location and uses Java 17 setup more cleanly.
- iOS:
  - Wired interstitial and rewarded events through the bridge layer.
  - Kept the vendored binary targets in `ios/Vendor/` for reproducible builds.
- Docs:
  - Reworked the quickstart into a one-screen onboarding path.
  - Added a Git LFS migration plan for the vendor payloads.
  - Added this release note draft so the next tag can be assembled quickly.

## Notes for the release tag
- This codebase still expects Android builds to run with a valid JDK 17.
- iOS builds are currently designed around the vendored local binaries under `ios/Vendor/`.
- If `ios/Vendor/` is migrated to Git LFS later, update the quickstart and CI instructions together.

## Suggested publish copy
"This release focuses on onboarding and build reliability. New developers now get a shorter quickstart, CI is more deterministic, and the sample covers rewarded/interstitial flows on both Android and iOS."
