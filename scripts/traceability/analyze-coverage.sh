#!/bin/bash
set -euo pipefail

MATRIX=".planning/traceability-matrix.csv"
OUTPUT="docs/testing/test-gap-report.md"

echo "# Test Coverage Gap Analysis" > "$OUTPUT"
echo "" >> "$OUTPUT"
echo "**Generated:** $(date -u +%Y-%m-%dT%H:%M:%SZ)" >> "$OUTPUT"
echo "**Source:** Phase 8 traceability matrix analysis" >> "$OUTPUT"
echo "" >> "$OUTPUT"

# Overall statistics
TOTAL_FINDINGS=$(tail -n +2 "$MATRIX" | wc -l | tr -d '[:space:]')
TESTED_RAW=$(tail -n +2 "$MATRIX" | grep -c -E ',tested,|,verified,' 2>/dev/null) || TESTED_RAW=0
TESTED_FINDINGS=$TESTED_RAW
if [ "$TOTAL_FINDINGS" -eq 0 ]; then
  COVERAGE_PCT=0
else
  COVERAGE_PCT=$((TESTED_FINDINGS * 100 / TOTAL_FINDINGS))
fi

echo "## Summary" >> "$OUTPUT"
echo "" >> "$OUTPUT"
echo "- **Total findings:** $TOTAL_FINDINGS" >> "$OUTPUT"
echo "- **Tested findings:** $TESTED_FINDINGS" >> "$OUTPUT"
echo "- **Coverage:** ${COVERAGE_PCT}%" >> "$OUTPUT"
echo "" >> "$OUTPUT"

# Scan existing test files
echo "## Existing Test Inventory" >> "$OUTPUT"
echo "" >> "$OUTPUT"

BACKEND_TESTS=$(find services -name "*Test.java" 2>/dev/null | wc -l)
FRONTEND_TESTS=$(find frontend/e2e -name "*.spec.ts" 2>/dev/null | wc -l)

echo "- **Backend integration tests:** $BACKEND_TESTS files" >> "$OUTPUT"
echo "- **Frontend E2E tests:** $FRONTEND_TESTS files" >> "$OUTPUT"
echo "" >> "$OUTPUT"

# Per-feature gap analysis
echo "## Coverage by Feature" >> "$OUTPUT"
echo "" >> "$OUTPUT"

for feature in F1 F2 F3 F4 F5 F6 F7 F8 F9 F10 F11 F12 F13; do
  FEATURE_TOTAL=$(tail -n +2 "$MATRIX" | grep -cE "(,|\")$feature([^0-9]|$)" 2>/dev/null) || FEATURE_TOTAL=0
  if [ "$FEATURE_TOTAL" -eq 0 ]; then
    continue
  fi
  
  FEATURE_TESTED=$(tail -n +2 "$MATRIX" | grep -E "(,|\")$feature([^0-9]|$)" | grep -c -E ',tested,|,verified,' 2>/dev/null) || FEATURE_TESTED=0
  FEATURE_PCT=$((FEATURE_TESTED * 100 / FEATURE_TOTAL))
  
  # Get feature name from REQUIREMENTS.md (format: "- [ ] **FN**: Description")
  FEATURE_NAME=$(grep "\*\*$feature\*\*:" .planning/REQUIREMENTS.md | head -1 | sed "s/.*\*\*$feature\*\*:[[:space:]]*//" || echo "Unknown")
  [ -z "$FEATURE_NAME" ] && FEATURE_NAME="Unknown"
  
  echo "### $feature: $FEATURE_NAME" >> "$OUTPUT"
  echo "" >> "$OUTPUT"
  echo "- **Coverage:** $FEATURE_TESTED/$FEATURE_TOTAL findings tested (${FEATURE_PCT}%)" >> "$OUTPUT"
  
  # List untested findings for this feature
  UNTESTED=$(tail -n +2 "$MATRIX" | grep -E "(,|\")$feature([^0-9]|$)" | grep -v ',tested,' | grep -v ',verified,' | cut -d, -f1) || UNTESTED=""
  if [ -n "$UNTESTED" ]; then
    echo "- **Gaps:** $(echo "$UNTESTED" | tr '\n' ' ' | xargs)" >> "$OUTPUT"
  fi
  echo "" >> "$OUTPUT"
done

# High-priority gaps (explicit F13.3 edge cases)
echo "## High-Priority Gaps (F13.3 Edge Cases)" >> "$OUTPUT"
echo "" >> "$OUTPUT"
echo "Per FRD F13.3, these findings require explicit test coverage before release:" >> "$OUTPUT"
echo "" >> "$OUTPUT"

