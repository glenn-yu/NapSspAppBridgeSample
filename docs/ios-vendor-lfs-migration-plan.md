# iOS Vendor Git LFS Migration Plan

This repo currently keeps the iOS vendored SDK payloads in `ios/Vendor/` so the sample can build without remote binary fetches. The long-term maintenance issue is clone size, so this document lays out a safe Git LFS migration path.

## Scope
Track the full `ios/Vendor/` tree in Git LFS, including:
- `*.xcframework` payloads
- `*.zip` archives used to store the vendor binaries
- any future binary-only vendor artifacts under `ios/Vendor/`

Recommended `.gitattributes` rule for the migration window:
```gitattributes
ios/Vendor/** filter=lfs diff=lfs merge=lfs -text
```

## Proposed migration steps
1. Install Git LFS on every maintainer machine and CI runner.
   ```bash
   git lfs install
   ```
2. Add or update tracking rules.
   ```bash
   git lfs track "ios/Vendor/**"
   git add .gitattributes
   git commit -m "chore: track ios/Vendor with Git LFS"
   ```
3. Rewrite history for the vendor tree on the target branch.
   ```bash
   git lfs migrate import --include="ios/Vendor/**" --include-ref=refs/heads/main
   ```
4. Push the rewritten branch with lease protection.
   ```bash
   git push --force-with-lease origin main
   ```

## Impact on clones and CI
- New clones need `git-lfs` installed before checkout.
- Plain `git clone` will initially fetch pointer files; `git lfs pull` is required to download the real binaries.
- GitHub Actions should use `actions/checkout@v4` with `lfs: true` or run an explicit `git lfs pull` step.
- First-time clones become lighter, but CI and contributor machines need LFS bandwidth and storage awareness.
- Fork-based PRs must also have LFS available, otherwise vendored iOS builds will fail.

## Recommended rollout
- Announce a short freeze window before the migration.
- Merge no other vendor-file changes during the history rewrite.
- Keep the release branch pinned until the rewritten history is verified.
- Update the quickstart and CI docs to mention the LFS requirement if/when the migration ships.

## Rollback plan
If the migration needs to be reversed before release:
1. Stop new merges into the rewritten branch.
2. Restore the pre-migration branch state from the last non-LFS commit.
3. Remove the tracking rule from `.gitattributes`.
4. Run the reverse migration if history must be rewritten again:
   ```bash
   git lfs migrate export --include="ios/Vendor/**" --include-ref=refs/heads/main
   ```
5. Force-push only after the rollback branch has been validated.

If the team decides the vendor tree should stay in normal Git, the safest fallback is to keep the current vendored layout and accept the larger clone size rather than mixing storage strategies mid-release.
