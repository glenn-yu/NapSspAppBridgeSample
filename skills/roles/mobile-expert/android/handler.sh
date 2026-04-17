#!/usr/bin/env bash
set -euo pipefail

CMD=${1:-help}
case "$CMD" in
  gen)
    mkdir -p docs/skills/mobile/android
    cp ../../templates/requirements.md docs/skills/mobile/android/requirements.md
    cp ../../templates/plan.md docs/skills/mobile/android/plan.md
    cp ../../templates/test-plan.md docs/skills/mobile/android/test-plan.md
    echo "Generated android skill docs in docs/skills/mobile/android"
    ;;
  smoke)
    echo "Run smoke tests: ensure device/emulator connected and JAVA_HOME set"
    echo "adb devices"
    echo "Run: adb logcat | grep -i NapSsp"
    ;;
  *)
    echo "Usage: $0 {gen|smoke}"
    ;;
esac
