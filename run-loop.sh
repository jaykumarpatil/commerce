#!/usr/bin/env bash
set -x
set -euo pipefail

PROMPT_FILE="PROMPT.md"
OUTPUT_FILE="last_output.txt"
MAX_ITERATIONS=50
ITERATION=1

echo "Starting Ralph loop execution..."

while true; do
  echo "-----------------------------"
  echo "Iteration: $ITERATION"
  echo "-----------------------------"

  # Run Claude and capture output
  claude --model mlx-community/gemma-4-26b-a4b-it  -p "$(cat "$PROMPT_FILE")" | tee "$OUTPUT_FILE"

  # 🔹 Auto Git Commit (after each iteration)
  TIMESTAMP=$(date +"%Y-%m-%d %H:%M:%S")
  git add --all

  # Only commit if there are changes
  if ! git diff --cached --quiet; then
    git commit -m "chore(loop): iteration $ITERATION - $TIMESTAMP"
    echo "✅ Changes committed"
  else
    echo "ℹ️ No changes to commit"
  fi

  # Check for completion condition
  if grep -q "DONE" "$OUTPUT_FILE"; then
    echo "✅ Completion detected (DONE found). Exiting loop."
    break
  fi

  # Safety limit
  if [[ "$ITERATION" -ge "$MAX_ITERATIONS" ]]; then
    echo "⚠️ Reached max iterations ($MAX_ITERATIONS). Exiting."
    break
  fi

  # Optional delay (avoid rate limits)
  sleep 2

  ((ITERATION++))
done

echo "Loop finished."