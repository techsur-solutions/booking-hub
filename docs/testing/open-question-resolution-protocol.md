# Open Question Resolution Protocol (F13.5)

**Rule:** No Open Question may be marked RESOLVED without a corresponding test being added.

## Background

Phase 1's legacy functional audit identified 25 Open Questions (see `.planning/phases/01-legacy-functional-audit/open-questions.md`):
- 12 questions RESOLVED with direct code citation
- 13 questions OPEN pending product/architecture decision

Per FRD F13.5 and PRD Section 7 release criteria: *"Every F0 Open Question that gets resolved has its decision recorded and a corresponding test added — a resolution without a new test is rejected."*

This protocol ensures decisions are verified, not just documented.

## When to Use This Protocol

**Trigger:** Product owner or architect makes a decision on any OPEN question from `open-questions.md`.

**Examples:**
- OQ #2: Conflict enforcement (hard block for all users vs. legacy's always-non-blocking behavior)
- OQ #3: Multi-resource conflict scope (how multiple resources aggregate into booking-level conflict verdict)
- OQ #7: Feed access gating (token-required vs. fully public)

## Resolution Workflow

### Step 1: Record the Decision

**Location:** `.planning/phases/01-legacy-functional-audit/open-questions.md`

Update the question's status block:

```markdown
## N. [Question title]

**Status:** RESOLVED (by [decision maker], [date])

**Decision:** [Clear statement of chosen behavior]

**Rationale:** [Why this choice over alternatives]

**Finding:** [Original unknown-because context preserved]

**Citation:** [Original source reference preserved]
```

**Example:**
```markdown
## 2. Conflict detection enforcement hard-block-vs-soft-warning

**Status:** RESOLVED (by Product Owner, 2026-10-15)

**Decision:** The new system will hard-block ALL users (including those with allowApproveBooking) when a location/resource conflict is detected. This is a deliberate improvement over the legacy's always-non-blocking behavior, prioritizing data integrity over convenience.

**Rationale:** Survey of similar booking systems shows hard-block is industry standard; non-blocking conflicts caused double-booking incidents in legacy deployment per stakeholder feedback.

**Finding:** [original finding preserved]

**Citation:** [original citation preserved]
```

### Step 2: Identify Required Test Coverage

**Question to answer:** What test would FAIL if someone violated this decision?

**Map to test scenarios:**

| Decision | Required Test |
|----------|---------------|
| "Hard block all users on conflict" | User WITH allowApproveBooking + overlapping booking → 409 Conflict (not 201 Created) |
| "Token required for feeds" | GET /feeds/rss2 without token header → 401 Unauthorized (not 200 OK with data) |
| "Multi-resource: ANY conflict blocks whole booking" | Booking with 3 resources, 1 conflicts → entire create rejected (not partial success) |

**Use the test-writing guide:** See `docs/testing/test-writing-guide.md` for patterns.

### Step 3: Write the Test

