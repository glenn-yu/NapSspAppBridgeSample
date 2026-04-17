#!/usr/bin/env bash
set -euo pipefail

PROJECT_NAME="${1:-sample-project}"
OWNER="${2:-}" 
MODE="${3:-dry-run}"

OUT_DIR="docs/process/${PROJECT_NAME}"
mkdir -p "$OUT_DIR"

echo "Generating templates for $PROJECT_NAME -> $OUT_DIR (mode=$MODE)"

for t in requirements plan test-plan release-checklist retrospective; do
  tpl="$(dirname "$0")/templates/${t}.md"
  out="$OUT_DIR/${t}.md"
  sed "s/{{project_name}}/${PROJECT_NAME}/g; s/{{owner}}/${OWNER}/g" "$tpl" > "$out"
  echo "Wrote $out"
done

if [ "$MODE" == "commit" ]; then
  git checkout -b docs/${PROJECT_NAME}-methodology
  git add "$OUT_DIR"
  git commit -m "docs(methodology): add process templates for ${PROJECT_NAME}"
  git push --set-upstream origin "docs/${PROJECT_NAME}-methodology"
  echo "Created branch docs/${PROJECT_NAME}-methodology and pushed. Create a PR to merge."
fi

if [ "$MODE" == "pr" ]; then
  echo "PR mode: creating branch and PR"
  git checkout -b docs/${PROJECT_NAME}-methodology
  git add "$OUT_DIR"
  git commit -m "docs(methodology): add process templates for ${PROJECT_NAME}"
  git push --set-upstream origin "docs/${PROJECT_NAME}-methodology"
  gh pr create --title "chore(docs): add methodology templates for ${PROJECT_NAME}" --body "Auto-generated methodology templates for ${PROJECT_NAME}." --head "docs/${PROJECT_NAME}-methodology" --base main
fi

exit 0