# Conflict detection edge cases (F2)
echo "### Conflict Detection (F2)" >> "$OUTPUT"
CONFLICT_GAPS=$(tail -n +2 "$MATRIX" | grep -E "(,|\")F2([^0-9]|$)" | grep -v ',tested,' | grep -v ',verified,' | cut -d, -f1) || CONFLICT_GAPS=""
if [ -n "$CONFLICT_GAPS" ]; then
  echo "- Findings: $(echo "$CONFLICT_GAPS" | tr '\n' ' ' | xargs)" >> "$OUTPUT"
  echo "- **Required tests:** Half-open interval semantics, location AND resource overlap, exclude booking's own prior state, exclude deleted/denied bookings" >> "$OUTPUT"
else
  echo "- ✓ All conflict detection findings tested" >> "$OUTPUT"
fi
echo "" >> "$OUTPUT"

# Approval/auto-approve interactions (F3)
echo "### Approval Workflow (F3)" >> "$OUTPUT"
APPROVAL_GAPS=$(tail -n +2 "$MATRIX" | grep -E "(,|\")F3([^0-9]|$)" | grep -v ',tested,' | grep -v ',verified,' | cut -d, -f1) || APPROVAL_GAPS=""
if [ -n "$APPROVAL_GAPS" ]; then
  echo "- Findings: $(echo "$APPROVAL_GAPS" | tr '\n' ' ' | xargs)" >> "$OUTPUT"
  echo "- **Required tests:** Auto-approve on/off transitions, one-way approve/deny (no re-toggling), permission-gated actions" >> "$OUTPUT"
else
  echo "- ✓ All approval workflow findings tested" >> "$OUTPUT"
fi
echo "" >> "$OUTPUT"

# Recurring booking scoping (F1.3)
echo "### Recurring Booking Scoping (F1.3)" >> "$OUTPUT"
RECURRING_GAPS=$(tail -n +2 "$MATRIX" | grep 'BF-011\|BF-015\|BF-017' | grep -v ',tested,' | grep -v ',verified,' | cut -d, -f1) || RECURRING_GAPS=""
if [ -n "$RECURRING_GAPS" ]; then
  echo "- Findings: $(echo "$RECURRING_GAPS" | tr '\n' ' ' | xargs)" >> "$OUTPUT"
  echo "- **Required tests:** Repeat-loop creates independent siblings, no series-aware edit/delete, repeatno upper bound" >> "$OUTPUT"
else
  echo "- ✓ All recurring booking findings tested" >> "$OUTPUT"
fi
echo "" >> "$OUTPUT"

# Permission matrix boundaries (F7)
echo "### Permission Matrix (F7)" >> "$OUTPUT"
PERMISSION_GAPS=$(tail -n +2 "$MATRIX" | grep -E "(,|\")F7([^0-9]|$)" | grep -v ',tested,' | grep -v ',verified,' | cut -d, -f1) || PERMISSION_GAPS=""
if [ -n "$PERMISSION_GAPS" ]; then
  echo "- Findings: $(echo "$PERMISSION_GAPS" | tr '\n' ' ' | xargs)" >> "$OUTPUT"
  echo "- **Required tests:** 17-flag inventory coverage, deny-by-default for unconfirmed flags, Tier1/Tier2 consistency" >> "$OUTPUT"
else
  echo "- ✓ All permission matrix findings tested" >> "$OUTPUT"
fi
echo "" >> "$OUTPUT"

# Open Questions needing tests
echo "## Open Questions Requiring Test Stubs" >> "$OUTPUT"
echo "" >> "$OUTPUT"
OPEN_QUESTIONS=$(tail -n +2 "$MATRIX" | grep 'Open Question' | cut -d, -f1) || OPEN_QUESTIONS=""
if [ -n "$OPEN_QUESTIONS" ]; then
  echo "Per F13.5, these Open Questions must have a test added when resolved:" >> "$OUTPUT"
  echo "" >> "$OUTPUT"
  echo "$(echo "$OPEN_QUESTIONS" | tr '\n' ' ' | xargs)" >> "$OUTPUT"
  echo "" >> "$OUTPUT"
  echo "See .planning/phases/01-legacy-functional-audit/open-questions.md for details." >> "$OUTPUT"
else
  echo "- ✓ No open questions pending resolution" >> "$OUTPUT"
fi
echo "" >> "$OUTPUT"

echo "---" >> "$OUTPUT"
echo "*Run \`bash scripts/traceability/analyze-coverage.sh\` after adding tests to update this report.*" >> "$OUTPUT"

echo "Coverage gap report written to $OUTPUT"
