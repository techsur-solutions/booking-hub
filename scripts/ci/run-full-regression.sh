#!/bin/bash
# run_regression: local pre-push regression suite runner
# Usage: bash scripts/ci/run-full-regression.sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

echo "========================================"
echo "BookingHub Regression Suite (Local Run)"
echo "========================================"
echo ""

# Ensure we run from project root
cd "${PROJECT_ROOT}"

# Check docker-compose is running
if ! docker compose ps 2>/dev/null | grep -q 'Up'; then
  echo "⚠️  docker-compose stack not running. Starting services..."
  docker compose up -d
  echo "Waiting for services to be healthy..."
  sleep 10
fi

FAILED=0

# Backend tests
echo "========================================"
echo "Running Backend Integration Tests"
echo "========================================"
echo ""

cd services
for service_dir in */; do
  service_name=$(basename "$service_dir")

  # Skip if no pom.xml (e.g., api-gateway might have different build)
  if [ ! -f "${service_dir}pom.xml" ]; then
    echo "⏭️  Skipping $service_name (no pom.xml)"
    continue
  fi

  echo "Testing $service_name..."
  if (cd "$service_dir" && mvn test -q); then
    echo "✅ $service_name passed"
  else
    echo "❌ $service_name FAILED"
    FAILED=$((FAILED + 1))
  fi
  echo ""
done
cd "${PROJECT_ROOT}"

# Frontend tests
echo "========================================"
echo "Running Frontend E2E Tests"
echo "========================================"
echo ""

cd frontend

# Ensure test:e2e script exists; use playwright test directly as fallback
if npm run --silent 2>&1 | grep -q 'test:e2e'; then
  if npm run test:e2e 2>&1 | tee /tmp/pw-output.txt | grep -qE '^[0-9]+ (passed|failed)'; then
    if grep -q 'failed' /tmp/pw-output.txt; then
      echo "❌ Frontend tests FAILED"
      FAILED=$((FAILED + 1))
    else
      echo "✅ Frontend tests passed"
    fi
  else
    # Playwright test exited non-zero
    echo "❌ Frontend tests FAILED (non-zero exit)"
    FAILED=$((FAILED + 1))
  fi
else
  # Fallback: run playwright directly
  if npx playwright test --workers=1 --reporter=list; then
    echo "✅ Frontend tests passed"
  else
    echo "❌ Frontend tests FAILED"
    FAILED=$((FAILED + 1))
  fi
fi
cd "${PROJECT_ROOT}"

# Summary
echo ""
echo "========================================"
if [ $FAILED -eq 0 ]; then
  echo "✅ All regression tests PASSED"
  echo "Safe to push."
  exit 0
else
  echo "❌ $FAILED test suite(s) FAILED"
  echo "Fix failures before pushing."
  exit 1
fi
