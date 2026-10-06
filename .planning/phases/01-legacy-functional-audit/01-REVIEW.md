---
phase: 1
status: issues_found
blockers: 0
warnings: 1
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
reviewed_at: 2026-10-06T17:02:24Z
iteration: 2
---

# Phase 1 Code Review

## BLOCKERs

None.

## WARNINGs

### W1 (iteration-1): VERIFIED FIXED — baseline-inventory.md footer count mismatch
- **Fix commit:** 89101a0
- **Verification:** `grep -c "| Open |" baseline-inventory.md` now returns exactly **13**, matching the footer's claim. Two new rows were added — BF-112 (mapped to open-questions.md item #2, conflict-enforcement forward decision) and BF-113 (mapped to item #3, multi-resource aggregation ambiguity). I read both new rows in full and cross-checked their description text against open-questions.md items #2 and #3 verbatim: BF-112 correctly summarizes "legacy's conflict-detection is unconditionally non-blocking... whether the NEW system should introduce a hard-block... is an open product decision" (matches open-questions.md:54-58 "Unknown because" text exactly in substance). BF-113 correctly summarizes the per-resource `checkavailability` aggregation ambiguity (matches open-questions.md:69-81 exactly in substance). Total unique `BF-\d+` IDs in the file = 113, contiguous BF-001 through BF-113, no gaps or duplicates. Footer text updated to "113 rows... 13 rows carry Status=Open" with an explanatory parenthetical citing this review's W1 finding. Table structure intact — every one of the 113 data rows has 8 pipe-delimited fields, including the 2 new rows. **Closed, no regression.**

### W2 (iteration-1): VERIFIED FIXED — PROJECT.md zero-duration Key Decision contradiction
- **Fix commit:** 2a5031a
- **Verification:** Read the full updated row at PROJECT.md:73. The Outcome column now reads "Deliberate divergence from legacy, confirmed as a NEW-system design choice, not a legacy-parity claim — F0 audit found the legacy system's own `Event.cfc: checkDates()` only tests `DateCompare(...) EQ -1`... so legacy actually ACCEPTS zero-duration bookings (see baseline-inventory.md BF-020, findings/01-booking-core.md)." This directly acknowledges the contradiction flagged in iteration 1 rather than silently asserting "precision fix, not a legacy-behavior ambiguity." Cross-checked the BF-020 citation: `baseline-inventory.md:32` reads "end must not be strictly before start (DateCompare EQ -1); zero-duration (end==start) PASSES validation — corrects PROJECT.md's 'zero-duration rejected' claim" — citation is accurate, not fabricated. Cross-checked findings/01-booking-core.md:325 and legacy-audit-findings.md:799 — both still contain the original Wave-1 correction text, now consistent with (not contradicted by) PROJECT.md's updated Outcome column. Table structure verified intact: row still has exactly 4 pipe characters (3 columns), matching every other row in the table — the fix's long inserted text contains no literal `|` characters that would have broken the Markdown table. Grepped repo-wide for the old contradictory phrase ("not deferred to F0, as this is a precision fix") — zero remaining occurrences anywhere in `.planning/`. **Closed, no regression in the fixed file itself.**

### W3 (new, introduced by W1's fix): STATE.md's row-count reference is now stale
- **File:** `.planning/STATE.md:96`
- **Evidence:** STATE.md's F0 sign-off gate bullet reads: *"`baseline-inventory.md` (F13 seed, **111 rows**) are complete and handed off."* This was accurate at iteration-1 time (baseline-inventory.md genuinely had 111 rows then) but the W1 fix (commit 89101a0) added BF-112 and BF-113, bringing the real count to 113. STATE.md was not touched by either fix commit and still asserts the pre-fix figure. This is a minor, non-blocking documentation drift — STATE.md's row count is descriptive color in a historical sign-off bullet, not something any downstream phase parses or depends on for a count-match — but it is a fix-introduced inconsistency between two files that iteration 1 did not flag because it was accurate at the time.
- **Fix direction:** Update STATE.md:96's "111 rows" to "113 rows" to keep it consistent with the now-current baseline-inventory.md footer.

## Cross-file seams checked

- `baseline-inventory.md` footer Open-row count ↔ actual `| Open |` row count in the same file — **OK, now matches (13 = 13)**, see W1 verification above.
- `baseline-inventory.md` BF-112/BF-113 content ↔ `open-questions.md` items #2/#3 content — OK, read both pairs in full, descriptions are substantively accurate paraphrases, not fabrications or distortions.
- `baseline-inventory.md` total-row footer ("113 rows, BF-001 through BF-113") ↔ actual unique BF-ID count in table — OK, verified 113 unique IDs, no gaps/dupes.
- PROJECT.md's zero-duration Key Decision row ↔ `findings/01-booking-core.md`'s Wave-1 correction ↔ `baseline-inventory.md` BF-020 — OK, now mutually consistent; PROJECT.md explicitly cites BF-020 and the finding, and the cited BF-020 text matches what PROJECT.md claims it says.
- PROJECT.md zero-duration row's Markdown table structure (pipe/column count) pre- vs post-fix — OK, unchanged at 3 columns; no table breakage from the inserted long-form text.
- `legacy-audit-findings.md` / `findings/01-booking-core.md` zero-duration passages (untouched by either fix) ↔ PROJECT.md's updated row — OK, now consistent (previously contradicted, per iteration-1 W2).
- `baseline-inventory.md` row count ↔ `STATE.md`'s F0 sign-off bullet row count — **see W3**: STATE.md still says 111, actual is now 113 (fixer did not update STATE.md, which was out of scope for either fix commit but is now the one remaining stale reference).
- `01-06-SUMMARY.md`'s "111 rows" historical claim ↔ current baseline-inventory.md — not flagged as a finding: this is a point-in-time SUMMARY of work completed at iteration-1 time, read as historical record rather than a live-maintained cross-reference; distinguishing it from STATE.md (a living status document) on that basis.
- `open-questions.md` 25-item count / 13-OPEN / 12-RESOLVED breakdown — OK, unaffected by either fix, still verified at 25/13/12 exactly as iteration 1 found.
