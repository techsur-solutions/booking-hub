#!/bin/bash
# seed-matrix.sh — Generates .planning/traceability-matrix.csv from baseline-inventory.md
# Part of F13 traceability tooling (Phase 8, Plan 01)
#
# Usage: bash scripts/traceability/seed-matrix.sh
#
# Reads: .planning/phases/01-legacy-functional-audit/baseline-inventory.md
# Writes: .planning/traceability-matrix.csv (always regenerated from source)
#
# Security: Input is a committed read-only artifact from Phase 1; no eval, no
# untrusted data, bounded regex BF-[0-9]{3} prevents injection/hang on malformed input.
set -euo pipefail

INPUT=".planning/phases/01-legacy-functional-audit/baseline-inventory.md"
OUTPUT=".planning/traceability-matrix.csv"

if [ ! -f "$INPUT" ]; then
  echo "ERROR: Input file not found: $INPUT" >&2
  exit 1
fi

# Write CSV header
echo "finding_id,requirement_ref,implementation_ref,test_ref,status,notes" > "$OUTPUT"

# Parse table rows — lines starting with "| BF-" in baseline-inventory.md
# Format: | ID | Source File | Citation | Description | Status | Maps-To-Feature |
grep -E '^\| BF-[0-9]{3} \|' "$INPUT" | while IFS='|' read -r _ id source citation desc status maps_to _; do
  # Trim leading/trailing whitespace from each field
  id=$(echo "$id" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
  status_clean=$(echo "$status" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
  maps_to=$(echo "$maps_to" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')

  # Skip rows that don't look like valid BF-NNN IDs
  if ! echo "$id" | grep -qE '^BF-[0-9]{3}$'; then
    continue
  fi

  # All findings start at not_started; Open findings get a note
  traceability_status="not_started"
  notes=""
  if [ "$status_clean" = "Open" ]; then
    notes="Open Question - see open-questions.md"
  fi

  # CSV-encode requirement_ref: if it contains a comma, wrap in double quotes
  req_ref="$maps_to"
  if echo "$req_ref" | grep -q ','; then
    req_ref="\"$req_ref\""
  fi

  # Output: finding_id, requirement_ref, empty impl_ref, empty test_ref, status, notes
  printf '%s,%s,,,%s,%s\n' "$id" "$req_ref" "$traceability_status" "$notes"
done >> "$OUTPUT"

# Report result
row_count=$(tail -n +2 "$OUTPUT" | wc -l | tr -d ' ')
echo "Seeded $OUTPUT with $row_count findings from $INPUT"