**Backend example (OQ #2 — conflict enforcement):**

File: `services/booking-service/src/test/java/com/bookinghub/booking/ConflictEnforcementTest.java`

```java
@SpringBootTest
@AutoConfigureMockMvc
class ConflictEnforcementTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createBooking_withConflict_blocksEvenForApprovers() throws Exception {
        // GIVEN: existing booking 10:00-11:00 in locationA
        // AND: user has allowApproveBooking permission
        // WHEN: create overlapping booking 10:30-11:30 in same location
        mockMvc.perform(post("/api/bookings")
                .with(jwt().authorities(
                    new SimpleGrantedAuthority("SCOPE_accessCalendar"),
                    new SimpleGrantedAuthority("SCOPE_allowRoomBooking"),
                    new SimpleGrantedAuthority("SCOPE_allowApproveBooking")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(conflictingBookingJson))
            // THEN: request is rejected with 409 Conflict (not 201 Created)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error_code").value("BOOKING_CONFLICT"));

        // Verify no booking was created
        assertThat(bookingRepository.findAll()).hasSize(1); // only the original
    }
}
```

**Frontend example (if UI behavior differs):**

File: `frontend/e2e/conflict-enforcement.spec.ts`

```typescript
test('conflicting booking shows error even for approvers', async ({ page }) => {
  // Given: logged in as approver, existing booking visible in calendar
  await page.goto('/bookings/calendar');

  // When: try to create overlapping booking
  await page.click('[data-testid="create-booking"]');
  await fillBookingForm(page, { location: 'Room A', start: '10:30', end: '11:30' });
  await page.click('button[type="submit"]');

  // Then: error message shown (not success)
  await expect(page.locator('.error-message')).toContainText('conflict');
  await expect(page.locator('.success-message')).not.toBeVisible();
});
```

### Step 4: Update Traceability Matrix

File: `.planning/traceability-matrix.csv`

Find the Open Question finding (grep for "Open Question"):

```bash
grep 'Open Question' .planning/traceability-matrix.csv
# BF-028,F2.1,,,not_started,Open Question - see open-questions.md
```

Update with test reference:

```csv
BF-028,F2.1,services/booking-service/.../ConflictDetectionEngine.java,ConflictEnforcementTest::createBooking_withConflict_blocksEvenForApprovers,tested,OQ#2 resolved: hard-block all users
```

Run coverage analysis to verify update:

```bash
bash scripts/traceability/analyze-coverage.sh
grep 'BF-028' docs/testing/test-gap-report.md
# Should now show "tested" status
```

### Step 5: Verify in CI

1. Push test to feature branch
2. CI runs regression suite
3. Verify test passes: confirms decision is correctly implemented
4. If test fails: implementation doesn't match decision — fix before merging

### Step 6: Code Review Checklist

Reviewer must confirm:
- [ ] Open Question status updated to RESOLVED in `open-questions.md`
- [ ] Decision and rationale clearly documented
- [ ] Test exists and covers the decision (would fail if violated)
- [ ] Test passes in CI
- [ ] Traceability matrix updated with test reference
- [ ] Coverage analysis re-run (gap report updated)

**Reject if:** Decision is documented but test is missing/incomplete.

## Test Stub Templates

Pre-written stubs for all 13 open questions exist at `.planning/test-stubs/open-questions/`.

When resolving a question, copy the relevant stub, fill in the decision-specific logic, and move to the appropriate test directory.

## Monitoring Open Question Progress

**Count remaining open questions:**
```bash
grep -c 'Status: OPEN' .planning/phases/01-legacy-functional-audit/open-questions.md
```

**List open findings in traceability matrix:**
```bash
bash scripts/traceability/query-matrix.sh open-questions
```

**Phase 8 is complete when:**
- All 13 OPEN questions are either:
  - RESOLVED with test added (ideal), OR
  - Explicitly deferred to v2 with product owner sign-off (exceptional)
- Traceability matrix shows 100% tested status for all non-deferred findings

## What NOT to Do

❌ **Do not:** Mark a question RESOLVED and write test later
- Test is PART OF resolution, not a follow-up task

❌ **Do not:** Write test that always passes regardless of decision
- Test must exercise the actual decision logic

❌ **Do not:** Resolve by "we'll keep legacy behavior" without test
- Legacy behavior needs verification too — prevents silent divergence

❌ **Do not:** Defer to v2 without explicit product owner approval
- Deferral = shipping with known unknowns, requires conscious choice

## The 13 Open Questions

For quick reference, the 13 questions still requiring resolution are:

| # | Question | Key Decision Needed |
|---|----------|---------------------|
| OQ-02 | Conflict enforcement: hard-block vs soft-warning vs always-allow | Choose enforcement policy |
| OQ-03 | Multi-resource conflict scope: block whole booking or per-resource | Choose aggregation behavior |
| OQ-14 | Location/Resource deletion: block, cascade, or allow orphans | Choose deletion policy |
| OQ-15 | `checkavailability` permission gate: admin-only vs. any booking user | Fix/clarify permission scope |
| OQ-17 | Session timeout duration | Set explicit timeout values |
| OQ-18 | Permission-change cache invalidation: restart vs. immediate | Choose cache strategy |
| OQ-19 | Password-reset token re-check at submit | Close or accept legacy gap |
| OQ-20 | Email enumeration: consistent generic vs. explicit "not found" | Choose messaging policy |
| OQ-21 | Email uniqueness collation: case-sensitive vs. case-insensitive | Choose collation policy |
| OQ-22 | Admin-cannot-assume-admin: UI-only vs. server-enforced | Enforce server-side or accept gap |
| OQ-23 | Event/Eventresource DB column list unconfirmed | Confirm schema or define fresh |
| OQ-24 | Error email feature: unimplemented or out-of-scope | Confirm and document |
| OQ-25 | `ajax` log type write-site not located | Confirm or decide not to replicate |

*(Note: open-questions.md uses sequential numbering 1–25; the 13 OPEN items are items 2, 3, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25 — OQ-14 through OQ-25 map to open-questions.md items 14–25 skipping the RESOLVED items.)*

## Examples from History

**Good resolution (hypothetical OQ #2):**
1. Decision: Hard-block all users
2. Test: `ConflictEnforcementTest::createBooking_withConflict_blocksEvenForApprovers`
3. Matrix: BF-028 status → tested
4. Result: Decision is verifiable, regression-protected

**Bad resolution:**
1. Decision: Hard-block all users
2. Test: *missing*
3. Matrix: BF-028 status → not_started
4. Result: 6 months later, someone implements soft-warning by mistake, no test catches it

---
*This protocol is the enforcement mechanism for "no functionality lost via assumption."*
