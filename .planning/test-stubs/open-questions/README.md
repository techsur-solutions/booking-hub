# Test Stubs for Open Questions

This directory contains test stub templates for the 13 OPEN questions from Phase 1's `open-questions.md`.

**Purpose:** When a question is resolved, copy the relevant stub, fill in decision-specific logic, and move to the appropriate test directory.

**Status of 13 Open Questions:**

| ID | Question | Stub File | Destination on Resolution |
|----|----------|-----------|---------------------------|
| OQ-02 | Conflict enforcement (hard-block vs soft-warning) | OQ-02-conflict-enforcement.md | services/booking-service/.../ConflictEnforcementTest.java |
| OQ-03 | Multi-resource conflict scope | OQ-03-multi-resource-conflict.md | services/booking-service/.../MultiResourceConflictTest.java |
| OQ-14 | Location/Resource deletion orphan-allow vs guard | OQ-other-stubs.md | services/locations-resources-service/.../DeletionPolicyTest.java |
| OQ-15 | Resource checkavailability permission gate | OQ-other-stubs.md | services/locations-resources-service/.../ResourceAvailabilityTest.java |
| OQ-17 | Session timeout values | OQ-other-stubs.md | Keycloak realm config + test |
| OQ-18 | Permission change cache invalidation | OQ-other-stubs.md | services/users-permissions-service/.../PermissionCacheTest.java |
| OQ-19 | Password reset token re-check at submit | OQ-other-stubs.md | services/users-permissions-service/.../PasswordResetTokenTest.java |
| OQ-20 | Email enumeration on password reset | OQ-other-stubs.md | services/users-permissions-service/.../PasswordResetSecurityTest.java |
| OQ-21 | Email uniqueness collation (case sensitivity) | OQ-other-stubs.md | services/users-permissions-service/.../UserEmailUniquenessTest.java |
| OQ-22 | Assume-user admin-only enforcement | OQ-other-stubs.md | services/users-permissions-service/.../AssumeUserTest.java |
| OQ-23 | Event/Eventresource DB column list unconfirmed | OQ-other-stubs.md | services/audit-log-service/.../AuditLogTypeTest.java |
| OQ-24 | Error email feature unimplemented or out-of-scope | OQ-other-stubs.md | services/*/error-handling tests or docs |
| OQ-25 | `ajax` log type write-site not located | OQ-other-stubs.md | services/audit-log-service/.../AuditLogTypeTest.java |

*Note: OQ IDs map to open-questions.md item numbers directly (OQ-02 = item 2, OQ-14 = item 14, etc.). OQ-07 (Feed access gating) is omitted — item #7 is RESOLVED in open-questions.md; the OQ-07-feed-access-gating.md stub file documents the nuance for test coverage purposes.*

## Reference: open-questions.md item → OQ mapping

The 13 OPEN items in open-questions.md (items 2, 3, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25) are mapped here as:
- OQ-02 = item 2 (Conflict enforcement)
- OQ-03 = item 3 (Multi-resource conflict)
- OQ-14 = item 14 (Location/Resource deletion policy)
- OQ-15 = item 15 (checkavailability permission gate)
- OQ-17 = item 17 (Session timeout)
- OQ-18 = item 18 (Permission-change cache)
- OQ-19 = item 19 (Password reset token re-check)
- OQ-20 = item 20 (Email enumeration inconsistency)
- OQ-21 = item 21 (Email uniqueness collation)
- OQ-22 = item 22 (Admin-cannot-assume-admin server enforcement)
- OQ-23 = item 23 (Event/Eventresource DB column list)
- OQ-24 = item 24 (Error email feature)
- OQ-25 = item 25 (ajax log type write-site)

## Usage

1. Product owner resolves an Open Question (e.g., OQ-02)
2. Copy the stub file: `cp .planning/test-stubs/open-questions/OQ-02-conflict-enforcement.md /tmp/stub.md`
3. Read the stub's "Decision-Specific Logic" section
4. Implement the test in the destination service/test directory
5. Follow the Open Question Resolution Protocol (`docs/testing/open-question-resolution-protocol.md`)
6. Update traceability matrix with `test_ref` column pointing to the new test
7. Delete or archive the stub (it's fulfilled)

## Related Files

- Protocol: `docs/testing/open-question-resolution-protocol.md`
- Open Questions source: `.planning/phases/01-legacy-functional-audit/open-questions.md`
- Traceability matrix: `.planning/traceability-matrix.csv`
- Test writing guide: `docs/testing/test-writing-guide.md`
