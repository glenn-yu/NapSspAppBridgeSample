#!/usr/bin/env bash
set -euo pipefail

case "${1:-help}" in
  gen)
    mkdir -p docs/skills/tech/java-spring
    cp ../../../../sw-methodology/templates/plan.md docs/skills/tech/java-spring/project-plan.md
    echo "Generated spring-boot skill docs in docs/skills/tech/java-spring"
    ;;
  *)
    echo "Usage: $0 gen"
    ;;
esac
