---
phase: 1
status: issues_found
blockers: 0
warnings: 2
files_reviewed: 10
files_reviewed_list:
  - .planning/PROJECT.md
  - .planning/STATE.md
  - .planning/phases/01-legacy-functional-audit/baseline-inventory.md
  - .planning/phases/01-legacy-functional-audit/findings/01-booking-core.md
  - .planning/phases/01-legacy-functional-audit/findings/02-reference-data.md
  - .planning/phases/01-legacy-functional-audit/findings/03-custom-fields.md
  - .planning/phases/01-legacy-functional-audit/findings/04-identity-access.md
  - .planning/phases/01-legacy-functional-audit/findings/05-platform-settings.md
  - .planning/phases/01-legacy-functional-audit/legacy-audit-findings.md
  - .planning/phases/01-legacy-functional-audit/open-questions.md
reviewed_at: 2026-10-06T16:57:57Z
iteration: 1
---

# Phase 1 Code Review

## BLOCKERs

None. This is a documentation-only phase; no application code, no build/test gate, no security surface. I spot-checked a representative sample of citations directly against the live `neokoenig/RoomBooking` GitHub repository (network access available) — every citation checked (`Bookings.cfc::check()`'s incomplete overlap query, `Event.cfc::checkDates()`'s `DateCompare EQ -1` zero-duration gap, `Api.cfc`'s `f_isValidAPIRequest`/`allowAPI` split-enforcement, `Locations.cfc`'s permission filter stack, the `permissions` seed-data role grants) matched the findings exactly, including subtle details (e.g., the `check()` query genuinely never references `params.end`, confirming the claimed overlap-test asymmetry is real, not a misreading). No fabricated findings, no silently-dropped Open Questions, no misattributed citations found. All 10 PRD Section 10 seed questions appear in `open-questions.md` with an explicit RESOLVED-or-OPEN verdict and "unknown because..." text where OPEN. Verbatim-preservation of Wave 1 findings into the consolidated `legacy-audit-findings.md` was diff-checked on two sections (Bookings controller, Customfield model) and matched exactly aside from header restructuring.

## WARNINGs

