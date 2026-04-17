#!/usr/bin/env bash
set -euo pipefail

case "${1:-help}" in
  gen)
    mkdir -p docs/skills/mobile/ios
    cp ../../templates/requirements.md docs/skills/mobile/ios/requirements.md
    cp ../../templates/test-plan.md docs/skills/mobile/ios/test-plan.md
    echo "Generated iOS skill docs in docs/skills/mobile/ios"
    ;;
  smoke)
    echo "Simulator smoke: xcodebuild -scheme NapSspIOSSample -destination 'platform=iOS Simulator,name=iPhone 14' build";
    ;;
  *)
    echo "Usage: $0 {gen|smoke}"
    ;;
esac
