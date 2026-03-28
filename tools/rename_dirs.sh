#!/usr/bin/env bash
set -euo pipefail

# Rename folders named 'se' to 'com' and 'magnus' to 'projects'
# across the repository using git mv to preserve history.
# This script uses a safe top-down approach by discovering target dirs
# and renaming them to their new paths where applicable.

dir_pairs_to_rename=(
  # This script generically handles all occurrences.
)

echo "Starting directory rename pass: se -> com, magnus -> projects"

# Use a robust search: find all directories named 'se' or 'magnus' and compute new path
find . -type d \( -name se -o -name magnus \) -print0 | while IFS= read -r -d '' d; do
  # Compute the new destination by replacing segments in the path
  nd=$(printf "%s" "$d" | sed -e 's#/se/#/com/#g' -e 's#/magnus/#/projects/#g')
  if [ "$d" != "$nd" ]; then
    echo "Renaming: $d -> $nd"
    mkdir -p "$(dirname "$nd")"
    git mv "$d" "$nd" || { echo "Warning: could not move $d to $nd"; }
  fi
done

echo "Directory rename pass completed."