### W1: `baseline-inventory.md`'s own summary footer misstates its Open-row count (13 claimed, 11 actual)
- **File:** `.planning/phases/01-legacy-functional-audit/baseline-inventory.md:127-134`
- **Evidence:** The closing note states "*Total: 111 rows (BF-001 through BF-111)... **13 rows carry Status=Open**, corresponding to the 13 OPEN items in `open-questions.md`*". A direct count (`grep -c "| Open |"`) over the table yields exactly **11** rows with `Status=Open` (BF-028, BF-038, BF-058, BF-062, BF-065, BF-067, BF-071, BF-076, BF-089, BF-102, BF-107), not 13. The 13-OPEN-items figure is correct for `open-questions.md` (verified: 13 items carry `**Status:** OPEN`), but two OPEN open-questions.md items have no corresponding `Open`-status row anywhere in `baseline-inventory.md`:
  - Item #2 (conflict hard-block-vs-soft-warning forward design decision) — the only baseline rows touching this topic (BF-012, BF-016) are both marked `Confirmed`, because the legacy *fact* is confirmed even though the forward decision is open; no row surfaces the open forward-decision itself.
  - Item #3 (multi-resource conflict aggregation semantics) — the only related rows (BF-017, BF-028) are `Confirmed`/`Open` respectively but BF-028 covers a different sub-finding (the admin-only gate on `checkavailability`, open-questions.md item #15), not the aggregation-semantics ambiguity of item #3. Item #3 itself has no baseline-inventory row at all.
  - This means F13 (which is explicitly told, per this same plan's own design in 01-06-PLAN.md, to treat `baseline-inventory.md` as "one row per finding... every cited finding, resolved or not") will not see dedicated traceability rows for Open Questions #2's forward-decision and #3's ambiguity when seeding its matrix — only the undisputed legacy facts are traced, not the live ambiguities themselves.
- **Fix direction:** Either correct the footer's count to 11 (if the mismatch is intentional/explained), or add the two missing baseline-inventory rows for open-questions.md items #2 and #3 so the count is truly 13 and F13's traceability seed doesn't silently omit two of the genuinely-open items alongside the eleven it does carry.
- **Resolution:** fixed (89101a0) — added BF-112 (open-questions.md #2, conflict-enforcement forward decision) and BF-113 (open-questions.md #3, multi-resource aggregation ambiguity) as dedicated Open rows, and corrected the footer to "113 rows (BF-001 through BF-113)" / "13 rows carry Status=Open." `grep -c "| Open |"` now returns 13, matching the footer.

### W2: PROJECT.md's uncorrected "zero-duration bookings rejected" Key Decision row contradicts the audit's own explicit correction, with no cross-reference
- **File:** `.planning/PROJECT.md:73`; contradicted by `.planning/phases/01-legacy-functional-audit/legacy-audit-findings.md:798-804` and `baseline-inventory.md:32` (BF-020)
- **Evidence:** PROJECT.md's Key Decisions table still reads: *"Booking time-range validation precision: `end_time` must be strictly after `start_time` (zero-duration bookings rejected)... Confirmed — not deferred to F0, as this is a precision fix rather than a legacy-behavior ambiguity"*. But `findings/01-booking-core.md` (and its verbatim copy into `legacy-audit-findings.md`) explicitly states: *"PROJECT.md states 'end_time must be strictly after start_time' with zero-duration bookings rejected. The actual code only checks `DateCompare(...) EQ -1` (strictly before) — it does not reject `DateCompare(...) EQ 0` (end exactly equal to start). A zero-duration booking... therefore passes this validation in the legacy system; PROJECT.md's 'zero-duration rejected' sub-claim is not supported by the cited code **and should be corrected in the consolidation pass**."* `baseline-inventory.md`'s BF-020 row independently repeats this same correction. 01-06-PLAN.md's Task 3 scope explicitly restricted which two Key Decision rows it was authorized to touch (conflict-enforcement and public-feed-access only) — this third, Wave-1-flagged correction was out of that plan's edit scope, so it was never applied, and PROJECT.md's own "Y0 schema CHECK constraint" framing for this row is arguably about the *new* system's intended behavior rather than a legacy-parity claim — but the row's rationale column still frames it as "not deferred to F0, as this is a precision fix rather than a legacy-behavior ambiguity," which is now directly contradicted by Wave 1's own evidence that this *is* a legacy-behavior correction (the legacy system accepts zero-duration bookings). No cross-reference to the audit's correction was added anywhere in PROJECT.md, STATE.md, or open-questions.md — a reader of PROJECT.md alone would not discover the contradiction.
- **Fix direction:** This is a narrow process gap, not a fabrication: 01-06-PLAN.md simply never scoped this row for update. Recommend either (a) adding this as a fourth row to open-questions.md (or a sub-note under the existing baseline-inventory BF-020 entry) with an explicit pointer back to PROJECT.md's Key Decisions row so the contradiction is discoverable, or (b) a follow-up plan task updating PROJECT.md's rationale/outcome column to acknowledge the audit found the legacy system does NOT reject zero-duration bookings, while still allowing the new system to deliberately diverge (stricter) as a named design choice rather than an implied legacy fact.
- **Resolution:** fixed (2a5031a) — applied fix direction (b): updated PROJECT.md's Outcome column for this row to state the audit's correction explicitly (legacy's `Event.cfc: checkDates()` accepts zero-duration bookings; only `DateCompare EQ -1` is rejected) and reframed the Y0 schema's stricter CHECK as a deliberate, named new-system divergence rather than an implied legacy-parity fact, with a citation to baseline-inventory.md BF-020 and findings/01-booking-core.md.

## Cross-file seams checked

- `legacy-audit-findings.md` H2 section headers ↔ 01-06-PLAN.md's required list (12 controllers + 7 model groups + 3 cross-cutting sections) — OK, all 22 sections present and correctly named/ordered.
- `legacy-audit-findings.md` content ↔ source `findings/01-05` content (verbatim-preservation claim) — OK, diff-checked on Bookings controller section and Customfield model section; byte-identical aside from heading restructuring and separator lines.
- `open-questions.md` items 1–10 ↔ PRD-BookingHub.md Section 10 seed questions 1–10 — OK, each of the 10 seed questions has a corresponding numbered item in the same order with an explicit RESOLVED/OPEN verdict and citation.
- `baseline-inventory.md` BF-xxx rows ↔ `open-questions.md` OPEN items — **see W1**: 11 of 13 OPEN items have a corresponding Open-status baseline row; items #2 (forward decision) and #3 (aggregation ambiguity) have no dedicated Open row, though the underlying legacy facts they rest on are separately traced as Confirmed rows.
- `baseline-inventory.md` Maps-To-Feature column ↔ REQUIREMENTS.md F0–F13 sub-feature IDs — OK, spot-checked F1.1, F1.3, F1.4, F2.1, F6.1-F6.5, F7.1-F7.4, F8.1-F8.4, F9.1-F9.6, F10.1-F10.4, F11.1-F11.4, F12.3, F0.4 all exist as real sub-IDs in REQUIREMENTS.md.
- PROJECT.md Key Decisions rows (conflict-enforcement, public-feed-access) ↔ `open-questions.md` items #2 and #7 — OK for #7 (fully corrected with citation, matches findings/05's verdict exactly). For #2, OK as far as scoped (correctly left the forward-decision open with a cross-reference to open-questions.md #2, per 01-06-PLAN.md Task 3's explicit instruction not to invent a resolution).
- PROJECT.md's third, out-of-scope Key Decisions row (zero-duration bookings) ↔ Wave 1's explicit correction in findings/01-booking-core.md — **see W2**: not updated, no cross-reference added, silently inconsistent with the audit's own finding (process gap from 01-06-PLAN.md's narrow edit scope, not a fabrication).
- STATE.md Blockers/Concerns ↔ actual open-questions.md resolved/open counts — OK, STATE.md correctly states "12 RESOLVED... 13 remain OPEN" matching the verified `open-questions.md` status-line counts (12 RESOLVED, 13 OPEN).
- Citations in findings/01, /02, /05 ↔ live `neokoenig/RoomBooking` GitHub source — OK, spot-checked 5 distinct citations across 3 different controllers/models; all matched the live source exactly, including subtle asymmetric-query and permission-filter-ordering details.
