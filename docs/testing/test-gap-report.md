# Test Coverage Gap Analysis

**Generated:** 2026-10-13T04:17:33Z
**Source:** Phase 8 traceability matrix analysis

## Summary

- **Total findings:** 113
- **Tested findings:** 0
- **Coverage:** 0%

## Existing Test Inventory

- **Backend integration tests:** 64 files
- **Frontend E2E tests:** 2 files

## Coverage by Feature

### F1: Unknown

- **Coverage:** 0/25 findings tested (0%)
- **Gaps:** BF-002 BF-003 BF-004 BF-005 BF-008 BF-011 BF-015 BF-020 BF-021 BF-023 BF-082 BF-083 BF-084 BF-086 BF-087 BF-096 BF-097 BF-098 BF-100 BF-101 BF-102 BF-105 BF-107 BF-108 BF-111

### F2: Unknown

- **Coverage:** 0/5 findings tested (0%)
- **Gaps:** BF-012 BF-016 BF-028 BF-112 BF-113

### F3: Unknown

- **Coverage:** 0/2 findings tested (0%)
- **Gaps:** BF-006 BF-007

### F4: Unknown

- **Coverage:** 0/11 findings tested (0%)
- **Gaps:** BF-025 BF-027 BF-029 BF-030 BF-031 BF-032 BF-033 BF-034 BF-035 BF-036 BF-038

### F5: Unknown

- **Coverage:** 0/13 findings tested (0%)
- **Gaps:** BF-040 BF-041 BF-042 BF-043 BF-044 BF-045 BF-046 BF-047 BF-048 BF-049 BF-050 BF-051 BF-052

### F6: Unknown

- **Coverage:** 0/22 findings tested (0%)
- **Gaps:** BF-053 BF-054 BF-055 BF-056 BF-057 BF-058 BF-059 BF-060 BF-061 BF-062 BF-063 BF-064 BF-065 BF-066 BF-067 BF-068 BF-072 BF-073 BF-075 BF-076 BF-077 BF-079

### F7: Unknown

- **Coverage:** 0/7 findings tested (0%)
- **Gaps:** BF-001 BF-069 BF-070 BF-071 BF-080 BF-081 BF-091

### F8: Unknown

- **Coverage:** 0/5 findings tested (0%)
- **Gaps:** BF-013 BF-014 BF-088 BF-089 BF-090

### F9: Unknown

- **Coverage:** 0/1 findings tested (0%)
- **Gaps:** BF-095

### F10: Unknown

- **Coverage:** 0/6 findings tested (0%)
- **Gaps:** BF-082 BF-083 BF-084 BF-086 BF-087 BF-108

### F11: Unknown

- **Coverage:** 0/7 findings tested (0%)
- **Gaps:** BF-096 BF-097 BF-098 BF-100 BF-101 BF-102 BF-107

### F12: Unknown

- **Coverage:** 0/2 findings tested (0%)
- **Gaps:** BF-105 BF-111

## High-Priority Gaps (F13.3 Edge Cases)

Per FRD F13.3, these findings require explicit test coverage before release:

### Conflict Detection (F2)
- Findings: BF-012 BF-016 BF-028 BF-112 BF-113
- **Required tests:** Half-open interval semantics, location AND resource overlap, exclude booking's own prior state, exclude deleted/denied bookings

### Approval Workflow (F3)
- Findings: BF-006 BF-007
- **Required tests:** Auto-approve on/off transitions, one-way approve/deny (no re-toggling), permission-gated actions

### Recurring Booking Scoping (F1.3)
- Findings: BF-011 BF-015 BF-017
- **Required tests:** Repeat-loop creates independent siblings, no series-aware edit/delete, repeatno upper bound

### Permission Matrix (F7)
- Findings: BF-001 BF-069 BF-070 BF-071 BF-080 BF-081 BF-091
- **Required tests:** 17-flag inventory coverage, deny-by-default for unconfirmed flags, Tier1/Tier2 consistency

## Open Questions Requiring Test Stubs

Per F13.5, these Open Questions must have a test added when resolved:

BF-028 BF-038 BF-058 BF-062 BF-065 BF-067 BF-071 BF-076 BF-089 BF-102 BF-107 BF-112 BF-113

See .planning/phases/01-legacy-functional-audit/open-questions.md for details.

---
*Run `bash scripts/traceability/analyze-coverage.sh` after adding tests to update this report.*
