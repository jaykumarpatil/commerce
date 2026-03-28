#!/usr/bin/env bash
set -euo pipefail

COVERAGE_THRESHOLD="${COVERAGE_THRESHOLD:-80}"
POSTGRES_PORT="${POSTGRES_PORT:-55432}"
POSTGRES_DB="${POSTGRES_DB:-ecommerce_test}"
POSTGRES_USER="${POSTGRES_USER:-ecommerce}"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-ecommerce_pw}"

readonly ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
readonly FRONTEND_DIR="$ROOT_DIR/frontend"
readonly COMPOSE_FILE="$ROOT_DIR/.tmp.ci-postgres.compose.yml"

log() { printf '[%s] %s\n' "$(date -u +'%Y-%m-%dT%H:%M:%SZ')" "$*"; }
fail() {
  local code="$1"; shift
  log "ERROR: $*"
  exit "$code"
}

cleanup() {
  local exit_code=$?
  if [[ -f "$COMPOSE_FILE" ]]; then
    log "Stopping integration test PostgreSQL container"
    docker compose -f "$COMPOSE_FILE" down -v --remove-orphans >/dev/null 2>&1 || true
    rm -f "$COMPOSE_FILE"
  fi
  if [[ $exit_code -ne 0 ]]; then
    log "Pipeline failed with exit code $exit_code"
  else
    log "Pipeline completed successfully"
  fi
}
trap cleanup EXIT

run_cmd() {
  log "RUN: $*"
  "$@"
}

section() {
  log "------------------------------------------------------------"
  log "$*"
  log "------------------------------------------------------------"
}

has_gradle_task() {
  local task="$1"
  ./gradlew tasks --all --no-daemon | awk '{print $1}' | grep -qx "$task"
}

wait_for_postgres() {
  local retries=30
  until docker exec ci-postgres pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB" >/dev/null 2>&1; do
    retries=$((retries - 1))
    if [[ $retries -le 0 ]]; then
      fail 21 "PostgreSQL test container did not become ready in time"
    fi
    sleep 2
  done
}

check_backend_coverage() {
  local xmls
  mapfile -t xmls < <(find "$ROOT_DIR" -path '*/build/reports/jacoco/test/jacocoTestReport.xml' -type f)
  [[ ${#xmls[@]} -gt 0 ]] || fail 31 "No JaCoCo XML reports were generated"

  python3 - "$COVERAGE_THRESHOLD" "${xmls[@]}" <<'PY'
import sys
from xml.etree import ElementTree as ET

threshold = float(sys.argv[1])
files = sys.argv[2:]
missed = covered = 0
for file in files:
    root = ET.parse(file).getroot()
    for counter in root.findall("counter"):
        if counter.attrib.get("type") == "LINE":
            missed += int(counter.attrib.get("missed", "0"))
            covered += int(counter.attrib.get("covered", "0"))

total = missed + covered
if total == 0:
    print("Backend coverage could not be calculated")
    sys.exit(2)
coverage = (covered / total) * 100.0
print(f"Backend line coverage: {coverage:.2f}%")
if coverage < threshold:
    print(f"Backend coverage is below threshold {threshold:.2f}%")
    sys.exit(3)
PY
}

check_frontend_coverage() {
  local lcov="$FRONTEND_DIR/coverage/lcov.info"
  [[ -f "$lcov" ]] || fail 32 "Frontend LCOV report is missing: $lcov"

  python3 - "$COVERAGE_THRESHOLD" "$lcov" <<'PY'
import sys
threshold = float(sys.argv[1])
path = sys.argv[2]
lf = lh = 0
with open(path, "r", encoding="utf-8") as f:
    for line in f:
        if line.startswith("LF:"):
            lf += int(line.split(":", 1)[1])
        elif line.startswith("LH:"):
            lh += int(line.split(":", 1)[1])
if lf == 0:
    print("Frontend coverage could not be calculated")
    sys.exit(2)
coverage = (lh / lf) * 100.0
print(f"Frontend line coverage: {coverage:.2f}%")
if coverage < threshold:
    print(f"Frontend coverage is below threshold {threshold:.2f}%")
    sys.exit(3)
PY
}

main() {
  cd "$ROOT_DIR"

  section "1) CLEAN: remove build artifacts, caches, and temporary files"
  run_cmd ./gradlew --stop
  run_cmd ./gradlew clean --no-daemon
  run_cmd bash -lc "rm -rf frontend/dist frontend/coverage frontend/.angular/cache frontend/.tmp build .tmp.ci-postgres.compose.yml"

  section "2) COMPILE/BUILD: compile source and resolve dependencies"
  run_cmd ./gradlew assemble --no-daemon
  pushd "$FRONTEND_DIR" >/dev/null
  run_cmd npm ci --prefer-offline
  run_cmd npm run build -- --configuration production
  popd >/dev/null

  section "3) UNIT TESTS (parallel): backend + frontend with coverage"
  (cd "$ROOT_DIR" && ./gradlew test jacocoTestReport --no-daemon) &
  BACKEND_PID=$!
  (cd "$FRONTEND_DIR" && npm run test:ci -- --code-coverage --watch=false --browsers=ChromeHeadlessNoSandbox) &
  FRONTEND_PID=$!

  wait "$BACKEND_PID" || fail 41 "Backend unit tests failed"
  wait "$FRONTEND_PID" || fail 42 "Frontend unit tests failed"

  section "4) INTEGRATION TESTS: PostgreSQL test container via Docker Compose"
  cat > "$COMPOSE_FILE" <<YAML
services:
  postgres:
    image: postgres:16-alpine
    container_name: ci-postgres
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "${POSTGRES_PORT}:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}"]
      interval: 5s
      timeout: 5s
      retries: 20
YAML
  run_cmd docker compose -f "$COMPOSE_FILE" up -d
  wait_for_postgres

  export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:${POSTGRES_PORT}/${POSTGRES_DB}"
  export SPRING_DATASOURCE_USERNAME="$POSTGRES_USER"
  export SPRING_DATASOURCE_PASSWORD="$POSTGRES_PASSWORD"

  if has_gradle_task integrationTest; then
    run_cmd ./gradlew integrationTest --no-daemon
  else
    run_cmd ./gradlew test --tests '*Integration*' --no-daemon
  fi

  section "5) VERIFY: static analysis, security scans, package verification"
  run_cmd ./gradlew check --no-daemon

  if has_gradle_task dependencyCheckAnalyze; then
    run_cmd ./gradlew dependencyCheckAnalyze --no-daemon
  else
    log "WARN: dependencyCheckAnalyze task not found; skipping OWASP dependency scan"
  fi

  if has_gradle_task sonarqube && [[ -n "${SONAR_HOST_URL:-}" && -n "${SONAR_TOKEN:-}" ]]; then
    run_cmd ./gradlew sonarqube --no-daemon -Dsonar.host.url="$SONAR_HOST_URL" -Dsonar.token="$SONAR_TOKEN"
  else
    log "WARN: SonarQube task or credentials not available; skipping SonarQube analysis"
  fi

  pushd "$FRONTEND_DIR" >/dev/null
  run_cmd npm audit --audit-level=high
  run_cmd npm ls --omit=optional >/dev/null
  popd >/dev/null

  section "6) QUALITY GATES: enforce thresholds"
  check_backend_coverage || fail 61 "Backend JaCoCo coverage quality gate failed"
  check_frontend_coverage || fail 62 "Frontend LCOV coverage quality gate failed"

  log "All quality gates passed (>= ${COVERAGE_THRESHOLD}% coverage)"
}

main "$@"
