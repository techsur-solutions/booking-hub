#!/bin/bash
# query-matrix.sh — Convenience queries for the F13 traceability matrix
# Part of F13 traceability tooling (Phase 8, Plan 01)
#
# Usage: bash scripts/traceability/query-matrix.sh <command> [args]
set -euo pipefail

MATRIX=".planning/traceability-matrix.csv"

if [ ! -f "$MATRIX" ]; then
  echo "ERROR: Matrix file not found: $MATRIX" >&2
  echo "Run: bash scripts/traceability/seed-matrix.sh" >&2
  exit 1
fi

usage() {
  cat <<USAGE
Usage: $0 <command> [args]

Commands:
  gaps                 - Show all findings without tests (status != tested|verified)
  by-feature <FN>      - Show findings for feature (e.g., F7, F2.3)
  by-status <status>   - Show findings with given status
  open-questions       - Show findings marked as Open Questions
  coverage-summary     - Status breakdown by feature
  finding <BF-NNN>     - Show single finding details

Examples:
  $0 gaps
  $0 by-feature F7
  $0 by-status tested
  $0 open-questions
  $0 coverage-summary
  $0 finding BF-028
USAGE
  exit 1
}

[ $# -lt 1 ] && usage

CMD="$1"
shift || true

case "$CMD" in
  gaps)
    echo "Findings without tests (not_started or implemented):"
    tail -n +2 "$MATRIX" | { grep -v ',tested,' || true; } | { grep -v ',verified,' || true; } | cut -d, -f1,2,5
    ;;
  by-feature)
    [ -z "${1:-}" ] && { echo "Error: Feature ID required"; usage; }
    FEATURE="$1"
    echo "Findings for feature $FEATURE:"
    tail -n +2 "$MATRIX" | { grep ",$FEATURE" || true; } | cut -d, -f1,2,5
    ;;
  by-status)
    [ -z "${1:-}" ] && { echo "Error: Status required"; usage; }
    STATUS="$1"
    echo "Findings with status=$STATUS:"
    tail -n +2 "$MATRIX" | { grep ",$STATUS," || true; } | cut -d, -f1,2,5
    ;;
  open-questions)
    echo "Findings marked as Open Questions:"
    tail -n +2 "$MATRIX" | { grep 'Open Question' || true; } | cut -d, -f1,2,6
    ;;
  coverage-summary)
    echo "Coverage summary by feature:"
    echo "Feature,not_started,implemented,tested,verified"
    for feature in F0 F1 F2 F3 F4 F5 F6 F7 F8 F9 F10 F11 F12 F13; do
      not_started=$(tail -n +2 "$MATRIX" | { grep ",$feature" || true; } | { grep ',not_started,' || true; } | wc -l | tr -d ' ')
      implemented=$(tail -n +2 "$MATRIX" | { grep ",$feature" || true; } | { grep ',implemented,' || true; } | wc -l | tr -d ' ')
      tested=$(tail -n +2 "$MATRIX" | { grep ",$feature" || true; } | { grep ',tested,' || true; } | wc -l | tr -d ' ')
      verified=$(tail -n +2 "$MATRIX" | { grep ",$feature" || true; } | { grep ',verified,' || true; } | wc -l | tr -d ' ')
      total=$((not_started + implemented + tested + verified))
      [ "$total" -gt 0 ] && echo "$feature,$not_started,$implemented,$tested,$verified"
    done
    ;;
  finding)
    [ -z "${1:-}" ] && { echo "Error: Finding ID required"; usage; }
    FINDING="$1"
    echo "Details for $FINDING:"
    head -1 "$MATRIX"
    tail -n +2 "$MATRIX" | { grep "^$FINDING," || true; }
    ;;
  *)
    echo "Error: Unknown command '$CMD'"
    usage
    ;;
esac
