#!/usr/bin/env bash
set -euo pipefail

# Fix residual path segments after initial rename pass:
# - Replace /com/magnus to /com/projects where applicable
# - Replace any trailing /magnus with /projects

echo "Starting post-rename fix pass for magnus -> projects in paths"

find . -type d -path "*/com/magnus" -print0 | while IFS= read -r -d '' d; do
  nd=$(echo "$d" | sed -e 's#/com/magnus#/com/projects#g' -e 's#/com/magnus$#/com/projects#')
  if [ "$d" != "$nd" ]; then
    echo "Fixing: $d -> $nd"
    mkdir -p "$(dirname "$nd")"
    git mv "$d" "$nd" || true
  fi
done

find . -type d -name magnus -print0 | while IFS= read -r -d '' d; do
  nd=$(echo "$d" | sed 's#/magnus$#/projects#')
  if [ "$d" != "$nd" ]; then
    echo "Renaming: $d -> $nd"
    mkdir -p "$(dirname "$nd")"
    git mv "$d" "$nd" || true
  fi
done

echo "Post-rename fix pass completed."
