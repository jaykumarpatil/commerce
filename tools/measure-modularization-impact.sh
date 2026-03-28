#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   tools/measure-modularization-impact.sh :app [label]
# Example:
#   tools/measure-modularization-impact.sh :app baseline

MODULE_PATH="${1:-:app}"
LABEL="${2:-run-$(date -u +%Y%m%dT%H%M%SZ)}"
OUT_DIR="build/modularization-metrics/${LABEL}"
mkdir -p "${OUT_DIR}"

JAR_TASK="${MODULE_PATH}:bootJar"

echo "[1/4] Building executable jar for ${MODULE_PATH}"
./gradlew "${JAR_TASK}" -x test > "${OUT_DIR}/gradle-build.log" 2>&1

MODULE_NAME="${MODULE_PATH##*:}"
JAR_PATH=$(find . -path "*/${MODULE_NAME}/build/libs/*.jar" | head -n 1 || true)
if [[ -z "${JAR_PATH}" ]]; then
  echo "Could not locate jar for ${MODULE_PATH}."
  echo "See ${OUT_DIR}/gradle-build.log"
  exit 1
fi

cp "${JAR_PATH}" "${OUT_DIR}/app.jar"

echo "[2/4] Collecting class count + jar size"
CLASS_COUNT=$(jar tf "${OUT_DIR}/app.jar" | rg -c '\.class$' || true)
JAR_BYTES=$(wc -c < "${OUT_DIR}/app.jar")
printf "class_count=%s\njar_bytes=%s\n" "${CLASS_COUNT}" "${JAR_BYTES}" > "${OUT_DIR}/artifact-metrics.txt"

echo "[3/4] Measuring startup time and RSS"
START_LOG="${OUT_DIR}/startup.log"
START_MS=$(( $(date +%s%3N) ))
java -jar "${OUT_DIR}/app.jar" --server.port=0 --spring.main.lazy-initialization=true > "${START_LOG}" 2>&1 &
PID=$!

READY=0
for _ in {1..120}; do
  if rg -q "Started .* in .* seconds" "${START_LOG}"; then
    READY=1
    break
  fi
  sleep 1
done

END_MS=$(( $(date +%s%3N) ))
STARTUP_MS=$(( END_MS - START_MS ))
RSS_KB=$(ps -o rss= -p "${PID}" | tr -d ' ' || echo "0")

if [[ "${READY}" -eq 0 ]]; then
  echo "warning=application_did_not_report_started" > "${OUT_DIR}/runtime-metrics.txt"
else
  printf "startup_ms=%s\nrss_kb=%s\n" "${STARTUP_MS}" "${RSS_KB}" > "${OUT_DIR}/runtime-metrics.txt"
fi

kill "${PID}" >/dev/null 2>&1 || true
wait "${PID}" 2>/dev/null || true

echo "[4/4] Optional OCI image size (requires Docker daemon)"
if command -v docker >/dev/null 2>&1; then
  IMAGE_TAG="mod-metrics-${LABEL,,}"
  if ./gradlew "${MODULE_PATH}:bootBuildImage" --imageName="${IMAGE_TAG}" > "${OUT_DIR}/bootBuildImage.log" 2>&1; then
    IMAGE_BYTES=$(docker image inspect "${IMAGE_TAG}" --format '{{.Size}}' || echo "0")
    printf "image_bytes=%s\n" "${IMAGE_BYTES}" > "${OUT_DIR}/image-metrics.txt"
  else
    echo "warning=bootBuildImage_failed" > "${OUT_DIR}/image-metrics.txt"
  fi
else
  echo "warning=docker_not_available" > "${OUT_DIR}/image-metrics.txt"
fi

echo "Metrics written to ${OUT_DIR}"
